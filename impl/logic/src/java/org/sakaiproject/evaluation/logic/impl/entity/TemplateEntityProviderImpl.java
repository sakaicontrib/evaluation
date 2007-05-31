/******************************************************************************
 * TemplateEntityProviderImpl.java - created by aaronz on 29 May 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.impl.entity;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.logic.entity.TemplateEntityProvider;

/**
 * Implementation for the entity provider for evaluation templates
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class TemplateEntityProviderImpl implements TemplateEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider {

	private EvalTemplatesLogic templatesLogic;
	public void setTemplatesLogic(EvalTemplatesLogic templatesLogic) {
		this.templatesLogic = templatesLogic;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.EntityProvider#getEntityPrefix()
	 */
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider#entityExists(java.lang.String)
	 */
	public boolean entityExists(String id) {
		Long templateId;
		try {
			templateId = new Long(id);
			if (templatesLogic.getTemplateById(templateId) != null) {
				return true;
			}
		} catch (NumberFormatException e) {
			// invalid number so roll through to the false
		}
		return false;
	}

}
