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
package org.sakaiproject.evaluation.tool.viewparams;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is meant to serve as a base for ViewParameters of different download
 * types that may require their own custom parameters. Ex. CSV, Excel, PDF etc.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Steven Githens
 */
public class DownloadReportViewParams extends BaseViewParameters {

	private static final Log LOG = LogFactory.getLog( DownloadReportViewParams.class) ;
	public Long templateId; 
	public Long evalId;
	public String filename;
	public String evaluateeId;

	// See the comment in EssayResponseParams.java
	public String[] groupIds;

	public DownloadReportViewParams() {}

	public DownloadReportViewParams(String viewID, Long templateId, Long evalId, String[] groupIds, String filename, boolean useNewReportStyle) {
		this.viewID = viewID;
		this.templateId = templateId;
		this.evalId = evalId;
		this.groupIds = groupIds;
		this.filename = filename;
		this.useNewReportStyle = useNewReportStyle;
	}
	
	public DownloadReportViewParams(String viewID, Long templateId, Long evalId, String[] groupIds, String filename, String evaluateeId, boolean useNewReportStyle) {
		if( LOG.isDebugEnabled() )
		{
			LOG.debug( "DownloadReportViewParams called with " + evaluateeId );
		}

		this.viewID = viewID;
		this.templateId = templateId;
		this.evalId = evalId;
		this.groupIds = groupIds;
		this.filename = filename;
		this.evaluateeId = evaluateeId;
		this.useNewReportStyle = useNewReportStyle;
	}

	public String getParseSpec() {
		// include a comma delimited list of the public properties in this class
		return super.getParseSpec() + ",templateId,evalId,groupIds,filename,evaluateeId";
	}
}
