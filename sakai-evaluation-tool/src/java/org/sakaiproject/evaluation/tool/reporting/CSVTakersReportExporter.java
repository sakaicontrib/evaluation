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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;

import com.opencsv.CSVWriter;

import lombok.extern.slf4j.Slf4j;

/**
 * Exports the full list of evaluation participants (respondents and non-respondents)
 * as a CSV file, with columns for group (when multiple groups), username, email,
 * display name, and response status.
 *
 * @author Steven Githens
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@Slf4j
public class CSVTakersReportExporter implements ReportExporter {

    private static final char DELIMITER = ',';

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalDeliveryService deliveryService;
    public void setDeliveryService(EvalDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    private EvalMessageLocator messageLocator;
    public void setEvalMessageLocator(EvalMessageLocator locator) {
        this.messageLocator = locator;
    }

    public void buildReport(EvalEvaluation evaluation, String[] groupIds, OutputStream outputStream, boolean newReportStyle) {
        buildReport(evaluation, groupIds, null, outputStream, newReportStyle);
    }

    public void buildReport(EvalEvaluation evaluation, String[] groupIds, String evaluateeId, OutputStream outputStream, boolean newReportStyle) {
        if (EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(evaluation.getAuthControl())) {
            try (OutputStreamWriter osw = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                osw.write(messageLocator.getMessage("reporting.respondents.nologin"));
            } catch (IOException e) {
                throw new RuntimeException("IO Exception writing CSV takers", e);
            }
            return;
        }

        boolean multiGroup = groupIds != null && groupIds.length > 1;

        // Load responses indexed by groupId → (userId → response)
        List<EvalResponse> responses = deliveryService.getEvaluationResponses(evaluation.getId(), groupIds, null);
        Map<String, Map<String, EvalResponse>> groupUserResponses = new HashMap<>();
        for (EvalResponse r : responses) {
            groupUserResponses.computeIfAbsent(r.getEvalGroupId(), k -> new HashMap<>())
                    .put(r.getOwner(), r);
        }

        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
                DELIMITER,
                CSVWriter.DEFAULT_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {

            // Header row
            List<String> headers = new ArrayList<>();
            if (multiGroup) {
                headers.add(messageLocator.getMessage("viewreport.takers.csv.group.header"));
            }
            headers.add(messageLocator.getMessage("viewreport.takers.csv.username.header"));
            headers.add(messageLocator.getMessage("viewreport.takers.csv.email.header"));
            headers.add(messageLocator.getMessage("viewreport.takers.csv.name.header"));
            headers.add(messageLocator.getMessage("viewreport.takers.csv.status.header"));
            writer.writeNext(headers.toArray(new String[0]));

            for (String groupId : groupIds) {
                String groupTitle = multiGroup ? commonLogic.makeEvalGroupObject(groupId).title : null;
                Map<String, EvalResponse> userResponses = groupUserResponses.getOrDefault(groupId, Collections.emptyMap());

                Set<String> userIds = commonLogic.getUserIdsForEvalGroup(groupId, EvalConstants.PERM_TAKE_EVALUATION, false);
                List<EvalUser> users = commonLogic.getEvalUsersByIds(new ArrayList<>(userIds));
                users.sort(new EvalUser.SortNameComparator());

                log.debug("Group {}: {} assigned users, {} responses", groupId, users.size(), userResponses.size());

                for (EvalUser user : users) {
                    EvalResponse resp = userResponses.get(user.userId);
                    String status;
                    if (resp == null) {
                        status = messageLocator.getMessage("evalresponders.status.untaken");
                    } else if (resp.complete) {
                        status = messageLocator.getMessage("evalresponders.status.complete");
                    } else {
                        status = messageLocator.getMessage("evalresponders.status.incomplete");
                    }

                    List<String> row = new ArrayList<>();
                    if (multiGroup) row.add(groupTitle);
                    row.add(user.username);
                    row.add(user.email);
                    row.add(user.displayName);
                    row.add(status);
                    writer.writeNext(row.toArray(new String[0]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("IO Exception writing CSV takers", e);
        }
    }

    public String getContentType() {
        return "text/csv";
    }
}