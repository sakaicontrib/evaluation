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
import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.viewparams.EvalTakeViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.PreviewEvalParameters;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * The summary producer rewrite
 * This creates a summary page for any user of the evaluation system and is the
 * starting page for anyone entering the system
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class SummaryProducer implements ViewComponentProducer, DefaultView, NavigationCaseReporter  {

	private final int maxGroupsToDisplay = 5;

	public static final String VIEW_ID = "summary";
	public String getViewID() {
		return VIEW_ID;
	}

	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private EvalEvaluationsLogic evaluationsLogic;
	public void setEvaluationsLogic(EvalEvaluationsLogic evaluationsLogic) {
		this.evaluationsLogic = evaluationsLogic;
	}

	private EvalResponsesLogic responsesLogic;
	public void setResponsesLogic(EvalResponsesLogic responsesLogic) {
		this.responsesLogic = responsesLogic;
	}

	private EvalTemplatesLogic templatesLogic;
	public void setTemplatesLogic(EvalTemplatesLogic templatesLogic) {
		this.templatesLogic = templatesLogic;
	}

	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
	}

	private Locale locale;
	public void setLocale(Locale locale) {
		this.locale = locale;
	}


	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		// local variables used in the render logic
		String currentUserId = externalLogic.getCurrentUserId();
		String currentGroup = externalLogic.getCurrentEvalGroup();
		boolean userAdmin = externalLogic.isUserAdmin(currentUserId);
		boolean createTemplate = templatesLogic.canCreateTemplate(currentUserId);
		boolean beginEvaluation = evaluationsLogic.canBeginEvaluation(currentUserId);
		// use a date which is related to the current users locale
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

		// page title
		UIMessage.make(tofill, "page-title", "summary.page.title");
		UIMessage.make(tofill, "page-instruction", "summary.page.instruction");

		/*
		 * top links here
		 */
		if (userAdmin) {
			UIInternalLink.make(tofill, "administrate-toplink",
					UIMessage.make("administrate.page.title"),
				new SimpleViewParameters(AdministrateProducer.VIEW_ID));
		}

		if (createTemplate) {
			UIInternalLink.make(tofill, "control-templates-link",
					UIMessage.make("controltemplates.page.title"),
				new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));
			UIInternalLink.make(tofill, "control-items-link",
					UIMessage.make("controlitems.page.title"),
				new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
		}

		if (beginEvaluation) {
			UIInternalLink.make(tofill, "control-evaluations-link",
					UIMessage.make("controlevaluations.page.title"),
				new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
		}

		/*
		 * Notification box listing box
		 */
		boolean userHasNotifications = false;
		if (userHasNotifications) {
			UIBranchContainer notificationsBC = UIBranchContainer.make(tofill, "notificationsBox:");
			UIMessage.make(notificationsBC, "notifications-title","summary.notifications.title");
			UIMessage.make(notificationsBC, "notifications-higher-level", "summary.eval.assigned.from.above");
			// add other stuff
		}

		/*
		 * for the evaluations taking box
		 */
		List<EvalEvaluation> evalsToTake = evaluationsLogic.getEvaluationsForUser(currentUserId, true, false);
		if (evalsToTake.size() > 0) {
			UIBranchContainer evalBC = UIBranchContainer.make(tofill, "evaluationsBox:");

			UIMessage.make(evalBC, "evaluations-title", "summary.evaluations.title");
			// build an array of evaluation ids
			Long[] evalIds = new Long[evalsToTake.size()];
			for (int i=0; i<evalsToTake.size(); i++) {
				evalIds[i] = ((EvalEvaluation) evalsToTake.get(i)).getId();
			}

			// now fetch all the information we care about for these evaluations at once (for speed)
			Map<Long, List<EvalGroup>> evalGroups = evaluationsLogic.getEvaluationGroups(evalIds, false);
			List<EvalResponse> evalResponses = responsesLogic.getEvaluationResponses(currentUserId, evalIds);

			for (Iterator<EvalEvaluation> itEvals = evalsToTake.iterator(); itEvals.hasNext();) {
				EvalEvaluation eval = (EvalEvaluation) itEvals.next();

				UIBranchContainer evalrow = UIBranchContainer.make(evalBC, "evaluationsList:", eval.getId().toString() );

				UIOutput.make(evalrow, "evaluationTitleTitle", eval.getTitle() );
				UIMessage.make(evalrow, "evaluationStartsTitle", "summary.evaluations.starts.title" );
            UIMessage.make(evalrow, "evaluationEndsTitle", "summary.evaluations.ends.title" );

            List<EvalGroup> groups = evalGroups.get(eval.getId());
				for (int j=0; j<groups.size(); j++) {
					EvalGroup group = (EvalGroup) groups.get(j);
					if (EvalConstants.GROUP_TYPE_INVALID.equals(group.type)) {
						continue; // skip processing for invalid groups
					}

					//check that the user can take evaluations in this evalGroupId
					if (externalLogic.isUserAllowedInEvalGroup(currentUserId, EvalConstants.PERM_TAKE_EVALUATION, group.evalGroupId)) {
						String groupId = group.evalGroupId;
						String title = group.title;
						String status = "unknown.caps";

						// find the object in the list matching the evalGroupId and evalId,
						// leave as null if not found -AZ
						EvalResponse response = null;
						for (int k=0; k<evalResponses.size(); k++) {
							EvalResponse er = (EvalResponse) evalResponses.get(k);
							if (groupId.equals(er.getEvalGroupId()) &&
									eval.getId().equals(er.getEvaluation().getId())) {
								response = er;
								break;
							}
						}

						if (groupId.equals(currentGroup)) {
							// TODO - do something when the evalGroupId matches
						}

						UIBranchContainer evalcourserow = UIBranchContainer.make(evalrow, "evaluationsCourseList:", groupId );

						// set status
						if (response != null && response.getEndTime() != null) {
						   // there is a response for this eval/group
							if (eval.getModifyResponsesAllowed().booleanValue()) {
							   // can modify responses so show the link still
								// take eval link when pending
								UIInternalLink.make(evalcourserow, "evaluationCourseLink", title,
										new EvalTakeViewParameters(TakeEvalProducer.VIEW_ID,
												eval.getId(), groupId, response.getId()) );
								status = "summary.status.completed";
							} else {
							   // show title only when completed
							   UIOutput.make(evalcourserow, "evaluationCourseTitle", title);
								status = "summary.status.completed";
							}
						} else {
						   // no response yet for this eval/group
							// take eval link when pending
							UIInternalLink.make(evalcourserow, "evaluationCourseLink", title,
									new EvalTakeViewParameters(TakeEvalProducer.VIEW_ID,
											eval.getId(), groupId) );
							status = "summary.status.pending";
						}
						UIMessage.make(evalcourserow, "evaluationCourseStatus", status );
						// moved down here as requested by UI design
		            UIOutput.make(evalcourserow, "evaluationStartDate", df.format(eval.getStartDate()) );
		            UIOutput.make(evalcourserow, "evaluationDueDate", df.format(eval.getDueDate()) );
					}
				}
			}
		}

		/**
		 * for the evaluations admin box
		 */

		/*
		 * In the list of parameters:
		 * first true => get recent only, and
		 * second true => get not-owned evals.
		 */
		List<EvalEvaluation> evals = evaluationsLogic.getVisibleEvaluationsForUser(currentUserId, true, true);

		/*
		 * If the person is an admin, then just point new evals to
		 * existing object.
		 * If the person is not an admin then only show owned evals +
		 * not-owned evals that are available for viewing results.
		 */
		List<EvalEvaluation> newEvals;
		if (userAdmin) {
			newEvals =  evals;
		} else {
			newEvals = new ArrayList<EvalEvaluation>();
			int numEvals = evals.size();
			Date currentDate = new Date();

			for (int count = 0; count < numEvals; count++) {
				EvalEvaluation evaluation = (EvalEvaluation) evals.get(count);

				// Add the owned evals
				if (currentUserId.equals(evaluation.getOwner())) {
					newEvals.add(evaluation);
				} else {
					// From the not-owned evals show those
					// that are available for viewing results.
					if (currentDate.before(evaluation.getViewDate())) {
						// Do nothing
					} else {
						newEvals.add(evaluation);
					}
				}
			}
		}

		if (! newEvals.isEmpty()) {
			UIBranchContainer evalAdminBC = UIBranchContainer.make(tofill, "evalAdminBox:");
			UIInternalLink.make(evalAdminBC, "evaladmin-title",
					UIMessage.make("summary.evaluations.admin"),
					new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID) );
			UIForm evalAdminForm = UIForm.make(evalAdminBC , "evalAdminForm");

			UIMessage.make(evalAdminForm, "evaladmin-header-title","summary.header.title");
			UIMessage.make(evalAdminForm, "evaladmin-header-status", "summary.header.status");
			UIMessage.make(evalAdminForm, "evaladmin-header-date", "summary.header.date");

			for (Iterator<EvalEvaluation> iter = newEvals.iterator(); iter.hasNext();) {
				EvalEvaluation eval = (EvalEvaluation) iter.next();

				UIBranchContainer evalrow = UIBranchContainer.make(evalAdminForm,
						"evalAdminList:", eval.getId().toString() );

				Date date;

				String evalStatus = evaluationsLogic.updateEvaluationState(eval.getId());
            if (EvalConstants.EVALUATION_STATE_INQUEUE.equals(evalStatus)) {
               date = eval.getStartDate();
               UIMessage.make(evalrow, "evalAdminStatus", "summary.status." + evalStatus);
            } else if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(evalStatus)) {
               date = eval.getStopDate();
               UIMessage.make(evalrow, "evalAdminStatus", "summary.status." + evalStatus);
            } else if (EvalConstants.EVALUATION_STATE_DUE.equals(evalStatus)) {
               date = eval.getDueDate();
               UIMessage.make(evalrow, "evalAdminStatus", "summary.status." + evalStatus);
            } else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(evalStatus)) {
               date = eval.getViewDate();
               UIMessage.make(evalrow, "evalAdminStatus", "summary.status." + evalStatus);
            } else if (EvalConstants.EVALUATION_STATE_VIEWABLE.equals(evalStatus)) {
               date = eval.getViewDate();
               int ctResponses = responsesLogic.countResponses(eval.getId(), null);
               int ctEnrollments = getTotalEnrollmentsForEval(eval.getId());
               Integer respReqToViewResults = (Integer) settings
                     .get(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS);
               if ((respReqToViewResults.intValue() <= ctResponses) || (ctResponses >= ctEnrollments)) {
                  UIInternalLink.make(evalrow, "viewReportLink", UIMessage.make("viewreport.page.title"),
                        new ReportParameters(ReportChooseGroupsProducer.VIEW_ID, eval.getId()));
               } else {
                  UIMessage.make(evalrow, "evalAdminStatus", "summary.status." + evalStatus);
               }
            } else {
               date = eval.getStartDate();
            }


				/**
				 *
				 * 1) if a evaluation is queued, title link go to EditSettings page with populated data
				 * 2) if a evaluation is active, title link go to EditSettings page with populated data
				 * but start date should be disabled
				 * 3) if a evaluation is closed, title link go to previewEval page with populated data
				 */
				if (EvalConstants.EVALUATION_STATE_CLOSED.equals(evalStatus)
                  || EvalConstants.EVALUATION_STATE_VIEWABLE.equals(evalStatus)) {
               UIInternalLink.make(evalrow, "evalAdminTitleLink_preview", eval.getTitle(),
                     new PreviewEvalParameters(PreviewEvalProducer.VIEW_ID, eval.getId(), 
                           eval.getTemplate().getId()));
            } else {
               UICommand evalEditUIC = UICommand.make(evalrow, "evalAdminTitleLink_edit", 
                     eval.getTitle(), "#{evaluationBean.editEvalSettingAction}");
               evalEditUIC.parameters.add(new UIELBinding("#{evaluationBean.eval.id}", eval.getId()));
            }

            UIMessage.make(evalrow, "evalAdminDateLabel", "summary.label." + evalStatus);
            UIOutput.make(evalrow, "evalAdminDate", df.format(date));
         }
		}


		/*
		 * Site/Group listing box
		 */
		// TODO - only render this when not inside a worksite -AZ
