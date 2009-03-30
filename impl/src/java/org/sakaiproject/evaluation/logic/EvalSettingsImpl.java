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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalConfig;
import org.sakaiproject.evaluation.utils.SettingsLogicUtils;
import org.sakaiproject.genericdao.api.search.Search;

/**
 * Implementation for the settings control
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalSettingsImpl implements EvalSettings {

    private static Log log = LogFactory.getLog(EvalSettingsImpl.class);

    private EvaluationDao dao;
    public void setDao(EvaluationDao dao) {
        this.dao = dao;
    }

    private EvalExternalLogic externalLogic;
    public void setExternalLogic(EvalExternalLogic externalLogic) {
        this.externalLogic = externalLogic;
    }


    private ConcurrentHashMap<String, EvalConfig> configCache = new ConcurrentHashMap<String, EvalConfig>();
    private HashSet<String> booleanSettings = new HashSet<String>();


    /**
     * spring init
     */
    public void init() {
        log.debug("init");

        log.debug("BOOLEAN_SETTINGS " + BOOLEAN_SETTINGS);

        // convert the array into a Set to make it easier to work with
        for (int i = 0; i < BOOLEAN_SETTINGS.length; i++) {
            booleanSettings.add(BOOLEAN_SETTINGS[i]);
        }


        // count the current config settings
        int count = dao.countAll(EvalConfig.class);
        if (count > 0) {
            log.info("Updating boolean only evaluation system settings to ensure they are not null...");
            // check the existing boolean settings for null values and fix them if they are null
            for (String setting : booleanSettings) {
                if (get(setting) == null) {
                    set(setting, false);
                }
            }
        }

        // initialize the cache
        resetCache(null);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvaluationSettings#get(java.lang.Object)
     */
    public Object get(String settingConstant) {
        log.debug("Getting admin setting for: " + settingConstant);
        String name = SettingsLogicUtils.getName(settingConstant);
        String type = SettingsLogicUtils.getType(settingConstant);

        Object setting = null;
        EvalConfig c = getConfigByName(name, true);
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
        EvalConfig c = getConfigByName(name, false);

        // unset (clear) this setting by removing the value from the database
        if (settingValue == null) {
            if (c != null) {
                try {
                    dao.delete(c); // now remove from storage
                    externalLogic.registerEntityEvent(EVENT_SET_ONE_CONFIG, EvalConfig.class, settingConstant); // register event
                    configCache.put(name, new Null()); // update the cache
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
            c = new EvalConfig(name, value);
        } else {
            c.setLastModified(new Date());
            c.setValue(value);
        }

        try {
            dao.save(c); // now save in the database
            externalLogic.registerEntityEvent(EVENT_SET_ONE_CONFIG, EvalConfig.class, settingConstant); // register event
            configCache.put(name, c); // update the cache
        } catch (Exception e) {
            log.error("Could not save system setting:" + name + ":" + value, e);
            return false;
        }
        return true;
    }

    /**
     * @param name the name value of the Config item
     * @param useCache if true then use cached values and return no persistent objects,
     * else always get the persistent object if exists
     * @return a Config object or null if none found
     */
    protected EvalConfig getConfigByName(String name, boolean useCache) {
        EvalConfig config = null;
        boolean found = false;
        if (useCache) {
            // get the cached version if it exists
            if (configCache.containsKey(name)) {
                found = true;
                config = configCache.get(name);
                if (config instanceof Null) {
                    config = null;
                }
            }
        }
        if (! found) {
            List<EvalConfig> l = dao.findBySearch(EvalConfig.class, 
                    new Search("name", name) );
            if (l.size() > 0) {
                config = (EvalConfig) l.get(0);
            } else {
                log.debug("No admin setting for this constant:" + name);
            }
            if (useCache) {
                if (config == null) {
                    // we want to store the fact that this is actually a null
                    configCache.put(name, new Null());
                } else {
                    // make the object non-persistent by copying it
                    config = new EvalConfig(name, config.getValue());
                    configCache.put(name, config); // update the cache
                }
            }
        }
        return config;
    }

    /**
     * clear out the cache and reload all config settings if the settingConstant is null,
     * if not null then clean out a single item from the cache,
     * it will be reloaded from the DB the next time someone attempts to fetch it
     * @param settingConstant (OPTIONAL) a setting constant from {@link EvalSettings}
     */
    public void resetCache(String settingConstant) {
        if (settingConstant == null) {
            // clear out cache
            configCache.clear();
            // reload all cache items
            List<EvalConfig> l = dao.findAll(EvalConfig.class);
            for (EvalConfig config : l) {
                // copy the values to avoid putting persistent objects in the cache
                config = new EvalConfig(config.getName(), config.getValue());
                configCache.put(config.getName(), config);
            }
            log.info("Resetting config settings cache: cleared and reloaded all "+l.size()+" values");
        } else {
            String name = SettingsLogicUtils.getName(settingConstant);
            if (configCache.containsKey(name)) {
                log.info("Resetting config settings cache: cleared single value: " + name);
                configCache.remove(name);
            }
        }
    }

    /**
     * Marker class to indicate a null since CHM cannot store null
     * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
     */
    private class Null extends EvalConfig { }

}
