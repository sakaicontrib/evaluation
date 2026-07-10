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
package org.sakaiproject.evaluation.logic;

import org.sakaiproject.evaluation.dao.EvaluationLockDao;


/**
 * 
 *
 */
public class EvalLockManagerImpl implements EvalLockManager {

    private EvaluationLockDao lockDao;
    public void setLockDao(EvaluationLockDao lockDao) {
        this.lockDao = lockDao;
    }

	
	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalLockManager#obtainLock(java.lang.String, java.lang.String, long)
	 */
	public Boolean obtainLock(String lockId, String executerId, long timePeriod) {
		return lockDao.obtainLock(lockId, executerId, timePeriod);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalLockManager#releaseLock(java.lang.String, java.lang.String)
	 */
	public Boolean releaseLock(String lockId, String executerId) {
		return lockDao.releaseLock(lockId, executerId);
	}

}
