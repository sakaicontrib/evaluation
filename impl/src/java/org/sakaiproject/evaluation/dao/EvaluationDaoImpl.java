/**
 * $Id$
 * $URL$
 * EvaluationDaoImpl.java - evaluation - Aug 21, 2006 12:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.dao;

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
import org.hibernate.Query;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAnswer;
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
import org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao;

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
public class EvaluationDaoImpl extends HibernateCompleteGenericDao implements EvaluationDao {

   private static Log log = LogFactory.getLog(EvaluationDaoImpl.class);

   public void init() {
      log.debug("init");
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

      if (sharingConstants != null && sharingConstants.length > 0) {
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
         for (int i = 0; i < options.length; i++) {
            if ("notHidden".equals(options[i])) {
               query.append(" and entity.hidden = false ");
            } else if ("notEmpty".equals(options[i])) {
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
    * Generates the HQL snippet needed to represent this property/comparison/value triple
    * 
    * @param property the name of the entity property
    * @param comparisonConstant the comparison constant (e.g. EQUALS)
    * @param value the value to compare the property to
    * @return a string representing the HQL snippet (e.g. propA = 'apple')
    */
   public String makeComparisonHQL(String property, int comparisonConstant, Object value) {
      String sval = null;
      if (value.getClass().isAssignableFrom(Boolean.class) 
            || value.getClass().isAssignableFrom(Number.class)) {
         // special handling for boolean and numbers
         sval = value.toString();
      } else {
         sval = "'" + value.toString() + "'";
      }
      switch (comparisonConstant) {
      case EQUALS:      return property + " = " + sval;
      case GREATER:     return property + " > " + sval;
      case LESS:        return property + " < " + sval;
      case LIKE:        return property + " like " + sval;
      case NOT_EQUALS:  return property + " <> " + sval;
      case NOT_NULL:    return property + " is not null";
      case NULL:        return property + " is null";
      default: throw new IllegalArgumentException("Invalid comparison constant: " + comparisonConstant);
      }
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
      log.debug("countSharedEntitiesForUser: HQL=" + hql);
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
      log.debug("getSharedEntitiesForUser: HQL=" + hql);
      Map<String, Object> params = new HashMap<String, Object>();
      List<T> l = executeHqlQuery(hql, params, start, limit);
      return l;
   }



   /**
    * Returns all evaluation objects associated with the input groups,
    * can also include anonymous evaluations and filter on a number
    * of options
    * 
    * @param evalGroupIds an array of eval group IDs to get associated evals for, 
    * can be empty or null but only anonymous evals will be returned
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
   public List<EvalEvaluation> getEvaluationsByEvalGroups(String[] evalGroupIds, Boolean activeOnly,
         Boolean approvedOnly, Boolean includeAnonymous, int startResult, int maxResults) {

      boolean emptyReturn = false;
      Map<String, Object> params = new HashMap<String, Object>();

      String groupsHQL = "";
      if (evalGroupIds != null && evalGroupIds.length > 0) {
         String approvedHQL = "";
         if (approvedOnly != null) {
            approvedHQL = " and assign.instructorApproval = :approval ";
            if (approvedOnly) {
               params.put("approval", true);
            } else {
               params.put("approval", false);               
            }
         }

         String anonymousHQL = "";
         if (includeAnonymous != null) {
            params.put("authControl", EvalConstants.EVALUATION_AUTHCONTROL_NONE);
            if (includeAnonymous) {
               anonymousHQL += "or eval.authControl = :authControl";
            } else {
               anonymousHQL += "and eval.authControl <> :authControl";            
            }
         }

         groupsHQL = " and (eval.id in (select distinct assign.evaluation.id from EvalAssignGroup as assign " 
            + "where assign.evalGroupId in (:evalGroupIds)" + approvedHQL + ")" + anonymousHQL + ")";
         params.put("evalGroupIds", evalGroupIds);
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

      List<EvalEvaluation> evals = null;
      if (emptyReturn) {
         evals = new ArrayList<EvalEvaluation>();
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

         String activeHQL = "";
         if (activeOnly != null) {
            if (activeOnly) {
               activeHQL = " and eval.state = :activeState ";
               params.put("activeState", EvalConstants.EVALUATION_STATE_ACTIVE);
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
         evals = executeHqlQuery(hql, params, startResult, maxResults);
         Collections.sort(evals, new ComparatorsUtils.EvaluationDateTitleIdComparator());
      }
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
      Map<String, Object> params = new HashMap<String, Object>();

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

      List<EvalEvaluation> evals = null;
      String hql = "select eval from EvalEvaluation as eval " 
         + " where 1=1 " + stateHQL + recentHQL + ownerGroupHQL 
         + " order by eval.dueDate, eval.title, eval.id";
      evals = executeHqlQuery(hql, params, startResult, maxResults);
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
      Map<String, Object> params = new HashMap<String, Object>();

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

      List<EvalAnswer> results = executeHqlQuery(hql, params, 0, 0);
      return results;
   }

   /**
    * Removes a group of templateItems and updates all related items 
    * and templates at the same time (inside one transaction)
    * 
    * @param templateItems the array of {@link EvalTemplateItem} to remove 
    */
   public void removeTemplateItems(EvalTemplateItem[] templateItems) {
      log.debug("Removing " + templateItems.length + " template items");
      Set<EvalTemplateItem> deleteTemplateItems = new HashSet<EvalTemplateItem>();

      for (int i = 0; i < templateItems.length; i++) {
         EvalTemplateItem eti = (EvalTemplateItem) getHibernateTemplate().merge(templateItems[i]);
         deleteTemplateItems.add(eti);

         eti.getItem().getTemplateItems().remove(eti);
         eti.getTemplate().getTemplateItems().remove(eti);
         getHibernateTemplate().update(eti);
      }

      // do the actual deletes
      getHibernateTemplate().deleteAll(deleteTemplateItems);
      log.info("Removed " + deleteTemplateItems.size() + " template items");
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
            Expression.eq("expert", new Boolean(includeExpert)));

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
      List<EvalItemGroup> results = new ArrayList<EvalItemGroup>();
      for (Object object : things) {
         results.add((EvalItemGroup) object);
      }
      return results;
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
      List<EvalTemplateItem> results = new ArrayList<EvalTemplateItem>();
      if (templateIds == null || templateIds.length == 0) {
         throw new IllegalArgumentException("Invalid templateIds, cannot be null or empty");
      } else {
         Map<String, Object> params = new HashMap<String, Object>();
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
      Map<String, Object> params = new HashMap<String, Object>();
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
      List<Long> responseIds = new ArrayList<Long>();
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
    * @throws Exception if there is a failure
    */
   public void removeResponses(Long[] responseIds) {
      if (responseIds != null && responseIds.length > 0) {
         String rids = "(" + ArrayUtils.arrayToString(responseIds) + ")";
         // purge out the answers first
         String hql = "delete EvalAnswer answer where answer.response.id in " + rids;
         log.debug("delete EvalAnswer HQL:" + hql);
         int results = getHibernateTemplate().bulkUpdate(hql);
         log.info("Remove " + results + " answers that were associated with the following responses: " + rids);

         // purge out the responses
         hql = "delete EvalResponse response where response.id in " + rids;
         log.debug("delete EvalResponse HQL:" + hql);
         results = getHibernateTemplate().bulkUpdate(hql);
         log.info("Remove " + results + " responses with the following ids: " + rids);
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
      return getHibernateTemplate().find(hql, params);
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
      List<String> l = getHibernateTemplate().find(hql, params);
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
      List<Long> results = getHibernateTemplate().find(hql, params);
      if (! results.isEmpty() 
            && results.get(0) != null) {
         templateId = results.get(0);
      }
      return templateId;
   }


   /**
    * Get all the users who have completely responded to an evaluation 
    * and optionally within group(s) assigned to that evaluation
    * 
    * @param evaluationId a unique id for an {@link EvalEvaluation}
    * @param evalGroupIds the unique eval group ids associated with this evaluation, 
    * can be null or empty to get all responses for this evaluation
    * @return a set of internal userIds
    */
   public Set<String> getResponseUserIds(Long evaluationId, String[] evalGroupIds) {
      Map<String, Object> params = new HashMap<String, Object>();
      String groupsHQL = "";
      if (evalGroupIds != null && evalGroupIds.length > 0) {
         groupsHQL = " and response.evalGroupId in (:evalGroupIds) ";
         params.put("evalGroupIds", evalGroupIds);
      }
      params.put("evaluationId", evaluationId);
      String hql = "SELECT response.owner from EvalResponse as response where response.evaluation.id = :evaluationId "
         + " and response.endTime is not null " + groupsHQL + " order by response.id";
      List<?> results = executeHqlQuery(hql, params, 0, 0);
      // put the results into a set and convert them to strings
      Set<String> responseUsers = new HashSet<String>();
      for (Object object : results) {
         responseUsers.add((String) object);
      }
      return responseUsers;
   }



   /**
    * Get all the evalGroupIds for an evaluation which are viewable by
    * the input permission,
    * can limit the eval groups to check by inputing an array of evalGroupIds<br/>
    * <b>NOTE:</b> If you input evalGroupIds then the returned set will always be
    * a subset (the same size or smaller) of the input
    * 
    * @param evaluationId a unique id for an {@link EvalEvaluation}
    * @param permissionConstant a permission constant which is 
    * {@link EvalConstants#PERM_BE_EVALUATED} for instructors/evaluatees OR
    * {@link EvalConstants#PERM_TAKE_EVALUATION} for students/evaluators,
    * other permissions will return no results
    * @param evalGroupIds the unique eval group ids associated with this evaluation, 
    * can be null or empty to get all ids for this evaluation
    * @return a set of eval group ids which allow viewing by the specified permission
    */
   public Set<String> getViewableEvalGroupIds(Long evaluationId, String permissionConstant, String[] evalGroupIds) {
      if (evaluationId == null || permissionConstant == null) {
         throw new IllegalArgumentException("evaluationId and permissionConstant both must not be null");
      }
      String permCheck = null;
      if (EvalConstants.PERM_BE_EVALUATED.equals(permissionConstant)) {
         permCheck = "instructorsViewResults";
      } else if (EvalConstants.PERM_TAKE_EVALUATION.equals(permissionConstant)) {
         permCheck = "studentsViewResults";
      }

      Set<String> viewableEvalGroupIds = new HashSet<String>();
      if (permCheck != null) {
         Map<String, Object> params = new HashMap<String, Object>();
         String groupsHQL = "";
         if (evalGroupIds != null && evalGroupIds.length > 0) {
            groupsHQL = " and ag.evalGroupId in (:evalGroupIds) ";
            params.put("evalGroupIds", evalGroupIds);
         }
         params.put("evaluationId", evaluationId);
         String hql = "SELECT ag.evalGroupId from EvalAssignGroup as ag where ag.evaluation.id = :evaluationId "
            + " and ag."+permCheck+" = true " + groupsHQL;
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

      List<EvalAdhocGroup> results = null;
      if (permCheck != null) {
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("userId", userId);
         // select b.baz from Foo f join f.bars b"
         // select g.* from EVAL_ADHOC_GROUP g join EVAL_ADHOC_PARTICIPANTS p on p.ID = g.ID and p.USER_ID = 'aaronz' order by g.ID
         String hql = "from EvalAdhocGroup ag join ag." + permCheck + " userIds  where userIds.id = :userId order by ag.id";
         results = executeHqlQuery(hql, params, 0, 0);
      } else {
         results = new ArrayList<EvalAdhocGroup>();
      }
      return results;
   }

   /**
    * Check if a user has a specified permission/role within an adhoc group
    * 
    * @param userId the internal user id (not username)
    * @param permission a permission string PERM constant (from this API),
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
      log.debug("scale:" + scale.getId());
      if (scale.getId() == null) {
         throw new IllegalStateException("Cannot change lock state on an unsaved scale object");
      }

      if (lockState.booleanValue()) {
         // locking this scale
         if (scale.getLocked().booleanValue()) {
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
         if (!scale.getLocked().booleanValue()) {
            // already unlocked, no change
            return false;
         } else {
            // unlock scale (if not locked elsewhere)
            DetachedCriteria dc = DetachedCriteria.forClass(EvalItem.class).add(
                  Restrictions.eq("locked", Boolean.TRUE)).add(Restrictions.eq("scale.id", scale.getId()))
                  .setProjection(Projections.rowCount());
            if (((Integer) getHibernateTemplate().findByCriteria(dc).get(0)).intValue() > 0) {
               // this is locked by something, we cannot unlock it
               log.info("Cannot unlock scale (" + scale.getId() + "), it is locked elsewhere");
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
      log.debug("item:" + item.getId() + ", lockState:" + lockState);
      if (item.getId() == null) {
         throw new IllegalStateException("Cannot change lock state on an unsaved item object");
      }

      if (lockState.booleanValue()) {
         // locking this item
         if (item.getLocked().booleanValue()) {
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
         if (!item.getLocked().booleanValue()) {
            // already unlocked, no change
            return false;
         } else {
            // unlock item (if not locked elsewhere)
            String hqlQuery = "from EvalTemplateItem as ti where ti.item.id = '" + item.getId()
            + "' and ti.template.locked = true";
            if (count(hqlQuery) > 0) {
               // this is locked by something, we cannot unlock it
               log.info("Cannot unlock item (" + item.getId() + "), it is locked elsewhere");
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
      log.debug("template:" + template.getId() + ", lockState:" + lockState);
      if (template.getId() == null) {
         throw new IllegalStateException("Cannot change lock state on an unsaved template object");
      }

      if (lockState.booleanValue()) {
         // locking this template
         if (template.getLocked().booleanValue()) {
            // already locked, no change
            return false;
         } else {
            // lock template and associated items (if set)
            template.setLocked(Boolean.TRUE);
            if (template.getTemplateItems() != null && template.getTemplateItems().size() > 0) {
               // loop through and lock all related items
               for (Iterator<?> iter = template.getTemplateItems().iterator(); iter.hasNext();) {
                  EvalTemplateItem eti = (EvalTemplateItem) iter.next();
                  lockItem(eti.getItem(), Boolean.TRUE);
               }
            }
            getHibernateTemplate().update(template);
            return true;
         }
      } else {
         // unlocking this template
         if (!template.getLocked().booleanValue()) {
            // already unlocked, no change
            return false;
         } else {
            // unlock template (if not locked elsewhere)
            String hqlQuery = "from EvalEvaluation as eval where eval.template.id = '" + template.getId()
            + "' and eval.locked = true";
            if (count(hqlQuery) > 0) {
               // this is locked by something, we cannot unlock it
               log.info("Cannot unlock template (" + template.getId() + "), it is locked elsewhere");
               return false;
            }

            // unlock template
            template.setLocked(Boolean.FALSE);
            getHibernateTemplate().update(template);

            // unlock associated items if there are any
            if (template.getTemplateItems() != null && template.getTemplateItems().size() > 0) {
               // loop through and unlock all related items
               for (Iterator<?> iter = template.getTemplateItems().iterator(); iter.hasNext();) {
                  EvalTemplateItem eti = (EvalTemplateItem) iter.next();
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
      log.debug("evaluation:" + evaluation.getId() + ", lockState:" + lockState);
      if (evaluation.getId() == null) {
         throw new IllegalStateException("Cannot change lock state on an unsaved evaluation object");
      }

      if (lockState.booleanValue()) {
         // locking this evaluation
         if (evaluation.getLocked().booleanValue()) {
            // already locked, no change
            return false;
         } else {
            // lock evaluation and associated template
            EvalTemplate template = evaluation.getTemplate();
            if (! template.getLocked().booleanValue()) {
               lockTemplate(template, Boolean.TRUE);
            }

            evaluation.setLocked(Boolean.TRUE);
            getHibernateTemplate().update(evaluation);
            return true;
         }
      } else {
         // unlocking this template
         if (! evaluation.getLocked().booleanValue()) {
            // already unlocked, no change
            return false;
         } else {
            // unlock evaluation
            evaluation.setLocked(Boolean.FALSE);
            getHibernateTemplate().update(evaluation);

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
      List<Long> l = getHibernateTemplate().find(hql, params);
      return l.toArray(new Long[] {});
   }


   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.evaluation.dao.EvaluationDaoImpl#isUsedScale(java.lang.Long)
    */
   public boolean isUsedScale(Long scaleId) {
      log.debug("scaleId: " + scaleId);
      String hqlQuery = "from EvalItem as item where item.scale.id = '" + scaleId + "'";
      if (count(hqlQuery) > 0) {
         // this is used by something
         return true;
      }
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.evaluation.dao.EvaluationDaoImpl#isUsedItem(java.lang.Long)
    */
   public boolean isUsedItem(Long itemId) {
      log.debug("itemId: " + itemId);
      String hqlQuery = "from EvalTemplateItem as ti where ti.item.id = '" + itemId + "'";
      if (count(hqlQuery) > 0) {
         // this is used by something
         return true;
      }
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.evaluation.dao.EvaluationDaoImpl#isUsedTemplate(java.lang.Long)
    */
   public boolean isUsedTemplate(Long templateId) {
      log.debug("templateId: " + templateId);
      String hqlQuery = "from EvalEvaluation as eval where eval.template.id = '" + templateId + "'";
      if (count(hqlQuery) > 0) {
         // this is used by something
         return true;
      }
      return false;
   }

   /**
    * Provides an easy way to execute an hql query with named parameters
    * 
    * @param hql
    *           a hibernate query language query
    * @param params
    *           the map of named parameters
    * @param start
    *           the entry number to start on (based on current sort rules), first entry is 0
    * @param limit
    *           the maximum number of entries to return, 0 returns as many entries as possible
    * @return a list of whatever you requested in the HQL
    */
   @SuppressWarnings("unchecked")
   private List executeHqlQuery(String hql, Map<String, Object> params, int start, int limit) {
      List l = null;
      try {
         Query query = getSession().createQuery(hql);
         query.setFirstResult(start);
         if (limit > 0) {
            query.setMaxResults(limit);
         }
         setParameters(query, params);
         l = query.list();
      } catch (org.hibernate.exception.SQLGrammarException e) {
         // failed to execute the query
         StringBuilder info = new StringBuilder();
         info.append("Failure info: errorCode=" + e.getErrorCode());
         info.append(", SQLstate=" + e.getSQLState());
         info.append(", SQL=" + e.getSQL());
         throw new RuntimeException("Unable to execute query: " + e.getMessage() + " :: HQL=" + hql + " :: " + info, e);
      }
      return l;
   }


   /**
    * This is supported natively in Hibernate 3.2.x and up
    * 
    * @param query
    * @param params
    */
   private void setParameters(Query query, Map<String, Object> params) {
      for (String name : params.keySet()) {
         Object param = params.get(name);
         if (param.getClass().isArray()) {
            query.setParameterList(name, (Object[]) param);
         } else {
            query.setParameter(name, param);
         }
      }
   }



   /**
    * Allows a lock to be obtained that is system wide,
    * this is primarily for ensuring something runs on a single server only in a cluster<br/>
    * <b>NOTE:</b> This intentionally returns a null on failure rather than an exception since exceptions will
    * cause a rollback which makes the current session effectively dead, this also makes it impossible to 
    * control the failure so instead we return null as a marker
    * 
    * @param lockId the name of the lock which we are seeking
    * @param holderId a unique id for the holder of this lock (normally a server id)
    * @param timePeriod the length of time (in milliseconds) that the lock should be valid for,
    * set this very low for non-repeating processes (the length of time the process should take to run)
    * and the length of the repeat period plus the time to run the process for repeating jobs
    * @return true if a lock was obtained, false if not, null if failure
    */
   @SuppressWarnings("unchecked")
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
      Boolean obtainedLock = false;
      try {
         // check the lock
         List<EvalLock> locks = findByProperties(EvalLock.class, 
               new String[] {"name"},
               new Object[] {lockId});
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
         log.fatal("Lock obtaining failure for lock ("+lockId+"): " + e.getMessage(), e);
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
    * @param holderId a unique id for the holder of this lock (normally a server id)
    * @return true if a lock was released, false if not, null if failure
    */
   @SuppressWarnings("unchecked")
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
         List<EvalLock> locks = findByProperties(EvalLock.class, 
               new String[] {"name"},
               new Object[] {lockId});
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
         log.fatal("Lock releasing failure for lock ("+lockId+"): " + e.getMessage(), e);
      }

      return releasedLock;
   }

   /**
    * Allows for an easy way to run some code ONLY if a lock can be obtained by
    * the executer, if the lock can be obtained then the Runnable is executed,
    * if not, then nothing happens<br/>
    * This is primarily useful for ensuring only a single server in the cluster
    * is executing some code<br/>
    * <b>NOTE:</b> This intentionally returns a null on failure rather than an exception since exceptions will
    * cause a rollback which makes the current session effectively dead, this also makes it impossible to 
    * control the failure so instead we return null as a marker
    * @param lockId the name of the lock which we are seeking
    * @param executerId a unique id for the executer of this code (normally a server id)
    * @param toExecute a {@link Runnable} which will have the run method executed if a lock can be obtained
    * 
    * @return true if the code was executed, false if someone else has the lock, null if there was a failure
    */
   @SuppressWarnings("unchecked")
   public Boolean lockAndExecuteRunnable(String lockId, String executerId, Runnable toExecute) {
      if (executerId == null || 
            "".equals(executerId)) {
         throw new IllegalArgumentException("The executer Id must be set");
      }
      if (lockId == null || 
            "".equals(lockId)) {
         throw new IllegalArgumentException("The lock Id must be set");
      }
      if (toExecute == null) {
         throw new IllegalArgumentException("The toExecute Runnable must not be null");
      }

      // basically we are opening a transaction to get the current lock and set it if it is not there
      Boolean loadingData = false;
      try {
         // check the lock
         List<EvalLock> locks = findByProperties(EvalLock.class, 
               new String[] {"name"},
               new Object[] {lockId});
         if (locks.size() > 0) {
            // check if this is my lock, if not, then exit, if so then go ahead
            EvalLock lock = locks.get(0);
            if (lock.getHolder().equals(executerId)) {
               loadingData = true;
            } else {
               loadingData = false;
            }
         } else {
            // obtain the lock
            EvalLock lock = new EvalLock(lockId, executerId);
            getHibernateTemplate().save(lock);
            getHibernateTemplate().flush(); // this should commit the data immediately
            loadingData = true;
         }
         locks.clear(); // clear the locks list

         if (loadingData) {
            // execute the runnable method
            toExecute.run();

            // clear the lock
            locks = findByProperties(EvalLock.class, 
                  new String[] {"name"},
                  new Object[] {lockId});
            getHibernateTemplate().deleteAll(locks);
            // commit preload and lock removal
            getHibernateTemplate().flush();
         }
      } catch (RuntimeException e) {
         loadingData = null; // null indicates the failure
         cleanupLockAfterFailure(lockId);
         log.fatal("Lock and execute failure for lock ("+lockId+"): " + e.getMessage(), e);
      }

      return loadingData;
   }


   /**
    * Cleans up lock if there was a failure
    * 
    * @param lockId
    */
   @SuppressWarnings("unchecked")
   private void cleanupLockAfterFailure(String lockId) {
      getHibernateTemplate().clear(); // cancel any pending operations
      // try to clear the lock if things died
      try {
         List<EvalLock> locks = findByProperties(EvalLock.class, 
               new String[] {"name"},
               new Object[] {lockId});
         getHibernateTemplate().deleteAll(locks);
         getHibernateTemplate().flush();
      } catch (Exception ex) {
         log.error("Could not cleanup the lock ("+lockId+") after failure: " + ex.getMessage(), ex);
      }
   }

}
