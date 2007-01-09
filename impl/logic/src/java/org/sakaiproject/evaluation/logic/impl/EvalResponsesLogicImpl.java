/******************************************************************************
 * EvalResponsesLogicImpl.java - created by aaronz@vt.edu
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.model.utils.EvalUtils;
import org.sakaiproject.genericdao.api.finders.ByPropsFinder;


/**
 * Implementation for EvalResponsesLogic
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalResponsesLogicImpl implements EvalResponsesLogic {

	private static Log log = LogFactory.getLog(EvalResponsesLogicImpl.class);

	private EvaluationDao dao;
	public void setDao(EvaluationDao dao) {
		this.dao = dao;
	}

	private EvalExternalLogic external;
	public void setExternalLogic(EvalExternalLogic external) {
		this.external = external;
	}

	// requires access to evaluations logic to check if user can take the evaluation
	private EvalEvaluationsLogic evaluations;
	public void setEvaluations(EvalEvaluationsLogic evaluations) {
		this.evaluations = evaluations;
	}


	// INIT method
	public void init() {
		log.debug("Init");
	}



	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalResponsesLogic#getEvaluationResponses(java.lang.String, java.lang.Long[])
	 */
	public List getEvaluationResponses(String userId, Long[] evaluationIds) {
		log.debug("userId: " + userId + ", evaluationIds: " + evaluationIds);

		if (evaluationIds.length <= 0) {
			throw new IllegalArgumentException("evaluationIds cannot be empty");
		}

		// check that the ids are actually valid
		int count = dao.countByProperties(EvalEvaluation.class, 
				new String[] {"id"},
				new Object[] {evaluationIds} );
		if (count != evaluationIds.length) {
			throw new IllegalArgumentException("One or more invalid evaluation ids in evaluationIds: " + evaluationIds);
		}

		if (external.isUserAdmin(userId)) {
			// if user is admin then return all matching responses for this evaluation
			return dao.findByProperties(EvalResponse.class,
					new String[] {"evaluation.id"},
					new Object[] {evaluationIds},
					new int[] {ByPropsFinder.EQUALS},
					new String[] {"id"});
		} else {
			// not admin, only return the responses for this user
			return dao.findByProperties(EvalResponse.class,
					new String[] {"owner","evaluation.id"},
					new Object[] {userId, evaluationIds},
					new int[] {ByPropsFinder.EQUALS, ByPropsFinder.EQUALS},
					new String[] {"id"});
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalResponsesLogic#countResponses(java.lang.Long, java.lang.String)
	 */
	public int countResponses(Long evaluationId, String context) {
		log.debug("evaluationId: " + evaluationId + ", context: " + context);

		if ( dao.countByProperties(EvalEvaluation.class, 
				new String[] {"id"}, new Object[] {evaluationId} ) <= 0 ) {
			throw new IllegalArgumentException("Could not find evaluation with id: " + evaluationId);
		}

		if (context == null) {
			// returns count of all responses in all contexts if context is null
			return dao.countByProperties(EvalResponse.class,
					new String[] {"evaluation.id"},
					new Object[] {evaluationId} );
		} else {
			// returns count of responses in this context only if set
			return dao.countByProperties(EvalResponse.class,
					new String[] {"evaluation.id", "context"},
					new Object[] {evaluationId, context} );			
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalResponsesLogic#getEvalAnswers(java.lang.Long, java.lang.Long)
	 */
	public List getEvalAnswers(Long itemId, Long evaluationId) {
		log.debug("itemId: " + itemId + ", evaluationId: " + evaluationId);

		if ( dao.countByProperties(EvalItem.class, 
				new String[] {"id"}, new Object[] {itemId} ) <= 0 ) {
			throw new IllegalArgumentException("Could not find item with id: " + itemId);
		}

		if ( dao.countByProperties(EvalEvaluation.class, 
				new String[] {"id"}, new Object[] {evaluationId} ) <= 0 ) {
			throw new IllegalArgumentException("Could not find evaluation with id: " + evaluationId);
		}

		return dao.getAnswers(itemId, evaluationId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalResponsesLogic#saveResponse(org.sakaiproject.evaluation.model.EvalResponse, java.lang.String)
	 */
	public void saveResponse(EvalResponse response, String userId) {
		log.debug("userId: " + userId + ", response: " + response.getId() + ", context: " + response.getContext());

		// set the date modified
		response.setLastModified( new Date() );

		if (response.getId() != null) {
			// TODO - existing response, don't allow change to any setting except starttime, endtime, and answers
		}

		// fill in any default values and nulls here

		// check perms and evaluation state
		if (checkUserModifyReponse(userId, response)) {
			// make sure the user can take this evalaution
			Long evaluationId = response.getEvaluation().getId();
			String context = response.getContext();
			if (! evaluations.canTakeEvaluation(userId, evaluationId, context)) {
				throw new IllegalStateException("User ("+userId+") cannot take this evaluation ("+evaluationId+") in this context ("+context+") right now");
			}

			// check to make sure answers are valid for this evaluation
			if (response.getAnswers() != null &&
					! response.getAnswers().isEmpty()) {
				// TODO - this is not doing anything yet
				//checkAnswersValidForEval(response);
			}

			if (response.getEndTime() != null) {
				// the response is complete
				// TODO - add logic to lock associated evaluations here
				log.error("TODO - Locking associated evaluations not implemented yet");
			}

			// save everything in one transaction
			Set[] entitySets = new Set[2];

			// response has to be saved first
			entitySets[0] = new HashSet();
			entitySets[0].add(response);
			entitySets[1] = response.getAnswers();

			dao.saveMixedSet(entitySets);

			int answerCount = response.getAnswers() == null ? 0 : response.getAnswers().size();
			log.info("User ("+userId+") saved response ("+response.getId()+"), context (" + 
					response.getContext() + ") and " + answerCount + " answers");
			return;
		}

		// should not get here so die if we do
		throw new RuntimeException("User ("+userId+") could NOT save response ("+response.getId()+"), context: " + response.getContext());
	}

	// PERMISSIONS

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalResponsesLogic#canModifyResponse(java.lang.String, java.lang.Long)
	 */
	public boolean canModifyResponse(String userId, Long responseId) {
		log.debug("userId: " + userId + ", responseId: " + responseId);
		// get the response by id
		EvalResponse response = (EvalResponse) dao.findById(EvalResponse.class, responseId);
		if (response == null) {
			throw new IllegalArgumentException("Cannot find response with id: " + responseId);
		}

		// valid state, check perms and locked
		try {
			return checkUserModifyReponse(userId, response);
		} catch (RuntimeException e) {
			log.info(e.getMessage());
		}			
		return false;
	}


	// INTERNAL METHODS

	/**
	 * Check if user has permission to modify this response
	 * @param userId
	 * @param response
	 * @return true if they do, exception otherwise
	 */
	protected boolean checkUserModifyReponse(String userId, EvalResponse response) {
		log.debug("context: " + response.getContext() + ", userId: " + userId);

		String state = EvalUtils.getEvaluationState( response.getEvaluation() );
		if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(state) ||
				EvalConstants.EVALUATION_STATE_ACTIVE.equals(state) ) {
			// check admin (admins can never save responses)
			if ( external.isUserAdmin(userId) ) {
				throw new IllegalArgumentException("Admin user ("+userId+") cannot create response ("+response.getId()+"), admins can never save responses");
			}

			// check ownership
			if ( response.getOwner().equals(userId) ) {
				return true;
			} else {
				throw new SecurityException("User ("+userId+") cannot modify response ("+response.getId()+") without permissions");
			}
		} else {
			throw new IllegalStateException("Evaluation state ("+state+") not valid for modifying responses");
		}
	}

	/**
	 * Checks the answers in the response for validity<br/>
	 * NOT WORKING YET
	 * 
	 * @param response
	 * @return true if all answers valid, exception otherwise
	 */
	protected boolean checkAnswersValidForEval(EvalResponse response) {
		// TODO - this should be calling a method somewhere else
		Long templateId = response.getEvaluation().getTemplate().getId();

		Long[] itemIds = new Long[response.getAnswers().size()];
		int i = 0;
		for (Iterator iter = response.getAnswers().iterator(); iter.hasNext(); i++) {
			EvalAnswer answer = (EvalAnswer) iter.next();
			itemIds[i] = answer.getItem().getId();
		}

		int count = dao.countByProperties(EvalItem.class,
				new String[] {"id", "templates.id"},
				new Object[] {itemIds, templateId} );
		if (count != itemIds.length) {
			throw new IllegalArgumentException("Invalid answers in the response, answers must correspond to items in this evaluation");
		}

		// TODO - check if numerical answers are valid

		return true;
	}

}
