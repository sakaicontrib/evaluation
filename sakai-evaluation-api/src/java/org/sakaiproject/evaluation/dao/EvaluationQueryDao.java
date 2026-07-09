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

import java.util.Date;
import java.util.List;

import org.sakaiproject.evaluation.model.EvalEvaluation;

public interface EvaluationQueryDao {

    public int countEvaluationsByIds(Long[] evaluationIds);

    public int countEvaluationById(Long evaluationId);

    public EvalEvaluation getEvaluationByEid(String eid);

    public int countEvaluationsByTemplateId(Long templateId);

    public List<EvalEvaluation> getEvaluationsByTemplateId(Long templateId);

    public List<EvalEvaluation> getEvaluationsByTermId(String termId);

    public List<EvalEvaluation> getEvaluationsByState(String state);

    public List<EvalEvaluation> getEvaluationsNotViewableOrDeleted();

    public List<EvalEvaluation> getEvaluationsByCategory(String evalCategory);

    public List<EvalEvaluation> getEvalsUserCanTake(String userId, Boolean activeOnly, Boolean approvedOnly,
            Boolean includeAnonymous, int startResult, int maxResults);

    public List<EvalEvaluation> getEvaluationsByEvalGroups(EvaluationGroupQuery query, int startResult, int maxResults);

    public List<EvalEvaluation> getEvaluationsForOwnerAndGroups(String userId, String[] evalGroupIds,
            Date recentClosedDate, int startResult, int maxResults, boolean includePartial);

    public List<String> getEvalCategories(String userId);

    public String getNodeIdForEvalGroup(String evalGroupId);
}
