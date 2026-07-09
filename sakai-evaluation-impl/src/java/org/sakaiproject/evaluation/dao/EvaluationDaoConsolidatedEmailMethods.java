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
package org.sakaiproject.evaluation.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.DateType;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.springframework.dao.DataAccessResourceFailureException;

import lombok.extern.slf4j.Slf4j;

/**
 * Hibernate-backed implementation methods for the matching evaluation DAO port.
 */
@Slf4j
abstract class EvaluationDaoConsolidatedEmailMethods extends EvaluationDaoLockMethods {

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.dao.EvaluationDao#countDistinctGroupsInConsolidatedEmailMapping()
     */
    public int countDistinctGroupsInConsolidatedEmailMapping() {
        Long count = currentSession().createQuery(
                "select count(distinct groupId) from EvalEmailProcessingData",
                Long.class)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }


	public List<Map<String,Object>>  getConsolidatedEmailMapping(boolean sendingAvailableEmails, int pageSize, int page) {
    	String query1 = "select userId,emailTemplateId,min(evalDueDate) from EvalEmailProcessingData group by emailTemplateId,userId order by emailTemplateId,userId";
    	
    	if(log.isDebugEnabled()) {
    		log.debug("getConsolidatedEmailMapping(" + sendingAvailableEmails + ", " + pageSize + ", " + page + ")");
    	}
    	
    	List<Map<String,Object>> rv = new ArrayList<>();
    	
    	Session session = currentSession();
    	
        Query<Object[]> query = session.createQuery(query1, Object[].class);
        query.setFirstResult(pageSize * page);
        query.setMaxResults(pageSize);
        
    	List<String> userIdList = new ArrayList<>();
    	Long previousTemplateId = null;
    	Long templateId = null;
    	
        List<Object[]> results = query.list();

        if(results != null) {
        	log.info("found items from email-processing-queue: " + results.size());

            for(int i = 0; i < results.size(); i++) {
                Object[] row = results.get(i);
                String userId = (String) row[0];
                templateId = (Long) row[1];
                Date earliestDueDate = (Date)row[2];
                if(userId == null || templateId == null) {
                    continue;
                }
                if(previousTemplateId == null ) {
                    previousTemplateId = templateId;
                }

                Map<String,Object> map = new HashMap<>();

                map.put(EvalConstants.KEY_USER_ID, userId);
                map.put(EvalConstants.KEY_EMAIL_TEMPLATE_ID,templateId);
                map.put(EvalConstants.KEY_EARLIEST_DUE_DATE,earliestDueDate);
                rv.add(map);
                log.info("added email-processing entry for user: " + userId + " templateId: " + templateId);
                if(templateId.longValue() != previousTemplateId.longValue() || userIdList.size() > MAX_UPDATE_SIZE) {
                    // mark eval_assign_user records as sent 
                    markRecordsAsSent(session, sendingAvailableEmails, templateId,
                            userIdList);
                }
                userIdList.add(userId);
                //updates.add((Long) row[0]);
            }
        }

		if(templateId == null || userIdList.isEmpty() ) {
			log.info("Can't mark EvalAssignUser records due to null values: userId == " + userIdList + "   templateId == " + templateId);
    	} else {
			// mark eval_assign_user records as sent 
	    	markRecordsAsSent(session, sendingAvailableEmails, templateId,
					userIdList);

    	}
        
    	return rv;
    }