//		if (currentContext == null) {
			String NO_ITEMS = "no.list.items";

			UIBranchContainer contextsBC = UIBranchContainer.make(tofill, "siteListingBox:");
			UIMessage.make(contextsBC, "sitelisting-title", "summary.sitelisting.title");

			UIMessage.make(contextsBC, "sitelisting-evaluated-text", "summary.sitelisting.evaluated");
			List<EvalGroup> evaluatedContexts = externalLogic.getEvalGroupsForUser(currentUserId, EvalConstants.PERM_BE_EVALUATED);
			if (evaluatedContexts.size() > 0) {
				for (int i=0; i<evaluatedContexts.size(); i++) {
					if (i > maxGroupsToDisplay) {
						UIMessage.make(contextsBC, "evaluatedListNone", "summary.sitelisting.maxshown",
								new Object[] { new Integer(evaluatedContexts.size() - maxGroupsToDisplay) });
						break;
					}
					UIBranchContainer evaluatedBC = UIBranchContainer.make(contextsBC, "evaluatedList:", i+"");
					EvalGroup c = (EvalGroup) evaluatedContexts.get(i);
					UIOutput.make(evaluatedBC, "evaluatedListTitle", c.title);
				}
			} else {
				UIMessage.make(contextsBC, "evaluatedListNone", NO_ITEMS );
			}

			UIMessage.make(contextsBC, "sitelisting-evaluate-text", "summary.sitelisting.evaluate");
			List<EvalGroup> evaluateContexts = externalLogic.getEvalGroupsForUser(currentUserId, EvalConstants.PERM_TAKE_EVALUATION);
			if (evaluateContexts.size() > 0) {
				for (int i=0; i<evaluateContexts.size(); i++) {
					if (i > maxGroupsToDisplay) {
						UIMessage.make(contextsBC, "evaluateListNone", "summary.sitelisting.maxshown",
								new Object[] { new Integer(evaluateContexts.size() - maxGroupsToDisplay) });
						break;
					}
					UIBranchContainer evaluateBC = UIBranchContainer.make(contextsBC, "evaluateList:", i+"");
					EvalGroup c = (EvalGroup) evaluateContexts.get(i);
					UIOutput.make(evaluateBC, "evaluateListTitle", c.title);
				}
			} else {
				UIMessage.make(contextsBC, "evaluateListNone", NO_ITEMS );
			}
