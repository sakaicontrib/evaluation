
package org.sakaiproject.evaluation.tool.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.util.UniversalRuntimeException;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class CSVReportExporter implements ReportExporter {

    private static final char COMMA = ',';

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalResponseAggregatorUtil responseAggregator;
    public void setEvalResponseAggregatorUtil(EvalResponseAggregatorUtil bean) {
        this.responseAggregator = bean;
    }

    private MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator locator) {
        this.messageLocator = locator;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.tool.reporting.ReportExporter#buildReport(org.sakaiproject.evaluation.model.EvalEvaluation, java.lang.String[], java.io.OutputStream)
     */
    public void buildReport(EvalEvaluation evaluation, String[] groupIds, OutputStream outputStream) {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        CSVWriter writer = new CSVWriter(outputStreamWriter, COMMA);

        // 1 Make TIDL
        TemplateItemDataList tidl = responseAggregator.prepareTemplateItemDataStructure(evaluation,
                groupIds);

        // 1.5 get instructor info
        Set<String> instructorIds = tidl.getAssociateIds(EvalConstants.ITEM_CATEGORY_INSTRUCTOR);
        Map<String, EvalUser> instructorIdtoEvalUser = responseAggregator
                .getInstructorsInformation(instructorIds);

        // 2 get DTIs for this eval from tidl
        List<DataTemplateItem> dtiList = tidl.getFlatListOfDataTemplateItems(true);

        // 3 use DTIs to make the headers
        List<String> questionCatRow = new ArrayList<String>();
        List<String> questionTypeRow = new ArrayList<String>();
        List<String> questionTextRow = new ArrayList<String>();
        for (DataTemplateItem dti : dtiList) {
            questionTypeRow.add(responseAggregator.getHeaderLabelForItemType(dti
                    .getTemplateItemType()));
            questionTextRow.add(commonLogic.makePlainTextFromHTML(dti.templateItem.getItem()
                    .getItemText()));
            if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(dti.associateType)) {
                questionCatRow.add(messageLocator.getMessage("reporting.spreadsheet.instructor",
                        instructorIdtoEvalUser.get(dti.associateId).displayName));
            } else if (EvalConstants.ITEM_CATEGORY_COURSE.equals(dti.associateType)) {
                questionCatRow.add(messageLocator.getMessage("reporting.spreadsheet.course"));
            } else {
                questionCatRow.add(messageLocator.getMessage("unknown.caps"));
            }

            if (dti.usesComments()) {
                // add an extra column for comments
                questionTypeRow.add(messageLocator.getMessage("viewreport.comments.header"));
                // also add in blanks for the other columns
                questionTextRow.add("");
                questionCatRow.add("");
            }

        }

        writer.writeNext(questionCatRow.toArray(new String[] {}));
        writer.writeNext(questionTypeRow.toArray(new String[] {}));
        writer.writeNext(questionTextRow.toArray(new String[] {}));

        // 4) get responseIds from tidl
        List<Long> responseIds = tidl.getResponseIdsForAnswers();

        // 5) loop over response ids
        for (Long responseId : responseIds) {
            // 6) loop over DTIs
            List<String> nextResponseRow = new ArrayList<String>();
            for (DataTemplateItem dti : dtiList) {
                EvalAnswer answer = dti.getAnswer(responseId);
                if (answer != null) {
                    nextResponseRow.add(responseAggregator.formatForSpreadSheet(answer
                            .getTemplateItem(), answer));
                    if (dti.usesComments()) {
                        // put comment in the next column
                        nextResponseRow.add(EvalUtils.isBlank(answer.getComment()) ? "" : answer
                                .getComment());
                    }
                } else {
                    nextResponseRow.add("");
                    if (dti.usesComments()) {
                        nextResponseRow.add(""); // put in blank to space columns correctly
                    }
                }
            }
            writer.writeNext(nextResponseRow.toArray(new String[] {}));
        }

        // convert the top row to an array
        /*
         * String[] topRowArray = new String[responses.topRow.size()]; for (int i = 0; i <
         * responses.topRow.size(); i++) { String questionString =
         * commonLogic.cleanupUserStrings(responses.topRow.get(i)); topRowArray[i] = (String)
         * questionString; } //write the top row to CSVWriter object writer.writeNext(topRowArray);
         * 
         * //for each response for (int i = 0; i < responses.numOfResponses; i++) { List currRow =
         * (List) responses.responseRows.get(i); //convert the current response to an array String[]
         * currRowArray = new String[currRow.size()]; for (int j = 0; j < currRow.size(); j++) {
         * currRowArray[j] = (String) currRow.get(j); } //writer the current response to CSVWriter
         * object writer.writeNext(currRowArray); }
         */

        try {
            writer.close();
        } catch (IOException e1) {
            throw UniversalRuntimeException.accumulate(e1, "Could not close the CSVWriter");
        }
    }

    public String getContentType() {
        return "text/csv";
    }
}
