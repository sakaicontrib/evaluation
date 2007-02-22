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
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * assign an evaluation to courses 
 * 
 * @author:Kapil Ahuja (kahuja@vt.edu)
 * @author: Rui Feng (fengr@vt.edu)
 */

public class EvaluationAssignProducer implements ViewComponentProducer, NavigationCaseReporter {
	public static final String VIEW_ID = "evaluation_assign"; //$NON-NLS-1$


	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}	
	
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}
	
	public String getViewID() {
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		
		UIOutput.make(tofill, "assign-eval-ext-title", messageLocator.getMessage("assigneval.page.ext.title")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "create-eval-title", messageLocator.getMessage("createeval.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "assign-eval-title", messageLocator.getMessage("assigneval.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		
		
		UIInternalLink.make(tofill, "summary-toplink", new SimpleViewParameters(SummaryProducer.VIEW_ID));	 //$NON-NLS-1$
		
		UIForm form = UIForm.make(tofill, "evalAssignForm"); //$NON-NLS-1$
		
		UIOutput.make(form, "evaluationTitle", null, "#{evaluationBean.eval.title}"); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIOutput.make(form, "assign-eval-instructions-pre", messageLocator.getMessage("assigneval.instructions.pre")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "assign-eval-instructions-post", messageLocator.getMessage("assigneval.instructions.post")); //$NON-NLS-1$ //$NON-NLS-2$
		
		
		List sites = external.getEvalGroupsForUser(external.getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED);
		if(sites.size() >0){
			String[] ids = new String[sites.size()];
			String[] labels = new String[sites.size()];
			for (int i=0; i< sites.size(); i++) {
				EvalGroup c = (EvalGroup) sites.get(i);
				ids[i] = c.evalGroupId;
				labels[i] = c.title;
			}
			
			UISelect siteCheckboxes = UISelect.makeMultiple(form, "siteCheckboxes", ids, "#{evaluationBean.selectedSakaiSiteIds}", null); //$NON-NLS-1$ //$NON-NLS-2$
			String selectID = siteCheckboxes.getFullID();
			    
			for (int i = 0; i < ids.length; ++i){
			    UIBranchContainer checkboxRow = UIBranchContainer.make(form, "sites:"); //$NON-NLS-1$
				UISelectChoice.make(checkboxRow, "siteId", selectID, i); //$NON-NLS-1$
				UIOutput.make(checkboxRow, "siteTitle", (String) labels[i]); //$NON-NLS-1$
			 }
		}
		/*
		 * TODO: If more than one course is selected and you come back to this page from confirm page,
		 * then without changing the selection you again go to confirm page, you get a null pointer
		 * that is created by RSF as:
		 * 
		 * 	"Error flattening value[Ljava.lang.String;@944d4a into class [Ljava.lang.String;
		 * 	...
		 *  java.lang.NullPointerException
    	 * 		at uk.org.ponder.arrayutil.ArrayUtil.lexicalCompare(ArrayUtil.java:205)
    	 *  	at uk.org.ponder.rsf.uitype.StringArrayUIType.valueUnchanged(StringArrayUIType.java:23)
		 *  ..."
		 */
/*		Map sites = evaluationBean.getSites();
		if ( sites !=null) {
		
			//Preparing the string array of template ids and corresponding title's 
			Object[] idObjects = sites.keySet().toArray();
			String[] ids = new String[idObjects.length];
			for (int count = 0; count < idObjects.length; count++)
				ids[count] = (String) idObjects[count];
			
			Object[] labels = sites.values().toArray();
			
			
		    UISelect siteCheckboxes = UISelect.makeMultiple(form, "siteCheckboxes", ids, "#{evaluationBean.selectedSakaiSiteIds}", null); //$NON-NLS-1$ //$NON-NLS-2$
		    String selectID = siteCheckboxes.getFullID();
		    
		   for (int i = 0; i < ids.length; ++i) 
		    {
		    	UIBranchContainer checkboxRow = UIBranchContainer.make(form, "sites:"); //$NON-NLS-1$
				UISelectChoice.make(checkboxRow, "siteId", selectID, i); //$NON-NLS-1$
				UIOutput.make(checkboxRow, "siteTitle", (String) labels[i]); //$NON-NLS-1$
		    }
		}
	*/	
		
		UIOutput.make(form, "name-header", messageLocator.getMessage("assigneval.name.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "select-header", messageLocator.getMessage("assigneval.select.header"));		 //$NON-NLS-1$ //$NON-NLS-2$
		
		UICommand.make(form, "cancel-button", messageLocator.getMessage("general.cancel.button"), "#{evaluationBean.cancelAssignAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		UICommand.make(form, "editSettings", messageLocator.getMessage("assigneval.edit.settings.button"), "#{evaluationBean.backToSettingsAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		UICommand.make(form, "confirmAssignCourses", messageLocator.getMessage("assigneval.save.assigned.button"), "#{evaluationBean.confirmAssignCoursesAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	public List reportNavigationCases() {
		List i = new ArrayList();

		i.add(new NavigationCase(SummaryProducer.VIEW_ID, new SimpleViewParameters(SummaryProducer.VIEW_ID)));
		i.add(new NavigationCase(EvaluationSettingsProducer.VIEW_ID, new SimpleViewParameters(EvaluationSettingsProducer.VIEW_ID)));
		i.add(new NavigationCase(EvaluationAssignConfirmProducer.VIEW_ID, new SimpleViewParameters(EvaluationAssignConfirmProducer.VIEW_ID)));

		return i;
	}
}

