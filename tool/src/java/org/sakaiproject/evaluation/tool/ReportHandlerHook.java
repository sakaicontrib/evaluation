/******************************************************************************
 * ReportHandlerHook.java - created by aaronz@vt.edu
 *
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 *
 * A copy of the Educational Community License has been included in this
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 *
 *****************************************************************************/

package org.sakaiproject.evaluation.tool;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.params.CSVReportViewParams;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.processor.HandlerHook;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.util.UniversalRuntimeException;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Handles the generation of a CSV file for exporting results
 *
 * @author Will Humphries (whumphri@vt.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 *  @author Rui Feng (fengr@vt.edu)
 */
public class ReportHandlerHook implements HandlerHook {

	private static Log log = LogFactory.getLog(ReportHandlerHook.class);

	private static final char COMMA = ',';
    private HttpServletResponse response;
	private ViewParameters viewparams;


	public void setRequest(HttpServletRequest request) {
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public void setViewparams(ViewParameters viewparams) {
		this.viewparams = viewparams;
	}
	
	private EvalEvaluationsLogic evalsLogic;
	public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
		this.evalsLogic = evalsLogic;
	}
	private EvalResponsesLogic responsesLogic;	
	public void setResponsesLogic(EvalResponsesLogic responsesLogic) {
		this.responsesLogic = responsesLogic;
	}
	

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.processor.HandlerHook#handle()
	 */
	public boolean handle() {
		log.debug("Handling report");
		// get viewparams so we know what to generate
		CSVReportViewParams crvp;
		if (viewparams instanceof CSVReportViewParams) {
			crvp = (CSVReportViewParams) viewparams;
		} else {
			// someone passed the wrong set of view params
			log.error("viewparams not received, or not an instance of CSVReportViewParams");
			return false;
		}

		// get evaluation and template from DAO
		EvalEvaluation evaluation = evalsLogic.getEvaluationById(crvp.evalId);//logic.getEvaluationById(crvp.evalId);
		//EvalTemplate template = logic.getTemplateById(evaluation.getTemplate().getId());
		EvalTemplate template = evaluation.getTemplate();
		
		Writer stringWriter = new StringWriter();
		CSVWriter writer = new CSVWriter(stringWriter, COMMA);

		List topRow = new ArrayList();		//holds top row (item text)
		List responseRows = new ArrayList();//holds response rows

		//determine number of responses
		int numOfResponses = responsesLogic.countResponses(crvp.evalId, null); //logic.countResponses(crvp.evalId, null);

		//add a row for each response
		for(int i=0; i<numOfResponses; i++){
			List currResponseRow = new ArrayList();
			responseRows.add(currResponseRow);
		}

		//get all items
		List childItems = new ArrayList(template.getItems());
		if (! childItems.isEmpty()) {
			Collections.sort(childItems, new ReportItemOrderComparator());
			//for each item
			for (int i = 0; i < childItems.size(); i++) {
				//fetch the item
				EvalItem item1 = (EvalItem) childItems.get(i);
				//if the item is scaled
				if(item1.getClassification().equals(EvalConstants.ITEM_TYPE_SCALED)){
					String labels[] = item1.getScale().getOptions();
					//add the item description to the top row
					topRow.add(item1.getItemText());
					//get all answers to this item within this evaluation
					List itemAnswers = responsesLogic.getEvalAnswers(item1.getId(), crvp.evalId);//logic.getEvalAnswers(item1.getId(), crvp.evalId);
					//for each response row
					for(int j=0; j<numOfResponses; j++){
						List currRow = (List)responseRows.get(j);
						EvalAnswer currAnswer=(EvalAnswer)itemAnswers.get(j);
						//add the answer to item within the current response to the output row
						currRow.add(labels[currAnswer.getNumeric().intValue()]);
					}
				}
				else if (item1.getClassification().equals(EvalConstants.ITEM_TYPE_BLOCK)) {//"Question Block"
				//	String labels[] = item1.getScale().getOptions();
					//add the block description to the top row
					topRow.add(item1.getItemText());
					for(int j=0; j<numOfResponses; j++){
						List currRow = (List)responseRows.get(j);
						//add blank response to block parent row
						currRow.add("");
					}
/* TODO: wait after aaron's itemLogic method to get block child items
 * 
					//get child block items
					if (item1.getBlockParent().booleanValue() == true) {
						Long parentID = item1.getId();
						Integer blockID = new Integer(parentID.intValue());

					List blockChildItems = logic.findItem(blockID);
					if (blockChildItems != null && blockChildItems.size() > 0) {
							//for each child item
							for (int j = 0; j < blockChildItems.size(); j++) {
								EvalItem child = (EvalItem) blockChildItems.get(j);
								//add child's text to top row
								topRow.add(child.getItemText());
								//get all answers to the child item within this eval
								List itemAnswers=logic.getEvalAnswers(child.getId(), crvp.evalId);
								//for each response row
								for(int y=0; y<numOfResponses;y++){
									List currRow = (List)responseRows.get(y);
									EvalAnswer currAnswer=(EvalAnswer)itemAnswers.get(y);
									//add the answer to item within the current response to the output row
									currRow.add(labels[currAnswer.getNumeric().intValue()]);
								}
							}
						}// end of if
					} // end of get child block item
					*/
				}
			}

			//convert the top row to an array
			String[] topRowArray = new String[topRow.size()];
			for(int i=0;i<topRow.size();i++){
				topRowArray[i]=(String)topRow.get(i);
			}
			//write the top row to CSVWriter object
			writer.writeNext(topRowArray);

			//for each response
			for(int i=0;i<numOfResponses;i++){
				List currRow=(List)responseRows.get(i);
				//conver the current response to an array
				String[] currRowArray = new String[currRow.size()];
				for(int j=0;j<currRow.size();j++){
					currRowArray[j]=(String)currRow.get(j);
				}
				//writer the current response to CSVWriter object
				writer.writeNext(currRowArray);
			}
		}
		response.setContentType("text/x-csv");
		response.setHeader("Content-disposition", "inline");
		response.setHeader("filename", "report.csv");
		String myCSV = stringWriter.toString();
		try {
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// dump the output to the response stream
		try {
			Writer w = response.getWriter();
			w.write( myCSV );
			return true;
		} catch (IOException e) {
			throw UniversalRuntimeException.accumulate(e,
					"Could not get Writer to dump output to csv");
		}
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.processor.HandlerHook#setHandlerHook(uk.org.ponder.rsf.processor.HandlerHook)
	 */
	public void setHandlerHook(HandlerHook arg0) {
		// don't bother with this for now -AZ
	}

	private static class ReportItemOrderComparator implements Comparator {
		public int compare(Object eval0, Object eval1) {
			// expects to get EvalItem objects
			return ((EvalItem)eval0).getId().
				compareTo(((EvalItem)eval1).getId());
		}
	}

//	private static class CSVAnswerOrderComparator implements Comparator {
//		public int compare(Object answer0, Object answer1) {
//			return ((EvalAnswer)answer0).getResponse().getId().
//				compareTo(((EvalAnswer)answer1).getResponse().getId());
//		}
//	}


}

