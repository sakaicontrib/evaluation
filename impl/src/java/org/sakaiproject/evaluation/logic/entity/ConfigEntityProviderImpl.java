/**
 * $Id$
 * $URL$
 * ConfigEntityProviderImpl.java - evaluation - Feb 28, 2008 11:50:32 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.entity;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.event.EventReceiver;
import org.sakaiproject.evaluation.logic.EvalSettings;

/**
 * Allows for detecting changes to the config via events,
 * note the id should be the constant from {@link EvalSettings}
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ConfigEntityProviderImpl implements ConfigEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, EventReceiver {

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

    public String getEntityPrefix() {
        return ENTITY_PREFIX;
    }

    public boolean entityExists(String id) {
        boolean exists = false;
        if (settings.get(id) != null) {
            exists = true;
        }
        return exists;
    }

    public String[] getEventNamePrefixes() {
        return new String[] {EvalSettings.EVENT_SET_ONE_CONFIG, EvalSettings.EVENT_SET_MANY_CONFIG};
    }

    public String getResourcePrefix() {
        return "";
    }

    public void receiveEvent(String eventName, String id) {
        if (EvalSettings.EVENT_SET_ONE_CONFIG.equals(eventName)) {
            settings.resetCache(id);
        } else if (EvalSettings.EVENT_SET_MANY_CONFIG.equals(eventName)) {
            settings.resetCache(null);
        }
    }

}
