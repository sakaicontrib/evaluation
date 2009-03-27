/******************************************************************************
 * AssignGroupEntityProviderImpl.java - created by aaronz on 28 May 2007
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.azeckoski.reflectutils.ReflectUtils;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.entity.AssignGroupEntityProvider;
import org.sakaiproject.evaluation.model.EvalAssignGroup;

/**
 * Implementation for the entity provider for evaluation groups
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class AssignGroupEntityProviderImpl implements AssignGroupEntityProvider, CoreEntityProvider, RESTful, AutoRegisterEntityProvider {

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalEvaluationSetupService evaluationSetupService;
    public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
        this.evaluationSetupService = evaluationSetupService;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.EntityProvider#getEntityPrefix()
     */
    public String getEntityPrefix() {
        return AssignGroupEntityProvider.ENTITY_PREFIX;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider#entityExists(java.lang.String)
     */
    public boolean entityExists(String id) {
        Long assignGroupId;
        try {
            assignGroupId = new Long(id);
            if (evaluationService.getAssignGroupById(assignGroupId) != null) {
                return true;
            }
        } catch (NumberFormatException e) {
            // invalid number so roll through to the false
        }
        return false;
    }

    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        EvalAssignGroup assignGroup = (EvalAssignGroup) entity;
        String userId = commonLogic.getCurrentUserId();
        evaluationSetupService.saveAssignGroup(assignGroup, userId);
        return assignGroup.getId().toString();
    }

    public Object getSampleEntity() {
        return new EvalAssignGroup();
    }

    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        EvalAssignGroup assignGroup = (EvalAssignGroup) entity;
        String userId = commonLogic.getCurrentUserId();
        evaluationSetupService.saveAssignGroup(assignGroup, userId);
    }

    public Object getEntity(EntityReference ref) {
        Long assignGroupId = getIdFromRef(ref);
        EvalAssignGroup assignGroup = evaluationService.getAssignGroupById(assignGroupId);
        EvalAssignGroup clone = cloneEAG(assignGroup);
        return clone;
    }

    public void deleteEntity(EntityReference ref, Map<String, Object> params) {
        Long assignGroupId = getIdFromRef(ref);
        String userId = commonLogic.getCurrentUserId();
        evaluationSetupService.deleteAssignGroup(assignGroupId, userId);
    }

    public List<?> getEntities(EntityReference ref, Search search) {
        Restriction r = search.getRestrictionByProperties(new String[] {"eval","evaluation","evalId","evaluationId"});
        if (r == null) {
            throw new IllegalArgumentException("Must specify an evaluation id (evalId param) in order to fetch assign groups");
        }
        Long evaluationId = ReflectUtils.getInstance().convert(r.getStringValue(), Long.class);
        List<EvalAssignGroup> assignGroups = evaluationService.getAssignGroupsForEvals(new Long[] {evaluationId}, true, true).get(evaluationId);
        List<EvalAssignGroup> groups = new ArrayList<EvalAssignGroup>(assignGroups.size());
        for (EvalAssignGroup evalAssignGroup : assignGroups) {
            EvalAssignGroup clone = cloneEAG(evalAssignGroup);
            groups.add(clone);
        }
        return groups;
    }

    public String[] getHandledOutputFormats() {
        return new String[] {Formats.JSON, Formats.XML};
    }

    public String[] getHandledInputFormats() {
        return new String[] {Formats.HTML, Formats.JSON, Formats.XML};
    }

    // Added for compatibility with 1.3.3
    public String createEntity(EntityReference ref, Object entity) {
        return createEntity(ref, entity, new HashMap<String, Object>(0));
    }

    public void updateEntity(EntityReference ref, Object entity) {
        updateEntity(ref, entity, new HashMap<String, Object>(0));
    }

    public void deleteEntity(EntityReference ref) {
        deleteEntity(ref, new HashMap<String, Object>(0));
    }

    /**
     * Cloning is required to break us out of the hibernate weirdness,
     * if hibernate goes away then we can avoid doing this
     * @param assignGroup
     * @return the clone of the assign group
     */
    protected EvalAssignGroup cloneEAG(EvalAssignGroup assignGroup) {
        return ReflectUtils.getInstance().clone(assignGroup, 1, new String[] {"evaluation"});
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
