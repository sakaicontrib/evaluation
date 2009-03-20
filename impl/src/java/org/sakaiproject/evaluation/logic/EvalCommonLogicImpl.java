/**
 * $Id$
 * $URL$
 * EvalExternalLogicImpl.java - evaluation - Dec 24, 2006 10:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvalAdhocSupport;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogicImpl;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalScheduledJob;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.providers.EvalGroupsProvider;
import org.sakaiproject.evaluation.utils.ArrayUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;

/**
 * This is the implementation for the base service
 * This is a BOTTOM level service and should depend on no other eval services (only those in Sakai)
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalCommonLogicImpl implements EvalCommonLogic {

    private static Log log = LogFactory.getLog(EvalCommonLogicImpl.class);

    /**
     * default admin user id
     */
    public static final String ADMIN_USER_ID = "admin";

    public static String UNKNOWN_TITLE = "--------"; 

    private EvalExternalLogic externalLogic;
    public void setExternalLogic(EvalExternalLogic externalLogic) {
        this.externalLogic = externalLogic;
    }

    // INTERNAL for adhoc user/group lookups

    private EvalAdhocSupport adhocSupportLogic;
    public void setAdhocSupportLogic(EvalAdhocSupport adhocSupportLogic) {
        this.adhocSupportLogic = adhocSupportLogic;
    }

    // PROVIDERS

    private EvalGroupsProvider evalGroupsProvider;
    public void setEvalGroupsProvider(EvalGroupsProvider evalGroupsProvider) {
        this.evalGroupsProvider = evalGroupsProvider;
    }


    public void init() {
        log.debug("init, register security perms");

        // setup provider
        if (evalGroupsProvider == null) {
            evalGroupsProvider = (EvalGroupsProvider) externalLogic.getBean(EvalGroupsProvider.class);
            if (evalGroupsProvider != null)
                log.info("EvalGroupsProvider found...");
        } else {
            log.debug("No EvalGroupsProvider found...");
        }
    }


    public String getCurrentUserId() {
        return externalLogic.getCurrentUserId();
    }

    public String getAdminUserId() {
        return externalLogic.getAdminUserId();
    }

    /**
     * INTERNAL METHOD<br/>
     * Get the user or return null if user cannot be found,
     * attempts to retrieve the user from the internal set and from sakai
     * 
     * @param userId
     * @return user or null if none found
     */
    protected EvalUser getEvalUserOrNull(String userId) {
        EvalUser user = externalLogic.getEvalUserById(userId);
        if (user == null) {
            // try to get internal user from eval
            EvalAdhocUser adhocUser = adhocSupportLogic.getAdhocUserById(EvalAdhocUser.getIdFromAdhocUserId(userId));
            if (adhocUser != null) {
                user = new EvalUser(userId, EvalConstants.USER_TYPE_INTERNAL,
                        adhocUser.getEmail(), adhocUser.getUsername(), 
                        adhocUser.getDisplayName() == null ? adhocUser.getEmail() : adhocUser.getDisplayName());
            }
        }
        return user;
    }

    /**
     * INTERNAL METHOD<br/>
     * Generate an invalid user with fields filled out correctly,
     * userId and email should not both be null
     * 
     * @param userId can be null
     * @param email can be null
     * @return
     */
    protected EvalUser makeInvalidUser(String userId, String email) {
        if (userId == null) {
            userId = "invalid:" + email;
        }
        EvalUser user = new EvalUser(userId, EvalConstants.USER_TYPE_INVALID, email);
        user.displayName = EvalConstants.USER_TYPE_INVALID;
        return user;
    }

    /**
     * INTERNAL METHOD<br/>
     * Generate an anonymous user with fields filled out correctly
     * 
     * @param userId
     * @return
     */
    protected EvalUser makeAnonymousUser(String userId) {
        return new EvalUser(userId, EvalConstants.USER_TYPE_ANONYMOUS,
                null, "eid:" + userId, EvalConstants.USER_TYPE_ANONYMOUS);
    }

    public boolean isUserAnonymous(String userId) {
        return externalLogic.isUserAnonymous(userId);
    }

    public String getUserId(String username) {
        String userId = externalLogic.getUserId(username);
        if (userId == null) {
            EvalAdhocUser adhocUser = adhocSupportLogic.getAdhocUserByUsername(username);
            if (adhocUser != null) {
                userId = adhocUser.getUserId();
            }
        }
        return userId;
    }

    public String getUserUsername(String userId) {
        String username = "------";
        if (userId != null) {
            EvalUser user = getEvalUserOrNull(userId);
            if (user != null) {
                username = user.username;
            } else {
                username = externalLogic.getUserUsername(userId);
            }
        }
        return username;
    }


    public EvalUser getEvalUserById(String userId) {
        EvalUser user = null;
        if (userId != null) {
            EvalUser eu = getEvalUserOrNull(userId);
            if (eu != null) {
                user = eu;
            }
        }
        if (user == null) {
            user = makeInvalidUser(userId, null);
        }
        return user;
    }

    public EvalUser getEvalUserByEmail(String email) {
        EvalUser user = externalLogic.getEvalUserByEmail(email);
        if (user == null) {
            // get the internal user if possible
            EvalAdhocUser adhocUser = adhocSupportLogic.getAdhocUserByEmail(email);
            if (adhocUser != null) {
                user = new EvalUser(adhocUser.getUserId(), EvalConstants.USER_TYPE_INTERNAL,
                        adhocUser.getEmail(), adhocUser.getUsername(), 
                        adhocUser.getDisplayName() == null ? adhocUser.getEmail() : adhocUser.getDisplayName());
            }
        }
        if (user == null) {
            user = makeInvalidUser(null, email);
        }
        return user;
    }

    public List<EvalUser> getEvalUsersByIds(String[] userIds) {
        List<EvalUser> users = new ArrayList<EvalUser>();
        boolean foundAll = false;
        if (userIds == null 
                || userIds.length == 0) {
            foundAll = true;
        }

        Map<String, EvalUser> externalUsers = new HashMap<String, EvalUser>();
        if (! foundAll) {
            // get users from external
            externalUsers = externalLogic.getEvalUsersByIds(userIds);
            if (users.size() == userIds.length) {
                foundAll = true;
            }
        }

        Map<String, EvalAdhocUser> adhocUsers = new HashMap<String, EvalAdhocUser>();
        if (! foundAll) {
            // get as many internal users as possible
            adhocUsers = adhocSupportLogic.getAdhocUsersByUserIds(userIds);
        }

        /* now put the users into the list in the original order of the array 
         * with INVALID EvalUser objects in place of not-found users
         */
        for (int i = 0; i < userIds.length; i++) {
            String userId = userIds[i];
            EvalUser user = null;
            if (adhocUsers.containsKey(userId)) {
                EvalAdhocUser adhocUser = adhocUsers.get(userId);
                user = new EvalUser(adhocUser.getUserId(), EvalConstants.USER_TYPE_INTERNAL,
                        adhocUser.getEmail(), adhocUser.getUsername(), 
                        adhocUser.getDisplayName() == null ? adhocUser.getEmail() : adhocUser.getDisplayName());
            } else if (externalUsers.containsKey(userId)) {
                user = externalUsers.get(userId);
            } else {
                user = makeInvalidUser(userId, null);
            }
            users.add(user);
        }
        //    original not very efficient version -AZ
        //    for (int i = 0; i < userIds.length; i++) {
        //    String userId = userIds[i];
        //    users.add( getEvalUserById(userId) );
        //    }
        return users;
    }


    public boolean isUserAdmin(String userId) {
        log.debug("Checking is eval super admin for: " + userId);
        return externalLogic.isUserAdmin(userId);
    }

    public Locale getUserLocale(String userId) {
        log.debug("userId: " + userId);
        return externalLogic.getUserLocale(userId);
    }

    public EvalGroup makeEvalGroupObject(String evalGroupId) {
        if (evalGroupId == null) {
            throw new IllegalArgumentException("evalGroupId cannot be null");
        }

        EvalGroup c = null;

        if (c == null) {
            // check external
            c = externalLogic.makeEvalGroupObject(evalGroupId);
            if (c != null && EvalConstants.GROUP_TYPE_INVALID.equals(c.type)) {
                c = null;
            }
        }

        if (c == null) {
            // try to get the adhoc group
            EvalAdhocGroup adhocGroup = adhocSupportLogic.getAdhocGroupById(EvalAdhocGroup.getIdFromAdhocEvalGroupId(evalGroupId));
            if (adhocGroup != null) {
                c = new EvalGroup( evalGroupId, adhocGroup.getTitle(), 
                        EvalConstants.GROUP_TYPE_ADHOC );
            }
        }

        if (c == null) {
            // use external provider
            if (evalGroupsProvider != null) {
                c = evalGroupsProvider.getGroupByGroupId(evalGroupId);
                if (c != null) {
                    c.type = EvalConstants.GROUP_TYPE_PROVIDED;
                    if (c.title == null
                            || c.title.trim().length() == 0) {
                        c.title = UNKNOWN_TITLE;
                    }
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

    public String getCurrentEvalGroup() {
        String location = externalLogic.getCurrentEvalGroup();
        if (location == null) {
            location = NO_LOCATION;
            log.info("Could not get the current location (we are probably outside the portal), returning the NON-location one: " + location);
        }
        return location;
    }

    public String getDisplayTitle(String evalGroupId) {
        String title = null;
        EvalGroup group = makeEvalGroupObject(evalGroupId);
        if (group != null 
                && group.title != null) {
            title = group.title;
        }
        return title;
    }

    public int countEvalGroupsForUser(String userId, String permission) {
        log.debug("userId: " + userId + ", permission: " + permission);

        int count = externalLogic.countEvalGroupsForUser(userId, permission);

        // also check the adhoc groups
        //    taking this out for now because we do not want to allow adhoc groups to provide permission to create templates/evals
        //    List<EvalAdhocGroup> adhocGroups = adhocSupportLogic.getAdhocGroupsForOwner(userId);
        //    count += adhocGroups.size();

        // also check provider
        if (evalGroupsProvider != null) {
            if (EvalConstants.PERM_BE_EVALUATED.equals(permission) 
                    || EvalConstants.PERM_TAKE_EVALUATION.equals(permission)
                    || EvalConstants.PERM_ASSISTANT_ROLE.equals(permission) ) {
                log.debug("Using eval groups provider: userId: " + userId + ", permission: " + permission);
                count += evalGroupsProvider.countEvalGroupsForUser(userId, EvalExternalLogicImpl.translatePermission(permission));
            }
        }

        return count;
    }

    @SuppressWarnings("unchecked")
    public List<EvalGroup> getEvalGroupsForUser(String userId, String permission) {
        log.debug("userId: " + userId + ", permission: " + permission);

        List<EvalGroup> l = new ArrayList<EvalGroup>();

        // get the groups from external
        l.addAll( externalLogic.getEvalGroupsForUser(userId, permission) );

        // also check the internal groups
        List<EvalAdhocGroup> adhocGroups = adhocSupportLogic.getAdhocGroupsByUserAndPerm(userId, permission);
        for (EvalAdhocGroup adhocGroup : adhocGroups) {
            l.add( new EvalGroup(adhocGroup.getEvalGroupId(), adhocGroup.getTitle(), EvalConstants.GROUP_TYPE_ADHOC) );
        }

        // also check provider
        if (evalGroupsProvider != null) {
            if (EvalConstants.PERM_BE_EVALUATED.equals(permission) 
                    || EvalConstants.PERM_TAKE_EVALUATION.equals(permission)
                    || EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission)
                    || EvalConstants.PERM_ASSISTANT_ROLE.equals(permission) ) {
                log.debug("Using eval groups provider: userId: " + userId + ", permission: " + permission);
                List eg = evalGroupsProvider.getEvalGroupsForUser(userId, EvalExternalLogicImpl.translatePermission(permission));
                for (Iterator iter = eg.iterator(); iter.hasNext();) {
                    EvalGroup c = (EvalGroup) iter.next();
                    c.type = EvalConstants.GROUP_TYPE_PROVIDED;
                    l.add(c);
                }
            }
        }

        if (l.isEmpty()) log.info("Empty list of groups for user:" + userId + ", permission: " + permission);
        return l;
    }

    public int countUserIdsForEvalGroup(String evalGroupId, String permission) {
        // get the count from the method which retrieves all the groups,
        // this method might be better to retire
        return getUserIdsForEvalGroup(evalGroupId, permission).size();
    }

    public Set<String> getUserIdsForEvalGroup(String evalGroupId, String permission) {
        Set<String> userIds = new HashSet<String>();

        /* NOTE: we are assuming there is not much chance that there will be some users stored in
         * multiple data stores for the same group id so we only check until we find at least one user,
         * this means checks for user in groups with no users in them end up being really costly
         */

        // check external
        userIds.addAll( externalLogic.getUserIdsForEvalGroup(evalGroupId, permission) );

        // only go on to check the internal adhocs if nothing was found
        if (userIds.size() == 0) {
            // check internal adhoc groups
            if (EvalConstants.PERM_BE_EVALUATED.equals(permission) ||
                    EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
                Long id = EvalAdhocGroup.getIdFromAdhocEvalGroupId(evalGroupId);
                if (id != null) {
                    EvalAdhocGroup adhocGroup = adhocSupportLogic.getAdhocGroupById(id);
                    if (adhocGroup != null) {
                        String[] ids = null;
                        if (EvalConstants.PERM_BE_EVALUATED.equals(permission)) {
                            ids = adhocGroup.getEvaluateeIds();
                        } else if (EvalConstants.PERM_TAKE_EVALUATION.equals(permission)) {
                            ids = adhocGroup.getParticipantIds();
                        }
                        if (ids != null) {
                            for (int i = 0; i < ids.length; i++) {
                                userIds.add( ids[i] );
                            }
                        }
                    }
                }
            }
        }

        // check the provider if we still found nothing
        if (userIds.size() == 0) {
            // also check provider
            if (evalGroupsProvider != null) {
                if (EvalConstants.PERM_BE_EVALUATED.equals(permission) 
                        || EvalConstants.PERM_TAKE_EVALUATION.equals(permission)
                        || EvalConstants.PERM_ASSISTANT_ROLE.equals(permission) ) {
                    log.debug("Using eval groups provider: evalGroupId: " + evalGroupId + ", permission: " + permission);
                    userIds.addAll( evalGroupsProvider.getUserIdsForEvalGroups(new String[] {evalGroupId}, 
                            EvalExternalLogicImpl.translatePermission(permission)) );
                }
            }
        }

        return userIds;
    }

    public boolean isUserAllowedInEvalGroup(String userId, String permission, String evalGroupId) {

        /* NOTE: false checks end up being really costly and should probably be cached
         */

        if (evalGroupId == null) {
            // special check for the admin user
            if (isUserAdmin(userId)) {
                return true;
            }
            return false;
        }

        // try checking external first
        if ( externalLogic.isUserAllowedInEvalGroup(userId, permission, evalGroupId) ) {
            return true;
        }

        // check the internal groups next
        if ( adhocSupportLogic.isUserAllowedInAdhocGroup(userId, permission, evalGroupId) ) {
            return true;
        }

        // finally check provider
        if (evalGroupsProvider != null) {
            if (EvalConstants.PERM_BE_EVALUATED.equals(permission) 
                    || EvalConstants.PERM_TAKE_EVALUATION.equals(permission)
                    || EvalConstants.PERM_ASSISTANT_ROLE.equals(permission) ) {
                log.debug("Using eval groups provider: userId: " + userId + ", permission: " + permission + ", evalGroupId: " + evalGroupId);
                if ( evalGroupsProvider.isUserAllowedInGroup(userId, EvalExternalLogicImpl.translatePermission(permission), evalGroupId) ) {
                    return true;
                }
            }
        }

        return false;
    }

    public String[] sendEmailsToUsers(String from, String[] toUserIds, String subject, String message, boolean deferExceptions) {
        // handle the list of TO addresses
        List<EvalUser> l = getEvalUsersByIds(toUserIds);
        List<String> toEmails = new ArrayList<String>();
        // email address validity is checked at entry but value should not be null
        for (Iterator<EvalUser> iterator = l.iterator(); iterator.hasNext();) {
            EvalUser user = iterator.next();
            if ( user.email == null || "".equals(user.email) ) {
                iterator.remove();
                log.warn("sendEmails: Could not get an email address for " + user.displayName + " ("+user.userId+")");
            } else {
                toEmails.add(user.email);
            }
        }

        if (l == null || l.size() <= 0) {
            log.warn("No users with email addresses found in the provided userIds ("+ArrayUtils.arrayToString(toUserIds)+"), cannot send email so exiting");
            return new String[] {};
        }

        return sendEmailsToAddresses(from, toEmails.toArray(new String[] {}), subject, message, deferExceptions);
    }

    public String[] sendEmailsToAddresses(String from, String[] to, String subject, String message, boolean deferExceptions) {
        return externalLogic.sendEmailsToAddresses(from, to, subject, message, deferExceptions);
    }


    // ENTITIES

    public String getServerUrl() {
        return externalLogic.getServerUrl();
    }

    public String getEntityURL(Serializable evaluationEntity) {
        return externalLogic.getEntityURL(evaluationEntity);
    }

    public String getEntityURL(String entityPrefix, String entityId) {
        return externalLogic.getEntityURL(entityPrefix, entityId);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalExternalLogic#registerEntityEvent(java.lang.String, java.io.Serializable)
     */
    public void registerEntityEvent(String eventName, Serializable evaluationEntity) {
        externalLogic.registerEntityEvent(eventName, evaluationEntity);
    }

    public void registerEntityEvent(String eventName, Class<? extends Serializable> entityClass, String entityId) {
        externalLogic.registerEntityEvent(eventName, entityClass, entityId);
    }

    public <T> T getConfigurationSetting(String settingName, T defaultValue) {
        return externalLogic.getConfigurationSetting(settingName, defaultValue);
    }

    public byte[] getFileContent(String abspath) {
        return externalLogic.getFileContent(abspath);
    }

    public String cleanupUserStrings(String userSubmittedString) {
        if (userSubmittedString == null) {
            // nulls are ok
            return null;
        } else if (userSubmittedString.length() == 0) {
            // empty string is ok
            return "";
        }

        String cleanup = EvalUtils.cleanupHtmlPtags(userSubmittedString.trim());

        // use external cleaner
        cleanup = externalLogic.cleanupUserStrings(cleanup).trim();
        return cleanup;
    }

    public String makePlainTextFromHTML(String html) {
        if (html == null) {
            // nulls are ok
            return null;
        } else if (html.length() == 0) {
            // empty string is ok
            return "";
        }
        return externalLogic.makePlainTextFromHTML(html).trim();
    }


    // JOBS related pass-through methods

    public String createScheduledJob(Date executionDate, Long evaluationId, String jobType) {
        return externalLogic.createScheduledJob(executionDate, evaluationId, jobType);
    }

    public void deleteScheduledJob(String jobID) {
        externalLogic.deleteScheduledJob(jobID);
    }

    public EvalScheduledJob[] findScheduledJobs(Long evaluationId, String jobType) {
        return externalLogic.findScheduledJobs(evaluationId, jobType);
    }

    // ADHOC

    public EvalAdhocGroup getAdhocGroupById(Long adhocGroupId) {
        return adhocSupportLogic.getAdhocGroupById(adhocGroupId);
    }

    public List<EvalAdhocGroup> getAdhocGroupsForOwner(String userId) {
        return adhocSupportLogic.getAdhocGroupsForOwner(userId);
    }

    public void saveAdhocGroup(EvalAdhocGroup group) {
        String currentUserId = getCurrentUserId();
        if (! isUserAdmin(currentUserId)) {
            if (externalLogic.isUserAnonymous(currentUserId)) {
                throw new SecurityException("Anonymous users cannot create EvalAdhocGroups: " + currentUserId);
            }
            if (! currentUserId.equals(group.getOwner())) {
                throw new SecurityException("Only the owner ("+group.getOwner()+") can modify this group ("+group.getEvalGroupId()+"), this is the current user: " + currentUserId);            
            }
        }
        adhocSupportLogic.saveAdhocGroup(group);
    }

    public void saveAdhocUser(EvalAdhocUser user) {
        String currentUserId = getCurrentUserId();
        if (! isUserAdmin(currentUserId)) {
            if (externalLogic.isUserAnonymous(currentUserId)) {
                throw new SecurityException("Anonymous users cannot create EvalAdhocUsers: " + currentUserId);
            }
            if (! currentUserId.equals(user.getOwner()) &&
                    ! currentUserId.equals(user.getUserId()) ) {
                throw new SecurityException("Only the owner ("+user.getOwner()+") can modify this user ("+user.getUserId()+"), this is the current user: " + currentUserId);            
            }
        }
        adhocSupportLogic.saveAdhocUser(user);
    }


    // protected internal

    /**
     * @param text string to make MD5 hash from
     * @param maxLength
     * @return an MD5 hash no longer than maxLength
     */
    protected String makeMD5(String text, int maxLength) {

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


public String getContentCollectionId(String siteId) {
	// TODO Auto-generated method stub
	return externalLogic.getContentCollectionId(siteId);
}

}
