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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;

public interface EvaluationAssignmentDao {

    public EvalAssignUser getAssignUserByEid(String eid);

    public int countEvaluationGroups(Long evaluationId, boolean includeUnApproved);

    public EvalAssignGroup getAssignGroupByEid(String eid);

    public int countParticipantsForEval(Long evaluationId, String[] evalGroupIds);

    public List<EvalAssignGroup> getApprovedAssignGroupsForEvaluation(Long evaluationId, String evalGroupId);

    public int countApprovedAssignGroupsForEvaluation(Long evaluationId, String[] evalGroupIds);

    public EvalAssignGroup getAssignGroupByEvalAndGroupId(Long evaluationId, String evalGroupId);

    public List<EvalAssignHierarchy> getAssignHierarchyByEval(Long evaluationId);

    public List<EvalAssignGroup> getAssignGroupsForEvals(Long[] evaluationIds, boolean includeUnApproved, Boolean includeHierarchyGroups);

    public int countAssignGroupsByEvalAndGroupId(Long evaluationId, String evalGroupId);

    public void deleteAssignmentsForEvaluation(Long evaluationId);

    public void saveAssignHierarchyAndGroups(Set<EvalAssignHierarchy> assignHierarchies, Set<EvalAssignGroup> assignGroups);

    public List<EvalAssignHierarchy> getAssignHierarchiesByIds(Long[] assignHierarchyIds);

    public List<EvalAssignGroup> getAssignGroupsByEvalAndNodeIds(Long evaluationId, Set<String> nodeIds);

    public void deleteAssignHierarchyAndGroups(Set<EvalAssignHierarchy> assignHierarchies, Set<EvalAssignGroup> assignGroups);

    public void saveAssignUsers(Collection<EvalAssignUser> assignUsers);

    public void deleteAssignUsersByIds(Long[] assignUserIds);

    public List<EvalEvaluation> getEvalsWithoutUserAssignments();

    public List<EvalAssignUser> getParticipantsForEval(Long evaluationId, String userId, String[] evalGroupIds,
            String assignTypeConstant, String assignStatusConstant, String includeConstant, String evalStateConstant);

    public Set<String> getViewableEvalGroupIds(Long evaluationId, String permissionConstant, String[] evalGroupIds);

    public EvalAssignGroup getAssignGroupById(Long assignGroupId);

    public EvalAssignUser getAssignUserById(Long assignUserId);

    public void saveAssignGroup(EvalAssignGroup assignGroup);

    public int deleteAssignGroupAndLinkedUsers(EvalAssignGroup assignGroup, String excludedUserStatus);
}
