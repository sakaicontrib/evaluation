/******************************************************************************
 * EvalViewParameters.java - created by aaronz on 31 May 2007
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *****************************************************************************/

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

}
