package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.tool.ReportsBean;
import org.sakaiproject.evaluation.tool.params.TemplateViewParameters;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class ChooseReportGroupsProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

	public static final String VIEW_ID = "report_groups"; //$NON-NLS-1$
	public String getViewID() {
		return VIEW_ID;
	}

	public ViewParameters getViewParameters() {
		return new TemplateViewParameters(VIEW_ID, null);
	}	

	private EvalEvaluationsLogic evalsLogic;
	public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
		this.evalsLogic = evalsLogic;
	}
	
	public ReportsBean reportsBean;
	public void setReportsBean(ReportsBean reportsBean) {
		this.reportsBean = reportsBean;
	}
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		UIMessage.make(tofill, "report-groups-title","reportgroups.page.title"); //$NON-NLS-1$ //$NON-NLS-2$
		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"), new SimpleViewParameters(SummaryProducer.VIEW_ID)); //$NON-NLS-1$ //$NON-NLS-2$

		TemplateViewParameters evalViewParams = (TemplateViewParameters) viewparams;
		if (evalViewParams.templateId != null) {
			UIForm form = UIForm.make(tofill, "report-groups-form");
			UIMessage.make(form , "report-group-main-message", "reportgroups.main.message"); //$NON-NLS-1$ //$NON-NLS-2$		
			Long[] evalIds = {evalViewParams.templateId};
			Map evalGroups = evalsLogic.getEvaluationGroups(evalIds, false);
			List groups = (List) evalGroups.get(evalViewParams.templateId);
			form.parameters.add(new UIELBinding("#{reportsBean.evalId}", evalViewParams.templateId));
			UIBranchContainer groupBranch = null;
			//fxn call to backing bean to set groups in backing bean, and make a list there
			//reportGroupsBean.setPossiblegroups(groups);
			for(int i=0;i<groups.size();i++){
				groupBranch=UIBranchContainer.make(form,"groupRow:",new Integer(i).toString());
				EvalGroup currGroup = (EvalGroup) groups.get(i);
				//checkbox - groupCheck
				UIBoundBoolean.make(groupBranch, "groupCheck",
						"#{reportsBean.groupIds."+currGroup.evalGroupId+"}", Boolean.FALSE); //$NON-NLS-1$ //$NON-NLS-2$
				//uioutput - groupname
				UIOutput.make(groupBranch, "groupName", currGroup.title);
			}
			//uicommand submit
			UICommand.make(form, "viewReport", UIMessage					//$NON-NLS-1$
					.make("general.submit.button"), "#{reportsBean.chooseGroupsAction}"); 	
		}
		
		
	}
	
	public List reportNavigationCases() {
		List i = new ArrayList();
		i.add(new NavigationCase("success", new TemplateViewParameters(ViewReportProducer.VIEW_ID, null), ARIResult.FLOW_ONESTEP));

		return i;
	}
}