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
package org.sakaiproject.evaluation.tool.wrapper;

import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.locators.TemplateBeanLocator;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * An interceptor for the outgoing URLs from action cycles that applies the
 * IDs from freshly saved entities to the templateId field in an outgoing
 * EvalViewParameters. 
 * 
 * @author Antranig Basman (amb26@ponder.org.uk)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TemplateActionResultInterceptor implements ActionResultInterceptor {

    private TemplateBeanLocator templateBeanLocator;
    public void setTemplateBeanLocator(TemplateBeanLocator templateBeanLocator) {
        this.templateBeanLocator = templateBeanLocator;
    }

    /* Standard semantics - if there was an incoming ID, outgoing will be the same
     * if incoming is blank, and OTP bean contains a new entity, outgoing will be
     * the new entity. - antranig
     */

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.ActionResultInterceptor#interceptActionResult(uk.org.ponder.rsf.flow.ARIResult, uk.org.ponder.rsf.viewstate.ViewParameters, java.lang.Object)
     */
    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        // for template creation
        if (incoming instanceof TemplateViewParameters
                && result.resultingView instanceof TemplateViewParameters) {
            TemplateViewParameters ineval = (TemplateViewParameters) incoming;
            TemplateViewParameters outgoing = (TemplateViewParameters) result.resultingView;
            if (ineval.templateId != null) {
                outgoing.templateId = ineval.templateId;
            } else {
                EvalTemplate template = (EvalTemplate) templateBeanLocator.locateBean(TemplateBeanLocator.NEW_1);
                if (template != null) {
                    outgoing.templateId = template.getId();
                }
            }
        }
    }

}
