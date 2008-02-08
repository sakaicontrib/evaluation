
package org.sakaiproject.evaluation.model;

import java.util.Date;

import org.sakaiproject.evaluation.logic.utils.EvalUtils;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * The Answer object holds answers for a single item and a single response
 * It has special fields for handling and storing multiple answers that are
 * not persisted but are populated by the logic layer<br/>
 * <b>NOTE:</b> In order to properly store the answers, we have to fill in every answer
 * field when saving them. Those fields are {@link #numeric}, {@link #text}, and {@link #multiAnswerCode}<br/>
 * You should ignore the various blanks which are inserted when retrieving answers, 
 * the blanks are all constants and are as follows:
 * {@link EvalConstants#NO_NUMERIC_ANSWER}, {@link EvalConstants#NO_TEXT_ANSWER}, {@link EvalConstants#NO_MULTIPLE_ANSWER}<br/>
 * <b>NOTE:</b> There is a special way to refer to N/A answers 
 * for all types which must be checked. If the numeric field is
 * set to: {@link EvalConstants#NA_VALUE} then this answer was 
 * set to Not Applicable and any other stored values should be ignored<br/>
 */
public class EvalAnswer implements java.io.Serializable {

   // Fields

   private Long id;

   private Date lastModified;

   private EvalTemplateItem templateItem;

   private EvalItem item;

   private EvalResponse response;

   private Integer numeric;

   private String text;

   private String multiAnswerCode;

   private String associatedId;

   private String associatedType;

   /**
    * Non-persistent field which will be populated with the actual scale array keys
    * by post processing, this will be null when loaded from the DB,
    * use {@link EvalUtils#decodeMultipleAnswers(String)} to populate this array
    * using the encoded string in {@link #multiAnswerCode}
    */
   public Integer[] multipleAnswers;


   // Constructors

   /** default constructor */
   public EvalAnswer() {
   }

   /**
    * minimal constructor
    */
   public EvalAnswer(Date lastModified, EvalTemplateItem templateItem,
         EvalResponse response) {
      this.lastModified = lastModified;
      this.templateItem = templateItem;
      this.response = response;
   }

   public EvalAnswer(Date lastModified, EvalTemplateItem templateItem,
         EvalResponse response, String text) {
      this.lastModified = lastModified;
      this.templateItem = templateItem;
      this.response = response;
      this.text = text;
   }

   public EvalAnswer(Date lastModified, EvalTemplateItem templateItem,
         EvalResponse response, Integer numeric) {
      this.lastModified = lastModified;
      this.templateItem = templateItem;
      this.response = response;
      this.numeric = numeric;
   }

   /** full constructor */
   public EvalAnswer(Date lastModified, EvalTemplateItem templateItem,
         EvalItem item, EvalResponse response, String text, Integer numeric,
         String associatedId, String associatedType) {
      this.lastModified = lastModified;
      this.templateItem = templateItem;
      this.item = item;
      this.response = response;
      this.text = text;
      this.numeric = numeric;
      this.associatedId = associatedId;
      this.associatedType = associatedType;
   }

   // Property accessors
   public Long getId() {
      return this.id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public Date getLastModified() {
      return this.lastModified;
   }

   public void setLastModified(Date lastModified) {
      this.lastModified = lastModified;
   }

   public EvalTemplateItem getTemplateItem() {
      return this.templateItem;
   }

   public void setTemplateItem(EvalTemplateItem templateItem) {
      this.templateItem = templateItem;
   }

   public EvalItem getItem() {
      return this.item;
   }

   public void setItem(EvalItem item) {
      this.item = item;
   }

   public EvalResponse getResponse() {
      return this.response;
   }

   public void setResponse(EvalResponse response) {
      this.response = response;
   }

   public String getText() {
      return this.text;
   }

   public void setText(String text) {
      this.text = text;
   }

   public Integer getNumeric() {
      return this.numeric;
   }

   public void setNumeric(Integer numeric) {
      this.numeric = numeric;
   }

   public String getAssociatedId() {
      return this.associatedId;
   }

   public void setAssociatedId(String associatedId) {
      this.associatedId = associatedId;
   }

   public String getAssociatedType() {
      return this.associatedType;
   }

   public void setAssociatedType(String associatedType) {
      this.associatedType = associatedType;
   }
   
   /**
    * Returns the encoded answers for an MA type item,
    * use {@link EvalUtils#decodeMultipleAnswers(String)} to
    * decode these answers into the {@link #multipleAnswers} field
    */
   public String getMultiAnswerCode() {
      return multiAnswerCode;
   }
   
   /**
    * @param multiAnswerCode an encoded value which is created using
    * {@link EvalUtils#encodeMultipleAnswers(Integer[])}
    */
   public void setMultiAnswerCode(String multiAnswerCode) {
      this.multiAnswerCode = multiAnswerCode;
   }

}
