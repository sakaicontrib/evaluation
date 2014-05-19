package org.sakaiproject.evaluation.model;

import java.util.Date;
import java.util.List;

/**
 * This is an item in a template,
 * Effectively items are reusable and therefore this is here to indicate an instance of an item
 * within a template, ordering and layout details are specified here
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EvalTemplateItem implements java.io.Serializable {

    // Fields    

    private Long id;

    private String eid;

    private Date lastModified;

    private String owner;

    private EvalTemplate template;

    private EvalItem item;

    private Integer displayOrder;

    private String category;

    private String hierarchyLevel;

    private String hierarchyNodeId;

    private Boolean usesNA;

    /**
     * Indicates that there should be a comments box displayed beneath this item when displayed to the user
     */
    private Boolean usesComment;

    private Integer displayRows;

    private String scaleDisplaySetting;

    private Boolean blockParent;

    private Long blockId;

    /**
     * Special flag used for indicating if the results of answering this template item should be shared
     */
    private String resultsSharing;

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
     * If this is not null then template item will be added to the copy of the template 
     * used for the evaluations with the related linking autoUseTag when the eval is created
     */
    private String autoUseTag;

    /**
     * If this is not null then it means that this item was saved into the template as part of an autoUseInsertion
     * and this field will be the autoUseTag which caused the insertion, primarily used to cause the item
     * to be displayed specially
     */
    private String autoUseInsertionTag;

    /**
     * If true then this item must be answered when taking evaluations
     */
    private Boolean compulsory;

    // NON-persistent fields

    /**
     * If this is a block parent then this can hold the list of child items,
     * this is only needed if rendering, however, in the case of rendering,
     * it MUST be included
     */
    public List<EvalTemplateItem> childTemplateItems = null;

    // Constructors

    /** default constructor */
    public EvalTemplateItem() {
    }

    /** minimal constructor */
    public EvalTemplateItem(String owner, EvalTemplate template, EvalItem item, Integer displayOrder, 
            String category, String hierarchyLevel,
            String hierarchyNodeId) {
        this(owner, template, item, displayOrder, category, hierarchyLevel, hierarchyNodeId, null, 
                null, null, null, null, null, null, null);
    }

    /** full constructor */
    public EvalTemplateItem(String owner, EvalTemplate template, EvalItem item, Integer displayOrder, 
            String category, String hierarchyLevel,
            String hierarchyNodeId, Integer displayRows, String scaleDisplaySetting, Boolean usesNA, Boolean usesComment, 
            Boolean compulsory, Boolean blockParent, Long blockId, String resultsSharing) {
        this.lastModified = new Date();
        this.owner = owner;
        this.template = template;
        this.item = item;
        this.displayOrder = displayOrder;
        this.category = category;
        this.hierarchyLevel = hierarchyLevel;
        this.hierarchyNodeId = hierarchyNodeId;
        this.displayRows = displayRows;
        this.scaleDisplaySetting = scaleDisplaySetting;
        this.usesNA = (usesNA == null ? Boolean.FALSE : usesNA); // default to false
        this.usesComment = (usesComment == null ? Boolean.FALSE : usesComment); // default to false
        this.compulsory = (compulsory == null ? Boolean.FALSE : compulsory); // default to false
        this.blockParent = blockParent;
        this.blockId = blockId;
        this.resultsSharing = resultsSharing;
    }

    @Override
    public String toString() {
        return this.id + ":item="+(this.item == null ? "NULL" : this.item.getId())
        + ":template="+(this.template == null ? "NULL" : this.template.getId())
        +":cat=" + this.category + ":order=" + this.displayOrder + ":copyOf=" + this.copyOf;
    }


    // Property accessors
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEid() {
        return this.eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public Date getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public EvalTemplate getTemplate() {
        return this.template;
    }

    public void setTemplate(EvalTemplate template) {
        this.template = template;
    }

    public EvalItem getItem() {
        return this.item;
    }

    public void setItem(EvalItem item) {
        this.item = item;
    }

    public Integer getDisplayOrder() {
        return this.displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getHierarchyLevel() {
        return this.hierarchyLevel;
    }

    public void setHierarchyLevel(String hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public String getHierarchyNodeId() {
        return this.hierarchyNodeId;
    }

    public void setHierarchyNodeId(String hierarchyNodeId) {
        this.hierarchyNodeId = hierarchyNodeId;
    }

    public Integer getDisplayRows() {
        return this.displayRows;
    }

    public void setDisplayRows(Integer displayRows) {
        this.displayRows = displayRows;
    }

    public String getScaleDisplaySetting() {
        return this.scaleDisplaySetting;
    }

    public void setScaleDisplaySetting(String scaleDisplaySetting) {
        this.scaleDisplaySetting = scaleDisplaySetting;
    }

    public Boolean getUsesNA() {
        return this.usesNA;
    }

    public void setUsesNA(Boolean usesNA) {
        this.usesNA = usesNA;
    }

    public Boolean getBlockParent() {
        return this.blockParent;
    }

    public void setBlockParent(Boolean blockParent) {
        this.blockParent = blockParent;
    }

    public Long getBlockId() {
        return this.blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public String getResultsSharing() {
        return resultsSharing;
    }

    public void setResultsSharing(String resultsSharing) {
        this.resultsSharing = resultsSharing;
    }

    public Long getCopyOf() {
        return copyOf;
    }

    public void setCopyOf(Long copyOf) {
        this.copyOf = copyOf;
    }

    public Boolean getUsesComment() {
        return usesComment;
    }

    public void setUsesComment(Boolean usesComment) {
        this.usesComment = usesComment;
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


    public String getAutoUseInsertionTag() {
        return autoUseInsertionTag;
    }


    public void setAutoUseInsertionTag(String autoUseInsertionTag) {
        this.autoUseInsertionTag = autoUseInsertionTag;
    }

    public Boolean isCompulsory() {
        return this.compulsory;
    }

    public void setCompulsory(Boolean compulsory) {
        this.compulsory = compulsory;
    }


}
