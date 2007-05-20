/******************************************************************************
 * ControlEvaluationsProducer.java - created by aaronz@vt.edu on Mar 19, 2007
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
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalAssignsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.params.PreviewEvalParameters;
import org.sakaiproject.evaluation.tool.params.TemplateViewParameters;

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
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * This lists evaluations for users so they can add, modify, remove them
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ControlEvaluationsProducer implements ViewComponentProducer, NavigationCaseReporter {

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ViewComponentProducer#getViewID()
	 */
	public static String VIEW_ID = "control_evaluations";
	public String getViewID() {
		return VIEW_ID;
	}


	private Locale locale;
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}
	
	private EvalAssignsLogic assignsLogic;
	public void setAssignsLogic(EvalAssignsLogic assignsLogic) {
		this.assignsLogic = assignsLogic;
	}

	private EvalEvaluationsLogic evaluationsLogic;
	public void setEvaluationsLogic(EvalEvaluationsLogic evaluationsLogic) {
		this.evaluationsLogic = evaluationsLogic;
	}

	private EvalTemplatesLogic templatesLogic;
	public void setTemplatesLogic(EvalTemplatesLogic templatesLogic) {
		this.templatesLogic = templatesLogic;
	}

	private EvalResponsesLogic responsesLogic;
	public void setResponsesLogic(EvalResponsesLogic responsesLogic) {
		this.responsesLogic = responsesLogic;
	}

	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
	}


	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		// local variables used in the render logic
		String currentUserId = external.getCurrentUserId();
		boolean userAdmin = external.isUserAdmin(currentUserId);
		boolean createTemplate = templatesLogic.canCreateTemplate(currentUserId);
		boolean beginEvaluation = evaluationsLogic.canBeginEvaluation(currentUserId);
		// use a date which is related to the current users locale
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

		// page title
		UIMessage.make(tofill, "page-title", "controlevaluations.page.title");

		/*
		 * top links here
		 */
		UIInternalLink.make(tofill, "summary-link", 
					UIMessage.make("summary.page.title"), 
				new SimpleViewParameters(SummaryProducer.VIEW_ID));

		if (userAdmin) {
			UIInternalLink.make(tofill, "administrate-link", 
					UIMessage.make("administrate.page.title"),
				new SimpleViewParameters(AdministrateProducer.VIEW_ID));
		}

		if (createTemplate) {
			UIInternalLink.make(tofill, "control-templates-link", //$NON-NLS-1$
					UIMessage.make("controltemplates.page.title"),  //$NON-NLS-1$
				new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));
			UIInternalLink.make(tofill, "control-items-link", //$NON-NLS-1$
					UIMessage.make("controlitems.page.title"),  //$NON-NLS-1$
				new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
		}

		if (!beginEvaluation) {
			throw new SecurityException("User attempted to access " + 
					ControlEvaluationsProducer.VIEW_ID + " when they are not allowed");
		}

		// get all the visible evaluations for the current user
		List inqueueEvals = new ArrayList();
		List activeEvals = new ArrayList();
		List closedEvals = new ArrayList();

		List evals = evaluationsLogic.getVisibleEvaluationsForUser(external.getCurrentUserId(), true, false);
		for (int j = 0; j < evals.size(); j++) {
			// get queued, active, closed evaluations by date
			// check the state of the eval to determine display data
			EvalEvaluation eval = (EvalEvaluation) evals.get(j);
			String evalStatus = evaluationsLogic.getEvaluationState(eval.getId());

			if ( EvalConstants.EVALUATION_STATE_INQUEUE.equals(evalStatus) ) {
				inqueueEvals.add(eval);
			} else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(evalStatus) ||
					EvalConstants.EVALUATION_STATE_VIEWABLE.equals(evalStatus) ) {
				closedEvals.add(eval);
			} else if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(evalStatus) ||
					EvalConstants.EVALUATION_STATE_DUE.equals(evalStatus) ) {
				activeEvals.add(eval);
			}
		}

		// create inqueue evaluations header and link
		UIMessage.make(tofill, "evals-inqueue-header", "controlevaluations.inqueue.header");
		UIMessage.make(tofill, "evals-inqueue-description", "controlevaluations.inqueue.description");
		UIForm startEvalForm = UIForm.make(tofill, "begin-evaluation-form");
		UICommand.make(startEvalForm, "begin-evaluation-link", UIMessage.make("starteval.page.title"), "#{evaluationBean.startEvaluation}");

		if (inqueueEvals.size() > 0) {
			UIBranchContainer evalListing = UIBranchContainer.make(tofill, "inqueue-eval-listing:");
			UIForm evalForm = UIForm.make(evalListing, "inqueue-eval-form");

			UIMessage.make(evalForm, "eval-title-header", "controlevaluations.eval.title.header");
			UIMessage.make(evalForm, "eval-assigned-header", "controlevaluations.eval.assigned.header");
			UIMessage.make(evalForm, "eval-startdate-header", "controlevaluations.eval.startdate.header");
			UIMessage.make(evalForm, "eval-duedate-header", "controlevaluations.eval.duedate.header");
			UIMessage.make(evalForm, "eval-settings-header", "controlevaluations.eval.settings.header");

			for (int i = 0; i < inqueueEvals.size(); i++) {
				EvalEvaluation evaluation = (EvalEvaluation) inqueueEvals.get(i);

				UIBranchContainer evaluationRow = UIBranchContainer.make(evalForm, "inqueue-eval-row:", evaluation.getId().toString());

				UIInternalLink.make(evaluationRow, "inqueue-eval-link", 
						evaluation.getTitle(), 
						new PreviewEvalParameters( PreviewEvalProducer.VIEW_ID, evaluation.getId(), evaluation.getTemplate().getId(), null, ControlEvaluationsProducer.VIEW_ID));

				// vary the display depending on the number of groups assigned
				int groupsCount = evaluationsLogic.countEvaluationGroups(evaluation.getId());
				if (groupsCount == 1) {
					UICommand evalAssigned = UICommand.make(evaluationRow, 
							"inqueue-eval-assigned-link", 
							getTitleForFirstEvalGroup(evaluation.getId()),
							"#{evaluationBean.evalAssigned}");
					evalAssigned.parameters.add(new UIELBinding("#{evaluationBean.evalId}", evaluation.getId()));
				} else {
					UICommand evalAssigned = UICommand.make(evaluationRow, 
							"inqueue-eval-assigned-link", 
							UIMessage.make("controlevaluations.eval.groups.link", new Object[] { new Integer(groupsCount) }),
							"#{evaluationBean.evalAssigned}");
					evalAssigned.parameters.add(new UIELBinding("#{evaluationBean.evalId}", evaluation.getId()));
				}

				UIOutput.make(evaluationRow, "inqueue-eval-startdate", df.format(evaluation.getStartDate()));
				UIOutput.make(evaluationRow, "inqueue-eval-duedate", df.format(evaluation.getDueDate()));

				UICommand evalEdit = UICommand.make(evaluationRow, 
						"inqueue-eval-edit-link", 
						UIMessage.make("controlevaluations.eval.edit.link"),
						"#{evaluationBean.editEvalSettingAction}");
				evalEdit.parameters.add(new UIELBinding("#{evaluationBean.eval.id}", evaluation.getId()));

				if ( evaluationsLogic.canRemoveEvaluation(currentUserId, evaluation.getId()) ) {
					// evaluation removable
					UIInternalLink.make(evaluationRow, "inqueue-eval-delete-link", 
							UIMessage.make("controlevaluations.eval.delete.link"), 
							new TemplateViewParameters( RemoveEvalProducer.VIEW_ID, evaluation.getId() ) );
				}

			}
		} else {
			UIMessage.make(tofill, "no-inqueue-evals", "controlevaluations.inqueue.none");
		}


		// create active evaluations header and link
		UIMessage.make(tofill, "evals-active-header", "controlevaluations.active.header");
		UIMessage.make(tofill, "evals-active-description", "controlevaluations.active.description");

		if (activeEvals.size() > 0) {
			UIBranchContainer evalListing = UIBranchContainer.make(tofill, "active-eval-listing:");
			UIForm evalForm = UIForm.make(evalListing, "active-eval-form");

			UIMessage.make(evalForm, "eval-title-header", "controlevaluations.eval.title.header");
			UIMessage.make(evalForm, "eval-assigned-header", "controlevaluations.eval.assigned.header");
			UIMessage.make(evalForm, "eval-users-header", "controlevaluations.eval.users.header");
			UIMessage.make(evalForm, "eval-startdate-header", "controlevaluations.eval.startdate.header");
			UIMessage.make(evalForm, "eval-duedate-header", "controlevaluations.eval.duedate.header");
			UIMessage.make(evalForm, "eval-settings-header", "controlevaluations.eval.settings.header");

			for (int i = 0; i < activeEvals.size(); i++) {
				EvalEvaluation evaluation = (EvalEvaluation) activeEvals.get(i);

				UIBranchContainer evaluationRow = UIBranchContainer.make(evalForm, "active-eval-row:", evaluation.getId().toString());

				UIInternalLink.make(evaluationRow, "active-eval-link", 
						evaluation.getTitle(), 
						new PreviewEvalParameters( PreviewEvalProducer.VIEW_ID, evaluation.getId(), evaluation.getTemplate().getId(), null, ControlEvaluationsProducer.VIEW_ID));

				// vary the display depending on the number of groups assigned
				int groupsCount = evaluationsLogic.countEvaluationGroups(evaluation.getId());
				if (groupsCount == 1) {
					UICommand evalAssigned = UICommand.make(evaluationRow, 
							"active-eval-assigned-link", 
							getTitleForFirstEvalGroup(evaluation.getId()),
							"#{evaluationBean.evalAssigned}");
					evalAssigned.parameters.add(new UIELBinding("#{evaluationBean.evalId}", evaluation.getId()));
				} else {
					UICommand evalAssigned = UICommand.make(evaluationRow, 
							"active-eval-assigned-link", 
							UIMessage.make("controlevaluations.eval.groups.link", new Object[] { new Integer(groupsCount) }),
							"#{evaluationBean.evalAssigned}");
					evalAssigned.parameters.add(new UIELBinding("#{evaluationBean.evalId}", evaluation.getId()));
				}

				// calculate the response rate
				int ctResponses = responsesLogic.countResponses(evaluation.getId(), null);
				int ctEnrollments = getTotalEnrollmentsForEval(evaluation.getId());
				UIOutput.make(evaluationRow, "active-eval-response-rate", ctResponses + "/" + ctEnrollments );

				UIOutput.make(evaluationRow, "active-eval-startdate", df.format(evaluation.getStartDate()));
				UIOutput.make(evaluationRow, "active-eval-duedate", df.format(evaluation.getDueDate()));

				UICommand evalEdit = UICommand.make(evaluationRow, 
						"active-eval-edit-link", 
						UIMessage.make("controlevaluations.eval.edit.link"),
						"#{evaluationBean.editEvalSettingAction}");
				evalEdit.parameters.add(new UIELBinding("#{evaluationBean.eval.id}", evaluation.getId()));

				if ( evaluationsLogic.canRemoveEvaluation(currentUserId, evaluation.getId()) ) {
					// evaluation removable
					UIInternalLink.make(evaluationRow, "active-eval-delete-link", 
							UIMessage.make("controlevaluations.eval.delete.link"), 
							new TemplateViewParameters( RemoveEvalProducer.VIEW_ID, evaluation.getId() ) );
				}

			}
		} else {
			UIMessage.make(tofill, "no-active-evals", "controlevaluations.active.none");
		}

		// create closed evaluations header and link
		UIMessage.make(tofill, "evals-closed-header", "controlevaluations.closed.header");
		UIMessage.make(tofill, "evals-closed-description", "controlevaluations.closed.description");

		if (closedEvals.size() > 0) {
			UIBranchContainer evalListing = UIBranchContainer.make(tofill, "closed-eval-listing:");
			UIForm evalForm = UIForm.make(evalListing, "closed-eval-form");

			UIMessage.make(evalForm, "eval-title-header", "controlevaluations.eval.title.header");
			UIMessage.make(evalForm, "eval-assigned-header", "controlevaluations.eval.assigned.header");
			UIMessage.make(evalForm, "eval-response-rate-header", "controlevaluations.eval.responserate.header");
			UIMessage.make(evalForm, "eval-startdate-header", "controlevaluations.eval.startdate.header");
			UIMessage.make(evalForm, "eval-report-header", "controlevaluations.eval.report.header");

			for (int i = 0; i < closedEvals.size(); i++) {
				EvalEvaluation evaluation = (EvalEvaluation) closedEvals.get(i);

				UIBranchContainer evaluationRow = UIBranchContainer.make(evalForm, "closed-eval-row:", evaluation.getId().toString());

				UIInternalLink.make(evaluationRow, "closed-eval-link", 
						evaluation.getTitle(), 
						new PreviewEvalParameters( PreviewEvalProducer.VIEW_ID, evaluation.getId(), evaluation.getTemplate().getId(), null, ControlEvaluationsProducer.VIEW_ID));

				// vary the display depending on the number of groups assigned
				int groupsCount = evaluationsLogic.countEvaluationGroups(evaluation.getId());
				if (groupsCount == 1) {
					UICommand evalAssigned = UICommand.make(evaluationRow, 
							"closed-eval-assigned-link", 
							getTitleForFirstEvalGroup(evaluation.getId()),
							"#{evaluationBean.evalAssigned}");
					evalAssigned.parameters.add(new UIELBinding("#{evaluationBean.evalId}", evaluation.getId()));
				} else {
					UICommand evalAssigned = UICommand.make(evaluationRow, 
							"closed-eval-assigned-link", 
							UIMessage.make("controlevaluations.eval.groups.link", new Object[] { new Integer(groupsCount) }),
							"#{evaluationBean.evalAssigned}");
					evalAssigned.parameters.add(new UIELBinding("#{evaluationBean.evalId}", evaluation.getId()));
				}

				// calculate the response rate
				int ctResponses = responsesLogic.countResponses(evaluation.getId(), null);
				int ctEnrollments = getTotalEnrollmentsForEval(evaluation.getId());
				double per = (1.0 * ctResponses )/ctEnrollments;
				long percentage = Math.round(per*100);				
				UIOutput.make(evaluationRow, "closed-eval-response-rate", ctResponses + "/"+ ctEnrollments +" - "+percentage +"%");

				UIOutput.make(evaluationRow, "closed-eval-duedate", df.format(evaluation.getDueDate()));

				UICommand evalEdit = UICommand.make(evaluationRow, 
						"closed-eval-edit-link", 
						UIMessage.make("controlevaluations.eval.edit.link"),
						"#{evaluationBean.editEvalSettingAction}");
				evalEdit.parameters.add(new UIELBinding("#{evaluationBean.eval.id}", evaluation.getId()));

				if (EvalConstants.EVALUATION_STATE_VIEWABLE.equals(evaluationsLogic.getEvaluationState(evaluation.getId())) ) {
					int respReqToViewResults = ((Integer) settings.get(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS)).intValue();
					if (respReqToViewResults <= ctResponses || ctResponses >= ctEnrollments) {
						UIInternalLink.make(evaluationRow, "closed-eval-report-link", 
								UIMessage.make("controlevaluations.eval.report.link"),
								new TemplateViewParameters(ChooseReportGroupsProducer.VIEW_ID, evaluation.getId() ));	
					} else {
						UIMessage.make(evaluationRow, "closed-eval-message", 
								"controlevaluations.eval.report.awaiting.responses");
					}
				} else {
					UIMessage.make(evaluationRow, "closed-eval-message", 
							"controlevaluations.eval.report.viewable.on",
							new String[] { df.format(evaluation.getViewDate()) });
				}

				if ( evaluationsLogic.canRemoveEvaluation(currentUserId, evaluation.getId()) ) {
					// evaluation removable
					UIInternalLink.make(evaluationRow, "closed-eval-delete-link", 
							UIMessage.make("controlevaluations.eval.delete.link"), 
							new TemplateViewParameters( RemoveEvalProducer.VIEW_ID, evaluation.getId() ) );
				}

			}
		} else {
			UIMessage.make(tofill, "no-closed-evals", "controlevaluations.closed.none");
		}

	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List i = new ArrayList();
		
		i.add(new NavigationCase(EvaluationSettingsProducer.VIEW_ID, new SimpleViewParameters(
				EvaluationSettingsProducer.VIEW_ID)));
		i.add(new NavigationCase(EvaluationStartProducer.VIEW_ID, new SimpleViewParameters(
				EvaluationStartProducer.VIEW_ID)));
		i.add(new NavigationCase(EvaluationAssignConfirmProducer.VIEW_ID, new SimpleViewParameters(
				EvaluationAssignConfirmProducer.VIEW_ID)));
		
		return i;
	}	
	
	/**
	 * Gets the title for the first returned context for this evaluation,
	 * should only be used when there is only one context assigned to an eval
	 * 
	 * @param evaluationId
	 * @return title of first context returned
	 */
	private String getTitleForFirstEvalGroup(Long evaluationId) {
		List acs = assignsLogic.getAssignGroupsByEvalId(evaluationId);
		EvalAssignGroup eac = (EvalAssignGroup) acs.get(0);
		return external.getDisplayTitle( eac.getEvalGroupId() );
	}

	/**
	 * Gets the total count of enrollments for an evaluation
	 * 
	 * @param evaluationId
	 * @return total number of users with take eval perms in this evaluation
	 */
	private int getTotalEnrollmentsForEval(Long evaluationId) {
		int totalEnrollments = 0;

		List l = assignsLogic.getAssignGroupsByEvalId(evaluationId);
		for (int i=0; i<l.size(); i++) {
			EvalAssignGroup eac = (EvalAssignGroup) l.get(i);
			String context = eac.getEvalGroupId();
			Set userIds = external.getUserIdsForEvalGroup(context, EvalConstants.PERM_TAKE_EVALUATION);
			totalEnrollments = totalEnrollments + userIds.size();
		}
		return totalEnrollments;
	}
}
