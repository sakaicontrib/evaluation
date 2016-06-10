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
package org.sakaiproject.evaluation.logic.entity;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;

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
		for( String categorie : categories ) {
			if (categorie.equals(id)) {
				return true;
			}
		}
		return false;
	}

}
