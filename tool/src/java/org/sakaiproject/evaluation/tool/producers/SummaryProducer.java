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

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.logic.model.Context;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.params.EvalTakeViewParameters;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;
import org.sakaiproject.evaluation.tool.params.PreviewEvalParameters;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
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

	public static final String VIEW_ID = "summary"; //$NON-NLS-1$
	public String getViewID() {
		return VIEW_ID;
	}

	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
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

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	private Locale locale;
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {

		// local variables used in the render logic
		String currentUserId = external.getCurrentUserId();
		String currentContext = external.getCurrentContext();
		boolean userAdmin = external.isUserAdmin(currentUserId);
		boolean createTemplate = templatesLogic.canCreateTemplate(currentUserId);
		boolean beginEvaluation = evaluationsLogic.canBeginEvaluation(currentUserId);
		// use a date which is related to the current users locale
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

		// page title
		UIOutput.make(tofill, "page-title", messageLocator.getMessage("summary.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		
		/*
		 * top links here
		 */
		if (createTemplate) {
			UIInternalLink.make(tofill, "control-panel-toplink", //$NON-NLS-1$
				messageLocator.getMessage("controlpanel.page.title"),  //$NON-NLS-1$
				new SimpleViewParameters(ControlPanelProducer.VIEW_ID));
			UIInternalLink.make(tofill, "create-template-toplink", //$NON-NLS-1$
					messageLocator.getMessage("createtemplate.page.title"),  //$NON-NLS-1$
					new EvalViewParameters(ModifyTemplateProducer.VIEW_ID, null));
		}

		if ( beginEvaluation ) {
			UIInternalLink.make(tofill, "begin-evaluation-toplink", //$NON-NLS-1$
				messageLocator.getMessage("beginevaluation.page.title"),  //$NON-NLS-1$
				new EvalViewParameters(EvaluationStartProducer.VIEW_ID, null));
		}

		if (userAdmin) {
			UIInternalLink.make(tofill, "administrate-toplink", //$NON-NLS-1$
				messageLocator.getMessage("administrate.page.title"),  //$NON-NLS-1$
				new SimpleViewParameters(AdministrateProducer.VIEW_ID));
		}

		/*
		 * Notification box listing box
		 */
		boolean userHasNotifications = false;
		if (userHasNotifications) {
			UIBranchContainer notificationsBC = UIBranchContainer.make(tofill, "notificationsBox:"); //$NON-NLS-1$
			UIOutput.make(notificationsBC, "notifications-title", messageLocator.getMessage("summary.notifications.title")); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(notificationsBC, "notifications-higher-level", messageLocator.getMessage("summary.eval.assigned.from.above")); //$NON-NLS-1$ //$NON-NLS-2$
			// add other stuff
		}

		/*
		 * for the evaluations taking box
		 */
		List evalsToTake = evaluationsLogic.getEvaluationsForUser(currentUserId, true, false);
		if (evalsToTake.size() > 0) {
			UIBranchContainer evalBC = UIBranchContainer.make(tofill, "evaluationsBox:"); //$NON-NLS-1$

			UIOutput.make(evalBC, "evaluations-title", messageLocator.getMessage("summary.evaluations.title")); //$NON-NLS-1$ //$NON-NLS-2$
			// build an array of evaluation ids
			Long[] evalIds = new Long[evalsToTake.size()];
			for (int i=0; i<evalsToTake.size(); i++) {
				evalIds[i] = ((EvalEvaluation) evalsToTake.get(i)).getId();
			}

			// now fetch all the information we care about for these evaluations at once (for speed)
			Map evalContexts = evaluationsLogic.getEvaluationContexts(evalIds, false);
			List evalResponses = responsesLogic.getEvaluationResponses(currentUserId, evalIds);

			for (Iterator itEvals = evalsToTake.iterator(); itEvals.hasNext();) {
				EvalEvaluation eval = (EvalEvaluation) itEvals.next();

				UIBranchContainer evalrow = UIBranchContainer.make(evalBC, 
						"evaluationsList:", eval.getId().toString() ); //$NON-NLS-1$

				UIOutput.make(evalrow, "evaluationTitle", eval.getTitle() ); //$NON-NLS-1$
				UIOutput.make(evalrow, "evaluationStartDate", df.format(eval.getStartDate()) ); //$NON-NLS-1$
				UIOutput.make(evalrow, "evaluationDueDate", df.format(eval.getDueDate()) ); //$NON-NLS-1$

				List contexts = (List) evalContexts.get(eval.getId());
				for (int j=0; j<contexts.size(); j++) {
					Context ctxt = (Context) contexts.get(j);
					//check that the user can take evaluations in this context
					if(external.isUserAllowedInContext(external.getCurrentUserId(), EvalConstants.PERM_TAKE_EVALUATION, ctxt.context)){
						String context = ctxt.context;
						String title = ctxt.title;
						String status = messageLocator.getMessage("unknown.caps"); //$NON-NLS-1$
	
						// find the object in the list matching the context and evalId,
						// leave as null if not found -AZ
						EvalResponse response = null;
	
						for (int k=0; k<evalResponses.size(); k++) {
							EvalResponse er = (EvalResponse) evalResponses.get(k);
							if (context.equals(er.getContext()) &&
									eval.getId().equals(er.getEvaluation().getId())) {
								response = er;
								break;
							}
						}
	
						if (context.equals(currentContext)) {
							// TODO - do something when the context matches
						}
	
						UIBranchContainer evalcourserow = UIBranchContainer.make(evalrow, 
								"evaluationsCourseList:", context ); //$NON-NLS-1$
	
				
						// set status
						if (response != null && response.getEndTime() != null) {
							if(eval.getModifyResponsesAllowed().booleanValue()){
								// take eval link when pending
								UIInternalLink.make(evalcourserow, "evaluationCourseLink", title,  //$NON-NLS-1$
										new EvalTakeViewParameters(TakeEvalProducer.VIEW_ID,
												eval.getId(), response.getId(), context) );
								status = messageLocator.getMessage("summary.status.pending"); //$NON-NLS-1$							
							}
							else{
								// preview only when completed
								UIInternalLink.make(evalcourserow, "evaluationCourseLink", title,  //$NON-NLS-1$
										new PreviewEvalParameters(PreviewEvalProducer.VIEW_ID,
												eval.getId(),eval.getTemplate().getId(),context, SummaryProducer.VIEW_ID) );
								status = messageLocator.getMessage("summary.status.completed"); //$NON-NLS-1$
							}
						} else {
							// take eval link when pending
							UIInternalLink.make(evalcourserow, "evaluationCourseLink", title,  //$NON-NLS-1$
									new EvalTakeViewParameters(TakeEvalProducer.VIEW_ID,
											eval.getId(), null, context) );
							status = messageLocator.getMessage("summary.status.pending"); //$NON-NLS-1$
						}
						UIOutput.make(evalcourserow, "evaluationCourseStatus", status );					 //$NON-NLS-1$
					}
				}
			}
		}

		/**
		 * for the evaluations admin box
		 */
		List evals = evaluationsLogic.getVisibleEvaluationsForUser(currentUserId, true);
		if (! evals.isEmpty()) {
			UIBranchContainer evalAdminBC = UIBranchContainer.make(tofill, "evalAdminBox:"); //$NON-NLS-1$
			UIInternalLink.make(evalAdminBC, "evaladmin-title",  //$NON-NLS-1$
					messageLocator.getMessage("summary.evaluations.admin"),  //$NON-NLS-1$
					new SimpleViewParameters(ControlPanelProducer.VIEW_ID) );
			UIForm evalAdminForm = UIForm.make(evalAdminBC , "evalAdminForm"); //$NON-NLS-1$
			
			UIOutput.make(evalAdminForm, "evaladmin-header-title", messageLocator.getMessage("summary.header.title") ); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(evalAdminForm, "evaladmin-header-status", messageLocator.getMessage("summary.header.status") ); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(evalAdminForm, "evaladmin-header-date", messageLocator.getMessage("summary.header.date") ); //$NON-NLS-1$ //$NON-NLS-2$

			for (Iterator iter = evals.iterator(); iter.hasNext();) {
				EvalEvaluation eval = (EvalEvaluation) iter.next();

				UIBranchContainer evalrow = UIBranchContainer.make(evalAdminForm, 
						"evalAdminList:", eval.getId().toString() ); //$NON-NLS-1$
				
				Date date;
				
				String evalStatus=evaluationsLogic.getEvaluationState(eval.getId());
				if (evalStatus==EvalConstants.EVALUATION_STATE_INQUEUE){
					date=eval.getStartDate();
				}
				else if (evalStatus==EvalConstants.EVALUATION_STATE_ACTIVE){
					date=eval.getStopDate();
				}
				else if (evalStatus==EvalConstants.EVALUATION_STATE_DUE){
					date=eval.getDueDate();
				}
				else if (evalStatus==EvalConstants.EVALUATION_STATE_CLOSED){
					date=eval.getViewDate();
				}
				else if (evalStatus==EvalConstants.EVALUATION_STATE_VIEWABLE){
					date=eval.getViewDate();
					UIInternalLink.make(evalrow, "viewReportLink", messageLocator.getMessage("viewreport.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
							new EvalViewParameters(ViewReportProducer.VIEW_ID, eval.getId() ));		
				}
				else date=eval.getStartDate();

				
				/**
				 * 
				 * 1) if a evaluation is queued, title link go to EditSettings page with populated data
				 * 2) if a evaluation is active, title link go to EditSettings page with populated data
				 * but start date should be disabled
				 * 3) if a evaluation is closed, title link go to previewEval page with populated data
				 */
				if (evalStatus==EvalConstants.EVALUATION_STATE_CLOSED || evalStatus==EvalConstants.EVALUATION_STATE_VIEWABLE){
					UIInternalLink.make(evalrow, "evalAdminTitleLink_preview", eval.getTitle(),  //$NON-NLS-1$
							new PreviewEvalParameters(PreviewEvalProducer.VIEW_ID,
									eval.getId(),eval.getTemplate().getId(),null, SummaryProducer.VIEW_ID) );
				} else {
					UICommand evalEditUIC = UICommand.make(evalrow, "evalAdminTitleLink_edit", eval.getTitle(), //$NON-NLS-1$
						"#{evaluationBean.editEvalSettingAction}"); //$NON-NLS-1$
					evalEditUIC.parameters.add(new UIELBinding("#{evaluationBean.eval.id}", eval.getId())); //$NON-NLS-1$
				}
				UIOutput.make(evalrow, "evalAdminStatus", messageLocator.getMessage("summary.status."+evalStatus) ); //$NON-NLS-1$
				UIOutput.make(evalrow, "evalAdminDateLabel", messageLocator.getMessage("summary.label."+evalStatus) ); //$NON-NLS-1$
				UIOutput.make(evalrow, "evalAdminDate", df.format(date) ); //$NON-NLS-1$
			}
		}


		/*
		 * Site/Group listing box
		 */
		// TODO - only render this when not inside a worksite -AZ
//		if (currentContext == null) {
			String NO_ITEMS = messageLocator.getMessage("no.list.items"); //$NON-NLS-1$

			UIBranchContainer contextsBC = UIBranchContainer.make(tofill, "siteListingBox:"); //$NON-NLS-1$
			UIOutput.make(contextsBC, "sitelisting-title", messageLocator.getMessage("summary.sitelisting.title") ); //$NON-NLS-1$ //$NON-NLS-2$

			UIOutput.make(contextsBC, "sitelisting-evaluated-text", messageLocator.getMessage("summary.sitelisting.evaluated") ); //$NON-NLS-1$ //$NON-NLS-2$
			List evaluatedContexts = external.getContextsForUser(currentUserId, EvalConstants.PERM_BE_EVALUATED);
			if (evaluatedContexts.size() > 0) {
				for (int i=0; i<evaluatedContexts.size(); i++) {
					Context c = (Context) evaluatedContexts.get(i);
					UIBranchContainer evaluatedBC = UIBranchContainer.make(contextsBC, "evaluatedList:"); //$NON-NLS-1$
					UIOutput.make(evaluatedBC, "evaluatedListTitle", c.title); //$NON-NLS-1$
				}
			} else {
				UIOutput.make(contextsBC, "evaluatedListNone", NO_ITEMS ); //$NON-NLS-1$
			}

			UIOutput.make(contextsBC, "sitelisting-evaluate-text", messageLocator.getMessage("summary.sitelisting.evaluate") ); //$NON-NLS-1$ //$NON-NLS-2$
			List evaluateContexts = external.getContextsForUser(currentUserId, EvalConstants.PERM_TAKE_EVALUATION);
			if (evaluateContexts.size() > 0) {
				for (int i=0; i<evaluateContexts.size(); i++) {
					Context c = (Context) evaluateContexts.get(i);
					UIBranchContainer evaluateBC = UIBranchContainer.make(contextsBC, "evaluateList:"); //$NON-NLS-1$
					UIOutput.make(evaluateBC, "evaluateListTitle", c.title); //$NON-NLS-1$
				}
			} else {
				UIOutput.make(contextsBC, "evaluateListNone", NO_ITEMS ); //$NON-NLS-1$
			}
//		}

		/*
		 * For the Evaluation tools box
		 */
		if (createTemplate || beginEvaluation) {
			UIBranchContainer toolsBC = UIBranchContainer.make(tofill, "toolsBox:"); //$NON-NLS-1$
			UIOutput.make(toolsBC, "tools-title", messageLocator.getMessage("summary.tools.title") ); //$NON-NLS-1$ //$NON-NLS-2$

			if ( createTemplate ) {
				UIInternalLink.make(toolsBC, "createTemplateLink", //$NON-NLS-1$
					messageLocator.getMessage("createtemplate.page.title"), //$NON-NLS-1$
					new EvalViewParameters(ModifyTemplateProducer.VIEW_ID, null));
			}

			if ( beginEvaluation ) {
				UIInternalLink.make(toolsBC, "beginEvaluationLink", //$NON-NLS-1$
					messageLocator.getMessage("beginevaluation.page.title"), //$NON-NLS-1$
					new EvalViewParameters(EvaluationStartProducer.VIEW_ID, null));
			}
		}

	}


	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List i = new ArrayList();
		i.add(new NavigationCase(EvaluationSettingsProducer.VIEW_ID, new SimpleViewParameters(
				EvaluationSettingsProducer.VIEW_ID)));
		i.add(new NavigationCase(PreviewEvalProducer.VIEW_ID, new SimpleViewParameters(
				PreviewEvalProducer.VIEW_ID)));
		return i;
	}

}
