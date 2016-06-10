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
package org.sakaiproject.evaluation.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.dao.PreloadDataImpl;


/**
 * This preloads data needed for testing<br/>
 * Do not load this data into a live or production database<br/>
 * Load this after the normal preload<br/>
 * Add the following (or something like it) to a spring beans def file:<br/>
 * <pre>
	<!-- create a test data preloading bean -->
	<bean id="org.sakaiproject.evaluation.test.PreloadTestData"
		class="org.sakaiproject.evaluation.test.PreloadTestDataImpl"
		init-method="init">
		<property name="evaluationDao"
			ref="org.sakaiproject.evaluation.dao.EvaluationDao" />
		<property name="preloadData"
			ref="org.sakaiproject.evaluation.dao.PreloadData" />
	</bean>
 * </pre>
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class PreloadTestDataImpl {

   private static final Log LOG = LogFactory.getLog(PreloadTestDataImpl.class);

   private EvaluationDao dao;
   public void setDao(EvaluationDao dao) {
      this.dao = dao;
   }

   private PreloadDataImpl preloadData;
   public void setPreloadData(PreloadDataImpl preloadData) {
      this.preloadData = preloadData;
   }


   private EvalTestDataLoad etdl;
   /**
    * @return the test data loading class with copies of all saved objects
    */
   public EvalTestDataLoad getEtdl() {
      return etdl;
   }

   public void init() {
      LOG.info("INIT");
      if (preloadData == null) {
         throw new NullPointerException("PreloadDataImpl must be loaded before this class");
      } else {
         // run the data preloading method
         preloadData.preload();
      }
      preloadDB();
   }

   /**
    * Preload the data
    */
   public void preloadDB(){
      LOG.info("preloading DB...");
      etdl = new EvalTestDataLoad(dao);
   }
}
