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
package org.sakaiproject.evaluation.tool.renderers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;

/**
 * The main implementation for the ItemRenderer class which allows the presentation programmers
 * to simply inject and use a single class for all rendering of items
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ItemRendererImpl implements ItemRenderer {
	
    private Map<String, ItemRenderer> renderImpls = new HashMap<>();
    public void setRenderTypes(List<ItemRenderer> types) {
        for( ItemRenderer ir : types )
        {
            renderImpls.put(ir.getRenderType(), ir);
        }
    }

    public void init() {
        // just check that the renderImpls are inited
        if (renderImpls.size() <= 0) {
            throw new IllegalStateException("The renderTypes must be set before this class can be used");
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#renderItem(uk.org.ponder.rsf.components.UIContainer, java.lang.String, org.sakaiproject.evaluation.model.EvalTemplateItem, int, boolean)
     */
    public UIJointContainer renderItem(UIContainer parent, String ID, String[] bindings, EvalTemplateItem templateItem, int displayNumber, boolean disabled, Map<String, Object> renderProperties) {
        // do a quick check to make sure stuff is ok
        if (templateItem == null) {
            throw new IllegalArgumentException("templateItem cannot be null");
        }
        if (templateItem.getItem() == null) {
            throw new IllegalArgumentException("item (from templateItem.getItem()) cannot be null");
        }

        if (bindings == null || bindings.length == 0) {
            bindings = new String[] {null};
        }

        if (renderProperties == null) {
            renderProperties = new HashMap<>(0);
        }

        // figure out the type of item and then call the appropriate renderer
        String itemTypeConstant = TemplateItemUtils.getTemplateItemType(templateItem);
        ItemRenderer renderer = (ItemRenderer) renderImpls.get( itemTypeConstant );
        if (renderer == null) {
            throw new IllegalStateException("No renderer available for this item type: " + itemTypeConstant);
        }
        return renderer.renderItem(parent, ID, bindings, templateItem, displayNumber, disabled, renderProperties);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#getRenderType()
     */
    public String getRenderType() {
        // this handles no specific type so return null
        return null;
    }

    /**
     * Render the comment block beneath an item if enabled for this item and handle the binding if there is one
     * 
     * @param parent
     * @param templateItem
     * @param bindings
     */
    public static void renderCommentBlock(UIContainer parent, EvalTemplateItem templateItem, String[] bindings) {
        // render the item comment if enabled
        boolean usesComment = templateItem.getUsesComment() == null ? false : templateItem.getUsesComment();
        if (usesComment) {
            String commentBinding = null;
            if (bindings.length >= 3) {
                commentBinding = bindings[2];
            }
            String commentInit = null;
            if (commentBinding == null) commentInit = "";

            UIBranchContainer showComment = UIBranchContainer.make(parent, "showComment:");
            UIMessage.make(showComment, "itemCommentHeader", "viewitem.comment.desc");
            UIMessage commentLink = UIMessage.make(showComment, "itemCommentShow", "comment.show");
            commentLink.decorate(new UITooltipDecorator( UIMessage.make("comment.show.tooltip") ));
            UIInput.make(showComment, "itemComment", commentBinding, commentInit);
        }
    }

}
