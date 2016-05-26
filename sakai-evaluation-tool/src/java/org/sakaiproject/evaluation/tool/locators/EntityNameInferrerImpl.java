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
package org.sakaiproject.evaluation.tool.locators;

import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

import uk.org.ponder.rsf.state.entity.EntityNameInferrer;

/**
 * This piece is around for identifying entities in evaluation for use with RSF OTP and EL
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityNameInferrerImpl implements EntityNameInferrer {

	private static final String[] ENTITY_CLASSES = new String[] {
			EvalScale.class.getName(),
			EvalItem.class.getName(),
			EvalTemplate.class.getName(),
			EvalTemplateItem.class.getName(),
			EvalEvaluation.class.getName(),
			EvalResponse.class.getName()
		};

	private static final String[] ENTITY_LOCATORS = new String[] {
			"scaleBeanLocator",
			"itemWBL",
			"templateBeanLocator",
			"templateItemWBL",
			"evaluationBeanLocator",
			"responseBeanLocator"
		};

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.state.entity.EntityNameInferrer#getEntityName(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
   public String getEntityName(Class entityclazz) {
		String name = entityclazz.getName();
		for (int i = 0; i < ENTITY_CLASSES.length; i++) {
			if (ENTITY_CLASSES[i].equals(name)) {
				return ENTITY_LOCATORS[i];
			}
		}
		return null;
	}

}
