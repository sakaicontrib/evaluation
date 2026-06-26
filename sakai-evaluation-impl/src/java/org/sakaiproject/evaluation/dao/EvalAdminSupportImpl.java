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

import java.util.Calendar;
import java.util.List;

import org.sakaiproject.evaluation.model.EvalAdmin;

import lombok.Setter;

public class EvalAdminSupportImpl implements EvalAdminSupport {

	@Setter
	private EvaluationDao dao;

	public List<EvalAdmin> getEvalAdmins() {
		return dao.getAllEvalAdmins();
	}

	public EvalAdmin getEvalAdmin(String userId) {
		if (userId == null) {
			return null;
		}
		return dao.getEvalAdminByUserId(userId);
	}
	
	public void assignEvalAdmin(String userId, String assignorUserId) {
		
		// check to make sure user is not already assigned as an eval admin
		if ((this.getEvalAdmin(userId)) != null)
			throw new IllegalArgumentException("User with userId (" + userId + ") is already assigned as an eval admin");
		
		EvalAdmin evalAdminObj = new EvalAdmin(userId, Calendar.getInstance().getTime(), assignorUserId);
		dao.save(evalAdminObj);
		
	}

	public void unassignEvalAdmin(String userId) {
		
		EvalAdmin evalAdminObj = this.getEvalAdmin(userId);
		
		// check that user is assigned as an eval admin
		if (evalAdminObj == null)
			throw new IllegalArgumentException("Cannot find eval admin with this userId: " + userId);
		
		dao.delete(evalAdminObj);
		
	}
	
	public boolean isUserEvalAdmin(String userId) {
		return (this.getEvalAdmin(userId) != null);
	}
	
}
