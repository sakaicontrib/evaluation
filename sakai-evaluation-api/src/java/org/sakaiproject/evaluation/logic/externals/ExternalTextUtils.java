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
