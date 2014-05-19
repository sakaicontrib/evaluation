/**
 * $Id$
 * $URL$
 * EvalDaoInvokerImpl.java - evaluation - Mar 7, 2008 1:20:49 PM - azeckoski
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
 * Impl
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalDaoInvokerImpl implements EvalDaoInvoker {

   public EvaluationDao dao;
   public void setDao(EvaluationDao dao) {
      this.dao = dao;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.dao.EvalDaoInvoker#invokeTransactionalAccess(java.lang.Runnable)
    */
   public void invokeTransactionalAccess(Runnable toInvoke) {
      dao.invokeTransactionalAccess(toInvoke);
   }

}
