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

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * Simple class which should be extended by most of the evaluation view params
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class BaseViewParameters extends SimpleViewParameters {

    /**
     * if this is link is coming from an external location then set this to true,
     * when this is true things like breadcrumb links should be suppressed
     */
    public boolean external = false;

    /**
     * the referring page if it is available,
     * this will make it easy to pass this value if needed
     */
    public String referrer = null;

    /**
     * New report style toggle
     */
    public boolean useNewReportStyle = false;

}
