/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.evaluation.logic.impl.scheduling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.externals.EvalTransition;
import org.sakaiproject.evaluation.model.EvalEvaluation;

/**
 * Work to be done at EvalEvalation transition points.
 * 
 * @author rwellis
 *
 */
public class EvalTransitionImpl implements EvalTransition {
	
	private static Log log = LogFactory.getLog(EvalTransitionImpl.class);
	
	private EvalEvaluation eval;
	
	private EvalEmailsLogic emails;
	public void setEmails(EvalEmailsLogic emails) {
		this.emails = emails;
	}
	private EvalEvaluationsLogic evalEvaluationsLogic;
	public void setEvalEvaluationsLogic(EvalEvaluationsLogic evalEvaluationsLogic) {
		this.evalEvaluationsLogic = evalEvaluationsLogic;
	}

	public void init() {
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalTransition#fixState(java.lang.Long)
	 */
	public void fixState(Long evalId) {
		log.info("fixState, eval id " + evalId);
		eval = evalEvaluationsLogic.getEvaluationById(evalId);
		evalEvaluationsLogic.saveEvaluation(eval, eval.getOwner());
	}


	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalTransition#sendActive(java.lang.Long)
	 */
	public void sendActive(Long evalId) {
		log.info("sendActive, eval id " + evalId);
		boolean includeEvaluatees = true;
		String[] sentMessages = emails.sendEvalAvailableNotifications(evalId, includeEvaluatees);
	}


	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalTransition#sendCreated(java.lang.Long)
	 */
	public void sendCreated(Long evalId) {
		log.info("sendCreated, " + evalId);
		boolean includeOwner = true;
		String[] sentMessages = emails.sendEvalCreatedNotifications(evalId, includeOwner);
	}


	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalTransition#sendReminder(java.lang.Long)
	 */
	public void sendReminder(Long evalId) {
		log.info("sendReminder, " + evalId);
		String includeConstant = null;
		String[] sentMessages = emails.sendEvalReminderNotifications(evalId, includeConstant);
	}


	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalTransition#sendViewable(java.lang.Long)
	 */
	public void sendViewable(Long evalId) {
		log.info("sendViewable, " + evalId);
		boolean includeEvaluatees = true;
		boolean includeAdmins = true;
		String[] sentMessages = emails.sendEvalResultsNotifications(evalId, includeEvaluatees, includeAdmins);
	}
}
