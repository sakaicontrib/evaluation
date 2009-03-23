/**
 * TextRenderer.java - evaluation - Oct 29, 2007 2:59:12 PM - azeckoski
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.renderers;

import static org.sakaiproject.evaluation.utils.TemplateItemUtils.*;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;

/**
 * This handles the rendering of text type items
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@SuppressWarnings("deprecation")
public class TextRenderer implements ItemRenderer {

    /**
     * This identifies the template component associated with this renderer
     */
    public static final String COMPONENT_ID = "render-text-item:";

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#renderItem(uk.org.ponder.rsf.components.UIContainer, java.lang.String, org.sakaiproject.evaluation.model.EvalTemplateItem, int, boolean)
     */
    public UIJointContainer renderItem(UIContainer parent, String ID, String[] bindings, EvalTemplateItem templateItem, int displayNumber, boolean disabled, Map<String, String> evalProperties) {

        UIJointContainer container = new UIJointContainer(parent, ID, COMPONENT_ID);
        if (evalProperties.containsKey(ItemRenderer.EVAL_PROP_RENDER_INVALID)) {
            container.decorate( new UIStyleDecorator("validFail") ); // must match the existing CSS class
        } else if (safeBool(templateItem.isCompulsory())) {
            container.decorate( new UIStyleDecorator("compulsory") ); // must match the existing CSS class
        }

        if (displayNumber <= 0) displayNumber = 0;
        String initValue = null;
        if (bindings[0] == null) initValue = "";

        String naBinding = null;
        if (bindings.length > 1) {
            naBinding = bindings[1];
        }
        Boolean naInit = null;
        if (naBinding == null) naInit = Boolean.FALSE;

        UIOutput.make(container, "itemNum", displayNumber+"" );
        UIVerbatim.make(container, "itemText", templateItem.getItem().getItemText());

        if ( templateItem.getUsesNA().booleanValue() ) {
            UIBranchContainer branchNA = UIBranchContainer.make(container, "showNA:");
            branchNA.decorators = new DecoratorList( new UIStyleDecorator("na") ); // must match the existing CSS class
            UIBoundBoolean checkbox = UIBoundBoolean.make(branchNA, "itemNA", naBinding, naInit);
            UIMessage.make(branchNA, "descNA", "viewitem.na.desc").decorate( new UILabelTargetDecorator(checkbox) );
        }

        UIInput textarea = UIInput.make(container, "essayBox", bindings[0], initValue); //$NON-NLS-2$

        Map<String, String> attrmap = new HashMap<String, String>();
        attrmap.put("rows", templateItem.getDisplayRows().toString());
        // disabling the textbox is undesireable -AZ
        //		if (disabled) {
        //			attrmap.put("disabled", "true"); //$NON-NLS-2$		
        //		}
        textarea.decorators = new DecoratorList( new UIFreeAttributeDecorator(attrmap) );

        return container;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#getRenderType()
     */
    public String getRenderType() {
        return EvalConstants.ITEM_TYPE_TEXT;
    }

}
