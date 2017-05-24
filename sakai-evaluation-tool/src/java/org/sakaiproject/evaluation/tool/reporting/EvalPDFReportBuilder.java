/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.tool.reporting;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;

import uk.org.ponder.util.UniversalRuntimeException;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.ListItem;
import com.lowagie.text.Phrase;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.ColumnText;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalPDFReportBuilder {

    private Document document;
    private PdfWriter pdfWriter;
    private ColumnText responseArea;
    private int column=0;
    private int status = ColumnText.START_COLUMN;

    private int frontTitleSize = 26;
    private Font questionTextFont;
    private Font paragraphFont;
    private Font paragraphFontBold;
    private Font frontTitleFont;
    private Font frontAuthorFont;
    private Font frontInfoFont;
    private Font titleTextFont; //Added for survey title
    private Font boldTextFont; //Added for block's weighted mean
    
    private final float SPACING_AFTER_COMMENT=7.0f;
    private final float SPACING_AFTER_COMMENT_TITLE=7.0f;
    private final float SPACING_AFTER_LIST_TITLE=7.0f;
    private final float SPACING_AFTER_HEADER=10.0f;
    private final float SPACING_AFTER_LAST_LIST_ITEM=7.0f;
    private final float SPACING_BETWEEN_LIST_ITEMS=1.0f;
    
    float pagefooter = 16.0f;
    
    private static final Log LOG = LogFactory.getLog(EvalPDFReportBuilder.class);

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

            titleTextFont = new Font(Font.TIMES_ROMAN, 22, Font.BOLD);
            boldTextFont = new Font(Font.TIMES_ROMAN, 10, Font.BOLD);
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
            document.close();
    }

    public void addTitlePage(String evaltitle, String groupNames, String startDate, String endDate,
            String responseInformation, byte[] bannerImageBytes, String evalSystemTitle, String informationTitle) {
        try {
        	float pagefooter = paragraphFont.getSize();
        	
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
            Paragraph infoPara = new Paragraph(informationTitle, frontInfoFont);
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

            responseArea = new ColumnText(cb);
            responseArea.setSimpleColumn(document.left(),document.top(),document.right()/2, document.bottom()+pagefooter);
            responseArea.go();
        } catch (DocumentException | IOException de) {
            throw UniversalRuntimeException.accumulate(de, "Unable to create title page");
        }
    }

    public void addIntroduction(String title, String text) {
    	this.addTitleText(title);
        this.addRegularText(text);
    }

    public void addSectionHeader(String headerText,boolean lastElementIsHeader, float theSize)
    {
    	LOG.debug("Added a header with text: "+headerText+" and size: "+theSize+". Previous element was another header: "+lastElementIsHeader);
    	if (!lastElementIsHeader)
    	{
    		Paragraph emptyPara = new Paragraph(" ");
        	this.addElementWithJump(emptyPara, true);
    	}
    	Paragraph headerPara = new Paragraph(headerText);
    	Font fuente = headerPara.getFont();
    	fuente.setSize(theSize);
    	headerPara.setFont(fuente);
    	headerPara.setSpacingAfter(SPACING_AFTER_HEADER);
    	this.addElementWithJump(headerPara, true);
    }
    public void addSectionHeader(String headerText,boolean lastElementIsHeader) {
        this.addSectionHeader(headerText, lastElementIsHeader, 12.0f);
    }
    
    public void addCommentList(String header, List<String> textItems, String none, String textNumberOfComments)
    {
    	ArrayList<Element> myElements = new ArrayList<>();
    	
        com.lowagie.text.List list = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
    	
    	Paragraph emptyPara = new Paragraph(" ",paragraphFont);
    	myElements.add(emptyPara);
    	
        Paragraph para = new Paragraph(header, paragraphFontBold);
    	para.setSpacingAfter(SPACING_AFTER_COMMENT_TITLE);
        myElements.add(para);

        if (textItems == null || textItems.isEmpty())
        {
            Paragraph p = new Paragraph(none, paragraphFont);
            myElements.add(p);
        }
        else
        {
            list.setListSymbol("\u2022   ");
            list.setIndentationLeft(20f);
            for (String text : textItems) {
                if (text != null && text.length() > 0) {
                    com.lowagie.text.ListItem item = new com.lowagie.text.ListItem(text,
                            this.paragraphFont);
                    item.setKeepTogether(false);
                    item.setSpacingAfter(SPACING_BETWEEN_LIST_ITEMS);
                    list.add(item);
                }
            }
            myElements.add(list);
            LOG.debug("Current comment list has "+textItems.size()+" comments.");
            myElements.add(new Paragraph(textNumberOfComments + " : " + textItems.size(), paragraphFont));
        }
        
        //With more than 30 answers, we do not try to calculate the space.
        if (list.size()<=30)
        {
            this.addElementArrayWithJump(myElements);
        }
        else
        {
            this.addBigElementArray(myElements);
        }
    }

    public void addTextItemsList(String header, List<String> textItems, boolean comment, String textNumberOfAnswers)
    {
    	
    	ArrayList<Element> myElements = new ArrayList<>();
    	
        com.lowagie.text.List list = new com.lowagie.text.List(com.lowagie.text.List.UNORDERED);
        list.setListSymbol("\u2022   ");
        list.setIndentationLeft(20f);
        
        int numItem = 0;
        
        for (String text : textItems)
        {
        	numItem++;
            if (text != null && text.length() > 0)
            {
                com.lowagie.text.ListItem item = new com.lowagie.text.ListItem(text,
                        this.paragraphFont);
                item.setKeepTogether(false);
                item.setSpacingBefore(SPACING_BETWEEN_LIST_ITEMS);
                if (numItem<textItems.size()) item.setSpacingAfter(1f);
                else item.setSpacingAfter(SPACING_AFTER_LAST_LIST_ITEM);
                list.add(item);
            }
        }
        
        if (!comment)
        {
        	Paragraph emptyPara = new Paragraph(" ");
        	myElements.add(emptyPara);
        	Paragraph para = new Paragraph(header, questionTextFont);
        	para.setSpacingAfter(SPACING_AFTER_COMMENT_TITLE);
        	myElements.add(para);
        }
        else
        {
        	Paragraph emptyPara = new Paragraph(" ");
        	myElements.add(emptyPara);
            Paragraph para = new Paragraph(header, paragraphFont);
            para.setSpacingAfter(SPACING_AFTER_LIST_TITLE);
        	myElements.add(para);
        }
        
        myElements.add(list);
        myElements.add(new Paragraph(textNumberOfAnswers + " : " + textItems.size(), paragraphFont));
        //With more than 30 answers, we do not try to calculate the space.
        if (list.size()<=30)
        {
            this.addElementArrayWithJump(myElements);
        }
        else
        {
            this.addBigElementArray(myElements);
        }
    }

	/**
     * @param question
     *            the question text
     * @param choices
     *            the text for the choices
     * @param values
     *            the count of answers for each choice (same order as choices)
     * @param responseCount
     *            the number of responses to the question
     * @param showPercentages
     *            if true then show the percentages
     * @param answersAndMean
     *            the text which will be displayed above the chart (normally the answers count and
     *            mean)
     * @param lastElementIsHeader
     *            If the last element was a header, the extra spacing paragraph is not needed.
     */
    public void addLikertResponse(String question, String[] choices, int[] values,
            int responseCount, boolean showPercentages, String answersAndMean, boolean lastElementIsHeader)
    {
    	ArrayList <Element> myElements = new ArrayList<>();
    	
        try
        {
        	if (!lastElementIsHeader)
        	{
        		Paragraph emptyPara = new Paragraph(" ");
            	this.addElementWithJump(emptyPara, false);
        	}
        	
        	Paragraph myPara = new Paragraph(question, questionTextFont);
        	myPara.setSpacingAfter(SPACING_AFTER_HEADER);
        	myElements.add(myPara);

			EvalLikertChartBuilder chartBuilder = new EvalLikertChartBuilder();
			chartBuilder.setValues(values);
			chartBuilder.setResponses(choices);
			chartBuilder.setShowPercentages(showPercentages);
			chartBuilder.setResponseCount(responseCount);
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
			myElements.add(image);
			
			if (answersAndMean != null)
			{
			    Paragraph header = new Paragraph(answersAndMean, paragraphFont);
			    header.setSpacingAfter(SPACING_BETWEEN_LIST_ITEMS);
			    myElements.add(header);
			}
			
			this.addElementArrayWithJump(myElements);
		} catch (BadElementException e) {
			// TODO Auto-generated catch block
			LOG.warn( e );
		}
    }

    public void addRegularText(String text)
    {
        Paragraph para = new Paragraph(text, paragraphFont);
        para.setSpacingAfter(SPACING_AFTER_HEADER);
    	//this.addElementWithJump(para, false);
        this.addLittleElementWithJump(para);
    }
    
    public void addTitleText(String title)
    {
    	Paragraph para = new Paragraph(title, titleTextFont);
		//this.addElementWithJump(para, false);
    	this.addLittleElementWithJump(para);
    }
    
    public void addBoldText(String title)
    {
    	Paragraph para = new Paragraph(title, boldTextFont);
		//this.addElementWithJump(para, false);
    	this.addLittleElementWithJump(para);
    }

    public void addFooter(String text)
    {
    	HeaderFooter footer = new HeaderFooter((new Phrase(text+" - Pag. ",paragraphFont)),true);;
    	footer.setAlignment(HeaderFooter.ALIGN_RIGHT);
    	footer.disableBorderSide(HeaderFooter.BOTTOM);
    	
    	document.setFooter(footer);
    }

    public void addElementWithJump(Element currentPara, boolean jumpIfLittleSpace)
    {
    	//20140226 - daniel.merino@unavarra.es - https://jira.sakaiproject.org/browse/EVALSYS-1100
    	//Adds a big size element (a header or a graphic) and jumps to another page/column if it detects that is below the last third of the document.
    	float y = responseArea.getYLine();
    	LOG.debug("Vertical position Y: "+y);
    	try
    	{
    		if ((jumpIfLittleSpace) && (y<(document.top()/3)))
    		{
    			//If there's little space to the bottom border, we jump to the next column. I use this only for headers.
    			
   				column = Math.abs(column - 1);
    			
    			if (column==0)
    			{
    				document.newPage();
    				responseArea.setSimpleColumn(document.left(),document.top(),document.right()/2, document.bottom()+pagefooter);
    			}
    			else
    			{
    				responseArea.setSimpleColumn(document.right()/2,document.top(),document.right(), document.bottom()+pagefooter);
    			}
    			//Just to align vertically the top elements
				if (!currentPara.toString().equals("[ ]")) responseArea.addElement(new Paragraph(" "));
    		}
			responseArea.addElement(currentPara);
			responseArea.go();
		}
    	catch (DocumentException e) {
			// TODO Auto-generated catch block
    		throw UniversalRuntimeException.accumulate(e, "Unable to add element to PDF Report");
		}
    }
    
    public void addElementArrayWithJump(ArrayList<Element> arrayElements)
    {
    	//20140226 - daniel.merino@unavarra.es - https://jira.sakaiproject.org/browse/EVALSYS-1100
    	//Adds an array of elements, jumping to next column/page if there is not space enough.
    	//First it tests if current element fits in current column.
    	//If it does not fit, we test if it fits in a whole column.
    	//If it neither fits, we call another method that adds the array element by element.
    	
    	try
    	{
    		//First test. Do the elements fit in current column?
    		float y = responseArea.getYLine();
    		LOG.debug("Vertical position Y: "+y);
    		
			for (Element element:arrayElements)
			{
				responseArea.addElement(element);
			}

			status = responseArea.go(true); //Add elements in simulation mode to see if there is a column jump or not.
			
			if (status==ColumnText.NO_MORE_COLUMN)
			{
				//Element has not fit in the column, a new column is needed.
				column = Math.abs(column - 1);
			
				if (column==0)
				{
					document.newPage();
					responseArea.setSimpleColumn(document.left(),document.top(),document.right()/2, document.bottom()+pagefooter);
				}
				else
				{
					responseArea.setSimpleColumn(document.right()/2,document.top(),document.right(), document.bottom()+pagefooter);
				}
				//Just to align vertically the top elements
				if (!((Element)arrayElements.get(0)).toString().equals("[ ]")) arrayElements.add(0, new Paragraph(" "));
				
				//Second test: Do the elements fit in a new column?
				responseArea.setText(null);
				
				y = responseArea.getYLine();
				for (Element element:arrayElements)
				{
					responseArea.addElement(element);
				}
				status = responseArea.go(true);
				
				if (status==ColumnText.NO_MORE_COLUMN)
				{
					responseArea.setYLine(y);
					addBigElementArray(arrayElements);
				}
				else
				{
					responseArea.setYLine(y);
					
					//After testing that they fit, we put the elements in a new column.
					responseArea.setText(null);
					for (Element element:arrayElements)
					{
						responseArea.addElement(element);
					}
					responseArea.go();
				}
			}
			else
			{
				responseArea.setYLine(y);
				
				//After testing that they fit, we put the elements in the current column.
				responseArea.setText(null);
				for (Element element:arrayElements)
				{
					responseArea.addElement(element);
				}
				responseArea.go();
			}
			

		}
    	catch (DocumentException e) {
			// TODO Auto-generated catch block
    		throw UniversalRuntimeException.accumulate(e, "Unable to add elements to PDF Report");
		}
    }

    private void addBigElementArray(ArrayList<Element> myElements)
    {
    	//20140226 - daniel.merino@unavarra.es - https://jira.sakaiproject.org/browse/EVALSYS-1100
    	//Adds an array of elements that does not fit in a column, one by one.
    	//Current lowagie library does not allow to copy a List if it exceeds a whole page. It is truncated.
    	//So we add Lists always one element at a time.
    	LOG.debug("Entering in AddBigElementArray with: "+myElements.toString()+". Curent column is: "+column);

		responseArea.setText(null);
		LOG.debug("Initial vertical position: "+responseArea.getYLine());
		
		for (Element element:myElements)
		{
			if (element.getClass().equals(com.lowagie.text.List.class))
			{
				LOG.debug("We have a List element to add.");
				com.lowagie.text.List myList=(com.lowagie.text.List)element;
				for (int i=0;i<myList.size();i++)
				{
					ArrayList<ListItem> arrayItems = myList.getItems();
					ListItem miItem = arrayItems.get(i);
					String text = (String) miItem.getContent();
					Paragraph para = new Paragraph("\u2022   "+text, paragraphFont);
			    	para.setIndentationLeft(20f);
			    	this.addLittleElementWithJump(para);
				}
			}
			else
			{
				addLittleElementWithJump(element);
			}
		}

	}
    
    public void addLittleElementWithJump(Element littleElement)
    {
    	//20140226 - daniel.merino@unavarra.es - https://jira.sakaiproject.org/browse/EVALSYS-1100
    	//Adds a little element testing if it fits in current column.
    	try
    	{
	    	responseArea.addElement(littleElement);
	    	
	    	status=responseArea.go();
			if (status==ColumnText.NO_MORE_COLUMN)
			{
				column = Math.abs(column - 1);
			
				if (column==0)
				{
					document.newPage();
					responseArea.setSimpleColumn(document.left(),document.top(),document.right()/2, document.bottom()+pagefooter);
				}
				else
				{
					responseArea.setSimpleColumn(document.right()/2,document.top(),document.right(), document.bottom()+pagefooter);
				}
			}
		}
		catch (DocumentException e) {
			// TODO Auto-generated catch block
			throw UniversalRuntimeException.accumulate(e, "Unable to add elements to PDF Report");
		}
    }
    
}
