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
 * Allows for passing of time intervals for limiting the display of
 * closed evaluations.
 * 
 * @author Paul Dagnall (dagnalpb@notes.udayton.edu)
 */
public class EvalListParameters extends SimpleViewParameters {

   public int maxAgeToDisplay; // Max age (in months) to display closed evals
   
   public EvalListParameters() { }

   public EvalListParameters(String viewID, int interval) {
      this.viewID = viewID;
      this.maxAgeToDisplay = interval;
   }
   
   

}
