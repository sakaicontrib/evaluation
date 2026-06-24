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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.exceptions.ResponseSaveException;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.dao.EvalDaoInvoker;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.utils.ScaledUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.HierarchyNodeGroup;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.TemplateItemGroup;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContextUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * View for completing an evaluation.
 * Spring MVC equivalent of TakeEvalProducer.
 */
@Slf4j
@Controller
@RequestMapping("/take_eval")
public class TakeEvalController {

    // ---- DTOs ----------------------------------------------------------------

    @Data
    public static class OptionData {
        int index;
        String value;
        String label;
        String matrixLegend;
        String matrixLegendAlign;
        boolean selected;
    }

    @Data
    public static class SteppedRow {
        String label;
        int middleCount;
        String value;
    }

    /** Item data for rendering and form binding */
    @Data
    public static class FormItemData {
        // Item metadata
        String itemType;
        String itemText;
        int displayNumber;
        int displayRows;
        boolean usesNA;
        boolean usesComment;
        boolean compulsory;

        // Scale rendering
        String scaleDisplaySetting;
        List<OptionData> options;
        String startLabel;
        String endLabel;
        String startClass;
        String endClass;
        String idealImageUrl;
        String matrixLabelStart;
        String matrixLabelEnd;
        String matrixLabelMiddle;
        List<SteppedRow> steppedRows;
        List<FormItemData> childItems;

        // Form binding (for take_eval)
        int answerIndex;           // position in answers[] list
        Long templateItemId;
        Long existingAnswerId;     // null if new answer
        String associatedId;       // null for course items
        String associatedType;     // null for course items

        // Current answer values (pre-fill form)
        Integer currentNumeric;
        String currentText;
        boolean currentNA;
        String currentComment;
        List<Integer> currentMultipleAnswers;

        boolean odd;

        // Helper: unique input name suffix for this item
        public String getInputKey() {
            return associatedId != null
                    ? templateItemId + "_" + associatedId.replaceAll("[^a-zA-Z0-9]", "_")
                    : String.valueOf(templateItemId);
        }
    }

    /** Wrapper for indexed form submission */
    @Data
    public static class EvalFormWrapper {
        private List<AnswerSubmission> answers = new ArrayList<>();

        @Data
        public static class AnswerSubmission {
            Long templateItemId;
            Long existingAnswerId;
            String associatedId;
            String associatedType;
            String itemType;
            Integer numeric;
            String text;
            List<String> multipleAnswers = new ArrayList<>();
            boolean na;
            String comment;
        }
    }

    @Data
    public static class NodeGroupData {
        String nodeTitle;
        List<FormItemData> items = new ArrayList<>();
    }

    @Data
    public static class CategorySection {
        String header;
        String associateId;
        String associateType;
        List<NodeGroupData> nodeGroups = new ArrayList<>();
    }

    // ---- Services ------------------------------------------------------------

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationService")
    private EvalEvaluationService evaluationService;

    @Resource(name = "org.sakaiproject.evaluation.dao.EvalDaoInvoker")
    private EvalDaoInvoker daoInvoker;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    private EvalAuthoringService authoringService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalDeliveryService")
    private EvalDeliveryService deliveryService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalSettings")
    private EvalSettings evalSettings;

    @Resource(name = "org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic")
    private ExternalHierarchyLogic hierarchyLogic;

    @Autowired
    private MessageSource messageSource;

    // ---- GET -----------------------------------------------------------------

