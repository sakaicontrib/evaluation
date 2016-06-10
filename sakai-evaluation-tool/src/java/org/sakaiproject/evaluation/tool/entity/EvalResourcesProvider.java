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
package org.sakaiproject.evaluation.tool.entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.util.ResourceLoader;

public class EvalResourcesProvider extends AbstractEntityProvider implements CoreEntityProvider, AutoRegisterEntityProvider, Resolvable, Outputable, ActionsExecutable {

	public static final String PREFIX = "eval-resources";
	public static final String CUSTOM_ACTION_BUNDLE = "message-bundle";
	public static final String CUSTOM_ACTION_LOCALE = "locale";
	public static final String MESSAGES_BUNDLE_DIR = "org.sakaiproject.evaluation.tool.bundle.messages";

	public String getEntityPrefix() {
        return PREFIX;
    }

	public boolean entityExists(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	public Object getEntity(EntityReference ref) {
		return null;
	}

	public String[] getHandledOutputFormats() {
        return new String[] {Formats.JSON};
    }
	
	@SuppressWarnings("unchecked")
	@EntityCustomAction(action=CUSTOM_ACTION_BUNDLE,viewKey=EntityView.VIEW_LIST)
	public Map<String, String> getMessageBundle() {
		Map<String, String> bundleMap = new HashMap<>();
		ResourceLoader resourceLoader = getResourceLoader();
    	Iterator<Entry<String, String>> selector = resourceLoader.entrySet().iterator();
		while ( selector.hasNext() ) {
        	Entry<String, String> pairs = selector.next();
        	bundleMap.put(pairs.getKey(), pairs.getValue());
		}
		
		return bundleMap;
	}
	
	@EntityCustomAction(action=CUSTOM_ACTION_LOCALE,viewKey=EntityView.VIEW_LIST)
	public String getLocale() {
		return getResourceLoader().getLocale().toString();
	}
	
	private ResourceLoader getResourceLoader(){
		return new ResourceLoader(MESSAGES_BUNDLE_DIR);
	}

}
