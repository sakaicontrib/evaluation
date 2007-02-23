/******************************************************************************
 * ControlProducer.java - rewrite by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu)
 * Rui Feng (fengr@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalAssignsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;
import org.sakaiproject.evaluation.tool.params.PreviewEvalParameters;

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
 * Rewritten to eliminate need for backing bean and refactored
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Rui Feng (fengr@vt.edu)
 */

public class ControlPanelProducer implements ViewComponentProducer, NavigationCaseReporter {

	public static final String VIEW_ID = "control_panel"; //$NON-NLS-1$
	public String getViewID() {
		return VIEW_ID;
	}

	private EvalAssignsLogic assignsLogic;
	public void setAssignsLogic(EvalAssignsLogic assignsLogic) {
		this.assignsLogic = assignsLogic;
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

	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	private Locale locale;
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		// local variables used in the render logic
		String currentUserId = external.getCurrentUserId();
		boolean userAdmin = external.isUserAdmin(currentUserId);
		boolean createTemplate = templatesLogic.canCreateTemplate(currentUserId);
		boolean beginEvaluation = evaluationsLogic.canBeginEvaluation(currentUserId);
		// use a date which is related to the current users locale
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

		UIMessage.make(tofill, "control-panel-title", "controlpanel.page.title"); //$NON-NLS-1$ //$NON-NLS-2$

		/*
		 * top links here
		 */
		if (userAdmin) {
			UIInternalLink.make(tofill, "administrate-toplink", UIMessage.make("administrate.page.title"), new SimpleViewParameters(AdministrateProducer.VIEW_ID)); //$NON-NLS-1$ //$NON-NLS-2$
		}

		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"), new SimpleViewParameters(SummaryProducer.VIEW_ID)); //$NON-NLS-1$ //$NON-NLS-2$

		// Create new template link
		if (createTemplate) {
			UIForm createTemplateForm = UIForm.make(tofill, "create-template-form"); //$NON-NLS-1$
			UIInternalLink.make(createTemplateForm, "createTemplateLink", //$NON-NLS-1$
					UIMessage.make("createtemplate.page.title"), //$NON-NLS-1$
					new EvalViewParameters(ModifyTemplateProducer.VIEW_ID, null));
			UIMessage.make(createTemplateForm, "eval-templates-header", "controlpanel.eval.templates.header"); //$NON-NLS-1$ //$NON-NLS-2$

			UIMessage.make(createTemplateForm, "template-desc-note","controlpanel.template.desc.note"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// get template List
		List templateList = templatesLogic.getTemplatesForUser(currentUserId, null, true);
		if (templateList != null && templateList.size() > 0) {

			UIBranchContainer templates = UIBranchContainer.make(tofill, "templateTable:"); //$NON-NLS-1$

			UIForm templateForm = UIForm.make(templates, "template-form"); //$NON-NLS-1$

			UIMessage.make(templateForm, "template-title-header", "controlpanel.template.title.header");	 //$NON-NLS-1$ //$NON-NLS-2$
			UIMessage.make(templateForm, "owner-header", "controlpanel.owner.header"); //$NON-NLS-1$ //$NON-NLS-2$
			UIMessage.make(templateForm, "last-update-header", "controlpanel.last.update.header"); //$NON-NLS-1$ //$NON-NLS-2$
			
			for (int i = 0; i < templateList.size(); i++) {
				UIBranchContainer templatesRb = UIBranchContainer.make(templateForm, "templateList:", Integer //$NON-NLS-1$
						.toString(i));

				EvalTemplate template1 = (EvalTemplate) (templateList.get(i));
                if(template1.getLocked().equals(new Boolean(false))){
				UIInternalLink.make(templatesRb, "template-modify", template1.getTitle(), 
                    new EvalViewParameters(ModifyTemplateItemsProducer.VIEW_ID, template1.getId()));
                }else{
                	UIOutput.make(templatesRb, "template-modify-label", template1.getTitle());
                }
				String ownerId = template1.getOwner();
				UIOutput.make(templatesRb, "templateOwner", external.getUserDisplayName(ownerId)); //$NON-NLS-1$
				
				Date date1 = template1.getLastModified();
				UIOutput.make(templatesRb, "lastUpdate", df.format(date1)); //$NON-NLS-1$
				
				UIInternalLink.make(templatesRb, "deleteTemplateLink", new EvalViewParameters( //$NON-NLS-1$
						RemoveTemplateProducer.VIEW_ID,template1.getId()));

			}// end of for loop
		}


		// Begin eval link
		if (beginEvaluation) {
			UIForm startEvalForm = UIForm.make(tofill, "start-evaluation-form"); //$NON-NLS-1$
			UICommand.make(startEvalForm, "startEvalLink", UIMessage.make("starteval.page.title"), //$NON-NLS-1$ //$NON-NLS-2$
									"#{evaluationBean.startEvaluation}"); //$NON-NLS-1$
			UIMessage.make(startEvalForm, "queued-eval-header", "controlpanel.queued.eval.header"); //$NON-NLS-1$ //$NON-NLS-2$
			UIMessage.make(startEvalForm, "evals-not-started-header", "controlpanel.evals.not.started.header"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// get all the visible evaluation to current user
		List evals = evaluationsLogic.getVisibleEvaluationsForUser(external.getCurrentUserId(), true);
		if (evals != null && evals.size() > 0) {
			// get queued, active, closed evaluations by date
			List queuedEvals = new ArrayList();
			List activeEvals = new ArrayList();
			List closedEvals = new ArrayList();

			// check the state of the eval to determine display data
			for (int j = 0; j < evals.size(); j++) {
				EvalEvaluation myEval = (EvalEvaluation) evals.get(j);
 				String evalStatus=evaluationsLogic.getEvaluationState(myEval.getId());
				if (evalStatus==EvalConstants.EVALUATION_STATE_INQUEUE) {
					queuedEvals.add(myEval);
				} else if (evalStatus==EvalConstants.EVALUATION_STATE_CLOSED || evalStatus==EvalConstants.EVALUATION_STATE_VIEWABLE) {
					closedEvals.add(myEval);
				} else {
					activeEvals.add(myEval);
				}
			}

			// display Queued Evaluation Table
			if (queuedEvals != null && queuedEvals.size() > 0) {
				
				UIBranchContainer queuedEvalTable = UIBranchContainer.make(tofill, "queuedEvalTable:"); //$NON-NLS-1$
				UIForm queuedEvalForm = UIForm.make(queuedEvalTable, "queuedEvalForm"); //$NON-NLS-1$

				UIMessage.make(queuedEvalForm, "eval-title-header", "controlpanel.eval.title.header"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(queuedEvalForm, "eval-assigned-header","controlpanel.eval.assigned.header"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(queuedEvalForm, "eval-start-date-header", "controlpanel.eval.start.date.header"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(queuedEvalForm, "eval-due-date-header", "controlpanel.eval.due.date.header"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(queuedEvalForm, "eval-settings-header", "controlpanel.eval.settings.header"); //$NON-NLS-1$ //$NON-NLS-2$
				
				
				for (int i = 0; i < queuedEvals.size(); i++) {
					UIBranchContainer queuedEvalsRb = UIBranchContainer.make(queuedEvalForm, "queuedEvalList:", //$NON-NLS-1$
							Integer.toString(i));

					EvalEvaluation eval1 = (EvalEvaluation) (queuedEvals.get(i));
		
					UIInternalLink.make(queuedEvalsRb, "queuedEvalTitle", eval1.getTitle(), new PreviewEvalParameters( //$NON-NLS-1$
							PreviewEvalProducer.VIEW_ID, eval1.getId(),eval1.getTemplate().getId(), null, ControlPanelProducer.VIEW_ID));
					
					// vary the display depending on the number of contexts assigned
					int contextCount = evaluationsLogic.countEvaluationContexts(eval1.getId());
					if (contextCount > 1) {
				
						UIOutput.make(queuedEvalsRb, "queuedEvalAssignedLabel", contextCount+" courses");
					} else {
					
						UIOutput.make(queuedEvalsRb, "queuedEvalAssignedLabel", getTitleForFirstEvalContext(eval1.getId()));

					}

					UIOutput.make(queuedEvalsRb, "queuedEvalStartDate", df.format(eval1.getStartDate())); //$NON-NLS-1$
					UIOutput.make(queuedEvalsRb, "queuedEvalDueDate", df.format(eval1.getDueDate())); //$NON-NLS-1$
					
					UICommand queuedEvalEdit = UICommand.make(queuedEvalsRb, "queuedEvalEditLink", UIMessage.make("controlpanel.eval.edit.link"), //$NON-NLS-1$ //$NON-NLS-2$
												"#{evaluationBean.editEvalSettingAction}"); //$NON-NLS-1$
					queuedEvalEdit.parameters.add(new UIELBinding("#{evaluationBean.eval.id}", eval1.getId())); //$NON-NLS-1$
					
					//Use EvalViewParameter to pass Evaluatio ID
					UIInternalLink.make(queuedEvalsRb, "deleteEvalLink", new EvalViewParameters( //$NON-NLS-1$
							RemoveEvalProducer.VIEW_ID, eval1.getId()));

				} // end of for loop
			}

			UIMessage.make(tofill, "active-eval-header-title", "controlpanel.active.eval.header.title"); //$NON-NLS-1$ //$NON-NLS-2$
			UIMessage.make(tofill, "active-eval-header-desc", "controlpanel.active.eval.header.desc"); //$NON-NLS-1$ //$NON-NLS-2$
			
			// display Active Evaluation Table
			if (activeEvals != null && activeEvals.size() > 0) {
				
				UIBranchContainer activeEvalTable = UIBranchContainer.make(tofill, "activeEvalTable:"); //$NON-NLS-1$
				UIForm activeEvalForm = UIForm.make(activeEvalTable, "activeEvalForm"); //$NON-NLS-1$
				
				UIMessage.make(activeEvalForm, "eval-title-header", "controlpanel.eval.title.header"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(activeEvalForm, "eval-assigned-header", "controlpanel.eval.assigned.header"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(activeEvalForm, "eval-start-date-header", "controlpanel.eval.start.date.header"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(activeEvalForm, "eval-due-date-header", "controlpanel.eval.due.date.header"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(activeEvalForm, "eval-users-header", "controlpanel.eval.users.header");				 //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(activeEvalForm, "eval-settings-header","controlpanel.eval.settings.header"); //$NON-NLS-1$ //$NON-NLS-2$
				
				for (int i = 0; i < activeEvals.size(); i++) {
					UIBranchContainer activeEvalsRb = UIBranchContainer.make(activeEvalForm, "activeEvalList:", //$NON-NLS-1$
							Integer.toString(i));

					EvalEvaluation eval1 = (EvalEvaluation) (activeEvals.get(i));
					UIInternalLink.make(activeEvalsRb, "activeEvalTitle", eval1.getTitle(),new PreviewEvalParameters( //$NON-NLS-1$
							PreviewEvalProducer.VIEW_ID, eval1.getId(),eval1.getTemplate().getId(), null, ControlPanelProducer.VIEW_ID));
					
					int count = evaluationsLogic.countEvaluationContexts(eval1.getId());
					if (count > 1) {
						UICommand activeEvalAssigned = UICommand.make(activeEvalsRb, 
								"activeEvalAssigned", count+"courses", //$NON-NLS-1$ //$NON-NLS-2$
								"#{evaluationBean.evalAssigned}"); //$NON-NLS-1$
						activeEvalAssigned.parameters.add(new UIELBinding("#{evaluationBean.evalId}", eval1.getId())); //$NON-NLS-1$
					} else {
						UICommand activeEvalAssigned = UICommand.make(activeEvalsRb, 
								"activeEvalAssigned", getTitleForFirstEvalContext(eval1.getId()), //$NON-NLS-1$
								"#{evaluationBean.evalAssigned}"); //$NON-NLS-1$
						activeEvalAssigned.parameters.add(new UIELBinding("#{evaluationBean.evalId}", eval1.getId())); //$NON-NLS-1$
					}

					UIOutput.make(activeEvalsRb, "activeEvalStartDate", df.format(eval1.getStartDate())); //$NON-NLS-1$
					UIOutput.make(activeEvalsRb, "activeEvalDueDate", df.format(eval1.getDueDate())); //$NON-NLS-1$
					
					UICommand activeEvalEdit = UICommand.make(activeEvalsRb, "activeEvalEditLink", UIMessage.make("controlpanel.eval.edit.link"), //$NON-NLS-1$ //$NON-NLS-2$
											"#{evaluationBean.editEvalSettingAction}"); //$NON-NLS-1$
					activeEvalEdit.parameters.add(new UIELBinding("#{evaluationBean.eval.id}", eval1.getId())); //$NON-NLS-1$
					
					// GET user Rate: Response/Enrollement
					int ctResponses = responsesLogic.countResponses(eval1.getId(), null);
					int ctEnrollments = getTotalEnrollmentsForEval(eval1.getId());
					UIOutput.make(activeEvalsRb, "users", //$NON-NLS-1$
							ctResponses + "/" + ctEnrollments ); //$NON-NLS-1$
				} // end of for loop
			}

			UIMessage.make(tofill, "eval-closed-header-title", "controlpanel.eval.closed.header.title"); //$NON-NLS-1$ //$NON-NLS-2$
			UIMessage.make(tofill, "eval-closed-header-desc", "controlpanel.eval.closed.header.desc"); //$NON-NLS-1$ //$NON-NLS-2$

			// display closed Evaluation Table
			if (closedEvals != null && closedEvals.size() > 0) {
				UIBranchContainer closedEvalTable = UIBranchContainer.make(tofill, "closedEvalTable:"); //$NON-NLS-1$

				UIMessage.make(closedEvalTable, "eval-title-header", "controlpanel.eval.title.header"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(closedEvalTable, "eval-due-date-header", "controlpanel.eval.due.date.header"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(closedEvalTable, "eval-response-rate-header", "controlpanel.eval.response.rate.header");				 //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(closedEvalTable, "eval-settings-header", "controlpanel.eval.report.header"); //$NON-NLS-1$ //$NON-NLS-2$

				
				for (int i = 0; i < closedEvals.size(); i++) {
					UIBranchContainer closedEvalsRb = UIBranchContainer.make(closedEvalTable, "closedEvalList:", //$NON-NLS-1$
							Integer.toString(i));

					EvalEvaluation eval1 = (EvalEvaluation) (closedEvals.get(i));
					UIInternalLink.make(closedEvalsRb, "closedEvalTitle", eval1.getTitle(),new PreviewEvalParameters( //$NON-NLS-1$
							PreviewEvalProducer.VIEW_ID, eval1.getId(),eval1.getTemplate().getId(), null, ControlPanelProducer.VIEW_ID));
					
					UIOutput.make(closedEvalsRb, "closedEvalDueDate", df.format(eval1.getDueDate())); //$NON-NLS-1$
					// get response, get enrollment
					int ctResponses = responsesLogic.countResponses(eval1.getId(), null);
					int ctEnrollments = getTotalEnrollmentsForEval(eval1.getId());
					double per = (1.0 * ctResponses )/ctEnrollments;
					long percentage = Math.round(per*100);
					
					UIOutput.make(closedEvalsRb, "reponseRate", ctResponses + "/"+ ctEnrollments +" - "+percentage +"%"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					if(evaluationsLogic.getEvaluationState(eval1.getId())==EvalConstants.EVALUATION_STATE_VIEWABLE){
						UIInternalLink.make(closedEvalsRb, "viewReportLink", UIMessage.make("controlpanel.eval.report.link"),  //$NON-NLS-1$ //$NON-NLS-2$
								new EvalViewParameters(ViewReportProducer.VIEW_ID, 
									eval1.getId()));	
					}
					else{
						UIMessage.make(closedEvalsRb, "viewReportLabel", "controlpanel.eval.report.viewable.on");
						UIOutput.make(closedEvalsRb, "closedEvalViewDate", df.format(eval1.getViewDate())); //$NON-NLS-1$
					}
				} // end of for loop
			}

		} // end of :if (evals !=null && evals.size() > 0)

	} // end of method:fillComponents


	public List reportNavigationCases() {
		List i = new ArrayList();
		
		i.add(new NavigationCase(ModifyTemplateItemsProducer.VIEW_ID, new SimpleViewParameters(
				ModifyTemplateItemsProducer.VIEW_ID)));
		i.add(new NavigationCase(ModifyTemplateProducer.VIEW_ID, new SimpleViewParameters(
				ModifyTemplateProducer.VIEW_ID)));
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
	private String getTitleForFirstEvalContext(Long evaluationId) {
		List acs = assignsLogic.getAssignContextsByEvalId(evaluationId);
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

		List l = assignsLogic.getAssignContextsByEvalId(evaluationId);
		for (int i=0; i<l.size(); i++) {
			EvalAssignGroup eac = (EvalAssignGroup) l.get(i);
			String context = eac.getEvalGroupId();
			Set userIds = external.getUserIdsForEvalGroup(context, EvalConstants.PERM_TAKE_EVALUATION);
			totalEnrollments = totalEnrollments + userIds.size();
		}
		return totalEnrollments;
	}

}
