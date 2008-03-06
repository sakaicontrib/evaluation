package org.sakaiproject.evaluation.tool.reporting;

import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.utils.EvalAggregatedResponses;
import org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.util.FormattedText;

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

      EvalUser user = externalLogic.getEvalUserById( externalLogic.getCurrentUserId() );

      DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);

      // calculate the response rate
      int responsesCount = deliveryService.countResponses(responses.evaluation.getId(), null, true);
      int enrollmentsCount = evaluationService.countParticipantsForEval(responses.evaluation.getId());

      evalPDFReportBuilder.addTitlePage(responses.evaluation.getTitle(), 
            user.displayName, 
            messageLocator.getMessage("reporting.pdf.accountinfo", new String[] {user.username, user.displayName}), 
            messageLocator.getMessage("reporting.pdf.startdatetime",df.format(responses.evaluation.getStartDate())),
            messageLocator.getMessage("reporting.pdf.replyrate", new String[] { EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount) }),
            bannerImageBytes, messageLocator.getMessage("reporting.pdf.defaultsystemname"));

      String plainInstructions = FormattedText.convertFormattedTextToPlaintext(responses.evaluation.getInstructions());
      evalPDFReportBuilder.addIntroduction(responses.evaluation.getTitle(), plainInstructions);

      List<EvalTemplateItem> allTemplateItems = new ArrayList<EvalTemplateItem>(responses.template.getTemplateItems());
      List<EvalTemplateItem> orderedItems = TemplateItemUtils.orderTemplateItems(allTemplateItems);

      for (int i = 0; i < orderedItems.size(); i++) {
         EvalTemplateItem templateItem = orderedItems.get(i);
         EvalItem item = templateItem.getItem();
         String questionText = FormattedText.convertFormattedTextToPlaintext(item.getItemText());
         
         // Security hole to pass in an empty groupID array because then we get *all* of them.
         List<EvalAnswer> itemAnswers = new ArrayList<EvalAnswer>();
         if (responses.groupIds != null && responses.groupIds.length > 0) {
            itemAnswers = deliveryService.getEvalAnswers(item.getId(), responses.evaluation.getId(), responses.groupIds);
         }

         String templateItemType = TemplateItemUtils.getTemplateItemType(templateItem);
         
         if (EvalConstants.ITEM_TYPE_HEADER.equals(templateItemType)) {
            evalPDFReportBuilder.addSectionHeader(questionText);
         }
         else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(templateItemType) ||
                  EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(templateItemType) ||
                  EvalConstants.ITEM_TYPE_SCALED.equals(templateItemType)) {
            boolean showPercentages = false;
            if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(templateItemType)) {
               showPercentages = true;
            }
            
            int[] responseArray = responseAggregator.countResponseChoices(templateItemType, item.getScale().getOptions().length, itemAnswers);
            
            String[] optionLabels;
            if (templateItem.getUsesNA()) {
               optionLabels = new String[item.getScale().getOptions().length+1];
               for (int m = 0; m < item.getScale().getOptions().length; m++) {
                  optionLabels[m] = item.getScale().getOptions()[m];
               }
               optionLabels[optionLabels.length-1] = messageLocator.getMessage("reporting.notapplicable.longlabel");
            }
            else {
               optionLabels = item.getScale().getOptions();
            }

            evalPDFReportBuilder.addLikertResponse(questionText, 
                  optionLabels, responseArray, showPercentages);
         }
         else if (EvalConstants.ITEM_TYPE_TEXT.equals(templateItemType)) {
            List<String> essays = new ArrayList<String>();
            for (EvalAnswer answer: itemAnswers) {
               essays.add(answer.getText());
            }
            evalPDFReportBuilder.addEssayResponse(questionText, essays);
         }
         else {
            log.warn("Trying to add unknown type to PDF: " + TemplateItemUtils.getTemplateItemType(templateItem));
         }
      }

      evalPDFReportBuilder.close();
   }
}