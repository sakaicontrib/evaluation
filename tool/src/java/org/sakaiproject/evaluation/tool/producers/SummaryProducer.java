/******************************************************************************
 * SummaryProducer.java - created by aaronz@vt.edu on Nov 10, 2006
 *
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * The summary producer rewrite This creates a summary page for any user of the
 * evaluation system and is the starting page for anyone entering the system
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class SummaryProducer implements ViewComponentProducer, DefaultView, NavigationCaseReporter {

    private final int maxGroupsToDisplay = 5;

    public static final String VIEW_ID = "summary";

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

    private EvalEvaluationSetupService evaluationSetupService;

    public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
        this.evaluationSetupService = evaluationSetupService;
    }

    private EvalDeliveryService deliveryService;

    public void setDeliveryService(EvalDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    private EvalSettings settings;

    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

    private EvalBeanUtils evalBeanUtils;

    public void setEvalBeanUtils(EvalBeanUtils evalBeanUtils) {
        this.evalBeanUtils = evalBeanUtils;
    }

    private Locale locale;

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder
     * .rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters,
     * uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        String currentGroup = commonLogic.getCurrentEvalGroup();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
        boolean createTemplate = authoringService.canCreateTemplate(currentUserId);
        boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);
        // use a date which is related to the current users locale
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

        /*
         * top links here
         */
        UIInternalLink.make(tofill, "summary-link", UIMessage.make("summary.page.title"), new SimpleViewParameters(SummaryProducer.VIEW_ID));

        if (userAdmin) {
            UIInternalLink.make(tofill, "administrate-link", UIMessage.make("administrate.page.title"), new SimpleViewParameters(AdministrateProducer.VIEW_ID));
            UIInternalLink.make(tofill, "control-scales-link", UIMessage.make("controlscales.page.title"), new SimpleViewParameters(
                    ControlScalesProducer.VIEW_ID));
        }

        if (createTemplate) {
            UIInternalLink.make(tofill, "control-templates-link", UIMessage.make("controltemplates.page.title"), new SimpleViewParameters(
                    ControlTemplatesProducer.VIEW_ID));
	      if (!((Boolean)settings.get(EvalSettings.DISABLE_ITEM_BANK))) {
		    UIInternalLink
			    .make(tofill, "control-items-link", UIMessage.make("controlitems.page.title"), new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
		}
        }

        if (beginEvaluation) {
            UIInternalLink.make(tofill, "control-evaluations-link", UIMessage.make("controlevaluations.page.title"), new SimpleViewParameters(
                    ControlEvaluationsProducer.VIEW_ID));
            UIMessage.make(tofill, "instructor-instructions", "summary.instructor.instruction");
        }

        /*
         * Notification box listing box
         */
        boolean userHasNotifications = false;
        if (userHasNotifications) {
            UIBranchContainer notificationsBC = UIBranchContainer.make(tofill, "notificationsBox:");
            UIMessage.make(notificationsBC, "notifications-title", "summary.notifications.title");
            UIMessage.make(notificationsBC, "notifications-higher-level", "summary.eval.assigned.from.above");
            // add other stuff
        }

        /*
         * for the evaluations taking box
         * FIXME - this is really inefficient
         */
        List<EvalEvaluation> evalsToTake = evaluationSetupService.getEvaluationsForUser(currentUserId, true, null, null);
        UIBranchContainer evalBC = UIBranchContainer.make(tofill, "evaluationsBox:");
        if (evalsToTake.size() > 0) {
            // build an array of evaluation ids
            Long[] evalIds = new Long[evalsToTake.size()];
            for (int i = 0; i < evalsToTake.size(); i++) {
                evalIds[i] = ((EvalEvaluation) evalsToTake.get(i)).getId();
            }

            // http://jira.sakaiproject.org/jira/browse/EVALSYS-588
            // Map<Long, List<EvalGroup>> evalGroups =
            // evaluationService.getEvalGroupsForEval(evalIds, false, null);
            List<EvalGroup> groups = commonLogic.getEvalGroupsForUser(currentUserId, EvalConstants.PERM_TAKE_EVALUATION);
            // now fetch all the information we care about for these evaluations
            // at once (for speed)

            List<EvalResponse> evalResponses = deliveryService.getEvaluationResponsesForUser(currentUserId, evalIds, true);

            for (Iterator<EvalEvaluation> itEvals = evalsToTake.iterator(); itEvals.hasNext();) {
                EvalEvaluation eval = (EvalEvaluation) itEvals.next();

                UIBranchContainer evalrow = UIBranchContainer.make(evalBC, "evaluationsList:", eval.getId().toString());

                UIOutput.make(evalrow, "evaluationTitleTitle", EvalUtils.makeMaxLengthString(eval.getTitle(), 70));
                UIMessage.make(evalrow, "evaluationCourseEvalTitle", "summary.evaluations.courseeval.title");
                UIMessage.make(evalrow, "evaluationStartsTitle", "summary.evaluations.starts.title");
                UIMessage.make(evalrow, "evaluationEndsTitle", "summary.evaluations.ends.title");

                // http://jira.sakaiproject.org/jira/browse/EVALSYS-588
                // List<EvalGroup> groups = evalGroups.get(eval.getId());
                for (int j = 0; j < groups.size(); j++) {
                    EvalGroup group = (EvalGroup) groups.get(j);
                    if (EvalConstants.GROUP_TYPE_INVALID.equals(group.type)) {
                        continue; // skip processing for invalid groups
                    }

                    // http://jira.sakaiproject.org/jira/browse/EVALSYS-588
                    // if (commonLogic.isUserAllowedInEvalGroup(currentUserId,
                    // EvalConstants.PERM_TAKE_EVALUATION, group.evalGroupId)) {
                    // check that the user can take evaluations in this
                    // evalGroupId
                    if (evaluationService.isEvalGroupValidForEvaluation(group.evalGroupId, eval.getId())) {
                        String groupId = group.evalGroupId;
                        String title = EvalUtils.makeMaxLengthString(group.title, 50);
                        String status = "unknown.caps";

                        // find the object in the list matching the evalGroupId
                        // and evalId,
                        // leave as null if not found -AZ
                        EvalResponse response = null;
                        for (int k = 0; k < evalResponses.size(); k++) {
                            EvalResponse er = (EvalResponse) evalResponses.get(k);
                            if (groupId.equals(er.getEvalGroupId()) && eval.getId().equals(er.getEvaluation().getId())) {
                                response = er;
                                break;
                            }
                        }

                        if (groupId.equals(currentGroup)) {
                            // TODO - do something when the evalGroupId matches
                        }

                        UIBranchContainer evalcourserow = UIBranchContainer.make(evalrow, "evaluationsCourseList:", groupId);

                        // set status
                        if (response != null && response.getEndTime() != null) {
                            // there is a response for this eval/group
                            status = "summary.status.completed";
                            if (eval.getModifyResponsesAllowed().booleanValue()) {
                                // can modify responses so show the link still
                                // take eval link when pending
                                UIInternalLink.make(evalcourserow, "evaluationCourseLink", title, new EvalViewParameters(TakeEvalProducer.VIEW_ID,
                                        eval.getId(), response.getId(), groupId));
                            } else {
                                // show title only when completed and cannot
                                // modify
                                UIOutput.make(evalcourserow, "evaluationCourseLink_disabled", title);
                            }
                        } else {
                            // no response yet for this eval/group
                            // take eval link when pending
                            UIInternalLink.make(evalcourserow, "evaluationCourseLink", title, new EvalViewParameters(TakeEvalProducer.VIEW_ID, eval.getId(),
                                    groupId));
                            status = "summary.status.pending";
                        }
                        UIMessage.make(evalcourserow, "evaluationCourseStatus", status);
                        // moved down here as requested by UI design
                        UIOutput.make(evalcourserow, "evaluationStartDate", df.format(eval.getStartDate()));
                        UIOutput.make(evalcourserow, "evaluationDueDate", df.format(eval.getDueDate()));
                    }
                }
            }
        } else {
            UIMessage.make(tofill, "evaluationsNone", "summary.evaluations.none");
        }

        /*
         * for the evaluations admin box
         */
        Boolean instViewResults = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
        if (instViewResults == null) {
            instViewResults = true;
        } // if configurable then we will assume some are probably shared
        List<EvalEvaluation> evals = evaluationSetupService.getVisibleEvaluationsForUser(currentUserId, true, instViewResults, false);
        /*
         * If the person is an admin, then just point new evals to existing
         * object. If the person is not an admin then only show owned evals +
         * not-owned evals that are available for viewing results.
         */
        List<EvalEvaluation> newEvals = evals;
        if (instViewResults && !userAdmin) {
            newEvals = new ArrayList<EvalEvaluation>();
            for (EvalEvaluation evaluation : evals) {
                // Add the owned evals
                if (currentUserId.equals(evaluation.getOwner())) {
                    newEvals.add(evaluation);
                } else {
                    // From the not-owned evals show those that are available
                    // for viewing results
                    if (EvalUtils.checkStateAfter(evaluation.getState(), EvalConstants.EVALUATION_STATE_VIEWABLE, true)) {
                        newEvals.add(evaluation);
                    }
                }
            }
        }

        if (!newEvals.isEmpty()) {
            UIBranchContainer evalAdminBC = UIBranchContainer.make(tofill, "evalAdminBox:");
            // Temporary fix for http://www.caret.cam.ac.uk/jira/browse/CTL-583
            // (need to send them to the eval control page eventually) -AZ
            if (beginEvaluation) {
                UIInternalLink.make(evalAdminBC, "evaladmin-title-link", UIMessage.make("summary.evaluations.admin"), new SimpleViewParameters(
                        ControlEvaluationsProducer.VIEW_ID));
            } else {
                UIMessage.make(evalAdminBC, "evaladmin-title", "summary.evaluations.admin");
            }
            UIForm evalAdminForm = UIForm.make(evalAdminBC, "evalAdminForm");

            UIMessage.make(evalAdminForm, "evaladmin-header-title", "summary.header.title");
            UIMessage.make(evalAdminForm, "evaladmin-header-status", "summary.header.status");
            UIMessage.make(evalAdminForm, "evaladmin-header-date", "summary.header.date");

            for (Iterator<EvalEvaluation> iter = newEvals.iterator(); iter.hasNext();) {
                EvalEvaluation eval = (EvalEvaluation) iter.next();

                UIBranchContainer evalrow = UIBranchContainer.make(evalAdminForm, "evalAdminList:", eval.getId().toString());

                String evalState = evaluationService.updateEvaluationState(eval.getId());
                if (EvalConstants.EVALUATION_STATE_INQUEUE.equals(evalState)) {
                    // If we are in the queue we are yet to start -- so say when
                    // we will

                    UIMessage.make(evalrow, "evalAdminDateLabel", "summary.label.starts");
                    UIOutput.make(evalrow, "evalAdminDate", df.format(eval.getStartDate()));

                    UIMessage.make(evalrow, "evalAdminStatus", "summary.status." + evalState);
                } else if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(evalState)) {
                    // Active evaluations can either be open forever or close at
                    // some point:
                    if (eval.getDueDate() != null) {
                        UIMessage.make(evalrow, "evalAdminDateLabel", "summary.label.due");
                        UIOutput.make(evalrow, "evalAdminDate", df.format(eval.getDueDate()));
                        // Should probably add something here if there's a grace
                        // period
                    } else {
                        UIMessage.make(evalrow, "evalAdminDateLabel", "summary.label.nevercloses");
                    }

                    UIMessage.make(evalrow, "evalAdminStatus", "summary.status." + evalState);
                } else if (EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(evalState)) {
                    // Evaluations can have a grace period, if so that must
                    // close at some point;
                    // Grace periods never remain open forever
                    UIMessage.make(evalrow, "evalAdminDateLabel", "summary.label.gracetill");
                    UIOutput.make(evalrow, "evalAdminDate", df.format(eval.getStopDate()));

                    UIMessage.make(evalrow, "evalAdminStatus", "summary.status." + evalState);
                } else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(evalState)) {
                    // if an evaluation is closed then it is not yet viewable
                    // and ViewDate must have been set
                    UIMessage.make(evalrow, "evalAdminDateLabel", "summary.label.resultsviewableon");
                    UIOutput.make(evalrow, "evalAdminDate", df.format(eval.getViewDate()));

                    UIMessage.make(evalrow, "evalAdminStatus", "summary.status." + evalState);
                } else if (EvalConstants.EVALUATION_STATE_VIEWABLE.equals(evalState)) {
                    // FIXME if an evaluation is viewable we may want to notify
                    // if
                    // there are instructor/student dates
                    UIMessage.make(evalrow, "evalAdminDateLabel", "summary.label.resultsviewablesince");
                    UIOutput.make(evalrow, "evalAdminDate", df.format(evalViewableOn(eval)));

                    int responsesCount = deliveryService.countResponses(eval.getId(), null, true);
                    int enrollmentsCount = evaluationService.countParticipantsForEval(eval.getId(), null);
                    int responsesNeeded = evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount);
                    if (responsesNeeded == 0) {
                        UIInternalLink.make(evalrow, "viewReportLink", UIMessage.make("viewreport.page.title"), new ReportParameters(
                                ReportChooseGroupsProducer.VIEW_ID, eval.getId()));
                    } else {
                        UIMessage.make(evalrow, "evalAdminStatus", "summary.status." + evalState).decorate(
                                new UITooltipDecorator(UIMessage.make("controlevaluations.eval.report.awaiting.responses", new Object[] { responsesNeeded })));
                    }
                } else {
                    UIMessage.make(evalrow, "evalAdminDateLabel", "summary.label.fallback");
                    UIOutput.make(evalrow, "evalAdminDate", df.format(eval.getStartDate()));
                }

                /*
                 * 1) if a evaluation is queued, title link go to EditSettings
                 * page with populated data 2) if a evaluation is active, title
                 * link go to EditSettings page with populated data but start
                 * date should be disabled 3) if a evaluation is closed, title
                 * link go to previewEval page with populated data
                 */
                if (EvalUtils.checkStateAfter(evalState, EvalConstants.EVALUATION_STATE_CLOSED, true)) {
                    UIInternalLink.make(evalrow, "evalAdminTitleLink_preview", EvalUtils.makeMaxLengthString(eval.getTitle(), 70), new EvalViewParameters(
                            PreviewEvalProducer.VIEW_ID, eval.getId(), eval.getTemplate().getId()));
                } else {
                    UIInternalLink.make(evalrow, "evalAdminTitleLink_edit", EvalUtils.makeMaxLengthString(eval.getTitle(), 70), new EvalViewParameters(
                            EvaluationSettingsProducer.VIEW_ID, eval.getId()));
                }

            }
        }

        /*
         * Site/Group listing box
         */
        Boolean enableSitesBox = (Boolean) settings.get(EvalSettings.ENABLE_SUMMARY_SITES_BOX);
        if (enableSitesBox) {
            // only show this if we cannot find our location OR if the option is
            // forced to on
            String NO_ITEMS = "no.list.items";

            UIBranchContainer contextsBC = UIBranchContainer.make(tofill, "siteListingBox:");
            UIMessage.make(contextsBC, "sitelisting-title", "summary.sitelisting.title");

            UIMessage.make(contextsBC, "sitelisting-evaluated-text", "summary.sitelisting.evaluated");
            List<EvalGroup> evaluatedGroups = commonLogic.getEvalGroupsForUser(currentUserId, EvalConstants.PERM_BE_EVALUATED);
            if (evaluatedGroups.size() > 0) {
                for (int i = 0; i < evaluatedGroups.size(); i++) {
                    if (i > maxGroupsToDisplay) {
                        UIMessage.make(contextsBC, "evaluatedListNone", "summary.sitelisting.maxshown", new Object[] { new Integer(evaluatedGroups.size()
                                - maxGroupsToDisplay) });
                        break;
                    }
                    UIBranchContainer evaluatedBC = UIBranchContainer.make(contextsBC, "evaluatedList:", i + "");
                    EvalGroup c = (EvalGroup) evaluatedGroups.get(i);
                    UIOutput.make(evaluatedBC, "evaluatedListTitle", c.title);
                }
            } else {
                UIMessage.make(contextsBC, "evaluatedListNone", NO_ITEMS);
            }

            UIMessage.make(contextsBC, "sitelisting-evaluate-text", "summary.sitelisting.evaluate");
            List<EvalGroup> evaluateGroups = commonLogic.getEvalGroupsForUser(currentUserId, EvalConstants.PERM_TAKE_EVALUATION);
            if (evaluateGroups.size() > 0) {
                for (int i = 0; i < evaluateGroups.size(); i++) {
                    if (i > maxGroupsToDisplay) {
                        UIMessage.make(contextsBC, "evaluateListNone", "summary.sitelisting.maxshown", new Object[] { new Integer(evaluateGroups.size()
                                - maxGroupsToDisplay) });
                        break;
                    }
                    UIBranchContainer evaluateBC = UIBranchContainer.make(contextsBC, "evaluateList:", i + "");
                    EvalGroup c = (EvalGroup) evaluateGroups.get(i);
                    UIOutput.make(evaluateBC, "evaluateListTitle", c.title);
                }
            } else {
                UIMessage.make(contextsBC, "evaluateListNone", NO_ITEMS);
            }
        }

        /*
         * For the Evaluation tools box
         */
        if (createTemplate || beginEvaluation) {
            UIBranchContainer toolsBC = UIBranchContainer.make(tofill, "toolsBox:");
            UIMessage.make(toolsBC, "tools-title", "summary.tools.title");

            if (createTemplate) {
                UIInternalLink.make(toolsBC, "createTemplateLink", UIMessage.make("createtemplate.page.title"), new TemplateViewParameters(
                        ModifyTemplateProducer.VIEW_ID, null));
            }

            if (beginEvaluation) {
                UIInternalLink.make(toolsBC, "beginEvaluationLink", UIMessage.make("starteval.page.title"), new EvalViewParameters(
                        EvaluationCreateProducer.VIEW_ID, null));
            }
        }

    }

    /**
     * Find out when an evaluation became first viewable.
     * 
     * @param eval
     *            an evaluation
     * @return the date results were first viewable on
     */
    private Date evalViewableOn(EvalEvaluation eval) {
        if (eval.getViewDate() != null) {
            return eval.getViewDate();
        } else if (eval.getStopDate() != null) {
            return eval.getStopDate();
        } else {
            return eval.getDueDate();
        }
    }

    /**
     * Gets a date to display to the user depending on the state, guarantees to
     * return a date even if the dates are null
     * 
     * FIXME this method seems to be wrong.
     * 
     * @param eval
     *            an evaluation (must be saved already)
     * @param evalState
     *            the state which you want to get the date for, e.g.
     *            EVALUATION_STATE_VIEWABLE would get the view date and
     *            EVALUATION_STATE_CLOSED would get the due date
     * @return a displayable date
     */
    /*
     * private Date getDisplayableDate(EvalEvaluation eval, String evalState) {
     * Date date = null; if (eval.getViewDate() != null &&
     * EvalConstants.EVALUATION_STATE_VIEWABLE.equals(evalState)) { date =
     * eval.getViewDate(); } else { if
     * (EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(evalState)) { if
     * (eval.getStopDate() != null) { date = eval.getStopDate(); } else if
     * (eval.getDueDate() != null) { date = eval.getDueDate(); } else { // FIXME
     * there's no due date so we give out a finishing date of the start?! date =
     * eval.getStartDate(); } } else { if (eval.getDueDate() != null &&
     * EvalConstants.EVALUATION_STATE_CLOSED.equals(evalState)) { date =
     * eval.getDueDate(); } else { date = eval.getStartDate(); } } } return
     * date; }
     */

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases
     * ()
     */
    @SuppressWarnings("unchecked")
    public List reportNavigationCases() {
        List i = new ArrayList();
        i.add(new NavigationCase(EvaluationSettingsProducer.VIEW_ID, new SimpleViewParameters(EvaluationSettingsProducer.VIEW_ID)));
        i.add(new NavigationCase(PreviewEvalProducer.VIEW_ID, new SimpleViewParameters(PreviewEvalProducer.VIEW_ID)));
        return i;
    }

}
