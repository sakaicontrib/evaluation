/******************************************************************************
 * EvalCategoryEntityProviderImpl.java - created by aaronz on 29 May 2007
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
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.entity.EvalCategoryEntityProvider;

/**
 * Implementation for the entity provider for evaluation categories
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalCategoryEntityProviderImpl implements EvalCategoryEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider {

	private EvalEvaluationSetupService evaluationsLogic;
	public void setEvaluationsLogic(EvalEvaluationSetupService evaluationsLogic) {
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
		String[] categories = evaluationsLogic.getEvalCategories(null);
		for (int i = 0; i < categories.length; i++) {
			if (categories[i].equals(id)) {
				return true;
			}
		}
		return false;
	}

}
