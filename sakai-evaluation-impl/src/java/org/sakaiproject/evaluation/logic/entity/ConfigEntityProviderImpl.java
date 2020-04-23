/**
 * Copyright (c) 2005-2020 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.evaluation.logic.entity;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.event.EventReceiver;
import org.sakaiproject.evaluation.logic.EvalSettings;

import lombok.extern.slf4j.Slf4j;

/**
 * Allows for detecting changes to the config via events,
 * note the id should be the constant from {@link EvalSettings}
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@Slf4j
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
        //If it gets an entity prefix back, convert the value to an id
        if (id.contains(ENTITY_PREFIX)) {
            id = new EntityReference(id).getId();
        }  
        if (EvalSettings.EVENT_SET_ONE_CONFIG.equals(eventName)) {
            if (log.isDebugEnabled()) {
                log.debug("eventName (" + eventName + ") settings.resetCache(" + id + ")");
            }
            settings.resetCache(id);
        } else if (EvalSettings.EVENT_SET_MANY_CONFIG.equals(eventName)) {
            if (log.isDebugEnabled()) {
                log.debug("eventName (" + eventName + ") settings.resetCache(null)");
            }
            settings.resetCache(null);
        }
        else {  
            if (log.isDebugEnabled()) {
                log.debug("eventName (" + eventName + ") EVENT received UNKNOWN!");
            }
        }
    }

}