    @GetMapping
    public String show(
            @RequestParam Long evaluationId,
            @RequestParam(required = false) String evalGroupId,
            @RequestParam(required = false) Long responseId,
            @RequestParam(required = false, defaultValue = "false") boolean external,
            @RequestParam(required = false) String error,
            Model model,
            HttpServletRequest request) {

        Locale locale = RequestContextUtils.getLocale(request);
        String currentUserId = commonLogic.getCurrentUserId();

        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        if (eval == null) {
            throw new IllegalArgumentException("Invalid evaluationId: " + evaluationId);
        }

        model.addAttribute("eval", eval);
        model.addAttribute("external", external);
        model.addAttribute("evaluationId", evaluationId);

        // Check eval state
        String evalState = evaluationService.returnAndFixEvalState(eval, true);
        if (EvalUtils.checkStateBefore(evalState, EvalConstants.EVALUATION_STATE_ACTIVE, false)) {
            model.addAttribute("cannotTakeMessage",
                    messageSource.getMessage("takeeval.eval.not.open", null, locale));
            return "take_eval";
        } else if (EvalUtils.checkStateAfter(evalState, EvalConstants.EVALUATION_STATE_CLOSED, true)) {
            model.addAttribute("cannotTakeMessage",
                    messageSource.getMessage("takeeval.eval.closed", null, locale));
            return "take_eval";
        }

        // Determine valid groups and access
        boolean userCanAccess = false;
        List<EvalGroup> validGroups = new ArrayList<>();

        Map<Long, List<EvalAssignGroup>> assignGroupsMap = evaluationService
                .getAssignGroupsForEvals(new Long[]{evaluationId}, true, null);
        List<EvalAssignGroup> allAssignGroups = assignGroupsMap.get(evaluationId);

        if (!commonLogic.isUserAnonymous(currentUserId) && commonLogic.isUserAdmin(currentUserId)) {
            userCanAccess = true;
            for (EvalAssignGroup ag : allAssignGroups) {
                if (evalGroupId == null) evalGroupId = ag.getEvalGroupId();
                validGroups.add(commonLogic.makeEvalGroupObject(ag.getEvalGroupId()));
            }
        } else {
            EvalGroup[] candidates;
            if (EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(eval.getAuthControl())) {
                candidates = new EvalGroup[allAssignGroups.size()];
                for (int i = 0; i < allAssignGroups.size(); i++) {
                    candidates[i] = commonLogic.makeEvalGroupObject(allAssignGroups.get(i).getEvalGroupId());
                }
            } else {
                List<EvalAssignUser> userAssignments = evaluationService.getParticipantsForEval(
                        evaluationId, currentUserId, null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
                Set<String> groupIds = EvalUtils.getGroupIdsFromUserAssignments(userAssignments);
                List<EvalGroup> groups = EvalUtils.makeGroupsFromGroupsIds(groupIds, commonLogic);
                candidates = EvalUtils.getGroupsInCommon(groups, allAssignGroups);
            }
            for (EvalGroup g : candidates) {
                if (evaluationService.canTakeEvaluation(currentUserId, evaluationId, g.evalGroupId)) {
                    if (evalGroupId == null) {
                        evalGroupId = g.evalGroupId;
                    }
                    userCanAccess = true;
                    validGroups.add(commonLogic.makeEvalGroupObject(g.evalGroupId));
                }
            }
        }

        if (!userCanAccess) {
            EvalUser current = commonLogic.getEvalUserById(currentUserId);
            model.addAttribute("cannotTakeMessage",
                    messageSource.getMessage("takeeval.user.cannot.take",
                            new Object[]{current.displayName, current.email, current.username}, locale));
            return "take_eval";
        }

        model.addAttribute("evalGroupId", evalGroupId);

        // Multiple groups selector
        if (validGroups.size() > 1) {
            List<String> groupValues = new ArrayList<>();
            List<String> groupLabels = new ArrayList<>();
            for (EvalGroup g : validGroups) {
                groupValues.add(g.evalGroupId);
                groupLabels.add(g.title);
            }
            model.addAttribute("groupValues", groupValues);
            model.addAttribute("groupLabels", groupLabels);
            model.addAttribute("showSwitchGroup", true);
        } else {
            model.addAttribute("showSwitchGroup", false);
        }

        // Group title
        EvalGroup evalGroup = commonLogic.makeEvalGroupObject(evalGroupId);
        model.addAttribute("groupTitle", evalGroup.title);

        // Load or create response
        EvalResponse response = null;
        if (responseId != null) {
            response = deliveryService.getResponseById(responseId);
        } else {
            response = evaluationService.getResponseForUserAndGroup(evaluationId, currentUserId, evalGroupId);
            if (response != null) {
                responseId = response.getId();
            }
        }
        model.addAttribute("responseId", responseId);

        // Load existing answers
        Map<String, EvalAnswer> answerMap = new HashMap<>();
        if (responseId != null && response != null) {
            answerMap = EvalUtils.getAnswersMapByTempItemAndAssociated(response);
            if (!response.complete) {
                model.addAttribute("savedWarning",
                        messageSource.getMessage("takeeval.saved.warning", null, locale));
            }
        }

        // Submission error from a previous POST attempt
        if ("missing_required".equals(error)) {
            model.addAttribute("validationError",
                    messageSource.getMessage("takeeval.user.must.answer.all.exception", null, locale));
        } else if ("blank_response".equals(error)) {
            model.addAttribute("validationError",
                    messageSource.getMessage("takeeval.user.blank.response.exception", null, locale));
        } else if ("savefailed".equals(error)) {
            model.addAttribute("validationError",
                    messageSource.getMessage("takeeval.user.cannot.save.reponse", null, locale));
        }

        // Settings
        Boolean studentAllowedLeaveUnanswered = (Boolean) evalSettings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED);
        if (studentAllowedLeaveUnanswered == null) {
            studentAllowedLeaveUnanswered = EvalUtils.safeBool(eval.getBlankResponsesAllowed(), false);
        }
        model.addAttribute("mustAnswerAll", !studentAllowedLeaveUnanswered);

        Boolean saveWithoutSubmit = (Boolean) evalSettings.get(EvalSettings.STUDENT_SAVE_WITHOUT_SUBMIT);
        Boolean cancelAllowed = (Boolean) evalSettings.get(EvalSettings.STUDENT_CANCEL_ALLOWED);
        Boolean useCourseOnly = Boolean.TRUE.equals(evalSettings.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY));
        Boolean showHierHeaders = Boolean.TRUE.equals(evalSettings.get(EvalSettings.DISPLAY_HIERARCHY_HEADERS));
        model.addAttribute("saveWithoutSubmit", Boolean.TRUE.equals(saveWithoutSubmit) && (response == null || !response.complete));
        model.addAttribute("cancelAllowed", Boolean.TRUE.equals(cancelAllowed));

