/**
 * TakeEvalProducer.java - evaluation - Sep 18, 2006 11:35:56 AM - azeckoski
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.producers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.LocalResponsesLogic;
import org.sakaiproject.evaluation.tool.locators.ResponseAnswersBeanLocator;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.viewparams.EvalCategoryViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.EvalTakeViewParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;


/**
 * This page is for a user with take evaluation permission to fill and submit the evaluation
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class TakeEvalProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter, ActionResultInterceptor {

   private static Log log = LogFactory.getLog(TakeEvalProducer.class);

   public static final String VIEW_ID = "take_eval";
   public String getViewID() {
      return VIEW_ID;
   }

   private EvalExternalLogic external;
   public void setExternal(EvalExternalLogic external) {
      this.external = external;
   }

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   ItemRenderer itemRenderer;
   public void setItemRenderer(ItemRenderer itemRenderer) {
      this.itemRenderer = itemRenderer;
   }

   private LocalResponsesLogic localResponsesLogic;
   public void setLocalResponsesLogic(LocalResponsesLogic localResponsesLogic) {
      this.localResponsesLogic = localResponsesLogic;
   }

   private EvalSettings evalSettings;
   public void setEvalSettings(EvalSettings evalSettings) {
      this.evalSettings = evalSettings;
   }

   private Locale locale;
   public void setLocale(Locale locale) {
      this.locale = locale;
   }

   private ExternalHierarchyLogic hierarchyLogic;
   public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
      this.hierarchyLogic = logic;
   }

   Long responseId;
   Long evaluationId;
   String evalGroupId;

   int displayNumber=1;
   int renderedItemCount=0;

   /**
    * List of all evaluation items for the current evaluation
    */
   List<EvalTemplateItem> allItems;
   /**
    * Map of key to Answers for the current response<br/>
    * key = templateItemId + answer.associatedType + answer.associatedId
    */
   Map<String, EvalAnswer> answerMap = new HashMap<String, EvalAnswer>();


   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      boolean canAccess = false; // can a user access this evaluation
      boolean userCanAccess = false; // can THIS user take this evaluation

      String currentUserId = external.getCurrentUserId();
      // use a date which is related to the current users locale
      DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

      UIMessage.make(tofill, "page-title", "takeeval.page.title");

      UIInternalLink.make(tofill, "summary-link", UIMessage.make("summary.page.title"), 
            new SimpleViewParameters(SummaryProducer.VIEW_ID));			

      // get passed in get params
      EvalTakeViewParameters evalTakeViewParams = (EvalTakeViewParameters) viewparams;
      evaluationId = evalTakeViewParams.evaluationId;
      evalGroupId = evalTakeViewParams.evalGroupId;
      responseId = evalTakeViewParams.responseId;

      // get the evaluation based on the passed in VPs
      EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
      if (eval == null) {
         log.error("Cannot find evalaution with id: " + evaluationId);
         throw new IllegalArgumentException("Invalid evaluationId ("+evaluationId+"), cannot load evaluation");
      }

      UIMessage.make(tofill, "eval-title-header", "takeeval.eval.title.header");
      UIOutput.make(tofill, "evalTitle", eval.getTitle());

      /* check the states of the evaluation first to give the user a tip that this eval is not takeable,
       * also avoids wasting time checking permissions when the evaluation certainly is closed,
       * also allows us to give the user a nice custom message
       */
      String evalState = evaluationService.returnAndFixEvalState(eval, true); // make sure state is up to date
      if (EvalUtils.checkStateBefore(evalState, EvalConstants.EVALUATION_STATE_ACTIVE, false)) {
         String dueDate = "--------";
         if (eval.getDueDate() != null) {
            dueDate = df.format(eval.getDueDate());
         }
         UIMessage.make(tofill, "eval-cannot-take-message", "takeeval.eval.not.open", 
               new String[] {df.format(eval.getStartDate()), dueDate} );
         log.info("User ("+currentUserId+") cannot take evaluation yet, not open until: " + eval.getStartDate());
      } else if (EvalUtils.checkStateAfter(evalState, EvalConstants.EVALUATION_STATE_CLOSED, true)) {
         UIMessage.make(tofill, "eval-cannot-take-message", "takeeval.eval.closed",
               new String[] {df.format(eval.getDueDate())} );
         log.info("User ("+currentUserId+") cannot take evaluation anymore, closed on: " + eval.getDueDate());
      } else {
         // eval state is possible to take eval
         canAccess = true;
      }

      List<EvalGroup> validGroups = new ArrayList<EvalGroup>(); // stores EvalGroup objects
      if (canAccess) {
         // eval is accessible so check user can take it
         if (evalGroupId != null) {
            // there was an eval group passed in so make sure things are ok
            if (evaluationService.canTakeEvaluation(currentUserId, evaluationId, evalGroupId)) {
               userCanAccess = true;
            }
         } else {
            // select the first eval group the current user can take evaluation in,
            // also store the total number so we can give the user a list to choose from if there are more than one
            Map<Long, List<EvalAssignGroup>> m = evaluationService.getEvaluationAssignGroups(new Long[] {evaluationId}, true);
            if ( external.isUserAdmin(currentUserId) ) {
               // special case, the super admin can always access
               userCanAccess = true;
               List<EvalAssignGroup> assignGroups = m.get(evaluationId);
               for (int i = 0; i < assignGroups.size(); i++) {
                  EvalAssignGroup assignGroup = assignGroups.get(i);
                  if (evalGroupId == null) {
                     // set the evalGroupId to the first valid group if unset
                     evalGroupId = assignGroup.getEvalGroupId();
                  }
                  validGroups.add( external.makeEvalGroupObject( assignGroup.getEvalGroupId() ));
               }
            } else {
               EvalGroup[] evalGroups;
               if ( EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(eval.getAuthControl()) ) {
                  // anonymous eval allows any group to be evaluated
                  List<EvalAssignGroup> assignGroups = m.get(evaluationId);
                  evalGroups = new EvalGroup[assignGroups.size()];
                  for (int i = 0; i < assignGroups.size(); i++) {
                     EvalAssignGroup assignGroup = (EvalAssignGroup) assignGroups.get(i);
                     evalGroups[i] = external.makeEvalGroupObject( assignGroup.getEvalGroupId() );
                  }
               } else {
                  evalGroups = EvalUtils.getGroupsInCommon(
                        external.getEvalGroupsForUser(currentUserId, EvalConstants.PERM_TAKE_EVALUATION), 
                        m.get(evaluationId) );
               }
               for (int i = 0; i < evalGroups.length; i++) {
                  EvalGroup group = evalGroups[i];
                  if (evaluationService.canTakeEvaluation(currentUserId, evaluationId, group.evalGroupId)) {
                     if (evalGroupId == null) {
                        // set the evalGroupId to the first valid group if unset
                        evalGroupId = group.evalGroupId;
                        userCanAccess = true;
                     }
                     validGroups.add( external.makeEvalGroupObject(group.evalGroupId) );
                  }
               }
            }
         }

         if (userCanAccess) {
            // load up the response if this user has one already
            if (responseId == null) {
               EvalResponse response = evaluationService.getResponseForUserAndGroup(evaluationId, currentUserId, evalGroupId);
               if (response == null) {
                  // create the initial response if there is not one
                  // EVALSYS-360 because of a hibernate issue this will not work, do a binding instead -AZ
                  //responseId = localResponsesLogic.createResponse(evaluationId, currentUserId, evalGroupId);
               } else {
                  responseId = response.getId();
               }
            }
   
            if (responseId != null) {
               // load up the previous responses for this user (no need to attempt to load if the response is new, there will be no answers yet)
               answerMap = localResponsesLogic.getAnswersMapByTempItemAndAssociated(responseId);
            }
   
            // show the switch group selection and form if there are other valid groups for this user
            if (validGroups.size() > 1) {
               String[] values = new String[validGroups.size()];
               String[] labels = new String[validGroups.size()];
               for (int i=0; i<validGroups.size(); i++) {
                  EvalGroup group = (EvalGroup) validGroups.get(i);
                  values[i] = group.evalGroupId;
                  labels[i] = group.title;
               }
               // show the switch group selection and form
               UIBranchContainer showSwitchGroup = UIBranchContainer.make(tofill, "show-switch-group:");
               UIMessage.make(showSwitchGroup, "switch-group-header", "takeeval.switch.group.header");
               UIForm chooseGroupForm = UIForm.make(showSwitchGroup, "switch-group-form", 
                     new EvalTakeViewParameters(TakeEvalProducer.VIEW_ID, evaluationId, evalGroupId, responseId));
               UISelect.make(chooseGroupForm, "switch-group-list", values, labels,  "#{evalGroupId}");
               UIMessage.make(chooseGroupForm, "switch-group-button", "takeeval.switch.group.button");            
            }
   
            // fill in group title
            UIBranchContainer groupTitle = UIBranchContainer.make(tofill, "show-group-title:");
            UIMessage.make(groupTitle, "group-title-header", "takeeval.group.title.header");	
            UIOutput.make(groupTitle, "group-title", external.getDisplayTitle(evalGroupId) );
   
            // show instructions if not null
            if (eval.getInstructions() != null && !("".equals(eval.getInstructions())) ) {
               UIBranchContainer instructions = UIBranchContainer.make(tofill, "show-eval-instructions:");
               UIMessage.make(instructions, "eval-instructions-header", "takeeval.instructions.header");	
               UIVerbatim.make(instructions, "eval-instructions", eval.getInstructions());
            }
   
            // get the setting and make sure it cannot be null (fix for http://www.caret.cam.ac.uk/jira/browse/CTL-531)
            Boolean studentAllowedLeaveUnanswered = (Boolean) evalSettings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED);
            if (studentAllowedLeaveUnanswered == null) {
               studentAllowedLeaveUnanswered = eval.getBlankResponsesAllowed();
               if (studentAllowedLeaveUnanswered == null) {
                  studentAllowedLeaveUnanswered = false;
               }
            }
            // show a warning to the user if all items must be filled in
            if ( studentAllowedLeaveUnanswered == false ) {
               UIBranchContainer note = UIBranchContainer.make(tofill, "show-eval-note:");
               UIMessage.make(note, "eval-note-text", "takeeval.user.must.answer.all.note");   
            }
   
            UIBranchContainer formBranch = UIBranchContainer.make(tofill, "form-branch:");
            UIForm form = UIForm.make(formBranch, "evaluationForm");
   
            // bind the evaluation and evalGroup to the ones in the take eval bean
            String evalOTP = "evaluationBeanLocator.";
            form.parameters.add( new UIELBinding("#{takeEvalBean.eval}", new ELReference(evalOTP + eval.getId())) );
            form.parameters.add( new UIELBinding("#{takeEvalBean.evalGroupId}", evalGroupId) );
   
            // now we begin the complex task of rendering the evaluation items
   
            // get the instructors for this evaluation
            Set<String> instructors = external.getUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_BE_EVALUATED);
            String[] instructorIds = instructors.toArray(new String[instructors.size()]);
   
            // Get the Hierarchy NodeIDs for the current Group and turn it into an array of ids
            List<EvalHierarchyNode> hierarchyNodes = hierarchyLogic.getNodesAboveEvalGroup(evalGroupId);
            String[] hierarchyNodeIDs = new String[hierarchyNodes.size()];
            for (int i = 0; i < hierarchyNodes.size(); i++) {
               hierarchyNodeIDs[i] = hierarchyNodes.get(i).id;
            }
   
            // get all items for this evaluation
            allItems = authoringService.getTemplateItemsForEvaluation(evaluationId, hierarchyNodeIDs, 
                  instructorIds, new String[] {evalGroupId});
   
            // filter out the block child items, to get a list of non-child items
            List<EvalTemplateItem> nonChildItemsList = TemplateItemUtils.getNonChildItems(allItems);
   
            if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_COURSE, nonChildItemsList)) {
               // for all course items, go through render process
               UIBranchContainer courseSection = UIBranchContainer.make(form, "courseSection:");
               UIMessage.make(courseSection, "course-questions-header", "takeeval.group.questions.header");
               // for each non-child item in this evaluation
               handleCategoryRender(
                     TemplateItemUtils.getCategoryTemplateItems(EvalConstants.ITEM_CATEGORY_COURSE, nonChildItemsList), 
                     form, courseSection, hierarchyNodes, null);
            }
   
            if (instructors.size() > 0 &&
                  TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, nonChildItemsList)) {	
               // for each instructor, make a branch containing all instructor questions
               for (String instructorUserId : instructors) {
                  UIBranchContainer instructorSection = UIBranchContainer.make(form, "instructorSection:", "inst"+displayNumber);
                  EvalUser instructor = external.getEvalUserById( instructorUserId );
                  UIMessage.make(instructorSection, "instructor-questions-header", 
                        "takeeval.instructor.questions.header", new Object[] { instructor.displayName });
                  // for each non-child item in this evaluation
                  handleCategoryRender(
                        TemplateItemUtils.getCategoryTemplateItems(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, nonChildItemsList), 
                        form, instructorSection, hierarchyNodes, instructorUserId);
               }
            }
   
            UICommand.make(form, "submitEvaluation", UIMessage.make("takeeval.submit.button"), "#{takeEvalBean.submitEvaluation}");
         } else {
            // user cannot access eval so give them a sad message
            UIMessage.make(tofill, "eval-cannot-take-message", "takeeval.user.cannot.take");
            log.info("User ("+currentUserId+") cannot take evaluation: " + eval.getId());
         }
      }
   }

   /**
    * Handles the rendering of a set of items in a category/section of the view
    * @param itemsList the list of items to render in this section
    * @param form the form the controls for this set of items are attached to
    * @param section the parent branch container that holds all the UI elements in this section
    * @param evalHierNodes
    * @param associatedId the ID if there is something associated with these items
    */
   private void handleCategoryRender(List<EvalTemplateItem> itemsList, UIForm form, UIBranchContainer section, 
         List<EvalHierarchyNode> evalHierNodes, String associatedId) {
      /* We need to render things by section. For now we'll just loop through each evalHierNode to start, and 
       * see if there are items of it. If there are we'll go ahead and render them with a group header.  We 
       * have to take care of the special toplevel hierarchy type first.
       * 
       * I don't suppose this is terribly efficient, just looping through the nodes and items over and over again.
       * However, I don't think any survey is going to have enough questions for it to ever matter.
       */
      List<EvalTemplateItem> templateItems = new ArrayList<EvalTemplateItem>();
      for (EvalTemplateItem item: itemsList) {
         if (item.getHierarchyLevel().equals(EvalConstants.HIERARCHY_LEVEL_TOP)) {
            templateItems.add(item);
         }
      }

      if (templateItems.size() > 0) {
         handleCategoryHierarchyNodeRender(templateItems, form, section, null, associatedId);
      }

      for (EvalHierarchyNode evalNode: evalHierNodes) {
         templateItems = new ArrayList<EvalTemplateItem>();
         for (EvalTemplateItem item: itemsList) {
            if (item.getHierarchyLevel().equals(EvalConstants.HIERARCHY_LEVEL_NODE) && item.getHierarchyNodeId().equals(evalNode.id)) {
               templateItems.add(item);
            }
         }
         if (templateItems.size() > 0) {
            handleCategoryHierarchyNodeRender(templateItems, form, section, evalNode.title, associatedId);
         }
      }
   }

   /**
    * This method is most likely to be invoked from handleCategoryRender. Basically it handles the grouping by node for each category.
    * So say in the Group/Courses category, you may have questions for "General", "Biology Department", and "Botany 101". You would call
    * this method once for each node group, passing in the items for that node, and the title (ie. "Biology Department") that you want rendered.
    * If you pass in a null or empty sectiontitle, it will assume that it is for the general top level node level.
    */
   private void handleCategoryHierarchyNodeRender(List<EvalTemplateItem> itemsInNode, UIForm form, UIContainer tofill, 
         String sectiontitle, String associatedId) {
      // Showing the section title is system configurable via the administrate view
      Boolean showHierSectionTitle = (Boolean) evalSettings.get(EvalSettings.DISPLAY_HIERARCHY_HEADERS);
      if (showHierSectionTitle != null && showHierSectionTitle.booleanValue() == true) {
         if (sectiontitle != null && !sectiontitle.equals("")) {
            UIBranchContainer labelrow = UIBranchContainer.make(tofill, "itemrow:hier-node-section");
            UIOutput.make(labelrow, "hier-node-title", sectiontitle);
         }
      }

      for (int i = 0; i <itemsInNode.size(); i++) {
         EvalTemplateItem templateItem = itemsInNode.get(i);
         UIBranchContainer radiobranch = UIBranchContainer.make(tofill, "itemrow:first", i+"");
         if (i % 2 == 1) {
            radiobranch.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
         }
         renderItemPrep(radiobranch, form, templateItem, associatedId);
      }
   }

   /**
    * Prep for rendering an item I assume (no comments by original authors) -AZ
    * 
    * @param branch
    * @param form
    * @param templateItem
    * @param associatedId
    */
   private void renderItemPrep(UIBranchContainer branch, UIForm form, EvalTemplateItem templateItem, String associatedId) {
      int displayIncrement = 0; // stores the increment in the display number
      String[] currentAnswerOTP = null; // holds array of bindings for items
      if (! TemplateItemUtils.isAnswerable(templateItem)) {
         // nothing to bind for unanswerable items unless it is a block parent
         if ( TemplateItemUtils.isBlockParent(templateItem) ) {
            // Handle the BLOCK PARENT special case - block item being rendered

            // get the child items for this block
            List<EvalTemplateItem> childList = TemplateItemUtils.getChildItems(allItems, templateItem.getId());
            currentAnswerOTP = new String[childList.size()];
            // for each child item, construct a binding
            for (int j = 0; j < childList.size(); j++) {
               EvalTemplateItem currChildItem = childList.get(j);
               // set up OTP paths
               String[] childAnswerOTP = setupCurrentAnswerBindings(form, currChildItem, associatedId);
               if (childAnswerOTP != null) {
                  currentAnswerOTP[j] = childAnswerOTP[0];
               }
               renderedItemCount++;
            }
            displayIncrement = currentAnswerOTP.length;
         }
      } else {
         // non-block and answerable items
         currentAnswerOTP = setupCurrentAnswerBindings(form, templateItem, associatedId);
         displayIncrement++;
      }

      // render the item
      itemRenderer.renderItem(branch, "rendered-item:", currentAnswerOTP, templateItem, displayNumber, false);

      /* increment the item counters, if we displayed 1 item, increment by 1,
       * if we displayed a block, renderedItem has been incremented for each child, increment displayNumber by the number of blockChildren,
       * here we are simply adding the display increment to the overall number -AZ
       */
      displayNumber += displayIncrement;
      renderedItemCount++;
   }

   /**
    * Generates the correct OTP path for the current answer associated with this templateItem,
    * also handles the binding of the item to answer and the associatedId
    * @param form the form to bind this data into
    * @param templateItem the template item which the answer should associate with
    * @param associatedId the associated ID to bind this TI with
    * @return an array of binding strings from the TI to the answer (first) and NA (second) which will bind to the input elements
    */
   private String[] setupCurrentAnswerBindings(UIForm form, EvalTemplateItem templateItem, String associatedId) {
      // the associated type is set only if the associatedId is set
      String associatedType = null;
      if (associatedId != null) {
         associatedType = templateItem.getCategory(); // type will match the template item category
      }

      // set up OTP paths for answerable items
      String responseAnswersOTP = "responseAnswersBeanLocator.";
      String currAnswerOTP;
      boolean newAnswer = false;
      if (responseId == null) {
         // it should not be the case that we have no response
         //throw new IllegalStateException("There is no response, something has failed to load correctly for takeeval");
         // EVALSYS-360 - have to use this again and add a binding for start time
         form.parameters.add( new UIELBinding("takeEvalBean.startDate", new Date()) );

         currAnswerOTP = responseAnswersOTP + ResponseAnswersBeanLocator.NEW_1 + "." + ResponseAnswersBeanLocator.NEW_PREFIX + renderedItemCount + ".";
         newAnswer = true;
      } else {
         // if the user has answered this question before, point at their response
         String key = templateItem.getId() + associatedType + associatedId;
         EvalAnswer currAnswer = (EvalAnswer) answerMap.get(key);
         if (currAnswer == null) {
            // this is a new answer
            newAnswer = true;
            currAnswerOTP = responseAnswersOTP + responseId + "." + ResponseAnswersBeanLocator.NEW_PREFIX + (renderedItemCount) + ".";
         } else {
            // existing answer
            newAnswer = false;
            currAnswerOTP = responseAnswersOTP + responseId + "." + currAnswer.getId() + ".";
         }
      }

      if (newAnswer) {
         // ADD in the bindings for the new answers

         // bind the template item to the answer
         form.parameters.add( new UIELBinding(currAnswerOTP + "templateItem", 
               new ELReference("templateItemWBL." + templateItem.getId())) );

         // bind the item to the answer
         form.parameters.add( new UIELBinding(currAnswerOTP + "item", 
               new ELReference("itemWBL." + templateItem.getItem().getId())) );

         // bind the associated id (current instructor id or environment) and type to the current answer
         if (associatedId != null) {
            // only do the binding if this is not null, otherwise it will bind in empty strings
            form.parameters.add( new UIELBinding(currAnswerOTP + "associatedId", associatedId) );
            form.parameters.add( new UIELBinding(currAnswerOTP + "associatedType", associatedType) );
         }
      }

      // generate binding for the UI input element (UIInput, UISelect, etc.) to the correct part of answer
      String[] bindings = null;
      String itemType = TemplateItemUtils.getTemplateItemType(templateItem);
      if ( EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(itemType) ) {
         bindings = new String[] { currAnswerOTP + "multipleAnswers", currAnswerOTP + "NA" };
      } else if ( EvalConstants.ITEM_TYPE_TEXT.equals(itemType) ) {
         bindings = new String[] { currAnswerOTP + "text", currAnswerOTP + "NA" };
      } else {
         // this is the default binding (scaled and MC)
         bindings = new String[] { currAnswerOTP + "numeric" };
      }
      return bindings;
   }


   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
    */
   public ViewParameters getViewParameters() {
      return new EvalTakeViewParameters();
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
    */
   @SuppressWarnings("unchecked")
   public List reportNavigationCases() {
      List i = new ArrayList();
      i.add(new NavigationCase("success", new SimpleViewParameters(SummaryProducer.VIEW_ID)));
      return i;
   }

   public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
      EvalTakeViewParameters etvp = (EvalTakeViewParameters) incoming;
      if (etvp.evalCategory != null) {
         result.resultingView = new EvalCategoryViewParameters(ShowEvalCategoryProducer.VIEW_ID, etvp.evalCategory);
      }
   }


}
