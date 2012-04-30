/**
 * Copyright 2006 Sakai Foundation Licensed under the
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
package org.sakaiproject.evaluation.model;

import java.util.Date;

public class EvalAdmin {
	
	private Long id;
	private String userId;
	private Date assignDate;
	private String assignorUserId;
	
	/**
	 * Default constructor
	 */
	public EvalAdmin() {}
	
	/**
	 * Full constructor 
	 * 
	 * @param userId the user id of the eval admin
	 * @param assignDate the date the eval admin is to be assigned
	 * @param assignorUserId the user id of the admin assigning the eval admin
	 */
	public EvalAdmin(String userId, Date assignDate, String assignorUserId) {
		this.userId = userId;
		this.assignDate = assignDate;
		this.assignorUserId = assignorUserId;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public Date getAssignDate() {
		return assignDate;
	}
	
	public void setAssignDate(Date assignDate) {
		this.assignDate = assignDate;
	}
	
	public String getAssignorUserId() {
		return assignorUserId;
	}
	
	public void setAssignorUserId(String assignorUserId) {
		this.assignorUserId = assignorUserId;
	}
	
}
