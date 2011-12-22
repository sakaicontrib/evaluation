/**
 * TakeEvalBean.java - evaluation - 16 Jan 2007 11:35:56 AM - whumphri
 * $URL: https://source.sakaiproject.org/contrib/evaluation/trunk/tool/src/java/org/sakaiproject/evaluation/tool/TakeEvalBean.java $
 * $Id: TakeEvalBean.java 69596 2010-08-05 07:43:53Z lovemore.nalube@uct.ac.za $
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
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

    private static Log log = LogFactory.getLog(CommonProducerBean.class);

    public void init() {
        log.info("INIT");
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

    @SuppressWarnings("unused")
    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

}