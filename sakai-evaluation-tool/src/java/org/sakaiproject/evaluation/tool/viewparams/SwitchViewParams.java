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
