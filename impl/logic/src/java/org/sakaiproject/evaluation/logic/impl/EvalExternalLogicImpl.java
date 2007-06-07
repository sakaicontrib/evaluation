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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.entity.AssignGroupEntityProvider;
import org.sakaiproject.evaluation.logic.entity.EvaluationEntityProvider;
import org.sakaiproject.evaluation.logic.entity.TemplateEntityProvider;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.providers.EvalGroupsProvider;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;


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

	private static final String ANON_USER_ATTRIBUTE = "AnonUserAttribute";

	private AuthzGroupService authzGroupService;
	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	private EmailService emailService;
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	private EntityBroker entityBroker;
	public void setEntityBroker(EntityBroker entityBroker) {
		this.entityBroker = entityBroker;
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

	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
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
			if (evalGroupsProvider != null) {
				log.info("EvalGroupsProvider found...");
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.external.EvalExternalLogic#getCurrentUserId()
	 */
	public String getCurrentUserId() {
		String userId = sessionManager.getCurrentSessionUserId();
		if (userId == null) {
			// if no user found then fake like there is one for this session,
			// we do not want to actually create a user though
			Session session = sessionManager.getCurrentSession();
			userId = (String) session.getAttribute(ANON_USER_ATTRIBUTE);
			if (userId == null) {
				String sessionUserId = session.getId() + new Date().getTime();
				userId = "Anon_User_" + makeMD5(sessionUserId, 40);
				session.setAttribute(ANON_USER_ATTRIBUTE, userId);
			}
		}
		return userId;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#isUserAnonymous(java.lang.String)
	 */
	public boolean isUserAnonymous(String userId) {
		String currentUserId = sessionManager.getCurrentSessionUserId();
		if (userId.equals(currentUserId)) {
			return false;
		}
		Session session = sessionManager.getCurrentSession();
		String sessionUserId = (String) session.getAttribute(ANON_USER_ATTRIBUTE);
		if (userId.equals(sessionUserId)) {
			return true;
		}
		// used up both cheap tests, now try the costly one
		try {
			// try to lookup this user, exception if we cannot find them
			userDirectoryService.getUserEid(userId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.external.EvalExternalLogic#getUserUsername(java.lang.String)
	 */
	public String getUserUsername(String userId) {
		if (isUserAnonymous(userId)) {
			return "anonymous";
		}
		try {
			return userDirectoryService.getUserEid(userId);
		} catch(UserNotDefinedException ex) {
			log.error("Could not get username from userId: " + userId, ex);
		}
		return "------";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.external.EvalExternalLogic#getUserDisplayName(java.lang.String)
	 */
	public String getUserDisplayName(String userId) {
		if (isUserAnonymous(userId)) {
			return "Anonymous User";
		}
		try {
			User user = userDirectoryService.getUser(userId);
			return user.getDisplayName();
		} catch(UserNotDefinedException ex) {
			log.error("Could not get user from userId: " + userId, ex);
		}
		return "----------";
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.external.EvalExternalLogic#isUserAdmin(java.lang.String)
	 */
	public boolean isUserAdmin(String userId) {
		log.debug("Checking is eval super admin for: " + userId);
		return securityService.isSuperUser(userId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#getUserLocale(java.lang.String)
	 */
	public Locale getUserLocale(String userId) {
		log.debug("userId: " + userId);
		log.warn("can only get the locale for the current user right now...");
		// TODO - this sucks because there is no way to get the locale for anything but the
		// current user.... terrible -AZ
		return new ResourceLoader().getLocale();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#makeEvalGroupObject(java.lang.String)
	 */
	public EvalGroup makeEvalGroupObject(String evalGroupId) {
		EvalGroup c = null;
		try {
			// try to get the site object based on the entity reference (which is the evalGroupId)
			Site site = (Site) entityBroker.fetchEntity(evalGroupId);
			c = new EvalGroup( evalGroupId, site.getTitle(), 
					getContextType(SAKAI_SITE_TYPE) );
		} catch (Exception e) {
			// invalid site reference
			log.debug("Could not get sakai site from evalGroupId:" + evalGroupId, e);
		}

		if (c == null) {
			// use external provider
			if (evalGroupsProvider != null) {
				c = evalGroupsProvider.getGroupByGroupId(evalGroupId);
				if (c != null) {
					c.type = EvalConstants.GROUP_TYPE_PROVIDED;
				}
			}
		}

		if (c == null) {
			log.error("Could not get group from evalGroupId:" + evalGroupId);
			// create a fake group placeholder as an error notice
			c = new EvalGroup( evalGroupId, "** INVALID: "+evalGroupId+" **", 
					EvalConstants.GROUP_TYPE_INVALID );
		}

		return c;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#getCurrentEvalGroup()
	 */
	public String getCurrentEvalGroup() {
		try {
			Site s = (Site) siteService.getSite( toolManager.getCurrentPlacement().getContext() );
			return s.getReference(); // get the entity reference to the site
		} catch (Exception e) {
			log.warn("Could not get the current location (we are probably outside the portal), returning the fake one");
			return NO_LOCATION;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.external.EvalExternalLogic#getDisplayTitle(java.lang.String)
	 */
	public String getDisplayTitle(String evalGroupId) {
		EvalGroup group = makeEvalGroupObject(evalGroupId);
		if (group != null && group.title != null) {
			return group.title;
		}
		log.warn("Cannot get the title for evalGroupId: " + evalGroupId);
		return "--------";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#countEvalGroupsForUser(java.lang.String, java.lang.String)
	 */
	public int countEvalGroupsForUser(String userId, String permission) {
		log.debug("userId: " + userId + ", permission: " + permission);

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
				count += evalGroupsProvider.countEvalGroupsForUser(userId, translatePermission(permission));
			}
		}

		return count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#getEvalGroupsForUser(java.lang.String, java.lang.String)
	 */
	public List getEvalGroupsForUser(String userId, String permission) {
		log.debug("userId: " + userId + ", permission: " + permission);

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
						l.add( new EvalGroup(r.getReference(), site.getTitle(), 
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
				List eg = evalGroupsProvider.getEvalGroupsForUser(userId, translatePermission(permission));
				for (Iterator iter = eg.iterator(); iter.hasNext();) {
					EvalGroup c = (EvalGroup) iter.next();
					c.type = EvalConstants.GROUP_TYPE_PROVIDED;
					l.add(c);
				}
			}
		}

		if (l.isEmpty()) log.warn("Empty list of groups for user:" + userId + ", permission: " + permission);
		return l;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#countUserIdsForEvalGroup(java.lang.String, java.lang.String)
	 */
	public int countUserIdsForEvalGroup(String evalGroupId, String permission) {
		int count = getUserIdsForEvalGroup(evalGroupId, permission).size();

		// also check provider
		if (evalGroupsProvider != null) {
			if (EvalConstants.PERM_BE_EVALUATED.equals(permission) ||
					EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				log.debug("Using eval groups provider: evalGroupId: " + evalGroupId + ", permission: " + permission);
				count += evalGroupsProvider.countUserIdsForEvalGroups(new String[] {evalGroupId}, translatePermission(permission));
			}
		}

		return count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups#getUserIdsForEvalGroup(java.lang.String, java.lang.String)
	 */
	public Set getUserIdsForEvalGroup(String evalGroupId, String permission) {
		String reference = evalGroupId;
		List azGroups = new ArrayList();
		azGroups.add(reference);
		Set userIds = authzGroupService.getUsersIsAllowed(permission, azGroups);

		// also check provider
		if (evalGroupsProvider != null) {
			if (EvalConstants.PERM_BE_EVALUATED.equals(permission) ||
					EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				log.debug("Using eval groups provider: evalGroupId: " + evalGroupId + ", permission: " + permission);
				userIds.addAll( evalGroupsProvider.getUserIdsForEvalGroups(new String[] {evalGroupId}, translatePermission(permission)) );
			}
		}

		return userIds;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#isUserAllowedInEvalGroup(java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean isUserAllowedInEvalGroup(String userId, String permission, String evalGroupId) {
        if (evalGroupId == null) {
            if (securityService.isSuperUser(userId)) {
                return true;
            }
            return false;
        }
  
		String reference = evalGroupId;
		if ( securityService.unlock(userId, permission, reference) ) {
			return true;
		}

		// also check provider
		if (evalGroupsProvider != null) {
			if (EvalConstants.PERM_BE_EVALUATED.equals(permission) ||
					EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				log.debug("Using eval groups provider: userId: " + userId + ", permission: " + permission + ", evalGroupId: " + evalGroupId);
				if ( evalGroupsProvider.isUserAllowedInGroup(userId, translatePermission(permission), evalGroupId) ) {
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
		
		//email address validity is checked at entry but value can be null
		for (ListIterator iterator = l.listIterator(); iterator.hasNext();) {
			User u = (User)iterator.next();
			if(u.getEmail().equals("")) {
				iterator.remove();
				log.warn("Could not get an email address for " + u.getDisplayName());
			}
		}

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


	// ENTITIES


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#getServerUrl()
	 */
	public String getServerUrl() {
		return serverConfigurationService.getPortalUrl();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#getEntityURL(java.io.Serializable)
	 */
	public String getEntityURL(Serializable evaluationEntity) {
		String ref = getEntityReference(evaluationEntity);
		if (ref != null) {
			return entityBroker.getEntityURL(ref);
		} else {
			return serverConfigurationService.getPortalUrl();
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#getEntityURL(java.lang.String, java.lang.String)
	 */
	public String getEntityURL(String entityPrefix, String entityId) {
		String ref = new IdEntityReference(entityPrefix, entityId).toString();
		if (ref != null) {
			return entityBroker.getEntityURL(ref);
		} else {
			return serverConfigurationService.getPortalUrl();
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#registerEntityEvent(java.lang.String, java.io.Serializable)
	 */
	public void registerEntityEvent(String eventName, Serializable evaluationEntity) {
		String ref = getEntityReference(evaluationEntity);
		if (ref != null) {
			entityBroker.fireEvent(eventName, ref);
		}
	}

	/**
	 * Get an entity reference to any of the evaluation objects which are treated as entities
	 * 
	 * @param entity
	 * @return an entity reference string or null if none can be found
	 */
	private String getEntityReference(Serializable entity) {
		String id = null;
		try {
			Class elementClass = entity.getClass();
			Method getIdMethod = elementClass.getMethod("getId", new Class[] {});
			Long realId = (Long) getIdMethod.invoke(entity, null);
			id = realId.toString();
			return getEntityReference(elementClass, id);
		} catch (Exception e) {
			log.warn("Failed to get id from entity object", e);
			return null;
		}
	}

	private String getEntityReference(Class entityClass, String entityId) {
		String prefix = null;
		// make sure this class is supported and get the prefix
		if (entityClass == EvalEvaluation.class) {
			prefix = EvaluationEntityProvider.ENTITY_PREFIX;
		} else if (entityClass == EvalAssignGroup.class) {
				prefix = AssignGroupEntityProvider.ENTITY_PREFIX;
		} else if (entityClass == EvalTemplate.class) {
			prefix = TemplateEntityProvider.ENTITY_PREFIX;
		} else {
			return null;
		}

		return new IdEntityReference(prefix, entityId).toString();
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
	 * Returns the appropriate internal site type identifier based on the given string
	 * 
	 * @param sakaiType a type identifier used in Sakai (should be a constant from Sakai)
	 * @return a CONTEXT_TYPE constant from {@link EvalConstants}
	 */
	private String getContextType(String sakaiType) {
		if (sakaiType.equals(SAKAI_SITE_TYPE)) {
			return EvalConstants.GROUP_TYPE_SITE;
		} else if (sakaiType.equals(SAKAI_GROUP_TYPE)) {
			return EvalConstants.GROUP_TYPE_GROUP;
		}
		return EvalConstants.GROUP_TYPE_UNKNOWN;
	}

	/**
	 * Simple method to translate one constant into another
	 * (this allows the provider to have no dependency on the model)
	 * @param permission a PERM constant from {@link EvalConstants}
	 * @return the translated constant from {@link EvalGroupsProvider}
	 */
	private String translatePermission(String permission) {
		if (EvalConstants.PERM_TAKE_EVALUATION.equals(permission)) {
			return EvalGroupsProvider.PERM_TAKE_EVALUATION;
		} else if (EvalConstants.PERM_BE_EVALUATED.equals(permission)) {
			return EvalGroupsProvider.PERM_BE_EVALUATED;
		}
		return "UNKNOWN";
	}

	/**
	 * @param text string to make MD5 hash from
	 * @param maxLength
	 * @return an MD5 hash no longer than maxLength
	 */
	private String makeMD5(String text, int maxLength) {

		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Stupid java sucks for MD5", e);
		}
		md.update(text.getBytes());

		// convert the binary md5 hash into hex
		String md5 = "";
		byte[] b_arr = md.digest();

		for (int i = 0; i < b_arr.length; i++) {
			// convert the high nibble
			byte b = b_arr[i];
			b >>>= 4;
			b &= 0x0f; // this clears the top half of the byte
			md5 += Integer.toHexString(b);

			// convert the low nibble
			b = b_arr[i];
			b &= 0x0F;
			md5 += Integer.toHexString(b);
		}
		if (maxLength > 0 && md5.length() > maxLength) {
			md5 = md5.substring(0, maxLength);
		}
		return md5;
	}

}
