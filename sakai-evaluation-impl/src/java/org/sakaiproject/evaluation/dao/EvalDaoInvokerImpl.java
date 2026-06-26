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

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.Setter;

/**
 * Impl
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalDaoInvokerImpl implements EvalDaoInvoker {

   @Setter
   private PlatformTransactionManager transactionManager;

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.dao.EvalDaoInvoker#invokeTransactionalAccess(java.lang.Runnable)
    */
   public void invokeTransactionalAccess(Runnable toInvoke) {
      if (transactionManager == null) {
         toInvoke.run();
         return;
      }

      TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
      transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
      transactionTemplate.execute(new TransactionCallbackWithoutResult() {
         @Override
         protected void doInTransactionWithoutResult(TransactionStatus status) {
            toInvoke.run();
         }
      });
   }

}
