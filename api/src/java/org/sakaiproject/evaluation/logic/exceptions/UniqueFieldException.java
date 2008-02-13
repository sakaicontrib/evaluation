/**
 * $Id$
 * $URL$
 * UniqueFieldException.java - evaluation - Feb 7, 2008 6:42:39 PM - azeckoski
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
 * Indicates that a field which must be unique was not
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class UniqueFieldException extends RuntimeException {

   /**
    * The name of the field that must be unique
    */
   public String fieldName;
   /**
    * The already used value that was in the field
    */
   public String fieldValue;

   public UniqueFieldException() {}
   
   public UniqueFieldException(String message) {
      super(message);
   }

   public UniqueFieldException(String message, String fieldName, String fieldValue) {
      super(message);
      this.fieldName = fieldName;
      this.fieldValue = fieldValue;
   }

}
