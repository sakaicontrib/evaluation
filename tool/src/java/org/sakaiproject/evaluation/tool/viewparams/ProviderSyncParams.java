/**
 * 
 */
package org.sakaiproject.evaluation.tool.viewparams;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * 
 * 
 */
public class ProviderSyncParams extends SimpleViewParameters {
	public String triggerName;
	//public String jobName;
	public Integer tab;
	
	public ProviderSyncParams(){}
	
	public ProviderSyncParams(String viewid, String triggerName) {
		this.viewID = viewid;
		this.triggerName = triggerName;
	}

}
