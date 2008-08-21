package org.sakaiproject.evaluation.tool.viewparams;

public class ListOfEvaluationTakersViewParams extends DownloadReportViewParams {

    public ListOfEvaluationTakersViewParams() {}

    public ListOfEvaluationTakersViewParams(String viewID, Long templateId, Long evalId, String[] groupIds, String filename) {
        this.viewID = viewID;
        this.templateId = templateId;
        this.evalId = evalId;
        this.groupIds = groupIds;
        this.filename = filename;
    }

}
