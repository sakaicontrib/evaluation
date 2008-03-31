package org.sakaiproject.evaluation.tool.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.utils.EvalAggregatedResponses;
import org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.HierarchyNodeGroup;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.TemplateItemGroup;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.util.UniversalRuntimeException;

public class XLSReportExporter {
   
   private static final short QUESTION_CAT_ROW = 3; // Course, Instructor, etc
   private static final short QUESTION_TYPE_ROW = 4;
   private static final short QUESTION_TEXT_ROW = 5;
   private static final short FIRST_ANSWER_ROW = 6;
   
   private EvalResponseAggregatorUtil responseAggregator;
   public void setEvalResponseAggregatorUtil(EvalResponseAggregatorUtil bean) {
      this.responseAggregator = bean;
   }

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
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
   
   /**
    * The regular set string value method in POI is deprecated, and this prefered
    * way is much more bulky, so this is a convenience method.
    * 
    * @param cell
    * @param value
    */
   private void setPlainStringCell(HSSFCell cell, String value) {
      cell.setCellValue(new HSSFRichTextString(value));
   }

   public void formatResponses(EvalEvaluation evaluation, String[] groupIds, OutputStream outputStream) {
      HSSFWorkbook wb = new HSSFWorkbook();
      HSSFSheet sheet = wb.createSheet("Responses"); //TODO i18n

      // Title Style
      HSSFFont font = wb.createFont();
      font.setFontHeightInPoints((short)12);
      font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
      HSSFCellStyle mainTitleStyle = wb.createCellStyle();
      mainTitleStyle.setFont(font);

      // Bold header style
      font = wb.createFont();
      font.setFontHeightInPoints((short)10);
      font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
      HSSFCellStyle boldHeaderStyle = wb.createCellStyle();
      boldHeaderStyle.setFont(font);

      // Italic meta header style
      font = wb.createFont();
      font.setFontHeightInPoints((short)10);
      font.setItalic(true);
      HSSFCellStyle italicMiniHeaderStyle = wb.createCellStyle();
      italicMiniHeaderStyle.setFont(font);

      // Evaluation Title
      HSSFRow row1 = sheet.createRow(0);
      HSSFCell cellA1 = row1.createCell((short)0);
      setPlainStringCell(cellA1,evaluation.getTitle());
      cellA1.setCellStyle(mainTitleStyle);

      // calculate the response rate
      int responsesCount = deliveryService.countResponses(evaluation.getId(), null, true);
      int enrollmentsCount = evaluationService.countParticipantsForEval(evaluation.getId());

      HSSFRow row2 = sheet.createRow(1);
      HSSFCell cellA2 = row2.createCell((short)0);
      cellA2.setCellStyle(boldHeaderStyle);
      setPlainStringCell(cellA2,EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount) );

      if (groupIds.length > 0) {
         HSSFRow row3 = sheet.createRow(2);
         HSSFCell cellA3 = row3.createCell((short)0);

         setPlainStringCell(cellA3,messageLocator.getMessage("reporting.xls.participants",
               new String[] {responseAggregator.getCommaSeperatedGroupNames(groupIds)}));
      }
      
      /* Logic for creating this view
       * 1) make tidl
       * 2) get DTIs for this eval from tidl
       * 3) use DTIs to make the headers
       * 4) get responseIds from tidl
       * 5) loop over response ids
       * 6) loop over DTIs
       * 7) check answersmap for an answer, if there put in cell, if missing, insert blank
       * 8) done
       */
      
      // 1 Make TIDL
      TemplateItemDataList tidl = responseAggregator.prepareTemplateItemDataStructure(evaluation, groupIds);

      // 2 get DTIs for this eval from tidl
      List<DataTemplateItem> dtiList = tidl.getFlatListOfDataTemplateItems(true);
      
      // 3 use DTIs to make the headers
      HSSFRow questionCatRow = sheet.createRow(QUESTION_CAT_ROW);
      HSSFRow questionTypeRow = sheet.createRow(QUESTION_TYPE_ROW);
      HSSFRow questionTextRow = sheet.createRow(QUESTION_TEXT_ROW);
      short headerCount = 1;
      for (DataTemplateItem dti: dtiList) {
         String type = TemplateItemUtils.getTemplateItemType(dti.templateItem);
         HSSFCell cell = questionTypeRow.createCell(headerCount);
         
         if (EvalConstants.ITEM_TYPE_SCALED.equals(type)) {
            setPlainStringCell(cell,messageLocator.getMessage("reporting.itemtypelabel.scale"));
         }
         else if (EvalConstants.ITEM_TYPE_TEXT.equals(type)) {
            setPlainStringCell(cell,messageLocator.getMessage("reporting.itemtypelabel.text"));
         }
         else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(type)) {
            setPlainStringCell(cell,messageLocator.getMessage("reporting.itemtypelabel.multipleanswer"));
         }
         else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(type)) {
            setPlainStringCell(cell,messageLocator.getMessage("reporting.itemtypelabel.multiplechoice"));
         }
         else {
            setPlainStringCell(cell,"");
         }
         cell.setCellStyle(italicMiniHeaderStyle);
         
         HSSFCell questionText = questionTextRow.createCell(headerCount);
         setPlainStringCell(questionText,externalLogic.cleanupUserStrings(
               dti.templateItem.getItem().getItemText()));
         
         HSSFCell questionCat = questionCatRow.createCell(headerCount);
         if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(dti.associateType)) {
            setPlainStringCell(questionCat,"Instructor: " + externalLogic.getUserUsername(dti.associateId)); // TODO FIXME i18n
         }
         else if (EvalConstants.ITEM_CATEGORY_COURSE.equals(dti.associateType)) {
            setPlainStringCell(questionCat,"Course"); // TODO FIXME i18n
         }
         else {
            setPlainStringCell(questionCat,"");
         }
         
         headerCount++;
      }

      // 4) get responseIds from tidl
      List<Long> responseIds = tidl.getResponseIdsForAnswers();
      
      // 5) loop over response ids
      short responseIdCounter = 0;
      for (Long responseId: responseIds) {
         HSSFRow row = sheet.createRow(responseIdCounter+FIRST_ANSWER_ROW); 
         HSSFCell indexCell = row.createCell((short)0);
         indexCell.setCellValue(responseIdCounter+1);
         indexCell.setCellStyle(boldHeaderStyle);
         // 6) loop over DTIs
         short dtiCounter = 1;
         for (DataTemplateItem dti: dtiList) {
            // 7) check answersmap for an answer, if there put in cell, if missing, insert blank
            EvalAnswer answer = dti.getAnswer(responseId);
            HSSFCell responseCell = row.createCell(dtiCounter);
            // In Eval, users can leave questions blank, in which case this will
            // be null
            if (answer != null) {
               setPlainStringCell(responseCell,responseAggregator.formatForSpreadSheet(answer.getTemplateItem(), answer));
            }
            dtiCounter++;
         }
         responseIdCounter++;
      }

      // dump the output to the response stream
      try {
         wb.write(outputStream);
      } catch (IOException e) {
         throw UniversalRuntimeException.accumulate(e, "Could not get Writer to dump output to csv");
      }
   }
}
