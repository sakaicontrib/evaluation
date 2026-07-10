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
package org.sakaiproject.evaluation.dao;

import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.model.EvalAdmin;
import org.sakaiproject.evaluation.model.EvalHierarchyRule;

public interface EvaluationAdminSupportDao {

    public List<EvalAdmin> getAllEvalAdmins();

    public EvalAdmin getEvalAdminByUserId(String userId);

    public List<EvalHierarchyRule> getAllHierarchyRules();

    public EvalHierarchyRule getHierarchyRuleById(Long ruleId);

    public List<EvalHierarchyRule> getHierarchyRulesByNodeId(Long nodeId);

    public void deleteHierarchyRules(Set<EvalHierarchyRule> rules);

    public EvalAdhocUser getAdhocUserByUsername(String username);

    public EvalAdhocUser getAdhocUserByEmail(String email);

    public List<EvalAdhocUser> getAllAdhocUsers();

    public List<EvalAdhocUser> getAdhocUsersByIds(Long[] ids);

    public List<EvalAdhocGroup> getAdhocGroupsForOwner(String userId);

    public List<EvalAdhocGroup> getEvalAdhocGroupsByUserAndPerm(String userId, String permissionConstant);

    public boolean isUserAllowedInAdhocGroup(String userId, String permissionConstant, String evalGroupId);
}
