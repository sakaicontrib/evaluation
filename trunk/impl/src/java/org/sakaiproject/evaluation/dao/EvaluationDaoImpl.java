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
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalLock;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.ArrayUtils;
import org.sakaiproject.evaluation.utils.ComparatorsUtils;
import org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao;
import org.springframework.dao.DataAccessException;

/**
 * Implementations for any methods from the EvaluationDao interface<br/> 
 * Includes locking method implementations,
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
    * Construct the HQL to do the templates query based on booleans and userId
    * 
    * @param userId
    *           the Sakai internal user Id (of the owner)
    * @param publicTemplates
    * @param visibleTemplates
    * @param sharedTemplates
    * @return an HQL query string
    */
   private String makeTemplateHQL(String userId, boolean includeEmpty, boolean publicTemplates,
         boolean visibleTemplates, boolean sharedTemplates) {

      /**
       * TODO - Hierarchy visible and shared sharing methods are meant to work by relating the
       * hierarchy level of the owner with the sharing setting in the template, however, that was
       * when we assumed there would only be one level per user. That is no longer anything we have
       * control over (since we depend on data that comes from another API) so we will have to add
       * in a table which will track the hierarchy levels and link them to the template. This will
       * be a very simple but necessary table.
       */

      boolean atleastOnePredicate = false;
      StringBuffer query = new StringBuffer("from EvalTemplate as template where template.type = '"
            + EvalConstants.TEMPLATE_TYPE_STANDARD + "' ");

      // do not include templates which have no items in them
      if (!includeEmpty) {
         query.append(" and template.templateItems.size > 0 and ");
      } else {
         query.append(" and ");
      }

      if (userId == null) {
         // null userId means return all private templates
         if (atleastOnePredicate) query.append(" or ");
         atleastOnePredicate = true;

         query.append(" template.sharing = '" + EvalConstants.SHARING_PRIVATE + "' ");
      } else if ("".equals(userId)) {
         // blank userId means no private templates
      } else {
         // all private templates based on the userId (owner)
         if (atleastOnePredicate) query.append(" or ");
         atleastOnePredicate = true;

         query.append(" (template.sharing = '" + EvalConstants.SHARING_PRIVATE + "' and template.owner = '"
               + userId + "') ");
      }

      if (publicTemplates) {
         if (atleastOnePredicate) query.append(" or ");
         atleastOnePredicate = true;

         query.append(" template.sharing = '" + EvalConstants.SHARING_PUBLIC + "'");
      }

      // if (visibleTemplates) {
      // if (atleastOnePredicate)
      // query.append(" or ");
      // atleastOnePredicate = true;

      // query.append(" template.sharing = '" + EvalConstants.SHARING_VISIBLE + "' ");
      // }

      // if (sharedTemplates) {
      // if (atleastOnePredicate)
      // query.append(" or ");
      // atleastOnePredicate = true;

      // query.append(" template.sharing = '" + EvalConstants.SHARING_SHARED + "' ");
      // }

      return query.toString();
   }

   public int countVisibleTemplates(String userId, String[] sharingConstants, boolean includeEmpty) {
      boolean publicTemplates = false;
      for (int i = 0; i < sharingConstants.length; i++) {
         String sharingConstant = sharingConstants[i];
         if (EvalConstants.SHARING_PRIVATE.equals(sharingConstant)) {
            if (userId == null || userId.equals("")) {
               throw new IllegalArgumentException("Must specify a userId when requesting private templates");
            }
         } else if (EvalConstants.SHARING_PUBLIC.equals(sharingConstant)) {
            publicTemplates = true;
         }
      }
      String hqlQuery = makeTemplateHQL(userId, includeEmpty, publicTemplates, false, false);

      int count = 0;
      try {
         count = count(hqlQuery);
      } catch (DataAccessException e) {
         // this may appear to be a swallowed error, but it is actually intended behavior
         log.error("Invalid argument combination (most likely you tried to request no items) caused failure",
               e);
      }
      return count;
   }

   @SuppressWarnings("unchecked")
   public List<EvalTemplate> getVisibleTemplates(String userId, String[] sharingConstants,
         boolean includeEmpty) {

      boolean publicTemplates = false;
      for (int i = 0; i < sharingConstants.length; i++) {
         String sharingConstant = sharingConstants[i];
         if (EvalConstants.SHARING_PRIVATE.equals(sharingConstant)) {
            if (userId == null || userId.equals("")) {
               throw new IllegalArgumentException("Must specify a userId when requesting private templates");
            }
         } else if (EvalConstants.SHARING_PUBLIC.equals(sharingConstant)) {
            publicTemplates = true;
         }
      }
      String hqlQuery = makeTemplateHQL(userId, includeEmpty, publicTemplates, false, false);

      // add ordering to returned values
      hqlQuery += " order by template.sharing, template.title";

      List<EvalTemplate> l = new ArrayList<EvalTemplate>();
      try {
         l = getHibernateTemplate().find(hqlQuery);
      } catch (DataAccessException e) {
         // this may appear to be a swallowed error, but it is actually intended behavior
         log.error("Invalid argument combination (most likely you tried to request no items) caused failure");
      }
      return l;
   }

   @SuppressWarnings("unchecked")
   public List<EvalEvaluation> getEvaluationsByEvalGroups(String[] evalGroupIds, boolean activeOnly,
         boolean includeUnApproved, boolean includeAnonymous) {
      boolean emptyReturn = false;
      Map<String, Object> params = new HashMap<String, Object>();
      String groupsHQL = "";
      if (evalGroupIds != null && evalGroupIds.length > 0) {
         String unapprovedHQL = "";
         if (! includeUnApproved) {
            unapprovedHQL = " and assign.instructorApproval = :approval ";
            params.put("approval", true);
         }
         groupsHQL = " eval.id in (select distinct assign.evaluation.id " +
         "from EvalAssignGroup as assign where assign.nodeId is null " +
         "and assign.evalGroupId in (:evalGroupIds)" + unapprovedHQL + ") ";
         params.put("evalGroupIds", evalGroupIds);

         params.put("authControl", EvalConstants.EVALUATION_AUTHCONTROL_NONE);
         if (includeAnonymous) {
            groupsHQL += "or eval.authControl = :authControl";
         } else {
            groupsHQL += "and eval.authControl != :authControl";            
         }
         groupsHQL = " and (" + groupsHQL + ") ";
      } else {
         // no groups but we want to get anonymous evals
         if (includeAnonymous) {
            params.put("authControl", EvalConstants.EVALUATION_AUTHCONTROL_NONE);
            groupsHQL += " and eval.authControl = :authControl ";
         } else {
            // no groups and no anonymous evals means nothing to return
            emptyReturn = true;
         }
      }

      List<EvalEvaluation> evals = null;
      if (emptyReturn) {
         evals = new ArrayList<EvalEvaluation>();
      } else {
         String activeHQL = "";
         if (activeOnly) {
            activeHQL = " and eval.state = :activeState ";
            params.put("activeState", EvalConstants.EVALUATION_STATE_ACTIVE);
         } else {
            // need to filter out the partial and deleted state evals
            activeHQL = " and eval.state <> :partialState and eval.state <> :deletedState ";
            params.put("partialState", EvalConstants.EVALUATION_STATE_PARTIAL);
            params.put("deletedState", EvalConstants.EVALUATION_STATE_DELETED);
         }
         String hql = "select eval from EvalEvaluation as eval " +
         " where 1=1 " + activeHQL + groupsHQL + 
         " order by eval.dueDate, eval.title, eval.id";
         evals = executeHqlQuery(hql, params, 0, 0);
         Collections.sort(evals, new ComparatorsUtils.EvaluationDateTitleIdComparator());
      }
      return evals;

   }


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.dao.EvaluationDao#getEvaluationsForOwnerAndGroups(java.lang.String, java.lang.String[], java.util.Date, int)
    */
   @SuppressWarnings("unchecked")
   public List<EvalEvaluation> getEvaluationsForOwnerAndGroups(String userId,
         String[] evalGroupIds, Date recentClosedDate, int startResult, int maxResults) {
      Map<String, Object> params = new HashMap<String, Object>();

      String recentHQL = "";
      if (recentClosedDate != null) {
         recentHQL = " and eval.stopDate >= :recentClosedDate ";
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
      String stateHQL = " and eval.state <> :partialState and eval.state <> :deletedState ";
      params.put("partialState", EvalConstants.EVALUATION_STATE_PARTIAL);
      params.put("deletedState", EvalConstants.EVALUATION_STATE_DELETED);

      List<EvalEvaluation> evals = null;
      String hql = "select eval from EvalEvaluation as eval " 
            + " where 1=1 " + stateHQL + recentHQL + ownerGroupHQL 
            + " order by eval.stopDate, eval.title, eval.id";
      evals = executeHqlQuery(hql, params, startResult, maxResults);
      Collections.sort(evals, new ComparatorsUtils.EvaluationDateTitleIdComparator());
      return evals;
   }


   public List<EvalAnswer> getAnswers(Long itemId, Long evalId, String[] evalGroupIds) {
      Map<String, Object> params = new HashMap<String, Object>();
      String groupsHQL = "";
      if (evalGroupIds != null && evalGroupIds.length > 0) {
         groupsHQL = " and ansswerresp.evalGroupId in (:evalGroupIds) ";
         params.put("evalGroupIds", evalGroupIds);
      }
      params.put("itemId", itemId);
      params.put("evalId", evalId);
      String hql = "select answer from EvalAnswer as answer join answer.response as ansswerresp "
         + "where ansswerresp.evaluation.id = :evalId and ansswerresp.endTime is not null " + groupsHQL
         + "and answer.item.id = :itemId order by ansswerresp.id";
      // TODO optimize this once we are using a newer version of hibernate that supports "with"

      // replaced with a join
      // String hql = "from EvalAnswer as answer where answer.item.id = :itemId and
      // answer.response.id in " +
      // "(select response.id from EvalResponse as response where response.evaluation.id = :evalId "
      // +
      // groupsHQL + " order by response.id)";

      List<?> things = executeHqlQuery(hql, params, 0, 0);
      List<EvalAnswer> results = new ArrayList<EvalAnswer>();
      for (Object object : things) {
         results.add((EvalAnswer) object);
      }
      return results;
   }

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


   public List<EvalTemplateItem> getTemplateItemsByTemplate(Long templateId, String[] nodeIds,
         String[] instructorIds, String[] groupIds) {
      return getTemplateItemsByTemplates(new Long[] {templateId}, nodeIds, instructorIds, groupIds);
   }


   public List<EvalTemplateItem> getTemplateItemsByEvaluation(Long evalId, String[] nodeIds, String[] instructorIds, String[] groupIds) {
      List<Long> templateIds = getTemplateIdsForEvaluation(evalId);
      return getTemplateItemsByTemplates(templateIds.toArray(new Long[] {}), nodeIds, instructorIds, groupIds);
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

   @SuppressWarnings("unchecked")
   public String getNodeIdForEvalGroup(String evalGroupId) {
      String hql = "select egn.nodeId from EvalGroupNodes egn where " +
      "? in elements(egn.evalGroups) order by egn.nodeId";
      String[] params = new String[] {evalGroupId};
      List<String> l = getHibernateTemplate().find(hql, params);
      if (l.isEmpty()) {
         return null;
      }
      return (String) l.get(0);
   }

   @SuppressWarnings("unchecked")
   public List<Long> getTemplateIdsForEvaluation(Long evaluationId) {
      String hql = "select eval.template.id, eval.addedTemplate.id from EvalEvaluation eval where eval.id = ?";
      Object[] params = new Object[] {evaluationId};
      List<Long> l = new ArrayList<Long>();
      List<Object[]> results = getHibernateTemplate().find(hql, params);
      if (!results.isEmpty() && results.get(0) != null) {
         Object[] stuff = results.get(0);
         if (stuff[0] != null)
            l.add( (Long) stuff[0]);
         if (stuff[1] != null)
            l.add( (Long) stuff[1]);
      }
      return l;
   }


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



   // LOCKING METHODS

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.evaluation.dao.EvaluationDao#lockScale(org.sakaiproject.evaluation.model.EvalScale,
    *      java.lang.Boolean)
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

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.evaluation.dao.EvaluationDao#lockItem(org.sakaiproject.evaluation.model.EvalItem,
    *      java.lang.Boolean)
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

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.evaluation.dao.EvaluationDao#lockTemplate(org.sakaiproject.evaluation.model.EvalTemplate,
    *      java.lang.Boolean)
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

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.evaluation.dao.EvaluationDao#lockEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)
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

            EvalTemplate addedTemplate = evaluation.getAddedTemplate();
            if (addedTemplate != null && !addedTemplate.getLocked().booleanValue()) {
               lockTemplate(addedTemplate, Boolean.TRUE);
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
            if (evaluation.getAddedTemplate() != null) {
               lockTemplate(evaluation.getAddedTemplate(), Boolean.FALSE);
            }

            return true;
         }
      }
   }

   // IN_USE checks

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.evaluation.dao.EvaluationDao#isUsedScale(java.lang.Long)
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
    * @see org.sakaiproject.evaluation.dao.EvaluationDao#isUsedItem(java.lang.Long)
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
    * @see org.sakaiproject.evaluation.dao.EvaluationDao#isUsedTemplate(java.lang.Long)
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
      Query query = getSession().createQuery(hql);
      query.setFirstResult(start);
      if (limit > 0) {
         query.setMaxResults(limit);
      }
      setParameters(query, params);
      log.debug("HQL query:" + query.getQueryString());
      return query.list();
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

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.dao.EvaluationDao#lockAndExecuteRunnable(java.lang.String, java.lang.String, java.lang.Runnable)
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
