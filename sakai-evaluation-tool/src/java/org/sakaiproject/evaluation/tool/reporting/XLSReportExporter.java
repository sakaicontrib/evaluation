/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.tool.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.reporting.CSVReportExporter.SortBySectionOrSiteComparator;
import org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class XLSReportExporter implements ReportExporter {

    private static final short QUESTION_CAT_ROW = 3; // Course, Instructor, etc
    private static final short QUESTION_TYPE_ROW = 4;
    private static final short QUESTION_TEXT_ROW = 5;
    private static final short FIRST_ANSWER_ROW = 6;
    private static final short SECTION_OR_SITE_COLUMN_NUM = 0;
    private static final short RESPONSE_ID_COLUMN_NUM = 1;
    private static final short INSTRUCTOR_ID_COLUMN_NUM = 2;
    private static final short INSTRUCTOR_FIRST_NAME_COLUMN_NUM = 3;
    private static final short INSTRUCTOR_LAST_NAME_COLUMN_NUM = 4;
    private static final short QUESTION_COMMENTS_COLUMN_START_INDEX_INSTRUCTOR_SHEET = 5;
    private static final short QUESTION_COMMENTS_COLUMN_START_INDEX_COURSE_SHEET = 2;

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalResponseAggregatorUtil responseAggregator;
    public void setEvalResponseAggregatorUtil(EvalResponseAggregatorUtil bean) {
        this.responseAggregator = bean;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator locator) {
        this.messageLocator = locator;
    }

    private UserDirectoryService userDirectoryService;
    public void setUserDirectoryService( UserDirectoryService userDirectoryService )
    {
        this.userDirectoryService = userDirectoryService;
    }

    CellStyle dateCellStyle;
    CreationHelper creationHelper;

    /**
     * Build the .xls report in the new (section based) format.
     * 
     * @param evaluation
     * @param groupIDs
     * @param outputStream
     */
    private void buildReportSectionAware( EvalEvaluation evaluation, String[] groupIDs, OutputStream outputStream )
    {
        // Get permission to view, current user and eval owner
        Boolean instructorViewAllResults = evaluation.getInstructorViewAllResults();
        String currentUserId = commonLogic.getCurrentUserId();
        String evalOwner = evaluation.getOwner();

        TemplateItemDataList tidl = getEvalTIDL( evaluation, groupIDs );
        List<DataTemplateItem> dtiList = tidl.getFlatListOfDataTemplateItems( true );
        Workbook wb = new XSSFWorkbook();
        creationHelper = wb.getCreationHelper();

        // Title style
        Sheet courseSheet = wb.createSheet( messageLocator.getMessage( "viewreport.xls.courseSheet.name" ) );
        Sheet instructorSheet = wb.createSheet( messageLocator.getMessage( "viewreport.xls.instructorSheet.name" ) );
        Font font = wb.createFont();
        font.setFontHeightInPoints( (short) 12 );
        font.setBoldweight( Font.BOLDWEIGHT_BOLD );
        CellStyle mainTitleStyle = wb.createCellStyle();
        mainTitleStyle.setFont( font );

        // Bold header style
        font = wb.createFont();
        font.setFontHeightInPoints( (short) 10 );
        font.setBoldweight( Font.BOLDWEIGHT_BOLD );
        CellStyle boldHeaderStyle = wb.createCellStyle();
        boldHeaderStyle.setFont( font );

        // Italic meta header style
        font = wb.createFont();
        font.setFontHeightInPoints( (short) 10 );
        font.setItalic( true );
        CellStyle italicMiniHeaderStyle = wb.createCellStyle();
        italicMiniHeaderStyle.setFont( font );

        // Date meta Style
        dateCellStyle = wb.createCellStyle();
        dateCellStyle.setDataFormat( (short) 0x16 );

        // Evaluation Title
        int rowCounter = 0;
        Row courseSheetRow1 = courseSheet.createRow( rowCounter );
        Row instructorSheetRow1 = instructorSheet.createRow( rowCounter );
        Cell courseSheetCellA1 = courseSheetRow1.createCell( (short) 0 );
        Cell instructorSheetCellA1 = instructorSheetRow1.createCell( (short) 0 );
        setPlainStringCell( courseSheetCellA1, evaluation.getTitle() + " - " + messageLocator.getMessage( "viewreport.xls.courseSheet.name" ) );
        setPlainStringCell( instructorSheetCellA1, evaluation.getTitle() + " - " + messageLocator.getMessage( "viewreport.xls.instructorSheet.name" ) );
        courseSheetCellA1.setCellStyle( mainTitleStyle );
        instructorSheetCellA1.setCellStyle( mainTitleStyle );

        // Calculate the response rate
        rowCounter++;
        int responsesCount = evaluationService.countResponses( null, new Long[] { evaluation.getId() }, groupIDs, null );
        int enrollmentsCount = evaluationService.countParticipantsForEval( evaluation.getId(), groupIDs );
        Row courseSheetRow2 = courseSheet.createRow( rowCounter );
        Row instructorSheetRow2 = instructorSheet.createRow( rowCounter );
        Cell courseSheetCellA2 = courseSheetRow2.createCell( (short) 0 );
        Cell instructorSheetCellA2 = instructorSheetRow2.createCell( (short) 0 );
        courseSheetCellA2.setCellStyle( boldHeaderStyle );
        instructorSheetCellA2.setCellStyle( boldHeaderStyle );
        setPlainStringCell( courseSheetCellA2, EvalUtils.makeResponseRateStringFromCounts( responsesCount, enrollmentsCount ) );
        setPlainStringCell( instructorSheetCellA2, EvalUtils.makeResponseRateStringFromCounts( responsesCount, enrollmentsCount ) );

        // Dates
        setPlainStringCell( courseSheetRow1.createCell( (short) 2 ), messageLocator.getMessage( "evalsettings.start.date.header" ) );
        setPlainStringCell( instructorSheetRow1.createCell( (short)  2 ), messageLocator.getMessage( "evalsettings.start.date.header" ) );
        setDateCell( courseSheetRow2.createCell( (short) 2 ), evaluation.getStartDate() );
        setDateCell( instructorSheetRow2.createCell( (short)  2 ), evaluation.getStartDate() );
        if( evaluation.getDueDate() != null )
        {
            setPlainStringCell( courseSheetRow1.createCell( (short) 3 ), messageLocator.getMessage( "evalsettings.due.date.header" ) );
            setPlainStringCell( instructorSheetRow1.createCell( (short) 3), messageLocator.getMessage( "evalsettings.due.date.header" ) );
            setDateCell( courseSheetRow2.createCell( (short) 3 ), evaluation.getDueDate() );
            setDateCell( instructorSheetRow2.createCell( (short) 3), evaluation.getDueDate() );
        }

        // List of groups
        if( groupIDs.length > 0 )
        {
            rowCounter++;
            Row courseSheetRow3 = courseSheet.createRow( rowCounter );
            Row instructorSheetRow3 = instructorSheet.createRow( rowCounter );
            Cell courseSheetCellA3 = courseSheetRow3.createCell( (short) 0 );
            Cell instructorSheetCellA3 = instructorSheetRow3.createCell( (short) 0 );

            // Get the section/site titles
            setPlainStringCell( courseSheetCellA3, messageLocator.getMessage( "reporting.xls.participants",
                    new Object[] { responseAggregator.getCommaSeparatedGroupNames( groupIDs ) } ) );
            setPlainStringCell( instructorSheetCellA3, messageLocator.getMessage( "reporting.xls.participants",
                    new Object[] { responseAggregator.getCommaSeparatedGroupNames( groupIDs ) } ) );
        }

        // Column headers (static)
        rowCounter += 2;
        short courseSheetHeaderCount = 1;
        short instructorSheetHeaderCount = 1;
        Row courseSheetHeaderRow = courseSheet.createRow( rowCounter );
        Row instructorSheetHeaderRow = instructorSheet.createRow( rowCounter );
        Cell courseSheetSectionHeaderCell = courseSheetHeaderRow.createCell( courseSheetHeaderCount++ );
        Cell instructorSheetSectionHeaderCell = instructorSheetHeaderRow.createCell( instructorSheetHeaderCount++ );
        if( evaluation.getSectionAwareness() )
        {
            courseSheetSectionHeaderCell.setCellValue( messageLocator.getMessage( "viewreport.section.header" ) );
            instructorSheetSectionHeaderCell.setCellValue( messageLocator.getMessage( "viewreport.section.header" ) );
        }
        else
        {
            courseSheetSectionHeaderCell.setCellValue( messageLocator.getMessage( "viewreport.site.header" ) );
            instructorSheetSectionHeaderCell.setCellValue( messageLocator.getMessage( "viewreport.site.header" ) );
        }
        courseSheetSectionHeaderCell.setCellStyle( boldHeaderStyle );
        instructorSheetSectionHeaderCell.setCellStyle( boldHeaderStyle );
        Cell courseSheetResponseIdHeaderCell = courseSheetHeaderRow.createCell( courseSheetHeaderCount++ );
        Cell instructorSheetResponseIdHeaderCell = instructorSheetHeaderRow.createCell( instructorSheetHeaderCount++ );
        courseSheetResponseIdHeaderCell.setCellValue( messageLocator.getMessage( "viewreport.responseID.header" ) );
        instructorSheetResponseIdHeaderCell.setCellValue( messageLocator.getMessage( "viewreport.responseID.header" ) );
        courseSheetResponseIdHeaderCell.setCellStyle( boldHeaderStyle );
        instructorSheetResponseIdHeaderCell.setCellStyle( boldHeaderStyle );
        Cell instructorSheetInstructorIdHeaderCell = instructorSheetHeaderRow.createCell( instructorSheetHeaderCount++ );
        instructorSheetInstructorIdHeaderCell.setCellValue( messageLocator.getMessage( "viewreport.instructorID.header" ) );
        instructorSheetInstructorIdHeaderCell.setCellStyle( boldHeaderStyle );
        Cell instructorSheetFirstNameHeaderCell = instructorSheetHeaderRow.createCell( instructorSheetHeaderCount++ );
        instructorSheetFirstNameHeaderCell.setCellValue( messageLocator.getMessage( "viewreport.firstName.header" ) );
        instructorSheetFirstNameHeaderCell.setCellStyle( boldHeaderStyle );
        Cell instructorSheetLastNameHeaderCell = instructorSheetHeaderRow.createCell( instructorSheetHeaderCount++ );
        instructorSheetLastNameHeaderCell.setCellValue( messageLocator.getMessage( "viewreport.lastName.header" ) );
        instructorSheetLastNameHeaderCell.setCellStyle( boldHeaderStyle );

        // Generate dynamic question headers
        List<String> instructorRelatedQuestions = new ArrayList<>();
        for( DataTemplateItem dti : dtiList )
        {
            // Skip items that aren't for the current user
            if( isItemNotForCurrentUser( instructorViewAllResults, currentUserId, evalOwner, dti ) )
            {
                continue;
            }

            // If there's already a header for a specific instructor question, don't list it twice
            String questionText = commonLogic.makePlainTextFromHTML( dti.templateItem.getItem().getItemText() );
            if( instructorRelatedQuestions.contains( questionText ) )
            {
                continue;
            }

            // Add the header to the appropriate worksheet
            Cell questionTextHeaderCell;
            if( EvalConstants.ITEM_CATEGORY_ASSISTANT.equals( dti.associateType ) || EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals( dti.associateType ) )
            {
                instructorRelatedQuestions.add( questionText );
                questionTextHeaderCell = instructorSheetHeaderRow.createCell( instructorSheetHeaderCount++ );
                questionTextHeaderCell.setCellStyle( boldHeaderStyle );
                if( dti.usesComments() )
                {
                    setPlainStringCell( instructorSheetHeaderRow.createCell( instructorSheetHeaderCount++ ),
                            messageLocator.getMessage( "viewreport.comments.header" ) ).setCellStyle( italicMiniHeaderStyle );
                }
            }
            else
            {
                questionTextHeaderCell = courseSheetHeaderRow.createCell( courseSheetHeaderCount++ );
                questionTextHeaderCell.setCellStyle( boldHeaderStyle );
                if( dti.usesComments() )
                {
                    setPlainStringCell( courseSheetHeaderRow.createCell( courseSheetHeaderCount++ ),
                            messageLocator.getMessage( "viewreport.comments.header" ) ).setCellStyle( italicMiniHeaderStyle );
                }
            }
            setPlainStringCell( questionTextHeaderCell, questionText );
        }

        // Parse out the instructor and course related responeses into separate structures
        List<Long> responseIDs = tidl.getResponseIdsForAnswers();
        List<List<String>> courseRelatedResponses = new ArrayList<>();
        Map<Long, Map<User, List<EvalAnswer>>> answerMap = new HashMap<>();
        for( Long responseID : responseIDs )
        {
            // Dump the (course related) data into a list of strings representing a spreadsheet row (so it can be sorted)
            List<String> row = new ArrayList<>();
            String groupID = "";
            List<EvalAnswer> answers = tidl.getAnswersByResponseId( responseID );
            if( answers != null && !answers.isEmpty() )
            {
                groupID = answers.get( 0 ).getResponse().getEvalGroupId();
            }
            row.add( SECTION_OR_SITE_COLUMN_NUM, responseAggregator.getCommaSeparatedGroupNames( new String[] { groupID } ) );
            row.add( RESPONSE_ID_COLUMN_NUM, responseID.toString() );

            // Add the response ID to the answer map
            answerMap.put( responseID, new HashMap<>() );

            // Loop through the data template items...
            int questionCounter = 0;
            for( DataTemplateItem dti : dtiList )
            {
                // Skip items that aren't for the current user
                if( isItemNotForCurrentUser( instructorViewAllResults, currentUserId, evalOwner, dti ) )
                {
                    continue;
                }

                // If it's an instructor related item... 
                EvalAnswer answer = dti.getAnswer( responseID );
                if( EvalConstants.ITEM_CATEGORY_ASSISTANT.equals( dti.associateType ) || EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals( dti.associateType ) )
                {
                    // If the answer is NOT null (it would be null for an instructor from a different section than the evaluator)
                    if( answer != null )
                    {
                        // Get the instructor
                        User instructor;
                        try { instructor = userDirectoryService.getUser( answer.getAssociatedId() ); }
                        catch( UserNotDefinedException ex ) { continue; }

                        // If the answer map has a list of answers for this response and this instructor, add the answer to the list
                        Map<User, List<EvalAnswer>> responseAnswers = answerMap.get( responseID );
                        List<EvalAnswer> instructorAnswers = responseAnswers.get( instructor );
                        if( instructorAnswers != null )
                        {
                            instructorAnswers.add( answer );
                        }

                        // Otherwise, the answer map doesn't have a list of answers for this response and this instructor,
                        // create the list and add the answer to it
                        else
                        {
                            instructorAnswers = new ArrayList<>();
                            instructorAnswers.add( answer );
                            responseAnswers.put( instructor, instructorAnswers );
                        }
                    }
                }

                // If it's a course related item, just add it normally to the course worksheet
                else
                {    
                    row.add( QUESTION_COMMENTS_COLUMN_START_INDEX_COURSE_SHEET + questionCounter, "" );
                    if( answer != null )
                    {
                        row.set( QUESTION_COMMENTS_COLUMN_START_INDEX_COURSE_SHEET + questionCounter, responseAggregator.formatForSpreadSheet( answer.getTemplateItem(), answer ) );
                    }
                    if( dti.usesComments() )
                    {
                        row.add( QUESTION_COMMENTS_COLUMN_START_INDEX_COURSE_SHEET + ++questionCounter, StringUtils.trimToEmpty( answer.getComment() ) );
                    }
                    questionCounter++;
                }
            }

            // Add the course row data to the list of course data rows
            courseRelatedResponses.add( row );
        }

        // Convert the map structure of instructor responses into a List<List<String>>, representing rows of data
        List<List<String>> instructorRelatedResponses = new ArrayList<>();
        for( Long responseID : answerMap.keySet() )
        {
            // Loop through the instructors for the current response
            for( User instructor : answerMap.get( responseID ).keySet() )
            {
                // Dump the data into a list of strings representing a spreadsheet row (so it can be sorted)
                List<String> row = new ArrayList<>();
                row.add( SECTION_OR_SITE_COLUMN_NUM, "" );
                row.add( RESPONSE_ID_COLUMN_NUM, responseID.toString() );
                row.add( INSTRUCTOR_ID_COLUMN_NUM, "" );
                row.add( INSTRUCTOR_FIRST_NAME_COLUMN_NUM, "" );
                row.add( INSTRUCTOR_LAST_NAME_COLUMN_NUM, "" );
                if( instructor != null )
                {
                    row.set( INSTRUCTOR_ID_COLUMN_NUM, instructor.getDisplayId() );
                    row.set( INSTRUCTOR_FIRST_NAME_COLUMN_NUM, instructor.getFirstName() );
                    row.set( INSTRUCTOR_LAST_NAME_COLUMN_NUM, instructor.getLastName() );
                }

                int questionCounter = 0;
                for( EvalAnswer answer : answerMap.get( responseID ).get( instructor ) )
                {
                    row.set( SECTION_OR_SITE_COLUMN_NUM, responseAggregator.getCommaSeparatedGroupNames( new String[] { answer.getResponse().getEvalGroupId() } ) );
                    row.add( QUESTION_COMMENTS_COLUMN_START_INDEX_INSTRUCTOR_SHEET + questionCounter, "" );
                    row.set( QUESTION_COMMENTS_COLUMN_START_INDEX_INSTRUCTOR_SHEET + questionCounter, responseAggregator.formatForSpreadSheet( answer.getTemplateItem(), answer ) );
                    String comment = StringUtils.trimToEmpty( answer.getComment() );
                    if( !comment.isEmpty() )
                    {
                        row.add( QUESTION_COMMENTS_COLUMN_START_INDEX_INSTRUCTOR_SHEET + ++questionCounter, ( StringUtils.trimToEmpty( answer.getComment() ) ) );
                    }
                    questionCounter++;
                }

                // Add the row to the list of rows
                instructorRelatedResponses.add( row );
            }
        }

        // Sort the row data lists
        SortBySectionOrSiteComparator sorter = new SortBySectionOrSiteComparator();
        Collections.sort( instructorRelatedResponses, sorter );
        Collections.sort( courseRelatedResponses, sorter );

        // Output the sorted course related data into the course spreadsheet
        rowCounter = 0;
        for( List<String> row : courseRelatedResponses )
        {
            // Course sheet answer row, index cell
            short columnCounter = SECTION_OR_SITE_COLUMN_NUM;
            Row courseSheetAnswerRow = courseSheet.createRow( FIRST_ANSWER_ROW + rowCounter );
            Cell courseAnswerIndexCell = courseSheetAnswerRow.createCell( columnCounter++ );
            courseAnswerIndexCell.setCellValue( rowCounter + 1 );
            courseAnswerIndexCell.setCellStyle( boldHeaderStyle );

            // Course sheet section cell, response ID cell
            Cell courseSheetSectionCell = courseSheetAnswerRow.createCell( columnCounter++ );
            Cell courseSheetResponseIdCell = courseSheetAnswerRow.createCell( columnCounter++ );
            courseSheetSectionCell.setCellValue( row.get( SECTION_OR_SITE_COLUMN_NUM ) );
            courseSheetResponseIdCell.setCellValue( Integer.parseInt( row.get( RESPONSE_ID_COLUMN_NUM ) ) );

            // Responses and comments
            for( int i = QUESTION_COMMENTS_COLUMN_START_INDEX_COURSE_SHEET; i < row.size(); i++ )
            {
                setPlainStringCell( courseSheetAnswerRow.createCell( columnCounter++ ), row.get( i ) );
            }

            // Increment the row counter
            rowCounter++;
        }

        // Output the sorted instructor related data into the instructor spreadsheet
        rowCounter = 0;
        for( List<String> row : instructorRelatedResponses )
        {
            // Answer row, index cell
            short columnCounter = SECTION_OR_SITE_COLUMN_NUM;
            Row instructorSheetAnswerRow = instructorSheet.createRow( FIRST_ANSWER_ROW + rowCounter );
            Cell instructorAnswerIndexCell = instructorSheetAnswerRow.createCell( columnCounter++ );
            instructorAnswerIndexCell.setCellValue( rowCounter + 1 );
            instructorAnswerIndexCell.setCellStyle( boldHeaderStyle );

            // Section cell, response ID cell
            Cell instructorSheetSectionCell = instructorSheetAnswerRow.createCell( columnCounter++ );
            Cell instructorSheetResponseIdCell = instructorSheetAnswerRow.createCell( columnCounter++ );
            instructorSheetSectionCell.setCellValue( row.get( SECTION_OR_SITE_COLUMN_NUM ) );
            instructorSheetResponseIdCell.setCellValue( Integer.parseInt( row.get( RESPONSE_ID_COLUMN_NUM ) ) );

            // Instructor ID, first name, last name cells
            Cell instructorIdCell = instructorSheetAnswerRow.createCell( columnCounter++ );
            Cell instructorFirstNameCell = instructorSheetAnswerRow.createCell( columnCounter++ );
            Cell instructorLastNameCell = instructorSheetAnswerRow.createCell( columnCounter++ );
            instructorIdCell.setCellValue( row.get( INSTRUCTOR_ID_COLUMN_NUM ) );
            instructorFirstNameCell.setCellValue( row.get( INSTRUCTOR_FIRST_NAME_COLUMN_NUM ) );
            instructorLastNameCell.setCellValue( row.get( INSTRUCTOR_LAST_NAME_COLUMN_NUM ) );

            // Responses and comments
            for( int i = QUESTION_COMMENTS_COLUMN_START_INDEX_INSTRUCTOR_SHEET; i < row.size(); i++ )
            {
                setPlainStringCell( instructorSheetAnswerRow.createCell( columnCounter++ ), row.get( i ) );
            }

            // Increment the row counter
            rowCounter++;
        }

        // Dump the output to the response stream
        try { wb.write( outputStream ); }
        catch( IOException e ) { throw UniversalRuntimeException.accumulate( e, "Could not get Writer to dump output to xls" ); }

    }

    /**
     * Determine if the current DataTemplateItem should be included in the report (for the current user)
     * @param instructorViewAllResults
     * @param currentUserID
     * @param evalOwner
     * @param dti
     * @return true if the item is for the current user; false otherwise
     */
    private boolean isItemNotForCurrentUser( boolean instructorViewAllResults, String currentUserID, String evalOwner, DataTemplateItem dti )
    {
        return !instructorViewAllResults                                                       // If the eval is so configured,
                && !commonLogic.isUserAdmin( currentUserID )                                  // and currentUser is not an admin
                && !currentUserID.equals( evalOwner )                                         // and currentUser is not the eval creator
                && !EvalConstants.ITEM_CATEGORY_COURSE.equals( dti.associateType )            // and the associate type is not 'course'
                && !currentUserID.equals( commonLogic.getEvalUserById( dti.associateId ).userId );
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sakaiproject.evaluation.tool.reporting.ReportExporter#buildReport(org.sakaiproject.evaluation
     * .model.EvalEvaluation, java.lang.String[], java.io.OutputStream)
     */
    public void buildReport(EvalEvaluation evaluation, String[] groupIds, OutputStream outputStream, boolean newReportStyle) {
        buildReport(evaluation, groupIds, null, outputStream, newReportStyle);
    }
	
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sakaiproject.evaluation.tool.reporting.ReportExporter#buildReport(org.sakaiproject.evaluation
     * .model.EvalEvaluation, java.lang.String[], java.lang.String, java.io.OutputStream)
     */
    public void buildReport(EvalEvaluation evaluation, String[] groupIds, String evaluateeId, OutputStream outputStream, boolean newReportStyle) {
    	
        /*
         * Logic for creating this view 1) make tidl 2) get DTIs for this eval from tidl 3) use DTIs
         * to make the headers 4) get responseIds from tidl 5) loop over response ids 6) loop over
         * DTIs 7) check answersmap for an answer, if there put in cell, if missing, insert blank 8)
         * done
         */

    	
    	//Make sure responseAggregator is using this messageLocator
        responseAggregator.setMessageLocator(messageLocator);

        // Determine which report style to use; normal or section based
        if( newReportStyle )
        {
            buildReportSectionAware( evaluation, groupIds, outputStream );
        }
        else
        {
            Boolean instructorViewAllResults = (boolean) evaluation.getInstructorViewAllResults();
            String currentUserId = commonLogic.getCurrentUserId();
            String evalOwner = evaluation.getOwner();

            boolean isCurrentUserAdmin = commonLogic.isUserAdmin(currentUserId);

           // 1 Make TIDL
           TemplateItemDataList tidl = getEvalTIDL(evaluation, groupIds);
           // 2: get DTIs for this eval from tidl
           List<DataTemplateItem> dtiList = tidl.getFlatListOfDataTemplateItems(true);

           Workbook wb = new XSSFWorkbook();
           creationHelper = wb.getCreationHelper();

           Sheet sheet = wb.createSheet(messageLocator.getMessage("reporting.xls.sheetname"));

           // Title Style
           Font font = wb.createFont();
           font.setFontHeightInPoints((short) 12);
           font.setBoldweight(Font.BOLDWEIGHT_BOLD);
           CellStyle mainTitleStyle = wb.createCellStyle();
           mainTitleStyle.setFont(font);

           // Bold header style
           font = wb.createFont();
           font.setFontHeightInPoints((short) 10);
           font.setBoldweight(Font.BOLDWEIGHT_BOLD);
           CellStyle boldHeaderStyle = wb.createCellStyle();
           boldHeaderStyle.setFont(font);

           // Italic meta header style
           font = wb.createFont();
           font.setFontHeightInPoints((short) 10);
           font.setItalic(true);
           CellStyle italicMiniHeaderStyle = wb.createCellStyle();
           italicMiniHeaderStyle.setFont(font);

           // Date meta Style
           dateCellStyle = wb.createCellStyle();
           // TODO FIXME HELPME To properly
           // String dateCellFormat = ((SimpleDateFormat)DateFormat.getDateInstance(DateFormat.MEDIUM,
           // localeGetter.get())).toLocalizedPattern();
           // http://poi.apache.org/apidocs/org/apache/poi/hssf/usermodel/HSSFDataFormat.html
           dateCellStyle.setDataFormat((short) 0x16);

           // Evaluation Title
           Row row1 = sheet.createRow(0);
           Cell cellA1 = row1.createCell((short) 0);
           setPlainStringCell(cellA1, evaluation.getTitle());
           cellA1.setCellStyle(mainTitleStyle);

           // calculate the response rate
           // int responsesCount = deliveryService.countResponses(evaluation.getId(), null, true);
           int responsesCount = evaluationService.countResponses(null, new Long[] {evaluation.getId()}, groupIds, null);
           int enrollmentsCount = evaluationService.countParticipantsForEval(evaluation.getId(), groupIds);

           Row row2 = sheet.createRow(1);
           Cell cellA2 = row2.createCell((short) 0);
           cellA2.setCellStyle(boldHeaderStyle);
           setPlainStringCell(cellA2, EvalUtils.makeResponseRateStringFromCounts(responsesCount,
                   enrollmentsCount));

           // dates
           setPlainStringCell(row1.createCell((short) 2), messageLocator
                   .getMessage("evalsettings.start.date.header"));
           setDateCell(row2.createCell((short) 2), evaluation.getStartDate());
           if (evaluation.getDueDate() != null) {
               setPlainStringCell(row1.createCell((short) 3), messageLocator
                       .getMessage("evalsettings.due.date.header"));
               setDateCell(row2.createCell((short) 3), evaluation.getDueDate());
           }

           // add in list of groups
           if (groupIds.length > 0) {
               Row row3 = sheet.createRow(2);
               Cell cellA3 = row3.createCell((short) 0);
               setPlainStringCell(cellA3, messageLocator.getMessage("reporting.xls.participants",
                       new Object[] { responseAggregator.getCommaSeparatedGroupNames(groupIds) }));
           }

           // 3 use DTIs to make the headers
           Row questionCatRow = sheet.createRow(QUESTION_CAT_ROW);
           Row questionTypeRow = sheet.createRow(QUESTION_TYPE_ROW);
           Row questionTextRow = sheet.createRow(QUESTION_TEXT_ROW);
           short headerCount = 1;
           for (DataTemplateItem dti : dtiList) {

               if (!instructorViewAllResults // If the eval is so configured,
                 && !isCurrentUserAdmin // and currentUser is not an admin
                 && !currentUserId.equals(evalOwner) // and currentUser is not the eval creator
                 && !EvalConstants.ITEM_CATEGORY_COURSE.equals(dti.associateType) 
                 && !currentUserId.equals(commonLogic.getEvalUserById(dti.associateId).userId) ) {
                   // skip items that aren't for the current user
                   continue;
               }

               Cell cell = questionTypeRow.createCell(headerCount);

               setPlainStringCell(cell, responseAggregator.getHeaderLabelForItemType(dti
                       .getTemplateItemType()));
               cell.setCellStyle(italicMiniHeaderStyle);

               Cell questionText = questionTextRow.createCell(headerCount);
               setPlainStringCell(questionText, commonLogic.makePlainTextFromHTML(dti.templateItem
                       .getItem().getItemText()));

               Cell questionCat = questionCatRow.createCell(headerCount);
               if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(dti.associateType)) {
                   EvalUser user = commonLogic.getEvalUserById( dti.associateId );
                   String instructorMsg = messageLocator.getMessage("reporting.spreadsheet.instructor", 
                           new Object[] {user.displayName});
                   setPlainStringCell(questionCat, instructorMsg );
               } else if (EvalConstants.ITEM_CATEGORY_ASSISTANT.equals(dti.associateType)) {
                   EvalUser user = commonLogic.getEvalUserById( dti.associateId );
                   String assistantMsg = messageLocator.getMessage("reporting.spreadsheet.ta", 
                           new Object[] {user.displayName});
                   setPlainStringCell(questionCat, assistantMsg );
               } else if (EvalConstants.ITEM_CATEGORY_COURSE.equals(dti.associateType)) {
                   setPlainStringCell(questionCat, messageLocator
                           .getMessage("reporting.spreadsheet.course"));
               } else {
                   setPlainStringCell(questionCat, messageLocator.getMessage("unknown.caps"));
               }

               headerCount++;

               if (dti.usesComments()) {
                   // add an extra column for comments
                   setPlainStringCell(questionTypeRow.createCell(headerCount),
                           messageLocator.getMessage("viewreport.comments.header")).setCellStyle(
                           italicMiniHeaderStyle);
                   headerCount++;
               }

           }

           // 4) get responseIds from tidl
           List<Long> responseIds = tidl.getResponseIdsForAnswers();

           // 5) loop over response ids
           short responseIdCounter = 0;
           for (Long responseId : responseIds) {
               Row row = sheet.createRow(responseIdCounter + FIRST_ANSWER_ROW);
               Cell indexCell = row.createCell((short) 0);
               indexCell.setCellValue(responseIdCounter + 1);
               indexCell.setCellStyle(boldHeaderStyle);
               // 6) loop over DTIs
               short dtiCounter = 1;
               for (DataTemplateItem dti : dtiList) {

                   if (!instructorViewAllResults // If the eval is so configured,
                     && !isCurrentUserAdmin // and currentUser is not an admin
                     && !currentUserId.equals(evalOwner) // and currentUser is not the eval creator
                     && !EvalConstants.ITEM_CATEGORY_COURSE.equals(dti.associateType) 
                     && !currentUserId.equals(commonLogic.getEvalUserById(dti.associateId).userId) ) {
                       //skip instructor items that aren't for the current user
                       continue;
                   }

                   // 7) check answersmap for an answer, if there put in cell, if missing, insert blank
                   EvalAnswer answer = dti.getAnswer(responseId);
                   Cell responseCell = row.createCell(dtiCounter);
                   // In Eval, users can leave questions blank, in which case this will be null
                   if (answer != null) {
                       setPlainStringCell(responseCell, responseAggregator.formatForSpreadSheet(answer.getTemplateItem(), answer));
                   }
                   if (dti.usesComments()) {
                       // put comment in the extra column
                       dtiCounter++;
                       setPlainStringCell(row.createCell(dtiCounter), 
                               (answer == null || EvalUtils.isBlank(answer.getComment())) ? "" : answer.getComment());
                   }
                   dtiCounter++;
               }
               responseIdCounter++;
           }

           // dump the output to the response stream
           try {
               wb.write(outputStream);
           } catch (IOException e) {
               throw UniversalRuntimeException.accumulate(e,
                       "Could not get Writer to dump output to xls");
           }
        }
    }

    private TemplateItemDataList getEvalTIDL(EvalEvaluation evaluation,
			String[] groupIds) {  	
        return responseAggregator.prepareTemplateItemDataStructure(evaluation.getId(), groupIds);
	}

	/**
     * The regular set string value method in POI is deprecated, and this preferred way is much more
     * bulky, so this is a convenience method.
     * 
     * @param cell
     * @param value
     */
    private Cell setPlainStringCell(Cell cell, String value) {
        cell.setCellValue( creationHelper.createRichTextString(value) );
        return cell;
    }

    /**
     * Sets the cell contents to the date (requires extra work because Excel stores dates as
     * numbers.
     * 
     * @param cell
     * @param date
     * @return
     */
    private Cell setDateCell(Cell cell, Date date) {
        cell.setCellStyle(dateCellStyle);
        cell.setCellValue(date);
        return cell;
    }

    public String getContentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }
    
}
