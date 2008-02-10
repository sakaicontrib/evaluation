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
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.utils.EvalAggregatedResponses;
import org.sakaiproject.evaluation.tool.utils.EvaluationCalcUtility;
import org.sakaiproject.util.FormattedText;

import uk.org.ponder.util.UniversalRuntimeException;

public class XLSReportExporter {

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private EvaluationCalcUtility evalCalcUtil;
   public void setEvaluationCalcUtility(EvaluationCalcUtility util) {
      this.evalCalcUtil = util;
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

      HSSFRow row2 = sheet.createRow(1);
      HSSFCell cellA2 = row2.createCell((short)0);
      cellA2.setCellStyle(boldHeaderStyle);
      cellA2.setCellValue(evalCalcUtil.getParticipantResults(responses.evaluation));

      //if (groupTitles.size() > 0) {
      if (responses.groupIds.length > 0) {
         HSSFRow row3 = sheet.createRow(2);
         HSSFCell cellA3 = row3.createCell((short)0);

         String groupsCellContents = "Participants: ";
         for (int groupCounter = 0; groupCounter < responses.groupIds.length; groupCounter++) {//groupTitles.size(); groupCounter++) {
            groupsCellContents +=  externalLogic.getDisplayTitle(responses.groupIds[groupCounter]); //groupTitles.get(groupCounter);
            if (groupCounter+1 < responses.groupIds.length) {//groupTitles.size()) {
               groupsCellContents += ", ";
            }
         }
         cellA3.setCellValue(groupsCellContents);
      }

      // Questions types (just above header row)
      HSSFRow questionTypeRow = sheet.createRow((short)4);
      for (int i = 0; i < responses.allEvalTemplateItems.size(); i++) {
         EvalTemplateItem tempItem = responses.allEvalTemplateItems.get(i);
         HSSFCell cell = questionTypeRow.createCell((short)(i+1));
         if (TemplateItemUtils.getTemplateItemType(tempItem).equals(EvalConstants.ITEM_TYPE_SCALED)) {
            cell.setCellValue("Rating scale");
         }
         else if (TemplateItemUtils.getTemplateItemType(tempItem).equals(EvalConstants.ITEM_TYPE_TEXT)) {
            cell.setCellValue("Free text / essay question");
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
