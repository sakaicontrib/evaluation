package org.sakaiproject.evaluation.tool.viewparams;

/**
 * Params for downloading evaluation responses as a comma separated value file.
 * 
 * @author Steven Githens
 */
public class CSVReportViewParams extends DownloadReportViewParams {

    public CSVReportViewParams() {}

    public CSVReportViewParams(String viewID, Long templateId, Long evalId, String[] groupIds, String filename) {
        this.viewID = viewID;
        this.templateId = templateId;
        this.evalId = evalId;
        this.groupIds = groupIds;
        this.filename = filename;
    }
    
}
