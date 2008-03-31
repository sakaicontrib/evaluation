package org.sakaiproject.evaluation.tool.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.utils.EvalAggregatedResponses;
import org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;

import uk.org.ponder.util.UniversalRuntimeException;
import au.com.bytecode.opencsv.CSVWriter;

public class CSVReportExporter {
    private static final char COMMA = ',';
    private EvalSettings evalSettings;
    
    private EvalExternalLogic externalLogic;
    public void setExternalLogic(EvalExternalLogic externalLogic) {
       this.externalLogic = externalLogic;
    }
    
    private EvalResponseAggregatorUtil responseAggregator;
    public void setEvalResponseAggregatorUtil(EvalResponseAggregatorUtil bean) {
       this.responseAggregator = bean;
    }


    public void formatResponses(EvalEvaluation evaluation, String[] groupIds, OutputStream outputStream) {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        CSVWriter writer = new CSVWriter(outputStreamWriter, COMMA);
        
        // 1 Make TIDL
        TemplateItemDataList tidl = responseAggregator.prepareTemplateItemDataStructure(evaluation, groupIds);

        // 2 get DTIs for this eval from tidl
        List<DataTemplateItem> dtiList = tidl.getFlatListOfDataTemplateItems(true);
        
        // 3 use DTIs to make the headers
        List<String> questionCatRow = new ArrayList<String>();
        List<String> questionTypeRow = new ArrayList<String>();
        List<String> questionTextRow = new ArrayList<String>();
        for (DataTemplateItem dti: dtiList) {
           String type = TemplateItemUtils.getTemplateItemType(dti.templateItem);
           questionTypeRow.add(responseAggregator.getHeaderLabelForItemType(type));
           questionTextRow.add(externalLogic.cleanupUserStrings(
               dti.templateItem.getItem().getItemText()));
           if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(dti.associateType)) {
              questionCatRow.add("Instructor: " + externalLogic.getUserUsername(dti.associateId)); // TODO FIXME i18n
           }
           else if (EvalConstants.ITEM_CATEGORY_COURSE.equals(dti.associateType)) {
              questionCatRow.add("Course"); // TODO FIXME i18n
           }
           else {
              questionCatRow.add("");
           }
        }
        
        writer.writeNext(questionCatRow.toArray(new String[] {}));
        writer.writeNext(questionTypeRow.toArray(new String[] {}));
        writer.writeNext(questionTextRow.toArray(new String[] {}));
        
        // 4) get responseIds from tidl
        List<Long> responseIds = tidl.getResponseIdsForAnswers();
        
        // 5) loop over response ids
        for (Long responseId: responseIds) {
           // 6) loop over DTIs
           List<String> nextResponseRow = new ArrayList<String>();
           for (DataTemplateItem dti: dtiList) {
              EvalAnswer answer = dti.getAnswer(responseId);
              if (answer != null) {
                 nextResponseRow.add(responseAggregator.formatForSpreadSheet(answer.getTemplateItem(), answer));
              }
              else {
                 nextResponseRow.add("");
              }
           }
           writer.writeNext(nextResponseRow.toArray(new String[] {}));
        }
        
        
      //convert the top row to an array
        /*
        String[] topRowArray = new String[responses.topRow.size()];
        for (int i = 0; i < responses.topRow.size(); i++) {
            String questionString = externalLogic.cleanupUserStrings(responses.topRow.get(i));
            topRowArray[i] = (String) questionString;
        }
        //write the top row to CSVWriter object
        writer.writeNext(topRowArray);
        
        //for each response
        for (int i = 0; i < responses.numOfResponses; i++) {
            List currRow = (List) responses.responseRows.get(i);
            //convert the current response to an array
            String[] currRowArray = new String[currRow.size()];
            for (int j = 0; j < currRow.size(); j++) {
                currRowArray[j] = (String) currRow.get(j);
            }
            //writer the current response to CSVWriter object
            writer.writeNext(currRowArray);
        }
        */

        try {
            writer.close();
        } catch (IOException e1) {
            throw UniversalRuntimeException.accumulate(e1, "Could not close the CSVWriter");
        }
    }

    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }
}
