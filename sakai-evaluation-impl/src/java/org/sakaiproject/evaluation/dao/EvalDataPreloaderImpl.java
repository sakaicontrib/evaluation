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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;

/**
 * This is a simple bean which will initiate the data preloading sequence with locks
 * when it initializes if autoDDL is enabled
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalDataPreloaderImpl {

   private static final Log LOG = LogFactory.getLog(PreloadDataImpl.class);

   public static String EVAL_PRELOAD_LOCK = "data.preload.lock";
   public static String EVAL_FIXUP_LOCK = "data.fixup.lock";

   private EvaluationDao dao;
   public void setDao(EvaluationDao evaluationDao) {
      this.dao = evaluationDao;
   }

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private PreloadDataImpl preloadData;
   public void setPreloadData(PreloadDataImpl preloadData) {
      this.preloadData = preloadData;
   }

   public void init() {
      boolean autoDDL = externalLogic.getConfigurationSetting(EvalExternalLogic.SETTING_AUTO_DDL, false);
      String serverId = externalLogic.getConfigurationSetting(EvalExternalLogic.SETTING_SERVER_ID, "UNKNOWN_SERVER_ID");
      boolean killSakaiOnError = externalLogic.getConfigurationSetting(EvalExternalLogic.SETTING_EVAL_CAN_KILL_SAKAI, false);
      if (autoDDL) {
         LOG.info("Auto DDL enabled: Checking preload data exists...");
         if (! preloadData.checkCriticalDataPreloaded() ) {
            LOG.info("Preload data missing, preparing to preload critical evaluation system data");
            Boolean gotLock = dao.obtainLock(EVAL_PRELOAD_LOCK, serverId, 10000);
            if (gotLock == null) {
            	if(killSakaiOnError) {
            		throw new IllegalStateException("Failure attempting to obtain lock ("+EVAL_PRELOAD_LOCK+") and preload evaluation system data, " 
            				+ "see logs just before this for more details, system terminating..."); 
            	} else {
            		LOG.warn("Failure attempting to obtain lock ("+EVAL_PRELOAD_LOCK+") and preload evaluation system data");
            	}
            } else if (gotLock) {
               preloadData.preload();
               dao.releaseLock(EVAL_PRELOAD_LOCK, serverId);
            }
         }
      } else {
         LOG.info("Auto DDL disabled: Skipping data preloading...");
         if ( preloadData.checkCriticalDataPreloaded() ) {
            LOG.info("Preloaded data is present");
         } else {
        	if(killSakaiOnError) {
        		throw new IllegalStateException("Preloaded data is missing, evaluation cannot start up in this state, " +
        				"you must either enable the auto.ddl flag or preload the critical system data (config settings, email templates, scales) " +
            			"manually, evaluation system shutting down...");
        	} else {
        		LOG.warn("Preloaded data is missing, evaluation cannot start up in this state. " +
        				"You must either enable the auto.ddl flag or preload the critical system data " + 
        				"(config settings, email templates, scales) manually)");
        	}
         }
      }

      Boolean gotLock = dao.obtainLock(EVAL_FIXUP_LOCK, serverId, 3000);
      if (gotLock != null && gotLock) {
         dao.fixupDatabase();
         dao.releaseLock(EVAL_FIXUP_LOCK, serverId);
      }
   }

}
