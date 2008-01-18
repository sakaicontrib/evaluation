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

package org.sakaiproject.evaluation.logic.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalNotificationSettings;

public class EvalNotificationSettingsImpl implements EvalNotificationSettings {
	
	private static Log log = LogFactory.getLog(EvalNotificationSettingsImpl.class);
	
	private String deliveryOption;
	
	private String logRecipients;
	
	private String waitInterval;
	
	private String batchSize;
	
	// INIT method
	public void init() {
		if(log.isDebugEnabled()) log.debug("Init");
	}
	
	public void setDeliveryOption(String deliveryOption) {
		this.deliveryOption = deliveryOption;
	}
	
	public void setLogRecipients(String logRecipients) {
		this.logRecipients = logRecipients;
	}
	
	public void setWaitInterval(String waitInterval) {
		this.waitInterval = waitInterval;
	}
	
	public void setBatchSize(String batchSize) {
		this.batchSize = batchSize;
	}

	public String getBatchSize() {
		return batchSize;
	}

	public String getDeliveryOption() {
		return deliveryOption;
	}

	public String getLogRecipients() {
		return logRecipients;
	}

	public String getWaitInterval() {
		return waitInterval;
	}
}
