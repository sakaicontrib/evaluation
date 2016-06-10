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
package org.sakaiproject.evaluation.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalSettings;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * This bean is called at the top of every producer,
 * this is a singleton service
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class CommonProducerBean {

    private static final Log LOG = LogFactory.getLog(CommonProducerBean.class);

    public void init() {
        LOG.info("INIT");
    }

    public void beforeProducer(String viewId, UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        //log.info("BEFORE "+viewId);
        String localCSS = (String) settings.get(EvalSettings.LOCAL_CSS_PATH);
        if (localCSS != null && ! "".equals(localCSS)) {
            UILink.make(tofill, "local_css_include", localCSS);
        }
    }

    public void afterProducer(String viewId, UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        //log.info("AFTER "+viewId);
        // nothing here right now
    }

    private EvalSettings settings;
    public void setEvalSettings(EvalSettings settings) {
        this.settings = settings;
    }

}
