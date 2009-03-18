
package org.sakaiproject.evaluation.tool.reporting;

import org.sakaiproject.evaluation.logic.ReportingPermissions;

/**
 * This little class is meant to be a request scope bean to bridge the gap between the Choose Groups
 * VCP and our VPI that redirects if there is only one group available to select.
 * 
 * TODO FIXME : We need to support redirecting for anonymous surveys too, but creating anonymous
 * evals seems to broke in the GUI right now. sgithens 2008-02-24 5:30PM Central Time
 */
public class CurrentViewableReports {

    private Long evalId;
    private String[] viewableGroupIDs;
    private ReportingPermissions reportingPermissions;

    public void populate(Long evalId) {
        this.evalId = evalId;
        viewableGroupIDs = reportingPermissions
                .getResultsViewableEvalGroupIdsForCurrentUser(evalId).toArray(new String[] {});
    }

    /* Boring java boilerplate code below */
    public String[] getViewableGroupIDs() {
        return viewableGroupIDs;
    }

    public void setViewableGroupIDs(String[] viewableGroupIDs) {
        this.viewableGroupIDs = viewableGroupIDs;
    }

    public void setReportingPermissions(ReportingPermissions reportingPermissions) {
        this.reportingPermissions = reportingPermissions;
    }

    public Long getEvalId() {
        return evalId;
    }

    public void setEvalId(Long evalId) {
        this.evalId = evalId;
    }

}
