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

import org.sakaiproject.evaluation.constant.EvalConstants;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * This is a view parameters class which defines the scale related variables 
 * that are passed from one page to another.
 * 
 * @author kahuja@vt.edu
 * @author azeckoski
 */
public class EvalScaleParameters extends SimpleViewParameters {	
    public Long id;
    public String[] points;
    public String ideal; // EvalConstants.SCALE_IDEAL_NONE;
    public String displaySetting; // EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED;

    public String findDisplaySetting() {
        return displaySetting == null ? EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED : displaySetting;
    }
    public String findIdeal() {
        return ideal == null ? EvalConstants.SCALE_IDEAL_NONE : ideal;
    }
    public String[] findPoints() {
        return points == null ? new String[] {} : points;
    }

    public EvalScaleParameters() { }

    public EvalScaleParameters(String viewID) {
        this.viewID = viewID;
        this.id = null;
    }

    public EvalScaleParameters(String viewID, Long scaleId) {
        this.viewID = viewID;
        this.id = scaleId;
    }

    /**
     * SPECIAL case for handling scale previews
     * @param viewID the view id
     * @param scaleId the scale id (must exist)
     * @param scaleDisplaySetting the constant from {@link EvalConstants} ITEM_SCALE_DISPLAY_*
     */
    public EvalScaleParameters(String viewID, Long scaleId, String scaleDisplaySetting) {
        this.viewID = viewID;
        this.id = scaleId;
        this.points = null;
        this.ideal = null;
        if (scaleDisplaySetting != null) {
            this.displaySetting = scaleDisplaySetting;
        }
    }

    public EvalScaleParameters(String viewID, String[] scalePoints) {
        this.viewID = viewID;
        this.id = null;
        this.points = scalePoints;
    }

}
