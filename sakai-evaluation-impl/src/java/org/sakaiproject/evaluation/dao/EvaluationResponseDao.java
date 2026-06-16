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

import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalResponse;

public interface EvaluationResponseDao {

    public List<EvalResponse> getEvaluationResponsesForUserAndGroup(Long evaluationId, String userId, String evalGroupId);

    public List<EvalResponse> getEvaluationResponsesForUser(Long[] evaluationIds, String ownerUserId, Boolean completed);

    public int countResponses(Long evaluationId, String evalGroupId, Boolean completed);

    public List<EvalResponse> getEvaluationResponses(Long evaluationId, String[] evalGroupIds, Boolean completed);

    public List<EvalResponse> getEvaluationResponses(Long[] evaluationIds, String ownerUserId, String[] evalGroupIds, Boolean completed);

    public int countEvaluationResponses(Long[] evaluationIds, String ownerUserId, String[] evalGroupIds, Boolean completed);

    public void saveResponseAndAnswers(EvalResponse response, Set<EvalAnswer> answers);

    public List<EvalAnswer> getAnswers(Long evalId, String[] evalGroupIds, Long[] templateItemIds);

    public List<Long> getResponseIds(Long evalId, String[] evalGroupIds, String[] userIds, Boolean completed);

    public void removeResponses(Long[] responseIds);

    public Set<String> getResponseUserIds(Long evaluationId, String[] evalGroupIds, Boolean completed);

    public List<EvalResponse> getResponsesSavedInProgress(boolean activeEvaluationsOnly);
}
