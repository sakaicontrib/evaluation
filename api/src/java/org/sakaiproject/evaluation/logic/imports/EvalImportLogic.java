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

import java.util.List;

/**
 * Handle the importing of external data into the Evaluation System.
 * <ul>
 * <li>EvalScale</li>
 * <li>EvalItem</li>
 * <li>EvalTemplate</li>
 * <li>EvalTemplateItem</li>
 * <li>EvalEvaluation</li>
 * <li>EvalAssignGroup</li>
 * </ul>
 * By default, processing occurs in the current interactive session. The
 * session is periodically set active to avoid timing out during a long-running
 * import. Property eval.qrtzImport=true in sakai.properties causes processing
 * in a Quartz job rather than the current interactive session but this is
 * currently not working - see EVALSYS-273.
 * 
 * @author rwellis
 */
public interface EvalImportLogic {

	/**
	 * Parse XML content in a ContentResource and persist the contained evaluation data
	 * 
	 * @param id of the Reference that identifies the ContentResource
	 * @return a List of String messages for the user
	 */
	public List<String> load(String id);
}
