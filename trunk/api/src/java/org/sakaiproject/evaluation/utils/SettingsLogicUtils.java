/**
 * $Id$
 * $URL$
 * SettingsLogicUtils.java - evaluation - Dec 25, 2006 12:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.utils;

/**
 * This is a weird location but it will have to do for now,
 * This util has a set of static methods which are used to process data
 * in a uniform way but limits the dependencies
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class SettingsLogicUtils {

   public static final String DEFAULT_TYPE = "java.lang.String";

   /**
    * Get the name associated with a settings constant, if this constant does not
    * have a proper format then the entire constant is returned (type assumed)
    * Proper Format: (name):(type)
    * 
    * @param constant a constant from this interface
    * @return the name based on the passed in constant
    */
   public static String getName(String constant) {
      constant = checkString(constant);
      if (constant.indexOf(":") > 0) {
         return constant.substring(0, constant.indexOf(":"));
      } else {
         return constant;
      }
   }

   /**
    * Get the type associated with a settings constant
    * @param constant a constant from this interface
    * @return the java type (as a string) of the value associated with this constant
    */
   public static String getType(String constant) {
      constant = checkString(constant);
      if (constant.indexOf(":")+1 > 0) {
         return constant.substring(constant.indexOf(":")+1);
      } else {
         return DEFAULT_TYPE;
      }
   }


   /**
    * Return the {@link Class} object from the text version of the fully qualifies classname
    * @param propname the type from the setting constant string,
    * Format: (name):(type)
    * @return a {@link Class} which represents the type of this setting
    */
   public static Class<?> getTypeClass(String propname) {
      String typename = getType(propname);
      try {
         return Class.forName(typename);
      } catch (Exception e) {
         throw new IllegalArgumentException("Could not match " + typename + " to a class, from propname=" + propname);
      }
   }


   /**
    * @param constant a settings constant to check for validity
    * @return a trimmed version of the constant
    */
   private static String checkString(String constant) {
      if (constant == null || constant.trim().length() <= 0) {
         throw new IllegalArgumentException("Invalid constant (empty or null)");
      }
      return constant.trim();
   }

}
