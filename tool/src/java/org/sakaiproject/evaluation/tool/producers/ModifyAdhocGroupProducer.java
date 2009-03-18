
package org.sakaiproject.evaluation.tool.producers;

import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.tool.locators.AdhocGroupsBean;
import org.sakaiproject.evaluation.tool.viewparams.AdhocGroupParams;

import uk.org.ponder.rsf.components.ParameterList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This view is for creating or modifying adhoc groups. If the Group ID in the incoming ViewParams
 * is null, we assume we are creating a new adhoc group. We have an optional returnURL param on the
 * AdhocGroupParams which we are currently using to go back to the Evaluation Assign Groups Page.
 * 
 * @author sgithens
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ModifyAdhocGroupProducer implements ViewComponentProducer, ViewParamsReporter,
ActionResultInterceptor {

    public static final String VIEW_ID = "modify_adhoc_group";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic bean) {
        this.commonLogic = bean;
    }

    private AdhocGroupsBean adhocGroupsBean;
    public void setAdhocGroupsBean(AdhocGroupsBean adhocGroupsBean) {
        this.adhocGroupsBean = adhocGroupsBean;
    }

    public void fillComponents(UIContainer tofill, ViewParameters viewparams,
            ComponentChecker checker) {
        AdhocGroupParams params = (AdhocGroupParams) viewparams;
        String curUserId = commonLogic.getCurrentUserId();

        boolean newGroup = false;
        EvalAdhocGroup evalAdhocGroup = null;
        if (params.adhocGroupId == null) {
            newGroup = true;
        } else {
            evalAdhocGroup = commonLogic.getAdhocGroupById(params.adhocGroupId);
            if (!curUserId.equals(evalAdhocGroup.getOwner())) {
                throw new SecurityException("Only owners can modify adhocgroups: " + curUserId + " , "
                        + evalAdhocGroup.getId());
            }
        }

        String adhocGroupTitle = "";
        if (!newGroup) {
            adhocGroupTitle = evalAdhocGroup.getTitle();
        }

        // Page Title
        if (newGroup) {
            UIMessage.make(tofill, "page-title", "modifyadhocgroup.page.title.new");
        } else {
            UIMessage.make(tofill, "page-title", "modifyadhocgroup.page.title.existing",
                    new String[] { evalAdhocGroup.getTitle() });
        }

        UIForm form = UIForm.make(tofill, "adhoc-group-form");
        UIInput.make(form, "group-name-input", "adhocGroupsBean.adhocGroupTitle", adhocGroupTitle);

        /*
         * If this is not a new group, then we render a table containing all the current group
         * members, along with a button on each row to remove that particular member.
         */
        if (!newGroup) {
            String[] participants = evalAdhocGroup.getParticipantIds();
            if (participants.length > 0) {
                List<EvalUser> evalUsers = commonLogic.getEvalUsersByIds(participants);
                UIOutput.make(form, "existing-members");
                for (EvalUser evalUser : evalUsers) {
                    UIBranchContainer row = UIBranchContainer.make(form, "member-row:");
                    if (EvalConstants.USER_TYPE_INTERNAL.equals(evalUser.type)) {
                        UIMessage.make(row, "user-id", "modifyadhocgroup.adhocuser.label");
                    } else {
                        UIOutput.make(row, "user-id", evalUser.username);
                    }
                    UIOutput.make(row, "user-display", evalUser.displayName);
                    // Remove Button
                    UICommand removeButton = UICommand.make(row, "remove-member",
                    "adhocGroupMemberRemovalBean.removeUser");
                    removeButton.parameters = new ParameterList();
                    removeButton.parameters.add(new UIELBinding(
                            "adhocGroupMemberRemovalBean.adhocGroupId", evalAdhocGroup.getId()));
                    removeButton.parameters.add(new UIELBinding(
                            "adhocGroupMemberRemovalBean.adhocUserId", evalUser.userId));
                }
            } else {
                UIMessage.make(form, "no-members-message", "modifyadhocgroup.message.emptygroup");
            }
        }

        // Place to add more users via email address
        UIInput.make(form, "add-members-input", "adhocGroupsBean.newAdhocGroupUsers");

        /*
         * There are two different methods depending on whether this is a new group or not. If it's
         * not, we also attach the Adhoc Group ID to the button.
         */
        if (newGroup) {
            UICommand.make(form, "save-button", UIMessage.make("modifyadhocgroup.newsave"),
            "adhocGroupsBean.addNewAdHocGroup");
        } else {
            UICommand saveButton = UICommand.make(form, "save-button", UIMessage
                    .make("modifyadhocgroup.update"), "adhocGroupsBean.addUsersToAdHocGroup");
            saveButton.parameters = new ParameterList(new UIELBinding("adhocGroupsBean.adhocGroupId",
                    evalAdhocGroup.getId()));
        }

        // Handler return URL to go back to the Evaluation Wizard if specified. In the future
        // we'll probably want to generalize this system.
        if (params.returnURL != null) {
            UILink.make(tofill, "return-link", UIMessage.make("modifyadhocgroup.backtoevalassign"),
                    params.returnURL);
        }
    }


    public ViewParameters getViewParameters() {
        return new AdhocGroupParams();
    }

    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        /*
         * If we just created a new adhoc group, then we want to redirect to the same page, but for
         * that new Adhoc Group ID.
         */
        if (AdhocGroupsBean.SAVED_NEW_ADHOCGROUP.equals(actionReturn)
                && incoming instanceof AdhocGroupParams) {
            AdhocGroupParams params = (AdhocGroupParams) incoming.copyBase();
            params.adhocGroupId = adhocGroupsBean.getAdhocGroupId();
            result.resultingView = params;
        }

    }

}
