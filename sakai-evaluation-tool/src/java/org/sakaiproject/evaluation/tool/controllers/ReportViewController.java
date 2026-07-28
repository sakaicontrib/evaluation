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
package org.sakaiproject.evaluation.tool.controllers;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.reporting.ReportItemVisibility;
import org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils.AnswersMean;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.HierarchyNodeGroup;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.TemplateItemGroup;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.util.Validator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Results view for an evaluation.
 * Spring MVC equivalent of ReportsViewingProducer.
 */
@Slf4j
@Controller
@RequestMapping("/report_view")
public class ReportViewController extends EvalControllerSupport {

    private static final String VIEWMODE_REGULAR = "viewmode_regular";
    private static final String VIEWMODE_ALLESSAYS = "viewmode_allessays";


    @Resource(name = "org.sakaiproject.evaluation.tool.utils.EvalResponseAggregatorUtil")
    private EvalResponseAggregatorUtil responseAggregator;


    // -------------------------------------------------------------------------
    // DTOs
    // -------------------------------------------------------------------------

    @Data
    public static class ChoiceRow {
        private final String label;
        private final int count;
        private final String percentage;
        private final boolean labelKey; // true → label is an i18n key
    }

    @Data
    public static class CommentRow {
        private final int num;
        private final String text;
        private final boolean odd;
    }

    @Data
    public static class ResponseRow {
        private final int num;
        private final String text;
        private final boolean odd;
    }

    @Data
    public static class ItemResult {
        private final String type; // "scaled", "essay", "textual"
        private final int displayNumber;
        private final String itemText;
        private final int responsesCount;
        private final List<ChoiceRow> choices;   // scaled only
        private final AnswersMean mean;           // scaled only, may be null
        private final List<CommentRow> comments;  // scaled with comments
        private final boolean usesComments;      // true → show comments section
        private final List<ResponseRow> responses; // essay only
        private final int naCount;               // essay only
        private final boolean usesNA;
        private final boolean odd;
    }

    @Data
    public static class CategorySection {
        private final String headerKey;    // null → no header
        private final String headerArg;   // user display name, null if not needed
        private final List<ItemResult> items;
    }

    @Data
    public static class ExportOption {
        private final String type;
        private final String sectionedType;
        private final String labelKey;
        private final String url;
        private final String sectionedUrl;
    }

    @Data
    public static class IndividualPdf {
        private final String userId;
        private final String displayName;
        private final String filename;
        private final String url;
    }

    // -------------------------------------------------------------------------
    // GET handler
    // -------------------------------------------------------------------------

    @GetMapping
    public String show(@RequestParam Long evaluationId,
                       @RequestParam(required = false) String[] groupIds,
                       @RequestParam(required = false, defaultValue = VIEWMODE_REGULAR) String viewmode,
                       HttpServletRequest request,
                       Model model) {

        String currentUserId = currentUserId();

        model.addAttribute("notAllowed", false);
        model.addAttribute("notEnoughResponses", false);
        model.addAttribute("hasItems", false);
        model.addAttribute("hasComments", false);
        model.addAttribute("hasTextResponses", false);
        model.addAttribute("exportOptions", new ArrayList<>());
        model.addAttribute("individualPdfs", new ArrayList<>());
        model.addAttribute("categorySections", new ArrayList<>());
        model.addAttribute("evaluationId", evaluationId);
        model.addAttribute("groupIds", groupIds);
        model.addAttribute("allEssays", false);
        model.addAttribute("canChooseGroups", false);

        Set<String> viewableGroups = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(evaluationId);
        if (viewableGroups.isEmpty()) {
            model.addAttribute("notAllowed", true);
            return "report_view";
        }

        boolean canChooseGroups = viewableGroups.size() > 1;
        if (groupIds == null || groupIds.length == 0) {
            if (canChooseGroups) {
                return "redirect:/report_groups?evaluationId=" + evaluationId;
            }
            groupIds = viewableGroups.toArray(new String[0]);
        }

        EvalEvaluation evaluation = evaluationService.getEvaluationById(evaluationId);

        // Minimum responses check
        int responsesCount = deliveryService.countResponses(evaluation.getId(), groupIds[0], true);
        int enrollmentsCount = evaluationService.countParticipantsForEval(evaluation.getId(), new String[]{groupIds[0]});
        int responsesNeeded = evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount);
        boolean controlEval = evaluationService.canControlEvaluation(currentUserId, evaluationId);
        if (!controlEval && responsesNeeded > 0) {
            model.addAttribute("notEnoughResponses", true);
            model.addAttribute("responsesNeeded", responsesNeeded);
            return "report_view";
        }

