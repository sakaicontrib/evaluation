
package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.ReportsBean;
import org.sakaiproject.evaluation.tool.viewparams.ExpertItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

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

/**
 * This allows users to choose the report they want to view for an evaluation
 * 
 * @author Will Humphries
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ReportChooseGroupsProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

    public static final String VIEW_ID = "report_groups";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalExternalLogic externalLogic;
    public void setExternalLogic(EvalExternalLogic externalLogic) {
        this.externalLogic = externalLogic;
    }

    private EvalEvaluationsLogic evalsLogic;
    public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
        this.evalsLogic = evalsLogic;
    }

    public ReportsBean reportsBean;
    public void setReportsBean(ReportsBean reportsBean) {
        this.reportsBean = reportsBean;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        String currentUserId = externalLogic.getCurrentUserId();

        UIMessage.make(tofill, "report-groups-title", "reportgroups.page.title");
        UIInternalLink.make(tofill, "summary-toplink", 
                UIMessage.make("summary.page.title"), new SimpleViewParameters(SummaryProducer.VIEW_ID));

        ReportParameters reportViewParams = (ReportParameters) viewparams;
        Long evaluationId = reportViewParams.evaluationId;
        if (evaluationId != null) {
            // get the evaluation from the id
            EvalEvaluation evaluation = evalsLogic.getEvaluationById(evaluationId);

            // do a permission check
            if (! currentUserId.equals(evaluation.getOwner()) &&
                    ! externalLogic.isUserAdmin(currentUserId)) { // TODO - this check is no good, we need a real one -AZ
                throw new SecurityException("Invalid user attempting to access reports page: " + currentUserId);
            }

            // create a copy of the VP and then set it to the right view (to avoid corrupting the original)
            ReportParameters rvp = (ReportParameters) reportViewParams.copyBase();

            // set the target for the get form
            rvp.viewID = ReportsViewingProducer.VIEW_ID;

            // use a get form which already has the evaluation id in it
            UIForm form = UIForm.make(tofill, "report-groups-form", rvp);
            UIMessage.make(form, "report-group-main-message", "reportgroups.main.message");		
            //form.parameters.add(new UIELBinding("#{reportsBean.evalId}", evaluationId));

            Map evalGroups = evalsLogic.getEvaluationGroups(new Long[] {evaluationId}, false);
            List groups = (List) evalGroups.get(evaluationId);
            for (int i = 0; i < groups.size(); i++) {
                UIBranchContainer groupBranch = UIBranchContainer.make(form, "groupRow:", i+"");
                EvalGroup currGroup = (EvalGroup) groups.get(i);
                // checkbox - groupCheck
                UIBoundBoolean.make(groupBranch, "groupCheck",
                        "#{groupIds." + currGroup.evalGroupId + "}", Boolean.FALSE);
                // uioutput - groupname
                UIOutput.make(groupBranch, "groupName", currGroup.title);
            }

            // uicommand submit
            UICommand.make(form, "viewReport", UIMessage.make("general.submit.button"), "#{reportsBean.chooseGroupsAction}");
        } else {
            throw new IllegalArgumentException("Evaluation id must be set");
        }

    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
     */
    public List reportNavigationCases() {
        List i = new ArrayList();
        i.add(new NavigationCase("success", new TemplateViewParameters(ReportsViewingProducer.VIEW_ID, null), ARIResult.FLOW_ONESTEP));
        return i;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new ReportParameters();
    }

}