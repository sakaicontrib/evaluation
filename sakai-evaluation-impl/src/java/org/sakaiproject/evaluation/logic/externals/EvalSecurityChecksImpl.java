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
package org.sakaiproject.evaluation.logic.externals;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.EvalUtils;


/**
 * Special class and bean for handling security checks within the logic layer,
 * this is just allowing us to share logic and should not be accessed outside the
 * logic layer
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalSecurityChecksImpl {

    private static final Log LOG = LogFactory.getLog(EvalSecurityChecksImpl.class);

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalBeanUtils evalBeanUtils;
    public void setEvalBeanUtils(EvalBeanUtils evalBeanUtils) {
        this.evalBeanUtils = evalBeanUtils;
    }

    /**
     * Check if a user can control (update/edit/write) an evaluation,
     * does not throw exception, does not check locked
     * @param userId internal user id
     * @param eval
     * @return true if they can, false otherwise
     */
    public boolean canUserControlEvaluation(String userId, EvalEvaluation eval) {
        // does not worry about locked
        return evalBeanUtils.checkUserPermission(userId, eval.getOwner());
    }

    /**
     * Check if user can remove this evaluation
     * @param userId internal user id
     * @param eval the evaluation, can be null
     * @return true if they can, false otherwise
     * @throws SecurityException if user not allowed,
     * {@link IllegalStateException} if this is locked
     */
    public boolean canUserRemoveEval(String userId, EvalEvaluation eval) {
        // if eval id is invalid then just log it
        boolean allowed = false;
        if (eval == null) {
            LOG.warn("Cannot find evaluation to delete");
        } else {
            // check locked first
            if (eval.getId() != null && eval.getLocked() == true) {
                // locked evals in the active/graceperiod state cannot be removed, all others can
                String evalState = EvalUtils.getEvaluationState(eval, false);
                if ( EvalConstants.EVALUATION_STATE_ACTIVE.equals(evalState) 
                        || EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(evalState) ) {
                    throw new IllegalStateException("Cannot remove evaluation ("+eval.getId()+") that has responses while it is active");
                } else {
                    // can remove any other evaluation since it effectively just deactivates it if there are responses
                    allowed = true;
                }
            }

            if (! canUserControlEvaluation(userId, eval) ) {
                throw new SecurityException("User ("+userId+") cannot control evaluation ("+eval.getId()+") without permissions");
            }
            allowed = true;
        }
        return allowed;
    }

    /**
     * Check if user has permissions to control (remove or update) this template
     * @param userId internal user id
     * @param template
     * @return true if they do, exception otherwise
     * @throws SecurityException if user not allowed,
     * {@link IllegalStateException} if this is locked
     */
    public boolean checkUserControlTemplate(String userId, EvalTemplate template) {
        LOG.debug("template: " + template.getTitle() + ", userId: " + userId);
        // check locked first
        if (template.getId() != null && template.getLocked() == true) {
            throw new IllegalStateException("Cannot control (modify) locked template ("+template.getId()+")");
        }

        if (! evalBeanUtils.checkUserPermission(userId, template.getOwner()) ) {
            throw new SecurityException("User ("+userId+") cannot control template ("+template.getId()+") without permissions");
        }
        return true;
    }

    /**
     * Check if user has permissions to control (remove or update) this scale
     * @param userId internal user id
     * @param scale
     * @return true if they do, exception otherwise
     * @throws SecurityException if user not allowed,
     * {@link IllegalStateException} if this is locked
     */
    public boolean checkUserControlScale(String userId, EvalScale scale) {
        // check locked first
        if (scale.getId() != null && scale.getLocked() == true) {
            throw new IllegalStateException("Cannot control locked scale ("+scale.getId()+")");
        }

        if (! evalBeanUtils.checkUserPermission(userId, scale.getOwner()) ) {
            throw new SecurityException("User ("+userId+") cannot control scale ("+scale.getId()+") without permissions");
        }
        return true;
    }

    /**
     * Check if a user can control (remove/update) an item
     * @param userId internal user id
     * @param item
     * @return true if they can, false otherwise
     * @throws SecurityException if user not allowed,
     * {@link IllegalStateException} if this is locked
     */
    public boolean checkUserControlItem(String userId, EvalItem item) {
        LOG.debug("item: " + item.getId() + ", userId: " + userId);
        // check locked first
        if (item.getId() != null && item.getLocked() == true) {
            throw new IllegalStateException("Cannot control (modify) locked item ("+item.getId()+")");
        }

        if (! evalBeanUtils.checkUserPermission(userId, item.getOwner()) ) {
            throw new SecurityException("User ("+userId+") cannot control item ("+item.getId()+") without permissions");
        }
        return true;
    }

    /**
     * Check if a user can control (remove/update) a template item
     * @param userId internal user id
     * @param templateItem
     * @return true if they can, false otherwise
     * @throws SecurityException if user not allowed,
     * {@link IllegalStateException} if this is locked
     */
    public boolean checkUserControlTemplateItem(String userId, EvalTemplateItem templateItem) {
        LOG.debug("templateItem: " + templateItem.getId() + ", userId: " + userId);
        // check locked first (expensive check)
        if (templateItem.getId() != null && templateItem.getTemplate().getLocked() == true) {
            throw new IllegalStateException("Cannot control (modify,remove) template item ("+
                    templateItem.getId()+") in locked template ("+templateItem.getTemplate().getTitle()+")");
        }

        if (! evalBeanUtils.checkUserPermission(userId, templateItem.getOwner()) ) {
            throw new SecurityException("User ("+userId+") cannot control templateItem ("+templateItem.getId()+") without permissions");
        }
        return true;
    }

    /**
     * Check if a user can control (remove/update) a template item
     * @param userId internal user id
     * @param itemGroup
     * @return true if they can, false otherwise
     * @throws SecurityException if user not allowed
     */
    public boolean checkUserControlItemGroup(String userId, EvalItemGroup itemGroup) {
        LOG.debug("itemGroup: " + itemGroup.getId() + ", userId: " + userId);

        if (! evalBeanUtils.checkUserPermission(userId, itemGroup.getOwner()) ) {
            throw new SecurityException("User ("+userId+") cannot control itemGroup ("+itemGroup.getId()+") without permissions");
        }
        return true;
    }

    /**
     * Check if user can control this AssignGroup/Hierarchy
     * @param userId internal user id
     * @param assignGroup can be an {@link EvalAssignGroup} or {@link EvalAssignHierarchy}
     * @return true if can, false otherwise
     * @throws SecurityException if user not allowed
     */
    public boolean checkControlAssignGroup(String userId, EvalAssignHierarchy assignGroup) {
        LOG.debug("userId: " + userId + ", assignGroup: " + assignGroup.getId());

        if (! evalBeanUtils.checkUserPermission(userId, assignGroup.getOwner()) ) {
            throw new SecurityException("User ("+userId+") cannot control assignGroup ("+assignGroup.getId()+") without permissions");
        }
        return true;
    }

    /**
     * Check if the user can create an AC in an eval
     * @param userId internal user id
     * @param eval
     * @return true if they can, throw exceptions otherwise
     * @throws IllegalArgumentException if the eval is null
     * @throws SecurityException if user not allowed
     * @throws IllegalStateException if this is locked or in a state where no assigns can happen
     */
    public boolean checkCreateAssignments(String userId, EvalEvaluation eval) {
        if (eval == null) {
            throw new IllegalArgumentException("eval must be set to check permission");
        }
        if (userId == null) {
            userId = commonLogic.getCurrentUserId();
        }
        // check state to see if assign groups can be added
        String state = EvalUtils.getEvaluationState(eval, false);
        if ( EvalUtils.checkStateBefore(state, EvalConstants.EVALUATION_STATE_ACTIVE, true) ) {
            // check eval user permissions (just owner and super at this point)
            if (! canUserControlEvaluation(userId, eval)) {
                throw new SecurityException("User ("+userId+") cannot create assignments in evaluation ("+eval.getId()+"), do not have permission");
            }
        } else {
            throw new IllegalStateException("User ("+userId+") cannot create assignments in evaluation ("+eval.getId()+"), invalid eval state: " + state);
        }
        return true;
    }

    /**
     * Check if user can remove an assignment which is assigned to an evaluation,
     * can specifically check for assign groups if desired
     * 
     * @param userId  (OPTIONAL) uses the current user if null, internal user id
     * @param assignGroup (OPTIONAL) checks the general permissions if not set
     * @param eval
     * @return true if they can, throw exceptions otherwise
     * @throws IllegalArgumentException if the eval is null OR the eval and assignment do not link
     * @throws SecurityException if user not allowed
     * @throws IllegalStateException if this is locked or in a state where no assigns can be removed
     */
    public boolean checkRemoveAssignments(String userId, EvalAssignHierarchy assignGroup, EvalEvaluation eval) {
        if (eval == null) {
            throw new IllegalArgumentException("eval must be set to check permission");
        }
        if (userId == null) {
            userId = commonLogic.getCurrentUserId();
        }
        if (assignGroup != null) {
            // check that eval and EAG line up
            if (! assignGroup.getEvaluation().getId().equals(eval.getId())) {
                throw new IllegalArgumentException("evaluation ("+eval.getId()+") and assigngroup ("+assignGroup.getId()+") are not linked, " +
                "the eval must be the one that is linked to the assigngroup");
            }
        }

        String state = EvalUtils.getEvaluationState(eval, false);
        if (EvalUtils.checkStateBefore(state, EvalConstants.EVALUATION_STATE_INQUEUE, true)) {
            if (assignGroup == null) {
                if (! canUserControlEvaluation(userId, eval) ) {
                    throw new SecurityException("User ("+userId+") cannot control evaluation and therefore cannot remove assignments either");
                }
            } else {
                checkControlAssignGroup(userId, assignGroup);
            }
        } else {
            throw new IllegalStateException("User ("+userId+") cannot remove this assign evalGroupId ("+assignGroup.getId()+"), invalid eval state: " + state);
        }
        return true;
    }

    /**
     * Check if a user can modify a response to an evaluation
     * @param userId internal user id
     * @param response
     * @param eval
     * @return true if they can, throw exceptions otherwise
     * @throws SecurityException if user not allowed,
     * {@link IllegalStateException} if this is locked or in a state where no assigns can be removed,
     * {@link IllegalArgumentException} if the eval and response do not link
     */
    public boolean checkUserModifyResponse(String userId, EvalResponse response, EvalEvaluation eval) {
        LOG.debug("evalGroupId: " + response.getEvalGroupId() + ", userId: " + userId);

        // check that eval and EAG line up
        if (! response.getEvaluation().getId().equals(eval.getId())) {
            throw new IllegalArgumentException("evaluation ("+eval.getId()+") and response ("+response.getId()+") are not linked, " +
            "the eval must be the one that is linked to the response");
        }

        boolean allowed = false;
        String state = EvalUtils.getEvaluationState(eval, false);
        if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(state) || EvalConstants.EVALUATION_STATE_ACTIVE.equals(state)) {
            // admin CAN save responses -AZ
            //       // check admin (admins can never save responses)
            //       if (external.isUserAdmin(userId)) {
            //       throw new IllegalArgumentException("Admin user (" + userId + ") cannot create response ("
            //       + response.getId() + "), admins can never save responses");
            //       }

            // check ownership
            if (response.getOwner().equals(userId)) {
                allowed = true;
            } else {
                throw new SecurityException("User (" + userId + ") cannot modify response (" + response.getId() + ") without permissions");
            }
        } else {
            throw new IllegalStateException("Evaluation state (" + state + ") not valid for modifying responses");
        }
        return allowed;
    }

    /**
     * Checks if a user can control (update/remove) this email template,
     * this only checks permissions, you should check the state of the evaluation as well or
     * use {@link #checkEvalTemplateControl(String, EvalEvaluation, EvalEmailTemplate)}
     * @param userId internal user id
     * @param emailTemplate
     * @return true if can control, false otherwise
     */
    public boolean canUserControlEmailTemplate(String userId, EvalEmailTemplate emailTemplate) {
        return evalBeanUtils.checkUserPermission(userId, emailTemplate.getOwner());
    }

    /**
     * Check if user can control evaluation and template combo
     * 
     * @param userId internal user id
     * @param eval can be null if only checking the template
     * @param emailTemplate if this is null or the default template then the eval permissions determine control
     * @return true if they can, throw exceptions otherwise
     */
    public boolean checkEvalTemplateControl(String userId, EvalEvaluation eval, EvalEmailTemplate emailTemplate) {

        boolean allowed = false;
        // special check for admin control over default templates
        if (emailTemplate != null  
                && emailTemplate.getDefaultType() != null
                && commonLogic.isUserAdmin(userId) ) {
            allowed = true;         
        } else {
            if (emailTemplate == null || 
                    emailTemplate.getDefaultType() != null) {
                // no template
                if (eval != null) {
                    // currently using the default templates so check eval perms
                    if (canUserControlEvaluation(userId, eval)) {
                        allowed = true;
                    } else {
                        throw new SecurityException("User (" + userId
                                + ") cannot control email template in evaluation (" + eval.getId()
                                + "), do not have permission");
                    }
                } else {
                    // this is an odd test of null eval and template which means there is nothing to really check, assume failure
                    throw new IllegalArgumentException("Cannot test permissions with null eval and null emailTemplate, at least one must be set");
                }
            } else {
                // check email template perms
                if (canUserControlEmailTemplate(userId, emailTemplate)) {
                    allowed = true;
                } else {
                    throw new SecurityException("User (" + userId + ") cannot control email template ("
                            + emailTemplate.getId() + ") without permissions");
                }
            }
        }
        return allowed;
    }

    public boolean checkUserControlAdhocUser(String userId, EvalAdhocUser user) {
        if (! evalBeanUtils.checkUserPermission(userId, user.getOwner()) ) {
            throw new SecurityException("User ("+userId+") cannot control adhoc user ("+user.getId()+") without permissions");
        }
        return true;
    }

    public boolean checkUserControlAdhocGroup(String userId, EvalAdhocGroup group) {
        if (! evalBeanUtils.checkUserPermission(userId, group.getOwner()) ) {
            throw new SecurityException("User ("+userId+") cannot control adhoc group ("+group.getId()+") without permissions");
        }
        return true;
    }

}
