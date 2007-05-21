/******************************************************************************
 * EvalAssignsLogicImpl.java - created by aaronz@vt.edu on Dec 28, 2006
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

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalAssignsLogic;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.model.utils.EvalUtils;


/**
 * Implementation for EvalAssignsLogic
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalAssignsLogicImpl implements EvalAssignsLogic {

	private static Log log = LogFactory.getLog(EvalAssignsLogicImpl.class);

	private EvaluationDao dao;
	public void setDao(EvaluationDao dao) {
		this.dao = dao;
	}
	
	private EvalEmailsLogic emails;
	public void setEmails(EvalEmailsLogic emails) {
		this.emails = emails;
	}

	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	// INIT method
	public void init() {
		log.debug("Init");
	}



	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#saveAssignContext(org.sakaiproject.evaluation.model.EvalAssignContext, java.lang.String)
	 */
	public void saveAssignGroup(EvalAssignGroup assignContext, String userId) {
		log.debug("userId: " + userId + ", context: " + assignContext.getEvalGroupId());

		// set the date modified
		assignContext.setLastModified( new Date() );

		EvalEvaluation eval = assignContext.getEvaluation();
		if (eval == null || eval.getId() == null) {
			throw new IllegalStateException("Evaluation (" + eval.getId() + ") is not set or not saved for assignContext (" + 
					assignContext.getId() + "), evalgroupId: " + assignContext.getEvalGroupId() );
		}

		if (assignContext.getId() == null) {
			// creating new AC
			if (checkCreateAC(userId, eval)) {
				// check for duplicate AC first
				if ( checkRemoveDuplicateAC(assignContext) ) {
					throw new IllegalStateException("Duplicate mapping error, there is already an AC that defines a link from context: " + 
							assignContext.getEvalGroupId() + " to eval: " + eval.getId());
				}

				dao.save(assignContext);
				log.info("User ("+userId+") created a new AC ("+assignContext.getId()+"), " +
						"linked context ("+assignContext.getEvalGroupId()+") with eval ("+eval.getId()+")");
			}
		} else {
			// updating an existing AC

			// fetch the existing AC out of the DB to compare it
			EvalAssignGroup existingAC = (EvalAssignGroup) dao.findById(EvalAssignGroup.class, assignContext.getId());
			//log.info("AZQ: current AC("+existingAC.getId()+"): ctxt:" + existingAC.getContext() + ", eval:" + existingAC.getEvaluation().getId());

			// check the user control permissions
			if (! checkControlAC(userId, assignContext) ) {
				throw new SecurityException("User ("+userId+") attempted to update existing AC ("+existingAC.getId()+") without permissions");
			}

			// cannot change the evaluation or context so fail if they have been changed
			if (! existingAC.getEvalGroupId().equals(assignContext.getEvalGroupId())) {
				throw new IllegalArgumentException("Cannot update context ("+assignContext.getEvalGroupId()+
						") for an existing AC, context ("+existingAC.getEvalGroupId()+")");
			} else if (! existingAC.getEvaluation().getId().equals(eval.getId())) {
				throw new IllegalArgumentException("Cannot update eval ("+eval.getId()+
						") for an existing AC, eval ("+existingAC.getEvaluation().getId()+")");
			}

			// fill in defaults
			if (assignContext.getInstructorApproval() == null) {
				if ( EvalConstants.INSTRUCTOR_OPT_IN.equals(eval.getInstructorOpt()) ) {
					assignContext.setInstructorApproval( Boolean.FALSE );
				} else {
					assignContext.setInstructorApproval( Boolean.TRUE );
				}
			}
			
			// if a late instructor opt-in, notify students in this group that an evaluation is available
			if(EvalConstants.INSTRUCTOR_OPT_IN.equals(eval.getInstructorOpt()) && 
					assignContext.getInstructorApproval().booleanValue() && 
					assignContext.getEvaluation().getStartDate().before(new Date())) {
				emails.sendEvalAvailableGroupNotification(assignContext.getEvaluation().getId(), assignContext.getEvalGroupId());
			}
			
			if (assignContext.getInstructorsViewResults() == null) {
				if (eval.getInstructorsDate() != null) {
					assignContext.setInstructorsViewResults( Boolean.TRUE );
				} else {
					assignContext.setInstructorsViewResults( Boolean.FALSE );
				}
			}
			if (assignContext.getStudentsViewResults() == null) {
				if (eval.getStudentsDate() != null) {
					assignContext.setStudentsViewResults( Boolean.TRUE );
				} else {
					assignContext.setStudentsViewResults( Boolean.FALSE );
				}
			}

			// allow any other changes
			dao.save(assignContext);
			log.info("User ("+userId+") updated existing AC ("+assignContext.getId()+") properties");
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#deleteAssignContext(java.lang.Long, java.lang.String)
	 */
	public void deleteAssignGroup(Long assignContextId, String userId) {
		log.debug("userId: " + userId + ", assignContextId: " + assignContextId);

		// get AC
		EvalAssignGroup assignContext = (EvalAssignGroup) dao.findById(EvalAssignGroup.class, assignContextId);
		if (assignContext == null) {
			throw new IllegalArgumentException("Cannot find assign context with this id: " + assignContextId);
		}

		if ( checkRemoveAC(userId, assignContext) ) {
			dao.delete(assignContext);
			log.info("User ("+userId+") deleted existing AC ("+assignContext.getId()+")");
			return;
		}

		// should not get here so die if we do
		throw new RuntimeException("User ("+userId+") could NOT delete AC ("+assignContext.getId()+")");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#getAssignContextsByEvalId(java.lang.Long)
	 */
	public List getAssignGroupsByEvalId(Long evaluationId) {
		log.debug("evaluationId: " + evaluationId);

		// get evaluation to check id
		EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
		if (eval == null) {
			throw new IllegalArgumentException("Cannot find evaluation with this id: " + evaluationId);
		}

		return dao.findByProperties(EvalAssignGroup.class, 
				new String[] {"evaluation.id"}, 
				new Object[] {evaluationId});
	}


	// PERMISSIONS

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#canCreateAssignEval(java.lang.String, java.lang.Long)
	 */
	public boolean canCreateAssignEval(String userId, Long evaluationId) {
		log.debug("userId: " + userId + ", evaluationId: " + evaluationId);

		// get evaluation
		EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
		if (eval == null) {
			throw new IllegalArgumentException("Cannot find evaluation with this id: " + evaluationId);
		}

		try {
			return checkCreateAC(userId, eval);
		} catch (RuntimeException e) {
			log.info(e.getMessage());
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#canDeleteAssignContext(java.lang.String, java.lang.Long)
	 */
	public boolean canDeleteAssignGroup(String userId, Long assignContextId) {
		log.debug("userId: " + userId + ", assignContextId: " + assignContextId);

		// get AC
		EvalAssignGroup assignContext = (EvalAssignGroup) dao.findById(EvalAssignGroup.class, assignContextId);
		if (assignContext == null) {
			throw new IllegalArgumentException("Cannot find assign context with this id: " + assignContextId);
		}

		try {
			return checkRemoveAC(userId, assignContext);
		} catch (RuntimeException e) {
			log.info(e.getMessage());
		}
		return false;
	}

	// PRIVATE METHODS

	/**
	 * Check if user can control this AC
	 * @param userId
	 * @param assignContext
	 * @return true if can, false otherwise
	 */
	private boolean checkControlAC(String userId, EvalAssignGroup assignContext) {
		log.debug("userId: " + userId + ", assignContext: " + assignContext.getId());

		// check user permissions (just owner and super at this point)
		if ( userId.equals(assignContext.getOwner()) ||
				externalLogic.isUserAdmin(userId) ) {
			return true;
		} else {
			return false;
		}	
	}

	/**
	 * Check if the user can create an AC in an eval
	 * @param userId
	 * @param eval
	 * @return true if they can, throw exceptions otherwise
	 */
	private boolean checkCreateAC(String userId, EvalEvaluation eval) {
		log.debug("userId: " + userId + ", eval: " + eval.getId());

		// check state to see if assign contexts can be added
		String state = EvalUtils.getEvaluationState(eval);
		if (EvalConstants.EVALUATION_STATE_INQUEUE.equals(state) || 
				EvalConstants.EVALUATION_STATE_ACTIVE.equals(state)) {

			// check eval user permissions (just owner and super at this point)
			// TODO - find a way to centralize this check
			if (userId.equals(eval.getOwner()) ||
					externalLogic.isUserAdmin(userId)) {
				return true;
			} else {
				throw new SecurityException("User ("+userId+") cannot create assign context in evaluation ("+eval.getId()+"), do not have permission");
			}
		} else {
			throw new IllegalStateException("User ("+userId+") cannot create assign context in evaluation ("+eval.getId()+"), invalid eval state");
		}
	}

	/**
	 * Check if user can remove an AC
	 * @param userId
	 * @param assignContext
	 * @return true if they can, throw exceptions otherwise
	 */
	private boolean checkRemoveAC(String userId, EvalAssignGroup assignContext) {
		log.debug("userId: " + userId + ", assignContextId: " + assignContext.getId());

		// get evaluation from AC
		EvalEvaluation eval = assignContext.getEvaluation();
		String state = EvalUtils.getEvaluationState(eval);
		if (EvalConstants.EVALUATION_STATE_INQUEUE.equals(state)) {
			
			// check user permissions (just owner and super at this point)
			if ( checkControlAC(userId, assignContext) ) {
				return true;
			} else {
				throw new SecurityException("User ("+userId+") cannot remove assign context ("+assignContext.getId()+"), do not have permission");
			}
		} else {
			throw new IllegalStateException("User ("+userId+") cannot remove this assign context ("+assignContext.getId()+"), invalid eval state");
		}
	}

	/**
	 * Check for existing AC which matches this ones linkage
	 * @param ac
	 * @return true if duplicate found
	 */
	private boolean checkRemoveDuplicateAC(EvalAssignGroup ac) {
		log.debug("assignContext: " + ac.getId());

//		log.info("AZ1: current AC("+ac.getId()+"): ctxt:" + ac.getContext() + ", eval:" + ac.getEvaluation().getId());
		List l = dao.findByProperties(EvalAssignGroup.class, 
				new String[] {"evalGroupId", "evaluation.id"}, 
				new Object[] {ac.getEvalGroupId(), ac.getEvaluation().getId()});
		if ( (ac.getId() == null && l.size() >= 1) || 
				(ac.getId() != null && l.size() >= 2) ) {
			// there is an existing AC which does the same mapping
//			EvalAssignContext eac = (EvalAssignContext) l.get(0);
//			log.info("AZ2: fetched AC("+eac.getId()+"): ctxt:" + eac.getContext() + ", eval:" + eac.getEvaluation().getId());
			return true;
		}
//		log.info("AZ3: " + l.size());
		return false;
	}
}
