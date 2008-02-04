package org.sakaiproject.evaluation.tool.reporting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.util.FormattedText;

import uk.org.ponder.util.UniversalRuntimeException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;

public class PDFReportExporter {
   private static Log log = LogFactory.getLog(PDFReportExporter.class);

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }
   
   private EvalSettings evalSettings;
   public void setEvalSettings(EvalSettings evalSettings) {
      this.evalSettings = evalSettings;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalDeliveryService deliveryService;
   public void setDeliveryService(EvalDeliveryService deliveryService) {
      this.deliveryService = deliveryService;
   }

   // FIXME - Do NOT use Sakai services directly, go through external logic
   private ContentHostingService contentHostingService;
   public void setContentHostingService(ContentHostingService contentHostingService) {
      this.contentHostingService = contentHostingService;
   }

   public void respondWithPDF(EvalEvaluation evaluation, EvalTemplate template,
         List<EvalItem> allEvalItems, List<EvalTemplateItem> allEvalTemplateItems,
         List<String> topRow, List<List<String>> responseRows, int numOfResponses,
         String[] groupIDs) {
      Document document = new Document();
      try {
         //PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
         document.open();

         // make a standard place in the eval webapp to put your custom image
         // FIXME confusing operation for user - if this will not fail when I check the box but don't have an image then why use a checkbox at all?
         // just have the user put in a banner image which shows up or leave the box blank, also should have the user browse for the banner image
         Boolean useBannerImage = (Boolean) evalSettings.get(EvalSettings.ENABLE_PDF_REPORT_BANNER);
         if (useBannerImage != null && useBannerImage == true) {
            String bannerImageLocation = (String) evalSettings.get(EvalSettings.PDF_BANNER_IMAGE_LOCATION);
            if (bannerImageLocation != null) {
               try {
                  ContentResource contentResource = contentHostingService.getResource(bannerImageLocation);
                  Image banner = Image.getInstance(contentResource.getContent());
                  banner.setAlignment(Element.ALIGN_CENTER);
                  document.add(banner);
               } catch (Exception e) {
                  log.warn("Cannot get PDF Banner Image for Evaluation Export", e);
               }
            }
         }

         // Title of Survey
         Paragraph title = new Paragraph(evaluation.getTitle());
         title.setAlignment(Element.ALIGN_CENTER);
         document.add(title);

         // Account Name
         Paragraph accountName = new Paragraph(externalLogic.getUserDisplayName(externalLogic.getCurrentUserId()));
         accountName.setAlignment(Element.ALIGN_CENTER);
         document.add(accountName);

         // Table with info on,
         // Carried out:  12th July 07 - 18th July 07
         // Response rate:  76% ( 80 / 126 )
         // Invitees: IB PMS 07/08
         PdfPTable table = new PdfPTable(2);
         table.addCell("Carried out:");
         String dates = evaluation.getStartDate() + " - " + evaluation.getStopDate();
         table.addCell(dates);
         table.addCell("Response Rate:");
         table.addCell(getParticipantResults(evaluation));
         table.addCell("Invitees:");
         String groupsCellContents = "";
         if (groupIDs.length > 0) {
            for (int groupCounter = 0; groupCounter < groupIDs.length; groupCounter++) {//groupTitles.size(); groupCounter++) {
               groupsCellContents +=  externalLogic.getDisplayTitle(groupIDs[groupCounter]); //groupTitles.get(groupCounter);
               if (groupCounter+1 < groupIDs.length) {//groupTitles.size()) {
                  groupsCellContents += ", ";
               }
            }
         }
         table.addCell(groupsCellContents);
         document.add(table);

         for (int i = 0; i < topRow.size(); i++) {
            String plainQuestionText = FormattedText.convertFormattedTextToPlaintext(topRow.get(i));
            Paragraph question = new Paragraph((i+1) + ". " + plainQuestionText);
            document.add(question);
            if (EvalConstants.ITEM_TYPE_TEXT.equals(TemplateItemUtils.getTemplateItemType(allEvalTemplateItems.get(i)))) {
               for (int j = 0; j < responseRows.size(); j++) {
                  String response = responseRows.get(j).get(i) + "\n";
                  Paragraph para = new Paragraph(response);
                  para.setIndentationLeft(20);
                  document.add(para);
                  if (j+1 < responseRows.size()) {
                     Paragraph spacer = new Paragraph("---");
                     //spacer.setAlignment(Element.ALIGN_CENTER);
                     spacer.setIndentationLeft(20);
                     document.add(spacer);
                  }
               }
            }
            else if (EvalConstants.ITEM_TYPE_SCALED.equals(TemplateItemUtils.getTemplateItemType(allEvalTemplateItems.get(i)))) {
               Map<String,Integer> resultMap = new HashMap<String,Integer>();
               for (int j = 0; j < responseRows.size(); j++) {
                  String response = responseRows.get(j).get(i);
                  if (resultMap.containsKey(response)) {
                     int value = resultMap.get(response).intValue();
                     value++;
                     resultMap.put(response, value);
                  }
                  else
                     resultMap.put(response, 1);
               }
               String overview = "";
               for (String key: resultMap.keySet()) {
                  overview += key + ", " + resultMap.get(key) + "   ";
               }
               document.add(new Paragraph(overview));
               
            }
            else { //TODO what do I do with other question types?
               // FIXME empty else block, put logging here or throw exception or remove this block (probably at least log this though), I would consider missing question types a bug
            }

            // Print out the Essay Results
            //if (TemplateItemUtils.getTemplateItemType(tempItem).equals(EvalConstants.ITEM_TYPE_TEXT)) {
            //    
            //}
         }

         document.close();
      } catch (DocumentException e) {
         throw UniversalRuntimeException.accumulate(e, "Error creating PDF Export");
      }


   }

   // FIXME Make this a utility rather than copy and paste
   // Bad duplicated code
   private String getParticipantResults(EvalEvaluation evaluation) {
      // Response Rate calculation... this is sort of duplicated code from ControlEvaluationsProducer
      // might be good to put it in one of the logic or utility classes.
      // TODO put this duplicate code in a utility class -AZ
      int countResponses = deliveryService.countResponses(evaluation.getId(), null, true);
      int countEnrollments = getTotalEnrollmentsForEval(evaluation.getId());
      long percentage = 0;
      if (countEnrollments > 0) {
         percentage = Math.round(  (((float)countResponses) / (float)countEnrollments) * 100.0 );
         return percentage + "%  ( " + countResponses + " / " + countEnrollments + " )";
         //UIOutput.make(evaluationRow, "closed-eval-response-rate", countResponses + "/"
         //      + countEnrollments + " - " + percentage + "%");
      } else {
         // don't bother showing percentage or "out of" when there are no enrollments
         //UIOutput.make(evaluationRow, "closed-eval-response-rate", countResponses + "");
         return countResponses + "";
      }
   }

   /**
    * FIXME Make this a utility rather than copy and paste
    * More duplicated code from ControlEvaluationsProducer
    * 
    * Gets the total count of enrollments for an evaluation
    * 
    * @param evaluationId
    * @return total number of users with take eval perms in this evaluation
    */
   private int getTotalEnrollmentsForEval(Long evaluationId) {
      int totalEnrollments = 0;
      Map<Long, List<EvalAssignGroup>> evalAssignGroups = evaluationService.getEvaluationAssignGroups(new Long[] {evaluationId}, true);
      List<EvalAssignGroup> groups = evalAssignGroups.get(evaluationId);
      for (int i=0; i<groups.size(); i++) {
         EvalAssignGroup eac = (EvalAssignGroup) groups.get(i);
         String context = eac.getEvalGroupId();
         Set<String> userIds = externalLogic.getUserIdsForEvalGroup(context, EvalConstants.PERM_TAKE_EVALUATION);
         totalEnrollments = totalEnrollments + userIds.size();
      }
      return totalEnrollments;
   }

}
