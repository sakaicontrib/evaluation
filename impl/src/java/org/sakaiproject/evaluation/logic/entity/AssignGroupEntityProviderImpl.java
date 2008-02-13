/******************************************************************************
 * AssignGroupEntityProviderImpl.java - created by aaronz on 28 May 2007
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.entity.AssignGroupEntityProvider;

/**
 * Implementation for the entity provider for evaluation groups
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class AssignGroupEntityProviderImpl implements AssignGroupEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider {

	private static Log log = LogFactory.getLog(AssignGroupEntityProviderImpl.class);
	
   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }


	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.EntityProvider#getEntityPrefix()
	 */
	public String getEntityPrefix() {
		return AssignGroupEntityProvider.ENTITY_PREFIX;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider#entityExists(java.lang.String)
	 */
	public boolean entityExists(String id) {
		log.warn("NOTE: checking if assign group exists: " + id);
		Long assignGroupId;
		try {
			assignGroupId = new Long(id);
			if (evaluationService.getAssignGroupById(assignGroupId) != null) {
				return true;
			}
		} catch (NumberFormatException e) {
			// invalid number so roll through to the false
		}
		log.warn("NOTE: assign group does not exist: " + id);
		return false;
	}

}
