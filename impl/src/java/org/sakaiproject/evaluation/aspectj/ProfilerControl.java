/**
 * $Id$
 * $URL$
 * ProfileSummary.java - evaluation - May 24, 2008 6:32:06 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.aspectj;

/**
 * This is just a nice easy way to force a profile summary output
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ProfilerControl {

   /**
    * Get the profiling summary and log it
    * @return the summary from the profiling aspect
    */
   public static String generateSummary() {
      // does nothing really
      return "Aspect should have populated this";
   }

   public static void enableProfiler() {}

   public static void resetProfiler() {}

   public static void enableMethodLogging() {}

}
