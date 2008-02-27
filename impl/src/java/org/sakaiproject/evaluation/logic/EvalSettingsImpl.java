/**
 * $Id$
 * $URL$
 * EvalSettingsImpl.java - evaluation - Dec 28, 2006 10:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalConfig;
import org.sakaiproject.evaluation.utils.SettingsLogicUtils;


/**
 * Implementation for the settings control
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalSettingsImpl implements EvalSettings {

   private static Log log = LogFactory.getLog(EvalSettingsImpl.class);

   // spring setters
   private EvaluationDao evaluationDao;
   public void setEvaluationDao(EvaluationDao evaluationDao) {
      this.evaluationDao = evaluationDao;
   }

   private HashSet<String> booleanSettings = new HashSet<String>();

   /**
    * spring init
    */
   public void init() {
      log.debug("init");

      // convert the array into a Set to make it easier to work with
      for (int i = 0; i < BOOLEAN_SETTINGS.length; i++) {
         booleanSettings.add(BOOLEAN_SETTINGS[i]);
      }      

      // count the current config settings
      int count = evaluationDao.countAll(EvalConfig.class);
      if (count > 0) {
         log.info("Updating boolean only evaluation system settings to ensure they are not null...");
         // check the existing boolean settings for null values and fix them if they are null
         for (String setting : booleanSettings) {
            if (get(setting) == null) {
               set(setting, false);
            }
         }
      }
   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvaluationSettings#get(java.lang.Object)
    */
   public Object get(String settingConstant) {
      log.debug("Getting admin setting for: " + settingConstant);
      String name = SettingsLogicUtils.getName(settingConstant);
      String type = SettingsLogicUtils.getType(settingConstant);

      Object setting = null;
      EvalConfig c = getConfigByName(name);
      if (c == null) {
         if (booleanSettings.contains(settingConstant)) {
            // if this boolean is null then make it false instead
            setting = Boolean.FALSE;
         }
      } else {
         if (type.equals("java.lang.Boolean")) {
            setting = new Boolean( c.getValue() );
         } else if (type.equals("java.lang.Integer")) {
            setting = new Integer( c.getValue() );
         } else if (type.equals("java.lang.Float")) {
            setting = new Float( c.getValue() );
         } else {
            setting = c.getValue();
         }
      }
      return setting;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvaluationSettings#set(java.lang.Object, java.lang.Object)
    */
   public boolean set(String settingConstant, Object settingValue) {
      log.debug("Setting admin setting to ("+settingValue+") for: " + settingConstant);
      String name = SettingsLogicUtils.getName(settingConstant);
      String type = SettingsLogicUtils.getType(settingConstant);

      // retrieve the current setting if it exists
      EvalConfig c = getConfigByName(name);

      // unset (clear) this setting by removing the value from the database
      if (settingValue == null) {
         if (c != null) {
            try {
               evaluationDao.delete(c); // now remove from storage
            } catch (Exception e) {
               log.error("Could not clear system setting:" + name + ":" + type, e);
               return false;
            }
         }
         return true;
      }

      // make sure the type is the one set
      Class<?> typeClass;
      try {
         typeClass = Class.forName(type);
      } catch (ClassNotFoundException e) {
         throw new IllegalArgumentException("Invalid class type " + type + " in constant: " + settingConstant, e);
      }

      if ( ! typeClass.isInstance(settingValue) ) {
         throw new IllegalArgumentException("Input class type (" + typeClass + ") does not match setting type:" + type);
      }

      // create a new setting if needed or update an existing one
      String value = settingValue.toString();
      if (c == null) {
         c = new EvalConfig(new Date(), name, value);
      } else {
         c.setLastModified(new Date());
         c.setValue(value);
      }

      try {
         evaluationDao.save(c); // now save in the database
      } catch (Exception e) {
         log.error("Could not save system setting:" + name + ":" + value, e);
         return false;
      }
      return true;
   }

   /**
    * @param name the name value of the Config item
    * @return a Config object or null if none found
    */
   @SuppressWarnings("unchecked")
   private EvalConfig getConfigByName(String name) {
      List<EvalConfig> l = evaluationDao.findByProperties(EvalConfig.class, 
            new String[] {"name"}, new Object[] {name});
      if (l.size() > 0) {
         return (EvalConfig) l.get(0);
      }
      log.debug("No admin setting for this constant:" + name);
      return null;
   }

}
