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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of ControlItemsProducer.
 * Lists the user's items for adding, editing, previewing or deleting.
 */
@Slf4j
@Controller
@RequestMapping("/control_items")
public class ControlItemsController extends EvalControllerSupport {

    private static final int PAGE_SIZE = 25;

    @Data
    public static class ItemRow {
        Long    itemId;
        int     number;
        String  classification;
        String  scaleDisplaySetting;
        String  ownerName;
        boolean expert;
        String  expertCategory;
        String  expertObjective;
        String  itemText;
        boolean canModify;
        boolean canDelete;
        boolean odd;
        Long    templateId;        // first template using this item, if any
        String  templateTitle;
        int     extraTemplatesCount; // more templates beyond the first one, if any
    }
    @GetMapping
    public String show(@RequestParam(defaultValue = "0") int page, Model model) {

        String currentUserId = currentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        boolean useExpertItems = (Boolean) settings.get(EvalSettings.USE_EXPERT_ITEMS);
        List<EvalItemGroup> itemGroups = authoringService.getAllItemGroups(currentUserId, true);

        int startResult = page * PAGE_SIZE;
        List<EvalItem> userItems = authoringService.getItemsForUser(currentUserId, null, null, userAdmin, startResult, PAGE_SIZE);
        int totalCount = authoringService.countItemsForUser(currentUserId, null, null, userAdmin);

        // Batch-resolve which template(s) use each item on this page, in a single query
        // instead of one round trip per row.
        List<Long> itemIds = new ArrayList<>();
        for (EvalItem item : userItems) {
            itemIds.add(item.getId());
        }
        Map<Long, List<EvalTemplate>> templatesByItemId = authoringService.getTemplatesUsingItems(itemIds);

        List<ItemRow> rows = new ArrayList<>();
        for (int i = 0; i < userItems.size(); i++) {
            EvalItem item = userItems.get(i);
            ItemRow row = new ItemRow();
            row.setItemId(item.getId());
            row.setNumber(startResult + i + 1);
            row.setClassification(item.getClassification());
            if (item.getScaleDisplaySetting() != null) {
                row.setScaleDisplaySetting(" - " + item.getScaleDisplaySetting());
            }
            row.setOwnerName(commonLogic.getEvalUserById(item.getOwner()).displayName);
            row.setItemText(item.getItemText());
            row.setOdd(i % 2 == 0);
            row.setCanModify(!item.getLocked() &&
                    authoringService.canModifyItem(currentUserId, item.getId()));
            row.setCanDelete(!item.getLocked() &&
                    authoringService.canRemoveItem(currentUserId, item.getId()));

            List<EvalTemplate> templates = templatesByItemId.get(item.getId());
            if (templates != null && !templates.isEmpty()) {
                row.setTemplateId(templates.get(0).getId());
                row.setTemplateTitle(templates.get(0).getTitle());
                row.setExtraTemplatesCount(templates.size() - 1);
            }

            if (item.getExpert()) {
                row.setExpert(true);
                // Find the expert group this item belongs to
                outer:
                for (EvalItemGroup eig : itemGroups) {
                    List<EvalItem> expertItems = authoringService.getItemsInItemGroup(eig.getId(), true);
                    for (EvalItem expertItem : expertItems) {
                        if (Objects.equals(expertItem.getId(), item.getId())) {
                            if (EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE.equals(eig.getType())) {
                                row.setExpertCategory(eig.getParent().getTitle() + " : ");
                                row.setExpertObjective(eig.getTitle());
                            } else {
                                row.setExpertCategory(eig.getTitle());
                            }
                            break outer;
                        }
                    }
                }
            }
            rows.add(row);
        }

        model.addAttribute("itemRows", rows);
        model.addAttribute("useExpertItems", useExpertItems);
        model.addAttribute("itemClassificationValues", EvalToolConstants.ITEM_SELECT_CLASSIFICATION_VALUES);
        model.addAttribute("itemClassificationLabels", EvalToolConstants.ITEM_SELECT_CLASSIFICATION_LABELS);

        model.addAttribute("page", page);
        if (totalCount > PAGE_SIZE) {
            int actualStart = totalCount == 0 ? 0 : startResult + 1;
            int actualEnd = Math.min(startResult + PAGE_SIZE, totalCount);
            int totalPages = (totalCount + PAGE_SIZE - 1) / PAGE_SIZE;
            model.addAttribute("pagerMsg", new Object[]{ actualStart, actualEnd, totalCount });
            model.addAttribute("hasPrev", page > 0);
            model.addAttribute("hasNext", totalCount > startResult + PAGE_SIZE);
            model.addAttribute("prevPage", page - 1);
            model.addAttribute("nextPage", page + 1);
            model.addAttribute("pageNumbers", buildPageNumbers(page, totalPages));
        }

        return "control_items";
    }

    /**
     * Builds the list of page numbers (0-based) to link to in the pager: always the first and
     * last page, plus a window of pages around the current one; -1 marks a gap ("...") between
     * non-consecutive numbers so the pager stays a fixed width even with hundreds of pages.
     */
    private List<Integer> buildPageNumbers(int currentPage, int totalPages) {
        List<Integer> pages = new ArrayList<>();
        int windowStart = Math.max(1, currentPage - 2);
        int windowEnd = Math.min(totalPages - 2, currentPage + 2);

        pages.add(0);
        if (windowStart > 1) {
            pages.add(-1);
        }
        for (int p = windowStart; p <= windowEnd; p++) {
            pages.add(p);
        }
        if (windowEnd < totalPages - 2) {
            pages.add(-1);
        }
        if (totalPages > 1) {
            pages.add(totalPages - 1);
        }
        return pages;
    }

    @PostMapping("/copy")
    public String copyItem(@RequestParam Long itemId, RedirectAttributes redirectAttrs) {
        String currentUserId = currentUserId();
        authoringService.copyItems(new Long[]{itemId}, currentUserId, false, true);
        redirectAttrs.addFlashAttribute("successMessage", "controlitems.copy.user.message");
        return "redirect:/control_items";
    }
}
