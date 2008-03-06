package org.sakaiproject.evaluation.tool.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.utils.EvalAggregatedResponses;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.util.FormattedText;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.util.UniversalRuntimeException;

public class XLSReportExporter {

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

   public void formatResponses(EvalAggregatedResponses responses, OutputStream outputStream) {
      HSSFWorkbook wb = new HSSFWorkbook();
      HSSFSheet sheet = wb.createSheet("Responses");

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
      cellA1.setCellValue(responses.evaluation.getTitle());
      cellA1.setCellStyle(mainTitleStyle);

      // calculate the response rate
      int responsesCount = deliveryService.countResponses(responses.evaluation.getId(), null, true);
      int enrollmentsCount = evaluationService.countParticipantsForEval(responses.evaluation.getId());

      HSSFRow row2 = sheet.createRow(1);
      HSSFCell cellA2 = row2.createCell((short)0);
      cellA2.setCellStyle(boldHeaderStyle);
      cellA2.setCellValue( EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount) );

      //if (groupTitles.size() > 0) {
      if (responses.groupIds.length > 0) {
         HSSFRow row3 = sheet.createRow(2);
         HSSFCell cellA3 = row3.createCell((short)0);

         String groupsString = "";
         for (int groupCounter = 0; groupCounter < responses.groupIds.length; groupCounter++) {
            groupsString +=  externalLogic.getDisplayTitle(responses.groupIds[groupCounter]);
            if (groupCounter+1 < responses.groupIds.length) {
               groupsString += ", ";
            }
         }
         cellA3.setCellValue(messageLocator.getMessage("reporting.xls.participants",
               new String[] {groupsString}));
      }

      // Questions types (just above header row)
      HSSFRow questionTypeRow = sheet.createRow((short)4);
      for (int i = 0; i < responses.allEvalTemplateItems.size(); i++) {
         EvalTemplateItem tempItem = responses.allEvalTemplateItems.get(i);
         HSSFCell cell = questionTypeRow.createCell((short)(i+1));
         if (EvalConstants.ITEM_TYPE_SCALED.equals(TemplateItemUtils.getTemplateItemType(tempItem))) {
            cell.setCellValue(messageLocator.getMessage("reporting.itemtypelabel.scale"));
         }
         else if (EvalConstants.ITEM_TYPE_TEXT.equals(TemplateItemUtils.getTemplateItemType(tempItem))) {
            cell.setCellValue(messageLocator.getMessage("reporting.itemtypelabel.text"));
         }
         else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(TemplateItemUtils.getTemplateItemType(tempItem))) {
            cell.setCellValue(messageLocator.getMessage("reporting.itemtypelabel.multipleanswer"));
         }
         else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(TemplateItemUtils.getTemplateItemType(tempItem))) {
            cell.setCellValue(messageLocator.getMessage("reporting.itemtypelabel.multiplechoice"));
         }
         else {
            cell.setCellValue("");
         }
         cell.setCellStyle(italicMiniHeaderStyle);
      }

      // Header Row
      HSSFRow headerRow = sheet.createRow((short)5);
      for (int i = 0; i < responses.topRow.size(); i++) {
         // Adding one because we want the first column to be a numbered list.
         HSSFCell cell = headerRow.createCell((short)(i+1));
         String questionString = FormattedText.convertFormattedTextToPlaintext(responses.topRow.get(i));
         cell.setCellValue(((String)questionString));
         cell.setCellStyle(boldHeaderStyle);
      }

      // Fill in the rest
      for (int i = 0; i < responses.responseRows.size(); i++) {
         HSSFRow row = sheet.createRow((short)(i+6)); 
         HSSFCell indexCell = row.createCell((short)0);
         indexCell.setCellValue(i+1);
         indexCell.setCellStyle(boldHeaderStyle);
         List<String> rowResponses = (List<String>) responses.responseRows.get(i);
         for (int j = 0 ; j < rowResponses.size(); j++) {
            HSSFCell responseCell = row.createCell((short)(j+1));
            responseCell.setCellValue(rowResponses.get(j));
         }
      }

      // dump the output to the response stream
      try {
         wb.write(outputStream);
      } catch (IOException e) {
         throw UniversalRuntimeException.accumulate(e, "Could not get Writer to dump output to csv");
      }
   }
}
