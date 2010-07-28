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
import java.util.Map;

import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.messageutil.MessageLocator;
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
    
    private MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator messageLocator) {
    	this.messageLocator = messageLocator;
    }

    private Locale locale;
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
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

        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        
        navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());
        
        if (beginEvaluation) {
        	// show instructor instructions
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
         * http://jira.sakaiproject.org/jira/browse/EVALSYS-618
         * Changed this to reduce the load on the services and make this load faster
         */
        List<EvalEvaluation> evalsToTake = evaluationSetupService.getEvaluationsForUser(currentUserId, true, null, null);
        UIBranchContainer evalBC = UIBranchContainer.make(tofill, "evaluationsBox:");
        if (evalsToTake.size() > 0) {
            // build an array of evaluation ids
            Long[] evalIds = new Long[evalsToTake.size()];
            for (int i = 0; i < evalsToTake.size(); i++) {
                evalIds[i] = ((EvalEvaluation) evalsToTake.get(i)).getId();
            }

            List<EvalResponse> evalResponses = deliveryService.getEvaluationResponsesForUser(currentUserId, evalIds, true);

            for (Iterator<EvalEvaluation> itEvals = evalsToTake.iterator(); itEvals.hasNext();) {
                EvalEvaluation eval = (EvalEvaluation) itEvals.next();

                UIBranchContainer evalrow = UIBranchContainer.make(evalBC, "evaluationsList:", eval.getId().toString());

                UIOutput.make(evalrow, "evaluationTitleTitle", EvalUtils.makeMaxLengthString(eval.getTitle(), 70));

                for (EvalAssignGroup eag : eval.getEvalAssignGroups()) {
                    EvalGroup group = commonLogic.makeEvalGroupObject(eag.getEvalGroupId());
                    if (EvalConstants.GROUP_TYPE_INVALID.equals(group.type)) {
                        continue; // skip processing for invalid groups
                    }

                    String groupId = group.evalGroupId;
                    String title = EvalUtils.makeMaxLengthString(group.title, 50);
                    String status = "unknown.caps";

                    // find the object in the list matching the evalGroupId and evalId,
                    // leave as null if not found -AZ
                    EvalResponse response = null;
                    for (int k = 0; k < evalResponses.size(); k++) {
                        EvalResponse er = (EvalResponse) evalResponses.get(k);
                        if (groupId.equals(er.getEvalGroupId()) 
                                && eval.getId().equals(er.getEvaluation().getId())) {
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
                            UIInternalLink.make(evalcourserow, "evaluationCourseLink", title, 
                                    new EvalViewParameters(TakeEvalProducer.VIEW_ID,
                                            eval.getId(), response.getId(), groupId));
                        } else {
                            // show title only when completed and cannot
                            // modify
                            UIOutput.make(evalcourserow, "evaluationCourseLink_disabled", title);
                        }
                    } else {
                        // no response yet for this eval/group
                        // take eval link when pending
                        UIInternalLink.make(evalcourserow, "evaluationCourseLink", title, 
                                new EvalViewParameters(TakeEvalProducer.VIEW_ID, eval.getId(),
                                        groupId));
                        status = "summary.status.pending";
                    }
                    UIMessage.make(evalcourserow, "evaluationCourseStatus", status);
                    // moved down here as requested by UI design
                    UIOutput.make(evalcourserow, "evaluationStartDate", df.format(eval.getStartDate()));
                    UIOutput.make(evalcourserow, "evaluationDueDate", df.format(eval.getDueDate()));
                }
            }
        } else {
            UIMessage.make(tofill, "evaluationsNone", "summary.evaluations.none");
        }

        // determine whether instructor widget is enabled
        boolean showEvaluateeBox = ((Boolean) settings.get(EvalSettings.ENABLE_EVALUATEE_BOX)).booleanValue();
        boolean evalsToShow = false;
        if(showEvaluateeBox) {
        	// need to determine if there are any evals in which the user can be evaluated
        	List<EvalEvaluation> evalsForInstructor = this.evaluationSetupService.getEvaluationsForEvaluatee(currentUserId);
        	if(evalsForInstructor == null || evalsForInstructor.isEmpty()) {
        		// no evals found
        		// show a message saying no evals?
        	} else {
        		// evals found; show the widget
        		UIBranchContainer evalResponsesBC = UIBranchContainer.make(tofill, "evalResponsesBox:");
        		UIForm evalResponsesForm = UIForm.make(evalResponsesBC , "evalResponsesForm");

        		UIBranchContainer evalResponseTable = null;
        		// build an array of evaluation ids
        		Long[] evalIds = new Long[evalsForInstructor.size()];
        		int i = 0;
        		for(EvalEvaluation eval : evalsForInstructor) {
        			evalIds[i++] = eval.getId();
        		}

        		// get the eval groups
        		Map<Long, List<EvalGroup>> evalGroups = evaluationService.getEvalGroupsForEval(evalIds, false, null);

        		// show a list of evals with four columns: 
        		for(EvalEvaluation eval : evalsForInstructor) {
        			//is this eval partial, in-queue, active or grace period?
        			if(EvalConstants.EVALUATION_STATE_INQUEUE.equals(eval.getState()) ||
        					EvalConstants.EVALUATION_STATE_PARTIAL.equals(eval.getState()) ||
        					EvalConstants.EVALUATION_STATE_ACTIVE.equals(eval.getState()) ||
        					EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(eval.getState())) {
        				//there is an eval in-queue, active or grace period
        				evalsToShow = true;
        				if(evalResponseTable == null) {
        					evalResponseTable = UIBranchContainer.make(evalResponsesForm, "evalResponseTable:");
        	        		// show four column headings
        	        		UIMessage.make(evalResponseTable, "evalresponses-header-title","summary.header.title");
        	        		UIMessage.make(evalResponseTable, "evalresponses-header-status", "summary.header.status");
        	        		UIMessage.make(evalResponseTable, "evalresponses-header-date", "summary.header.date");
        	        		UIMessage.make(evalResponseTable, "evalresponses-header-responses", "summary.header.responses");

        				}
        				//set display values for this eval
        				String evalState = evaluationService.updateEvaluationState(eval.getId());
           				//show one link per group assigned to in-queue, active or grace period eval
        				List<EvalGroup> groups = evalGroups.get(eval.getId());
        				for(EvalGroup group : groups) {
        					UIBranchContainer evalrow = UIBranchContainer.make(evalResponseTable,"evalResponsesList:");
 
        					String evalTitle = messageLocator.getMessage("summary.responses.eval.title", new String[]{ group.title, eval.getTitle() });
         					UIInternalLink.make(evalrow, "evalResponsesTitleLink_preview", 
        							EvalUtils.makeMaxLengthString(evalTitle, 70),
        							new EvalViewParameters(PreviewEvalProducer.VIEW_ID, eval.getId(), group.evalGroupId));//view params
        					//UIOutput.make(evalrow, "evalResponsesStatus", eval.getState());
        					
        					makeDateComponent(evalrow, eval, evalState, "evalResponsesDateLabel", "evalResponsesDate", "evalResponsesStatus");
        					
        					int responsesCount = deliveryService.countResponses(eval.getId(), group.evalGroupId, true);
        					int enrollmentsCount = evaluationService.countParticipantsForEval(eval.getId(), new String[]{group.evalGroupId});
        					UIMessage.make(evalrow, "evalResponsesDisplay", "summary.responses.counts", new Integer[]{responsesCount,enrollmentsCount});
        				}//link per group
        			}//partial, in-queue, active or grace period eval
        		}//for evals iterator
                if(!evalsToShow) {
                	UIMessage.make(evalResponsesForm, "summary-be-evaluated-none", "summary.be.evaluated.none");
                }
                
        	}//there are evals for instructor
        }

        /*
         * for the evaluations admin box
         */
	  Boolean showAdministratingBox = (Boolean) settings.get(EvalSettings.ENABLE_ADMINISTRATING_BOX);
	  if(showAdministratingBox != null && showAdministratingBox == true) {
	  
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

                String evalState = evaluationService.returnAndFixEvalState(eval, true);
                
 				makeDateComponent(evalrow, eval, evalState, "evalAdminDateLabel", "evalAdminDate", "evalAdminStatus");

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
	  } //showAdministratingBox true

        /*
         * Site/Group listing box
         */
        Boolean enableSitesBox = (Boolean) settings.get(EvalSettings.ENABLE_SUMMARY_SITES_BOX);
        if (enableSitesBox) {
            // only show this if we cannot find our location OR if the option is forced to on
            String NO_ITEMS = "no.list.items";

            UIBranchContainer contextsBC = UIBranchContainer.make(tofill, "siteListingBox:");
            UIMessage.make(contextsBC, "sitelisting-title", "summary.sitelisting.title");

            UIMessage.make(contextsBC, "sitelisting-evaluated-text", "summary.sitelisting.evaluated");
            // NOTE: OK usage of perms here
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
            // NOTE: OK usage of perms here
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
	 * @param evalrow
	 * @param eval
	 * @param evalState
	 * @param evalDateLabel
	 * @param evalDateItem
	 */
	protected void makeDateComponent(UIContainer evalrow,
			EvalEvaluation eval, String evalState, 
			String evalDateLabel, String evalDateItem, String evalStatusItem) {
        // use a date which is related to the current users locale
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		if (EvalConstants.EVALUATION_STATE_INQUEUE.equals(evalState)) {
		    // If we are in the queue we are yet to start,
		    // so say when we will
		    UIMessage.make(evalrow, evalDateLabel, "summary.label.starts");
		    UIOutput.make(evalrow, evalDateItem, df.format(eval.getStartDate()));

		    UIMessage.make(evalrow, evalStatusItem, "summary.status." + evalState);
		} else if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(evalState)) {
		    // Active evaluations can either be open forever or close at
		    // some point:
		    if (eval.getDueDate() != null) {
		        UIMessage.make(evalrow, evalDateLabel, "summary.label.due");
		        UIOutput.make(evalrow, evalDateItem, df.format(eval.getDueDate()));
		        // Should probably add something here if there's a grace period
		    } else {
		        UIMessage.make(evalrow, evalDateLabel, "summary.label.nevercloses");
		    }

		    UIMessage.make(evalrow, evalStatusItem, "summary.status." + evalState);
		} else if (EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(evalState)) {
		    // Evaluations can have a grace period, if so that must
		    // close at some point;
		    // Grace periods never remain open forever
		    UIMessage.make(evalrow, evalDateLabel, "summary.label.gracetill");
		    UIOutput.make(evalrow, evalDateItem, df.format(eval.getSafeStopDate()));

		    UIMessage.make(evalrow, evalStatusItem, "summary.status." + evalState);
		} else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(evalState)) {
		    // if an evaluation is closed then it is not yet viewable
		    // and ViewDate must have been set
		    UIMessage.make(evalrow, evalDateLabel, "summary.label.resultsviewableon");
		    UIOutput.make(evalrow, evalDateItem, df.format(eval.getSafeViewDate()));

		    UIMessage.make(evalrow, evalStatusItem, "summary.status." + evalState);
		} else if (EvalConstants.EVALUATION_STATE_VIEWABLE.equals(evalState)) {
		    // TODO if an evaluation is viewable we may want to notify
		    // if there are instructor/student dates
		    UIMessage.make(evalrow, evalDateLabel, "summary.label.resultsviewablesince");
		    UIOutput.make(evalrow, evalDateItem, df.format(eval.getSafeViewDate()));

		    int responsesCount = deliveryService.countResponses(eval.getId(), null, true);
		    int enrollmentsCount = evaluationService.countParticipantsForEval(eval.getId(), null);
		    int responsesNeeded = evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount);
		    if (responsesNeeded == 0) {
		        UIInternalLink.make(evalrow, "viewReportLink", UIMessage.make("viewreport.page.title"), new ReportParameters(
		                ReportChooseGroupsProducer.VIEW_ID, eval.getId()));
		    } else {
		        UIMessage.make(evalrow, evalStatusItem, "summary.status." + evalState).decorate(
		                new UITooltipDecorator(UIMessage.make("controlevaluations.eval.report.awaiting.responses", new Object[] { responsesNeeded })));
		    }
		} else {
		    UIMessage.make(evalrow, evalDateLabel, "summary.label.fallback");
		    UIOutput.make(evalrow, evalDateItem, df.format(eval.getStartDate()));
		}
	}

    @SuppressWarnings("unchecked")
    public List reportNavigationCases() {
        List i = new ArrayList();
        i.add(new NavigationCase(EvaluationSettingsProducer.VIEW_ID, new SimpleViewParameters(EvaluationSettingsProducer.VIEW_ID)));
        i.add(new NavigationCase(PreviewEvalProducer.VIEW_ID, new SimpleViewParameters(PreviewEvalProducer.VIEW_ID)));
        return i;
    }

}
