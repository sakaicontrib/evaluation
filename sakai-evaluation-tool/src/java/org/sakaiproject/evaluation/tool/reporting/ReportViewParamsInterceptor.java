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
package org.sakaiproject.evaluation.tool.reporting;

import org.sakaiproject.evaluation.tool.producers.ReportChooseGroupsProducer;
import org.sakaiproject.evaluation.tool.producers.ReportsViewingProducer;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;

import uk.org.ponder.rsf.viewstate.AnyViewParameters;
import uk.org.ponder.rsf.viewstate.RedirectViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsInterceptor;

/**
 * This interceptor checks for incoming requests to the Choose Groups for Report
 * viewing page. If there is less than 2 groups to choose from, it redirects you
 * straight to the report view.
 * 
 * @author sgithens
 */
public class ReportViewParamsInterceptor implements ViewParamsInterceptor {

    private CurrentViewableReports curViewableReports;
    public void setCurrentViewableReports(CurrentViewableReports cur) {
        this.curViewableReports = cur;
    }

    public AnyViewParameters adjustViewParameters(ViewParameters incoming) {
        AnyViewParameters togo = incoming;

        if (ReportChooseGroupsProducer.VIEW_ID.equals(incoming.viewID)) {
            ReportParameters params = (ReportParameters) incoming;
            curViewableReports.populate(params.evaluationId);
            if (curViewableReports.getViewableGroupIDs().length <= 1) {
                ReportParameters viewReports = (ReportParameters) params.copyBase();
                viewReports.viewID = ReportsViewingProducer.VIEW_ID;
                viewReports.groupIds = curViewableReports.getViewableGroupIDs();
                togo = new RedirectViewParameters(viewReports);
            }
        }
        return togo;
    }

}
