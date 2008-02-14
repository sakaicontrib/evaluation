/**
 * $Id$
 * $URL$
 * EvalDataPreloaderImpl.java - evaluation - Feb 14, 2008 11:12:41 AM - azeckoski
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

   private static Log log = LogFactory.getLog(PreloadDataImpl.class);

   public static String EVAL_PRELOAD_LOCK = "data.preload.lock";

   private EvaluationDao evaluationDao;
   public void setEvaluationDao(EvaluationDao evaluationDao) {
      this.evaluationDao = evaluationDao;
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
      boolean autoDDL = externalLogic.getConfigurationSetting(EvalExternalLogic.SETTING_AUTO_DLL, false);
      if (autoDDL) {
         log.info("Auto DDL enabled: Checking preload data exists...");
         String serverId = externalLogic.getConfigurationSetting(EvalExternalLogic.SETTING_SERVER_ID, "UNKNOWN_SERVER_ID");
         evaluationDao.lockAndExecuteRunnable(serverId, EVAL_PRELOAD_LOCK, preloadData);
      }
   }

}
