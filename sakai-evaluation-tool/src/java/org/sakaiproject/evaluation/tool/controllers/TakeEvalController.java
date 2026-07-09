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

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.exceptions.ResponseSaveException;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.utils.EvalItemViewData;
import org.sakaiproject.evaluation.tool.utils.EvalItemViewDataBuilder;
import org.sakaiproject.evaluation.tool.utils.ScaleOptionsBuilder;
import org.sakaiproject.evaluation.tool.utils.ScaleOptionsBuilder.OptionData;
import org.sakaiproject.evaluation.tool.utils.ScaleOptionsBuilder.SteppedRow;
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
public class TakeEvalController extends EvalControllerSupport {

    // ---- DTOs ----------------------------------------------------------------

    /** Item data for rendering and form binding */
    public static class FormItemData extends EvalItemViewDataBuilder.TakeEvalItemData {
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

    private static class TakeEvalAccess {
        boolean userCanAccess;
        String evalGroupId;
        List<EvalGroup> validGroups = new ArrayList<>();
    }

    private static class ResponseState {
        Long responseId;
        EvalResponse response;
        Map<String, EvalAnswer> answerMap = new HashMap<>();
    }

    private static class TakeEvalFormModel {
        List<CategorySection> sections = new ArrayList<>();
        EvalFormWrapper formWrapper = new EvalFormWrapper();
    }

    // ---- Services ------------------------------------------------------------


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
        String currentUserId = currentUserId();

        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        if (eval == null) {
            throw new IllegalArgumentException("Invalid evaluationId: " + evaluationId);
        }

        addBaseModel(model, eval, external, evaluationId);
        if (addEvaluationUnavailableMessage(eval, locale, model)) {
            return "take_eval";
        }

        TakeEvalAccess access = resolveTakeEvalAccess(eval, evaluationId, evalGroupId, currentUserId);
        if (!access.userCanAccess) {
            addCannotTakeMessage(currentUserId, locale, model);
            return "take_eval";
        }

        model.addAttribute("evalGroupId", access.evalGroupId);
        addGroupSelectorModel(access.validGroups, model);
        EvalGroup evalGroup = commonLogic.makeEvalGroupObject(access.evalGroupId);
        model.addAttribute("groupTitle", evalGroup.title);

        ResponseState responseState = loadResponseState(evaluationId, currentUserId, access.evalGroupId, responseId);
        model.addAttribute("responseId", responseState.responseId);
        addSavedWarning(responseState, locale, model);
        addSubmissionError(error, locale, model);
        addTakeSettings(eval, responseState.response, model);

