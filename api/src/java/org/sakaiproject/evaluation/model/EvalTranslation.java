/**
 * $Id$
 * $URL$
 * EvalTagsMeta.java - evaluation - Apr 2, 2008 9:21:35 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
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
