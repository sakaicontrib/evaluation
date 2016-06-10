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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.azeckoski.reflectutils.ReflectUtils;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;

/**
 * Implementation for the entity provider for evaluation adhoc groups
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class AdhocGroupEntityProviderImpl implements AdhocGroupEntityProvider, CoreEntityProvider, RESTful, AutoRegisterEntityProvider {

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.EntityProvider#getEntityPrefix()
     */
    public String getEntityPrefix() {
        return AdhocGroupEntityProvider.ENTITY_PREFIX;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider#entityExists(java.lang.String)
     */
    public boolean entityExists(String id) {
        Long adhocGroupId;
        try {
            adhocGroupId = new Long(id);
            if (commonLogic.getAdhocGroupById(adhocGroupId) != null) {
                return true;
            }
        } catch (NumberFormatException e) {
            // invalid number so roll through to the false
        }
        return false;
    }

    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        EvalAdhocGroup adhocGroup = (EvalAdhocGroup) entity;
        commonLogic.saveAdhocGroup(adhocGroup);
        return adhocGroup.getId().toString();
    }

    public Object getSampleEntity() {
        return new EvalAdhocGroup();
    }

    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        EvalAdhocGroup adhocGroup = (EvalAdhocGroup) entity;
        commonLogic.saveAdhocGroup(adhocGroup);
    }

    public Object getEntity(EntityReference ref) {
        Long adhocGroupId = getIdFromRef(ref);
        EvalAdhocGroup adhocGroup = commonLogic.getAdhocGroupById(adhocGroupId);
        EvalAdhocGroup clone = cloneEAG(adhocGroup);
        return clone;
    }

    public void deleteEntity(EntityReference ref, Map<String, Object> params) {
        Long adhocGroupId = getIdFromRef(ref);
        commonLogic.deleteAdhocGroup(adhocGroupId);
    }

    public List<?> getEntities(EntityReference ref, Search search) {
        String userId = commonLogic.getCurrentUserId();
        List<EvalAdhocGroup> adhocGroups = commonLogic.getAdhocGroupsForOwner(userId);
        return adhocGroups;
    }

    public String[] getHandledOutputFormats() {
        return new String[] {Formats.JSON, Formats.XML};
    }

    public String[] getHandledInputFormats() {
        return new String[] {Formats.HTML, Formats.JSON, Formats.XML};
    }

    // Added for compatibility with 1.3.3
    public String createEntity(EntityReference ref, Object entity) {
        return createEntity(ref, entity, new HashMap<>(0));
    }

    public void updateEntity(EntityReference ref, Object entity) {
        updateEntity(ref, entity, new HashMap<>(0));
    }

    public void deleteEntity(EntityReference ref) {
        deleteEntity(ref, new HashMap<>(0));
    }

    /**
     * Cloning is required to break us out of the hibernate weirdness,
     * if hibernate goes away then we can avoid doing this
     * @param adhocGroup
     * @return the clone of the group
     */
    protected EvalAdhocGroup cloneEAG(EvalAdhocGroup adhocGroup) {
        return ReflectUtils.getInstance().clone(adhocGroup, 1, new String[] {"evaluation"});
    }

    /**
     * Extract a numeric id from the ref if possible
     * @param ref the entity reference
     * @return the Long number version of the id
     * @throws IllegalArgumentException if the number cannot be extracted
     */
    protected Long getIdFromRef(EntityReference ref) {
        Long id = null;
        String refId = ref.getId();
        if (refId != null) {
            try {
                id = Long.valueOf(refId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number found in reference ("+ref+") id: " + e);
            }
        } else {
            throw new IllegalArgumentException("No id in reference ("+ref+") id, cannot extract numeric id");
        }
        return id;
    }

}
