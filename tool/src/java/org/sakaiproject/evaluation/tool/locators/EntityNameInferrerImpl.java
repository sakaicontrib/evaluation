/**
 * EntityNameInferrerImpl.java - evaluation - 23 May 2007 11:35:56 AM - azeckoski
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
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

	private String[] ENTITY_CLASSES = new String[] {
			EvalScale.class.getName(),
			EvalItem.class.getName(),
			EvalTemplate.class.getName(),
			EvalTemplateItem.class.getName(),
			EvalEvaluation.class.getName(),
			EvalResponse.class.getName()
		};

	private String[] ENTITY_LOCATORS = new String[] {
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
