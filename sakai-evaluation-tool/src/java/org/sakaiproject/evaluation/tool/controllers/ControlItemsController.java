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
import java.util.Objects;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Data
    public static class ItemRow {
        Long    itemId;
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
    }
    @GetMapping
    public String show(Model model) {

        String currentUserId = currentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        boolean useExpertItems = (Boolean) settings.get(EvalSettings.USE_EXPERT_ITEMS);
        List<EvalItemGroup> itemGroups = authoringService.getAllItemGroups(currentUserId, true);

        List<EvalItem> userItems = authoringService.getItemsForUser(currentUserId, null, null, userAdmin);
        List<ItemRow> rows = new ArrayList<>();
        for (int i = 0; i < userItems.size(); i++) {
            EvalItem item = userItems.get(i);
            ItemRow row = new ItemRow();
            row.setItemId(item.getId());
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
        return "control_items";
    }

    @PostMapping("/copy")
    public String copyItem(@RequestParam Long itemId) {
        String currentUserId = currentUserId();
        authoringService.copyItems(new Long[]{itemId}, currentUserId, false, true);
        return "redirect:/control_items";
    }
}
