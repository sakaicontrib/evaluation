package org.sakaiproject.evaluation.tool.reporting;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ExtendedCategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.FormattedText;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public class PDFReportExporter {
    private static Log log = LogFactory.getLog(PDFReportExporter.class);

    private EvalSettings evalSettings;
    private EvalResponsesLogic responsesLogic;
    private EvalEvaluationsLogic evalsLogic;
    private HttpServletResponse response;
    private EvalExternalLogic externalLogic;
    private ContentHostingService contentHostingService;
    // TODO I think we are supposed to use ExternalUsers but I don't see any 
    // bean declaration of it yet.
    private UserDirectoryService userDirectoryService;

    public void respondWithPDF(EvalEvaluation evaluation, EvalTemplate template,
            List<EvalItem> allEvalItems, List<EvalTemplateItem> allEvalTemplateItems,
            List<String> topRow, List<List<String>> responseRows, int numOfResponses,
            String[] groupIDs) {
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            
            // make a standard place in the eval webapp to put your custom image
            Boolean useBannerImage = (Boolean) evalSettings.get(EvalSettings.ENABLE_PDF_REPORT_BANNER);
            if (useBannerImage != null && useBannerImage == true) {
                String bannerImageLocation = (String) evalSettings.get(EvalSettings.PDF_BANNER_IMAGE_LOCATION);
                if (bannerImageLocation != null) {
                    try {
                        ContentResource contentResource = contentHostingService.getResource(bannerImageLocation);
                        Image banner = Image.getInstance(contentResource.getContent());
                        banner.setAlignment(Element.ALIGN_CENTER);
                        document.add(banner);
                    } catch (Exception e) {
                        log.warn("Cannot get PDF Banner Image for Evaluation Export", e);
                    }
                    
                }
            }
            
            // Title of Survey
            Paragraph title = new Paragraph(evaluation.getTitle());
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            // Account Name
            Paragraph accountName = new Paragraph(userDirectoryService.getCurrentUser().getDisplayName());
            accountName.setAlignment(Element.ALIGN_CENTER);
            document.add(accountName);
            
            // Table with info on,
            // Carried out:  12th July 07 - 18th July 07
            // Response rate:  76% ( 80 / 126 )
            // Invitees: IB PMS 07/08
            PdfPTable table = new PdfPTable(2);
            table.addCell("Carried out:");
            String dates = evaluation.getStartDate() + " - " + evaluation.getStopDate();
            table.addCell(dates);
            table.addCell("Response Rate:");
            table.addCell(getParticipantResults(evaluation));
            table.addCell("Invitees:");
            String groupsCellContents = "";
            if (groupIDs.length > 0) {
                for (int groupCounter = 0; groupCounter < groupIDs.length; groupCounter++) {//groupTitles.size(); groupCounter++) {
                    groupsCellContents +=  externalLogic.getDisplayTitle(groupIDs[groupCounter]); //groupTitles.get(groupCounter);
                    if (groupCounter+1 < groupIDs.length) {//groupTitles.size()) {
                        groupsCellContents += ", ";
                    }
                }
            }
            table.addCell(groupsCellContents);
            document.add(table);
            
            for (int i = 0; i < topRow.size(); i++) {
                String plainQuestionText = FormattedText.convertFormattedTextToPlaintext(topRow.get(i));
                Paragraph question = new Paragraph((i+1) + ". " + plainQuestionText);
                document.add(question);
                if (TemplateItemUtils.getTemplateItemType(allEvalTemplateItems.get(i)).equals(EvalConstants.ITEM_TYPE_TEXT)) {
                    for (int j = 0; j < responseRows.size(); j++) {
                        String response = responseRows.get(j).get(i) + "\n";
                        Paragraph para = new Paragraph(response);
                        para.setIndentationLeft(20);
                        document.add(para);
                        if (j+1 < responseRows.size()) {
                            Paragraph spacer = new Paragraph("---");
                            //spacer.setAlignment(Element.ALIGN_CENTER);
                            spacer.setIndentationLeft(20);
                            document.add(spacer);
                        }
                    }
                }
                else if (TemplateItemUtils.getTemplateItemType(allEvalTemplateItems.get(i)).equals(EvalConstants.ITEM_TYPE_SCALED)) {
                    Map<String,Integer> resultMap = new HashMap<String,Integer>();
                    for (int j = 0; j < responseRows.size(); j++) {
                        String response = responseRows.get(j).get(i);
                        if (resultMap.containsKey(response)) {
                            int value = resultMap.get(response).intValue();
                            value++;
                            resultMap.put(response, value);
                        }
                        else
                            resultMap.put(response, 1);
                    }
                    String overview = "";
                    for (String key: resultMap.keySet()) {
                        overview += key + ", " + resultMap.get(key) + "   ";
                    }
                    document.add(new Paragraph(overview));
                    /*
                    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                    
                    for (String key: resultMap.keySet()) {
                        dataset.addValue(resultMap.get(key), "Results", key);
                    }
                    JFreeChart chart = ChartFactory.createBarChart(
                            null,                         // chart title
                            null,                         // domain axis label
                            null,                         // range axis label
                            dataset,                      // data
                            PlotOrientation.HORIZONTAL,   // orientation
                            false,                        // include legend
                            true,
                            false
                        );
                    chart.setBackgroundPaint(Color.white);
                    chart.getPlot().setOutlinePaint(null);
                    TextTitle charttitle = new TextTitle("Figure 6 | Overall SEO Rating");
                    charttitle.setHorizontalAlignment(HorizontalAlignment.LEFT);
                    charttitle.setBackgroundPaint(Color.red);
                    charttitle.setPaint(Color.white);
                    
                    chart.setTitle(charttitle);
                    CategoryPlot plot = (CategoryPlot) chart.getPlot();
                    
                    ValueAxis rangeAxis = plot.getRangeAxis();
                    rangeAxis.setRange(0.0, 4.0);
                    rangeAxis.setVisible(false);
                    
                    ExtendedCategoryAxis domainAxis = new ExtendedCategoryAxis(null);
                    domainAxis.setTickLabelFont(new Font("SansSerif", Font.BOLD, 12));
                    domainAxis.setCategoryMargin(0.30);
                   
                    for (String key: resultMap.keySet()) {
                        domainAxis.addSubLabel("Response", key);
                    }
                    CategoryLabelPositions p = domainAxis.getCategoryLabelPositions();
                    CategoryLabelPosition left = new CategoryLabelPosition(
                            RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT);
                    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.replaceLeftPosition(p, left));
                    plot.setDomainAxis(domainAxis);
                    
                    BarRenderer renderer = (BarRenderer) plot.getRenderer();
                    renderer.setSeriesPaint(0, new Color(0x9C, 0xA4, 0x4A));
                    renderer.setDrawBarOutline(false);
                    
                    StandardCategoryItemLabelGenerator generator 
                            = new StandardCategoryItemLabelGenerator("{2}", 
                                    new DecimalFormat("0.00"));
                    renderer.setBaseItemLabelGenerator(generator);
                    renderer.setBaseItemLabelsVisible(true);
                    renderer.setBaseItemLabelFont(new Font("SansSerif", Font.PLAIN, 18));
                    ItemLabelPosition position = new ItemLabelPosition(
                            ItemLabelAnchor.INSIDE3, TextAnchor.CENTER_RIGHT);
                    renderer.setBasePositiveItemLabelPosition(position);
                    renderer.setPositiveItemLabelPositionFallback(new ItemLabelPosition());
                    
                    PdfContentByte cb = writer.getDirectContent();
                    PdfTemplate tp = cb.createTemplate(400, 300);
                    Graphics2D g2 = tp.createGraphics(400, 300, new DefaultFontMapper());
                    Rectangle2D r2D = new Rectangle2D.Double(0, 0, 400, 300);
                    chart.draw(g2, r2D);
                    g2.dispose();
                    cb.addTemplate(tp, 0, 0);
                    */
                }
                else { //TODO what do I do with other question types?
                    
                }
                
                // Print out the Essay Results
                //if (TemplateItemUtils.getTemplateItemType(tempItem).equals(EvalConstants.ITEM_TYPE_TEXT)) {
                //    
                //}
            }
            
            document.close();
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }
    
    // Bad duplicated code
    private String getParticipantResults(EvalEvaluation evaluation) {
     // Response Rate calculation... this is sort of duplicated code from ControlEvaluationsProducer
        // might be good to put it in one of the logic or utility classes.
        int countResponses = responsesLogic.countResponses(evaluation.getId(), null);
        int countEnrollments = getTotalEnrollmentsForEval(evaluation.getId());
        long percentage = 0;
        if (countEnrollments > 0) {
           percentage = Math.round(  (((float)countResponses) / (float)countEnrollments) * 100.0 );
           return percentage + "%  ( " + countResponses + " / " + countEnrollments + " )";
           //UIOutput.make(evaluationRow, "closed-eval-response-rate", countResponses + "/"
           //      + countEnrollments + " - " + percentage + "%");
        } else {
           // don't bother showing percentage or "out of" when there are no enrollments
           //UIOutput.make(evaluationRow, "closed-eval-response-rate", countResponses + "");
           return countResponses + "";
        }
    }
    
    /**
     * More duplicated code from ControlEvaluationsProducer
     * 
     * Gets the total count of enrollments for an evaluation
     * 
     * @param evaluationId
     * @return total number of users with take eval perms in this evaluation
     */
    private int getTotalEnrollmentsForEval(Long evaluationId) {
        int totalEnrollments = 0;
        Map<Long, List<EvalAssignGroup>> evalAssignGroups = evalsLogic.getEvaluationAssignGroups(new Long[] {evaluationId}, true);
        List<EvalAssignGroup> groups = evalAssignGroups.get(evaluationId);
        for (int i=0; i<groups.size(); i++) {
            EvalAssignGroup eac = (EvalAssignGroup) groups.get(i);
            String context = eac.getEvalGroupId();
            Set<String> userIds = externalLogic.getUserIdsForEvalGroup(context, EvalConstants.PERM_TAKE_EVALUATION);
            totalEnrollments = totalEnrollments + userIds.size();
        }
        return totalEnrollments;
    }

    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setResponsesLogic(EvalResponsesLogic responsesLogic) {
        this.responsesLogic = responsesLogic;
    }

    public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
        this.evalsLogic = evalsLogic;
    }

    public void setExternalLogic(EvalExternalLogic externalLogic) {
        this.externalLogic = externalLogic;
    }
}
