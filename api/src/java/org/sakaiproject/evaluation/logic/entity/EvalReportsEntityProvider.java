/**
 * $Id: EvaluationEntityProvider.java 46025 2008-02-27 13:01:48Z aaronz@vt.edu $
 * $URL: https://source.sakaiproject.org/contrib/evaluation/trunk/api/src/java/org/sakaiproject/evaluation/logic/entity/EvaluationEntityProvider.java $
 * EvaluationEntityProvider.java - evaluation - May 23, 2007 12:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.entity;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;

/**
 * Provides access to reporting information for evaluations
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalReportsEntityProvider extends EntityProvider {
	public final static String ENTITY_PREFIX = "eval-reports";
}
