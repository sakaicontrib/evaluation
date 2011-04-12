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
