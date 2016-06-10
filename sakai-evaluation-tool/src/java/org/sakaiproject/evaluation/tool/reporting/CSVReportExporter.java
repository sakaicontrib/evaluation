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
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.util.UniversalRuntimeException;
import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class CSVReportExporter implements ReportExporter {

    private static final char COMMA = ',';
    private static final String COMMA_DELIMITER = ",";
    private static final String DOUBLE_QUOTE = "\"";
    private static final String NEW_LINE = "\n";
    private static final int SECTION_OR_SITE_COLUMN_NUM = 0;

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalResponseAggregatorUtil responseAggregator;
    public void setEvalResponseAggregatorUtil(EvalResponseAggregatorUtil bean) {
        this.responseAggregator = bean;
    }

    private MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator locator) {
        this.messageLocator = locator;
    }

    /**
     * Utility method to take a list of strings and append them to a StringBuilder
     * in CSV style
     * 
     * @param sb - the StringBuilder object to use
     * @param entries - the entries to add to the StringBuilder
     */
    private void appendToStringBuilder( StringBuilder sb, List<String> entries )
    {
        String prefix = "";
        for( String str : entries )
        {
            sb.append( prefix ).append( DOUBLE_QUOTE ).append( str ).append(DOUBLE_QUOTE);
            prefix = COMMA_DELIMITER;
        }
        sb.append( NEW_LINE );
    }

    /**
     * Custom comparator to sort row data based on the section/site column
     */
    public static class SortBySectionOrSiteComparator implements Comparator<List<String>>
    {
        @Override
        public int compare( List<String> row1, List<String> row2 )
        {
            String row1Value = row1.get( SECTION_OR_SITE_COLUMN_NUM );
            String row2Value = row2.get( SECTION_OR_SITE_COLUMN_NUM );
            return row1Value.compareTo( row2Value );
        }
    }

    /**
     * Build the .csv report in the new (section based) format.
     * 
     * @param evaluation - the EvalEvaluation object
     * @param groupIDs - group ID's associated with the evaluation
     * @param outputStream - the OutputStream to write to
     */
    private void buildReportSectionAware( EvalEvaluation evaluation, String[] groupIDs, OutputStream outputStream )
    {
        try( ZipOutputStream zout = new ZipOutputStream( outputStream ) )
        {
            String evalTitle = evaluation.getTitle().replaceAll( " ", "_" );

            // Get permission to view, current user and eval owner
            Boolean instructorViewAllResults = evaluation.getInstructorViewAllResults();
            String currentUserID = commonLogic.getCurrentUserId();
            String evalOwner = evaluation.getOwner();

            // Get the TIDL and DTIs for this evaluation
            TemplateItemDataList tidl = responseAggregator.prepareTemplateItemDataStructure( evaluation.getId(), groupIDs );
            List<DataTemplateItem> dtiList = tidl.getFlatListOfDataTemplateItems( true );

            // Create all the holders
            List<String> instructorRelatedQuestionHeaders = new ArrayList<>();
            List<String> courseRelatedQuestionHeaders = new ArrayList<>();
            List<List<String>> courseRelatedQuestions = new ArrayList<>();
            List<List<String>> instructorRelatedQuestions = new ArrayList<>();

            // Generate static column headers
            if( evaluation.getSectionAwareness() )
            {
                instructorRelatedQuestionHeaders.add( messageLocator.getMessage( "viewreport.section.header" ) );
                courseRelatedQuestionHeaders.add( messageLocator.getMessage( "viewreport.section.header" ) );
            }
            else
            {
                instructorRelatedQuestionHeaders.add( messageLocator.getMessage( "viewreport.site.header" ) );
                courseRelatedQuestionHeaders.add( messageLocator.getMessage( "viewreport.site.header" ) );
            }
            courseRelatedQuestionHeaders.add( messageLocator.getMessage( "viewreport.responseID.header" ) );
            instructorRelatedQuestionHeaders.add( messageLocator.getMessage( "viewreport.responseID.header" ) );
            instructorRelatedQuestionHeaders.add( messageLocator.getMessage( "viewreport.instructorID.header" ) );
            instructorRelatedQuestionHeaders.add( messageLocator.getMessage( "viewreport.firstName.header" ) );
            instructorRelatedQuestionHeaders.add( messageLocator.getMessage( "viewreport.lastName.header" ) );

            // Generate dynamic question (column) headers
            for( DataTemplateItem dti : dtiList )
            {
                // Skip items that aren't for the current user
                if( isItemNotForCurrentUser( instructorViewAllResults, currentUserID, evalOwner, dti ) )
                {
                    continue;
                }

                // If there's already a header for a specific instructor question, don't list it twice
                String questionText = commonLogic.makePlainTextFromHTML( dti.templateItem.getItem().getItemText() );
                if( instructorRelatedQuestionHeaders.contains( questionText ) )
                {
                    continue;
                }

                // Add the header to the appropriate CSV file
                if( EvalConstants.ITEM_CATEGORY_ASSISTANT.equals( dti.associateType ) || EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals( dti.associateType ) )
                {
                    instructorRelatedQuestionHeaders.add( questionText );
                    if( dti.usesComments() )
                    {
                        instructorRelatedQuestionHeaders.add( messageLocator.getMessage( "viewreport.comments.header" ) );
                    }
                }
                else
                {
                    courseRelatedQuestionHeaders.add( questionText );
                    if( dti.usesComments() )
                    {
                        courseRelatedQuestionHeaders.add( messageLocator.getMessage( "viewreport.comments.header" ) );
                    }
                }
            }

            // Loop through the responses
            List<Long> responseIDs = tidl.getResponseIdsForAnswers();
            Map<Long, Map<User, List<EvalAnswer>>> answerMap = new HashMap<>();
            for( Long responseID : responseIDs )
            {
                // Course related: section/site
                List<String> row = new ArrayList<>();
                String sectionName = "";
                List<EvalAnswer> answers = tidl.getAnswersByResponseId( responseID );
                if( answers != null && !answers.isEmpty() )
                {
                    sectionName = responseAggregator.getCommaSeparatedGroupNames( new String[] { answers.get( 0 ).getResponse().getEvalGroupId() } );
                }
                row.add( sectionName );

                // Course related: response ID
                row.add( responseID.toString() );

                // Add the response ID to the answer map
                answerMap.put( responseID, new HashMap<>() );

                // Loop through the DTIs
                for( DataTemplateItem dti : dtiList )
                {
                    // Skip items that aren't for the current user
                    if( isItemNotForCurrentUser( instructorViewAllResults, currentUserID, evalOwner, dti ) )
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
                            User instructor;
                            try { instructor = UserDirectoryService.getUser( answer.getAssociatedId() ); }
                            catch( UserNotDefinedException ex ) { continue; }

                            // If the answer map has a list of answers for this response and this instructor, add the answer to the list
                            if( answerMap.get( responseID ).containsKey( instructor ) )
                            {
                                answerMap.get( responseID ).get( instructor ).add( answer );
                            }

                            // Otherwise, the answer map doesn't have a list of answers for this response and this instructor,
                            // create the list and add the answer to it
                            else
                            {
                                List<EvalAnswer> list = new ArrayList<>();
                                list.add( answer );
                                answerMap.get( responseID ).put( instructor, list );
                            }
                        }
                    }

                    // If it's a course related item, add it to the course 'row'
                    else
                    {
                        // If the answer is not null, put the answer in the row
                        if( answer != null )
                        {
                            row.add( responseAggregator.formatForSpreadSheet( answer.getTemplateItem(), answer ) );
                        }

                        // Otherwise, put in a blank entry as a column placeholder
                        else
                        {
                            row.add( "" );
                        }

                        // If this DTI uses comments, put in the comment or a blank placeholder in the next column
                        if( dti.usesComments() )
                        {
                            row.add( StringUtils.trimToEmpty( answer.getComment() ) );
                        }
                    }
                }

                // Add the 'row' to the course related items list
                courseRelatedQuestions.add( row );
            }

            // Loop through the instructor related items
            for( Long responseID : answerMap.keySet() )
            {
                // Loop through the instructors for the current response
                for( User instructor : answerMap.get( responseID ).keySet() )
                {
                    // Loop through the questions for the current instructor and response
                    int answerCounter = 0;
                    List<String> row = new ArrayList<>();
                    for( EvalAnswer answer : answerMap.get( responseID ).get( instructor ) )
                    {
                        // If this is the first answer for this instructor and this response ID, add the qualifying columns
                        if( answerCounter == 0  )
                        {
                            // Instructor related: section/site
                            row.add( responseAggregator.getCommaSeparatedGroupNames( new String[] { answer.getResponse().getEvalGroupId() } ) );

                            // Instructor related: response ID, instructor identifiers
                            row.add( responseID.toString() );
                            row.add( instructor.getDisplayId() );
                            row.add( instructor.getFirstName() );
                            row.add( instructor.getLastName() );
                        }

                        // Instructor related response
                        // If the answer is not null, put the answer in the row
                        if( answer != null )
                        {
                            row.add( responseAggregator.formatForSpreadSheet( answer.getTemplateItem(), answer ) );
                        }

                        // Otherwise, put in a blank entry as a column placeholder
                        else
                        {
                            row.add( "" );
                        }

                        // If this uses comments, put in the comment or a blank placeholder in the next column
                        if( answer != null && answer.getComment() != null )
                        {
                            row.add( EvalUtils.isBlank( answer.getComment() ) ? "" : answer.getComment() );
                        }

                        // Increment the answer counter
                        answerCounter++;
                    }

                    // Add the 'row' to the course related items list
                    instructorRelatedQuestions.add( row );
                }
            }

            // Sort the responses by section/site
            SortBySectionOrSiteComparator sorter = new SortBySectionOrSiteComparator();
            Collections.sort( courseRelatedQuestions, sorter );
            Collections.sort( instructorRelatedQuestions, sorter );

            // Dump all the aggregated data to StringBuilders
            StringBuilder sbCourseItems = new StringBuilder();
            StringBuilder sbInstructorItems = new StringBuilder();
            appendToStringBuilder( sbCourseItems, courseRelatedQuestionHeaders );
            appendToStringBuilder( sbInstructorItems, instructorRelatedQuestionHeaders );
            for( List<String> row : courseRelatedQuestions )
            {
                appendToStringBuilder( sbCourseItems, row );
            }
            for( List<String> row : instructorRelatedQuestions )
            {
                appendToStringBuilder( sbInstructorItems, row );
            }

            // Create a ZipEntry for the course related data
            ZipEntry entry = new ZipEntry( evalTitle + "-courseItems.csv" );
            zout.putNextEntry( entry );
            byte[] data = sbCourseItems.toString().getBytes();
            zout.write( data, 0, data.length );
            zout.closeEntry();

            // Create a ZipEntry for the instructor related data
            entry = new ZipEntry( evalTitle + "-instructorItems.csv" );
            zout.putNextEntry( entry );
            data = sbInstructorItems.toString().getBytes();
            zout.write( data, 0, data.length );
            zout.closeEntry();

            // Close the ZipOutputStream
            zout.close();
        }
        catch( IOException ex ) { throw UniversalRuntimeException.accumulate( ex, "Could not close the ZipOutputStream" ); }
    }

    /**
     * Determine if the current DataTemplateItem should be included in the report (for the current user)
     * @param instructorViewAllResults
     * @param currentUserID
     * @param evalOwner
     * @param dti
     * @return true if the item is for the current user; false otherwise
     */
    public boolean isItemNotForCurrentUser( boolean instructorViewAllResults, String currentUserID, String evalOwner, DataTemplateItem dti )
    {
        return !instructorViewAllResults                                                       // If the eval is so configured,
                && !commonLogic.isUserAdmin( currentUserID )                                  // and currentUser is not an admin
                && !currentUserID.equals( evalOwner )                                         // and currentUser is not the eval creator
                && !EvalConstants.ITEM_CATEGORY_COURSE.equals( dti.associateType )            // and the associate type is not 'course'
                && !currentUserID.equals( commonLogic.getEvalUserById( dti.associateId ).userId );
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.tool.reporting.ReportExporter#buildReport(org.sakaiproject.evaluation.model.EvalEvaluation, java.lang.String[], java.io.OutputStream)
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
    	//Make sure responseAggregator is using this messageLocator
        responseAggregator.setMessageLocator(messageLocator);

        if( newReportStyle )
        {
            buildReportSectionAware( evaluation, groupIds, outputStream );
        }
        else
        {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            CSVWriter writer = new CSVWriter(outputStreamWriter, COMMA);

            Boolean instructorViewAllResults = (boolean) evaluation.getInstructorViewAllResults();
            String currentUserId = commonLogic.getCurrentUserId();
            String evalOwner = evaluation.getOwner();

            boolean isCurrentUserAdmin = commonLogic.isUserAdmin(currentUserId);

            // 1 Make TIDL
            TemplateItemDataList tidl = responseAggregator.prepareTemplateItemDataStructure(evaluation.getId(), groupIds);

            // 2 get DTIs for this eval from tidl
            List<DataTemplateItem> dtiList = tidl.getFlatListOfDataTemplateItems(true);

            // 3 use DTIs to make the headers
            List<String> questionCatRow = new ArrayList<>();
            List<String> questionTypeRow = new ArrayList<>();
            List<String> questionTextRow = new ArrayList<>();
            for (DataTemplateItem dti : dtiList) {

                if (!instructorViewAllResults // If the eval is so configured,
                  && !isCurrentUserAdmin // and currentUser is not an admin
                  && !currentUserId.equals(evalOwner) // and currentUser is not the eval creator
                  && !EvalConstants.ITEM_CATEGORY_COURSE.equals(dti.associateType) 
                  && !currentUserId.equals(commonLogic.getEvalUserById(dti.associateId).userId) ) {
                    //skip instructor items that aren't for the current user
                    continue;
                }

                questionTypeRow.add(responseAggregator.getHeaderLabelForItemType(dti.getTemplateItemType()));
                questionTextRow.add(commonLogic.makePlainTextFromHTML(dti.templateItem.getItem().getItemText()));
                if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(dti.associateType)) {
                    EvalUser user = commonLogic.getEvalUserById( dti.associateId );
                    String instructorMsg = messageLocator.getMessage("reporting.spreadsheet.instructor", 
                            new Object[] {user.displayName} );
                    questionCatRow.add( instructorMsg );
                } else if (EvalConstants.ITEM_CATEGORY_ASSISTANT.equals(dti.associateType)) {
                    EvalUser user = commonLogic.getEvalUserById( dti.associateId );
                    String assistantMsg = messageLocator.getMessage("reporting.spreadsheet.ta", 
                            new Object[] {user.displayName} );
                    questionCatRow.add( assistantMsg );
                } else if (EvalConstants.ITEM_CATEGORY_COURSE.equals(dti.associateType)) {
                    questionCatRow.add(messageLocator.getMessage("reporting.spreadsheet.course"));
                } else {
                    questionCatRow.add(messageLocator.getMessage("unknown.caps"));
                }

                if (dti.usesComments()) {
                    // add an extra column for comments
                    questionTypeRow.add(messageLocator.getMessage("viewreport.comments.header"));
                    // also add in blanks for the other columns
                    questionTextRow.add("");
                    questionCatRow.add("");
                }

            }

            writer.writeNext(questionCatRow.toArray(new String[] {}));
            writer.writeNext(questionTypeRow.toArray(new String[] {}));
            writer.writeNext(questionTextRow.toArray(new String[] {}));

            // 4) get responseIds from tidl
            List<Long> responseIds = tidl.getResponseIdsForAnswers();

            // 5) loop over response ids
            for (Long responseId : responseIds) {
                // 6) loop over DTIs
                List<String> nextResponseRow = new ArrayList<>();
                for (DataTemplateItem dti : dtiList) {

                    if (!instructorViewAllResults // If the eval is so configured,
                      && !isCurrentUserAdmin // and currentUser is not an admin
                      && !currentUserId.equals(evalOwner) // and currentUser is not the eval creator
                      && !EvalConstants.ITEM_CATEGORY_COURSE.equals(dti.associateType) 
                      && !currentUserId.equals(commonLogic.getEvalUserById(dti.associateId).userId) ) {
                        //skip instructor items that aren't for the current user
                        continue;
                    }

                    EvalAnswer answer = dti.getAnswer(responseId);
                    if (answer != null) {
                        nextResponseRow.add(responseAggregator.formatForSpreadSheet(answer.getTemplateItem(), answer));
                        if (dti.usesComments()) {
                            // put comment in the next column
                            nextResponseRow.add(StringUtils.trimToEmpty(answer.getComment()));
                        }
                    } else {
                        nextResponseRow.add("");
                        if (dti.usesComments()) {
                            nextResponseRow.add(""); // put in blank to space columns correctly
                        }
                    }
                }
                writer.writeNext(nextResponseRow.toArray(new String[] {}));
            }

            try {
                writer.close();
            } catch (IOException e1) {
                throw UniversalRuntimeException.accumulate(e1, "Could not close the CSVWriter");
            }
        }
    }

    public String getContentType() {
        return "text/csv";
    }
}
