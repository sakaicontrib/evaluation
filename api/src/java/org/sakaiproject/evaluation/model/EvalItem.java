
package org.sakaiproject.evaluation.model;

//Generated Mar 20, 2007 10:08:13 AM by Hibernate Tools 3.2.0.beta6a

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a question/item that would be used in an evaluation/template,
 * it is reusable and can be placed within item banks
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

   private Boolean usesNA;

   private Integer displayRows;

   private String scaleDisplaySetting;

   private String category;

   private Boolean locked;

   /**
    * Indicates that this is a copy of an item and therefore should be hidden from views and 
    * only revealed when taking/previewing (not as part of item banks, etc.),
    * this will be the id of the persistent object it is a copy of
    */
   private Long copyOf;

   // Constructors

   /** default constructor */
   public EvalItem() {
   }

   /** minimal constructor */
   public EvalItem(Date lastModified, String owner, String itemText, String sharing, String classification,
         Boolean expert) {
      this.lastModified = lastModified;
      this.owner = owner;
      this.itemText = itemText;
      this.sharing = sharing;
      this.classification = classification;
      this.expert = expert;
   }

   /** full constructor */
   public EvalItem(Date lastModified, String owner, String itemText, String description, String sharing,
         String classification, Boolean expert, String expertDescription, EvalScale scale,
         Set<EvalTemplateItem> templateItems, Boolean usesNA, Integer displayRows,
         String scaleDisplaySetting, String category, Boolean locked) {
      this.lastModified = lastModified;
      this.owner = owner;
      this.itemText = itemText;
      this.description = description;
      this.sharing = sharing;
      this.classification = classification;
      this.expert = expert;
      this.expertDescription = expertDescription;
      this.scale = scale;
      this.templateItems = templateItems;
      this.usesNA = usesNA;
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

}
