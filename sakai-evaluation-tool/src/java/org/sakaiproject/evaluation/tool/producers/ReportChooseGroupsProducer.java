/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.tool.producers;

import java.util.Set;
import java.util.Arrays;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.ReportingPermissions;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This allows users to choose the report they want to view for an evaluation
 * 
 * @author Will Humphries
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Steven Githens
 */
public class ReportChooseGroupsProducer extends EvalCommonProducer implements ViewParamsReporter {

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

    private ReportingPermissions reportingPermissions;
    public void setReportingPermissions(ReportingPermissions perms) {
        this.reportingPermissions = perms;
    }

    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        ReportParameters reportViewParams = (ReportParameters) viewparams;
        Long evaluationId = reportViewParams.evaluationId;

        UIMessage.make(tofill, "report-groups-title", "reportgroups.page.title");

        if (! reportViewParams.external) {
            /*
             * top links here
             */
            navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());
        }

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

            final String[] possibleGroupIdsToView = new String[evalGroupIds.size()];
            final String[] possibleGroupTitlesToView = new String[evalGroupIds.size()];
            int counter = 0;
			
            for (String evalGroupId : evalGroupIds) {
                possibleGroupIdsToView[counter] = evalGroupId;
                possibleGroupTitlesToView[counter] = commonLogic.makeEvalGroupObject(evalGroupId).title;
                counter++;
            }
			
            //Sort strings so the groups are listed alphabetically
            String[] possibleGroupIdsToViewSorted = new String[possibleGroupIdsToView.length];
            String[] possibleGroupTitlesToViewSorted = new String[possibleGroupTitlesToView.length];
			
            // Initialize index array
            Integer[] sortOrder = new Integer[possibleGroupIdsToView.length];
            for(int i=0; i<sortOrder.length; i++){
                sortOrder[i] = i;
            }
            // sort and save to index
            Arrays.sort(sortOrder, (Integer a, Integer b) -> possibleGroupTitlesToView[a].compareTo(possibleGroupTitlesToView[b]));
            for(int i=0;i<sortOrder.length;i++){
                possibleGroupIdsToViewSorted[i] = possibleGroupIdsToView[sortOrder[i]];
                possibleGroupTitlesToViewSorted[i] = possibleGroupTitlesToView[sortOrder[i]];
            }

            UISelect radios = UISelect.makeMultiple(form, "selectHolder", possibleGroupIdsToViewSorted, possibleGroupTitlesToViewSorted, "groupIds", null);
            String selectID = radios.getFullID();

            for (int i = 0; i < possibleGroupIdsToViewSorted.length; i++) {
                UIBranchContainer groupBranch = UIBranchContainer.make(form, "groupRow:", i+"");
                UISelectChoice choice = UISelectChoice.make(groupBranch, "groupCheck", selectID, i);
                UISelectLabel.make(groupBranch, "groupName", selectID, i).decorate(new UILabelTargetDecorator(choice) );
            }

            // Cancel button, select all button, select none button
            UICommand.make( form, "cancel-button", UIMessage.make( "general.cancel.button" ) );
            UICommand.make( form, "select-all", UIMessage.make( "modifytemplate.group.add.select.all" ) );
            UICommand.make( form, "deselect-all", UIMessage.make( "modifytemplate.group.add.select.none" ) );

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
