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

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
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

	public static final String VIEW_ID = "evaluation_assign";
	public String getViewID() {
		return VIEW_ID;
	}

	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}


	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		
		UIMessage.make(tofill, "assign-eval-ext-title", "assigneval.page.ext.title");
		UIMessage.make(tofill, "create-eval-title", "starteval.page.title");
		UIMessage.make(tofill, "eval-settings-title", "evalsettings.page.title");
		UIMessage.make(tofill, "assign-eval-title", "assigneval.page.title");


		UIInternalLink.make(tofill, "summary-toplink", new SimpleViewParameters(SummaryProducer.VIEW_ID));	
		
		UIForm form = UIForm.make(tofill, "evalAssignForm");
		
		UIOutput.make(form, "evaluationTitle", null, "#{evaluationBean.eval.title}");
		
		UIMessage.make(form, "assign-eval-instructions-pre", "assigneval.instructions.pre");
		UIMessage.make(form, "assign-eval-instructions-post", "assigneval.instructions.post");
		
		
		List sites = external.getEvalGroupsForUser(external.getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED);
		if (sites.size() > 0) {
			String[] ids = new String[sites.size()];
			String[] labels = new String[sites.size()];
			for (int i=0; i < sites.size(); i++) {
				EvalGroup c = (EvalGroup) sites.get(i);
				ids[i] = c.evalGroupId;
				labels[i] = c.title;
			}
			
			UISelect siteCheckboxes = UISelect.makeMultiple(form, "siteCheckboxes", ids, "#{evaluationBean.selectedSakaiSiteIds}", null);
			String selectID = siteCheckboxes.getFullID();
			    
			for (int i = 0; i < ids.length; i++){
			    UIBranchContainer checkboxRow = UIBranchContainer.make(form, "sites:", i+"");
				UISelectChoice.make(checkboxRow, "siteId", selectID, i);
				UIOutput.make(checkboxRow, "siteTitle", (String) labels[i]);
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

		UIMessage.make(form, "name-header", "assigneval.name.header");
		UIMessage.make(form, "select-header", "assigneval.select.header");		
		UICommand.make(form, "cancel-button", UIMessage.make("general.cancel.button"), "#{evaluationBean.cancelAssignAction}");
		UICommand.make(form, "editSettings", UIMessage.make("assigneval.edit.settings.button"), "#{evaluationBean.backToSettingsAction}");
		UICommand.make(form, "confirmAssignCourses", UIMessage.make("assigneval.save.assigned.button"), "#{evaluationBean.confirmAssignCoursesAction}");
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List i = new ArrayList();
		i.add(new NavigationCase(SummaryProducer.VIEW_ID, new SimpleViewParameters(SummaryProducer.VIEW_ID)));
		i.add(new NavigationCase(EvaluationSettingsProducer.VIEW_ID, new SimpleViewParameters(EvaluationSettingsProducer.VIEW_ID)));
		i.add(new NavigationCase(EvaluationAssignConfirmProducer.VIEW_ID, new SimpleViewParameters(EvaluationAssignConfirmProducer.VIEW_ID)));
		return i;
	}
}

