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

import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;

public interface EvaluationLockDao {

    public boolean lockScale(EvalScale scale, Boolean lockState);

    public boolean lockItem(EvalItem item, Boolean lockState);

    public boolean lockTemplate(EvalTemplate template, Boolean lockState);

    public boolean lockEvaluation(EvalEvaluation evaluation, Boolean lockState);

    public boolean isUsedScale(Long scaleId);

    public boolean isUsedItem(Long itemId);

    public boolean isUsedTemplate(Long templateId);

    public Boolean obtainLock(String lockId, String executerId, long timePeriod);

    public Boolean releaseLock(String lockId, String executerId);
}
