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
 * @author jimeng
 *
 */
public class UserAssignment {
	protected long id;
	protected long evaluationInfoId;
	protected long emailRecipientId;
	protected String sakaiUserId;
	protected String groupId;
	protected Date dueDate;
	protected long emailTemplateId;
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	/**
	 * @return the evaluationInfoId
	 */
	public long getEvaluationInfoId() {
		return evaluationInfoId;
	}
	/**
	 * @return the emailRecipientId
	 */
	public long getEmailRecipientId() {
		return emailRecipientId;
	}
	/**
	 * @return the sakaiUserId
	 */
	public String getSakaiUserId() {
		return sakaiUserId;
	}
	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}
	/**
	 * @return the dueDate
	 */
	public Date getDueDate() {
		return dueDate;
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
	 * @param evaluationInfoId the evaluationInfoId to set
	 */
	public void setEvaluationInfoId(long evaluationInfoId) {
		this.evaluationInfoId = evaluationInfoId;
	}
	/**
	 * @param emailRecipientId the emailRecipientId to set
	 */
	public void setEmailRecipientId(long emailRecipientId) {
		this.emailRecipientId = emailRecipientId;
	}
	/**
	 * @param sakaiUserId the sakaiUserId to set
	 */
	public void setSakaiUserId(String sakaiUserId) {
		this.sakaiUserId = sakaiUserId;
	}
	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	/**
	 * @param dueDate the dueDate to set
	 */
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	/**
	 * @param emailTemplateId the emailTemplateId to set
	 */
	public void setEmailTemplateId(long emailTemplateId) {
		this.emailTemplateId = emailTemplateId;
	}
	
	
}
