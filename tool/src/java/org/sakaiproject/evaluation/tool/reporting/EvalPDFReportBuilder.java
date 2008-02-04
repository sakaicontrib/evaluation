package org.sakaiproject.evaluation.tool.reporting;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

import org.jfree.chart.JFreeChart;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public class EvalPDFReportBuilder {
    private OutputStream outputStream;
    private Document document;
    private PdfWriter pdfWriter;
    
    public EvalPDFReportBuilder(OutputStream outputStream) {
        this.outputStream = outputStream;
        document = new Document();
        try {
            pdfWriter = PdfWriter.getInstance(document, outputStream);
            pdfWriter.setStrictImageSequence(true);
            document.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void close() {
        document.close();
    }
    
    public void addTitlePage(String evaltitle, String username, String userEid,
            Date startDate, int numResponses, int totalPossibleResponses) {
        try {
        
        // Title
        Paragraph titlePara = new Paragraph(evaltitle);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        document.add(titlePara);
        
        // User Name
        Paragraph usernamePara = new Paragraph(username);
        usernamePara.setAlignment(Element.ALIGN_CENTER);
        document.add(usernamePara);
        
        // Little info area? I don't know, it was on the mockup though
        Paragraph infoPara = new Paragraph("Results of evaluation");
        infoPara.setAlignment(Element.ALIGN_CENTER);
        document.add(infoPara);
        
        // Account stuff
        Paragraph accountPara = new Paragraph("Account: " + userEid + " (" + username + "'s Account");
        accountPara.setAlignment(Element.ALIGN_CENTER);
        document.add(accountPara);
        
        // Started on
        Paragraph startedPara = new Paragraph("Started: " + startDate);
        startedPara.setAlignment(Element.ALIGN_CENTER);
        document.add(startedPara);
        
        // Reply Rate
        Paragraph replyRatePara = new Paragraph("Reply rate: " + numResponses);
        if (totalPossibleResponses > 0) {
            replyRatePara.add(" / " + totalPossibleResponses);
        }
        else {
            replyRatePara.add(" responses");
        }
        
        // Logo and Tagline
        Paragraph productInfoPara = new Paragraph("Camtool Online Evaluation System");
        productInfoPara.setAlignment(Element.ALIGN_CENTER);
        document.add(productInfoPara);
        
        document.newPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void addIntroduction(String title, String text) {
        try {
            Paragraph titlePara = new Paragraph(title);
            document.add(titlePara);
            
            Paragraph textPara = new Paragraph(text);
            document.add(textPara);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void addLikertResponse(String question, String[] choices, int[] values, boolean showPercentages) {
        try {
            
        Paragraph questionPara = new Paragraph(question);
        document.add(questionPara);
        
        EvalLikertChartBuilder chartBuilder = new EvalLikertChartBuilder();
        chartBuilder.setValues(values);
        chartBuilder.setResponses(choices);
        chartBuilder.setShowPercentages(showPercentages);
        JFreeChart chart = chartBuilder.makeLikertChart();
        
        PdfContentByte cb = pdfWriter.getDirectContent();
        PdfTemplate tp = cb.createTemplate(200, 300);
        Graphics2D g2d = tp.createGraphics(200, 300, new DefaultFontMapper());
        Rectangle2D r2d = new Rectangle2D.Double(0,0,200,300);
        chart.draw(g2d, r2d);
        g2d.dispose();
        Image image = Image.getInstance(tp);
        document.add(image);
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
