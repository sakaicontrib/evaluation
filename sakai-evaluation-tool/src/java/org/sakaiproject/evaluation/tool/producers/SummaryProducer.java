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

import java.util.ArrayList;
import java.util.List;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.tool.renderers.AdminBoxRenderer;
import org.sakaiproject.evaluation.tool.renderers.BeEvaluatedBoxRenderer;
import org.sakaiproject.evaluation.tool.renderers.EvaluateBoxRenderer;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * The summary producer rewrite This creates a summary page for any user of the
 * evaluation system and is the starting page for anyone entering the system
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class SummaryProducer extends EvalCommonProducer implements DefaultView, NavigationCaseReporter {

    private final int maxGroupsToDisplay = 5;

    public static final String VIEW_ID = "summary";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
        this.navBarRenderer = navBarRenderer;
    }

    private AdminBoxRenderer adminBoxRenderer;
    public void setAdminBoxRenderer(AdminBoxRenderer adminBoxRenderer) {
        this.adminBoxRenderer = adminBoxRenderer;
    }

    private BeEvaluatedBoxRenderer beEvaluatedBoxRenderer;
    public void setBeEvaluatedBoxRenderer(BeEvaluatedBoxRenderer beEvaluatedBoxRenderer) {
        this.beEvaluatedBoxRenderer = beEvaluatedBoxRenderer;
    }

    private EvaluateBoxRenderer evaluateBoxRenderer;
    public void setEvaluateBoxRenderer(EvaluateBoxRenderer evaluateBoxRenderer) {
        this.evaluateBoxRenderer = evaluateBoxRenderer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder
     * .rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters,
     * uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        //boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
        boolean createTemplate = authoringService.canCreateTemplate(currentUserId);
        boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);

        navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

        if (beginEvaluation) {
            // show instructor instructions
            UIMessage.make(tofill, "instructor-instructions", "summary.instructor.instruction");
        }

        /*
         * Notification box listing box
         */
        boolean userHasNotifications = false;
        if (userHasNotifications) {
            UIBranchContainer notificationsBC = UIBranchContainer.make(tofill, "notificationsBox:");
            UIMessage.make(notificationsBC, "notifications-title", "summary.notifications.title");
            UIMessage.make(notificationsBC, "notifications-higher-level", "summary.eval.assigned.from.above");
            // add other stuff
        }

        //
        // for the evaluations taking box
        // http://jira.sakaiproject.org/jira/browse/EVALSYS-618
        // Changed this to reduce the load on the services and make this load faster
        evaluateBoxRenderer.renderBox(tofill, currentUserId);

        // show evaluations that the user is being evaluated in
        boolean showEvaluateeBox = ((Boolean) settings.get(EvalSettings.ENABLE_EVALUATEE_BOX));
        if(showEvaluateeBox) {
            beEvaluatedBoxRenderer.renderBox(tofill, currentUserId);
        }

        // show evaluations that user is administering
        Boolean showAdministratingBox = (Boolean) settings.get(EvalSettings.ENABLE_ADMINISTRATING_BOX);
        if(showAdministratingBox != null && showAdministratingBox == true) {
            adminBoxRenderer.renderItem(tofill, "evalAdminBox");
        } //showAdministratingBox true

        /*
         * Site/Group listing box
         */
        Boolean enableSitesBox = (Boolean) settings.get(EvalSettings.ENABLE_SUMMARY_SITES_BOX);
        if (enableSitesBox) {
            // only show this if we cannot find our location OR if the option is forced to on
            String NO_ITEMS = "no.list.items";

            UIBranchContainer contextsBC = UIBranchContainer.make(tofill, "siteListingBox:");
            UIMessage.make(contextsBC, "sitelisting-title", "summary.sitelisting.title");

            UIMessage.make(contextsBC, "sitelisting-evaluated-text", "summary.sitelisting.evaluated");
            // NOTE: OK usage of perms here
            List<EvalGroup> evaluatedGroups = commonLogic.getEvalGroupsForUser(currentUserId, EvalConstants.PERM_BE_EVALUATED);
            if (evaluatedGroups.size() > 0) {
                for (int i = 0; i < evaluatedGroups.size(); i++) {
                    if (i > maxGroupsToDisplay) {
                        UIMessage.make(contextsBC, "evaluatedListNone", "summary.sitelisting.maxshown", new Object[] { evaluatedGroups.size()
                                                                                                                       - maxGroupsToDisplay});
                        break;
                    }
                    UIBranchContainer evaluatedBC = UIBranchContainer.make(contextsBC, "evaluatedList:", i + "");
                    EvalGroup c = (EvalGroup) evaluatedGroups.get(i);
                    UIOutput.make(evaluatedBC, "evaluatedListTitle", c.title);
                }
            } else {
                UIMessage.make(contextsBC, "evaluatedListNone", NO_ITEMS);
            }

            UIMessage.make(contextsBC, "sitelisting-evaluate-text", "summary.sitelisting.evaluate");
            // NOTE: OK usage of perms here
            List<EvalGroup> evaluateGroups = commonLogic.getEvalGroupsForUser(currentUserId, EvalConstants.PERM_TAKE_EVALUATION);
            if (evaluateGroups.size() > 0) {
                for (int i = 0; i < evaluateGroups.size(); i++) {
                    if (i > maxGroupsToDisplay) {
                        UIMessage.make(contextsBC, "evaluateListNone", "summary.sitelisting.maxshown", new Object[] { evaluateGroups.size() - maxGroupsToDisplay});
                        break;
                    }
                    UIBranchContainer evaluateBC = UIBranchContainer.make(contextsBC, "evaluateList:", i + "");
                    EvalGroup c = (EvalGroup) evaluateGroups.get(i);
                    UIOutput.make(evaluateBC, "evaluateListTitle", c.title);
                }
            } else {
                UIMessage.make(contextsBC, "evaluateListNone", NO_ITEMS);
            }
        }

        /*
         * For the Evaluation tools box
         */
        if (createTemplate || beginEvaluation) {
            UIBranchContainer toolsBC = UIBranchContainer.make(tofill, "toolsBox:");
            UIMessage.make(toolsBC, "tools-title", "summary.tools.title");

            if (createTemplate) {
                UIInternalLink.make(toolsBC, "createTemplateLink", UIMessage.make("createtemplate.page.title"), new TemplateViewParameters(
                        ModifyTemplateProducer.VIEW_ID, null));
            }

            if (beginEvaluation) {
                UIInternalLink.make(toolsBC, "beginEvaluationLink", UIMessage.make("starteval.page.title"), new EvalViewParameters(
                        EvaluationCreateProducer.VIEW_ID, null));
            }
        }

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List reportNavigationCases() {
        List i = new ArrayList();
        i.add(new NavigationCase(EvaluationSettingsProducer.VIEW_ID, new SimpleViewParameters(EvaluationSettingsProducer.VIEW_ID)));
        i.add(new NavigationCase(PreviewEvalProducer.VIEW_ID, new SimpleViewParameters(PreviewEvalProducer.VIEW_ID)));
        return i;
    }

}
