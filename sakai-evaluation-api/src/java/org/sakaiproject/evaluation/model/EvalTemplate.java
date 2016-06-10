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

//Generated Mar 20, 2007 10:08:13 AM by Hibernate Tools 3.2.0.beta6a

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * This represents a template for an evaluation,
 * basically it controls the set of items and the ordering and layout of
 * an evaluation (this is like the original of a paper survey)
 */
public class EvalTemplate implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    // Fields

    private Long id;

    private String eid;

    private Date lastModified;

    private String owner;

    private String type;

    private String title;

    private String description;

    private String sharing;

    private Boolean expert;

    private String expertDescription;

    private Set<EvalTemplateItem> templateItems = new HashSet<>(0);

    private Boolean locked;

    /**
     * Indicates that this is a copy of an item and therefore should be hidden from views and 
     * only revealed when taking/previewing (not as part of item banks, etc.),
     * this will be the id of the persistent object it is a copy of
     */
    private Long copyOf;

    /**
     * Indicates that the object is hidden from the control views and will not be visible to the user for editing/removal
     */
    private boolean hidden = false;

    /**
     * If this is not null then all items from this template will be added to the copy of the template 
     * used for the evaluations with the related linking autoUseTag when the eval is created
     */
    private String autoUseTag;


    // Constructors

    /** default constructor */
    public EvalTemplate() {
    }

    /** 
     * minimal constructor
     * @param owner
     * @param type
     * @param title
     * @param sharing
     * @param expert 
     */
    public EvalTemplate(String owner, String type, String title, String sharing, Boolean expert) {
        this(owner, type, title, title, sharing, expert, null, null, null, false);
    }

    /** 
     * full constructor
     * @param owner
     * @param type
     * @param title
     * @param description
     * @param sharing
     * @param expert
     * @param expertDescription
     * @param templateItems
     * @param locked
     * @param hidden 
     */
    public EvalTemplate(String owner, String type, String title, String description, String sharing,
            Boolean expert, String expertDescription, Set<EvalTemplateItem> templateItems, Boolean locked,
            boolean hidden) {
        this.lastModified = new Date();
        this.owner = owner;
        this.type = type;
        this.title = title;
        this.description = description;
        this.sharing = sharing;
        this.expert = expert;
        this.expertDescription = expertDescription;
        if (templateItems != null) {
            this.templateItems = templateItems;
        }
        this.locked = locked;
        this.hidden = hidden;
    }

    @Override
    public String toString() {
        return this.getId() + ":" + this.getTitle() + ":" + this.getType() + ":copyOf=" + this.copyOf;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public Boolean getExpert() {
        return expert;
    }

    public void setExpert(Boolean expert) {
        this.expert = expert;
    }

    public String getExpertDescription() {
        return expertDescription;
    }

    public void setExpertDescription(String expertDescription) {
        this.expertDescription = expertDescription;
    }

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

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getSharing() {
        return sharing;
    }

    public void setSharing(String sharing) {
        this.sharing = sharing;
    }

    public Set<EvalTemplateItem> getTemplateItems() {
        return templateItems;
    }

    public void setTemplateItems(Set<EvalTemplateItem> templateItems) {
        this.templateItems = templateItems;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCopyOf() {
        return copyOf;
    }

    public void setCopyOf(Long copyOf) {
        this.copyOf = copyOf;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }


    public String getAutoUseTag() {
        return autoUseTag;
    }


    public void setAutoUseTag(String autoUseTag) {
        this.autoUseTag = autoUseTag;
    }

}
