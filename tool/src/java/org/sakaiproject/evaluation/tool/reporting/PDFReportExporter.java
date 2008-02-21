package org.sakaiproject.evaluation.tool.reporting;

import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.utils.EvalAggregatedResponses;
import org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

import uk.org.ponder.messageutil.MessageLocator;

public class PDFReportExporter {
   private static Log log = LogFactory.getLog(PDFReportExporter.class);

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalSettings evalSettings;
   public void setEvalSettings(EvalSettings evalSettings) {
      this.evalSettings = evalSettings;
   }

   private EvalDeliveryService deliveryService;
   public void setDeliveryService(EvalDeliveryService deliveryService) {
      this.deliveryService = deliveryService;
   }

   private EvalResponseAggregatorUtil responseAggregator;
   public void setEvalResponseAggregatorUtil(EvalResponseAggregatorUtil bean) {
      this.responseAggregator = bean;
   }

   private MessageLocator messageLocator;
   public void setMessageLocator(MessageLocator locator) {
      this.messageLocator = locator;
   }


   public void formatResponses(EvalAggregatedResponses responses, OutputStream outputStream) {

      EvalPDFReportBuilder evalPDFReportBuilder = new EvalPDFReportBuilder(outputStream);

      Boolean useBannerImage = (Boolean) evalSettings.get(EvalSettings.ENABLE_PDF_REPORT_BANNER);
      byte[] bannerImageBytes = null;
      if (useBannerImage != null && useBannerImage == true) {
         String bannerImageLocation = (String) evalSettings.get(EvalSettings.PDF_BANNER_IMAGE_LOCATION);
         if (bannerImageLocation != null) {
            bannerImageBytes = externalLogic.getFileContent(bannerImageLocation);
         }
      }

      String userDisplayName = externalLogic.getUserDisplayName(externalLogic.getCurrentUserId());
      String userEid = externalLogic.getUserUsername(externalLogic.getCurrentUserId());

      DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);

      // calculate the response rate
      int responsesCount = deliveryService.countResponses(responses.evaluation.getId(), null, true);
      int enrollmentsCount = evaluationService.countParticipantsForEval(responses.evaluation.getId());

      evalPDFReportBuilder.addTitlePage(responses.evaluation.getTitle(), 
            userDisplayName, 
            messageLocator.getMessage("reporting.pdf.accountinfo", new String[] {userEid, userDisplayName}), 
            messageLocator.getMessage("reporting.pdf.startdatetime",df.format(responses.evaluation.getStartDate())),
            messageLocator.getMessage("reporting.pdf.replyrate", new String[] { EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount) }),
            bannerImageBytes, messageLocator.getMessage("reporting.pdf.defaultsystemname"));

      evalPDFReportBuilder.addIntroduction(responses.evaluation.getTitle(), responses.evaluation.getInstructions());

      List<EvalTemplateItem> allTemplateItems = new ArrayList<EvalTemplateItem>(responses.template.getTemplateItems());
      List<EvalTemplateItem> orderedItems = TemplateItemUtils.orderTemplateItems(allTemplateItems);

      for (int i = 0; i < orderedItems.size(); i++) {
         EvalTemplateItem templateItem = orderedItems.get(i);
         EvalItem item = templateItem.getItem();
         List<EvalAnswer> itemAnswers = deliveryService.getEvalAnswers(item.getId(), responses.evaluation.getId(), responses.groupIds);

         if (EvalConstants.ITEM_TYPE_HEADER.equals(TemplateItemUtils.getTemplateItemType(templateItem))) {
            evalPDFReportBuilder.addSectionHeader(item.getItemText());
         }
         else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(TemplateItemUtils.getTemplateItemType(templateItem))) {
            evalPDFReportBuilder.addLikertResponse(templateItem.getItem().getItemText(), 
                  item.getScale().getOptions(), responseAggregator.countResponseChoices(EvalConstants.ITEM_TYPE_MULTIPLEANSWER, item.getScale().getOptions().length, itemAnswers), false);
         }
         else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(TemplateItemUtils.getTemplateItemType(templateItem))) {
            evalPDFReportBuilder.addLikertResponse(templateItem.getItem().getItemText(), 
                  item.getScale().getOptions(), responseAggregator.countResponseChoices(EvalConstants.ITEM_TYPE_MULTIPLECHOICE, item.getScale().getOptions().length, itemAnswers), true);
         }
         else if (EvalConstants.ITEM_TYPE_TEXT.equals(TemplateItemUtils.getTemplateItemType(templateItem))) {
            List<String> essays = new ArrayList<String>();
            for (EvalAnswer answer: itemAnswers) {
               essays.add(answer.getText());
            }
            evalPDFReportBuilder.addEssayResponse(templateItem.getItem().getItemText(), essays);
         }
         else if (EvalConstants.ITEM_TYPE_SCALED.equals(TemplateItemUtils.getTemplateItemType(templateItem))) {
            evalPDFReportBuilder.addLikertResponse(templateItem.getItem().getItemText(), 
                  item.getScale().getOptions(), responseAggregator.countResponseChoices(EvalConstants.ITEM_TYPE_MULTIPLECHOICE, item.getScale().getOptions().length, itemAnswers), false);
         }
         else {
            log.warn("Trying to add unknown type to PDF: " + TemplateItemUtils.getTemplateItemType(templateItem));
         }
      }

      evalPDFReportBuilder.close();
   }
}