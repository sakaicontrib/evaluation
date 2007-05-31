/******************************************************************************
 * TemplateEntityProvider.java - created by aaronz on 29 May 2007
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

package org.sakaiproject.evaluation.logic.entity;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.evaluation.model.EvalTemplate;

/**
 * Allows external packages to find out the prefix for the eval group entity 
 * (deals with {@link EvalTemplate} model class)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface TemplateEntityProvider extends EntityProvider {
	public final static String ENTITY_PREFIX = "eval-template";
}
