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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.util.UniversalRuntimeException;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class CSVTakersReportExporter implements ReportExporter {

    private static final Log LOG = LogFactory.getLog(ReportExporterBean.class);

    private static final char DELIMITER = ',';

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator locator) {
        this.messageLocator = locator;
    }

    public void buildReport(EvalEvaluation evaluation, String[] groupIds, OutputStream outputStream, boolean newReportStyle) {
        buildReport(evaluation, groupIds, null, outputStream, newReportStyle);
    }
	
    public void buildReport(EvalEvaluation evaluation, String[] groupIds, String evaluateeId, OutputStream outputStream, boolean newReportStyle) {
        OutputStreamWriter osw = new OutputStreamWriter(outputStream);
        if (EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(evaluation.getAuthControl())) {
            try {
                osw.write(messageLocator.getMessage("reporting.respondents.nologin"));
                osw.flush();
                osw.close();
                return;
            } catch (IOException e) {
                throw UniversalRuntimeException.accumulate(e, "IO Exception thrown whilst trying to print out CSV of evaluation takers");
            }
        }

        CSVWriter writer = new CSVWriter(osw, DELIMITER);

        Set<EvalResponse> responses = evaluation.getResponses();
        Set<String> groupIdSet = new HashSet<>(Arrays.asList(groupIds));
        String[] userIds = ownersOfResponses(responses, groupIdSet);
        List<EvalUser> users = commonLogic.getEvalUsersByIds(userIds);
        Collections.sort(users, new EvalUser.SortNameComparator());
        LOG.debug("users.size(): " + users.size());
        String[] row = new String[3];

        // Headers
        row[0] = messageLocator.getMessage( "viewreport.takers.csv.email.header" );
        row[1] = messageLocator.getMessage( "viewreport.takers.csv.name.header" );
        writer.writeNext( row );

        try {

            for (EvalUser user : users) {
                row[0] = user.username;
                row[1] = user.email;
                row[2] = user.sortName;
                writer.writeNext(row);
            }
            writer.close();
            osw.close();
        } catch (IOException e) {
            throw UniversalRuntimeException.accumulate(e, "IO Exception thrown whilst trying to print out CSV of evaluation takers");
        }
    }

    private String[] ownersOfResponses(Set<EvalResponse> responses, Set<String> groupIdSet) {
        ArrayList<String> owners = new ArrayList<>(responses.size());
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