        if (!reportingPermissions.canViewEvaluationResponses(evaluation, groupIds)) {
            model.addAttribute("notAllowed", true);
            return "report_view";
        }

        boolean allEssays = VIEWMODE_ALLESSAYS.equals(viewmode);

        TemplateItemDataList tidl = responseAggregator.prepareTemplateItemDataStructure(evaluationId, groupIds);
        List<EvalTemplateItem> allTemplateItems = tidl.getAllTemplateItems();

        List<CategorySection> categorySections = new ArrayList<>();
        int displayNumber = 0;
        int totalComments = 0;
        int totalTextResponses = 0;
        int renderedItemCount = 0;

        boolean useCourseOnly = !Boolean.FALSE.equals(settings.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY));
        boolean isCurrentUserAdmin = commonLogic.isUserAdmin(currentUserId);

        for (TemplateItemGroup tig : tidl.getTemplateItemGroups()) {
            if (!ReportItemVisibility.isVisibleToViewer(evaluation, isCurrentUserAdmin,
                    currentUserId, tig.associateType, tig.associateId)
                    || !renderAny(tig.getTemplateItems(), false)) {
                continue;
            }

            EvalUser associatedUser = commonLogic.getEvalUserById(tig.associateId);
            String headerKey = null;
            String headerArg = null;
            if (EvalConstants.ITEM_CATEGORY_COURSE.equals(tig.associateType) && !useCourseOnly) {
                headerKey = "viewreport.itemlist.course";
            } else if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(tig.associateType)) {
                headerKey = "viewreport.itemlist.instructor";
                headerArg = associatedUser.displayName;
            } else if (EvalConstants.ITEM_CATEGORY_ASSISTANT.equals(tig.associateType)) {
                headerKey = "viewreport.itemlist.ta";
                headerArg = associatedUser.displayName;
            }

            List<ItemResult> sectionItems = new ArrayList<>();

            for (HierarchyNodeGroup hng : tig.hierarchyNodeGroups) {
                if (!renderAny(hng.templateItems, allEssays)) continue;

                List<DataTemplateItem> dtis = hng.getDataTemplateItems(true);
                for (DataTemplateItem dti : dtis) {
                    if (!shouldRender(dti.templateItem, allEssays)) continue;

                    ItemResult result = buildItemResult(dti, displayNumber, renderedItemCount, allEssays);
                    if (result != null) {
                        if (!"textual".equals(result.getType())) displayNumber++;
                        totalComments += result.getComments() != null ? result.getComments().size() : 0;
                        totalTextResponses += result.getResponses() != null ? result.getResponses().size() : 0;
                        sectionItems.add(result);
                        renderedItemCount++;
                    }
                }
            }

            if (!sectionItems.isEmpty()) {
                categorySections.add(new CategorySection(headerKey, headerArg, sectionItems));
            }
        }

        // Export options
        String ctxPath = request.getContextPath();
        List<ExportOption> exportOptions = buildExportOptions(evaluation, groupIds, ctxPath);
        List<IndividualPdf> individualPdfs = buildIndividualPdfs(evaluation, groupIds, ctxPath);
        boolean hasSectionedExports = exportOptions.stream()
                .anyMatch(opt -> opt.getSectionedType() != null);

        model.addAttribute("evaluation", evaluation);
        model.addAttribute("evaluationId", evaluationId);
        model.addAttribute("groupIds", groupIds);
        model.addAttribute("groupNames", responseAggregator.getCommaSeparatedGroupNames(groupIds));
        model.addAttribute("canChooseGroups", canChooseGroups);
        model.addAttribute("viewmode", viewmode);
        model.addAttribute("allEssays", allEssays);
        model.addAttribute("categorySections", categorySections);
        model.addAttribute("hasItems", !allTemplateItems.isEmpty());
        model.addAttribute("hasComments", totalComments > 0);
        model.addAttribute("hasTextResponses", totalTextResponses > 0);
        model.addAttribute("exportOptions", exportOptions);
        model.addAttribute("hasSectionedExports", hasSectionedExports);
        model.addAttribute("individualPdfs", individualPdfs);
        model.addAttribute("notAllowed", false);

        return "report_view";
    }

    // -------------------------------------------------------------------------
    // Export download handler
    // -------------------------------------------------------------------------

    @GetMapping("/download")
    public void download(@RequestParam Long evaluationId,
                         @RequestParam(required = false) String[] groupIds,
                         @RequestParam String type,
                         @RequestParam(required = false) String userId,
                         HttpServletResponse response) throws IOException {

        EvalEvaluation evaluation = evaluationService.getEvaluationById(evaluationId);
        String currentUserId = currentUserId();

        if (!reportingPermissions.canViewEvaluationResponses(evaluation, groupIds)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Enforce the same minimum-response threshold as /report_view
        if (groupIds != null && groupIds.length > 0) {
            int responsesCount = deliveryService.countResponses(evaluationId, groupIds[0], true);
            int enrollmentsCount = evaluationService.countParticipantsForEval(evaluationId, new String[]{groupIds[0]});
            int responsesNeeded = evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount);
            boolean controlEval = evaluationService.canControlEvaluation(currentUserId, evaluationId);
            if (!controlEval && responsesNeeded > 0) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        String title = sanitizeFilename(evaluation.getTitle());
        String filename;
        String contentType;
        switch (type) {
            case EvalEvaluationService.XLS_RESULTS_REPORT:
            case EvalEvaluationService.XLS_RESULTS_REPORT_SECTIONED:
                filename = title + ".xlsx";
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                break;
            case EvalEvaluationService.CSV_RESULTS_REPORT:
                filename = title + ".csv";
                contentType = "text/csv";
                break;
            case EvalEvaluationService.CSV_RESULTS_REPORT_SECTIONED:
                filename = title + ".zip";
                contentType = "application/zip";
                break;
            case EvalEvaluationService.CSV_TAKERS_REPORT:
                filename = title + "-takers.csv";
                contentType = "text/csv";
                break;
            case EvalEvaluationService.PDF_RESULTS_REPORT:
                filename = title + ".pdf";
                contentType = "application/pdf";
                break;
            case EvalEvaluationService.PDF_RESULTS_REPORT_INDIVIDUAL:
                filename = title + "Individual.pdf";
                contentType = "application/pdf";
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
        }

        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        evaluationService.exportReport(evaluation, groupIds, userId, response.getOutputStream(), type);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private ItemResult buildItemResult(DataTemplateItem dti, int displayNumber, int renderedItemCount, boolean allEssays) {
        EvalTemplateItem templateItem = dti.templateItem;
        String type = TemplateItemUtils.getTemplateItemType(templateItem);
        List<EvalAnswer> answers = dti.getAnswers();
        boolean odd = renderedItemCount % 2 != 0;

        if (EvalConstants.ITEM_TYPE_SCALED.equals(type)
                || EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(type)
                || EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(type)
                || EvalConstants.ITEM_TYPE_BLOCK_CHILD.equals(type)) {

            int totalResponses = answers.size();
            List<ChoiceRow> choices = new ArrayList<>();
            AnswersMean mean = null;

            if (!allEssays) {
                EvalScale scale = templateItem.getItem().getScale();
                List<String> opts = scale.getOptions();
                List<String> labels = RenderingUtils.makeReportingScaleLabels(templateItem, opts);
                int[] counts = TemplateItemDataList.getAnswerChoicesCounts(type, opts.size(), answers);

                for (int x = 0; x < labels.size(); x++) {
                    choices.add(new ChoiceRow(labels.get(x), counts[x], makePercent(counts[x], totalResponses), false));
                }
                if (Boolean.TRUE.equals(templateItem.getUsesNA())) {
                    int naCount = counts[counts.length - 1];
                    choices.add(new ChoiceRow("reporting.notapplicable.longlabel", naCount, makePercent(naCount, totalResponses), true));
                }
                mean = RenderingUtils.calculateAnswersMean(counts, opts, templateItem.getUsesNA());
            }

            boolean usesComments = dti.usesComments();
            List<CommentRow> comments = new ArrayList<>();
            if (usesComments) {
                int n = 0;
                for (String c : dti.getComments()) {
                    comments.add(new CommentRow(n + 1, c, n % 2 != 0));
                    n++;
                }
            }

            return new ItemResult("scaled", displayNumber + 1, templateItem.getItem().getItemText(),
                    totalResponses, choices, mean, comments, usesComments, null, 0,
                    Boolean.TRUE.equals(templateItem.getUsesNA()), odd);

        } else if (EvalConstants.ITEM_TYPE_TEXT.equals(type)) {

            List<ResponseRow> responses = new ArrayList<>();
            int naCount = 0;
            int n = 0;
            for (EvalAnswer answer : answers) {
                if (answer.NA) {
                    naCount++;
                } else if (!EvalUtils.isBlank(answer.getText())) {
                    responses.add(new ResponseRow(n + 1, answer.getText(), n % 2 != 0));
                    n++;
                }
            }
            return new ItemResult("essay", displayNumber + 1, templateItem.getItem().getItemText(),
                    answers.size(), null, null, null, false, responses, naCount,
                    Boolean.TRUE.equals(templateItem.getUsesNA()), odd);

        } else if (EvalConstants.ITEM_TYPE_HEADER.equals(type)
                || EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(type)) {

            return new ItemResult("textual", 0, templateItem.getItem().getItemText(),
                    0, null, null, null, false, null, 0, false, odd);
        }

        log.warn("Skipped unknown item type ({}): TI: {}", type, templateItem.getId());
        return null;
    }

    private List<ExportOption> buildExportOptions(EvalEvaluation evaluation, String[] groupIds, String ctxPath) {
        List<ExportOption> options = new ArrayList<>();
        if (Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_XLS_REPORT_EXPORT))) {
            options.add(new ExportOption(
                    EvalEvaluationService.XLS_RESULTS_REPORT,
                    EvalEvaluationService.XLS_RESULTS_REPORT_SECTIONED,
                    "viewreport.view.xls",
                    buildDownloadUrl(ctxPath, evaluation.getId(), groupIds, EvalEvaluationService.XLS_RESULTS_REPORT, null),
                    buildDownloadUrl(ctxPath, evaluation.getId(), groupIds, EvalEvaluationService.XLS_RESULTS_REPORT_SECTIONED, null)));
        }
        if (Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_CSV_REPORT_EXPORT))) {
            options.add(new ExportOption(
                    EvalEvaluationService.CSV_RESULTS_REPORT,
                    EvalEvaluationService.CSV_RESULTS_REPORT_SECTIONED,
                    "viewreport.view.csv",
                    buildDownloadUrl(ctxPath, evaluation.getId(), groupIds, EvalEvaluationService.CSV_RESULTS_REPORT, null),
                    buildDownloadUrl(ctxPath, evaluation.getId(), groupIds, EvalEvaluationService.CSV_RESULTS_REPORT_SECTIONED, null)));
        }
        if (Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_PDF_REPORT_EXPORT))) {
            options.add(new ExportOption(
                    EvalEvaluationService.PDF_RESULTS_REPORT,
                    null,
                    "viewreport.view.pdf",
                    buildDownloadUrl(ctxPath, evaluation.getId(), groupIds, EvalEvaluationService.PDF_RESULTS_REPORT, null),
                    null));
        }
        if (Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_LIST_OF_TAKERS_EXPORT))) {
            options.add(new ExportOption(
                    EvalEvaluationService.CSV_TAKERS_REPORT,
                    null,
                    "viewreport.view.listofevaluationtakers",
                    buildDownloadUrl(ctxPath, evaluation.getId(), groupIds, EvalEvaluationService.CSV_TAKERS_REPORT, null),
                    null));
        }
        return options;
    }

    private List<IndividualPdf> buildIndividualPdfs(EvalEvaluation evaluation, String[] groupIds, String ctxPath) {
        List<IndividualPdf> list = new ArrayList<>();
        if (!Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_PDF_REPORT_EXPORT))) return list;
        String title = sanitizeFilename(evaluation.getTitle());
        List<EvalAssignUser> evaluatees = evaluationService.getParticipantsForEval(
                evaluation.getId(), null, null, EvalAssignUser.TYPE_EVALUATEE, null, null, null);
        evaluatees.addAll(evaluationService.getParticipantsForEval(
                evaluation.getId(), null, null, EvalAssignUser.TYPE_ASSISTANT, null, null, null));
        List<String> seen = new ArrayList<>();
        for (EvalAssignUser eu : evaluatees) {
            if (!seen.contains(eu.getUserId())) {
                EvalUser user = commonLogic.getEvalUserById(eu.getUserId());
                list.add(new IndividualPdf(eu.getUserId(), user.displayName, title + "Individual.pdf",
                        buildDownloadUrl(ctxPath, evaluation.getId(), groupIds,
                                EvalEvaluationService.PDF_RESULTS_REPORT_INDIVIDUAL, eu.getUserId())));
                seen.add(eu.getUserId());
            }
        }
        return list;
    }

    private String buildDownloadUrl(String ctxPath, Long evaluationId, String[] groupIds,
                                    String type, String userId) {
        StringBuilder url = new StringBuilder(ctxPath)
                .append("/report_view/download?evaluationId=").append(evaluationId);
        if (groupIds != null) {
            for (String gid : groupIds) {
                url.append("&groupIds=").append(URLEncoder.encode(gid, StandardCharsets.UTF_8));
            }
        }
        url.append("&type=").append(URLEncoder.encode(type, StandardCharsets.UTF_8));
        if (userId != null) {
            url.append("&userId=").append(URLEncoder.encode(userId, StandardCharsets.UTF_8));
        }
        return url.toString();
    }

    private String sanitizeFilename(String title) {
        if (title.length() > EvalToolConstants.EVAL_REPORTING_MAX_NAME_LENGTH) {
            title = title.substring(0, EvalToolConstants.EVAL_REPORTING_MAX_NAME_LENGTH);
        }
        return Validator.escapeZipEntry(title);
    }

    private boolean renderAny(List<EvalTemplateItem> items, boolean allEssays) {
        for (EvalTemplateItem ti : items) {
            if (shouldRender(ti, allEssays)) return true;
        }
        return false;
    }

    private boolean shouldRender(EvalTemplateItem ti, boolean allEssays) {
        if (!allEssays) return true;
        String type = TemplateItemUtils.getTemplateItemType(ti);
        return EvalConstants.ITEM_TYPE_TEXT.equals(type) || Boolean.TRUE.equals(ti.getUsesComment());
    }

    private String makePercent(int value, int total) {
        return total > 0 ? String.format("%.0f", (float) (value * 100.0 / total)) : "0";
    }
}
