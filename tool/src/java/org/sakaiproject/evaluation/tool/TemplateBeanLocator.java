/******************************************************************************
 * TemplateBeanLocator.java - created by whumphri@vt.edu on Jan 20, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Will Humphries (whumphri@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalTemplate;

import uk.org.ponder.beanutil.BeanLocator;


/**
 * This is the OTP bean used to locate templates.
 * 
 * @author 
 */

public class TemplateBeanLocator implements BeanLocator {
	
	private EvalTemplatesLogic templatesLogic;
	public void setTemplatesLogic( EvalTemplatesLogic templatesLogic) {
		this.templatesLogic = templatesLogic;
	}
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}
	
	private Map delivered = new HashMap();
	
	public Object locateBean(String path) {
		Object togo=delivered.get(path);
		if(togo==null){
			if(path.startsWith("new")){
				togo = new EvalTemplate(new Date(), external.getCurrentUserId(),
						"", "", Boolean.FALSE);
			}else{ 
				togo=templatesLogic.getTemplateById(new Long(Long.parseLong(path.trim())));
			}
			delivered.put(path, togo);
		}
		return togo;
	}


}