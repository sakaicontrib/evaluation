/******************************************************************************
 * PreviewEvalProducer.java - recreated by aaronz on 30 May 2007
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.HierarchyNodeGroup;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.TemplateItemGroup;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * View for previewing a template or evaluation
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class PreviewEvalProducer implements ViewComponentProducer, ViewParamsReporter {

   public static final String VIEW_ID = "preview_eval";
   public String getViewID() {
      return VIEW_ID;
   }

   private EvalExternalLogic external;
   public void setExternal(EvalExternalLogic external) {
      this.external = external;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }

   private ItemRenderer itemRenderer;
   public void setItemRenderer(ItemRenderer itemRenderer) {
      this.itemRenderer = itemRenderer;
   }

   private MessageLocator messageLocator;
   public void setMessageLocator(MessageLocator messageLocator) {
      this.messageLocator = messageLocator;
   }

   private ExternalHierarchyLogic hierarchyLogic;
   public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
      this.hierarchyLogic = logic;
   }

   private EvalSettings evalSettings;
   public void setEvalSettings(EvalSettings evalSettings) {
      this.evalSettings = evalSettings;
   }

   int displayNumber = 1; //  determines the number to display next to each item

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      String currentUserId = external.getCurrentUserId();

      EvalViewParameters previewEvalViewParams = (EvalViewParameters)viewparams;
      if (previewEvalViewParams.evaluationId == null && 
            previewEvalViewParams.templateId == null) {
         throw new IllegalArgumentException("Must specify template id or evaluation id, both cannot be null");
      }

      Long evaluationId = previewEvalViewParams.evaluationId;
      Long templateId = previewEvalViewParams.templateId;
      String evalGroupId = previewEvalViewParams.evalGroupId;
      EvalEvaluation eval = null;
      EvalTemplate template = null;

      if (evaluationId == null) {
         // previewing a template
         UIMessage.make(tofill, "preview-title", "previeweval.template.title");
         // load up the template
         template = authoringService.getTemplateById(templateId);
         // create a fake evaluation
         eval = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, currentUserId, 
               messageLocator.getMessage("previeweval.evaluation.title.default"), 
               new Date(), new Date(), new Date(), new Date(), EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.SHARING_VISIBLE,
               new Integer(1), template);
         eval.setInstructions(messageLocator.getMessage("previeweval.instructions.default"));
      } else {
         // previewing an evaluation
         UIMessage.make(tofill, "preview-title", "previeweval.evaluation.title");
         UIMessage.make(tofill, "preview-title-prefix", "previeweval.evaluation.title.prefix");
         // load the real evaluation and template
         eval = evaluationService.getEvaluationById(evaluationId);
         template = authoringService.getTemplateById(eval.getTemplate().getId());
      }

      UIMessage.make(tofill, "eval-title-header", "takeeval.eval.title.header");
      UIOutput.make(tofill, "evalTitle", eval.getTitle());

      UIBranchContainer groupTitle = UIBranchContainer.make(tofill, "show-group-title:");
      UIMessage.make(groupTitle, "group-title-header", "takeeval.group.title.header");
      if (evalGroupId == null) {
         UIMessage.make(groupTitle, "group-title", "previeweval.course.title.default");
      } else {
         UIOutput.make(groupTitle, "group-title", external.getDisplayTitle(evalGroupId) );
      }

      // show instructions if not null
      if (eval.getInstructions() != null) {
         UIBranchContainer instructions = UIBranchContainer.make(tofill, "show-eval-instructions:");
         UIMessage.make(instructions, "eval-instructions-header", "takeeval.instructions.header");	
         UIVerbatim.make(instructions, "eval-instructions", eval.getInstructions());
      }


      // make up 2 fake instructors for this evaluation (to show the instructor added items)
      List<String> instructors = new ArrayList<String>();
      instructors.add("fake1");
      instructors.add("fake2");

      // get all items for this template
      List<EvalTemplateItem> allItems = 
         authoringService.getTemplateItemsForTemplate(templateId, new String[] {}, new String[] {}, new String[] {});

      // Get the sorted list of all nodes for this set of template items
      List<EvalHierarchyNode> hierarchyNodes = RenderingUtils.makeEvalNodesList(hierarchyLogic, allItems);

      // make the TI data structure
      Map<String, List<String>> associates = new HashMap<String, List<String>>();
      associates.put(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, instructors);
      TemplateItemDataList tidl = new TemplateItemDataList(allItems, hierarchyNodes, associates, null);

      // loop through the TIGs and handle each associated category
      for (TemplateItemGroup tig : tidl.getTemplateItemGroups()) {
         UIBranchContainer categorySectionBranch = UIBranchContainer.make(tofill, "categorySection:");
         // handle printing the category header
         if (EvalConstants.ITEM_CATEGORY_COURSE.equals(tig.associateType)) {
            UIMessage.make(categorySectionBranch, "categoryHeader", "takeeval.group.questions.header");
         } else if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(tig.associateType)) {
            String instructorName = tig.associateId.equals("fake2") ? "Steven Githens" : "Aaron Zeckoski";
            UIMessage.make(categorySectionBranch, "categoryHeader", 
                  "takeeval.instructor.questions.header", new Object[] { instructorName });
         }

         // loop through the hierarchy node groups
         for (HierarchyNodeGroup hng : tig.hierarchyNodeGroups) {
            // render a node title
            if (hng.node != null) {
               // Showing the section title is system configurable via the administrate view
               Boolean showHierSectionTitle = (Boolean) evalSettings.get(EvalSettings.DISPLAY_HIERARCHY_HEADERS);
               if (showHierSectionTitle) {
                  UIBranchContainer nodeTitleBranch = UIBranchContainer.make(categorySectionBranch, "itemrow:nodeSection");
                  UIOutput.make(nodeTitleBranch, "nodeTitle", hng.node.title);
               }
            }

            List<DataTemplateItem> dtis = hng.getDataTemplateItems(false);
            for (int i = 0; i < dtis.size(); i++) {
               DataTemplateItem dti = dtis.get(i);
               UIBranchContainer nodeItemsBranch = UIBranchContainer.make(categorySectionBranch, "itemrow:templateItem");
               if (i % 2 == 1) {
                  nodeItemsBranch.decorate( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
               }
               renderItemPrep(nodeItemsBranch, dti);
            }
         }
      }

   }

   /**
    * Prepare to render an item, this handles blocks correctly
    * 
    * @param parent the parent container
    * @param dti the wrapped template item we will render
    */
   private void renderItemPrep(UIBranchContainer parent, DataTemplateItem dti) {
      int displayIncrement = 0; // stores the increment in the display number
      EvalTemplateItem templateItem = dti.templateItem;
      if (! TemplateItemUtils.isAnswerable(templateItem)) {
         // nothing to bind for unanswerable items unless it is a block parent
         if ( dti.blockChildItems != null ) {
            // Handle the BLOCK PARENT special case - block item being rendered
            displayIncrement = dti.blockChildItems.size();
         }
      } else {
         // non-block and answerable items
         displayIncrement++;
      }

      // render the item
      itemRenderer.renderItem(parent, "renderedItem:", null, templateItem, displayNumber, true);

      // increment the display number
      displayNumber += displayIncrement;
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
    */
   public ViewParameters getViewParameters() {
      return new EvalViewParameters();
   }

}
