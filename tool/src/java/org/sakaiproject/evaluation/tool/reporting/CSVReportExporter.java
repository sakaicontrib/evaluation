package org.sakaiproject.evaluation.tool.reporting;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.util.FormattedText;

import uk.org.ponder.util.UniversalRuntimeException;
import au.com.bytecode.opencsv.CSVWriter;

public class CSVReportExporter {
    
    private static final char COMMA = ',';

    private EvalSettings evalSettings;
    private HttpServletResponse response;

    public void respondWithCSV(List<String> topRow, List responseRows, int numOfResponses) {
        Writer stringWriter = new StringWriter();
        CSVWriter writer = new CSVWriter(stringWriter, COMMA);
        
      //convert the top row to an array
        String[] topRowArray = new String[topRow.size()];
        for (int i = 0; i < topRow.size(); i++) {
            String questionString = FormattedText.convertFormattedTextToPlaintext(topRow.get(i));
            topRowArray[i] = (String) questionString;
        }
        //write the top row to CSVWriter object
        writer.writeNext(topRowArray);
        
        //for each response
        for (int i = 0; i < numOfResponses; i++) {
            List currRow = (List) responseRows.get(i);
            //convert the current response to an array
            String[] currRowArray = new String[currRow.size()];
            for (int j = 0; j < currRow.size(); j++) {
                currRowArray[j] = (String) currRow.get(j);
            }
            //writer the current response to CSVWriter object
            writer.writeNext(currRowArray);
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
            w.write(myCSV);
        } catch (IOException e) {
            throw UniversalRuntimeException.accumulate(e, "Could not get Writer to dump output to csv");
        }
    }

    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }
}
