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
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.utils.EvalUtils;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
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


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.evaluation.logic.EvalResponsesLogic#getNonResponders(java.lang.Long)
	 */
	public Set getNonResponders(Long evaluationId, EvalGroup group) {

		Long[] evaluationIds = { evaluationId };
		Set userIds = new HashSet();

		// get everyone permitted to take the evaluation
		Set ids = external.getUserIdsForEvalGroup(group.evalGroupId, EvalConstants.PERM_TAKE_EVALUATION);
		for (Iterator i = ids.iterator(); i.hasNext();) {
			String userId = (String) i.next();

			// if this user hasn't submitted a response, add the user's id
			if (getEvaluationResponses(userId, evaluationIds).isEmpty()) {
				userIds.add(userId);
			}
		}
		return userIds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.evaluation.logic.EvalResponsesLogic#getResponseById(java.lang.Long)
	 */
	public EvalResponse getResponseById(Long responseId) {
		log.debug("responseId: " + responseId);
		// get the response by passing in id
		return (EvalResponse) dao.findById(EvalResponse.class, responseId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.evaluation.logic.EvalResponsesLogic#getEvaluationResponses(java.lang.String,
	 *      java.lang.Long[])
	 */
	public List getEvaluationResponses(String userId, Long[] evaluationIds) {
		log.debug("userId: " + userId + ", evaluationIds: " + evaluationIds);

		if (evaluationIds.length <= 0) {
			throw new IllegalArgumentException("evaluationIds cannot be empty");
		}

		// check that the ids are actually valid
		int count = dao.countByProperties(EvalEvaluation.class, new String[] { "id" }, new Object[] { evaluationIds });
		if (count != evaluationIds.length) {
			throw new IllegalArgumentException("One or more invalid evaluation ids in evaluationIds: " + evaluationIds);
		}

		if (external.isUserAdmin(userId)) {
			// if user is admin then return all matching responses for this evaluation
			return dao.findByProperties(EvalResponse.class, new String[] { "evaluation.id" },
					new Object[] { evaluationIds }, new int[] { ByPropsFinder.EQUALS }, new String[] { "id" });
		} else {
			// not admin, only return the responses for this user
			return dao.findByProperties(EvalResponse.class, new String[] { "owner", "evaluation.id" }, new Object[] {
					userId, evaluationIds }, new int[] { ByPropsFinder.EQUALS, ByPropsFinder.EQUALS },
					new String[] { "id" });
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.evaluation.logic.EvalResponsesLogic#countResponses(java.lang.Long,
	 *      java.lang.String)
	 */
	public int countResponses(Long evaluationId, String evalGroupId) {
		log.debug("evaluationId: " + evaluationId + ", evalGroupId: " + evalGroupId);

		if (dao.countByProperties(EvalEvaluation.class, new String[] { "id" }, new Object[] { evaluationId }) <= 0) {
			throw new IllegalArgumentException("Could not find evaluation with id: " + evaluationId);
		}

		if (evalGroupId == null) {
			// returns count of all responses in all eval groups if evalGroupId
			// is null
			return dao.countByProperties(EvalResponse.class, new String[] { "evaluation.id" },
					new Object[] { evaluationId });
		} else {
			// returns count of responses in this evalGroupId only if set
			return dao.countByProperties(EvalResponse.class, new String[] { "evaluation.id", "evalGroupId" },
					new Object[] { evaluationId, evalGroupId });
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.evaluation.logic.EvalResponsesLogic#getEvalAnswers(java.lang.Long,
	 *      java.lang.Long)
	 */
	public List getEvalAnswers(Long itemId, Long evaluationId, String[] evalGroupIds) {
		log.debug("itemId: " + itemId + ", evaluationId: " + evaluationId);

		if (dao.countByProperties(EvalItem.class, new String[] { "id" }, new Object[] { itemId }) <= 0) {
			throw new IllegalArgumentException("Could not find item with id: " + itemId);
		}

		if (dao.countByProperties(EvalEvaluation.class, new String[] { "id" }, new Object[] { evaluationId }) <= 0) {
			throw new IllegalArgumentException("Could not find evaluation with id: " + evaluationId);
		}

		return dao.getAnswers(itemId, evaluationId, evalGroupIds);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.evaluation.logic.EvalResponsesLogic#getEvalResponseIds(java.lang.Long,
	 *      java.lang.String[])
	 */
	public List getEvalResponseIds(Long evaluationId, String[] evalGroupIds) {
		log.debug("evaluationId: " + evaluationId);

		if (dao.countByProperties(EvalEvaluation.class, new String[] { "id" }, new Object[] { evaluationId }) <= 0) {
			throw new IllegalArgumentException("Could not find evaluation with id: " + evaluationId);
		}

		return dao.getResponseIds(evaluationId, evalGroupIds);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.evaluation.logic.EvalResponsesLogic#saveResponse(org.sakaiproject.evaluation.model.EvalResponse,
	 *      java.lang.String)
	 */
	public void saveResponse(EvalResponse response, String userId) {
		log.debug("userId: " + userId + ", response: " + response.getId() + ", evalGroupId: " + response.getEvalGroupId());

		// set the date modified
		response.setLastModified(new Date());

		if (response.getId() != null) {
			// TODO - existing response, don't allow change to any setting
			// except starttime, endtime, and answers
		}

		// fill in any default values and nulls here

		// check perms and evaluation state
		if (checkUserModifyReponse(userId, response)) {
			// make sure the user can take this evalaution
			Long evaluationId = response.getEvaluation().getId();
			String context = response.getEvalGroupId();
			if (!evaluations.canTakeEvaluation(userId, evaluationId, context)) {
				throw new IllegalStateException("User (" + userId + ") cannot take this evaluation (" + evaluationId
						+ ") in this evalGroupId (" + context + ") right now");
			}

			// check to make sure answers are valid for this evaluation
			if (response.getAnswers() != null && !response.getAnswers().isEmpty()) {
				checkAnswersValidForEval(response);
			} else {
				response.setAnswers(new HashSet());
			}

			// save everything in one transaction
			Set[] entitySets = new Set[2];

			// response has to be saved first
			entitySets[0] = new HashSet();
			entitySets[0].add(response);
			entitySets[1] = response.getAnswers();

			dao.saveMixedSet(entitySets);

			if (response.getEndTime() != null) {
				// the response is complete so lock evaluation
				log.info("Locking evaluation (" + response.getEvaluation().getId() + ") and associated entities");
				dao.lockEvaluation(response.getEvaluation());
			}

			int answerCount = response.getAnswers() == null ? 0 : response.getAnswers().size();
			log.info("User (" + userId + ") saved response (" + response.getId() + "), evalGroupId ("
					+ response.getEvalGroupId() + ") and " + answerCount + " answers");
			return;
		}

		// should not get here so die if we do
		throw new RuntimeException("User (" + userId + ") could NOT save response (" + response.getId()
				+ "), evalGroupId: " + response.getEvalGroupId());
	}

	// PERMISSIONS

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.evaluation.logic.EvalResponsesLogic#canModifyResponse(java.lang.String,
	 *      java.lang.Long)
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
	 * 
	 * @param userId
	 * @param response
	 * @return true if they do, exception otherwise
	 */
	protected boolean checkUserModifyReponse(String userId, EvalResponse response) {
		log.debug("evalGroupId: " + response.getEvalGroupId() + ", userId: " + userId);

		String state = EvalUtils.getEvaluationState(response.getEvaluation());
		if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(state) || EvalConstants.EVALUATION_STATE_ACTIVE.equals(state)) {
			// admin CAN save responses -AZ
//			// check admin (admins can never save responses)
//			if (external.isUserAdmin(userId)) {
//				throw new IllegalArgumentException("Admin user (" + userId + ") cannot create response ("
//						+ response.getId() + "), admins can never save responses");
//			}

			// check ownership
			if (response.getOwner().equals(userId)) {
				return true;
			} else {
				throw new SecurityException("User (" + userId + ") cannot modify response (" + response.getId()
						+ ") without permissions");
			}
		} else {
			throw new IllegalStateException("Evaluation state (" + state + ") not valid for modifying responses");
		}
	}

	/**
	 * Checks the answers in the response for validity<br/>
	 * 
	 * @param response
	 * @return true if all answers valid, exception otherwise
	 */
	protected boolean checkAnswersValidForEval(EvalResponse response) {

		// get a list of the valid templateItems for this evaluation
		// maybe this should use a method elsewhere?
		Long templateId = response.getEvaluation().getTemplate().getId();
		List templateItems = dao.findByProperties(EvalTemplateItem.class, new String[] { "template.id" },
				new Object[] { templateId });
		Set templateItemIds = new HashSet();
		for (int i = 0; i < templateItems.size(); i++) {
			templateItemIds.add(((EvalTemplateItem) templateItems.get(i)).getId());
		}

		// check the answers
		for (Iterator iter = response.getAnswers().iterator(); iter.hasNext();) {
			EvalAnswer answer = (EvalAnswer) iter.next();
			if (answer.getNumeric() == null && answer.getText() == null) {
				throw new IllegalArgumentException("Cannot save blank answers: answer for templateItem: "
						+ answer.getTemplateItem().getId());
			}

			// verify the base state of new answers
			if (answer.getId() == null) {
				// force the item to be set correctly
				answer.setItem(answer.getTemplateItem().getItem());

				// check that the associated id is filled in for associated items
				if (EvalConstants.ITEM_CATEGORY_COURSE.equals(answer.getTemplateItem().getItemCategory())) {
					if (answer.getAssociatedId() != null) {
						log.warn("Course answers should have the associated id field blank, for templateItem: "
								+ answer.getTemplateItem().getId());
					}
					answer.setAssociatedId(null);
					answer.setAssociatedType(null);
				} else if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(answer.getTemplateItem().getItemCategory())) {
					if (answer.getAssociatedId() == null) {
						throw new IllegalArgumentException(
								"Instructor answers must have the associated field filled in with the instructor userId, for templateItem: "
										+ answer.getTemplateItem().getId());
					}
					answer.setAssociatedType(EvalConstants.ITEM_CATEGORY_INSTRUCTOR);
				} else if (EvalConstants.ITEM_CATEGORY_ENVIRONMENT.equals(answer.getTemplateItem().getItemCategory())) {
					if (answer.getAssociatedId() == null) {
						throw new IllegalArgumentException(
								"Environment answers must have the associated field filled in with the unique environment id, for templateItem: "
										+ answer.getTemplateItem().getId());
					}
					answer.setAssociatedType(EvalConstants.ITEM_CATEGORY_ENVIRONMENT);
				} else {
					throw new IllegalArgumentException("Do not know how to handle a templateItem category of ("
							+ answer.getTemplateItem().getItemCategory() + ") for templateItem: "
							+ answer.getTemplateItem().getId());
				}

				// make sure answer is associated with a valid templateItem for
				// this evaluation
				if (!templateItemIds.contains(answer.getTemplateItem().getId())) {
					throw new IllegalArgumentException("This answer templateItem (" + answer.getTemplateItem().getId()
							+ ") is not part of this evaluation (" + response.getEvaluation().getTitle() + ")");
				}
			}

			// TODO - check if numerical answers are valid?
		}

		return true;
	}

}
