package org.sakaiproject.evaluation.tool.viewparams;

/**
 * Parameters for downloading evaluation responses in a PDF File.
 * 
 * @author Steven Githens
 */
public class PDFReportViewParams extends DownloadReportViewParams {
    public PDFReportViewParams() {}

    public PDFReportViewParams(String viewID, Long templateId, Long evalId, String[] groupIds, String filename) {
        this.viewID = viewID;
        this.templateId = templateId;
        this.evalId = evalId;
        this.groupIds = groupIds;
        this.filename = filename;
    }
}
