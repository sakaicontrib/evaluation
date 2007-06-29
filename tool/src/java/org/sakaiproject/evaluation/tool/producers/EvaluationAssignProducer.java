/******************************************************************************
 * EvaluationAssignProducer.java - created by kahuja@vt.edu on Oct 05, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationBean;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * assign an evaluation to courses 
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Rui Feng (fengr@vt.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationAssignProducer implements ViewComponentProducer, NavigationCaseReporter {

	public static final String VIEW_ID = "evaluation_assign";
	public String getViewID() {
		return VIEW_ID;
	}

	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private EvaluationBean evaluationBean;
	public void setEvaluationBean(EvaluationBean evaluationBean) {
		this.evaluationBean = evaluationBean;
	}


	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		UIMessage.make(tofill, "page-title", "assigneval.page.title");

		UIInternalLink.make(tofill, "summary-toplink", new SimpleViewParameters(SummaryProducer.VIEW_ID));	

		UIMessage.make(tofill, "create-eval-title", "starteval.page.title");
		UIMessage.make(tofill, "eval-settings-title", "evalsettings.page.title");

		UIMessage.make(tofill, "assign-eval-edit-page-title", "assigneval.assign.page.title", new Object[] {evaluationBean.eval.getTitle()});
		UIMessage.make(tofill, "assign-eval-instructions", "assigneval.assign.instructions", new Object[] {evaluationBean.eval.getTitle()});

		UIForm form = UIForm.make(tofill, "eval-assign-form");

		UIMessage.make(form, "name-header", "assigneval.name.header");
		UIMessage.make(form, "select-header", "assigneval.select.header");		

		List evalGroups = externalLogic.getEvalGroupsForUser(externalLogic.getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED);
		if (evalGroups.size() > 0) {
			String[] ids = new String[evalGroups.size()];
			String[] labels = new String[evalGroups.size()];
			for (int i=0; i < evalGroups.size(); i++) {
				EvalGroup c = (EvalGroup) evalGroups.get(i);
				ids[i] = c.evalGroupId;
				labels[i] = c.title;
			}

			UISelect siteCheckboxes = UISelect.makeMultiple(form, "siteCheckboxes", ids, "#{evaluationBean.selectedSakaiSiteIds}", null);
			String selectID = siteCheckboxes.getFullID();

			for (int i=0; i < ids.length; i++){
				UIBranchContainer checkboxRow = UIBranchContainer.make(form, "sites:", i+"");
				if (i % 2 == 0) {
					checkboxRow.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
				}
				UISelectChoice checkbox = UISelectChoice.make(checkboxRow, "siteId", selectID, i);
				UIOutput title = UIOutput.make(checkboxRow, "siteTitle", (String) labels[i]);
				UILabelTargetDecorator.targetLabel(title, checkbox); // make title a label for checkbox
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

