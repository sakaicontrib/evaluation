/**
 * $Id$
 * $URL$
 * ExternalUtils.java - evaluation - Mar 31, 2008 4:56:53 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.externals;


/**
 * Handles external utility calls
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ExternalTextUtils {

   /**
    * Take an html string or a string with html in it and then strip out all tags and 
    * return a string that is plaintext, convert html returns in plantext returns 
    * 
    * @param html an html string
    * @return a plaintext version of the string
    */
   public String makePlainTextFromHTML(String html);

   /**
    * Cleans up the users submitted strings to protect us from XSS
    * 
    * @param userSubmittedString any string from the user which could be dangerous
    * @return a cleaned up string which is now safe
    */
   public String cleanupUserStrings(String userSubmittedString);

}
