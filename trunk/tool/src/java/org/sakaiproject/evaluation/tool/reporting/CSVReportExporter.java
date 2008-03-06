package org.sakaiproject.evaluation.tool.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.utils.EvalAggregatedResponses;
import org.sakaiproject.util.FormattedText;

import uk.org.ponder.util.UniversalRuntimeException;
import au.com.bytecode.opencsv.CSVWriter;

public class CSVReportExporter {
    private static final char COMMA = ',';
    private EvalSettings evalSettings;

    public void formatResponses(EvalAggregatedResponses responses, OutputStream outputStream) {
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        CSVWriter writer = new CSVWriter(outputStreamWriter, COMMA);
        
      //convert the top row to an array
        String[] topRowArray = new String[responses.topRow.size()];
        for (int i = 0; i < responses.topRow.size(); i++) {
            String questionString = 
               FormattedText.convertFormattedTextToPlaintext(responses.topRow.get(i));
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
