package org.sakaiproject.evaluation.tool.viewparams;

/**
 * Parameters for downloading evaluation responses in an Excel File
 * 
 * @author Steven Githens
 */
public class ExcelReportViewParams extends DownloadReportViewParams {
    public ExcelReportViewParams() {}

    public ExcelReportViewParams(String viewID, Long templateId, Long evalId, String[] groupIds) {
        this.viewID = viewID;
        this.templateId = templateId;
        this.evalId = evalId;
        this.groupIds = groupIds;
    }
}
