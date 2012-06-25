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
package org.sakaiproject.evaluation.model;

import java.util.Date;

/**
 * Stores the tag relation for everything that is tagged in the system
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalTags implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Date lastModified;
    /**
     * The tag to associate with this entity
     */
    private String tag;
    /**
     * the entity type for the thing being tagged,
     * this should probably be the entity prefix and not the actual object type
     */
    private String entityType;
    /**
     * the unique id of the thing being tagged (unique within the prefix anyway) 
     */
    private String entityId;

    public EvalTags() {
    }

    /**
     * MINIMAL constructor
     * 
     * @param tag the tag to associate with this entity
     * @param entityType entity type for the thing being tagged,
     * this should probably be the entity prefix and not the actual object type
     * @param entityId unique id of the thing being tagged
     */
    public EvalTags(String tag, String entityType, String entityId) {
        if (this.lastModified == null) {
            this.lastModified = new Date();
        }
        this.tag = tag;
        this.entityType = entityType;
        this.entityId = entityId;
    }

    // GETTERS AND SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

}
