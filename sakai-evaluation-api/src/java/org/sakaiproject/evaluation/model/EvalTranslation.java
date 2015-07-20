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
 * Stores the translation strings for any persistent object field in the system,
 * NOTE: currently not being used but here for future updates
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalTranslation implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Date lastModified;
    /**
     * the language code of the translated string,
     * e.g. en_US, en_GB, etc.
     */
    private String languageCode;
    /**
     * the object class of the thing which contains the field we are translating
     */
    private String objectClass;
    /**
     * Unique id for the object of the class indicated
     */
    private String objectId;
    /**
     * the name of the field on the object
     */
    private String fieldName;
    /**
     * The translation of this field value into the language indicated by the language code
     */
    private String translation;

    public EvalTranslation() {
    }

    /**
     * MINIMAL constructor
     * 
     * @param languageCode
     * @param objectClass
     * @param objectId
     * @param fieldName
     * @param translation
     */
    public EvalTranslation(String languageCode, String objectClass, String objectId,
            String fieldName, String translation) {
        if (this.lastModified == null) {
            this.lastModified = new Date();
        }
        this.languageCode = languageCode;
        this.objectClass = objectClass;
        this.objectId = objectId;
        this.fieldName = fieldName;
        this.translation = translation;
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

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(String objectClass) {
        this.objectClass = objectClass;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

}
