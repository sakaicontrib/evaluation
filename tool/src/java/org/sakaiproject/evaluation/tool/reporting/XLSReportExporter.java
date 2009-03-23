
package org.sakaiproject.evaluation.tool.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;

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

    private EvalDeliveryService deliveryService;
    public void setDeliveryService(EvalDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    private MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator locator) {
        this.messageLocator = locator;
    }

    HSSFCellStyle dateCellStyle;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.sakaiproject.evaluation.tool.reporting.ReportExporter#buildReport(org.sakaiproject.evaluation
     * .model.EvalEvaluation, java.lang.String[], java.io.OutputStream)
     */
    public void buildReport(EvalEvaluation evaluation, String[] groupIds, OutputStream outputStream) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet(messageLocator.getMessage("reporting.xls.sheetname"));

        // Title Style
        HSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        HSSFCellStyle mainTitleStyle = wb.createCellStyle();
        mainTitleStyle.setFont(font);

        // Bold header style
        font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        HSSFCellStyle boldHeaderStyle = wb.createCellStyle();
        boldHeaderStyle.setFont(font);

        // Italic meta header style
        font = wb.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setItalic(true);
        HSSFCellStyle italicMiniHeaderStyle = wb.createCellStyle();
        italicMiniHeaderStyle.setFont(font);

        // Date meta Style
        dateCellStyle = wb.createCellStyle();
        // TODO FIXME HELPME To properly
        // String dateCellFormat = ((SimpleDateFormat)DateFormat.getDateInstance(DateFormat.MEDIUM,
        // localeGetter.get())).toLocalizedPattern();
        // http://poi.apache.org/apidocs/org/apache/poi/hssf/usermodel/HSSFDataFormat.html
        dateCellStyle.setDataFormat((short) 0x16);

        // Evaluation Title
        HSSFRow row1 = sheet.createRow(0);
        HSSFCell cellA1 = row1.createCell((short) 0);
        setPlainStringCell(cellA1, evaluation.getTitle());
        cellA1.setCellStyle(mainTitleStyle);

        // calculate the response rate
        int responsesCount = deliveryService.countResponses(evaluation.getId(), null, true);
        int enrollmentsCount = evaluationService.countParticipantsForEval(evaluation.getId(), null);

        HSSFRow row2 = sheet.createRow(1);
        HSSFCell cellA2 = row2.createCell((short) 0);
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
            HSSFRow row3 = sheet.createRow(2);
            HSSFCell cellA3 = row3.createCell((short) 0);
            setPlainStringCell(cellA3, messageLocator.getMessage("reporting.xls.participants",
                    new Object[] { responseAggregator.getCommaSeparatedGroupNames(groupIds) }));
        }

        /*
         * Logic for creating this view 1) make tidl 2) get DTIs for this eval from tidl 3) use DTIs
         * to make the headers 4) get responseIds from tidl 5) loop over response ids 6) loop over
         * DTIs 7) check answersmap for an answer, if there put in cell, if missing, insert blank 8)
         * done
         */

        // 1 Make TIDL
        TemplateItemDataList tidl = responseAggregator.prepareTemplateItemDataStructure(evaluation.getId(), groupIds);

        // 2 get DTIs for this eval from tidl
        List<DataTemplateItem> dtiList = tidl.getFlatListOfDataTemplateItems(true);

        // 3 use DTIs to make the headers
        HSSFRow questionCatRow = sheet.createRow(QUESTION_CAT_ROW);
        HSSFRow questionTypeRow = sheet.createRow(QUESTION_TYPE_ROW);
        HSSFRow questionTextRow = sheet.createRow(QUESTION_TEXT_ROW);
        short headerCount = 1;
        for (DataTemplateItem dti : dtiList) {
            HSSFCell cell = questionTypeRow.createCell(headerCount);

            setPlainStringCell(cell, responseAggregator.getHeaderLabelForItemType(dti
                    .getTemplateItemType()));
            cell.setCellStyle(italicMiniHeaderStyle);

            HSSFCell questionText = questionTextRow.createCell(headerCount);
            setPlainStringCell(questionText, commonLogic.makePlainTextFromHTML(dti.templateItem
                    .getItem().getItemText()));

            HSSFCell questionCat = questionCatRow.createCell(headerCount);
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
            HSSFRow row = sheet.createRow(responseIdCounter + FIRST_ANSWER_ROW);
            HSSFCell indexCell = row.createCell((short) 0);
            indexCell.setCellValue(responseIdCounter + 1);
            indexCell.setCellStyle(boldHeaderStyle);
            // 6) loop over DTIs
            short dtiCounter = 1;
            for (DataTemplateItem dti : dtiList) {
                // 7) check answersmap for an answer, if there put in cell, if missing, insert blank
                EvalAnswer answer = dti.getAnswer(responseId);
                HSSFCell responseCell = row.createCell(dtiCounter);
                // In Eval, users can leave questions blank, in which case this will be null
                if (answer != null) {
                    setPlainStringCell(responseCell, responseAggregator.formatForSpreadSheet(answer
                            .getTemplateItem(), answer));
                }
                if (dti.usesComments()) {
                    // put comment in the extra column
                    dtiCounter++;
                    setPlainStringCell(row.createCell(dtiCounter), (answer == null || EvalUtils
                            .isBlank(answer.getComment())) ? "" : answer.getComment());
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
                    "Could not get Writer to dump output to csv");
        }
    }

    /**
     * The regular set string value method in POI is deprecated, and this preferred way is much more
     * bulky, so this is a convenience method.
     * 
     * @param cell
     * @param value
     */
    private HSSFCell setPlainStringCell(HSSFCell cell, String value) {
        cell.setCellValue(new HSSFRichTextString(value));
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
    private HSSFCell setDateCell(HSSFCell cell, Date date) {
        cell.setCellStyle(dateCellStyle);
        cell.setCellValue(date);
        return cell;
    }

    public String getContentType() {
        return "application/vnd.ms-excel";
    }

}
