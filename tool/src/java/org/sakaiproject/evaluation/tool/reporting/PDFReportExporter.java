package org.sakaiproject.evaluation.tool.reporting;

import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.utils.EvalAggregatedResponses;
import org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.HierarchyNodeGroup;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.TemplateItemGroup;
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
   
   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
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
   
   private ExternalHierarchyLogic hierarchyLogic;
   public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
      this.hierarchyLogic = logic;
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

      String plainInstructions = externalLogic.cleanupUserStrings(responses.evaluation.getInstructions());
      evalPDFReportBuilder.addIntroduction(responses.evaluation.getTitle(), plainInstructions);

      List<EvalTemplateItem> allTemplateItems = new ArrayList<EvalTemplateItem>(responses.template.getTemplateItems());
      List<EvalTemplateItem> orderedItems = TemplateItemUtils.orderTemplateItems(allTemplateItems, false);

      TemplateItemDataList tidl = prepareTemplateItemDataStructure(responses.evaluation, responses.groupIds);
      
      // Loop through the major group types: Course Questions, Instructor Questions, etc.
      int renderedItemCount = 0;
      for (TemplateItemGroup tig: tidl.getTemplateItemGroups()) {
         
         // Print the type of the next group we're doing
         if (EvalConstants.ITEM_CATEGORY_COURSE.equals(tig.associateType)) {
            evalPDFReportBuilder.addSectionHeader(messageLocator.getMessage("viewreport.itemlist.course"));
         }
         else if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(tig.associateType)) {
            String instructor = externalLogic.getEvalUserById(tig.associateId).displayName;
            evalPDFReportBuilder.addSectionHeader(messageLocator.getMessage("viewreport.itemlist.instructor", new String[] {instructor}));
         }
         
         for (HierarchyNodeGroup hng: tig.hierarchyNodeGroups) {
            // Render the Node title if it's enabled in the admin settings.
            if (hng.node != null) {
               // Showing the section title is system configurable via the administrate view
               Boolean showHierSectionTitle = (Boolean) evalSettings.get(EvalSettings.DISPLAY_HIERARCHY_HEADERS);
               if (showHierSectionTitle) {
                  evalPDFReportBuilder.addSectionHeader(hng.node.title);
               }
            }
            
            List<DataTemplateItem> dtis = hng.getDataTemplateItems(true); // include block children
            for (int i = 0; i < dtis.size(); i++) {
               DataTemplateItem dti = dtis.get(i);
               renderDataTemplateItem(evalPDFReportBuilder, dti);
               renderedItemCount++;
            }
         }
      }
      
      evalPDFReportBuilder.close();
   }
      
   /**
    * Renders a single question giving the DataTemplateItem.
    * 
    * @param evalPDFReportBuilder
    * @param dti
    */
   private void renderDataTemplateItem(EvalPDFReportBuilder evalPDFReportBuilder, DataTemplateItem dti) {
         EvalTemplateItem templateItem = dti.templateItem;
         EvalItem item = templateItem.getItem();
         String questionText = externalLogic.cleanupUserStrings(item.getItemText());
         
         List<EvalAnswer> itemAnswers = dti.getAnswers();

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
   
   /**
    * Does the preparation work for getting the DITL.  At the moment, this is basically
    * everything from ReportsViewingProducer before it started iterating through
    * the template data items.  Looking into making this the same for all the reporting
    * formats.
    * 
    * @param eval
    * @param groupIds
    * @return
    */
   public TemplateItemDataList prepareTemplateItemDataStructure(EvalEvaluation eval, String[] groupIds) {
      List<EvalTemplateItem> allTemplateItems = 
         authoringService.getTemplateItemsForTemplate(eval.getTemplate().getId(), new String[] {}, new String[] {}, new String[] {});
      
      // get all the answers
      List<EvalAnswer> answers = deliveryService.getAnswersForEval(eval.getId(), groupIds, null);
      
      // get the list of all instructors for this report and put the user objects for them into a map
      Set<String> instructorIds = TemplateItemDataList.getInstructorsForAnswers(answers);
      List<EvalUser> instructors = externalLogic.getEvalUsersByIds(instructorIds.toArray(new String[] {}));
      Map<String,EvalUser> instructorIdtoEvalUser = new HashMap<String, EvalUser>();
      for (EvalUser evalUser : instructors) {
         instructorIdtoEvalUser.put(evalUser.userId, evalUser);
      }
      
      // Get the sorted list of all nodes for this set of template items
      List<EvalHierarchyNode> hierarchyNodes = RenderingUtils.makeEvalNodesList(hierarchyLogic, allTemplateItems);
      
      // make the TI data structure
      Map<String, List<String>> associates = new HashMap<String, List<String>>();
      associates.put(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, new ArrayList<String>(instructorIds));
      TemplateItemDataList tidl = new TemplateItemDataList(allTemplateItems, hierarchyNodes, associates, answers);
      
      return tidl;
   }
}