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

import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Detach a single child item from its block, turning it back into a standalone
 * template item. Spring MVC equivalent of the RSF-era
 * TemplateItemEntityProviderImpl#unblock() custom action, which was only reachable
 * through a legacy EntityBroker AJAX call.
 */
@Controller
@RequestMapping("/unblock_item")
public class UnblockItemController extends EvalControllerSupport {

    @GetMapping
    public String show(@RequestParam Long templateItemId,
                       @RequestParam Long templateId,
                       Model model) {

        EvalTemplateItem templateItem = authoringService.getTemplateItemById(templateItemId);
        if (!TemplateItemUtils.isBlockChild(templateItem)) {
            throw new IllegalStateException("Template item " + templateItemId + " is not part of a group");
        }

        model.addAttribute("templateItemId", templateItemId);
        model.addAttribute("templateId", templateId);
        model.addAttribute("itemText", templateItem.getItem().getItemText());
        return "unblock_item";
    }

    @PostMapping
    public String unblock(@RequestParam Long templateItemId,
                          @RequestParam Long templateId) {

        daoInvoker.invokeTransactionalAccess(() -> {
            EvalTemplateItem templateItem = authoringService.getTemplateItemById(templateItemId);
            if (!TemplateItemUtils.isBlockChild(templateItem)) {
                throw new IllegalStateException("Template item " + templateItemId + " is not part of a group");
            }

            templateItem.setBlockParent(null);
            templateItem.setBlockId(null);
            // Saving without a display order puts the item at the bottom of the template.
            templateItem.setDisplayOrder(null);
            authoringService.saveTemplateItem(templateItem, currentUserId());
        });

        return "redirect:/modify_template_items?templateId=" + templateId;
    }
}
