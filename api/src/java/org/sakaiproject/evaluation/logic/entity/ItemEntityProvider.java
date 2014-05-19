/**
 * $Id$
 * $URL$
 * ItemEntityProvider.java - evaluation - Jan 31, 2008 11:49:07 AM - azeckoski
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
import org.sakaiproject.evaluation.model.EvalItem;

/**
 * Allows external packages to find out the prefix for the eval group entity 
 * (deals with {@link EvalItem} model class)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ItemEntityProvider extends EntityProvider {
   public final static String ENTITY_PREFIX = "eval-item";
}