        // Build TIDL and item DTO list
        TemplateItemDataList tidl = new TemplateItemDataList(evaluationId, evalGroupId,
                evaluationService, authoringService, hierarchyLogic, null);

        List<CategorySection> sections = new ArrayList<>();
        EvalFormWrapper formWrapper = new EvalFormWrapper();
        int displayNumber = 1;
        int answerIndex = 0;

        for (TemplateItemGroup tig : tidl.getTemplateItemGroups()) {
            CategorySection section = new CategorySection();
            section.setAssociateType(tig.associateType);
            section.setAssociateId(tig.associateId);

            if (!useCourseOnly) {
                if (EvalConstants.ITEM_CATEGORY_COURSE.equals(tig.associateType)) {
                    section.setHeader(messageSource.getMessage("takeeval.group.questions.header", null, locale));
                } else if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(tig.associateType)) {
                    EvalUser user = commonLogic.getEvalUserById(tig.associateId);
                    section.setHeader(messageSource.getMessage("takeeval.instructor.questions.header",
                            new Object[]{user.displayName}, locale));
                } else if (EvalConstants.ITEM_CATEGORY_ASSISTANT.equals(tig.associateType)) {
                    EvalUser user = commonLogic.getEvalUserById(tig.associateId);
                    section.setHeader(messageSource.getMessage("takeeval.assistant.questions.header",
                            new Object[]{user.displayName}, locale));
                } else {
                    section.setHeader("");
                }
            } else {
                section.setHeader("");
            }

