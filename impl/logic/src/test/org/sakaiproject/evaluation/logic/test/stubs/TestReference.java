/******************************************************************************
 * TestReference.java - created by aaronz@vt.edu on Dec 23, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.test.stubs;

import java.util.Collection;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;

/**
 * Test class for the Sakai Placement object<br/>
 * This has to be here since I cannot create a Reference object in Sakai for some 
 * reason... sure would be nice if I could though -AZ
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class TestReference implements Reference {

	private String id = "REF12345";
	private String type;
	private String context;
	private String reference;

	public TestReference(String id, String type) {
		this.id = id;
		this.type = type;
	}

	public TestReference(String id, String type, String context, String reference) {
		this.id = id;
		this.type = type;
		this.context = context;
		this.reference = reference;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#addSiteContextAuthzGroup(java.util.Collection)
	 */
	public void addSiteContextAuthzGroup(Collection rv) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#addUserAuthzGroup(java.util.Collection, java.lang.String)
	 */
	public void addUserAuthzGroup(Collection rv, String id) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#addUserTemplateAuthzGroup(java.util.Collection, java.lang.String)
	 */
	public void addUserTemplateAuthzGroup(Collection rv, String id) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#getAuthzGroups()
	 */
	public Collection getAuthzGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#getAuthzGroups(java.lang.String)
	 */
	public Collection getAuthzGroups(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#getContainer()
	 */
	public String getContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#getContext()
	 */
	public String getContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#getDescription()
	 */
	public String getDescription() {
		return "Description";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#getEntity()
	 */
	public Entity getEntity() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#getEntityProducer()
	 */
	public EntityProducer getEntityProducer() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#getId()
	 */
	public String getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#getProperties()
	 */
	public ResourceProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#getReference()
	 */
	public String getReference() {
		return reference;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#getSubType()
	 */
	public String getSubType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#getType()
	 */
	public String getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#getUrl()
	 */
	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#isKnownType()
	 */
	public boolean isKnownType() {
		if (type != null) return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.entity.api.Reference#set(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean set(String type, String subType, String id,
			String container, String context) {
		// TODO Auto-generated method stub
		return false;
	}

}
