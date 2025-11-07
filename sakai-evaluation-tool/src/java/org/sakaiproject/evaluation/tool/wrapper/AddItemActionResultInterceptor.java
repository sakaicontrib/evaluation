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
 * or implied. See the License for specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.tool.wrapper;

import org.sakaiproject.evaluation.tool.TemplateBBean;

import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Ensures that when the add-item form submits, the selected view parameters
 * are used for navigation.
 */
public class AddItemActionResultInterceptor implements ActionResultInterceptor {

    private TemplateBBean templateBBean;

    public void setTemplateBBean(TemplateBBean templateBBean) {
        this.templateBBean = templateBBean;
    }

    @Override
    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        if (templateBBean == null) {
            return;
        }
        ViewParameters target = templateBBean.consumePendingAddItemView();
        if (target != null) {
            result.resultingView = target;
        }
    }
}
