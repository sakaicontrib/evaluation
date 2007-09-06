/******************************************************************************
 * TakeEvalProducer.java - created on Sept 18, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.awt.Color;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.utils.EvalUtils;
import org.sakaiproject.evaluation.logic.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.LocalResponsesLogic;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.viewparams.EvalCategoryViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.EvalTakeViewParameters;

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
import uk.org.ponder.rsf.components.decorators.UIColourDecorator;
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

    // removed original authors for writing code that does not even work -AZ

    public static final String VIEW_ID = "take_eval";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalExternalLogic external;
    public void setExternal(EvalExternalLogic external) {
        this.external = external;
    }

    private EvalEvaluationsLogic evalsLogic;
    public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
        this.evalsLogic = evalsLogic;
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


    // TODO - clean up this mess of otp binding strings
    String responseOTPBinding = "responseBeanLocator";
    String responseOTP = responseOTPBinding + ".";
    String newResponseOTPBinding = responseOTP + "new";
    String newResponseOTP = newResponseOTPBinding + ".";

    String responseAnswersOTPBinding = "responseAnswersBeanLocator";
    String responseAnswersOTP = responseAnswersOTPBinding + ".";
    String newResponseAnswersOTPBinding = responseAnswersOTP + "new";
    String newResponseAnswersOTP = newResponseAnswersOTPBinding + ".";    

    String evalOTPBinding = "evaluationBeanLocator";
    String evalOTP = evalOTPBinding+".";

    Long responseId;
    Long evaluationId;
    String evalGroupId;

    int displayNumber=1;
    int renderedItemCount=0;

    List allItems; // should be set to a list of all evaluation items


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        boolean canAccess = false;
        boolean userCanAccess = false;

        String currentUserId = external.getCurrentUserId();
        // use a date which is related to the current users locale
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

        UIMessage.make(tofill, "page-title", "takeeval.page.title");

        UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"), 
                new SimpleViewParameters(SummaryProducer.VIEW_ID));			

        // get passed in get params
        EvalTakeViewParameters evalTakeViewParams = (EvalTakeViewParameters) viewparams;
        evaluationId = evalTakeViewParams.evaluationId;
        evalGroupId = evalTakeViewParams.evalGroupId;
        responseId = evalTakeViewParams.responseId;

        // get the evaluation based on the passed in VPs
        EvalEvaluation eval = evalsLogic.getEvaluationById(evaluationId);
        if (eval == null) {
            throw new IllegalArgumentException("Invalid evaluationId ("+evaluationId+"), cannot load evaluation");
        }

        UIMessage.make(tofill, "eval-title-header", "takeeval.eval.title.header");
        UIOutput.make(tofill, "evalTitle", eval.getTitle());

        // check the states of the evaluation first to give the user a tip that this eval is not takeable,
        // also avoids wasting time checking permissions when the evaluation certainly is closed
        String evalStatus = evalsLogic.getEvaluationState(evaluationId); // make sure state is up to date
        if (EvalConstants.EVALUATION_STATE_INQUEUE.equals(evalStatus)) {
            UIMessage.make(tofill, "eval-cannot-take-message", "takeeval.eval.not.open", 
                    new String[] {df.format(eval.getStartDate()), df.format(eval.getDueDate())} );
        } else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(evalStatus) ||
                EvalConstants.EVALUATION_STATE_VIEWABLE.equals(evalStatus)) {
            UIMessage.make(tofill, "eval-cannot-take-message", "takeeval.eval.closed",
                    new String[] {df.format(eval.getDueDate())} );
        } else {
            // eval state is possible to take eval
            canAccess = true;
        }

        if (canAccess) {
            // eval is accessible so check user can take it
            if (evalGroupId != null) {
                // there was an eval group passed in so make sure things are ok
                if (evalsLogic.canTakeEvaluation(currentUserId, evaluationId, evalGroupId)) {
                    userCanAccess = true;
                }
            } else {
                // select the first eval group the current user can take evaluation in,
                // also store the total number so we can give the user a list to choose from if there are more than one
                Map m = evalsLogic.getEvaluationAssignGroups(new Long[] {evaluationId}, false);
                List<EvalGroup> validGroups = new ArrayList<EvalGroup>(); // stores EvalGroup objects
                if ( external.isUserAdmin(currentUserId) ) {
                    // special case, the super admin can always access
                    userCanAccess = true;
                    List assignGroups = (List) m.get(evaluationId);
                    for (int i = 0; i < assignGroups.size(); i++) {
                        EvalAssignGroup assignGroup = (EvalAssignGroup) assignGroups.get(i);
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
                        List assignGroups = (List) m.get(evaluationId);
                        evalGroups = new EvalGroup[assignGroups.size()];
                        for (int i = 0; i < assignGroups.size(); i++) {
                            EvalAssignGroup assignGroup = (EvalAssignGroup) assignGroups.get(i);
                            evalGroups[i] = external.makeEvalGroupObject( assignGroup.getEvalGroupId() );
                        }
                    } else {
                        evalGroups = EvalUtils.getGroupsInCommon(
                                external.getEvalGroupsForUser(currentUserId, EvalConstants.PERM_TAKE_EVALUATION), 
                                (List) m.get(evaluationId) );
                    }
                    for (int i = 0; i < evalGroups.length; i++) {
                        EvalGroup group = evalGroups[i];
                        if (evalsLogic.canTakeEvaluation(currentUserId, evaluationId, group.evalGroupId)) {
                            if (evalGroupId == null) {
                                // set the evalGroupId to the first valid group if unset
                                evalGroupId = group.evalGroupId;
                                userCanAccess = true;
                            }
                            validGroups.add( external.makeEvalGroupObject(group.evalGroupId) );
                        }
                    }
                }

                // generate the get form to allow the user to choose a group if more than one is available
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
                    UISelect.make(chooseGroupForm, "switch-group-list", values, labels,	"#{evalGroupId}");
                    UIMessage.make(chooseGroupForm, "switch-group-button", "takeeval.switch.group.button");
                }
            }
        }

        if (userCanAccess) {
            // fill in group title
            UIBranchContainer groupTitle = UIBranchContainer.make(tofill, "show-group-title:");
            UIMessage.make(groupTitle, "group-title-header", "takeeval.group.title.header");	
            UIOutput.make(groupTitle, "group-title", external.getDisplayTitle(evalGroupId) );

            // show instructions if not null
            if (eval.getInstructions() != null) {
                UIBranchContainer instructions = UIBranchContainer.make(tofill, "show-eval-instructions:");
                UIMessage.make(instructions, "eval-instructions-header", "takeeval.instructions.header");	
                UIVerbatim.make(instructions, "eval-instructions", eval.getInstructions());
            }

            if ( ((Boolean)evalSettings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED)).booleanValue() == false ) {
                // all items must be completed so show warning
                UIBranchContainer note = UIBranchContainer.make(tofill, "show-eval-note:");
                UIMessage.make(note, "eval-note-header", "general.note");   
                UIMessage.make(note, "eval-note-text", "takeeval.user.must.answer.all");   
            }

            UIBranchContainer formBranch = UIBranchContainer.make(tofill, "form-branch:");
            UIForm form = UIForm.make(formBranch, "evaluationForm");

            // bind the evaluation and evalGroup to the ones in the take eval bean
            form.parameters.add( new UIELBinding("#{takeEvalBean.eval}", new ELReference(evalOTP + eval.getId())) );
            form.parameters.add( new UIELBinding("#{takeEvalBean.evalGroupId}", evalGroupId) );

            // get all items for this evaluation main template
            allItems = new ArrayList(eval.getTemplate().getTemplateItems());

            //filter out the block child items, to get a list non-child items
            List ncItemsList = TemplateItemUtils.getNonChildItems(allItems);

            // load up the previous responses for this user
            Map answerMap = new HashMap();
            if (responseId != null) {
                answerMap = localResponsesLogic.getAnswersMapByTempItemAndAssociated(responseId);
            }

            if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_COURSE, ncItemsList)) {
                // for all course items, go through render process
                UIBranchContainer courseSection = UIBranchContainer.make(form, "courseSection:");
                UIMessage.make(courseSection, "course-questions-header", "takeeval.group.questions.header");
                // for each non-child item in this evaluation
                for (int i = 0; i <ncItemsList.size(); i++) {
                    EvalTemplateItem templateItem = (EvalTemplateItem) ncItemsList.get(i);
                    String cat = templateItem.getItemCategory();
                    UIBranchContainer radiobranch = null;

                    // if the given item is of type course, render it here
                    if (cat.equals(EvalConstants.ITEM_CATEGORY_COURSE)) {
                        radiobranch = UIBranchContainer.make(courseSection, "itemrow:first", i+"");
                        if (i % 2 == 1) {
                        	radiobranch.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") );// must match the existing CSS class
                        }
                        renderItemPrep(radiobranch, form, templateItem, answerMap, "null", "null");
                    }
                }
                UIMessage.make(courseSection, "course-questions-header", "takeeval.group.questions.header");
            }

            if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, ncItemsList)) {	
                // for each instructor, make a branch containing all instructor questions
                Set instructors = external.getUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_BE_EVALUATED);
                for (Iterator it = instructors.iterator(); it.hasNext();) {
                    String instructor = (String) it.next();
                    UIBranchContainer instructorSection = UIBranchContainer.make(form, "instructorSection:", "inst"+displayNumber);
                    UIMessage.make(instructorSection, "instructor-questions-header", 
                            "takeeval.instructor.questions.header", new Object[] { external.getUserDisplayName(instructor) });
                    // for each non-child item in this evaluation
                    for (int i = 0; i <ncItemsList.size(); i++) {
                        EvalTemplateItem templateItem = (EvalTemplateItem) ncItemsList.get(i);
                        String category = templateItem.getItemCategory();
                        UIBranchContainer radiobranch = null;

                        // if the given item is of type instructor, render it here
                        if ( EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(category) ) {
                            radiobranch = UIBranchContainer.make(instructorSection,	"itemrow:first", i+"");
                            if (i % 2 == 1) {
                            	radiobranch.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") );// must match the existing CSS class
                            }
                            renderItemPrep(radiobranch, form, templateItem, answerMap, category, instructor);
                        }
                    } // end of for loop				
                }
            }

            UICommand.make(form, "submitEvaluation", UIMessage.make("takeeval.submit.button"), "#{takeEvalBean.submitEvaluation}");
        } else {
            // user cannot access eval so give them a sad message
            UIMessage.make(tofill, "eval-cannot-take-message", "takeeval.user.cannot.take");
        }
    }

    /**
     * Prep for rendering an item I assume (no comments by original authors) -AZ
     * 
     * @param radiobranch
     * @param form
     * @param templateItem
     * @param answerMap
     * @param itemCategory
     * @param associatedId
     */
    private void renderItemPrep(UIBranchContainer radiobranch, UIForm form, EvalTemplateItem templateItem, Map answerMap, String itemCategory, String associatedId) {
        // holds array of bindings for child block items
        String[] caOTP = null;
        if (templateItem.getBlockParent() != null && templateItem.getBlockParent().booleanValue() == true) {
            // block item being rendered

            // get the child items of tempItem1
            List childList = TemplateItemUtils.getChildItems(allItems, templateItem.getId());
            caOTP = new String[childList.size()];
            // for each child item, construct a binding
            for (int j = 0; j < childList.size(); j++) {
                EvalTemplateItem currChildItem = (EvalTemplateItem) childList.get(j);
                // set up OTP paths
                if (responseId == null) {
                    caOTP[j] = newResponseAnswersOTP + "new" + (renderedItemCount) + ".";
                } else {
                    // if the user has answered this question before, point at their response
                    EvalAnswer currAnswer = (EvalAnswer) answerMap.get(currChildItem.getId() + itemCategory	+ associatedId);

                    // there is an existing response, but there is no answer
                    if (currAnswer == null) {
                        caOTP[j] = responseAnswersOTP + responseId + "." + "new" + (renderedItemCount) + ".";
                    } else {
                        caOTP[j] = responseAnswersOTP + responseId + "." + currAnswer.getId() + ".";
                    }
                }

                // bind the current EvalTemplateItem's EvalItem to the current EvalAnswer's EvalItem
                form.parameters.add(new UIELBinding(caOTP[j] + "templateItem", 
                        new ELReference("templateItemWBL." + currChildItem.getId())));
                if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(itemCategory)
                        || EvalConstants.ITEM_CATEGORY_ENVIRONMENT.equals(itemCategory)) {
                    // bind the current instructor id to the current EvalAnswer.associated
                    form.parameters.add(new UIELBinding(caOTP[j] + "associatedId", associatedId));
                }
                // set up binding for UISelect
                caOTP[j] += "numeric";
                renderedItemCount++;
            }
        } else if ( EvalConstants.ITEM_TYPE_SCALED.equals(templateItem.getItem().getClassification())
                || EvalConstants.ITEM_TYPE_TEXT.equals(templateItem.getItem().getClassification()) ) {
            // single item being rendered

            // set up OTP paths for scaled/text type items
            String currAnswerOTP;
            if (responseId == null) {
                currAnswerOTP = newResponseAnswersOTP + "new" + renderedItemCount + ".";
            } else {
                // if the user has answered this question before, point at their response
                EvalAnswer currAnswer = (EvalAnswer) answerMap.get(templateItem.getId() + "null" + "null");

                if (currAnswer == null) {
                    currAnswerOTP = responseAnswersOTP + responseId + "." + "new" + (renderedItemCount) + ".";
                } else {
                    currAnswerOTP = responseAnswersOTP + responseId + "." + currAnswer.getId() + ".";
                }
            }
            // bind the current EvalTemplateItem's EvalItem to the current EvalAnswer's EvalItem
            form.parameters.add( new UIELBinding(currAnswerOTP + "templateItem", 
                    new ELReference("templateItemWBL." + templateItem.getId())) );
            if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(itemCategory)
                    || EvalConstants.ITEM_CATEGORY_ENVIRONMENT.equals(itemCategory)) {
                // bind the current instructor id to the current EvalAnswer.associated
                form.parameters.add(new UIELBinding(currAnswerOTP + "associatedId", associatedId));
            }
            // update curranswerOTP for binding the UI input element (UIInput, UISelect, etc.)
            if ( EvalConstants.ITEM_TYPE_SCALED.equals(templateItem.getItem().getClassification()) ) {
                caOTP = new String[] { currAnswerOTP + "numeric" };
            } else if ( EvalConstants.ITEM_TYPE_TEXT.equals(templateItem.getItem().getClassification()) ) {
                caOTP = new String[] { currAnswerOTP + "text" };
            }
        }

        // render the item
        itemRenderer.renderItem(radiobranch, "rendered-item:", caOTP, templateItem, displayNumber, false);

        // increment the item counters, if we displayed 1 item, increment by 1,
        // if we displayed a block, renderedItem has been incremented, increment displayNumber by the number of blockChildren,
        // this happens to coincide with the number of current answer OTP strings exactly -AZ
        if (caOTP != null) displayNumber += caOTP.length;
        renderedItemCount++;
    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new EvalTakeViewParameters();
    }

    /*
     * (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
     */
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
