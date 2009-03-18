
package org.sakaiproject.evaluation.tool.reporting;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.OutputStream;
import java.util.List;

import org.jfree.chart.JFreeChart;

import uk.org.ponder.util.UniversalRuntimeException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.MultiColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalPDFReportBuilder {

    private Document document;
    private PdfWriter pdfWriter;
    private MultiColumnText responseArea;

    private int frontTitleSize = 26;
    private Font questionTextFont;
    private Font paragraphFont;
    private Font paragraphFontBold;
    private Font frontTitleFont;
    private Font frontAuthorFont;
    private Font frontInfoFont;

    public EvalPDFReportBuilder(OutputStream outputStream) {
        document = new Document();
        try {
            pdfWriter = PdfWriter.getInstance(document, outputStream);
            pdfWriter.setStrictImageSequence(true);
            document.open();

            // attempting to handle i18n chars better
            // BaseFont evalBF = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.IDENTITY_H,
            // BaseFont.EMBEDDED);
            // paragraphFont = new Font(evalBF, 9, Font.NORMAL);
            // paragraphFont = new Font(Font.TIMES_ROMAN, 9, Font.NORMAL);

            questionTextFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, Font.BOLD);
            paragraphFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9, Font.NORMAL);
            paragraphFontBold = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9, Font.BOLD);
            frontTitleFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, frontTitleSize, Font.NORMAL);
            frontAuthorFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 18, Font.NORMAL);
            frontInfoFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 16, Font.NORMAL);
        } catch (Exception e) {
            throw UniversalRuntimeException.accumulate(e, "Unable to start PDF Report");
        }
    }

    public void close() {
        try {
            document.add(responseArea);
            document.close();
        } catch (DocumentException e) {
            throw UniversalRuntimeException.accumulate(e, "Unable to finish PDF Report.");
        }
    }

    public void addTitlePage(String evaltitle, String groupNames, String startDate, String endDate,
            String responseInformation, byte[] bannerImageBytes, String evalSystemTitle) {
        try {
            PdfContentByte cb = pdfWriter.getDirectContent();

            float docMiddle = (document.right() - document.left()) / 2 + document.leftMargin();

            Paragraph emptyPara = new Paragraph(" ");
            emptyPara.setSpacingAfter(100.0f);

            // Title
            Paragraph titlePara = new Paragraph("\n\n\n" + evaltitle, frontTitleFont);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            document.add(titlePara);

            // Groups

            Paragraph groupPara = new Paragraph(groupNames, frontAuthorFont);
            groupPara.setSpacingBefore(25.0f);
            groupPara.setAlignment(Element.ALIGN_CENTER);
            document.add(groupPara);

            // Little info area? I don't know, it was on the mockup though
            Paragraph infoPara = new Paragraph("Results of survey", frontInfoFont);
            infoPara.setAlignment(Element.ALIGN_CENTER);
            infoPara.setSpacingBefore(90.0f);
            document.add(infoPara);

            // Started on
            Paragraph startedPara = new Paragraph(startDate, frontInfoFont);
            startedPara.setAlignment(Element.ALIGN_CENTER);
            startedPara.setSpacingBefore(25.0f);
            document.add(startedPara);

            // Ended on
            Paragraph endedPara = new Paragraph(endDate, frontInfoFont);
            endedPara.setAlignment(Element.ALIGN_CENTER);
            endedPara.setSpacingBefore(25.0f);
            document.add(endedPara);

            // Reply Rate
            Paragraph replyRatePara = new Paragraph(responseInformation, frontInfoFont);
            replyRatePara.setAlignment(Element.ALIGN_CENTER);
            replyRatePara.setSpacingBefore(25.0f);
            document.add(replyRatePara);

            // Logo and Tagline
            cb.beginText();
            cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252,
                    BaseFont.EMBEDDED), 12);
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, evalSystemTitle, docMiddle, document
                    .bottom() + 20, 0);
            cb.endText();

            if (bannerImageBytes != null) {
                Image banner = Image.getInstance(bannerImageBytes);
                cb.addImage(banner, banner.getWidth(), 0, 0, banner.getHeight(), docMiddle
                        - (banner.getWidth() / 2), document.bottom() + 35);
            }

            document.newPage();

            responseArea = new MultiColumnText();
            responseArea.addRegularColumns(document.left(), document.right(), 20f, 2);
        } catch (Exception de) {
            throw UniversalRuntimeException.accumulate(de, "Unable to create title page");
        }
    }

    public void addIntroduction(String title, String text) {
        this.addQuestionText(title);

        // Paragraph textPara = new Paragraph(text);
        // responseArea.addElement(textPara);
        this.addRegularText(text);
    }

    public void addSectionHeader(String headerText) {
        try {
            Paragraph headerPara = new Paragraph(headerText);
            responseArea.addElement(headerPara);
        } catch (DocumentException e) {
            throw UniversalRuntimeException.accumulate(e, "Unable to add Header to PDF Report");
        }
    }

    public void addCommentList(String header, List<String> textItems, String none) {
        try {
            Paragraph para = new Paragraph(header, paragraphFontBold);
            this.responseArea.addElement(para);

            if (textItems == null || textItems.size() == 0) {
                Paragraph p = new Paragraph(none, paragraphFont);
                this.responseArea.addElement(p);
            } else {
                com.lowagie.text.List list = new com.lowagie.text.List(
                        com.lowagie.text.List.UNORDERED);
                list.setListSymbol("\u2022   ");
                list.setIndentationLeft(20f);
                for (String text : textItems) {
                    if (text != null && text.length() > 0) {
                        com.lowagie.text.ListItem item = new com.lowagie.text.ListItem(text,
                                this.paragraphFont);
                        item.setSpacingAfter(8f);
                        list.add(item);
                    }
                }
                this.responseArea.addElement(list);
            }
        } catch (DocumentException e) {
            throw UniversalRuntimeException.accumulate(e, "Unable to add Essay question (" + header
                    + ") to PDF Report");
        }
    }

    public void addTextItemsList(String header, List<String> textItems) {
        try {
            // Paragraph questionPara = new Paragraph(question);
            // responseArea.addElement(questionPara);
            this.addQuestionText(header);
            com.lowagie.text.List list = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
            list.setListSymbol("\u2022   ");
            list.setIndentationLeft(20f);
            for (String text : textItems) {
                if (text != null && text.length() > 0) {
                    com.lowagie.text.ListItem item = new com.lowagie.text.ListItem(text,
                            this.paragraphFont);
                    item.setSpacingAfter(8f);
                    list.add(item);
                }
            }
            this.responseArea.addElement(list);
        } catch (DocumentException e) {
            throw UniversalRuntimeException.accumulate(e, "Unable to add Essay question (" + header
                    + ") to PDF Report");
        }
    }

    /**
     * @param question
     *            the question text
     * @param choices
     *            the text for the choices
     * @param values
     *            the count of answers for each choice (same order as choices)
     * @param showPercentages
     *            if true then show the percentages
     * @param answersAndMean
     *            the text which will be displayed above the chart (normally the answers count and
     *            mean)
     */
    public void addLikertResponse(String question, String[] choices, int[] values,
            boolean showPercentages, String answersAndMean) {
        try {
            responseArea.addElement(new Paragraph(question, questionTextFont));

            if (answersAndMean != null) {
                Paragraph header = new Paragraph(answersAndMean, paragraphFont);
                header.setSpacingAfter(1.0f);
                responseArea.addElement(header);
            }

            EvalLikertChartBuilder chartBuilder = new EvalLikertChartBuilder();
            chartBuilder.setValues(values);
            chartBuilder.setResponses(choices);
            chartBuilder.setShowPercentages(showPercentages);
            JFreeChart chart = chartBuilder.makeLikertChart();

            /* The height is going to be based off the number of choices */
            int height = 15 * choices.length;

            PdfContentByte cb = pdfWriter.getDirectContent();
            PdfTemplate tp = cb.createTemplate(200, height);
            Graphics2D g2d = tp.createGraphics(200, height, new DefaultFontMapper());
            Rectangle2D r2d = new Rectangle2D.Double(0, 0, 200, height);
            chart.draw(g2d, r2d);
            g2d.dispose();
            Image image = Image.getInstance(tp);

            // put image in the document
            responseArea.addElement(image);
        } catch (DocumentException e) {
            throw UniversalRuntimeException.accumulate(e);
        }
    }

    public void addRegularText(String text) {
        Paragraph para = new Paragraph(text, paragraphFont);
        try {
            responseArea.addElement(para);
        } catch (DocumentException e) {
            throw UniversalRuntimeException.accumulate(e, "Cannot add regular text");
        }
    }

    private void addQuestionText(String question) {
        Paragraph para = new Paragraph(question, questionTextFont);
        para.setSpacingAfter(1.0f);
        // Paragraph spacer = new Paragraph(" "); // Should probably just set margins on
        // questionTextFont
        try {
            responseArea.addElement(para);
        } catch (DocumentException e) {
            throw UniversalRuntimeException.accumulate(e, "Cannot add question text");
        }
    }

}
