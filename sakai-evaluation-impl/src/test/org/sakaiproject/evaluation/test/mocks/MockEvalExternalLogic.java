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
package org.sakaiproject.evaluation.test.mocks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.quartz.Job;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalScheduledJob;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.logic.model.HierarchyNodeRule;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.utils.EvalUtils;


/**
 * This is a mock class for testing purposes, it will allow us to test all the classes
 * that depend on it since it has way to many external dependencies to make it worth
 * it to mock them all up<br/>
 * <br/>
 * It is emulating the following system state:<br/>
 * 4 users: ADMIN_USER_ID (super admin), MAINT_USER_ID, USER_ID, STUDENT_USER_ID
 * 2 sites:<br/>
 * 1) CONTEXT1/SITE_ID (Site) -
 * USER_ID can take eval, MAINT_USER_ID can admin evals and be evaluated (cannot take)<br/>
 * 2) CONTEXT2/SITE2_ID (Group) -
 * USER_ID and STUDENT_USER_ID can take eval, MAINT_USER_ID can be evaluated but can not admin (cannot take)<br/>
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class MockEvalExternalLogic implements EvalExternalLogic {

    /**
     * Note: Admin has all perms in all sites
     * 2 sites:<br/>
     * 1) CONTEXT1/SITE_ID -
     * USER_ID can take eval, MAINT_USER_ID can admin evals and be evaluated (cannot take)<br/>
     * 2) CONTEXT2/SITE2_ID -
     * USER_ID and STUDENT_USER_ID can take eval, MAINT_USER_ID can be evaluated but can not admin (cannot take)<br/>
     */
    public int countEvalGroupsForUser(String userId, String permission) {
        if ( EvalTestDataLoad.ADMIN_USER_ID.equals(userId) ) {
            return 2;
        } else if ( EvalTestDataLoad.MAINT_USER_ID.equals(userId) ) {
            if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
                return 1;
            } else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
                return 2;
            } else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
                return 0;				
            } else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
                return 1;
            } else {
                return 0;				
            }
        } else if ( EvalTestDataLoad.USER_ID.equals(userId) ) {
            if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
                return 0;
            } else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
                return 0;
            } else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
                return 2;
            } else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
                return 0;
            } else {
                return 0;
            }
        } else if ( EvalTestDataLoad.STUDENT_USER_ID.equals(userId) ) {
            if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
                return 0;
            } else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
                return 0;
            } else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
                return 1;
            } else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
                return 0;
            } else {
                return 0;
            }
        } else {
            // do nothing
        }
        return 0;
    }


    /**
     * Note: Admin has all perms in all sites
     * 2 sites:<br/>
     * 1) CONTEXT1/SITE_ID -
     * USER_ID can take eval, MAINT_USER_ID can admin evals and be evaluated (cannot take)<br/>
     * 2) CONTEXT2/SITE2_ID -
     * USER_ID and STUDENT_USER_ID can take eval, MAINT_USER_ID can be evaluated but can not admin (cannot take)<br/>
     */
    public List<EvalGroup> getEvalGroupsForUser(String userId, String permission) {
        List<EvalGroup> l = new ArrayList<>();
        if ( EvalTestDataLoad.ADMIN_USER_ID.equals(userId) ) {
            l.add( makeEvalGroupObject(EvalTestDataLoad.SITE1_REF) );
            l.add( makeEvalGroupObject(EvalTestDataLoad.SITE2_REF) );
        } else if ( EvalTestDataLoad.MAINT_USER_ID.equals(userId) ) {
            if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
                l.add( makeEvalGroupObject(EvalTestDataLoad.SITE1_REF) );
            } else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
                l.add( makeEvalGroupObject(EvalTestDataLoad.SITE1_REF) );
                l.add( makeEvalGroupObject(EvalTestDataLoad.SITE2_REF) );
            } else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
                // nothing
            } else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
                l.add( makeEvalGroupObject(EvalTestDataLoad.SITE1_REF) );
            } else {
                // nothing
            }
        } else if ( EvalTestDataLoad.USER_ID.equals(userId) ) {
            if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
                // nothing
            } else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
                // nothing
            } else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
                l.add( makeEvalGroupObject(EvalTestDataLoad.SITE1_REF) );
                l.add( makeEvalGroupObject(EvalTestDataLoad.SITE2_REF) );
            } else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
                // nothing
            } else {
                // nothing
            }
        } else if ( EvalTestDataLoad.STUDENT_USER_ID.equals(userId) ) {
            if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
                // nothing
            } else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
                // nothing
            } else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
                l.add( makeEvalGroupObject(EvalTestDataLoad.SITE2_REF) );
            } else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
                // nothing
            } else {
                // nothing
            }
        } else {
            // do nothing
        }
        return l;
    }

    /**
     * Note: Admin has all perms in all sites
     * 2 sites:<br/>
     * 1) CONTEXT1/SITE_ID -
     * USER_ID can take eval, MAINT_USER_ID can admin evals and be evaluated (cannot take)<br/>
     * 2) CONTEXT2/SITE2_ID -
     * USER_ID and STUDENT_USER_ID can take eval, MAINT_USER_ID can be evaluated but can not admin (cannot take)<br/>
     * @param context
     * @param permission
     * @return 
     */
    public Set<String> getUserIdsForEvalGroup(String context, String permission) {
        Set<String> s = new HashSet<>();
        // Maybe should add the admin user here? -AZ
        if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
            if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
                s.add(EvalTestDataLoad.MAINT_USER_ID);
            } else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
                s.add(EvalTestDataLoad.MAINT_USER_ID);
            } else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
                s.add(EvalTestDataLoad.USER_ID);
            } else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
                s.add(EvalTestDataLoad.MAINT_USER_ID);
            } else {
                // nothing
            }
        } else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
            if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
                // nothing
            } else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
                s.add(EvalTestDataLoad.MAINT_USER_ID);
            } else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
                s.add(EvalTestDataLoad.USER_ID);
                s.add(EvalTestDataLoad.STUDENT_USER_ID);
            } else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
                // nothing
            } else {
                // nothing
            }
        } else if ( EvalTestDataLoad.EVALSYS_1007_SITE_REF_01.equals(context) ){
        	if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
        		s.add(EvalTestDataLoad.EVALSYS_1007_MAINT_USER_ID_01);
        	} else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
        		s.add(EvalTestDataLoad.EVALSYS_1007_USER_ID_01);
        	}			
		} else if ( EvalTestDataLoad.SITE4_REF.equals(context) ) {
			if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
				s.add(EvalTestDataLoad.MAINT_USER_ID_3);
			} else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				s.add(EvalTestDataLoad.USER_ID_4);
			} else {
				// nothing
			}
		} else if ( EvalTestDataLoad.SITE5_REF.equals(context) ) {
			if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
				s.add(EvalTestDataLoad.MAINT_USER_ID_3);
			} else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				s.add(EvalTestDataLoad.USER_ID_4);
			} else {
				// nothing
			}
		} else if ( EvalTestDataLoad.SITE6_REF.equals(context) ) {
			if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
				s.add(EvalTestDataLoad.MAINT_USER_ID_3);
			} else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				s.add(EvalTestDataLoad.USER_ID_4);
			} else {
				// nothing
			}
		} else if ( EvalTestDataLoad.SITE7_REF.equals(context) ) {
			if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
				s.add(EvalTestDataLoad.USER_ID_5);
			} else {
				// nothing
			}
		} else {
            // do nothing
        }
        return s;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalContexts#countUserIdsForContext(java.lang.String, java.lang.String)
     */
    public int countUserIdsForEvalGroup(String context, String permission) {
        // just use the other stub method
        return getUserIdsForEvalGroup(context, permission).size();
    }

    /**
     * Note: Admin has all perms in all sites
     * 2 sites:<br/>
     * 1) CONTEXT1/SITE_ID -
     * USER_ID can take eval, MAINT_USER_ID can admin evals and be evaluated (cannot take)<br/>
     * 2) CONTEXT2/SITE2_ID -
     * USER_ID and STUDENT_USER_ID can take eval, MAINT_USER_ID can be evaluated but can not admin (cannot take)<br/>
     * @param context
     */
    public boolean isUserAllowedInEvalGroup(String userId, String permission, String context) {
        if ( EvalTestDataLoad.ADMIN_USER_ID.equals(userId) ) {
            return true;
        } else if ( EvalTestDataLoad.MAINT_USER_ID.equals(userId) ) {
            if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
                if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
                    return true;
                } else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
                    return false;					
                }
            } else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
                if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
                    return true;
                } else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
                    return true;					
                }
            } else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
                if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
                    return false;
                } else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
                    return false;					
                }
            } else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
                if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
                    return true;
                } else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
                    return false;					
                }
            }
        } else if ( EvalTestDataLoad.USER_ID.equals(userId) ) {
            if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
                if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
                    return false;
                } else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
                    return false;					
                }
            } else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
                if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
                    return false;
                } else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
                    return false;					
                }
            } else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
                if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
                    return true;
                } else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
                    return true;					
                }
            } else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
                if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
                    return false;
                } else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
                    return false;					
                }
            }
        } else if ( EvalTestDataLoad.STUDENT_USER_ID.equals(userId) ) {
            if ( EvalConstants.PERM_ASSIGN_EVALUATION.equals(permission) ) {
                if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
                    return false;
                } else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
                    return false;					
                }
            } else if ( EvalConstants.PERM_BE_EVALUATED.equals(permission) ) {
                if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
                    return false;
                } else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
                    return false;					
                }
            } else if ( EvalConstants.PERM_TAKE_EVALUATION.equals(permission) ) {
                if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
                    return false;
                } else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
                    return true;					
                }
            } else if ( EvalConstants.PERM_WRITE_TEMPLATE.equals(permission) ) {
                if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
                    return false;
                } else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
                    return false;					
                }
            }
        } else {
            // do nothing
        }
        return false;
    }

    /**
     * Always assume the current evalGroupId is CONTEXT1
     */
    public String getCurrentEvalGroup() {
        return currentGroupId;
    }

    /**
     * Always assume the current user is USER_ID
     */
    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getAdminUserId() {
        return EvalTestDataLoad.ADMIN_USER_ID;
    }

    public List<EvalUser> getSakaiAdmins() {
        List<EvalUser> sakaiAdminList = new ArrayList<>();
        sakaiAdminList.add(this.getEvalUserById(EvalTestDataLoad.ADMIN_USER_ID));
        return sakaiAdminList;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#isUserAnonymous(java.lang.String)
     */
    public boolean isUserAnonymous(String userId) {
        return !(userId.equals(EvalTestDataLoad.USER_ID) 
                 ||	userId.equals(EvalTestDataLoad.MAINT_USER_ID)
                 ||	userId.equals(EvalTestDataLoad.ADMIN_USER_ID)
                 || userId.equals(EvalTestDataLoad.STUDENT_USER_ID)
                 || userId.equals(EvalTestDataLoad.MAINT_USER_ID_3));
    }

    public EvalUser getEvalUserById(String userId) {
        EvalUser user = new EvalUser(userId, EvalConstants.USER_TYPE_INVALID, null);
        if ( EvalTestDataLoad.ADMIN_USER_ID.equals(userId) ) {
            user = new EvalUser(userId, EvalConstants.USER_TYPE_INTERNAL, userId + "@institution.edu",
                    EvalTestDataLoad.ADMIN_USER_NAME, EvalTestDataLoad.ADMIN_USER_DISPLAY);
        } else if ( EvalTestDataLoad.MAINT_USER_ID.equals(userId) ) {
            user = new EvalUser(userId, EvalConstants.USER_TYPE_EXTERNAL, userId + "@institution.edu",
                    EvalTestDataLoad.MAINT_USER_NAME, EvalTestDataLoad.MAINT_USER_DISPLAY);
        } else if ( EvalTestDataLoad.USER_ID.equals(userId) ) {
            user = new EvalUser(userId, EvalConstants.USER_TYPE_EXTERNAL, userId + "@institution.edu",
                    EvalTestDataLoad.USER_NAME, EvalTestDataLoad.USER_DISPLAY);
        } else if ( EvalTestDataLoad.STUDENT_USER_ID.equals(userId) ) {
            user = new EvalUser(userId, EvalConstants.USER_TYPE_EXTERNAL, userId + "@institution.edu",
                    EvalTestDataLoad.STUDENT_USER_ID, EvalTestDataLoad.STUDENT_USER_ID + "name");
        }
        return user;
    }

    public EvalUser getEvalUserByEmail(String email) {
        String userId = email; // userId + "@institution.edu"
        int position = userId.indexOf('@');
        if (position != -1) {
            userId = userId.substring(0, position);
        }
        EvalUser user = getEvalUserById(userId);
        return user;
    }

    /**
     * Return usernames from the data load class
     */
    public String getUserUsername(String userId) {
        if ( EvalTestDataLoad.ADMIN_USER_ID.equals(userId) ) {
            return EvalTestDataLoad.ADMIN_USER_NAME;
        } else if ( EvalTestDataLoad.MAINT_USER_ID.equals(userId) ) {
            return EvalTestDataLoad.MAINT_USER_NAME;			
        } else if ( EvalTestDataLoad.USER_ID.equals(userId) ) {
            return EvalTestDataLoad.USER_NAME;			
        }
        return "------";
    }


    public String getUserId(String username) {
        if ( EvalTestDataLoad.ADMIN_USER_NAME.equals(username) ) {
            return EvalTestDataLoad.ADMIN_USER_ID;
        } else if ( EvalTestDataLoad.MAINT_USER_NAME.equals(username) ) {
            return EvalTestDataLoad.MAINT_USER_ID;            
        } else if ( EvalTestDataLoad.USER_NAME.equals(username) ) {
            return EvalTestDataLoad.USER_ID;          
        }
        return null;
    }

    /**
     * only true for ADMIN_USER_ID
     */
    public boolean isUserSakaiAdmin(String userId) {
        return EvalTestDataLoad.ADMIN_USER_ID.equals(userId);
    }

    public Locale getUserLocale(String userId) {
        return Locale.US;
    }

    /**
     * Return titles from the data load class
     * @param evalGroupId
     * @return 
     */
    public String getDisplayTitle(String evalGroupId) {
        if ( EvalTestDataLoad.SITE1_REF.equals(evalGroupId) ) {
            return EvalTestDataLoad.SITE1_TITLE;
        } else if ( EvalTestDataLoad.SITE2_REF.equals(evalGroupId) ) {
            return EvalTestDataLoad.SITE2_TITLE;         
        }
        return "--------";
    }

    /**
     * Return Context objects based on data from the data load class
     * CONTEXT1 = Site, CONTEXT2 = Group
     * @param context
     */
    public EvalGroup makeEvalGroupObject(String context) {
        EvalGroup c = new EvalGroup(context, null, EvalConstants.GROUP_TYPE_UNKNOWN);
        if ( EvalTestDataLoad.SITE1_REF.equals(context) ) {
            c.title = EvalTestDataLoad.SITE1_TITLE;
            c.type = EvalConstants.GROUP_TYPE_SITE;
        } else if ( EvalTestDataLoad.SITE2_REF.equals(context) ) {
            c.title = EvalTestDataLoad.SITE2_TITLE;			
            c.type = EvalConstants.GROUP_TYPE_GROUP;
        }
        return c;
    }

    public String[] sendEmailsToUsers(String from, String[] to, String subject, String message, boolean deferExceptions) {
        if (from == null || to == null || subject == null || message == null) {
            throw new NullPointerException("All params are required (none can be null)");
        }
        emailsSentCounter += to.length;
        return to;
    }

    public String[] sendEmailsToAddresses(String from, String[] to, String subject, String message,
            boolean deferExceptions) {
        if (from == null || to == null || subject == null || message == null) {
            throw new NullPointerException("All params are required (none can be null)");
        }
        emailsSentCounter += to.length;
        return to;
    }

    public String getServerUrl() {
        return "http://localhost:8080/portal/";
    }

    public String getEntityURL(Serializable evaluationEntity) {
        return "http://localhost:8080/access/eval-evaluation/123/";
    }

    public String getEntityURL(String entityPrefix, String entityId) {
        return getEntityURL(null);
    }

    public void registerEntityEvent(String eventName, Serializable evaluationEntity) {
        // pretending it worked
    }

    public void registerEntityEvent(String eventName, Class<? extends Serializable> entityClass, String entityId) {
        // pretending it worked
    }



    @SuppressWarnings("unchecked")
    public <T> T getConfigurationSetting(String settingName, T defaultValue) {
        T returnValue = defaultValue;
        if (defaultValue == null) {
            returnValue = (T) (settingName + ":NULL");
        }
        return returnValue;
    }

    public byte[] getFileContent(String abspath) {
        return new byte[] {'H','E','L','L','O',' ','E','V','A','L','U','A','T','I','O','N' };
    }

    public String cleanupUserStrings(String userSubmittedString) {
        return EvalUtils.cleanupHtmlPtags(userSubmittedString);
    }

    public String makePlainTextFromHTML(String html) {
        return html;
    }



    // FOR scheduling
    // the methods below should return some fake jobs which will correctly correspond to the jobs one would expect to find
    // in the system based on the current test dats (i.e. maybe 8 or so jobs should exist based on the current test data)

    public String createScheduledJob(Date executionDate, Long evaluationId, String jobType) {
        // TODO - make these return some fake data for testing - Dick should do this when he writes tests for this code
        throw new UnsupportedOperationException();
    }

    public void deleteScheduledJob(String jobID) {
        // TODO - make these return some fake data for testing - Dick should do this when he writes tests for this code
        throw new UnsupportedOperationException();
    }

    public EvalScheduledJob[] findScheduledJobs(Long evaluationId, String jobType) {
        // TODO - make these return some fake data for testing - Dick should do this when he writes tests for this code
        throw new UnsupportedOperationException();
    }


    // testing methods

    private int emailsSentCounter = 0;
    /**
     * TESTING method:
     * Provides a way to determine the number of emails sent via this mock service since the service started up
     * or the counter was reset, reset the counter using {@link #resetEmailsSentCounter()}
     * @return 
     */
    public int getNumEmailsSent() {
        return emailsSentCounter;
    }
    /**
     * TESTING method:
     * Resets the emails sent test counter to 0
     */
    public void resetEmailsSentCounter() {
        emailsSentCounter = 0;
    }

    private String currentUserId = EvalTestDataLoad.USER_ID;
    public void setCurrentUserId(String userId) {
        currentUserId = userId;
    }

    private String currentGroupId = EvalTestDataLoad.SITE1_REF;
    public void setCurrentGroupId(String evalGroupId) {
        currentGroupId = evalGroupId;
    }

    public <T> T getBean(Class<T> type) {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, EvalUser> getEvalUsersByIds(String[] userIds) {
        Map<String, EvalUser> users = new HashMap<>();
        for (String userId : userIds) {
            users.put(userId, getEvalUserById(userId) );
        }
        return users;
    }

    public String getContentCollectionId(String siteId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<String[]> getUsersByRole(String evalGroupId, String perm) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<EvalGroup> getFilteredEvalGroupsForUser(String arg0, String arg1,
            String arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isEvalGroupPublished(String evalGroupId) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public String scheduleCronJob(Class<? extends Job> jobClass, Map<String, String> dataMap) {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String,Map<String, String>> getCronJobs(String jobGroup) {
        return null;
    }

    public String getServerId() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<String> getServers() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean deleteCronJob(String jobName, String groupName) {
        return false;
    }

    @Override
    public String scheduleCronJob(String jobClassBeanId, Map<String, String> dataMap) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getMyWorkspaceDashboard(String userId) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setSessionTimeout(int Seconds) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isUserReadonlyAdmin(String userId) {
        // TODO Auto-generated method stub
        return false;
    }

    public List<EvalGroup> makeEvalGroupObjectsForSectionAwareness( String evalGroupId )
    {
        return new ArrayList<>();
    }

    public int countUserIdsForEvalGroup( String evalGroupID, String permission, Boolean sectionAware )
    {
        return getUserIdsForEvalGroup( evalGroupID, permission, sectionAware ).size();
    }

    public Set<String> getUserIdsForEvalGroup( String evalGroupID, String permission, Boolean sectionAware )
    {
        return new HashSet<>();
    }

    @Override
    public List<HierarchyNodeRule> getRulesByNodeID( Long nodeID )
    {
        return new ArrayList<>();
    }

    @Override
    public void assignNodeRule( String ruleText, String qualifier, String option, Long nodeID )
    {
    }

    @Override
    public void updateNodeRule( Long ruleID, String ruleText, String qualifier, String option, Long nodeID )
    {
    }

    @Override
    public void removeNodeRule( Long ruleID )
    {
    }

    @Override
    public void removeAllRulesForNode( Long nodeID )
    {
    }

    @Override
    public HierarchyNodeRule getRuleByID( Long ruleID )
    {
        return null;
    }

    @Override
    public boolean isRuleAlreadyAssignedToNode( String ruleText, String qualifierSelection, String optionSelection, Long nodeID )
    {
        return false;
    }

    @Override
    public List<HierarchyNodeRule> getAllRules()
    {
        return new ArrayList<>();
    }
}
