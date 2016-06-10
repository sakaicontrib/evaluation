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
package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.tool.LocalResponsesLogic;

import uk.org.ponder.beanutil.BeanLocator;

/**
 * Special bean locator which is used to locate other bean locators, sneaky huh?
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ResponseAnswersBeanLocator implements BeanLocator {
   public static final String NEW_PREFIX = "new";
   public static String NEW_1 = NEW_PREFIX +"1";

   private LocalResponsesLogic localResponsesLogic;
   public void setLocalResponsesLogic(LocalResponsesLogic localResponsesLogic) {
      this.localResponsesLogic = localResponsesLogic;
   }

   private ResponseBeanLocator responseBeanLocator;
   public void setResponseBeanLocator(ResponseBeanLocator responseBeanLocator) {
      this.responseBeanLocator = responseBeanLocator;
   }

   private Map<String, BeanLocator> delivered = new HashMap<>();

   public Object locateBean(String path) {
      BeanLocator togo = delivered.get(path);
      if (togo == null) {
         EvalResponse parent = (EvalResponse) responseBeanLocator.locateBean(path);
         togo = new AnswersBeanLocator(parent, localResponsesLogic);
         delivered.put(path, togo);
      }
      return togo;
   }
}
