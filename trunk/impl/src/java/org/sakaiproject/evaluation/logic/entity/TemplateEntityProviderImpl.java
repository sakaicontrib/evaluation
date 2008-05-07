/******************************************************************************
 * TemplateEntityProviderImpl.java - created by aaronz on 29 May 2007
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.entity;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.entity.TemplateEntityProvider;

/**
 * Implementation for the entity provider for evaluation templates
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class TemplateEntityProviderImpl implements TemplateEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider {

	private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
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
	   boolean exists = false;
		Long templateId;
		try {
			templateId = new Long(id);
			if (authoringService.getTemplateById(templateId) != null) {
			   exists = true;
			}
		} catch (NumberFormatException e) {
			// invalid number so roll through to the false
		   exists = false;
		}
		return exists;
	}

}
