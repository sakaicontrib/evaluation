/**
 * $Id$
 * $URL$
 * EvalDaoInvoker.java - evaluation - Mar 7, 2008 1:17:03 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.dao;


/**
 * Used to allow the tool to wrap our transactions, 
 * this allows us to have one large transaction per tool request
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface EvalDaoInvoker {
   
   /**
    * A runnable to wrap a transaction around (as controlled by the dao)
    * 
    * @param toInvoke a Runnable
    */
   public void invokeTransactionalAccess(Runnable toInvoke);

}
