/**
 * $Id$
 * $URL$
 * EvalImportLogic.java - evaluation - May 28, 2007 12:07:31 AM - rwellis
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.imports;

/**
 * Import external data into the Evaluation System as tagged XML content.
 * 
 * <ul>
 * <li>EvalScale</li>
 * <li>EvalItem</li>
 * <li>EvalTemplate</li>
 * <li>EvalEmailTemplate</li>
 * <li>EvalTemplateItem</li>
 * <li>EvalEvaluation</li>
 * <li>EvalAssignGroup</li>
 * </ul>
 * 
 * @author rwellis
 */
public interface EvalImportLogic {

	/**
	 * Parse XML content in a ContentResource and persist the contained evaluation data
	 * 
	 * @param id of the Reference that identifies the ContentResource
	 * @return a String navigation case
	 */
	public String load(String id);
}
