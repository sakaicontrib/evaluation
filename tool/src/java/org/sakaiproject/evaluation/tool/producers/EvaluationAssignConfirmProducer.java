/******************************************************************************
 * EvaluationStartProducer.java - created by kahuja@vt.edu on Oct 05, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationBean;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Confirmation of assign an evaluation to courses
 * 
 * @author:Kapil Ahuja (kahuja@vt.edu)
 * @author: Rui Feng (fengr@vt.edu)
 */


public class EvaluationAssignConfirmProducer implements ViewComponentProducer, NavigationCaseReporter {
	public static final String VIEW_ID = "evaluation_assign_confirm"; //$NON-NLS-1$
	public String getViewID() {
		return VIEW_ID;
	}
	
	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}	
	
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}
	
	private EvaluationBean evaluationBean;
	public void setEvaluationBean(EvaluationBean evaluationBean) {
		this.evaluationBean = evaluationBean;
	}

	

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		UIOutput.make(tofill, "confirm-assignment-title", messageLocator.getMessage("evaluationassignconfirm.page.title")); //$NON-NLS-1$ //$NON-NLS-2$

		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
							new SimpleViewParameters(SummaryProducer.VIEW_ID));			

		UIOutput.make(tofill, "create-evaluation-title", messageLocator.getMessage("createeval.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "assign-evaluation-title", messageLocator.getMessage("assigneval.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "confirm-assignment-title", messageLocator.getMessage("evaluationassignconfirm.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIOutput.make(tofill, "evaluationTitle", null, "#{evaluationBean.eval.title}");		 //$NON-NLS-1$ //$NON-NLS-2$
		//Get selected id's and all id + title map.
		String[] selectedIds = evaluationBean.selectedSakaiSiteIds;
		int[] enrollment = evaluationBean.enrollment;
		
		List evaluatedContexts = external.getEvalGroupsForUser(external.getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED);
		Map allIdTitleMap = new HashMap();  //Map allIdTitleMap = evaluationBean.getSites();
		for (int i=0; i< evaluatedContexts.size(); i++) {
			EvalGroup c = (EvalGroup) evaluatedContexts.get(i);
			//ids[i] = c.context;
			//labels[i] = c.title;
			allIdTitleMap.put(c.evalGroupId, c.title);
		}
		UIOutput.make(tofill, "eval-assign-desc-prename", messageLocator.getMessage("evaluationassignconfirm.eval.assign.desc.prename")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "eval-assign-desc-postname", messageLocator.getMessage("evaluationassignconfirm.eval.assign.desc.postname")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "courses-selected-header", messageLocator.getMessage("evaluationassignconfirm.courses.selected.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "title-header", messageLocator.getMessage("evaluationassignconfirm.title.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "enrollment-header", messageLocator.getMessage("evaluationassignconfirm.enrollment.header")); //$NON-NLS-1$ //$NON-NLS-2$
		
	    for (int i = 0; i < selectedIds.length; ++i) 
	    {
	    	UIBranchContainer siteRow = UIBranchContainer.make(tofill, "sites:"); //$NON-NLS-1$
			UIOutput.make(siteRow, "siteTitle", (String) allIdTitleMap.get(selectedIds[i])); //$NON-NLS-1$

			UIOutput.make(siteRow, "enrollment", "" + enrollment[i] + ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    }
		
	    /*
	     * show submit buttons for first time evaluation creation && Queued Evaluation case
	     * 
	     * */
	    if(evaluationBean.eval.getId()== null ){//first time evaluation creation
	    	showButtonsForm(tofill);
	    	
	    }else{
	     //check if evaluation	is queued;Closed,started evaluation can not be changed
	    	Date today = new Date();
	    	Date startDate = evaluationBean.eval.getStartDate();
	    	if(today.before(startDate)){
	    		showButtonsForm(tofill);
	    	}
	    }
	    
	}
	
	private void showButtonsForm(UIContainer tofill){
		
		UIBranchContainer showButtons = UIBranchContainer.make(tofill, "showButtons:"); //$NON-NLS-1$
		UIForm evalAssignForm = UIForm.make(showButtons, "evalAssignForm"); //$NON-NLS-1$
		UICommand.make(evalAssignForm, "doneAssignment", messageLocator.getMessage("evaluationassignconfirm.done.button"), "#{evaluationBean.doneAssignmentAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		UICommand.make(evalAssignForm, "changeAssignedCourse", messageLocator.getMessage("evaluationassignconfirm.changes.assigned.courses.button"),  //$NON-NLS-1$ //$NON-NLS-2$
				"#{evaluationBean.changeAssignedCourseAction}"); //$NON-NLS-1$
	}
	
	public List reportNavigationCases() {
		List i = new ArrayList();
		i.add(new NavigationCase(ControlPanelProducer.VIEW_ID, new SimpleViewParameters(ControlPanelProducer.VIEW_ID)));
		i.add(new NavigationCase(EvaluationAssignProducer.VIEW_ID, new SimpleViewParameters(EvaluationAssignProducer.VIEW_ID)));
	
		return i;
	}
}
