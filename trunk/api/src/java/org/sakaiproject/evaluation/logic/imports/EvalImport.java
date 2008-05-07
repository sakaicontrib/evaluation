/**
 * $Id$
 * $URL$
 * EvalImport.java - evaluation - May 28, 2007 12:07:31 AM - rwellis
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
 * Import evaluation data contained in an XML ContentResource
 * 
 * @author rwellis
 *
 */
public interface EvalImport {
	
	/**
	 * Process an XML ContentResource and save/update evaluation data
	 * 
	 * @param id the ContentResource identifier
	 * @param currentUserId the identifier of the current user
	 * @return a List of String messages for the current user
	 */
	public List<String> process(String id, String currentUserId);

}