            for (HierarchyNodeGroup hng : tig.hierarchyNodeGroups) {
                NodeGroupData ngd = new NodeGroupData();
                if (hng.node != null && showHierHeaders) {
                    ngd.setNodeTitle(hng.node.title);
                }

                List<DataTemplateItem> dtis = hng.getDataTemplateItems(false);
                int rowIndex = 0;
                for (DataTemplateItem dti : dtis) {
                    EvalTemplateItem ti = dti.templateItem;
                    String type = TemplateItemUtils.getTemplateItemType(ti);

                    if (TemplateItemUtils.isAnswerable(ti)) {
                        // Answerable item — create submission slot
                        EvalFormWrapper.AnswerSubmission slot = new EvalFormWrapper.AnswerSubmission();
                        slot.setTemplateItemId(ti.getId());
                        slot.setAssociatedId(dti.associateId);
                        slot.setAssociatedType(dti.associateId != null ? ti.getCategory() : null);
                        slot.setItemType(type);

                        // Pre-fill from existing answer
                        String key = TemplateItemUtils.makeTemplateItemAnswerKey(
                                ti.getId(), slot.getAssociatedType(), dti.associateId);
                        EvalAnswer existing = answerMap.get(key);
                        if (existing != null) {
                            slot.setExistingAnswerId(existing.getId());
                            slot.setNumeric(existing.getNumeric());
                            slot.setText(existing.getText());
                            slot.setNa(EvalUtils.decodeAnswerNA(existing));
                            slot.setComment(existing.getComment());
                            if (existing.multipleAnswers != null) {
                                List<String> maList = new ArrayList<>();
                                for (Integer v : existing.multipleAnswers) {
                                    if (v != null) maList.add(String.valueOf(v));
                                }
                                slot.setMultipleAnswers(maList);
                            }
                        }
                        formWrapper.getAnswers().add(slot);

                        FormItemData fid = buildFormItemData(ti, displayNumber, dti.associateId,
                                slot.getAssociatedType(), answerIndex, existing, locale);
                        fid.setOdd(rowIndex % 2 != 0);
                        ngd.getItems().add(fid);
                        answerIndex++;
                        displayNumber++;

                    } else if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(type)) {
                        // Block parent: add slots for all children
                        List<EvalTemplateItem> children = dti.blockChildItems != null
                                ? dti.blockChildItems : new ArrayList<>();

                        FormItemData parentFid = buildFormItemData(ti, displayNumber, null, null,
                                -1, null, locale);
                        parentFid.setOdd(rowIndex % 2 != 0);
                        List<FormItemData> childFids = new ArrayList<>();

                        for (int ci = 0; ci < children.size(); ci++) {
                            EvalTemplateItem child = children.get(ci);
                            EvalFormWrapper.AnswerSubmission slot = new EvalFormWrapper.AnswerSubmission();
                            slot.setTemplateItemId(child.getId());
                            slot.setAssociatedId(dti.associateId);
                            slot.setAssociatedType(dti.associateId != null ? child.getCategory() : null);
                            slot.setItemType(TemplateItemUtils.getTemplateItemType(child));

                            String key = TemplateItemUtils.makeTemplateItemAnswerKey(
                                    child.getId(), slot.getAssociatedType(), dti.associateId);
                            EvalAnswer existing = answerMap.get(key);
                            if (existing != null) {
                                slot.setExistingAnswerId(existing.getId());
                                slot.setNumeric(existing.getNumeric());
                                slot.setNa(EvalUtils.decodeAnswerNA(existing));
                                slot.setComment(existing.getComment());
                            }
                            formWrapper.getAnswers().add(slot);

                            FormItemData childFid = buildFormItemData(child, displayNumber + ci,
                                    dti.associateId, slot.getAssociatedType(), answerIndex, existing, locale);
                            childFids.add(childFid);
                            answerIndex++;
                        }
                        parentFid.setChildItems(childFids);
                        ngd.getItems().add(parentFid);
                        displayNumber += children.size();

                    } else {
                        // Header or non-answerable — no form slot
                        FormItemData fid = buildFormItemData(ti, displayNumber, null, null, -1, null, locale);
                        fid.setOdd(rowIndex % 2 != 0);
                        ngd.getItems().add(fid);
                    }
                    rowIndex++;
                }
                section.getNodeGroups().add(ngd);
            }
            sections.add(section);
        }

        model.addAttribute("categorySections", sections);
        model.addAttribute("evalForm", formWrapper);
        return "take_eval";
    }

    // ---- POST ----------------------------------------------------------------

    @PostMapping
    public String submit(
            @RequestParam Long evaluationId,
            @RequestParam String evalGroupId,
            @RequestParam(required = false) Long responseId,
            @RequestParam(required = false) String actionSave,
            @RequestParam(required = false) String actionSubmit,
            @ModelAttribute("evalForm") EvalFormWrapper formWrapper) {

        String currentUserId = commonLogic.getCurrentUserId();
        // actionSubmit is non-null only when the submit button was clicked (not save)
        boolean submit = (actionSubmit != null);

        // Step 1: Always save answers without endTime in its own transaction.
        // This commits immediately so we always have a responseId we can include in
        // any subsequent error redirect, allowing the form to reload with saved answers.
        Long[] savedId = {responseId};
        try {
            daoInvoker.invokeTransactionalAccess(() -> {
                EvalResponse response;
                if (savedId[0] != null) {
                    response = deliveryService.getResponseById(savedId[0]);
                } else {
                    // No responseId in the post (e.g. a stale/cached form re-submitted without
                    // it) — reuse the existing response for this user/eval/group if there is one,
                    // same lookup show() does, instead of unconditionally creating a new one.
                    // Creating a second EvalResponse here violates the one-response-per-user/
                    // eval/group invariant and permanently locks the user out: every later
                    // canTakeEvaluation() call throws IllegalStateException via
                    // getResponseForUserAndGroup().
                    response = evaluationService.getResponseForUserAndGroup(evaluationId, currentUserId, evalGroupId);
                    if (response == null) {
                        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
                        response = new EvalResponse(currentUserId, evalGroupId, eval, new Date());
                    }
                }

                response.setAnswers(buildAnswers(response, formWrapper));
                // No endTime set here — partial save, responseComplete=false
                deliveryService.saveResponse(response, currentUserId);
                savedId[0] = response.getId();
            });
        } catch (Exception e) {
            log.error("Error saving partial response for evaluation {}: {}", evaluationId, e.getMessage(), e);
            return "redirect:/take_eval?evaluationId=" + evaluationId
                    + "&evalGroupId=" + evalGroupId
                    + (responseId != null ? "&responseId=" + responseId : "")
                    + "&error=savefailed";
        }

        if (!submit) {
            // Save-only: return to form so user can see saved progress
            return "redirect:/take_eval?evaluationId=" + evaluationId
                    + "&evalGroupId=" + evalGroupId
                    + "&responseId=" + savedId[0];
        }

        // Step 2: Mark response complete in a separate transaction from step 1.
        // If validation fails (required answers missing), step 1 has already committed,
        // so the redirect includes responseId and the form reloads with the saved answers.
        Long finalId = savedId[0];
        try {
            daoInvoker.invokeTransactionalAccess(() -> {
                EvalResponse response = deliveryService.getResponseById(finalId);
                // Decode multiAnswerCode into the transient multipleAnswers field so
                // saveResponse's cleanup re-encodes them correctly rather than losing them.
                for (EvalAnswer answer : response.getAnswers()) {
                    String mac = answer.getMultiAnswerCode();
                    if (mac != null && !mac.isEmpty()) {
                        answer.multipleAnswers = EvalUtils.decodeMultipleAnswers(mac);
                    }
                }
                response.setEndTime(new Date());
                deliveryService.saveResponse(response, currentUserId);
            });
        } catch (ResponseSaveException e) {
            String errorCode = ResponseSaveException.TYPE_BLANK_RESPONSE.equals(e.type)
                    ? "blank_response" : "missing_required";
            log.warn("Submission rejected for response {}: {}", finalId, e.getMessage());
            return "redirect:/take_eval?evaluationId=" + evaluationId
                    + "&evalGroupId=" + evalGroupId
                    + "&responseId=" + finalId
                    + "&error=" + errorCode;
        } catch (Exception e) {
            log.error("Error completing response {}: {}", finalId, e.getMessage(), e);
            return "redirect:/take_eval?evaluationId=" + evaluationId
                    + "&evalGroupId=" + evalGroupId
                    + "&responseId=" + finalId
                    + "&error=savefailed";
        }

        return "redirect:/summary";
    }

    /** Builds the answer set from the submitted form data. */
    private Set<EvalAnswer> buildAnswers(EvalResponse response, EvalFormWrapper formWrapper) {
        Set<EvalAnswer> answers = new HashSet<>();
        for (EvalFormWrapper.AnswerSubmission sub : formWrapper.getAnswers()) {
            if (sub.getTemplateItemId() == null) continue;

            EvalTemplateItem ti = authoringService.getTemplateItemById(sub.getTemplateItemId());
            if (ti == null) continue;

            EvalAnswer answer;
            if (sub.getExistingAnswerId() != null) {
                answer = findAnswerInResponse(response, sub.getExistingAnswerId());
                if (answer == null) {
                    answer = new EvalAnswer(response, ti, ti.getItem());
                }
            } else {
                answer = new EvalAnswer(response, ti, ti.getItem());
            }

            // Normalize empty strings to null: th:value="${null}" renders as "" in HTML,
            // so blank form values must be treated as null to match the answer map keys
            // built by makeTemplateItemAnswerKey (which uses null, not "").
            String assocId = sub.getAssociatedId();
            answer.setAssociatedId(assocId != null && !assocId.isEmpty() ? assocId : null);
            String assocType = sub.getAssociatedType();
            answer.setAssociatedType(assocType != null && !assocType.isEmpty() ? assocType : null);

            String type = sub.getItemType();
            if (EvalConstants.ITEM_TYPE_TEXT.equals(type)) {
                answer.setText(sub.getText());
            } else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(type)) {
                if (sub.isNa()) {
                    answer.setNumeric(EvalConstants.NA_VALUE);
                    // Clear any stale multiple-answer state from a previous save.
                    answer.multipleAnswers = null;
                    answer.setMultiAnswerCode(null);
                } else if (sub.getMultipleAnswers() != null && !sub.getMultipleAnswers().isEmpty()) {
                    Integer[] vals = sub.getMultipleAnswers().stream()
                            .filter(s -> s != null && !s.isEmpty())
                            .map(Integer::parseInt)
                            .toArray(Integer[]::new);
                    answer.multipleAnswers = vals;
                    answer.setMultiAnswerCode(EvalUtils.encodeMultipleAnswers(vals));
                    // Clear stale NA_VALUE if the user switched from N/A back to a selection.
                    answer.setNumeric(null);
                } else {
                    // All checkboxes deselected: clear everything.
                    answer.setNumeric(null);
                    answer.multipleAnswers = null;
                    answer.setMultiAnswerCode(null);
                }
            } else {
                if (sub.isNa()) {
                    answer.setNumeric(EvalConstants.NA_VALUE);
                } else {
                    answer.setNumeric(sub.getNumeric());
                }
            }
            answer.setComment(sub.getComment());
            answers.add(answer);
        }
        return answers;
    }

    // ---- Item building -------------------------------------------------------

    private FormItemData buildFormItemData(EvalTemplateItem ti, int displayNumber,
            String associatedId, String associatedType, int answerIndex,
            EvalAnswer existing, Locale locale) {

        FormItemData d = new FormItemData();
        String type = TemplateItemUtils.getTemplateItemType(ti);
        d.setItemType(type);
        d.setItemText(ti.getItem().getItemText());
        d.setDisplayNumber(displayNumber);
        d.setUsesNA(Boolean.TRUE.equals(ti.getUsesNA()));
        d.setUsesComment(Boolean.TRUE.equals(ti.getUsesComment()));
        d.setCompulsory(Boolean.TRUE.equals(ti.isCompulsory()));
        d.setAnswerIndex(answerIndex);
        d.setTemplateItemId(ti.getId());
        d.setAssociatedId(associatedId);
        d.setAssociatedType(associatedType);

        if (existing != null) {
            d.setExistingAnswerId(existing.getId());
            d.setCurrentNumeric(existing.getNumeric());
            d.setCurrentText(existing.getText());
            d.setCurrentNA(EvalUtils.decodeAnswerNA(existing));
            d.setCurrentComment(existing.getComment());
            if (existing.multipleAnswers != null) {
                List<Integer> maList = new ArrayList<>();
                for (Integer v : existing.multipleAnswers) {
                    if (v != null) maList.add(v);
                }
                d.setCurrentMultipleAnswers(maList);
            }
        }

        if (EvalConstants.ITEM_TYPE_TEXT.equals(type)) {
            d.setDisplayRows(ti.getDisplayRows() != null ? ti.getDisplayRows() : 3);

        } else if (EvalConstants.ITEM_TYPE_SCALED.equals(type)) {
            populateScaleData(d, ti);

        } else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(type)
                || EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(type)) {
            populateChoiceData(d, ti);

        } else if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(type)) {
            populateScaleData(d, ti);
        }

        return d;
    }

    private void populateScaleData(FormItemData d, EvalTemplateItem ti) {
        EvalScale scale = ti.getItem().getScale();
        List<String> rawOptions = scale.getOptions();
        int n = rawOptions.size();

        String ds = ti.getScaleDisplaySetting();
        if (ds == null) ds = EvalConstants.ITEM_SCALE_DISPLAY_FULL;
        d.setScaleDisplaySetting(ds);

        boolean isColored = EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED.equals(ds)
                || EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED.equals(ds)
                || EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED.equals(ds)
                || EvalConstants.ITEM_SCALE_DISPLAY_MATRIX_COLORED.equals(ds);
        boolean isCompact = EvalConstants.ITEM_SCALE_DISPLAY_COMPACT.equals(ds)
                || EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED.equals(ds);
        boolean isStepped = EvalConstants.ITEM_SCALE_DISPLAY_STEPPED.equals(ds)
                || EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED.equals(ds);
        boolean isMatrix = EvalConstants.ITEM_SCALE_DISPLAY_MATRIX.equals(ds)
                || EvalConstants.ITEM_SCALE_DISPLAY_MATRIX_COLORED.equals(ds);

        if (isColored) {
            d.setIdealImageUrl(resolveContextUrl(EvalToolConstants.COLORED_IMAGE_URLS[ScaledUtils.idealIndex(scale)]));
        }

        if (isCompact) {
            d.setStartLabel(rawOptions.get(0));
            d.setEndLabel(rawOptions.get(n - 1));
            if (isColored) {
                d.setStartClass(ScaledUtils.getStartClass(scale));
                d.setEndClass(ScaledUtils.getEndClass(scale));
            }
            List<OptionData> opts = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                OptionData o = new OptionData();
                o.setIndex(j); o.setValue(String.valueOf(j)); o.setLabel(" ");
                opts.add(o);
            }
            d.setOptions(opts);

        } else if (isStepped) {
            List<SteppedRow> rows = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                SteppedRow row = new SteppedRow();
                row.setLabel(rawOptions.get(n - 1 - j));
                row.setMiddleCount(j);
                row.setValue(String.valueOf(n - 1 - j));
                rows.add(row);
            }
            d.setSteppedRows(rows);
            List<OptionData> opts = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                OptionData o = new OptionData();
                o.setIndex(j); o.setValue(String.valueOf(n - 1 - j)); o.setLabel(rawOptions.get(n - 1 - j));
                opts.add(o);
            }
            d.setOptions(opts);

        } else if (isMatrix) {
            List<String> headers = RenderingUtils.getMatrixLabels(rawOptions);
            d.setMatrixLabelStart(headers.get(0));
            d.setMatrixLabelEnd(headers.get(1));
            if (headers.size() >= 3) d.setMatrixLabelMiddle(headers.get(2));
            boolean numericScale = RenderingUtils.isNumericScale(rawOptions);
            int middleIndex = n > 4 ? (n - 1) / 2 : -1;
            List<OptionData> opts = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                OptionData o = new OptionData();
                o.setIndex(j); o.setValue(String.valueOf(n - 1 - j)); o.setLabel(String.valueOf(j + 1));
                if (!numericScale) {
                    if (j == 0) { o.setMatrixLegend(headers.get(0)); o.setMatrixLegendAlign("left"); }
                    else if (j == n - 1) { o.setMatrixLegend(headers.get(1)); o.setMatrixLegendAlign("right"); }
                    else if (j == middleIndex) { o.setMatrixLegend(headers.get(2)); o.setMatrixLegendAlign("center"); }
                }
                opts.add(o);
            }
            d.setOptions(opts);

        } else {
            List<OptionData> opts = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                OptionData o = new OptionData();
                o.setIndex(j); o.setValue(String.valueOf(j)); o.setLabel(rawOptions.get(j));
                opts.add(o);
            }
            d.setOptions(opts);
        }
    }

    private void populateChoiceData(FormItemData d, EvalTemplateItem ti) {
        EvalScale scale = ti.getItem().getScale();
        List<String> rawOptions = scale.getOptions();
        String ds = ti.getScaleDisplaySetting();
        if (ds == null) ds = EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL;
        d.setScaleDisplaySetting(ds);
        d.setUsesNA(Boolean.TRUE.equals(ti.getUsesNA()));
        List<OptionData> opts = new ArrayList<>();
        for (int j = 0; j < rawOptions.size(); j++) {
            OptionData o = new OptionData();
            o.setIndex(j); o.setValue(String.valueOf(j)); o.setLabel(rawOptions.get(j));
            o.setSelected(d.getCurrentMultipleAnswers() != null && d.getCurrentMultipleAnswers().contains(j));
            opts.add(o);
        }
        d.setOptions(opts);
    }

    private String resolveContextUrl(String url) {
        return url.replace("$context", "");
    }

    private EvalAnswer findAnswerInResponse(EvalResponse response, Long answerId) {
        if (response.getAnswers() == null) return null;
        for (EvalAnswer a : response.getAnswers()) {
            if (answerId.equals(a.getId())) return a;
        }
        return null;
    }
}