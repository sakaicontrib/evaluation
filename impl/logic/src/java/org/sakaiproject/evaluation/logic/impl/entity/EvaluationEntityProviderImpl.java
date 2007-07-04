/******************************************************************************
 * EvaluationEntityProviderImpl.java - created by aaronz on 23 May 2007
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.entity.EvaluationEntityProvider;

/**
 * Implementation for the entity provider for evaluations
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationEntityProviderImpl implements EvaluationEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider {

	private static Log log = LogFactory.getLog(EvaluationEntityProviderImpl.class);
	
	private EvalEvaluationsLogic evaluationsLogic;
	public void setEvaluationsLogic(EvalEvaluationsLogic evaluationsLogic) {
		this.evaluationsLogic = evaluationsLogic;
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
		log.warn("NOTE: checking if evaluation exists: " + id);
		Long evalId;
		try {
			evalId = new Long(id);
			if (evaluationsLogic.getEvaluationById(evalId) != null) {
				return true;
			}
		} catch (NumberFormatException e) {
			// invalid number so roll through to the false
		}
		log.warn("NOTE: evaluation does not exist: " + id);
		return false;
	}

}
