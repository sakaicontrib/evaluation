/**
 * ItemRenderer.java - evaluation - Oct 29, 2007 2:59:12 PM - azeckoski
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

import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;

/**
 * Interface for class which handles rendering items<br/>
 * This allows us to split out the rendering for items so that we do not
 * have a lot of code duplication
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface ItemRenderer {

    /**
     * If this key is present it indicates this item requires an answer
     */
    public static final String EVAL_PROP_ANSWER_REQUIRED = "evalAnswersRequired";
    /**
     * if this key is present it indicates this item was filled out in an invalid way
     */
    public static final String EVAL_PROP_RENDER_INVALID = "evalRenderInvalid";

    /**
     * Renders an item correctly in a view based on the type and the settings stored within it<br/>
     * 
     * @param parent any RSF {@link UIContainer} object which will contain the rendered item
     * @param ID the (RSF) ID of this component
     * @param bindings an array of EL expressions to be used as the value binding for the contained String value, can be null if no binding,
     * use an array with the elements as follows:
     * 0 = main answer binding
     * 1 = not applicable binding
     * 2 = item comment binding
     * <b>NOTE:</b> if you are rendering a block then you should pass in the child item bindings in display order, 
     * do not include a binding for the parent
     * @param templateItem the templateItem to render (if you only have an item then
     * simply create an {@link EvalTemplateItem} and wrap the item in it)
     * @param displayNumber the number to display next to this item (if 0 or less then display none)
     * @param disabled if true, then the item is rendered as disabled and cannot be submitted, if false, the item can be submitted
     * @param renderProperties additional rendering properties to send along (can be null if there are none),
     * these are used to indicate transient state which should cause a rendering change
     * @return a {@link UIJointContainer} which has been populated correctly
     */
    public UIJointContainer renderItem(UIContainer parent, String ID, String[] bindings, EvalTemplateItem templateItem, int displayNumber, boolean disabled, Map<String, Object> renderProperties);

    /**
     * Indicates the type of item this renderer handles
     * 
     * @return an ITEM_TYPE constant from {@link EvalConstants}
     */
    public String getRenderType();

}
