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
package org.sakaiproject.evaluation.logic.entity;

import java.util.Map;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Deleteable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.model.EvalTemplate;

/**
 * Implementation for the entity provider for evaluation templates
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class TemplateEntityProviderImpl implements TemplateEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, Resolvable, Outputable, Deleteable {

	private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }
   
   private DeveloperHelperService developerHelperService;
   public void setDeveloperHelperService (DeveloperHelperService developerHelperService){
	   this.developerHelperService = developerHelperService;
   }
  


	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.EntityProvider#getEntityPrefix()
	 */
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider#entityExists(java.lang.String)
	 */
	public boolean entityExists(String id) {
	   boolean exists = false;
		Long templateId;
		try {
			templateId = new Long(id);
			if (authoringService.getTemplateById(templateId) != null) {
			   exists = true;
			}
		} catch (NumberFormatException e) {
			// invalid number so roll through to the false
		   exists = false;
		}
		return exists;
	}

	public Object getEntity(EntityReference ref) {
		// TODO Auto-generated method stub
		EvalTemplate item = authoringService.getTemplateById(new Long(ref.getId()));
		if(item != null && isAllowedAccessEvalTemplateItem(ref)){
			return (developerHelperService.cloneBean(item, 1, new String[]{})); 
		}
		else
			throw new IllegalArgumentException("id is invalid.");
		
	}


	public String[] getHandledOutputFormats() {
		// TODO Auto-generated method stub
		return new String[] {Formats.JSON, Formats.XML};
	}

	protected boolean isAllowedAccessEvalTemplateItem(EntityReference ref) {
	    // check if the current user can access this
	    String userRef = developerHelperService.getCurrentUserReference();
	    if (userRef == null) {
	        throw new SecurityException("Anonymous users may not view this Eval-item");
	    } else {
	        if (!developerHelperService.isUserAllowedInEntityReference(userRef, "VIEW", ref.getId())) {
	            throw new SecurityException("This Eval-item is not accessible for the current user: " + userRef);
	        }
	    }
	    return true;
	}



	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		// TODO Auto-generated method stub
		if(isAllowedAccessEvalTemplateItem(ref) && authoringService.canRemoveTemplate(developerHelperService.getCurrentUserId(), new Long(ref.getId()))){
			authoringService.deleteTemplate(new Long(ref.getId()), developerHelperService.getCurrentUserId());
		}
		
	}


}
