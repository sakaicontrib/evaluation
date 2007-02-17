/******************************************************************************
 * EvalExternalLogicImpl.java - created by aaronz@vt.edu on Dec 24, 2006
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

package org.sakaiproject.evaluation.logic.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.Context;
import org.sakaiproject.evaluation.logic.providers.EvalGroupsProvider;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;


/**
 * This class handles the Sakai based implementation of the external logic inteface<br/>
 * This is sorta the provider for the evaluation system though it should be broken up
 * if it is going to be used like that
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalExternalLogicImpl implements EvalExternalLogic {

	private static Log log = LogFactory.getLog(EvalExternalLogicImpl.class);

	private static final String SAKAI_SITE_TYPE = SiteService.APPLICATION_ID;
	private static final String SAKAI_GROUP_TYPE = SiteService.GROUP_SUBTYPE;

	private AuthzGroupService authzGroupService;
	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	private EmailService emailService;
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	private EntityManager entityManager;
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	private FunctionManager functionManager;
	public void setFunctionManager(FunctionManager functionManager) {
		this.functionManager = functionManager;
	}

	private SecurityService securityService;
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	private SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	private ToolManager toolManager;
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	// PROVIDERS

	private EvalGroupsProvider evalGroupsProvider;
	public void setEvalGroupsProvider(EvalGroupsProvider evalGroupsProvider) {
		this.evalGroupsProvider = evalGroupsProvider;
	}


	public void init() {
		log.debug("init, register security perms");
		
		// register Sakai permissions for this tool
		registerPermissions();

		// setup providers
		if (evalGroupsProvider == null) {
			evalGroupsProvider = (EvalGroupsProvider) ComponentManager.get(EvalGroupsProvider.class.getName());
		}
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.external.EvalExternalLogic#getCurrentUserId()
	 */
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.external.EvalExternalLogic#getUserUsername(java.lang.String)
	 */
	public String getUserUsername(String userId) {
		try {
			return userDirectoryService.getUserEid(userId);
		} catch(UserNotDefinedException ex) {
			log.error("Could not get username from userId: " + userId, ex);
		}
		return "UnknownUsername";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.external.EvalExternalLogic#getUserDisplayName(java.lang.String)
	 */
	public String getUserDisplayName(String userId) {
		try {
			User user = userDirectoryService.getUser(userId);
			return user.getDisplayName();
		} catch(UserNotDefinedException ex) {
			log.error("Could not get user from userId: " + userId, ex);
		}
		return "Unknown DisplayName";
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.external.EvalExternalLogic#isUserAdmin(java.lang.String)
	 */
	public boolean isUserAdmin(String userId) {
		log.info("Checking is eval super admin for: " + userId);
		return securityService.isSuperUser(userId);
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.external.EvalExternalLogic#makeContextObject(java.lang.String)
	 */
	public Context makeContextObject(String context) {
		// TODO - make this work for other context types
		Context c = null;
		try {
			Site site = siteService.getSite(context);
			c = new Context( context, site.getTitle(), 
					getContextType(SAKAI_SITE_TYPE) );
		} catch (IdUnusedException e) {
			// invalid site Id
			log.debug("Could not get site from context:" + context, e);

			// use external provider
			if (evalGroupsProvider != null) {
				c = evalGroupsProvider.getContextByGroupId(context);
			}
		}

		if (c == null)
			log.error("Could not get site from context:" + context);

		return c;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.external.EvalExternalLogic#getCurrentContext()
	 */
	public String getCurrentContext() {
		return toolManager.getCurrentPlacement().getContext();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.external.EvalExternalLogic#getDisplayTitle(java.lang.String)
	 */
	public String getDisplayTitle(String context) {
		// TODO - make this handle non-Sites also
		try {
			Site site = siteService.getSite(context);
			return site.getTitle();
		} catch (IdUnusedException e) {
			if (evalGroupsProvider != null) {
				Context c = evalGroupsProvider.getContextByGroupId(context);
				return c.title;
			}
		}

		log.warn("Cannot get the info about context:" + context);
		return "Unknown DisplayTitle";
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.external.EvalExternalLogic#countContextsForUser(java.lang.String, java.lang.String)
	 */
	public int countContextsForUser(String userId, String permission) {
		log.debug("userId: " + userId + ", permission: " + permission);

		// TODO - make this handle non-Sites also
		int count = 0;
		Set authzGroupIds = authzGroupService.getAuthzGroupsIsAllowed(userId, permission, null);
		Iterator it = authzGroupIds.iterator();
		while (it.hasNext()) {
			String authzGroupId = (String) it.next();
			Reference r = entityManager.newReference(authzGroupId);
			if(r.isKnownType()) {
				// check if this is a Sakai Site
				if(r.getType().equals(SiteService.APPLICATION_ID)) {
					count++;
				}
			}
		}

		// also check provider
		if (evalGroupsProvider != null) {
			if (EvalConstants.PERM_BE_EVALUATED.equals(permission) ||
					EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				log.debug("Using eval groups provider: userId: " + userId + ", permission: " + permission);
				count += evalGroupsProvider.countEvalGroupsForUser(userId, permission);
			}
		}

		return count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.external.EvalExternalLogic#getContextsForUser(java.lang.String, java.lang.String)
	 */
	public List getContextsForUser(String userId, String permission) {
		log.debug("userId: " + userId + ", permission: " + permission);

		// TODO - make this handle non-Sites also
		log.info("Sites for user:" + userId + ", permission: " + permission);
		List l = new ArrayList();
		Set authzGroupIds = authzGroupService.getAuthzGroupsIsAllowed(userId, permission, null);
		Iterator it = authzGroupIds.iterator();
		while (it.hasNext()) {
			String authzGroupId = (String) it.next();
			Reference r = entityManager.newReference(authzGroupId);
			if(r.isKnownType()) {
				// check if this is a Sakai Site
				if(r.getType().equals(SiteService.APPLICATION_ID)) {
					String siteId = r.getId();
					try {
						Site site = siteService.getSite(siteId);
						l.add( new Context(site.getId(), site.getTitle(), 
								getContextType(r.getType())) );
					} catch (IdUnusedException e) {
						// invalid site Id returned
						throw new RuntimeException("Could not get site from siteId:" + siteId);
					}
				}
			}
		}

		// also check provider
		if (evalGroupsProvider != null) {
			if (EvalConstants.PERM_BE_EVALUATED.equals(permission) ||
					EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				log.debug("Using eval groups provider: userId: " + userId + ", permission: " + permission);
				l.addAll( evalGroupsProvider.getEvalGroupsForUser(userId, permission) );
			}
		}

		if (l.isEmpty()) log.warn("Empty list of sites for user:" + userId + ", permission: " + permission);
		return l;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalContexts#countUserIdsForContext(java.lang.String, java.lang.String)
	 */
	public int countUserIdsForContext(String context, String permission) {
		int count = getUserIdsForContext(context, permission).size();

		// also check provider
		if (evalGroupsProvider != null) {
			if (EvalConstants.PERM_BE_EVALUATED.equals(permission) ||
					EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				log.debug("Using eval groups provider: context: " + context + ", permission: " + permission);
				count += evalGroupsProvider.countUserIdsForEvalGroups(new String[] {context}, permission);
			}
		}

		return count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.external.EvalExternalLogic#getUserIdsForContext(java.lang.String, java.lang.String)
	 */
	public Set getUserIdsForContext(String context, String permission) {
		String reference = getReference(context);
		List azGroups = new ArrayList();
		azGroups.add(reference);
		Set userIds = authzGroupService.getUsersIsAllowed(permission, azGroups);

		// also check provider
		if (evalGroupsProvider != null) {
			if (EvalConstants.PERM_BE_EVALUATED.equals(permission) ||
					EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				log.debug("Using eval groups provider: context: " + context + ", permission: " + permission);
				userIds.addAll( evalGroupsProvider.getUserIdsForEvalGroups(new String[] {context}, permission) );
			}
		}

		return userIds;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.external.EvalExternalLogic#isUserAllowedInContext(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean isUserAllowedInContext(String userId, String permission, String context) {
		String reference = getReference(context);
		if ( securityService.unlock(userId, permission, reference) ) {
			return true;
		}

		// also check provider
		if (evalGroupsProvider != null) {
			if (EvalConstants.PERM_BE_EVALUATED.equals(permission) ||
					EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				log.debug("Using eval groups provider: userId: " + userId + ", permission: " + permission + ", context: " + context);
				if ( evalGroupsProvider.isUserAllowedInGroup(userId, permission, context) ) {
					return true;
				}
			}
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#sendEmails(java.lang.String, java.lang.String[], java.lang.String, java.lang.String)
	 */
	public void sendEmails(String from, String[] toUserIds, String subject, String message) {
		InternetAddress fromAddress;
		try {
			fromAddress = new InternetAddress(from);
		} catch (AddressException e) {
			throw new IllegalArgumentException("Invalid from address: " + from);
		}

		// handle the list of TO addresses
		List userIds = Arrays.asList( toUserIds );
		List l = userDirectoryService.getUsers( userIds );

		if (l == null || l.size() <= 0) {
			throw new IllegalArgumentException("Could not get users from any provided userIds");
		}

		InternetAddress[] toAddresses = new InternetAddress[ l.size() ];
		InternetAddress[] replyTo = new InternetAddress[ l.size() ];
		for (int i = 0; i < l.size(); i++) {
			User u = (User) l.get(i);
			try {
				InternetAddress toAddress = new InternetAddress( u.getEmail() );
				toAddresses[i] = toAddress;
				replyTo[i] = fromAddress;
			} catch (AddressException e) {
				throw new IllegalArgumentException("Invalid to address: " + toUserIds[i]);
			}
		}

		emailService.sendMail(fromAddress, toAddresses, subject, message, null, replyTo, null);
	}


	/**
	 * Register the various permissions
	 */
	private void registerPermissions() {
		// register Sakai permissions for this tool
		functionManager.registerFunction(EvalConstants.PERM_WRITE_TEMPLATE);
		functionManager.registerFunction(EvalConstants.PERM_ASSIGN_EVALUATION);
		functionManager.registerFunction(EvalConstants.PERM_BE_EVALUATED);
		functionManager.registerFunction(EvalConstants.PERM_TAKE_EVALUATION);
	}

	/**
	 * Takes a Sakai context and returns a Sakai reference string (as needed by authz)
	 * 
	 * @param context a Sakai context string
	 * @return a Sakai reference string
	 */
	private String getReference(String context) {
		// TODO - make this work for other context types
		String reference = siteService.siteReference(context);
		return reference;
	}

	/**
	 * Returns the appropriate internal site type identifier based on the given string
	 * 
	 * @param sakaiType a type identifier used in Sakai (should be a constant from Sakai)
	 * @return an int representing a constant from EvalExternalLogic
	 */
	private int getContextType(String sakaiType) {
		if (sakaiType.equals(SAKAI_SITE_TYPE)) {
			return EvalConstants.CONTEXT_TYPE_SITE;
		} else if (sakaiType.equals(SAKAI_GROUP_TYPE)) {
			return EvalConstants.CONTEXT_TYPE_GROUP;
		}
		return EvalConstants.CONTEXT_TYPE_UNKNOWN;
	}

}
