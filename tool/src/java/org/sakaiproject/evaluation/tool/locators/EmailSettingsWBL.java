/**
 * 
 */
package org.sakaiproject.evaluation.tool.locators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalSettings;

/**
 * 
 *
 */
public class EmailSettingsWBL extends SettingsWBL {
	
	// default delay before scheduling a job after a change in the 
	protected static final long DEFAULT_DELAY = 5L * 1000L;

	private Log logger = LogFactory.getLog(EmailSettingsWBL.class);
	
	protected Object lock = new Object();
	protected boolean updateNeeded = false;
	
    public void set(String beanname, Object toset) {
    	super.set(beanname, toset);
    	if(EvalSettings.SINGLE_EMAIL_REMINDER_DAYS.equals(beanname) || EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_TIME.equals(beanname)) {
        	logger.info("set(" + beanname + "," + toset + ") ");
    		synchronized(lock) {
    			updateNeeded = true;
    		}
    	} 
    }
    
    public void saveSettings() {
    	logger.info("saveSettings() -- Saving email settings ");
    	synchronized (lock) {
    		if(this.updateNeeded) {
    			this.scheduleJob();
    			this.updateNeeded = false;
    		}
    	}

    }

	protected void scheduleJob() {
		logger.info("scheduleJob() -- Scheduling email job ");
		
		
	}

}
