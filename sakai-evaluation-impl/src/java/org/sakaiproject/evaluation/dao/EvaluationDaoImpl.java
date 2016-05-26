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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalLock;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.ArrayUtils;
import org.sakaiproject.evaluation.utils.ComparatorsUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.genericdao.hibernate.HibernateGeneralGenericDao;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * This is the more specific Evaluation data access interface,
 * it should contain specific DAO methods, the generic ones
 * are included from the GenericDao already<br/>
 * This is now isolated to the logic layer only to ensure there is no access from the outside<br/>
 * <br/>
 * <b>LOCKING methods note:</b><br/>
 * The locking logic is designed to make it easier to know if an entity should or should not be changed or removed<br/> 
 * Locked entities can never be removed via the APIs and should not be removed with direct access to the DB<br/>
 * Locking handled as indicated here:<br/>
 * http://bugs.sakaiproject.org/confluence/display/EVALSYS/Evaluation+Implementation<br/>
 * <br/>
 * This is a BOTTOM level service and should depend on no other services
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationDaoImpl extends HibernateGeneralGenericDao implements EvaluationDao {

    private static final Log LOG = LogFactory.getLog(EvaluationDaoImpl.class);

    protected static final int MAX_UPDATE_SIZE = 999;

    private static final String SQL_SELECT_SITE_IDS_MATCHING_SECTION_TITLE = "SELECT DISTINCT realm.realm_id "
            + "FROM SAKAI_REALM realm "
            + "JOIN SAKAI_REALM_PROVIDER provider "
            + "ON realm.realm_key = provider.realm_key "
            + "JOIN CM_MEMBER_CONTAINER_T section "
            + "ON provider.provider_id = section.enterprise_id "
            + "WHERE section.class_discr = 'org.sakaiproject.coursemanagement.impl.SectionCmImpl' "
            + "AND section.title LIKE :title "
            + "AND realm.realm_id NOT LIKE '%/group/%'";
    private static final String SQL_SELECT_SITE_IDS_MATCHING_SITE_TITLE = "SELECT site_id FROM SAKAI_SITE WHERE title LIKE :title";

    public void init() {
        LOG.debug("init");
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.dao.EvaluationDao#forceCommit()
     */
    public void forceCommit() {
        getHibernateTemplate().flush(); // this should sync the data immediately
        // do a commit using the current transaction or make a new one
        if (getSession().getTransaction() != null) {
            getSession().getTransaction().commit();
            getSession().beginTransaction(); // start a new one
        } else {
            // establish a transaction and then force the commit
            getSession().beginTransaction().commit();
        }
        // should probably use the org.springframework.transaction.PlatformTransactionManager
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.dao.EvaluationDao#forceRollback()
     */
    public void forceRollback() {
        getHibernateTemplate().clear(); // clear pending data
        // do a rollback using the current transaction or make a new one
        if (getSession().getTransaction() != null) {
            getSession().getTransaction().rollback();
            getSession().beginTransaction(); // start a new one
        } else {
            // establish a transaction and then force the rollback
            getSession().beginTransaction().rollback();
        }
    }

    /**
     * This really does not work for most cases so be very careful with it
     * @param object
     */
    protected void forceEvict(Serializable object) {
        boolean active = false;
        try {
            Session session = getSession();
            if (session.isOpen() && session.isConnected()) {
                if (session.contains(object)) {
                    active = true;
                    session.evict(object);
                }
            } else {
                LOG.warn("Session is not open OR not connected, cannot evict objects");
            }
            if (!active) {
                LOG.info("Unable to evict object ("+object.getClass().getName()+") from session, it is not persistent: "+object);
            }
        } catch (DataAccessResourceFailureException | IllegalStateException | HibernateException e) {
            LOG.warn("Failure while attempting to evict object ("+object.getClass().getName()+") from session", e);
        }
    }

    public void fixupDatabase() {
        // fix up some of the null fields
        long count;
        count = countBySearch(EvalEvaluation.class, new Search("studentViewResults","", Restriction.NULL) );
        if (count > 0) {
            int counter = 0;
            counter += getHibernateTemplate().bulkUpdate("update EvalEvaluation eval set eval.studentViewResults = false where eval.studentsDate is null");
            counter += getHibernateTemplate().bulkUpdate("update EvalEvaluation eval set eval.studentViewResults = true where eval.studentsDate is not null");
            LOG.info("Updated " + counter + " EvalEvaluation.studentViewResults fields from null to boolean values based on studentsDate values");
        }
        count = countBySearch(EvalEvaluation.class, new Search("instructorViewResults","", Restriction.NULL) );
        if (count > 0) {
            int counter = 0;
            counter += getHibernateTemplate().bulkUpdate("update EvalEvaluation eval set eval.instructorViewResults = false where eval.instructorsDate is null");
            counter += getHibernateTemplate().bulkUpdate("update EvalEvaluation eval set eval.instructorViewResults = true where eval.instructorsDate is not null");
            LOG.info("Updated " + counter + " EvalEvaluation.instructorViewResults fields from null to boolean values based on instructorsDate values");
        }
        count = countBySearch(EvalEvaluation.class, new Search("modifyResponsesAllowed","", Restriction.NULL) );
        if (count > 0) {
            int counter = 0;
            counter += getHibernateTemplate().bulkUpdate("update EvalEvaluation eval set eval.modifyResponsesAllowed = false where eval.modifyResponsesAllowed is null");
            LOG.info("Updated " + counter + " EvalEvaluation.modifyResponsesAllowed fields from null to default");
        }
        count = countBySearch(EvalEvaluation.class, new Search("blankResponsesAllowed","", Restriction.NULL) );
        if (count > 0) {
            int counter = 0;
            counter += getHibernateTemplate().bulkUpdate("update EvalEvaluation eval set eval.blankResponsesAllowed = false where eval.blankResponsesAllowed is null");
            LOG.info("Updated " + counter + " EvalEvaluation.blankResponsesAllowed fields from null to default");
        }
    }

    /**
     * Get the list of all participants for an evaluation,
     * can limit it to a single group which is assigned to the evaluation and
     * can filter the results to only include some of the participants,
     * this should be used in all cases where  <br/>
     * Will not include any assignments with {@link EvalAssignUser#STATUS_REMOVED}
     * <br/>
     * You must include at least one of the following (non-null):
     * evaluationId OR userId
     * <br/> Uses the current user for permissions checks
     * 
     * @param evaluationId (OPTIONAL) the unique id of an {@link EvalEvaluation} object,
     * if this is null then assignments from any evaluation are returned
     * @param userId (OPTIONAL) limit the returned assignments to those for this user,
     * will return assignments for any user if this is null
     * @param evalGroupIds (OPTIONAL) an array of unique IDs for eval groups, 
     * if this is null or empty then results include participants from the entire evaluation,
     * NOTE: these ids are not validated
     * @param assignTypeConstant (OPTIONAL) a constant to indicate which types of assignment participants to include,
     * use the TYPE_* constants from {@link EvalAssignUser}, default (null) is to include all types of assignments
     * @param assignStatusConstant (OPTIONAL) a constant to indicate which status of assignment participants to include,
     * use the STATUS_* constants from {@link EvalAssignUser}, to include users with any status use {@link #STATUS_ANY}, 
     * default (null) is to include {@link EvalAssignUser#STATUS_LINKED} and {@link EvalAssignUser#STATUS_UNLINKED},
     * @param includeConstant (OPTIONAL) a constant to indicate what users should be retrieved, 
     * EVAL_INCLUDE_* from {@link EvalConstants}, default (null) is {@link EvalConstants#EVAL_INCLUDE_ALL},
     * <b>NOTE</b>: if this is non-null it will filter users to type {@link EvalAssignUser#TYPE_EVALUATOR} automatically
     * regardless of what the assignTypeConstant is set to
     * @param evalStateConstant (OPTIONAL) this is the state of the evals to limit the results to,
     * this should be one of the EVALUATION_STATE_* constants (e.g. {@link EvalConstants#EVALUATION_STATE_ACTIVE}),
     * if null then evaluations with any state are included
     * @return the list of user assignments ({@link EvalAssignUser} objects)
     * @throws IllegalArgumentException if all inputs are null or the inputs are invalid
     */
    @SuppressWarnings("unchecked")
    public List<EvalAssignUser> getParticipantsForEval(Long evaluationId, String userId,
            String[] evalGroupIds, String assignTypeConstant, String assignStatusConstant, 
            String includeConstant, String evalStateConstant) {
        // validate arguments
        if (evaluationId == null && (userId == null || "".equals(userId)) ) {
            throw new IllegalArgumentException("At least one of the following must be set: evaluationId, userId");
        }

        Map<String, Object> params = new HashMap<>();
        String joinHQL = "";

        String evalHQL = "";
        if (evaluationId != null) {
            params.put("evalId", evaluationId);
            evalHQL = " and eau.evaluation.id = :evalId";
        }
        String evalStateHQL = "";
        if (evalStateConstant != null) {
            EvalUtils.validateStateConstant(evalStateConstant);
            params.put("evalStateConstant", evalStateConstant);
            evalStateHQL = " and eval.state = :evalStateConstant";
            joinHQL = " join eau.evaluation eval";
        }
        String groupsHQL = "";
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            params.put("evalGroupIds", evalGroupIds);
            groupsHQL = " and eau.evalGroupId in (:evalGroupIds)";
        }
        String assignTypeHQL = "";
        if (assignTypeConstant != null 
                && includeConstant == null) {
            // only set this if the includeConstant is not set
            EvalAssignUser.validateType(assignTypeConstant);
            params.put("assignType", assignTypeConstant);
            assignTypeHQL = " and eau.type = :assignType";
        }
        String assignStatusHQL = "";
        if (assignStatusConstant == null) {
            params.put("assignStatus", EvalAssignUser.STATUS_REMOVED);
            assignStatusHQL = " and eau.status <> :assignStatus";
        } else if (EvalEvaluationService.STATUS_ANY.equals(assignStatusConstant)) {
            // no restriction needed in this case
        } else {
            EvalAssignUser.validateStatus(assignStatusConstant);
            params.put("assignStatus", assignStatusConstant);
            assignStatusHQL = " and eau.status = :assignStatus";
        }
        String userHQL = "";
        if (userId != null && ! "".equals(userId)) {
            params.put("userId", userId);
            userHQL = " and eau.userId = :userId";
        }
        boolean includeFilterUsers = false;
        Set<String> userFilter = null;
        if (includeConstant != null) {
            EvalUtils.validateEmailIncludeConstant(includeConstant);
            String[] groupIds = new String[] {};
            if (evalGroupIds != null && evalGroupIds.length > 0) {
                groupIds = evalGroupIds;
            }
            // force the results to only include eval takers
            params.put("assignType", EvalAssignUser.TYPE_EVALUATOR);
            assignTypeHQL = " and eau.type = :assignType";
            // now set up the filter
            if (EvalConstants.EVAL_INCLUDE_NONTAKERS.equals(includeConstant)) {
                // get all users who have responded either way
                userFilter = getResponseUserIds(evaluationId, groupIds, null); // exclude
                includeFilterUsers = false; // INVERT the search
            } else if (EvalConstants.EVAL_INCLUDE_RESPONDENTS.equals(includeConstant)) {
                // get all users who have responded
                userFilter = getResponseUserIds(evaluationId, groupIds, true);
                includeFilterUsers = true;
            } else if (EvalConstants.EVAL_INCLUDE_IN_PROGRESS.equals(includeConstant)) {
                // get all users who have saved
                userFilter = getResponseUserIds(evaluationId, groupIds, false);
                includeFilterUsers = true;
            } else if (EvalConstants.EVAL_INCLUDE_ALL.equals(includeConstant)) {
                // do nothing
            } else {
                throw new IllegalArgumentException("Unknown includeConstant: " + includeConstant);
            }
        }

        // get the assignments based on the search/HQL
        String hql = "select eau from EvalAssignUser eau "+joinHQL+" where 1=1 "+evalHQL+userHQL+evalStateHQL+assignStatusHQL+assignTypeHQL+groupsHQL
        +" order by eau.id";
        List<EvalAssignUser> results = (List<EvalAssignUser>) executeHqlQuery(hql, params, 0, 0);
        List<EvalAssignUser> assignments = new ArrayList<>( results );

        // This code is potentially expensive but there is not really a better way to handle it -AZ
        if (userFilter != null) {
            if (userFilter.isEmpty()) {
                // employ shortcuts when the filter set is empty
                if (includeFilterUsers) {
                    // no one to include so just wipe the set
                    assignments.clear();
                } else {
                    // no one to exclude to just return the complete set
                }
            } else {
                // filter the results based on the userFilter
                for (Iterator<EvalAssignUser> iterator = assignments.iterator(); iterator.hasNext();) {
                    EvalAssignUser evalAssignUser = iterator.next();
                    String uid = evalAssignUser.getUserId();
                    if (includeFilterUsers) {
                        // only include users in the filter
                        if (! userFilter.contains(uid)) {
                            iterator.remove();
                        }
                    } else {
                        // exclude all users in the filter
                        if (userFilter.contains(uid)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }

        return assignments;
    }

    /*  SELECT * FROM eval_evaluation as EVAL
        LEFT join eval_assign_user as AU on EVAL.ID = AU.EVALUATION_FK 
        WHERE AU.ID IS NULL
     */
    @SuppressWarnings("unchecked")
    public List<EvalEvaluation> getEvalsWithoutUserAssignments() {
        String hql = "select eval from EvalAssignUser eau right join eau.evaluation eval where eau.id is null";
        List<EvalEvaluation> evals = (List<EvalEvaluation>) executeHqlQuery(hql, new Object[] {}, 0, 0);
        return evals;
    }

    /**
     * Construct the HQL to do the sharing query based on sharing constants and userId
     * @return the HQL query string
     */
    private <T> String buildSharingHQL(String className, String userId,
            String[] sharingConstants, String[] props, Object[] values, int[] comparisons,
            String[] order, String[] options) {
        if (sharingConstants == null || sharingConstants.length == 0) {
            throw new IllegalArgumentException("No sharing constants specified, you must specify at least one");
        }

        StringBuilder query = new StringBuilder();
        query.append("from ");
        query.append(className);
        query.append(" as entity where 1=1 ");

        if (sharingConstants.length > 0) {
            query.append(" and (");
            for (int i = 0; i < sharingConstants.length; i++) {
                if (i > 0) {
                    query.append(" or ");
                }
                // check if we include private (owner equivalent)
                if (EvalConstants.SHARING_PRIVATE.equals(sharingConstants[i])
                        || EvalConstants.SHARING_OWNER.equals(sharingConstants[i]) ) {
                    if (userId == null) {
                        query.append(" entity.sharing = '");
                        query.append(EvalConstants.SHARING_PRIVATE);
                        query.append("' ");
                    } else {
                        query.append(" (entity.sharing = '");
                        query.append(EvalConstants.SHARING_PRIVATE);
                        query.append("' and entity.owner = '");
                        query.append(userId);
                        query.append("') ");
                    }
                } else if (EvalConstants.SHARING_PUBLIC.equals(sharingConstants[i])) {
                    query.append(" entity.sharing = '");
                    query.append(EvalConstants.SHARING_PUBLIC);
                    query.append("' ");
                } else {
                    query.append(" entity.sharing = '");
                    query.append(sharingConstants[i]);
                    query.append("' ");               
                }
            }
            query.append(") ");
        }

        // add in the optional prop/value comparisons
        if (props != null && props.length > 0
                && values != null && values.length > 0
                && comparisons != null && comparisons.length > 0) {
            if (props.length != values.length 
                    && values.length != comparisons.length) {
                throw new IllegalArgumentException("Invalid array sizes: props ("+props.length+"), values("
                        +values.length+"), and comparisons("+comparisons.length+") must match");
            }
            for (int i = 0; i < props.length; i++) {
                query.append(" and entity.");
                query.append( makeComparisonHQL(props[i], comparisons[i], values[i]) );
                query.append(" ");
            }
        }

        // handle special options
        if (options != null && options.length > 0) {
            for( String option : options )
            {
                if( "notHidden".equals( option ) )
                {
                    query.append(" and entity.hidden = false ");
                }
                else if( "notEmpty".equals( option ) )
                {
                    query.append(" and entity.templateItems.size > 0 ");
                }
            }
        }

        // add ordering to returned values
        if (order != null && order.length > 0) {
            for (int i = 0; i < order.length; i++) {
                if (i == 0) {
                    query.append(" order by entity.");
                    query.append(order[i]);
                } else {
                    query.append(", entity.");
                    query.append(order[i]);
                }
            }
        }

        return query.toString();
    }

    /**
     * A general method for counting entities which are shared for a specific user,
     * this is abstracting the idea of ((private & owner) or (public)) and (other options)
     * 
     * @param <T>
     * @param entityClass the class of the entity to be retrieved
     * @param userId the internal user Id (of the owner),
     * null userId means return all private templates,
     * has no effect if private constant is not included in the sharingConstants list
     * @param sharingConstants an array of SHARING_ constants from {@link EvalConstants},
     * this cannot be null or empty
     * @param props an array of extra properties to compare to values
     * @param values an array of extra values
     * @param comparisons an array of extra comparisons
     * @param options extra options which are specially handled: 
     * notHidden for scales/items/TIs/templates,
     * notEmpty for templates
     * @return a count of the matching entities
     * @see #getSharedEntitiesForUser(Class, String, String[], String[], Object[], int[], String[], String[])
     */
    public <T> int countSharedEntitiesForUser(Class<T> entityClass, String userId,
            String[] sharingConstants, String[] props, Object[] values, int[] comparisons,
            String[] options) {

        String hql = buildSharingHQL(entityClass.getName(), userId, sharingConstants, 
                props, values, comparisons, null, options);
        LOG.debug("countSharedEntitiesForUser: HQL=" + hql);
        int count = count(hql);
        return count;
    }

    /**
     * A general method for fetching entities which are shared for a specific user,
     * this is abstracting the idea of ((private & owner) or (public)) and (other options)
     * 
     * @param <T>
     * @param entityClass the class of the entity to be retrieved
     * @param userId the internal user Id (of the owner),
     * null userId means return all private templates,
     * has no effect if private constant is not included in the sharingConstants list
     * @param sharingConstants an array of SHARING_ constants from {@link EvalConstants},
     * this cannot be null or empty
     * @param props an array of extra properties to compare to values
     * @param values an array of extra values
     * @param comparisons an array of extra comparisons
     * @param order a string array of property names to order by
     * @param options extra options which are specially handled: 
     * notHidden for scales/items/TIs/templates,
     * notEmpty for templates
     * @param start the returned entity to start with (for paging), 0 means start with the first one
     * @param limit the total number of entities to return, 0 means return all
     * @return a list of entities
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getSharedEntitiesForUser(Class<T> entityClass, String userId,
            String[] sharingConstants, String[] props, Object[] values, int[] comparisons,
            String[] order, String[] options, int start, int limit) {

        String hql = buildSharingHQL(entityClass.getName(), userId, sharingConstants, 
                props, values, comparisons, order, options);
        LOG.debug("getSharedEntitiesForUser: HQL=" + hql);
        Map<String, Object> params = new HashMap<>();
        List<T> l = (List<T>) executeHqlQuery(hql, params, start, limit);
        return l;
    }



    /**
     * Returns all evaluation objects associated with the input groups,
     * can also include anonymous evaluations and filter on a number
     * of options (fills in the optional assign groups)
     * 
     * @param evalGroupIds an array of eval group IDs to get associated evals for, 
     * can be empty or null but only anonymous evals will be returned
     * @param activeOnly if true, only include active (and grace period) evaluations, 
     * if false only include inactive (inqueue, graceperiod, closed, viewable), 
     * if null, include all evaluations (except partial and deleted)
     * @param approvedOnly if true, include the evaluations for groups which have been instructor approved only,
     * if false, include evals for groups which have not been approved only,
     * if null, include approved and unapproved,
     * NOTE: you should not include unapproved when displaying evaluations to users to take or sending emails
     * @param includeAnonymous if true, only include evaluations authcontrol = anon, 
     * if false, include any evaluations with authcontrol != anon,
     * if null, include all evaluations regardless of authcontrol
     * @return a List of EvalEvaluation objects sorted by due date, title, and id
     */
    @SuppressWarnings("unchecked")
    public List<EvalEvaluation> getEvaluationsByEvalGroups(String[] evalGroupIds, Boolean activeOnly,
            Boolean approvedOnly, Boolean includeAnonymous, int startResult, int maxResults) {

        HashMap<Long, List<EvalAssignGroup>> evalToAGList = new HashMap<>();

        boolean emptyReturn = false;
        Map<String, Object> params = new HashMap<>();

        String groupsHQL = "";
        if (evalGroupIds != null && evalGroupIds.length > 0) {

            Search search = new Search("evalGroupId", evalGroupIds);
            if (approvedOnly != null) {
                search.addRestriction( new Restriction("instructorApproval", approvedOnly) );
            }
            List<EvalAssignGroup> eags = findBySearch(EvalAssignGroup.class, search);
            for (EvalAssignGroup evalAssignGroup : eags) {
                Long evalId = evalAssignGroup.getEvaluation().getId();
                if (! evalToAGList.containsKey(evalId)) {
                    List<EvalAssignGroup> l = new ArrayList<>();
                    evalToAGList.put(evalId, l);
                }
                evalToAGList.get(evalId).add(evalAssignGroup);
            }

            if (eags.isEmpty()) {
                groupsHQL = " and (eval.id = -1) "; // this will never match, that's the point
            } else {
                String anonymousHQL = "";
                if (includeAnonymous != null) {
                    params.put("authControl", EvalConstants.EVALUATION_AUTHCONTROL_NONE);
                    if (includeAnonymous) {
                        anonymousHQL += "or eval.authControl = :authControl";
                    } else {
                        anonymousHQL += "and eval.authControl <> :authControl";            
                    }
                }
    
                groupsHQL = " and (eval.id in (:evalIds)" + anonymousHQL + ")";
                Set<Long> s = evalToAGList.keySet();
                params.put("evalIds", s.toArray(new Long[s.size()]));
            }
        } else {
            // no groups but we want to get anonymous evals
            if (includeAnonymous != null
                    && includeAnonymous) {
                params.put("authControl", EvalConstants.EVALUATION_AUTHCONTROL_NONE);
                groupsHQL = " and eval.authControl = :authControl ";
            } else {
                // no groups and no anonymous evals means nothing to return
                emptyReturn = true;
            }
        }

        List<EvalEvaluation> evals;
        if (emptyReturn) {
            evals = new ArrayList<>();
        } else {
            // there should be some evaluations

            // giving up on this join for now, it would have to take groups into account as well
            //         String responsesHQL = "";
            //         if (untakenOnly != null) {
            //            if (EvalUtils.isBlank(userId)) {
            //               throw new IllegalArgumentException("userId cannot be null if untakenOnly is not null");
            //            }
            //            joinHQL += ", EvalResponse as resp ";
            //            responsesHQL = " and resp.evaluation.id = eval.id and (resp.owner = :userId and resp.endTime is ";
            //            if (untakenOnly) {
            //               responsesHQL += "null) ";
            //            } else {
            //               responsesHQL += "not null) ";
            //            }
            //            params.put("userId", userId);
            //         }

            String activeHQL;
            if (activeOnly != null) {
                if (activeOnly) {
                	activeHQL = " and ( eval.state = :activeState or eval.state = :graceState) ";
                    params.put("activeState", EvalConstants.EVALUATION_STATE_ACTIVE);
                    params.put("graceState", EvalConstants.EVALUATION_STATE_GRACEPERIOD);
                } else {
                    activeHQL = " and (eval.state = :queueState or eval.state = :graceState or eval.state = :closedState or eval.state = :viewState) ";
                    params.put("queueState", EvalConstants.EVALUATION_STATE_INQUEUE);
                    params.put("graceState", EvalConstants.EVALUATION_STATE_GRACEPERIOD);
                    params.put("closedState", EvalConstants.EVALUATION_STATE_CLOSED);
                    params.put("viewState", EvalConstants.EVALUATION_STATE_VIEWABLE);
                }
            } else {
                // need to filter out the partial and deleted state evals
                activeHQL = " and eval.state <> :partialState and eval.state <> :deletedState ";
                params.put("partialState", EvalConstants.EVALUATION_STATE_PARTIAL);
                params.put("deletedState", EvalConstants.EVALUATION_STATE_DELETED);
            }

            String hql = "select eval from EvalEvaluation as eval " 
                + " where 1=1 " + activeHQL + groupsHQL //+ responsesHQL 
                + " order by eval.dueDate, eval.title, eval.id";
            evals = (List<EvalEvaluation>) executeHqlQuery(hql, params, startResult, maxResults);
            Collections.sort(evals, new ComparatorsUtils.EvaluationDateTitleIdComparator());
        }
        // add in the filtered assign groups which we retrieved earlier
        for (EvalEvaluation eval : evals) {
            Long evalId = eval.getId();
            List<EvalAssignGroup> l = evalToAGList.get(evalId);
            if (l == null) {
                l = new ArrayList<>(0);
            }
            eval.setEvalAssignGroups(l);
        }
        return evals;

    }


    /**
     * Returns all evaluations which the given user can take,
     * can also include anonymous evaluations and filter on active/approved
     * 
     * @param userId the internal user id for the user who we are checking evals they can take
     * @param activeOnly if true, only include active evaluations, 
     * if false only include inactive (inqueue, graceperiod, closed, viewable), 
     * if null, include all evaluations (except partial and deleted)
     * @param approvedOnly if true, include the evaluations for groups which have been instructor approved only,
     * if false, include evals for groups which have not been approved only,
     * if null, include approved and unapproved,
     * NOTE: you should not include unapproved when displaying evaluations to users to take or sending emails
     * @param includeAnonymous if true, only include evaluations authcontrol = anon, 
     * if false, include any evaluations with authcontrol != anon,
     * if null, include all evaluations regardless of authcontrol
     * @return a List of EvalEvaluation objects sorted by due date, title, and id
     */
    @SuppressWarnings("unchecked")
    public List<EvalEvaluation> getEvalsUserCanTake(String userId, Boolean activeOnly,
            Boolean approvedOnly, Boolean includeAnonymous, int startResult, int maxResults) {
        if (userId == null || "".equals(userId)) {
            throw new IllegalArgumentException("userId cannot be null or blank");
        }

        Map<String, Object> params = new HashMap<>();

        /**
        String anonymousHQL = "";
        if (includeAnonymous != null) {
            params.put("authControl", EvalConstants.EVALUATION_AUTHCONTROL_NONE);
            if (includeAnonymous) {
                anonymousHQL += "or eval.authControl = :authControl";
            } else {
                anonymousHQL += "and eval.authControl <> :authControl";            
            }
        }

        String approvedHQL = "";
        if (approvedOnly != null) {
//            "select eval from EvalAssignUser eau right join eau.evaluation eval where eau.id is null"
            String groupsHQL = " and (eval.id in (select distinct assign.evaluation.id from EvalAssignGroup as assign " 
                + "where assign.evalGroupId in (:evalGroupIds)" + approvedHQL + ")" + anonymousHQL + ")";
//            params.put("evalGroupIds", evalGroupIds);

            approvedHQL = " and assign.instructorApproval = :approval ";
            if (approvedOnly) {
                params.put("approval", true);
            } else {
                params.put("approval", false);
            }
        }
         **/

        String activeHQL;
        if (activeOnly != null) {
            if (activeOnly) {
                activeHQL = " and eval.state = :activeState";
                params.put("activeState", EvalConstants.EVALUATION_STATE_ACTIVE);
            } else {
                activeHQL = " and (eval.state = :queueState or eval.state = :graceState or eval.state = :closedState or eval.state = :viewState)";
                params.put("queueState", EvalConstants.EVALUATION_STATE_INQUEUE);
                params.put("graceState", EvalConstants.EVALUATION_STATE_GRACEPERIOD);
                params.put("closedState", EvalConstants.EVALUATION_STATE_CLOSED);
                params.put("viewState", EvalConstants.EVALUATION_STATE_VIEWABLE);
            }
        } else {
            // need to filter out the partial and deleted state evals
            activeHQL = " and eval.state <> :partialState and eval.state <> :deletedState";
            params.put("partialState", EvalConstants.EVALUATION_STATE_PARTIAL);
            params.put("deletedState", EvalConstants.EVALUATION_STATE_DELETED);
        }

        params.put("userId", userId);
        params.put("assignUserType", EvalAssignUser.TYPE_EVALUATOR);
        String userAssignHQL = " eau.type = :assignUserType and eau.userId = :userId";

        String userAssignAuthHQL;
        if (includeAnonymous == null) {
            // include all
            params.put("authControl", EvalConstants.EVALUATION_AUTHCONTROL_NONE);
            userAssignAuthHQL = " and (("+userAssignHQL+" and eval.authControl <> :authControl) or eval.authControl = :authControl)";
        } else {
            params.put("authControl", EvalConstants.EVALUATION_AUTHCONTROL_NONE);
            if (includeAnonymous) {
                // only anon
                userAssignAuthHQL = " and eval.authControl = :authControl";
            } else {
                // only not anon
                userAssignAuthHQL = " and " + userAssignHQL + " and eval.authControl <> :authControl";            
            }
        }

        /* THIS IS THE ORIGINAL SQL
         * 
        SELECT distinct(eval.id), eval.* FROM eval_evaluation eval
        left join eval_assign_user eau on eau.EVALUATION_FK = eval.ID and eau.ASSIGN_TYPE = 'evaluator' and eau.USER_ID='f17f9a9c-1ac2-48de-ae33-d6f904bbb0c7'
        left join eval_assign_group eag on eag.EVALUATION_FK = eval.ID and eag.INSTRUCTOR_APPROVAL = true
        where eval.STATE = 'active' and ((eval.AUTH_CONTROL <> 'NONE'
        and eau.GROUP_ID = eag.group_id) OR eval.AUTH_CONTROL = 'NONE')
         */

        String hql = "select distinct eval from EvalAssignUser eau "
            + "right join eau.evaluation eval "
            + "where 1=1 "+activeHQL+userAssignAuthHQL
            + " order by eval.dueDate, eval.title, eval.id";

        List<EvalEvaluation> evals = (List<EvalEvaluation>) executeHqlQuery(hql, params, startResult, maxResults);

        // TODO populate the assign groups


        // sort the evals remaining
        Collections.sort(evals, new ComparatorsUtils.EvaluationDateTitleIdComparator());

        return evals;
    }




    /**
     * Get a set of evaluations based on the owner and their groups
     * 
     * @param userId internal user id, owner of the evaluations, if null then do not filter on the owner id
     * @param evalGroupIds an array of eval group IDs to get associated evals for, can be empty or null to get all evals
     * @param recentClosedDate only return evaluations which closed after this date
     * @param startResult 0 to start with the first result, otherwise start with this result number
     * @param maxResults 0 to return all results, otherwise limit the number of evals returned to this
     * @return a List of EvalEvaluation objects sorted by stop date, title, and id
     */
    @SuppressWarnings("unchecked")
    public List<EvalEvaluation> getEvaluationsForOwnerAndGroups(String userId,
            String[] evalGroupIds, Date recentClosedDate, int startResult, int maxResults, boolean includePartial) {
        Map<String, Object> params = new HashMap<>();

        String recentHQL = "";
        if (recentClosedDate != null) {
            recentHQL = " and ( (eval.viewDate is not null and eval.viewDate >= :recentClosedDate) "
                + "or (eval.stopDate is not null and eval.stopDate >= :recentClosedDate) "
                + "or (eval.dueDate is not null and eval.dueDate >= :recentClosedDate) ) ";
            params.put("recentClosedDate", recentClosedDate);
        }

        String ownerHQL = "";
        if (userId != null && userId.length() > 0) {
            ownerHQL = " eval.owner = :ownerId ";
            params.put("ownerId", userId);
        }

        String groupsHQL = "";
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            groupsHQL = " eval.id in (select distinct assign.evaluation.id "
                + "from EvalAssignGroup as assign where assign.nodeId is null "
                + "and assign.evalGroupId in (:evalGroupIds) ) ";
            params.put("evalGroupIds", evalGroupIds);
        }

        // merge the owner and groups HQL if needed
        String ownerGroupHQL = "";
        if (ownerHQL.length() > 0 && groupsHQL.length() > 0) {
            ownerGroupHQL = " and (" + ownerHQL + " or " + groupsHQL + ") ";
        } else if (ownerHQL.length() > 0) {
            ownerGroupHQL = " and " + ownerHQL;
        } else if (groupsHQL.length() > 0) {
            ownerGroupHQL = " and " + groupsHQL;
        }

        // need to filter out the partial and deleted state evals
        String stateHQL = " and eval.state <> :deletedState ";
        params.put("deletedState", EvalConstants.EVALUATION_STATE_DELETED);
        if (! includePartial) {
            stateHQL = stateHQL + " and eval.state <> :partialState ";
            params.put("partialState", EvalConstants.EVALUATION_STATE_PARTIAL);
        }

        List<EvalEvaluation> evals;
        String hql = "select eval from EvalEvaluation as eval " 
            + " where 1=1 " + stateHQL + recentHQL + ownerGroupHQL 
            + " order by eval.dueDate, eval.title, eval.id";
        evals = (List<EvalEvaluation>) executeHqlQuery(hql, params, startResult, maxResults);
        Collections.sort(evals, new ComparatorsUtils.EvaluationDateTitleIdComparator());
        return evals;
    }


    /**
     * Returns all answers to the given item associated with 
     * responses which are associated with the given evaluation,
     * only returns the answers for completed responses
     * 
     * @param evalId the id of the evaluation you want answers from
     * @param evalGroupIds an array of eval group IDs to return answers for,
     * if null then just return answers for all groups
     * @param templateItemIds the ids of the template items you want answers for,
     * if null then return answers for all template items
     * @return a list of EvalAnswer objects or empty list if none found
     */
    @SuppressWarnings("unchecked")
    public List<EvalAnswer> getAnswers(Long evalId, String[] evalGroupIds, Long[] templateItemIds) {
        Map<String, Object> params = new HashMap<>();

        String groupsHQL = "";
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            groupsHQL = " and ansswerresp.evalGroupId in (:evalGroupIds) ";
            params.put("evalGroupIds", evalGroupIds);
        }

        String itemsHQL = "";
        if (templateItemIds != null && templateItemIds.length > 0) {
            itemsHQL = " and answer.templateItem.id in (:templateItemIds) ";
            params.put("templateItemIds", templateItemIds);
        }

        params.put("evalId", evalId);
        String hql = "select answer from EvalAnswer as answer join answer.response as ansswerresp"
            + " where ansswerresp.evaluation.id = :evalId and ansswerresp.endTime is not null " + groupsHQL + itemsHQL
            + " order by ansswerresp.id, answer.id";
        // TODO optimize this once we are using a newer version of hibernate that supports "with"

        List<EvalAnswer> results = (List<EvalAnswer>) executeHqlQuery(hql, params, 0, 0);
        return results;
    }

    /**
     * Removes a group of templateItems and updates all related items 
     * and templates at the same time (inside one transaction)
     * 
     * @param templateItems the array of {@link EvalTemplateItem} to remove 
     */
    public void removeTemplateItems(EvalTemplateItem[] templateItems) {
        LOG.debug("Removing " + templateItems.length + " template items");
        Set<EvalTemplateItem> deleteTemplateItems = new HashSet<>();

        for( EvalTemplateItem templateItem : templateItems )
        {
            EvalTemplateItem eti = (EvalTemplateItem) getHibernateTemplate().merge( templateItem );
            deleteTemplateItems.add(eti);
            eti.getItem().getTemplateItems().remove(eti);
            eti.getTemplate().getTemplateItems().remove(eti);
            getHibernateTemplate().update(eti);
        }

        // do the actual deletes
        getHibernateTemplate().deleteAll(deleteTemplateItems);
        LOG.info("Removed " + deleteTemplateItems.size() + " template items");
    }


    /**
     * Get item groups contained within a specific group<br/>
     * <b>Note:</b> If parent is null then get all the highest level groups
     * 
     * @param parentItemGroupId the unique id of an {@link EvalItemGroup}, if null then get all the highest level groups
     * @param userId the internal user id (not username)
     * @param includeEmpty if true then include all groups (even those with nothing in them), else return only groups
     * which contain other groups or other items
     * @param includeExpert if true then include expert groups only, else include non-expert groups only
     * @return a List of {@link EvalItemGroup} objects, ordered by title alphabetically
     */
    public List<EvalItemGroup> getItemGroups(Long parentItemGroupId, String userId, boolean includeEmpty,
            boolean includeExpert) {

        DetachedCriteria dc = DetachedCriteria.forClass(EvalItemGroup.class).add(
                Expression.eq("expert", includeExpert));

        if (parentItemGroupId == null) {
            dc.add(Expression.isNull("parent"));
        } else {
            dc.add(Property.forName("parent.id").eq(parentItemGroupId));
        }

        if (!includeEmpty) {
            String hqlQuery = "select distinct eig.parent.id from EvalItemGroup eig where eig.parent is not null";
            List<?> parentIds = getHibernateTemplate().find(hqlQuery);

            // only include categories with items OR groups using them as a parent
            dc.add(Restrictions.disjunction().add(Property.forName("groupItems").isNotEmpty()).add(
                    Property.forName("id").in(parentIds)));
        }

        dc.addOrder(Order.asc("title"));

        List<?> things = getHibernateTemplate().findByCriteria(dc);
        List<EvalItemGroup> results = new ArrayList<>();
        for (Object object : things) {
            results.add((EvalItemGroup) object);
        }
        return results;
    }

    /**
     * Get item groups contained within a specific group<br/>
     * <b>Note:</b> If parent is null then get all the highest level groups
     * 
     * @param itemId
     * @param userId the internal user id (not username)
     * @return a List of {@link EvalItemGroup} objects, ordered by title alphabetically
     */
    @SuppressWarnings("unchecked")
    public Long getItemGroupIdByItemId(Long itemId, String userId) {

    	Long itemGroupId = null;
    	
    	String hql = "select eig.id from EvalItemGroup eig, EvalItem ei where eig.ig_item_id = ei.id and ei.id= ?";
        Object[] params = new Object[] {itemId};
        
        List<Long> results = (List<Long>) getHibernateTemplate().find(hql, params);
        if (! results.isEmpty() 
                && results.get(0) != null) {
            itemGroupId = results.get(0);
        }
        return itemGroupId;    
    }

    /**
     * Get all the templateItems for this template limited by the various hierarchy
     * settings specified, always returns the top hierarchy level set of items,
     * will include the template items limited by the various hierarchy levels and
     * ids of the parts of the nodes
     * 
     * @param templateId the unique id of an EvalTemplate object
     * @param nodeIds the unique ids of a set of hierarchy nodes for which we 
     * want all associated template items, null excludes all TIs associated with nodes,
     * an empty array will include all TIs associated with nodes
     * @param instructorIds a set of internal userIds of instructors for instructor added items,
     * null will exclude all instructor added items, empty array will include all
     * @param groupIds the unique eval group ids associated with a set of TIs in this template
     * (typically items which are associated with a specific eval group),
     * null excludes all associated TIs, empty array includes all 
     * @return a list of {@link EvalTemplateItem} objects, ordered by displayOrder
     */
    public List<EvalTemplateItem> getTemplateItemsByTemplate(Long templateId, String[] nodeIds,
            String[] instructorIds, String[] groupIds) {
        return getTemplateItemsByTemplates(new Long[] {templateId}, nodeIds, instructorIds, groupIds);
    }


    /**
     * Get all the templateItems for this evaluation limited by the various hierarchy
     * settings specified, always returns the top hierarchy level set of items,
     * will include the template items limited by the various hierarchy levels and
     * ids of the parts of the nodes, should be ordered in the list by the proper display order
     * 
     * @param evalId the unique id of an {@link EvalEvaluation} object
     * @param nodeIds the unique ids of a set of hierarchy nodes for which we 
     * want all associated template items, null excludes all TIs associated with nodes,
     * an empty array will include all TIs associated with nodes
     * @param instructorIds a set of internal userIds of instructors for instructor added items,
     * null will exclude all instructor added items, empty array will include all
     * @param groupIds the unique eval group ids associated with a set of TIs in this template
     * (typically items which are associated with a specific eval group),
     * null excludes all associated TIs, empty array includes all 
     * @return a list of {@link EvalTemplateItem} objects, ordered by displayOrder and template
     */
    public List<EvalTemplateItem> getTemplateItemsByEvaluation(Long evalId, String[] nodeIds, String[] instructorIds, String[] groupIds) {
        Long templateId = getTemplateIdForEvaluation(evalId);
        if (templateId == null) {
            throw new IllegalArgumentException("Could not retrieve a template id for this evaluation");
        }
        return getTemplateItemsByTemplates(new Long[] {templateId}, nodeIds, instructorIds, groupIds);
    }

    /**
     * Fetch all the template items based on templates and various params
     * @param templateIds
     * @param nodeIds
     * @param instructorIds
     * @param groupIds
     * @return a list of template items ordered by display order and template
     */
    private List<EvalTemplateItem> getTemplateItemsByTemplates(Long[] templateIds, String[] nodeIds, String[] instructorIds, String[] groupIds) {
        List<EvalTemplateItem> results = new ArrayList<>();
        if (templateIds == null || templateIds.length == 0) {
            throw new IllegalArgumentException("Invalid templateIds, cannot be null or empty");
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("templateIds", templateIds);
            params.put("hierarchyLevel1", EvalConstants.HIERARCHY_LEVEL_TOP);
            StringBuilder hql = new StringBuilder();
            hql.append("from EvalTemplateItem ti where ti.template.id in (:templateIds) and (ti.hierarchyLevel = :hierarchyLevel1 ");

            if (nodeIds != null) {
                if (nodeIds.length == 0) {
                    hql.append(" or (ti.hierarchyLevel = :hierarchyLevelNodes) ");
                    params.put("hierarchyLevelNodes", EvalConstants.HIERARCHY_LEVEL_NODE);
                } else {
                    hql.append(" or (ti.hierarchyLevel = :hierarchyLevelNodes and ti.hierarchyNodeId in (:nodeIds) ) ");
                    params.put("hierarchyLevelNodes", EvalConstants.HIERARCHY_LEVEL_NODE);
                    params.put("nodeIds", nodeIds);
                }
            }

            if (instructorIds != null) {
                if (instructorIds.length == 0) {
                    hql.append(" or (ti.hierarchyLevel = :hierarchyLevelInst) ");
                    params.put("hierarchyLevelInst", EvalConstants.HIERARCHY_LEVEL_INSTRUCTOR);
                } else {
                    hql.append(" or (ti.hierarchyLevel = :hierarchyLevelInst and ti.hierarchyNodeId in (:instructorIds) ) ");
                    params.put("hierarchyLevelInst", EvalConstants.HIERARCHY_LEVEL_INSTRUCTOR);
                    params.put("instructorIds", instructorIds);
                }
            }

            if (groupIds != null) {
                if (groupIds.length == 0) {
                    hql.append(" or (ti.hierarchyLevel = :hierarchyLevelGroup) ");
                    params.put("hierarchyLevelGroup", EvalConstants.HIERARCHY_LEVEL_GROUP);
                } else {
                    hql.append(" or (ti.hierarchyLevel = :hierarchyLevelGroup and ti.hierarchyNodeId in (:groupIds) ) ");
                    params.put("hierarchyLevelGroup", EvalConstants.HIERARCHY_LEVEL_GROUP);
                    params.put("groupIds", groupIds);
                }
            }

            hql.append(") order by ti.displayOrder, ti.template.id");

            List<?> things = executeHqlQuery(hql.toString(), params, 0, 0);
            for (Object object : things) {
                results.add((EvalTemplateItem) object);
            }
        }
        return results;
    }

    // public Integer getNextBlockId() {
    // String hqlQuery = "select max(item.blockId) from EvalItem item";
    // Integer max = (Integer) getHibernateTemplate().iterate(hqlQuery).next();
    // if (max == null) {
    // return new Integer(0);
    // }
    // return new Integer(max.intValue() + 1);
    // }


    /**
     * Returns list of response ids for a given evaluation
     * and for specific groups and for specific users if desired,
     * can limit to only completed responses
     *
     * @param evalId the id of the evaluation you want the response ids for
     * @param evalGroupIds an array of eval group IDs to return response ids for,
     * if null or empty then return responses ids for all evalGroups associated with this eval
     * @param userIds an array of internal userIds to return responses for,
     * if null or empty then return responses ids for all users
     * @param completed if true only return the completed responses, 
     * if false only return the incomplete responses,
     * if null then return all responses
     * @return a list of response ids (Long) for {@link EvalResponse} objects
     */
    public List<Long> getResponseIds(Long evalId, String[] evalGroupIds, String[] userIds, Boolean completed) {
        Map<String, Object> params = new HashMap<>();
        String groupsHQL = "";
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            groupsHQL = " and response.evalGroupId in (:evalGroupIds) ";
            params.put("evalGroupIds", evalGroupIds);
        }
        String usersHQL = "";
        if (userIds != null && userIds.length > 0) {
            usersHQL = " and response.owner in (:userIds) ";
            params.put("userIds", userIds);
        }
        String completedHQL = "";
        if (completed != null) {
            // if endTime is null then the response is incomplete, if not null then it is complete
            if (completed) {
                completedHQL = " and response.endTime is not null ";
            } else {
                completedHQL = " and response.endTime is null ";
            }
        }
        params.put("evalId", evalId);
        String hql = "SELECT response.id from EvalResponse as response where response.evaluation.id = :evalId "
            + groupsHQL + usersHQL + completedHQL + " order by response.id";
        List<?> results = executeHqlQuery(hql, params, 0, 0);
        List<Long> responseIds = new ArrayList<>();
        for (Object object : results) {
            responseIds.add((Long) object);
        }
        return responseIds;
    }


    /**
     * Removes an array of responses and all their associated answers at
     * the same time (in a single transaction)<br/>
     * Use {@link #getResponseIds(Long, String[], String[], Boolean)} to get the set of responseIds to remove<br/>
     * <b>WARNING:</b> This does not check permissions for removal of responses so you should
     * be sure to check that responses can be removed (system setting) and that they can be removed for this evaluation and user
     * 
     * @param responseIds the array of ids for {@link EvalResponse} objects to remove
     */
    public void removeResponses(Long[] responseIds) {
        if (responseIds != null && responseIds.length > 0) {
            String rids = "(" + ArrayUtils.arrayToString(responseIds) + ")";
            // purge out the answers first
            String hql = "delete EvalAnswer answer where answer.response.id in " + rids;
            LOG.debug("delete EvalAnswer HQL:" + hql);
            int results = getHibernateTemplate().bulkUpdate(hql);
            LOG.info("Remove " + results + " answers that were associated with the following responses: " + rids);

            // purge out the responses
            hql = "delete EvalResponse response where response.id in " + rids;
            LOG.debug("delete EvalResponse HQL:" + hql);
            results = getHibernateTemplate().bulkUpdate(hql);
            LOG.info("Remove " + results + " responses with the following ids: " + rids);
        }
    }


    /**
     * Get a list of evaluation categories
     * 
     * @param userId the internal user id (not username), if null then return all categories
     * @return a list of {@link String}
     */
    @SuppressWarnings("unchecked")
    public List<String> getEvalCategories(String userId) {
        String hql = "select distinct eval.evalCategory from EvalEvaluation eval where eval.evalCategory is not null ";
        String[] params = new String[] {};
        if (userId != null) {
            hql += " and eval.owner = ? ";
            params = new String[] { userId };
        }
        hql += " order by eval.evalCategory";
        return (List<String>) getHibernateTemplate().find(hql, (Object[]) params);
    }

    /**
     * Get the node which contains this evalgroup,
     * Note: this will always only return a single node so if an evalgroup is assigned to multiple
     * nodes then only the first one will be returned
     * @param evalGroupId a unique id for an eval group
     * @return a unique id for the containing node or null if none found
     */
    @SuppressWarnings("unchecked")
    public String getNodeIdForEvalGroup(String evalGroupId) {
        String hql = "select egn.nodeId from EvalGroupNodes egn join egn.evalGroups egrps where egrps.id = ? order by egn.nodeId";
        // switched to join from the subselect version
        //    String hql = "select egn.nodeId from EvalGroupNodes egn where ? in elements(egn.evalGroups) order by egn.nodeId";
        String[] params = new String[] {evalGroupId};
        List<String> l = (List<String>) getHibernateTemplate().find(hql, (Object[]) params);
        if (l.isEmpty()) {
            return null;
        }
        return (String) l.get(0);
    }

    /**
     * Get all the templates ids associated with an evaluation
     * 
     * @param evaluationId a unique id for an {@link EvalEvaluation}
     * @return a list of template ids for {@link EvalTemplate} objects
     */
    @SuppressWarnings("unchecked")
    protected Long getTemplateIdForEvaluation(Long evaluationId) {
        Long templateId = null;
        String hql = "select eval.template.id from EvalEvaluation eval where eval.id = ?";
        Object[] params = new Object[] {evaluationId};
        List<Long> results = (List<Long>) getHibernateTemplate().find(hql, params);
        if (! results.isEmpty() 
                && results.get(0) != null) {
            templateId = results.get(0);
        }
        return templateId;
    }


    /**
     * Get all the users who have responded to an evaluation (completely or partly)
     * and optionally within group(s) assigned to that evaluation
     * 
     * @param evaluationId a unique id for an {@link EvalEvaluation}
     * @param evalGroupIds [OPTIONAL] the unique eval group ids associated with this evaluation, 
     * can be null or empty to get all responses for this evaluation
     * @param completed [OPTIONAL] if true then only completed (submitted) responses, 
     *      if false, then only incomplete (saved) responses,
     *      if null, then retrieve all responses (incomplete and complete)
     * @return a set of internal userIds
     */
    public Set<String> getResponseUserIds(Long evaluationId, String[] evalGroupIds, Boolean completed) {
        Map<String, Object> params = new HashMap<>();
        String groupsHQL = "";
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            groupsHQL = " and response.evalGroupId in (:evalGroupIds) ";
            params.put("evalGroupIds", evalGroupIds);
        }
        String completeHQL = "";
        if (completed != null) {
            completeHQL = " and response.endTime is "+(completed ? "not" : "")+" null ";
        }
        params.put("evaluationId", evaluationId);
        String hql = "SELECT response.owner from EvalResponse as response where response.evaluation.id = :evaluationId "
            + completeHQL + groupsHQL + " order by response.id";
        List<?> results = executeHqlQuery(hql, params, 0, 0);
        // put the results into a set and convert them to strings
        Set<String> responseUsers = new HashSet<>();
        for (Object object : results) {
            responseUsers.add((String) object);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("ResponseUserIds(eval:"+evaluationId+", groups:"
                +ArrayUtils.arrayToString(evalGroupIds)+", completed="+completed+"): users="+responseUsers);
        }
        return responseUsers;
    }

    /** getResponsesSavedInProgress returns a List of EvalResponses that have been saved
     * but not submitted, meaning that they will not be included in any statistics.
     * @param activeEvaluationsOnly If true, only include responses associated with Evaluations
     * that are still open.  If false, only include respones associated with Evaluations that are closed
     * @see org.sakaiproject.evaluation.dao.EvaluationDao#getResponsesSavedInProgress()
     */
    @SuppressWarnings("unchecked")
    public List<EvalResponse> getResponsesSavedInProgress(boolean activeEvaluationsOnly) {
        Map<String, Object> params = new HashMap<>();
        String evalState = EvalConstants.EVALUATION_STATE_ACTIVE;
        params.put("evalState", evalState);
        String hql = "SELECT response from EvalResponse as response where response.endTime is null";         		
        if (activeEvaluationsOnly) {
        	hql += " and response.evaluation.state = :evalState"; 
        } else {
        	hql += " and response.evaluation.state != :evalState";
        }
        List<EvalResponse> results = (List<EvalResponse>) executeHqlQuery(hql, params, 0, 0);
        return results;
    }

    /**
     * Get all the evalGroupIds for an evaluation which are viewable by
     * the input permission,
     * can limit the eval groups to check by inputing an array of evalGroupIds<br/>
     * <b>NOTE:</b> If you input evalGroupIds then the returned set will always be
     * a subset (the same size or smaller) of the input
     * 
     * @param evaluationId a unique id for an {@link EvalEvaluation}
     * @param assignTypeConstant an assign type constant which is 
     * {@link EvalAssignUser#TYPE_EVALUATEE} for instructors/evaluatees OR
     * {@link EvalAssignUser#TYPE_EVALUATOR} for students/evaluators,
     * other permissions will return no results
     * @param evalGroupIds the unique eval group ids associated with this evaluation, 
     * can be null or empty to get all ids for this evaluation
     * @return a set of eval group ids which allow viewing by the specified permission
     */
    public Set<String> getViewableEvalGroupIds(Long evaluationId, String assignTypeConstant, String[] evalGroupIds) {
        if (evaluationId == null || assignTypeConstant == null) {
            throw new IllegalArgumentException("evaluationId and assignTypeConstant both must not be null");
        }
        EvalAssignUser.validateType(assignTypeConstant);
        String permCheck = null;
        if (EvalAssignUser.TYPE_EVALUATEE.equals(assignTypeConstant)) {
            permCheck = "instructorsViewResults";
        } else if (EvalAssignUser.TYPE_EVALUATOR.equals(assignTypeConstant)) {
            permCheck = "studentsViewResults";
        }

        Set<String> viewableEvalGroupIds = new HashSet<>();
        if (permCheck != null) {
            Map<String, Object> params = new HashMap<>();
            String groupsHQL = "";
            if (evalGroupIds != null && evalGroupIds.length > 0) {
                groupsHQL = " and eag.evalGroupId in (:evalGroupIds) ";
                params.put("evalGroupIds", evalGroupIds);
            }
            params.put("evaluationId", evaluationId);
            params.put("assignTypeConstant", assignTypeConstant);
            String hql = "SELECT eag.evalGroupId from EvalAssignGroup eag where eag.evaluation.id = :evaluationId "
                + " and eag.evalGroupId in (select distinct eau.evalGroupId from EvalAssignUser eau " +
                		"where eau.evaluation.id = :evaluationId and eau.type = :assignTypeConstant)"
                + " and eag."+permCheck+" = true " + groupsHQL;
            List<?> results = executeHqlQuery(hql, params, 0, 0);
            // put the results into a set and convert them to strings
            for (Object object : results) {
                viewableEvalGroupIds.add((String) object);
            }
        }
        return viewableEvalGroupIds;
    }


    /**
     * Get adhoc groups for a user and permission, 
     * this is a way to check the perms for a user
     * 
     * @param userId the internal user id (not username)
     * @param permissionConstant a permission constant which is 
     * {@link EvalConstants#PERM_BE_EVALUATED} for instructors/evaluatees OR
     * {@link EvalConstants#PERM_TAKE_EVALUATION} for students/evaluators,
     * other permissions will return no results
     * @return a list of adhoc groups for which this user has this permission
     */
    @SuppressWarnings("unchecked")
    public List<EvalAdhocGroup> getEvalAdhocGroupsByUserAndPerm(String userId, String permissionConstant) {

        String permCheck = null;
        if (EvalConstants.PERM_BE_EVALUATED.equals(permissionConstant)) {
            permCheck = "evaluateeIds";
        } else if (EvalConstants.PERM_TAKE_EVALUATION.equals(permissionConstant)) {
            permCheck = "participantIds";
        }

        List<EvalAdhocGroup> results;
        if (permCheck != null) {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            // select b.baz from Foo f join f.bars b"
            // select g.* from EVAL_ADHOC_GROUP g join EVAL_ADHOC_PARTICIPANTS p on p.ID = g.ID and p.USER_ID = 'aaronz' order by g.ID
            String hql = "from EvalAdhocGroup ag join ag." + permCheck + " userIds  where userIds.id = :userId order by ag.id";
            results = (List<EvalAdhocGroup>) executeHqlQuery(hql, params, 0, 0);
        } else {
            results = new ArrayList<>();
        }
        return results;
    }

    /**
     * Check if a user has a specified permission/role within an adhoc group
     * 
     * @param userId the internal user id (not username)
     * @param permissionConstant a permission string PERM constant (from this API),
     * <b>Note</b>: only take evaluation and be evaluated are supported
     * @param evalGroupId the unique id of an eval group
     * @return true if allowed, false otherwise
     */
    public boolean isUserAllowedInAdhocGroup(String userId, String permissionConstant, String evalGroupId) {
        boolean allowed = false;
        if (userId == null || evalGroupId == null) {
            throw new IllegalArgumentException("userId and evalGroupId must not be null");
        }

        String permCheck = null;
        if (EvalConstants.PERM_BE_EVALUATED.equals(permissionConstant)) {
            permCheck = "evaluateeIds";
        } else if (EvalConstants.PERM_TAKE_EVALUATION.equals(permissionConstant)) {
            permCheck = "participantIds";
        }

        if (permCheck != null) {
            Long id = EvalAdhocGroup.getIdFromAdhocEvalGroupId(evalGroupId);
            if (id != null) {
                // from EvalAdhocGroup ag join ag." + permCheck + " userIds where userIds.id = :userId
                String hql = "select count(ag) from EvalAdhocGroup ag join ag." + permCheck + " userIds "
                + " where ag.id = " + id + " and userIds.id = '" + userId + "'";
                Query query = getSession().createQuery(hql);
                int count = ( (Number) query.iterate().next() ).intValue();
                if (count >= 1) {
                    allowed = true;
                }
            }
        }

        return allowed;
    }



    // LOCKING METHODS

    /**
     * Set lock state if scale is not already at that lock state
     * 
     * @param scale
     * @param lockState if true then lock this scale, otherwise unlock it
     * @return true if success, false otherwise
     */
    public boolean lockScale(EvalScale scale, Boolean lockState) {
        LOG.debug("scale:" + scale.getId());
        if (scale.getId() == null) {
            throw new IllegalStateException("Cannot change lock state on an unsaved scale object");
        }

        if (lockState) {
            // locking this scale
            if (scale.getLocked()) {
                // already locked, no change
                return false;
            } else {
                // lock scale
                scale.setLocked(Boolean.TRUE);
                getHibernateTemplate().update(scale);
                return true;
            }
        } else {
            // unlocking this scale
            if (!scale.getLocked()) {
                // already unlocked, no change
                return false;
            } else {
                // unlock scale (if not locked elsewhere)
                DetachedCriteria dc = DetachedCriteria.forClass(EvalItem.class).add(
                        Restrictions.eq("locked", Boolean.TRUE)).add(Restrictions.eq("scale.id", scale.getId()))
                        .setProjection(Projections.rowCount());
                if (((Long) getHibernateTemplate().findByCriteria(dc).get(0)).intValue() > 0) {
                    // this is locked by something, we cannot unlock it
                    LOG.info("Cannot unlock scale (" + scale.getId() + "), it is locked elsewhere");
                    return false;
                }

                // unlock scale
                scale.setLocked(Boolean.FALSE);
                getHibernateTemplate().update(scale);
                return true;
            }
        }
    }

    /**
     * Set lock state if item is not already at that lock state,
     * lock associated scale if it does not match OR
     * unlock associated scale if not locked by other item(s) 
     * 
     * @param item
     * @param lockState if true then lock this item, otherwise unlock it
     * @return true if success, false otherwise
     */
    public boolean lockItem(EvalItem item, Boolean lockState) {
        LOG.debug("item:" + item.getId() + ", lockState:" + lockState);
        if (item.getId() == null) {
            throw new IllegalStateException("Cannot change lock state on an unsaved item object");
        }

        if (lockState) {
            // locking this item
            if (item.getLocked()) {
                // already locked, no change
                return false;
            } else {
                // lock item and associated scale (if set)
                item.setLocked(Boolean.TRUE);
                if (item.getScale() != null) {
                    lockScale(item.getScale(), Boolean.TRUE);
                }
                getHibernateTemplate().update(item);
                return true;
            }
        } else {
            // unlocking this item
            if (!item.getLocked()) {
                // already unlocked, no change
                return false;
            } else {
                // unlock item (if not locked elsewhere)
                String hqlQuery = "from EvalTemplateItem as ti where ti.item.id = '" + item.getId() + "' and ti.template.locked = true";
                if (count(hqlQuery) > 0) {
                    // this is locked by something, we cannot unlock it
                    LOG.info("Cannot unlock item (" + item.getId() + "), it is locked elsewhere");
                    return false;
                }

                // unlock item
                item.setLocked(Boolean.FALSE);
                getHibernateTemplate().update(item);

                // unlock associated scale if there is one
                if (item.getScale() != null) {
                    lockScale(item.getScale(), Boolean.FALSE);
                }

                return true;
            }
        }
    }

    /**
     * Set lock state if template is not already at that lock state,
     * lock associated item(s) if they do not match OR
     * unlock associated item(s) if not locked by other template(s) 
     * 
     * @param template
     * @param lockState if true then lock this template, otherwise unlock it
     * @return true if success, false otherwise
     */
    public boolean lockTemplate(EvalTemplate template, Boolean lockState) {
        LOG.debug("template:" + template.getId() + ", lockState:" + lockState);
        if (template.getId() == null) {
            throw new IllegalStateException("Cannot change lock state on an unsaved template object");
        }

        if (lockState) {
            // locking this template
            if (template.getLocked()) {
                // already locked, no change
                return false;
            } else {
                // lock template and associated items (if set)
                template.setLocked(Boolean.TRUE);
                if (template.getTemplateItems() != null && template.getTemplateItems().size() > 0) {
                    // loop through and lock all related items
                    for( EvalTemplateItem eti : template.getTemplateItems() )
                    {
                        lockItem(eti.getItem(), Boolean.TRUE);
                    }
                }
                getHibernateTemplate().update(template);
                return true;
            }
        } else {
            // unlocking this template
            if (!template.getLocked()) {
                // already unlocked, no change
                return false;
            } else {
                // unlock template (if not locked elsewhere)
                String hqlQuery = "from EvalEvaluation as eval where eval.template.id = '" + template.getId() + "' and eval.locked = true";
                if (count(hqlQuery) > 0) {
                    // this is locked by something, we cannot unlock it
                    LOG.info("Cannot unlock template (" + template.getId() + "), it is locked elsewhere");
                    return false;
                }

                // unlock template
                template.setLocked(Boolean.FALSE);
                getHibernateTemplate().update(template);

                // unlock associated items if there are any
                if (template.getTemplateItems() != null && template.getTemplateItems().size() > 0) {
                    // loop through and unlock all related items
                    for( EvalTemplateItem eti : template.getTemplateItems() )
                    {
                        lockItem(eti.getItem(), Boolean.FALSE);
                    }
                }

                return true;
            }
        }
    }

    /**
     * Lock evaluation if not already locked,
     * lock associated template(s) if not locked OR
     * unlock associated template(s) if not locked by other evaluations
     * 
     * @param evaluation
     * @param lockState if true then lock this evaluations, otherwise unlock it
     * @return true if success, false otherwise
     */
    public boolean lockEvaluation(EvalEvaluation evaluation, Boolean lockState) {
        LOG.debug("evaluation:" + evaluation.getId() + ", lockState:" + lockState);
        if (evaluation.getId() == null) {
            throw new IllegalStateException("Cannot change lock state on an unsaved evaluation object");
        }

        if (lockState) {
            // locking this evaluation
            if (evaluation.getLocked()) {
                // already locked, no change
                return false;
            } else {
                // lock evaluation and associated template
                EvalTemplate template = evaluation.getTemplate();
                if (! template.getLocked()) {
                    lockTemplate(template, Boolean.TRUE);
                }

                // This is a horrible hack to try to work around hibernate stupidity
                evaluation.setLocked(Boolean.TRUE);
                getSession().merge(evaluation);
                getSession().evict(evaluation);
                return true;
            }
        } else {
            // unlocking this template
            if (! evaluation.getLocked()) {
                // already unlocked, no change
                return false;
            } else {
                // unlock evaluation
                // This is a horrible hack to try to work around hibernate stupidity
                evaluation.setLocked(Boolean.FALSE);
                getSession().merge(evaluation);
                getSession().evict(evaluation);

                // unlock associated templates if there are any
                if (evaluation.getTemplate() != null) {
                    lockTemplate(evaluation.getTemplate(), Boolean.FALSE);
                }

                return true;
            }
        }
    }

    // IN_USE checks

    /**
     * NOT USED
     * @param scaleId
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Long[] getItemIdsUsingScale(Long scaleId) {
        String hql = "select item.id from EvalItem item join item.scale itemScale where itemScale.id = ? order by item.id";
        Object[] params = new Object[] {scaleId};
        List<Long> l = (List<Long>) getHibernateTemplate().find(hql, params);
        return l.toArray(new Long[] {});
    }

    /**
     * @param scaleId
     * @return true if this scale is used in any items
     */
    public boolean isUsedScale(Long scaleId) {
        if (scaleId != null) {
            LOG.debug("scaleId: " + scaleId);
            String hqlQuery = "from EvalItem as item where item.scale.id = '" + scaleId + "'";
            if (count(hqlQuery) > 0) {
                // this is used by something
                return true;
            }
        }
        return false;
    }

    /**
     * @param itemId
     * @return true if this item is used in any template (via a template item)
     */
    public boolean isUsedItem(Long itemId) {
        if (itemId != null) {
            LOG.debug("itemId: " + itemId);
            String hqlQuery = "from EvalTemplateItem as ti where ti.item.id = '" + itemId + "'";
            if (count(hqlQuery) > 0) {
                // this is used by something
                return true;
            }
        }
        return false;
    }

    /**
     * @param templateId
     * @return true if this template is used in any evalautions
     */
    public boolean isUsedTemplate(Long templateId) {
        if (templateId != null) {
            LOG.debug("templateId: " + templateId);
            String hqlQuery = "from EvalEvaluation as eval where eval.template.id = '" + templateId + "'";
            if (count(hqlQuery) > 0) {
                // this is used by something
                return true;
            }
        }
        return false;
    }


    /**
     * Allows a lock to be obtained that is system wide,
     * this is primarily for ensuring something runs on a single server only in a cluster<br/>
     * <b>NOTE:</b> This intentionally returns a null on failure rather than an exception since exceptions will
     * cause a rollback which makes the current session effectively dead, this also makes it impossible to 
     * control the failure so instead we return null as a marker
     * 
     * @param lockId the name of the lock which we are seeking
     * @param executerId a unique id for the executer of this lock (normally a server id)
     * @param timePeriod the length of time (in milliseconds) that the lock should be valid for,
     * set this very low for non-repeating processes (the length of time the process should take to run)
     * and the length of the repeat period plus the time to run the process for repeating jobs
     * @return true if a lock was obtained, false if not, null if failure
     */
    public Boolean obtainLock(String lockId, String executerId, long timePeriod) {
        if (executerId == null || 
                "".equals(executerId)) {
            throw new IllegalArgumentException("The executer Id must be set");
        }
        if (lockId == null || 
                "".equals(lockId)) {
            throw new IllegalArgumentException("The lock Id must be set");
        }

        // basically we are opening a transaction to get the current lock and set it if it is not there
        Boolean obtainedLock;
        try {
            // check the lock
            List<EvalLock> locks = findBySearch(EvalLock.class, new Search("name", lockId) );
            if (locks.size() > 0) {
                // check if this is my lock, if not, then exit, if so then go ahead
                EvalLock lock = locks.get(0);
                if (lock.getHolder().equals(executerId)) {
                    obtainedLock = true;
                    // if this is my lock then update it immediately
                    lock.setLastModified(new Date());
                    getHibernateTemplate().save(lock);
                    getHibernateTemplate().flush(); // this should commit the data immediately
                } else {
                    // not the lock owner but we can still get the lock
                    long validTime = lock.getLastModified().getTime() + timePeriod + 100;
                    if (System.currentTimeMillis() > validTime) {
                        // the old lock is no longer valid so we are taking it
                        obtainedLock = true;
                        lock.setLastModified(new Date());
                        lock.setHolder(executerId);
                        getHibernateTemplate().save(lock);
                        getHibernateTemplate().flush(); // this should commit the data immediately
                    } else {
                        // someone else is holding a valid lock still
                        obtainedLock = false;
                    }
                }
            } else {
                // obtain the lock
                EvalLock lock = new EvalLock(lockId, executerId);
                getHibernateTemplate().save(lock);
                getHibernateTemplate().flush(); // this should commit the data immediately
                obtainedLock = true;
            }
        } catch (RuntimeException e) {
            obtainedLock = null; // null indicates the failure
            cleanupLockAfterFailure(lockId);
            LOG.fatal("Lock obtaining failure for lock ("+lockId+"): " + e.getMessage(), e);
        }

        return obtainedLock;
    }

    /**
     * Releases a lock that was being held,
     * this is useful if you know a server is shutting down and you want to release your locks early<br/>
     * <b>NOTE:</b> This intentionally returns a null on failure rather than an exception since exceptions will
     * cause a rollback which makes the current session effectively dead, this also makes it impossible to 
     * control the failure so instead we return null as a marker
     * 
     * @param lockId the name of the lock which we are seeking
     * @param executerId a unique id for the executer of this lock (normally a server id)
     * @return true if a lock was released, false if not, null if failure
     */
    public Boolean releaseLock(String lockId, String executerId) {
        if (executerId == null || 
                "".equals(executerId)) {
            throw new IllegalArgumentException("The executer Id must be set");
        }
        if (lockId == null || 
                "".equals(lockId)) {
            throw new IllegalArgumentException("The lock Id must be set");
        }

        // basically we are opening a transaction to get the current lock and set it if it is not there
        Boolean releasedLock = false;
        try {
            // check the lock
            List<EvalLock> locks = findBySearch(EvalLock.class, new Search("name", lockId) );
            if (locks.size() > 0) {
                // check if this is my lock, if not, then exit, if so then go ahead
                EvalLock lock = locks.get(0);
                if (lock.getHolder().equals(executerId)) {
                    releasedLock = true;
                    // if this is my lock then remove it immediately
                    getHibernateTemplate().delete(lock);
                    getHibernateTemplate().flush(); // this should commit the data immediately
                } else {
                    releasedLock = false;
                }
            }
        } catch (RuntimeException e) {
            releasedLock = null; // null indicates the failure
            cleanupLockAfterFailure(lockId);
            LOG.fatal("Lock releasing failure for lock ("+lockId+"): " + e.getMessage(), e);
        }

        return releasedLock;
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.dao.EvaluationDao#countDistinctGroupsInConsolidatedEmailMapping()
     */
    @SuppressWarnings("rawtypes")
	public int countDistinctGroupsInConsolidatedEmailMapping() {
    	String hql = "select count(distinct groupId) from EvalEmailProcessingData";
    	Session session = getSession();
    	
        Query query = session.createQuery(hql);
    	
        List results = query.list();
        int count = 0;
        if(results == null || results.isEmpty()) {
        	// log error
        } else {
        	count = ((Integer) results.get(0));
        }
        return count;
    }

    /**
     * Access one page of summary info needed to render consolidated email templates. 
     * The summary info consists of a user-id, a user-eid, a template-id (EmailTemplate.ID) 
     * and the earliest due date of Active evals which use the email template and which the 
     * referenced user can take.
     * @param pageSize The maximum number of mappings to return. A mapping consists of a user-id, an email template
     * 		id and a date.
     * @param page The zero-based starting page. In other words, return a page of items beginning at index 
     * 		(pageSize * page).
     * @return map of email mappings
     */
    @SuppressWarnings("rawtypes")
	public List<Map<String,Object>>  getConsolidatedEmailMapping(boolean sendingAvailableEmails, int pageSize, int page) {
    	String query1 = "select userId,emailTemplateId,min(evalDueDate) from EvalEmailProcessingData group by emailTemplateId,userId order by emailTemplateId,userId";
    	
    	if(LOG.isDebugEnabled()) {
    		LOG.debug("getConsolidatedEmailMapping(" + sendingAvailableEmails + ", " + pageSize + ", " + page + ")");
    	}
    	
    	List<Map<String,Object>> rv = new ArrayList<>();
    	
    	Session session = getSession();
    	
        Query query = session.createQuery(query1);
        query.setFirstResult(pageSize * page);
        query.setMaxResults(pageSize);
        
    	List<String> userIdList = new ArrayList<>();
    	Long previousTemplateId = null;
    	Long templateId = null;
    	
        List results = query.list();

        if(results != null) {
        	LOG.info("found items from email-processing-queue: " + results.size());

            for(int i = 0; i < results.size(); i++) {
                Object[] row = (Object[]) results.get(i);
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
                LOG.info("added email-processing entry for user: " + userId + " templateId: " + templateId);
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
			LOG.info("Can't mark EvalAssignUser records due to null values: userId == " + userIdList + "   templateId == " + templateId);
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
		
		Query updateQuery = session.createQuery(hqlBuffer.toString());
		
		updateQuery.setDate("dateSent", new Date());
		updateQuery.setLong("emailTemplateId", templateId);
		
		for(String userId : userIdList) {
			try {
				
				updateQuery.setString("userId", userId);
				updateQuery.executeUpdate();
				
			} catch (HibernateException e) {
				LOG.warn("Error trying to update evalAssignUser. " + userId, e);
			}
		}
		if(LOG.isDebugEnabled()) {
			LOG.debug("         --> marked entries for users: " + userIdList);
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
		Query query = getSession().createQuery(deleteHql);
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
		
    	Query query = getSession().createQuery(queryBuf.toString());
    	
    	for(Map.Entry<String,Object> entry : params.entrySet()) {
    		if(entry.getValue() instanceof Date) {
    			query.setDate(entry.getKey(), (Date) entry.getValue());
    		} else if(entry.getValue() instanceof String) {
    			query.setString(entry.getKey(), (String) entry.getValue());
    		}
    	}
    	
	    	count = query.executeUpdate();
    	LOG.debug("Rows inserted into EVAL_EMAIL_PROCESSING_QUEUE: " + count);
		} catch(DataAccessResourceFailureException | IllegalStateException | HibernateException e) {
			LOG.warn("error processing consolidated-email query: " + e);
		}
		
    	return count;
	}

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.dao.EvaluationDao#getAllSiteIDsMatchingSectionTitle(java.lang.String)
     */
    public Set<String> getAllSiteIDsMatchingSectionTitle( String sectionTitleWithWildcards )
    {
        Session session = getSessionFactory().openSession();
        SQLQuery query = session.createSQLQuery( SQL_SELECT_SITE_IDS_MATCHING_SECTION_TITLE );
        query.setParameter( "title", sectionTitleWithWildcards );
        Set<String> results = new HashSet<>( query.list() );
        session.close();
        return results;
    }

    /*
     * (non-Javadoc)
     * @see org.sakaiproject.evaluation.dao.EvaluationDao#getAllSiteIDsMatchingSiteTitle(java.lang.String)
     */
    public Set<String> getAllSiteIDsMatchingSiteTitle( String siteTitleWithWildcards )
    {
        Session session = getSessionFactory().openSession();
        SQLQuery query = session.createSQLQuery( SQL_SELECT_SITE_IDS_MATCHING_SITE_TITLE );
        query.setParameter( "title", siteTitleWithWildcards );
        Set<String> results = new HashSet<>( query.list() );
        session.close();
        return results;
    }

    /**
     * Cleans up lock if there was a failure
     * 
     * @param lockId
     */
    private void cleanupLockAfterFailure(String lockId) {
        getHibernateTemplate().clear(); // cancel any pending operations
        // try to clear the lock if things died
        try {
            List<EvalLock> locks = findBySearch(EvalLock.class, new Search("name", lockId) );
            getHibernateTemplate().deleteAll(locks);
            getHibernateTemplate().flush();
        } catch (Exception ex) {
            LOG.error("Could not cleanup the lock ("+lockId+") after failure: " + ex.getMessage(), ex);
        }
    }

}
