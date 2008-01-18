/******************************************************************************
 * EvalExternalLogicStub.java - created by aaronz@vt.edu on Dec 25, 2006
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;


/**
 * This is a stub class for testing purposes, it will allow us to test all the classes
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
public class EvalExternalLogicStub implements EvalExternalLogic {

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
	public List getEvalGroupsForUser(String userId, String permission) {
		List l = new ArrayList();
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
	 */
	public Set getUserIdsForEvalGroup(String context, String permission) {
		Set s = new HashSet();
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
		return EvalTestDataLoad.SITE1_REF;
	}

	/**
	 * Always assume the current user is USER_ID
	 */
	public String getCurrentUserId() {
		return EvalTestDataLoad.USER_ID;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.ExternalUsers#isUserAnonymous(java.lang.String)
	 */
	public boolean isUserAnonymous(String userId) {
		if (userId.equals(EvalTestDataLoad.USER_ID) ||
				userId.equals(EvalTestDataLoad.MAINT_USER_ID) ||
				userId.equals(EvalTestDataLoad.ADMIN_USER_ID)) {
			return false;
		}
		return true;
	}

	/**
	 * Return titles from the data load class
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
	 * Return names from the data load class
	 */
	public String getUserDisplayName(String userId) {
		if ( EvalTestDataLoad.ADMIN_USER_ID.equals(userId) ) {
			return EvalTestDataLoad.ADMIN_USER_DISPLAY;
		} else if ( EvalTestDataLoad.MAINT_USER_ID.equals(userId) ) {
			return EvalTestDataLoad.MAINT_USER_DISPLAY;			
		} else if ( EvalTestDataLoad.USER_ID.equals(userId) ) {
			return EvalTestDataLoad.USER_DISPLAY;			
		}
		return "----------";
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
	public boolean isUserAdmin(String userId) {
		if ( EvalTestDataLoad.ADMIN_USER_ID.equals(userId) ) {
			return true;
		}
		return false;
	}

	public Locale getUserLocale(String userId) {
		return Locale.US;
	}


	/**
	 * Return Context objects based on data from the data load class
	 * CONTEXT1 = Site, CONTEXT2 = Group
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

	/**
	 * check if the options entered were nulls
	 */
	public void sendEmails(String from, String[] to, String subject, String message) {
		if (from == null || to == null || subject == null || message == null) {
			throw new NullPointerException("All params are required (none can be null)");
		}
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

	public String getMyWorkspaceUrl(String id) {
		return "https://testctools.ds.itd.umich.edu/portal/site/~rwellis/page/866dd4e6-0323-43a1-807c-9522bb3167b7";
	}

	public Map<String, String> getNotificationSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserEmail(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