//		}

		/*
		 * For the Evaluation tools box
		 */
		if (createTemplate || beginEvaluation) {
			UIBranchContainer toolsBC = UIBranchContainer.make(tofill, "toolsBox:");
			UIMessage.make(toolsBC, "tools-title", "summary.tools.title");

			if ( createTemplate ) {
				UIInternalLink.make(toolsBC, "createTemplateLink",
					UIMessage.make("createtemplate.page.title"),
					new TemplateViewParameters(ModifyTemplateProducer.VIEW_ID, null));
			}

			if ( beginEvaluation ) {
				UIInternalLink.make(toolsBC, "beginEvaluationLink",
					UIMessage.make("starteval.page.title"),
					new TemplateViewParameters(EvaluationStartProducer.VIEW_ID, null));
			}
		}

	}


	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	@SuppressWarnings("unchecked")
   public List reportNavigationCases() {
		List i = new ArrayList();
		i.add(new NavigationCase(EvaluationSettingsProducer.VIEW_ID, new SimpleViewParameters(
				EvaluationSettingsProducer.VIEW_ID)));
		i.add(new NavigationCase(PreviewEvalProducer.VIEW_ID, new SimpleViewParameters(
				PreviewEvalProducer.VIEW_ID)));
		return i;
	}

	/**
	 * Gets the total count of enrollments for an evaluation
	 *
	 * @param evaluationId
	 * @return total number of users with take eval perms in this evaluation
	 */
	private int getTotalEnrollmentsForEval(Long evaluationId) {
		int totalEnrollments = 0;

		Map<Long, List<EvalAssignGroup>> evalAssignGroups = evaluationsLogic.getEvaluationAssignGroups(new Long[] {evaluationId}, true);
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
