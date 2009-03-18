/**
 * $Id$
 * $URL$
 * TemplateActionResultInterceptor.java - evaluation - Mar 19, 2008 5:04:23 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
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
