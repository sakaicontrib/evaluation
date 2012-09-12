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
package org.sakaiproject.evaluation.logic.tqm;

import java.util.Date;

/**
 * 
 *
 */
public class EmailRecipient {
	
	protected long id;
	protected String sakaiUserId;
	protected Date earliestDueDate;
	protected int evalCount;
	protected long emailTemplateId;
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	/**
	 * @return the sakaiUserId
	 */
	public String getSakaiUserId() {
		return sakaiUserId;
	}
	/**
	 * @return the earliestDueDate
	 */
	public Date getEarliestDueDate() {
		return earliestDueDate;
	}
	/**
	 * @return the evalCount
	 */
	public int getEvalCount() {
		return evalCount;
	}
	/**
	 * @return the emailTemplateId
	 */
	public long getEmailTemplateId() {
		return emailTemplateId;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	/**
	 * @param sakaiUserId the sakaiUserId to set
	 */
	public void setSakaiUserId(String sakaiUserId) {
		this.sakaiUserId = sakaiUserId;
	}
	/**
	 * @param earliestDueDate the earliestDueDate to set
	 */
	public void setEarliestDueDate(Date earliestDueDate) {
		this.earliestDueDate = earliestDueDate;
	}
	/**
	 * @param evalCount the evalCount to set
	 */
	public void setEvalCount(int evalCount) {
		this.evalCount = evalCount;
	}
	/**
	 * @param emailTemplateId the emailTemplateId to set
	 */
	public void setEmailTemplateId(long emailTemplateId) {
		this.emailTemplateId = emailTemplateId;
	}

	
}
