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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.exceptions.ResponseSaveException;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.LocalResponsesLogic;
import org.sakaiproject.evaluation.tool.locators.ResponseAnswersBeanLocator;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.viewparams.EvalCategoryViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.HierarchyNodeGroup;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.TemplateItemGroup;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundBoolean;
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
import uk.org.ponder.rsf.components.decorators.UICSSDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
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

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
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

    private ExternalHierarchyLogic hierarchyLogic;
    public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
        this.hierarchyLogic = logic;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }

    private TargettedMessageList messages;
    public void setMessages(TargettedMessageList messages) {
        this.messages = messages;
    }

    private Locale locale;
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    Long responseId;

    int displayNumber=1;
    int renderedItemCount=0;

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

        String currentUserId = commonLogic.getCurrentUserId();
        // use a date which is related to the current users locale
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

        UIMessage.make(tofill, "page-title", "takeeval.page.title");

        UIInternalLink.make(tofill, "summary-link", UIMessage.make("summary.page.title"), 
                new SimpleViewParameters(SummaryProducer.VIEW_ID));			

        // get passed in get params
        EvalViewParameters evalTakeViewParams = (EvalViewParameters) viewparams;
        Long evaluationId = evalTakeViewParams.evaluationId;
        if (evaluationId == null) {
            // redirect over to the main view maybe?? (not sure how to do this in RSF)
            log.error("User ("+currentUserId+") cannot take evaluation, eval id is not set");
            throw new IllegalArgumentException("Invalid evaluationId: id must be set and cannot be null, cannot load evaluation");
        }
        String evalGroupId = evalTakeViewParams.evalGroupId;
        responseId = evalTakeViewParams.responseId;

        // get the evaluation based on the passed in VPs
        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        if (eval == null) {
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
                Map<Long, List<EvalAssignGroup>> m = evaluationService.getAssignGroupsForEvals(new Long[] {evaluationId}, true, null);
                if ( commonLogic.isUserAdmin(currentUserId) ) {
                    // special case, the super admin can always access
                    userCanAccess = true;
                    List<EvalAssignGroup> assignGroups = m.get(evaluationId);
                    for (int i = 0; i < assignGroups.size(); i++) {
                        EvalAssignGroup assignGroup = assignGroups.get(i);
                        if (evalGroupId == null) {
                            // set the evalGroupId to the first valid group if unset
                            evalGroupId = assignGroup.getEvalGroupId();
                        }
                        validGroups.add( commonLogic.makeEvalGroupObject( assignGroup.getEvalGroupId() ));
                    }
                } else {
                    EvalGroup[] evalGroups;
                    if ( EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(eval.getAuthControl()) ) {
                        // anonymous eval allows any group to be evaluated
                        List<EvalAssignGroup> assignGroups = m.get(evaluationId);
                        evalGroups = new EvalGroup[assignGroups.size()];
                        for (int i = 0; i < assignGroups.size(); i++) {
                            EvalAssignGroup assignGroup = assignGroups.get(i);
                            evalGroups[i] = commonLogic.makeEvalGroupObject( assignGroup.getEvalGroupId() );
                        }
                    } else {
                        List<EvalAssignUser> userAssignments = evaluationService.getParticipantsForEval(evaluationId, currentUserId, null, 
                                EvalAssignUser.TYPE_EVALUATOR, null, null, null);
                        Set<String> evalGroupIds = EvalUtils.getGroupIdsFromUserAssignments(userAssignments);
                        List<EvalGroup> groups = EvalUtils.makeGroupsFromGroupsIds(evalGroupIds, commonLogic);
                        evalGroups = EvalUtils.getGroupsInCommon(groups, m.get(evaluationId) );
                    }
                    for (int i = 0; i < evalGroups.length; i++) {
                        EvalGroup group = evalGroups[i];
                        if (evaluationService.canTakeEvaluation(currentUserId, evaluationId, group.evalGroupId)) {
                            if (evalGroupId == null) {
                                // set the evalGroupId to the first valid group if unset
                                evalGroupId = group.evalGroupId;
                                userCanAccess = true;
                            }
                            validGroups.add( commonLogic.makeEvalGroupObject(group.evalGroupId) );
                        }
                    }
                }
            }

            if (userCanAccess) {
                // check if we had a failure during a previous submit and get the missingKeys out if that was the failure
                Set<String> missingKeys = new HashSet<String>();
                if (messages.isError() && messages.size() > 0) {
                    for (int i = 0; i < messages.size(); i++) {
                        TargettedMessage message = messages.messageAt(i);
                        Exception e = message.exception;
                        if (e instanceof ResponseSaveException) {
                            ResponseSaveException rse = (ResponseSaveException) e;
                            if (ResponseSaveException.TYPE_MISSING_REQUIRED_ANSWERS.equals(rse.type)) {
                                if (rse.missingItemAnswerKeys != null
                                        && rse.missingItemAnswerKeys.length > 0) {
                                    for (int j = 0; j < rse.missingItemAnswerKeys.length; j++) {
                                        missingKeys.add(rse.missingItemAnswerKeys[j]);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }

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
                        EvalGroup group = validGroups.get(i);
                        values[i] = group.evalGroupId;
                        labels[i] = group.title;
                    }
                    // show the switch group selection and form
                    UIBranchContainer showSwitchGroup = UIBranchContainer.make(tofill, "show-switch-group:");
                    UIMessage.make(showSwitchGroup, "switch-group-header", "takeeval.switch.group.header");
                    UIForm chooseGroupForm = UIForm.make(showSwitchGroup, "switch-group-form", 
                            new EvalViewParameters(TakeEvalProducer.VIEW_ID, evaluationId, responseId, evalGroupId));
                    UISelect.make(chooseGroupForm, "switch-group-list", values, labels,  "#{evalGroupId}");
                    UIMessage.make(chooseGroupForm, "switch-group-button", "takeeval.switch.group.button");            
                }

                // fill in group title
                EvalGroup evalGroup = commonLogic.makeEvalGroupObject( evalGroupId );
                UIBranchContainer groupTitle = UIBranchContainer.make(tofill, "show-group-title:");
                UIMessage.make(groupTitle, "group-title-header", "takeeval.group.title.header");	
                UIOutput.make(groupTitle, "group-title", evalGroup.title );

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

                // BEGIN the complex task of rendering the evaluation items

                // get the instructors for this evaluation
                List<EvalAssignUser> instAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                        new String[] {evalGroupId}, EvalAssignUser.TYPE_EVALUATEE, null, null, null);
                Set<String> instructors = EvalUtils.getUserIdsFromUserAssignments(instAssignments);

                // Get the Hierarchy Nodes for the current Group and turn it into an array of node ids
                List<EvalHierarchyNode> hierarchyNodes = hierarchyLogic.getNodesAboveEvalGroup(evalGroupId);
                String[] hierarchyNodeIDs = new String[hierarchyNodes.size()];
                for (int i = 0; i < hierarchyNodes.size(); i++) {
                    hierarchyNodeIDs[i] = hierarchyNodes.get(i).id;
                }

                // get all items for this evaluation
                List<EvalTemplateItem> allItems = authoringService.getTemplateItemsForEvaluation(evaluationId, hierarchyNodeIDs, 
                        instructors.toArray(new String[instructors.size()]), new String[] {evalGroupId});

                // make the TI data structure and get the flat list of DTIs
                Map<String, List<String>> associates = new HashMap<String, List<String>>();
                associates.put(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, new ArrayList<String>(instructors));

                // add in the TA list if there are any TAs
                Set<String> teachingAssistants = new HashSet<String>(0);
                Boolean taEnabled = (Boolean) evalSettings.get(EvalSettings.ENABLE_ASSISTANT_CATEGORY);
                if (taEnabled) {
                    List<EvalAssignUser> assistAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                            new String[] {evalGroupId}, EvalAssignUser.TYPE_ASSISTANT, null, null, null);
                    teachingAssistants = EvalUtils.getUserIdsFromUserAssignments(assistAssignments);
                    if (teachingAssistants.size() > 0) {
                        associates.put(EvalConstants.ITEM_CATEGORY_ASSISTANT, new ArrayList<String>(teachingAssistants));
                    }
                }

                TemplateItemDataList tidl = new TemplateItemDataList(allItems, hierarchyNodes, associates, null);

                // loop through all DTIs and flag items that were missing
                if (! missingKeys.isEmpty()) {
                    for (DataTemplateItem dti : tidl.getFlatListOfDataTemplateItems(true)) {
                        if (missingKeys.contains(dti.getKey())) {
                            // flag this template item for invalidated rendering if it was missing
                            dti.templateItem.renderOption = true;
                        }
                    }
                }

                // SELECTION Code - added by lovenalube
                Boolean selectEnabled = (Boolean) evalSettings.get(EvalSettings.ENABLE_INSTRUCTOR_ASSISTANT_SELECTION);
                String instructorsSel = eval.getInstructorSelection();
                if (selectEnabled && instructors.size( ) >0) {
                    // TODO rename these (what does v mean?) and ONLY create them when they are used
                	List<String> v = new ArrayList<String>();
                    List<String> l = new ArrayList<String>();
                    // Iterator<String> inst = instructors.iterator(); // TODO don't create iterator that is not always used
                	if (instructorsSel.equals(EvalAssignHierarchy.SELECTION_ALL)) {
                		// nothing special to do in all case
                	} else if (instructorsSel.equals(EvalAssignHierarchy.SELECTION_MULTIPLE)){
                	    UIBranchContainer showSwitchGroup = UIBranchContainer.make(tofill, "select-instructors-multiple:");
                	    //UIForm chooseGroupForm = UIForm.make(showSwitchGroup, "select-instructors-multiple-form", "");
                	    for (String userId : instructors) {
                	        EvalUser user = commonLogic.getEvalUserById( userId );
                	        UIBranchContainer row = UIBranchContainer.make(showSwitchGroup, "select-instructors-multiple-row:");
                	        UIOutput checkBranch = UIOutput.make(row, "select-instructors-multiple-label", user.displayName);	                        
                	        UIBoundBoolean b = UIBoundBoolean.make(row, "select-instructors-multiple-box", Boolean.FALSE);
                	        // we have to force the id so the JS block checking can work
                	        b.decorators = new DecoratorList( new UIIDStrategyDecorator(user.userId) );
                	        // have to force the target id so that the label for works 
                	        UILabelTargetDecorator uild = new UILabelTargetDecorator(b);
                	        uild.targetFullID = user.userId;
                	        checkBranch.decorators = new DecoratorList( uild );
                	        // tooltip
                	        // b.decorators.add( new UITooltipDecorator( UIMessage.make("select-instructors-multiple-label") ) );
                	    }
                	    UIMessage.make(showSwitchGroup, "select-instructors-one-button", "takeeval.switch.group.button");     
					} else if (instructorsSel.equals(EvalAssignHierarchy.SELECTION_ONE)) {
					    // TODO use the more efficient method: commonLogic.getEvalUsersByIds(userIds);
					    for (String userId : instructors) {
					        EvalUser user = commonLogic.getEvalUserById( userId );
					        v.add(user.userId);
					        l.add(user.displayName);
					    }
					    UIBranchContainer showSwitchGroup = UIBranchContainer.make(tofill, "select-instructors-one:");
					    UIOutput.make(showSwitchGroup, "select-instructors-one-header");
					    UISelect.make(showSwitchGroup, "select-instructors-one-list", v.toArray(new String[v.size()]), l.toArray(new String[l.size()]),  "#{evalUserId}");
					    UIMessage.make(showSwitchGroup, "select-instructors-one-button", "takeeval.switch.group.button");             
					} else {
                        throw new IllegalStateException("Invalid selection option ("+instructorsSel+"): do not know how to handle this");
                    }
				}


                // loop through the TIGs and handle each associated category
                for (TemplateItemGroup tig : tidl.getTemplateItemGroups()) {
                    UIBranchContainer categorySectionBranch = UIBranchContainer.make(form, "categorySection:");
                    // handle printing the category header
                    if (EvalConstants.ITEM_CATEGORY_COURSE.equals(tig.associateType) && !((Boolean)evalSettings.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY))) {
                        UIMessage.make(categorySectionBranch, "categoryHeader", "takeeval.group.questions.header");
                    } else if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(tig.associateType)) {
                        EvalUser user = commonLogic.getEvalUserById( tig.associateId );
                        // TODO header variable is unused, what does the code you added do, put in comments!
                        UIMessage header = UIMessage.make(categorySectionBranch, "categoryHeader", 
                                "takeeval.instructor.questions.header", new Object[] { user.displayName });
                        categorySectionBranch.decorators = new DecoratorList(new UIFreeAttributeDecorator(new String[]{"name", "class"}, new String[]{user.userId, "instructorBranch"}));
                        if(!instructorsSel.equals(EvalAssignHierarchy.SELECTION_ALL)){
                        	Map<String, String> css = new HashMap<String, String>();
                        	css.put("display", "none");
                        	categorySectionBranch.decorators.add(new UICSSDecorator(css));
                        }
                    } else if (EvalConstants.ITEM_CATEGORY_ASSISTANT.equals(tig.associateType)) {
                        EvalUser user = commonLogic.getEvalUserById( tig.associateId );
                        // TODO header variable is unused, what does the code you added do, put in comments!
                        UIMessage header = UIMessage.make(categorySectionBranch, "categoryHeader", 
                                "takeeval.ta.questions.header", new Object[] { user.displayName });
                        categorySectionBranch.decorators = new DecoratorList(new UIFreeAttributeDecorator(new String[]{"name", "class"}, new String[]{user.userId, "taBranch"}));
                        if(!instructorsSel.equals(EvalAssignHierarchy.SELECTION_ALL)){
                        	Map<String, String> css = new HashMap<String, String>();
                        	css.put("display", "none");
                        	categorySectionBranch.decorators.add(new UICSSDecorator(css));
                        }
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

                            renderItemPrep(nodeItemsBranch, form, dti, eval);
                        }
                    }

                }

                UICommand.make(form, "submitEvaluation", UIMessage.make("takeeval.submit.button"), "#{takeEvalBean.submitEvaluation}");
            } else {
                // user cannot access eval so give them a sad message
                EvalUser current = commonLogic.getEvalUserById(currentUserId);
                UIMessage.make(tofill, "eval-cannot-take-message", "takeeval.user.cannot.take", 
                        new String[] {current.displayName, current.email, current.username});
                log.info("User ("+currentUserId+") cannot take evaluation: " + eval.getId());
            }
        }
    }

    /**
     * Prepare to render an item, this handles blocks correctly
     * 
     * @param parent the parent container
     * @param form the form this item will associate with
     * @param dti the wrapped template item we will render
     */
    private void renderItemPrep(UIBranchContainer parent, UIForm form, DataTemplateItem dti, EvalEvaluation eval) {
        int displayIncrement = 0; // stores the increment in the display number
        String[] currentAnswerOTP = null; // holds array of bindings for items
        EvalTemplateItem templateItem = dti.templateItem;
        if (! TemplateItemUtils.isAnswerable(templateItem)) {
            // nothing to bind for unanswerable items unless it is a block parent
            if ( dti.blockChildItems != null ) {
                // Handle the BLOCK PARENT special case - block item being rendered

                // get the child items for this block
                List<EvalTemplateItem> childList = dti.blockChildItems;
                currentAnswerOTP = new String[childList.size()];
                // for each child item, construct a binding
                for (int j = 0; j < childList.size(); j++) {
                    EvalTemplateItem currChildItem = childList.get(j);
                    // set up OTP paths
                    String[] childAnswerOTP = setupCurrentAnswerBindings(form, currChildItem, dti.associateId);
                    if (childAnswerOTP != null) {
                        currentAnswerOTP[j] = childAnswerOTP[0];
                    }
                    renderedItemCount++;
                }
                displayIncrement = currentAnswerOTP.length;
            }
        } else {
            // non-block and answerable items
            currentAnswerOTP = setupCurrentAnswerBindings(form, templateItem, dti.associateId);
            displayIncrement++;
        }

        // render the item
        Map<String, String> evalProps = new HashMap<String, String>();
        Boolean answerRequired = true;
        if (eval.getBlankResponsesAllowed().booleanValue()) {
            answerRequired = false;
        }
        evalProps.put(ItemRenderer.EVAL_PROP_ANSWER_REQUIRED, answerRequired.toString());

        itemRenderer.renderItem(parent, "renderedItem:", currentAnswerOTP, templateItem, displayNumber, false, evalProps);

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
            String key = TemplateItemUtils.makeTemplateItemAnswerKey(templateItem.getId(), associatedType, associatedId);
            EvalAnswer currAnswer = answerMap.get(key);
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
        String[] bindings = new String[3];
        // set the primary binding depending on the item type
        String itemType = TemplateItemUtils.getTemplateItemType(templateItem);
        if ( EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(itemType) ) {
            bindings[0] = currAnswerOTP + "multipleAnswers";
        } else if ( EvalConstants.ITEM_TYPE_TEXT.equals(itemType) ) {
            bindings[0] = currAnswerOTP + "text";
        } else {
            // this is the default binding (scaled and MC)
            bindings[0] = currAnswerOTP + "numeric";
        }
        // set the NA and comment bindings
        bindings[1] = currAnswerOTP + "NA";
        bindings[2] = currAnswerOTP + "comment";
        return bindings;
    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new EvalViewParameters();
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
     */
    public List<NavigationCase> reportNavigationCases() {
        List<NavigationCase> i = new ArrayList<NavigationCase>();
        i.add(new NavigationCase("success", new SimpleViewParameters(SummaryProducer.VIEW_ID)));
        return i;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.ActionResultInterceptor#interceptActionResult(uk.org.ponder.rsf.flow.ARIResult, uk.org.ponder.rsf.viewstate.ViewParameters, java.lang.Object)
     */
    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        EvalViewParameters etvp = (EvalViewParameters) incoming;
        if (etvp.evalCategory != null) {
            result.resultingView = new EvalCategoryViewParameters(ShowEvalCategoryProducer.VIEW_ID, etvp.evalCategory);
        }
    }


}
