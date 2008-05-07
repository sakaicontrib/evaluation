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
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.MultiColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public class EvalPDFReportBuilder {
   private Document document;
   private PdfWriter pdfWriter;
   private MultiColumnText responseArea;

   private Font questionTextFont;
   private Font paragraphFont;
   int frontTitleSize = 26;
   private Font frontTitleFont;
   private Font frontAuthorFont;
   private Font frontInfoFont;

   public EvalPDFReportBuilder(OutputStream outputStream) {
      document = new Document();
      try {
         pdfWriter = PdfWriter.getInstance(document, outputStream);
         pdfWriter.setStrictImageSequence(true);
         document.open();

         questionTextFont = new Font(Font.TIMES_ROMAN, 14, Font.BOLD);
         paragraphFont = new Font(Font.TIMES_ROMAN, 10, Font.NORMAL);
         frontTitleFont = new Font(Font.TIMES_ROMAN, frontTitleSize, Font.NORMAL);
         frontAuthorFont = new Font(Font.TIMES_ROMAN, 18, Font.NORMAL);
         frontInfoFont = new Font(Font.TIMES_ROMAN, 16, Font.NORMAL);
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

   public void addTitlePage(String evaltitle, String username, String accountInfo,
         String startDate, String responseInformation, byte[] bannerImageBytes,
         String evalSystemTitle) {
      try {
         PdfContentByte cb = pdfWriter.getDirectContent();

         float docMiddle = (document.right()-document.left()) / 2 + document.leftMargin();

         Paragraph emptyPara = new Paragraph(" ");
         emptyPara.setSpacingAfter(100.0f);

         // Title
         Paragraph titlePara = new Paragraph("\n\n\n" + evaltitle, frontTitleFont);
         titlePara.setAlignment(Element.ALIGN_CENTER);
         document.add(titlePara);

         // User Name
         Paragraph usernamePara = new Paragraph(username, frontAuthorFont);
         usernamePara.setSpacingBefore(25.0f);
         usernamePara.setAlignment(Element.ALIGN_CENTER);
         document.add(usernamePara);


         // Little info area? I don't know, it was on the mockup though
         Paragraph infoPara = new Paragraph("Results of survey", frontInfoFont);
         infoPara.setAlignment(Element.ALIGN_CENTER);
         infoPara.setSpacingBefore(90.0f);
         document.add(infoPara);

         // Account stuff
         Paragraph accountPara = new Paragraph(accountInfo, frontInfoFont);
         accountPara.setAlignment(Element.ALIGN_CENTER);
         accountPara.setSpacingBefore(110.0f);
         document.add(accountPara);

         // Started on
         Paragraph startedPara = new Paragraph(startDate, frontInfoFont);
         startedPara.setAlignment(Element.ALIGN_CENTER);
         startedPara.setSpacingBefore(25.0f);
         document.add(startedPara);

         // Reply Rate
         Paragraph replyRatePara = new Paragraph(responseInformation, frontInfoFont);
         replyRatePara.setAlignment(Element.ALIGN_CENTER);
         replyRatePara.setSpacingBefore(25.0f);
         document.add(replyRatePara);

         // Logo and Tagline
         cb.beginText();
         cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.EMBEDDED), 12);
         cb.showTextAligned(PdfContentByte.ALIGN_CENTER, evalSystemTitle, 
               docMiddle, 
               document.bottom() + 20, 0);
         cb.endText();

         if (bannerImageBytes != null) {
            Image banner = Image.getInstance(bannerImageBytes);
            cb.addImage(banner, banner.getWidth(), 0, 0, banner.getHeight(), 
                  docMiddle - (banner.getWidth() / 2), document.bottom() + 35);
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

      //Paragraph textPara = new Paragraph(text);
      //responseArea.addElement(textPara);
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

   public void addTextItemsList(String header, List<String> textItems) {
      try {
         //Paragraph questionPara = new Paragraph(question);
         //responseArea.addElement(questionPara);
         this.addQuestionText(header);
         com.lowagie.text.List list = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
         list.setListSymbol("\u2022   ");
         list.setIndentationLeft(20f);
         for (String text: textItems) {
            if (text != null && text.length() > 0) {
               com.lowagie.text.ListItem item = new com.lowagie.text.ListItem(text, this.paragraphFont);
               item.setSpacingAfter(8f);
               list.add(item);
            }
         }
         this.responseArea.addElement(list);
      } catch (DocumentException e) {
         throw UniversalRuntimeException.accumulate(e, "Unable to add Essay question ("+header+") to PDF Report");
      }
   }

   public void addLikertResponse(String question, String[] choices, int[] values, boolean showPercentages) {
      try {
         this.addQuestionText(question);

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
         Rectangle2D r2d = new Rectangle2D.Double(0,0,200,height);
         chart.draw(g2d, r2d);
         g2d.dispose();
         Image image = Image.getInstance(tp);
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
      Paragraph spacer = new Paragraph(" "); // Should probably just set margins on questionTextFont
      try {
         responseArea.addElement(para);
         responseArea.addElement(spacer);
      } catch (DocumentException e) {
         throw UniversalRuntimeException.accumulate(e, "Cannot add question header");
      }
   }

}
