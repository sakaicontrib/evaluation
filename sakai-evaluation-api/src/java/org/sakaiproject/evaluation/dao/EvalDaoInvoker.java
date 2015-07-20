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
