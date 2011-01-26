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
	public String fullJobName;
	//public String jobName;
	public Integer tab;
	
	public ProviderSyncParams(){}
	
	public ProviderSyncParams(String viewid, String fullJobName) {
		this.viewID = viewid;
		this.fullJobName = fullJobName;
	}

}
