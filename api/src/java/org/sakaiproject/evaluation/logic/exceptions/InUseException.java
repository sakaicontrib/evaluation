/**
 * $Id$
 * $URL$
 * InUseException.java - evaluation - May 5, 2008 11:25:51 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.exceptions;


/**
 * Indicates that this persistent object is still in use and therefore cannot be removed or modified right now
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class InUseException extends RuntimeException {

   /**
    * The type of the persistent object which caused this exception
    */
   public Class<?> type;
   /**
    * The id of the persistent object which caused this exception
    */
   public Long id;

   public InUseException(String message, Class<?> type, Long id) {
      super(message);
      this.type = type;
      this.id = id;
   }

   public InUseException(String message, Throwable cause) {
      super(message, cause);
   }

   public InUseException(String message) {
      super(message);
   }

}
