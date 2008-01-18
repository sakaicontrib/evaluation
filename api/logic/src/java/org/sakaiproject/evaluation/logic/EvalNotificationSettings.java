/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.evaluation.logic;

/**
 * Contains Spring-injected sakai.properties for overriding certain defaults in EvalSettings.
 * 
 * @author rwellis
 *
 */
public interface EvalNotificationSettings {
	
	/**
	 * Get the destination of email (send|log|none).
	 * 
	 * @return String send|log|none
	 */
	public String getDeliveryOption();
	
	/**
	 * Set the destination of email (send|log|none).
	 * 
	 * @param deliveryOption
	 */
	public void setDeliveryOption(String deliveryOption);
	
	/**
	 * Get whether the email address of those getting email is logged or not.
	 * 
	 * @return String true|false
	 */
	public String getLogRecipients();
	
	/**
	 * Set whether the email address of those getting email is logged or not.
	 * 
	 * @param logRecipients true|false
	 */
	public void setLogRecipients(String logRecipients);
	
	/**
	 * Get the time to wait in seconds between sending batch of emails.
	 * 
	 * @return String representation of integer value in seconds
	 */
	public String getWaitInterval();
	
	/**
	 * Set the time to wait in seconds between sending batch of emails.
	 * 
	 * @param waitInterval String representation of integer value in seconds
	 */
	public void setWaitInterval(String waitInterval);
	
	/**
	 * 
	 * @return
	 */
	public String getBatchSize();
	
	/**
	 * Set the number of emails to send before waiting.
	 * 
	 * @param batchSize
	 */
	public void setBatchSize(String batchSize);
}
