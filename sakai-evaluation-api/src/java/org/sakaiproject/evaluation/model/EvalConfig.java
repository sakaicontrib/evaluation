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
 * Stores configuration settings for the overall eval system
 */
public class EvalConfig implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    // Fields    

    private Long id;

    private Date lastModified;

    private String name;

    private String value;

    // Constructors

    /** default constructor */
    public EvalConfig() {
    }

    /** full constructor
     * @param name
     * @param value 
     */
    public EvalConfig(String name, String value) {
        this.lastModified = new Date();
        this.name = name;
        this.value = value;
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

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
