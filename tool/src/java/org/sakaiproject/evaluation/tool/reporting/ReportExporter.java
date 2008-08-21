/**
 * $Id$
 * $URL$
 * ReportExporter.java - evaluation - Mar 31, 2008 12:45:38 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.reporting;

import java.io.OutputStream;

import org.sakaiproject.evaluation.model.EvalEvaluation;


/**
 * An interface to implement when exporting reports
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ReportExporter {

   /**
    * Generates the export which will be placed into the OutputStream for sending to the user via an HTTP response
    * 
    * @param evaluation the {@link EvalEvaluation} object to build the report for
    * @param groupIds the set of groups to include results data from
    * @param outputStream the resulting data will be placed into this
    */
   public void buildReport(EvalEvaluation evaluation, String[] groupIds, OutputStream outputStream);
   
   public String getContentType();

}