        Boolean useCourseOnly = Boolean.TRUE.equals(settings.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY));
        Boolean showHierHeaders = Boolean.TRUE.equals(settings.get(EvalSettings.DISPLAY_HIERARCHY_HEADERS));
        TakeEvalFormModel formModel = buildTakeEvalFormModel(evaluationId, access.evalGroupId,
                responseState.answerMap, useCourseOnly, showHierHeaders, locale);
        model.addAttribute("categorySections", formModel.sections);
        model.addAttribute("evalForm", formModel.formWrapper);
        return "take_eval";
    }

    private void addBaseModel(Model model, EvalEvaluation eval, boolean external, Long evaluationId) {
        model.addAttribute("eval", eval);
        model.addAttribute("external", external);
        model.addAttribute("evaluationId", evaluationId);
    }

    private boolean addEvaluationUnavailableMessage(EvalEvaluation eval, Locale locale, Model model) {
        String evalState = evaluationService.returnAndFixEvalState(eval, true);
        if (EvalUtils.checkStateBefore(evalState, EvalConstants.EVALUATION_STATE_ACTIVE, false)) {
            model.addAttribute("cannotTakeMessage",
                    messageSource.getMessage("takeeval.eval.not.open", null, locale));
            return true;
        } else if (EvalUtils.checkStateAfter(evalState, EvalConstants.EVALUATION_STATE_CLOSED, true)) {
            model.addAttribute("cannotTakeMessage",
                    messageSource.getMessage("takeeval.eval.closed", null, locale));
            return true;
        }
        return false;
    }

    private TakeEvalAccess resolveTakeEvalAccess(EvalEvaluation eval, Long evaluationId, String evalGroupId, String currentUserId) {
        TakeEvalAccess access = new TakeEvalAccess();
        access.evalGroupId = evalGroupId;

        Map<Long, List<EvalAssignGroup>> assignGroupsMap = evaluationService
                .getAssignGroupsForEvals(new Long[]{evaluationId}, true, null);
        List<EvalAssignGroup> allAssignGroups = assignGroupsMap.get(evaluationId);

        if (!commonLogic.isUserAnonymous(currentUserId) && commonLogic.isUserAdmin(currentUserId)) {
            access.userCanAccess = true;
            for (EvalAssignGroup ag : allAssignGroups) {
                if (access.evalGroupId == null) {
                    access.evalGroupId = ag.getEvalGroupId();
                }
                access.validGroups.add(commonLogic.makeEvalGroupObject(ag.getEvalGroupId()));
            }
            return access;
        }

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
                if (access.evalGroupId == null) {
                    access.evalGroupId = g.evalGroupId;
                }
                access.userCanAccess = true;
                access.validGroups.add(commonLogic.makeEvalGroupObject(g.evalGroupId));
            }
        }
        return access;
    }

    private void addCannotTakeMessage(String currentUserId, Locale locale, Model model) {
        EvalUser current = commonLogic.getEvalUserById(currentUserId);
        model.addAttribute("cannotTakeMessage",
                messageSource.getMessage("takeeval.user.cannot.take",
                        new Object[]{current.displayName, current.email, current.username}, locale));
    }

    private void addGroupSelectorModel(List<EvalGroup> validGroups, Model model) {
        if (validGroups.size() <= 1) {
            model.addAttribute("showSwitchGroup", false);
            return;
        }
        List<String> groupValues = new ArrayList<>();
        List<String> groupLabels = new ArrayList<>();
        for (EvalGroup g : validGroups) {
            groupValues.add(g.evalGroupId);
            groupLabels.add(g.title);
        }
        model.addAttribute("groupValues", groupValues);
        model.addAttribute("groupLabels", groupLabels);
        model.addAttribute("showSwitchGroup", true);
    }

    private ResponseState loadResponseState(Long evaluationId, String currentUserId, String evalGroupId, Long responseId) {
        ResponseState responseState = new ResponseState();
        responseState.responseId = responseId;
        if (responseId != null) {
            responseState.response = deliveryService.getResponseById(responseId);
        } else {
            responseState.response = evaluationService.getResponseForUserAndGroup(evaluationId, currentUserId, evalGroupId);
            if (responseState.response != null) {
                responseState.responseId = responseState.response.getId();
            }
        }
        if (responseState.responseId != null && responseState.response != null) {
            responseState.answerMap = EvalUtils.getAnswersMapByTempItemAndAssociated(responseState.response);
        }
        return responseState;
    }

    private void addSavedWarning(ResponseState responseState, Locale locale, Model model) {
        if (responseState.responseId != null && responseState.response != null && !responseState.response.complete) {
            model.addAttribute("savedWarning",
                    messageSource.getMessage("takeeval.saved.warning", null, locale));
        }
    }

    private void addSubmissionError(String error, Locale locale, Model model) {
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
    }

    private void addTakeSettings(EvalEvaluation eval, EvalResponse response, Model model) {
        Boolean studentAllowedLeaveUnanswered = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED);
        if (studentAllowedLeaveUnanswered == null) {
            studentAllowedLeaveUnanswered = EvalUtils.safeBool(eval.getBlankResponsesAllowed(), false);
        }
        model.addAttribute("mustAnswerAll", !studentAllowedLeaveUnanswered);

        Boolean saveWithoutSubmit = (Boolean) settings.get(EvalSettings.STUDENT_SAVE_WITHOUT_SUBMIT);
        Boolean cancelAllowed = (Boolean) settings.get(EvalSettings.STUDENT_CANCEL_ALLOWED);
        model.addAttribute("saveWithoutSubmit", Boolean.TRUE.equals(saveWithoutSubmit) && (response == null || !response.complete));
        model.addAttribute("cancelAllowed", Boolean.TRUE.equals(cancelAllowed));
    }

    private TakeEvalFormModel buildTakeEvalFormModel(Long evaluationId, String evalGroupId,
            Map<String, EvalAnswer> answerMap, boolean useCourseOnly, boolean showHierHeaders, Locale locale) {

        TemplateItemDataList tidl = new TemplateItemDataList(evaluationId, evalGroupId,
                evaluationService, authoringService, hierarchyLogic, null);
        TakeEvalFormModel formModel = new TakeEvalFormModel();
        int displayNumber = 1;
        int answerIndex = 0;

        for (TemplateItemGroup tig : tidl.getTemplateItemGroups()) {
            CategorySection section = buildCategorySection(tig, useCourseOnly, locale);
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
                        EvalFormWrapper.AnswerSubmission slot = buildAnswerSlot(ti, dti.associateId, type, answerMap, true);
                        formModel.formWrapper.getAnswers().add(slot);

                        FormItemData fid = buildFormItemData(ti, displayNumber, dti.associateId,
                                slot.getAssociatedType(), answerIndex, answerMap.get(TemplateItemUtils.makeTemplateItemAnswerKey(
                                        ti.getId(), slot.getAssociatedType(), dti.associateId)), locale);
                        fid.setOdd(rowIndex % 2 != 0);
                        ngd.getItems().add(fid);
                        answerIndex++;
                        displayNumber++;

                    } else if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(type)) {
                        List<EvalTemplateItem> children = dti.blockChildItems != null
                                ? dti.blockChildItems : new ArrayList<>();

                        FormItemData parentFid = buildFormItemData(ti, displayNumber, null, null,
                                -1, null, locale);
                        parentFid.setOdd(rowIndex % 2 != 0);
                        List<FormItemData> childFids = new ArrayList<>();

                        for (int ci = 0; ci < children.size(); ci++) {
                            EvalTemplateItem child = children.get(ci);
                            EvalFormWrapper.AnswerSubmission slot = buildAnswerSlot(child, dti.associateId,
                                    TemplateItemUtils.getTemplateItemType(child), answerMap, false);
                            formModel.formWrapper.getAnswers().add(slot);

                            EvalAnswer existing = answerMap.get(TemplateItemUtils.makeTemplateItemAnswerKey(
                                    child.getId(), slot.getAssociatedType(), dti.associateId));
                            FormItemData childFid = buildFormItemData(child, displayNumber + ci,
                                    dti.associateId, slot.getAssociatedType(), answerIndex, existing, locale);
                            childFids.add(childFid);
                            answerIndex++;
                        }
                        parentFid.setChildItems(new ArrayList<EvalItemViewData>(childFids));
                        ngd.getItems().add(parentFid);
                        displayNumber += children.size();

                    } else {
                        FormItemData fid = buildFormItemData(ti, displayNumber, null, null, -1, null, locale);
                        fid.setOdd(rowIndex % 2 != 0);
                        ngd.getItems().add(fid);
                    }
                    rowIndex++;
                }
                section.getNodeGroups().add(ngd);
            }
            formModel.sections.add(section);
        }
        return formModel;
    }

    private CategorySection buildCategorySection(TemplateItemGroup tig, boolean useCourseOnly, Locale locale) {
        CategorySection section = new CategorySection();
        section.setAssociateType(tig.associateType);
        section.setAssociateId(tig.associateId);
        if (useCourseOnly) {
            section.setHeader("");
        } else if (EvalConstants.ITEM_CATEGORY_COURSE.equals(tig.associateType)) {
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
        return section;
    }

    private EvalFormWrapper.AnswerSubmission buildAnswerSlot(EvalTemplateItem ti, String associateId, String type,
            Map<String, EvalAnswer> answerMap, boolean includeFullAnswerValues) {
        EvalFormWrapper.AnswerSubmission slot = new EvalFormWrapper.AnswerSubmission();
        slot.setTemplateItemId(ti.getId());
        slot.setAssociatedId(associateId);
        slot.setAssociatedType(associateId != null ? ti.getCategory() : null);
        slot.setItemType(type);

        String key = TemplateItemUtils.makeTemplateItemAnswerKey(ti.getId(), slot.getAssociatedType(), associateId);
        EvalAnswer existing = answerMap.get(key);
        if (existing != null) {
            slot.setExistingAnswerId(existing.getId());
            slot.setNumeric(existing.getNumeric());
            slot.setNa(EvalUtils.decodeAnswerNA(existing));
            slot.setComment(existing.getComment());
            if (includeFullAnswerValues) {
                slot.setText(existing.getText());
            }
            if (includeFullAnswerValues && existing.multipleAnswers != null) {
                List<String> maList = new ArrayList<>();
                for (Integer v : existing.multipleAnswers) {
                    if (v != null) maList.add(String.valueOf(v));
                }
                slot.setMultipleAnswers(maList);
            }
        }
        return slot;
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

        String currentUserId = currentUserId();
        boolean submit = (actionSubmit != null);

        Long savedId;
        try {
            savedId = savePartialResponse(evaluationId, evalGroupId, responseId, currentUserId, formWrapper);
        } catch (Exception e) {
            log.error("Error saving partial response for evaluation {}: {}", evaluationId, e.getMessage(), e);
            return redirectToTakeEval(evaluationId, evalGroupId, responseId, "savefailed");
        }

        if (!submit) {
            return redirectToTakeEval(evaluationId, evalGroupId, savedId, null);
        }

        try {
            completeResponse(savedId, currentUserId);
        } catch (ResponseSaveException e) {
            String errorCode = ResponseSaveException.TYPE_BLANK_RESPONSE.equals(e.type)
                    ? "blank_response" : "missing_required";
            log.warn("Submission rejected for response {}: {}", savedId, e.getMessage());
            return redirectToTakeEval(evaluationId, evalGroupId, savedId, errorCode);
        } catch (Exception e) {
            log.error("Error completing response {}: {}", savedId, e.getMessage(), e);
            return redirectToTakeEval(evaluationId, evalGroupId, savedId, "savefailed");
        }

        return "redirect:/summary";
    }

    private Long savePartialResponse(Long evaluationId, String evalGroupId, Long responseId,
            String currentUserId, EvalFormWrapper formWrapper) {
        Long[] savedId = {responseId};
        daoInvoker.invokeTransactionalAccess(() -> {
            EvalResponse response = loadOrCreateResponseForSave(evaluationId, evalGroupId, savedId[0], currentUserId);
            response.setAnswers(buildAnswers(response, formWrapper));
            deliveryService.saveResponse(response, currentUserId);
            savedId[0] = response.getId();
        });
        return savedId[0];
    }

    private EvalResponse loadOrCreateResponseForSave(Long evaluationId, String evalGroupId, Long responseId, String currentUserId) {
        if (responseId != null) {
            return deliveryService.getResponseById(responseId);
        }

        EvalResponse response = evaluationService.getResponseForUserAndGroup(evaluationId, currentUserId, evalGroupId);
        if (response != null) {
            return response;
        }

        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        return new EvalResponse(currentUserId, evalGroupId, eval, new Date());
    }

    private void completeResponse(Long responseId, String currentUserId) {
        daoInvoker.invokeTransactionalAccess(() -> {
            EvalResponse response = deliveryService.getResponseById(responseId);
            decodeMultipleAnswers(response);
            response.setEndTime(new Date());
            deliveryService.saveResponse(response, currentUserId);
        });
    }

    private void decodeMultipleAnswers(EvalResponse response) {
        for (EvalAnswer answer : response.getAnswers()) {
            String mac = answer.getMultiAnswerCode();
            if (mac != null && !mac.isEmpty()) {
                answer.multipleAnswers = EvalUtils.decodeMultipleAnswers(mac);
            }
        }
    }

    private String redirectToTakeEval(Long evaluationId, String evalGroupId, Long responseId, String error) {
        StringBuilder redirect = new StringBuilder("redirect:/take_eval?evaluationId=")
                .append(evaluationId)
                .append("&evalGroupId=")
                .append(evalGroupId);
        if (responseId != null) {
            redirect.append("&responseId=").append(responseId);
        }
        if (error != null) {
            redirect.append("&error=").append(error);
        }
        return redirect.toString();
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
        return EvalItemViewDataBuilder.buildForTake(
                ti, displayNumber, associatedId, associatedType, answerIndex, existing, new FormItemData());
    }

    private EvalAnswer findAnswerInResponse(EvalResponse response, Long answerId) {
        if (response.getAnswers() == null) return null;
        for (EvalAnswer a : response.getAnswers()) {
            if (answerId.equals(a.getId())) return a;
        }
        return null;
    }
}
