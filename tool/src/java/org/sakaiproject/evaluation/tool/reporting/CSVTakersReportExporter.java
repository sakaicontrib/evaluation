package org.sakaiproject.evaluation.tool.reporting;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.util.UniversalRuntimeException;
import au.com.bytecode.opencsv.CSVWriter;

public class CSVTakersReportExporter implements ReportExporter {

    private static Log log = LogFactory.getLog(ReportExporterBean.class);

    private static final char DELIMITER = ',';
    private EvalCommonLogic commonLogic;

    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private MessageLocator messageLocator;

    public void setMessageLocator(MessageLocator locator) {
        this.messageLocator = locator;
    }

    public void buildReport(EvalEvaluation evaluation, String[] groupIds, OutputStream outputStream) {
        if (evaluation.getUnregisteredAllowed()) {
            throw new SecurityException("This evaluation allowed unregistered users to provide answers.");
        }

        OutputStreamWriter osw = new OutputStreamWriter(outputStream);
        CSVWriter writer = new CSVWriter(osw, DELIMITER);

        Set<EvalResponse> responses = evaluation.getResponses();
        Set<String> groupIdSet = new HashSet<String>(Arrays.asList(groupIds));
        String[] userIds = ownersOfResponses(responses, groupIdSet);
        List<EvalUser> users = commonLogic.getEvalUsersByIds(userIds);
        log.info("users.size(): " + users.size());
        String[] row = new String[3];
        
        try {
            row[0] = messageLocator.getMessage("reporting.numberofrespondents");
            row[1] = Integer.toString(users.size());
            writer.writeNext(row);

            row[0] = messageLocator.getMessage("reporting.username");
            row[1] = messageLocator.getMessage("reporting.displayname");
            row[2] = messageLocator.getMessage("reporting.email");
            writer.writeNext(row);
            
            for (EvalUser user : users) {
                row[0] = user.username;
                row[1] = user.displayName;
                row[2] = user.email;
                writer.writeNext(row);
            }
            writer.close();
            osw.close();
        } catch (IOException e) {
            throw UniversalRuntimeException.accumulate(e, "IO Exception thrown whilst trying to print out CSV of evaluation takers");
        }
    }

    private String[] ownersOfResponses(Set<EvalResponse> responses, Set<String> groupIdSet) {
        ArrayList<String> owners = new ArrayList<String>(responses.size());
        for (EvalResponse response : responses) {
            if (response.getEvalGroupId() != null && groupIdSet.contains(response.getEvalGroupId())) {
                owners.add(response.getOwner());
            }
        }
        return (String[]) owners.toArray(new String[owners.size()]);
    }

    public String getContentType() {
        return "text/csv";
    }

}
