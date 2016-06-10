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
package org.sakaiproject.evaluation.tool.producers;

import org.sakaiproject.evaluation.tool.CommonProducerBean;

import uk.org.ponder.beanutil.PathUtil;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBoundList;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Special bean which almost all eval producers should extend
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public abstract class EvalCommonProducer implements ViewComponentProducer {

    public abstract String getViewID();

    CommonProducerBean commonProducerBean;
    public void setCommonProducerBean(CommonProducerBean commonProducerBean) {
        this.commonProducerBean = commonProducerBean;
    }
    public CommonProducerBean getCommonProducerBean() {
        return commonProducerBean;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        String viewId = getViewID();
        this.commonProducerBean.beforeProducer(viewId, tofill, viewparams, checker);
        fill(tofill, viewparams, checker);
        this.commonProducerBean.afterProducer(viewId, tofill, viewparams, checker);
    }

    /** 
     * Same as {@link #fillComponents(UIContainer, ViewParameters, ComponentChecker)}
     * 
     * @param tofill The container into which produced components will be inserted.
     * @param viewparams The view parameters specifying the currently rendering  view
     * @param checker A ComponentChecker (actually an interface into a ViewTemplate)
     *  that can be used by the producer to "short-circuit" the production of 
     *  potentially expensive components if they are not present in the chosen
     *  template for this view. Since the IKAT algorithm cannot run at this time, it
     *  is currently only economic to check components that are present at the root
     *  level of the template, but these are the most likely to be expensive.
     */
    public abstract void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker);

    // Some common utils

    /**
     * Construct a UIBoundBoolean administrative setting component
     * 
     * @param parent the containing UIContainer
     * @param ID the component's RSF id
     * @param beanId TODO
     * @param adminkey the administrative setting constant in org.sakaiproject.evaluation.logic.EvalSettings
     * @return 
     */
    public static UIBoundBoolean makeBoolean(UIContainer parent, String ID, String beanId, String adminkey) {
        // Must use "composePath" here since admin keys currently contain periods
        UIBoundBoolean bb = UIBoundBoolean.make(parent, ID, adminkey == null ? null : PathUtil.composePath(beanId, adminkey));
        return bb;
    }

    /**
     * Construct a UISelect administrative setting component
     * 
     * Values and labels are contained in org.sakaiproject.evaluation.tool.EvalToolConstants.<br />
     * Administrative setting constant in org.sakaiproject.evaluation.logic.EvalSettings<br />
     * 
     * @param parent the containing UIContainer
     * @param ID the component's RSF id
     * @param values the options from which the the user has to choose
     * @param labels the labels for the options from which the user has to choose
     * @param beanId TODO
     * @param adminkey the administrative setting constant 
     * @param message use message properties if true, do not use message properties if false
     * @return 
     */
    public static UISelect makeSelect(UIContainer parent, String ID, String[] values, String[] labels, String beanId, String adminkey, boolean message) {
        UISelect selection = UISelect.make(parent, ID); 
        selection.selection = new UIInput();
        if (adminkey != null) {
            selection.selection.valuebinding = new ELReference(PathUtil.composePath(beanId, adminkey));
        }
        UIBoundList selectvalues = new UIBoundList();
        selectvalues.setValue(values);
        selection.optionlist = selectvalues;
        UIBoundList selectlabels = new UIBoundList();
        selectlabels.setValue(labels);
        selection.optionnames = selectlabels;   

        if (message) {
            selection.setMessageKeys();
        }
        return selection;
    }

    /**
     * Construct a UIInput administrative setting component
     * 
     * Administrative setting constant in org.sakaiproject.evaluation.logic.EvalSettings.<br />
     * 
     * @param parent the containing UIContainer
     * @param ID the component's RSF id
     * @param beanId TODO
     * @param adminkey the administrative setting constant
     * @return 
     */
    public static UIInput makeInput(UIContainer parent, String ID, String beanId, String adminkey) {
        UIInput input = UIInput.make(parent, ID, PathUtil.composePath(beanId, adminkey));
        return input;
    }

}