	/**
	 * 
	 * @param session
	 * @param sendingAvailableEmails
	 * @param templateId
	 * @param userIdList
	 */
	protected void markRecordsAsSent(Session session,
			boolean sendingAvailableEmails, Long templateId,
			List<String> userIdList) {
		
		
		StringBuilder hqlBuffer = new StringBuilder();
		
		hqlBuffer.append("update EvalAssignUser ");
		if(sendingAvailableEmails) {
			hqlBuffer.append("set availableEmailSent = :dateSent ");
		} else {
			hqlBuffer.append("set reminderEmailSent = :dateSent ");
		}
		hqlBuffer.append("where id in (select eauId from EvalEmailProcessingData where emailTemplateId = :emailTemplateId and userId = :userId)");
		
		Query<?> updateQuery = session.createQuery(hqlBuffer.toString());
		
		updateQuery.setParameter("dateSent", new Date());
		updateQuery.setParameter("emailTemplateId", templateId);
		
		for(String userId : userIdList) {
			try {
				
				updateQuery.setParameter("userId", userId);
				updateQuery.executeUpdate();
				
			} catch (HibernateException e) {
				log.warn("Error trying to update evalAssignUser. " + userId, e);
			}
		}
		if(log.isDebugEnabled()) {
			log.debug("         --> marked entries for users: " + userIdList);
		}
		session.flush();
		userIdList.clear();
	}

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.dao.EvaluationDao#resetConsolidatedEmailRecipients()
     */
	public int resetConsolidatedEmailRecipients() {
		String deleteHql = "delete from EvalEmailProcessingData";
		Query<?> query = currentSession().createQuery(deleteHql);
		return query.executeUpdate();
	}
	
    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.dao.EvaluationDao#selectConsolidatedEmailRecipients(boolean, java.util.Date, boolean, java.util.Date, java.lang.String)
     */
	public int selectConsolidatedEmailRecipients(boolean useAvailableEmailSent, Date availableEmailSent, boolean useReminderEmailSent, Date reminderEmailSent, String emailTemplateType) {
		int count = 0;
		try {
    	StringBuilder queryBuf = new StringBuilder();
    	Map<String,Object> params = new HashMap<>();
    	
	    	queryBuf.append("insert into EvalEmailProcessingData (eauId,userId,groupId,emailTemplateId,evalId,evalDueDate) ");
	    	queryBuf.append("select user.id as eauId,user.userId as userId,user.evalGroupId as groupId, ");
    	if(EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE.equalsIgnoreCase(emailTemplateType)) {
    		queryBuf.append("eval.availableEmailTemplate.id as emailTemplateId");
    	} else if(EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER.equalsIgnoreCase(emailTemplateType)) {
    		queryBuf.append("eval.reminderEmailTemplate.id as emailTemplateId");
    	} else {
    		queryBuf.append("'' as emailTemplateId");
    	}
	    	queryBuf.append(",eval.id as evalId, eval.dueDate as evalDueDate ");
	    	queryBuf.append("from EvalAssignUser as user ");
		queryBuf.append("inner join user.evaluation as eval ");
			queryBuf.append("where user.type = :userType and eval.startDate <= current_timestamp() and user.completedDate is null ");
		params.put("userType", EvalAssignUser.TYPE_EVALUATOR);
    	if(EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE.equalsIgnoreCase(emailTemplateType)) {
    		queryBuf.append("and eval.availableEmailTemplate.type = :emailTemplateType ");
    		params.put("emailTemplateType", emailTemplateType);
    		
    	} else if(EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER.equalsIgnoreCase(emailTemplateType)) {
    		queryBuf.append("and eval.reminderEmailTemplate.type = :emailTemplateType ");
    		params.put("emailTemplateType", emailTemplateType);
    	} 
    	
    	if(useAvailableEmailSent) {
    		if(availableEmailSent == null) {
    			queryBuf.append("and user.availableEmailSent is null ");
    		} else {
    			queryBuf.append("and (user.availableEmailSent is null or user.availableEmailSent < :availableEmailSent) ");
    			params.put("availableEmailSent", availableEmailSent);
    		}
    	}
   	
    	if(useReminderEmailSent) {
    		if(reminderEmailSent == null) {
    			queryBuf.append("and user.reminderEmailSent is null ");
    		} else {
    			queryBuf.append("and (user.reminderEmailSent is null or user.reminderEmailSent < :reminderEmailSent) ");
    			params.put("reminderEmailSent", reminderEmailSent);
    		}
    	}
		
        Query<?> query = currentSession().createQuery(queryBuf.toString());
    	
    	for(Map.Entry<String,Object> entry : params.entrySet()) {
    		if(entry.getValue() instanceof Date) {
    			query.setParameter(entry.getKey(), (Date) entry.getValue(), DateType.INSTANCE);
    		} else if(entry.getValue() instanceof String) {
    			query.setParameter(entry.getKey(), (String) entry.getValue());
    		}
    	}
    	
	    	count = query.executeUpdate();
    	log.debug("Rows inserted into EVAL_EMAIL_PROCESSING_QUEUE: " + count);
		} catch(DataAccessResourceFailureException | IllegalStateException | HibernateException e) {
			log.warn("error processing consolidated-email query: " + e);
		}
		
    	return count;
	}

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.dao.EvaluationDao#getAllSiteIDsMatchingSectionTitle(java.lang.String)
     */
    public Set<String> getAllSiteIDsMatchingSectionTitle( String sectionTitleWithWildcards )
    {
        try (Session session = getSessionFactory().openSession()) {
            NativeQuery<String> query = session.createNativeQuery(SQL_SELECT_SITE_IDS_MATCHING_SECTION_TITLE, String.class);
            query.setParameter("title", sectionTitleWithWildcards);
            return new HashSet<>(query.list());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.dao.EvaluationDao#getAllSiteIDsMatchingSiteTitle(java.lang.String)
     */
    public Set<String> getAllSiteIDsMatchingSiteTitle( String siteTitleWithWildcards )
    {
        try (Session session = getSessionFactory().openSession()) {
            NativeQuery<String> query = session.createNativeQuery(SQL_SELECT_SITE_IDS_MATCHING_SITE_TITLE, String.class);
            query.setParameter("title", siteTitleWithWildcards);
            return new HashSet<>(query.list());
        }
    }
}
