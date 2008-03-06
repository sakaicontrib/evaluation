/**
 * $Id$
 * $URL$
 * SwitchViewParams.java - evaluation - Feb 29, 2008 1:44:57 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.viewparams;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;


/**
 * ViewParams for handling simple switching parameter that carries a simple string
 * value to determine the current viewing state for a page
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class SwitchViewParams extends SimpleViewParameters {
   
   public String switcher;

   public SwitchViewParams() {}

   public SwitchViewParams(String viewID, String switcher) {
      super(viewID);
      this.switcher = switcher;
   }
   
}
