
package org.sakaiproject.evaluation.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a question/item that would be used in an evaluation/template,
 * it is reusable and can be placed within item banks
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EvalItem implements java.io.Serializable {

    // Fields

    private Long id;

    private String eid;

    private Date lastModified;

    private String owner;

    private String itemText;

    private String description;

    private String sharing;

    private String classification;

    private Boolean expert;

    private String expertDescription;

    private EvalScale scale;

    private Set<EvalTemplateItem> templateItems = new HashSet<EvalTemplateItem>(0);

    /**
     * display hint
     * If true then this item must be answered when taking evaluations
     */
    private Boolean compulsory;

    /**
     * display hint
     */
    private Boolean usesNA;

    /**
     * display hint
     */
    private Boolean usesComment;

    /**
     * display hint
     */
    private Integer displayRows;

    /**
     * display hint
     */
    private String scaleDisplaySetting;

    private String category;

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
     * If this is not null then this item will be added to the copy of the template 
     * used for the evaluations with the related linking autoUseTag when the eval is created<br/>
     * <b>NOTE:</b> It is preferable to handle this using the template item or template
     */
    private String autoUseTag;


    // Constructors



    /** default constructor */
    public EvalItem() {
    }

    /** minimal constructor */
    public EvalItem(String owner, String itemText, String sharing, String classification, Boolean expert) {
        this(owner, itemText, null, sharing, classification, expert, null, null, null, null, null, false, null, null, null, null);
    }

    /** full constructor */
    public EvalItem(String owner, String itemText, String description, String sharing, String classification,
            Boolean expert, String expertDescription, EvalScale scale, Set<EvalTemplateItem> templateItems,
            Boolean usesNA, Boolean usesComment, Boolean compulsory,
            Integer displayRows, String scaleDisplaySetting, String category, Boolean locked) {
        this.lastModified = new Date();
        this.owner = owner;
        this.itemText = itemText;
        this.description = description;
        this.sharing = sharing;
        this.classification = classification;
        this.expert = expert;
        this.expertDescription = expertDescription;
        this.scale = scale;
        if (templateItems != null) {
            this.templateItems = templateItems;
        }
        this.usesNA = (usesNA == null ? Boolean.FALSE : usesNA); // default to false
        this.usesComment = (usesComment == null ? Boolean.FALSE : usesComment); // default to false
        this.compulsory = (compulsory == null ? Boolean.FALSE : compulsory); // default to false
        this.displayRows = displayRows;
        this.scaleDisplaySetting = scaleDisplaySetting;
        this.category = category;
        this.locked = locked;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDisplayRows() {
        return displayRows;
    }

    public void setDisplayRows(Integer displayRows) {
        this.displayRows = displayRows;
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

    public String getItemText() {
        return itemText;
    }

    public void setItemText(String itemText) {
        this.itemText = itemText;
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

    public EvalScale getScale() {
        return scale;
    }

    public void setScale(EvalScale scale) {
        this.scale = scale;
    }

    public String getScaleDisplaySetting() {
        return scaleDisplaySetting;
    }

    public void setScaleDisplaySetting(String scaleDisplaySetting) {
        this.scaleDisplaySetting = scaleDisplaySetting;
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

    public Boolean getUsesNA() {
        return usesNA;
    }

    public void setUsesNA(Boolean usesNA) {
        this.usesNA = usesNA;
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

    public Boolean getUsesComment() {
        return usesComment;
    }

    public void setUsesComment(Boolean usesComment) {
        this.usesComment = usesComment;
    }

    public String getAutoUseTag() {
        return autoUseTag;
    }

    public void setAutoUseTag(String autoUseTag) {
        this.autoUseTag = autoUseTag;
    }

    public Boolean isCompulsory() {
        return this.compulsory;
    }

    public void setCompulsory(Boolean compulsory) {
        this.compulsory = compulsory;
    }

}
