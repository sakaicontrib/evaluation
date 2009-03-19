
package org.sakaiproject.evaluation.tool.producers;

import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.ReportingPermissions;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
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
 * @author Steven Githens
 */
public class ReportChooseGroupsProducer implements ViewComponentProducer, ViewParamsReporter {

    public static final String VIEW_ID = "report_groups";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    private ReportingPermissions reportingPermissions;
    public void setReportingPermissions(ReportingPermissions perms) {
        this.reportingPermissions = perms;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
        boolean createTemplate = authoringService.canCreateTemplate(currentUserId);
        boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);

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
            UIInternalLink.make(tofill, "control-scales-link",
                    UIMessage.make("controlscales.page.title"),
                    new SimpleViewParameters(ControlScalesProducer.VIEW_ID));
        }

        if (createTemplate) {
            UIInternalLink.make(tofill, "control-templates-link",
                    UIMessage.make("controltemplates.page.title"), 
                    new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));
            if (!((Boolean) evalSettings.get(EvalSettings.DISABLE_ITEM_BANK))) {
                UIInternalLink.make(tofill, "control-items-link",
                        UIMessage.make("controlitems.page.title"), 
                        new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
            }
        } else {
            throw new SecurityException("User attempted to access " + 
                    VIEW_ID + " when they are not allowed");
        }

        if (beginEvaluation) {
            UIInternalLink.make(tofill, "control-evaluations-link",
                    UIMessage.make("controlevaluations.page.title"),
                    new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
        }

        UIMessage.make(tofill, "report-groups-title", "reportgroups.page.title");

        ReportParameters reportViewParams = (ReportParameters) viewparams;
        Long evaluationId = reportViewParams.evaluationId;
        if (evaluationId != null) {
            // get the evaluation from the id
            EvalEvaluation evaluation = evaluationService.getEvaluationById(evaluationId);

            // do a permission check
            Set<String> evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(evaluation);
            if (evalGroupIds.isEmpty()) {
                UIMessage.make(tofill, "security-warning", "viewreport.not.allowed");
                return;
            }

            // create a copy of the VP and then set it to the right view (to avoid corrupting the original)
            ReportParameters rvp = (ReportParameters) reportViewParams.copyBase();

            // set the target for the get form
            rvp.viewID = ReportsViewingProducer.VIEW_ID;

            // use a get form which already has the evaluation id in it
            UIForm form = UIForm.make(tofill, "report-groups-form", rvp);
            UIMessage.make(form, "report-group-main-message", "reportgroups.main.message");

            String[] possibleGroupIdsToView = new String[evalGroupIds.size()];
            String[] possibleGroupTitlesToView = new String[evalGroupIds.size()];
            int counter = 0;
            for (String evalGroupId : evalGroupIds) {
                possibleGroupIdsToView[counter] = evalGroupId;
                possibleGroupTitlesToView[counter] = commonLogic.makeEvalGroupObject(evalGroupId).title;
                counter++;
            }

            UISelect radios = UISelect.makeMultiple(form, "selectHolder", possibleGroupIdsToView, possibleGroupTitlesToView, "groupIds", null);
            String selectID = radios.getFullID();

            for (int i = 0; i < possibleGroupIdsToView.length; i++) {
                UIBranchContainer groupBranch = UIBranchContainer.make(form, "groupRow:", i+"");
                UISelectChoice choice = UISelectChoice.make(groupBranch, "groupCheck", selectID, i);
                UISelectLabel.make(groupBranch, "groupName", selectID, i).decorate(new UILabelTargetDecorator(choice) );
            }

            UICommand.make(form, "viewReport", UIMessage.make("general.submit.button"));
        } else {
            throw new IllegalArgumentException("Evaluation id must be set");
        }

    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new ReportParameters();
    }

}
