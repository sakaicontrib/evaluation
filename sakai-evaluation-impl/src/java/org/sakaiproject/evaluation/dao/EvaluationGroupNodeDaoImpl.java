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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.model.EvalGroupNodes;

public class EvaluationGroupNodeDaoImpl extends EvaluationDaoHibernateSupport implements EvaluationGroupNodeDao {

    public List<EvalGroupNodes> getEvalGroupNodesByNodeIds(String[] nodeIds) {
        if (nodeIds == null) {
            throw new IllegalArgumentException("nodeIds cannot be null");
        }
        if (nodeIds.length == 0) {
            return new ArrayList<>(0);
        }
        return currentSession().createQuery(
                "select groupNode from EvalGroupNodes groupNode where groupNode.nodeId in (:nodeIds) order by groupNode.id",
                EvalGroupNodes.class)
                .setParameterList("nodeIds", nodeIds)
                .list();
    }

    public void setEvalGroupsForNode(String nodeId, Set<String> evalGroupIds) {
        EvalGroupNodes evalGroupNodes = getEvalGroupNodesByNodeIds(new String[] {nodeId})
                .stream()
                .findFirst()
                .orElse(null);
        if (evalGroupIds == null || evalGroupIds.isEmpty()) {
            if (evalGroupNodes != null) {
                delete(evalGroupNodes);
            }
            return;
        }
        if (evalGroupNodes == null) {
            evalGroupNodes = new EvalGroupNodes(new java.util.Date(), nodeId);
        }
        evalGroupNodes.setEvalGroups(new ArrayList<>(evalGroupIds));
        save(evalGroupNodes);
    }
}
