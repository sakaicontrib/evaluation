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
package org.sakaiproject.evaluation.model;

import java.util.Date;

/**
 * This defines locks for various evaluation resources (primary this is used for locking the data preloads)
 * to allow for cluster operations
 */
public class EvalLock implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

	private Long id;

	private Date lastModified;

	/**
	 * The name of the lock
	 */
	private String name;

	/**
	 * The holder (owner) of this lock
	 */
	private String holder;

	// Constructors

	/** default constructor */
	public EvalLock() {
	}

	/** full constructor
	 * @param name
	 * @param holder */
	public EvalLock(String name, String holder) {
		this.lastModified = new Date();
		this.name = name;
		this.holder = holder;
	}

	// Property accessors
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getLastModified() {
		return this.lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
   
   public String getHolder() {
      return holder;
   }
   
   public void setHolder(String holder) {
      this.holder = holder;
   }


}
