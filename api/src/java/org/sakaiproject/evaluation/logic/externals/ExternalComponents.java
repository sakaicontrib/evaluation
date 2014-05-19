/**
 * $Id$
 * $URL$
 * ExternalComponents.java - evaluation - May 12, 2008 9:48:17 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.externals;


/**
 * Provides access to the external component/bean manager if there is one
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ExternalComponents {

   /**
    * Looks up a bean in the central component manager context (i.e spring)
    * 
    * @param <T>
    * @param type a class type for a bean
    * @return the bean of this class type OR null if none can be found
    */
   public <T> T getBean(Class<T> type);

}
