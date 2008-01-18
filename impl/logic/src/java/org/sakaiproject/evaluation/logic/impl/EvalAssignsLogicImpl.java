/******************************************************************************
 * EvalAssignsLogicImpl.java - created by aaronz@vt.edu on Dec 28, 2006
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalAssignsLogic;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.utils.ArrayUtils;
import org.sakaiproject.evaluation.logic.utils.EvalUtils;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.genericdao.api.finders.ByPropsFinder;


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

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private ExternalHierarchyLogic hierarchyLogic;
   public void setHierarchyLogic(ExternalHierarchyLogic hierarchyLogic) {
      this.hierarchyLogic = hierarchyLogic;
   }

   private EvalJobLogic evalJobLogic;
   public void setEvalJobLogic(EvalJobLogic evalJobLogic) {
      this.evalJobLogic = evalJobLogic;
   }

   // INIT method
   public void init() {
      log.debug("Init");
   }



   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#saveAssignContext(org.sakaiproject.evaluation.model.EvalAssignContext, java.lang.String)
    */
   public void saveAssignGroup(EvalAssignGroup assignContext, String userId) {
      log.debug("userId: " + userId + ", evalGroupId: " + assignContext.getEvalGroupId());

      // set the date modified
      assignContext.setLastModified( new Date() );

      EvalEvaluation eval = assignContext.getEvaluation();
      if (eval == null || eval.getId() == null) {
         throw new IllegalStateException("Evaluation (" + eval.getId() + ") is not set or not saved for assignContext (" + 
               assignContext.getId() + "), evalgroupId: " + assignContext.getEvalGroupId() );
      }

      if (assignContext.getId() == null) {
         // creating new AC
         if (checkCreateAssignGroup(userId, eval)) {
            // check for duplicate AC first
            if ( checkRemoveDuplicateAssignGroup(assignContext) ) {
               throw new IllegalStateException("Duplicate mapping error, there is already an AC that defines a link from evalGroupId: " + 
                     assignContext.getEvalGroupId() + " to eval: " + eval.getId());
            }

            dao.save(assignContext);
            log.info("User ("+userId+") created a new AC ("+assignContext.getId()+"), " +
                  "linked evalGroupId ("+assignContext.getEvalGroupId()+") with eval ("+eval.getId()+")");
         }
      } else {
         // updating an existing AC

         // fetch the existing AC out of the DB to compare it
         EvalAssignGroup existingAC = (EvalAssignGroup) dao.findById(EvalAssignGroup.class, assignContext.getId());
         //log.info("AZQ: current AC("+existingAC.getId()+"): ctxt:" + existingAC.getContext() + ", eval:" + existingAC.getEvaluation().getId());

         // check the user control permissions
         if (! checkControlAssignGroup(userId, assignContext) ) {
            throw new SecurityException("User ("+userId+") attempted to update existing AC ("+existingAC.getId()+") without permissions");
         }

         // cannot change the evaluation or evalGroupId so fail if they have been changed
         if (! existingAC.getEvalGroupId().equals(assignContext.getEvalGroupId())) {
            throw new IllegalArgumentException("Cannot update evalGroupId ("+assignContext.getEvalGroupId()+
                  ") for an existing AC, evalGroupId ("+existingAC.getEvalGroupId()+")");
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
         
         /* if a late instructor opt-in, notify students in this group that an evaluation is available,
          * and schedule a reminder if there isn't a reminder going to all groups already scheduled
          */
         if(EvalConstants.INSTRUCTOR_OPT_IN.equals(eval.getInstructorOpt()) && 
               assignContext.getInstructorApproval().booleanValue() && 
               assignContext.getEvaluation().getStartDate().before(new Date())) {
        	 evalJobLogic.scheduleLateOptInNotification(assignContext.getEvaluation().getId(), assignContext.getEvalGroupId());
            if(!evalJobLogic.isJobTypeScheduled(assignContext.getEvaluation().getId(), EvalConstants.JOB_TYPE_REMINDER)) {
               //we need to also schedule a reminder
               evalJobLogic.scheduleReminder(assignContext.getEvaluation().getId());
            }
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
    * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#deleteAssignGroup(java.lang.Long, java.lang.String)
    */
   public void deleteAssignGroup(Long assignGroupId, String userId) {
      log.debug("userId: " + userId + ", assignGroupId: " + assignGroupId);

      // get AC
      EvalAssignGroup assignGroup = (EvalAssignGroup) dao.findById(EvalAssignGroup.class, assignGroupId);
      if (assignGroup == null) {
         throw new IllegalArgumentException("Cannot find assign evalGroupId with this id: " + assignGroupId);
      }

      if ( checkRemoveAssignGroup(userId, assignGroup) ) {
         dao.delete(assignGroup);
         log.info("User ("+userId+") deleted existing AC ("+assignGroup.getId()+")");
         return;
      }

      // should not get here so die if we do
      throw new RuntimeException("User ("+userId+") could NOT delete AC ("+assignGroup.getId()+")");
   }
   
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#getEvalgroupByEid(java.lang.String)
	 */
	public EvalAssignGroup getAssignGroupByEid(String eid) {
		List evalAssignGroups = new ArrayList();
		EvalAssignGroup group = null;
		if(eid != null) {
			evalAssignGroups = (List)dao.findByProperties(EvalAssignGroup.class, new String[] {"eid"}, new Object[] {eid});
			if(!evalAssignGroups.isEmpty())
				group = (EvalAssignGroup)evalAssignGroups.get(0);
		}
		return group;
	}

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#getAssignGroupById(java.lang.Long)
    */
   public EvalAssignGroup getAssignGroupById(Long assignGroupId) {
      log.debug("assignGroupId: " + assignGroupId);
      return (EvalAssignGroup) dao.findById(EvalAssignGroup.class, assignGroupId);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#getAssignGroupId(java.lang.Long, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public Long getAssignGroupId(Long evaluationId, String evalGroupId) {
      log.debug("evaluationId: " + evaluationId + ", evalGroupId: " + evalGroupId);
      List<EvalAssignGroup> l = dao.findByProperties(EvalAssignGroup.class, 
            new String[] {"evaluation.id", "evalGroupId"}, 
            new Object[] {evaluationId, evalGroupId} );
      if (l.size() == 1) {
         EvalAssignGroup assignGroup = (EvalAssignGroup) l.get(0);
         return assignGroup.getId();
      }
      return null;
   }

   /**
    * Retrieve the complete set of eval assign groups for this evaluation
    * @param evaluationId
    * @return
    */
   @SuppressWarnings("unchecked")
   private List<EvalAssignGroup> getEvaluationAssignGroups(Long evaluationId) {
      // get all the evalGroupIds for the given eval ids in one storage call
      List<EvalAssignGroup> l = new ArrayList<EvalAssignGroup>();
      l = dao.findByProperties(EvalAssignGroup.class,
            new String[] {"evaluation.id"}, 
            new Object[] {evaluationId} );
      return l;
   }

   // HIERARCHY


   public List<EvalAssignHierarchy> addEvalAssignments(Long evaluationId, String[] nodeIds, String[] evalGroupIds) {

      // get the evaluation
      EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
      if (eval == null) {
         throw new IllegalArgumentException("Invalid eval id, cannot find evaluation with this id: " + evaluationId);
      }

      // check if this evaluation can be modified
      String userId = externalLogic.getCurrentUserId();
      if (checkCreateAssignGroup(userId, eval)) {

         // first we have to get all the assigned hierarchy nodes for this eval
         Set<String> nodeIdsSet = new HashSet<String>();
         Set<String> currentNodeIds = new HashSet<String>();

         List<EvalAssignHierarchy> current = getAssignHierarchyByEval(evaluationId);
         for (EvalAssignHierarchy evalAssignHierarchy : current) {
            currentNodeIds.add(evalAssignHierarchy.getNodeId());
         }

         for (int i = 0; i < nodeIds.length; i++) {
            nodeIdsSet.add(nodeIds[i]);
         }

         // then remove the duplicates so we end up with the filtered list to only new ones
         nodeIdsSet.removeAll(currentNodeIds);
         nodeIds = nodeIdsSet.toArray(nodeIds);

         // now we need to create all the persistent hierarchy assignment objects
         Set<EvalAssignHierarchy> nodeAssignments = new HashSet<EvalAssignHierarchy>();
         for (String nodeId : nodeIdsSet) {
            EvalAssignHierarchy eah = new EvalAssignHierarchy(new Date(), userId, nodeId, false, true, false, eval);
            // fill in defaults and the values from the evaluation
            setDefaults(eval, eah);
            nodeAssignments.add(eah);
         }

         // next we have to get all the assigned eval groups for this eval
         Set<String> evalGroupIdsSet = new HashSet<String>();
         Set<String> currentEvalGroupIds = new HashSet<String>();

         // get the current list of assigned eval groups
         List<EvalAssignGroup> currentGroups = getEvaluationAssignGroups(evaluationId);
         for (EvalAssignGroup evalAssignGroup : currentGroups) {
            currentEvalGroupIds.add(evalAssignGroup.getEvalGroupId());
         }

         /*
          * According to the API Documentation, evalGroupIds can be null if there
          * are none to assign.
          */
         if (evalGroupIds == null) {
             evalGroupIds = new String[] {};
         }
         
         for (int i = 0; i < evalGroupIds.length; i++) {
             evalGroupIdsSet.add(evalGroupIds[i]);
         }
         
         // next we need to expand all the assigned hierarchy nodes into a massive set of eval assign groups
         Set<String> allNodeIds = new HashSet<String>();
         allNodeIds.addAll(nodeIdsSet);
         allNodeIds.addAll(currentNodeIds);
         Map<String, Set<String>> allEvalGroupIds = hierarchyLogic.getEvalGroupsForNodes( allNodeIds.toArray(nodeIds) );

         // now eliminate the evalgroupids from the evalGroupIds array which happen to be contained in the nodes,
         // this leaves us with only the group ids which are not contained in the nodes which are already assigned
         for (Set<String> egIds : allEvalGroupIds.values()) {
            evalGroupIdsSet.removeAll(egIds);
         }

         // then remove the eval groups ids which are already assigned to this eval so we only have new ones
         evalGroupIdsSet.removeAll(currentEvalGroupIds);
         evalGroupIds = evalGroupIdsSet.toArray(evalGroupIds);

         // now we need to create all the persistent group assignment objects for the new groups
         Set<EvalAssignGroup> groupAssignments = new HashSet<EvalAssignGroup>();
         groupAssignments.addAll( makeAssignGroups(eval, userId, evalGroupIdsSet, null) );

         // finally we add in the groups for all the new expanded assign groups
         for (String nodeId : nodeIdsSet) {
            if (allEvalGroupIds.containsKey(nodeId)) {
               groupAssignments.addAll( makeAssignGroups(eval, userId, allEvalGroupIds.get(nodeId), nodeId) );               
            }
         }

         // save everything at once
         dao.saveMixedSet(new Set[] {nodeAssignments, groupAssignments});
         log.info("User (" + userId + ") added nodes (" + ArrayUtils.arrayToString(nodeIds)
               + ") and groups (" + ArrayUtils.arrayToString(evalGroupIds) + ") to evaluation ("
               + evaluationId + ")");
         List<EvalAssignHierarchy> results = new ArrayList<EvalAssignHierarchy>(nodeAssignments);
         results.addAll(groupAssignments);
         return results;
      }

      // should not get here so die if we do
      throw new RuntimeException("User (" + userId
            + ") could NOT create hierarchy assignments for nodes ("
            + ArrayUtils.arrayToString(nodeIds) + ") in evaluation (" + evaluationId + ")");
   }

   /**
    * Create EvalAssignGroup objects from a set of evalGroupIds for an eval and user
    * @param eval
    * @param userId
    * @param evalGroupIdsSet
    * @param nodeId (optional), null if there is no nodeId association, otherwise set to the associated nodeId
    * @return the set with the new assignments
    */
   private Set<EvalAssignGroup> makeAssignGroups(EvalEvaluation eval, String userId, Set<String> evalGroupIdsSet, String nodeId) {
      Set<EvalAssignGroup> groupAssignments = new HashSet<EvalAssignGroup>();
      for (String evalGroupId : evalGroupIdsSet) {
         String type = EvalConstants.GROUP_TYPE_PROVIDED;
         if (evalGroupId.startsWith("/site")) {
            type = EvalConstants.GROUP_TYPE_SITE;
         }
         EvalAssignGroup eag = new EvalAssignGroup(new Date(), userId, evalGroupId, type, false, true, false, eval, nodeId);
         // fill in defaults and the values from the evaluation
         setDefaults(eval, eag);
         groupAssignments.add(eag);
      }
      return groupAssignments;
   }

   /**
    * @param eval
    * @param eah
    */
   private void setDefaults(EvalEvaluation eval, EvalAssignHierarchy eah) {
      if ( EvalConstants.INSTRUCTOR_OPT_IN.equals(eval.getInstructorOpt()) ) {
         eah.setInstructorApproval( Boolean.FALSE );
      } else {
         eah.setInstructorApproval( Boolean.TRUE );
      }
      if (eval.getInstructorsDate() != null) {
         eah.setInstructorsViewResults( Boolean.TRUE );
      } else {
         eah.setInstructorsViewResults( Boolean.FALSE );
      }
      if (eval.getStudentsDate() != null) {
         eah.setStudentsViewResults( Boolean.TRUE );
      } else {
         eah.setStudentsViewResults( Boolean.FALSE );
      }
   }


   @SuppressWarnings("unchecked")
   public void deleteAssignHierarchyNodesById(Long[] assignHierarchyIds) {
      String userId = externalLogic.getCurrentUserId();
      // get the list of hierarchy assignments
      List<EvalAssignHierarchy> l = dao.findByProperties(EvalAssignHierarchy.class,
            new String[] { "id" }, new Object[] { assignHierarchyIds });
      if (l.size() > 0) {
         Set<String> nodeIds = new HashSet<String>();         
         Long evaluationId = l.get(0).getEvaluation().getId();

         Set<EvalAssignHierarchy> eahs = new HashSet<EvalAssignHierarchy>();
         for (EvalAssignHierarchy evalAssignHierarchy : l) {
            if (checkRemoveAssignGroup(userId, evalAssignHierarchy)) {
               nodeIds.add(evalAssignHierarchy.getNodeId());
               eahs.add(evalAssignHierarchy);
            }
         }

         // now get the list of assign groups with a nodeId that matches any of these and remove those also
         List<EvalAssignGroup> eags = dao.findByProperties(EvalAssignGroup.class,
               new String[] { "evaluation.id", "nodeId" }, 
               new Object[] { evaluationId, nodeIds });
         Set<EvalAssignGroup> groups = new HashSet<EvalAssignGroup>();
         StringBuilder groupListing = new StringBuilder();
         if (eags.size() > 0) {
            for (EvalAssignGroup evalAssignGroup : groups) {
               if (checkRemoveAssignGroup(userId, evalAssignGroup)) {
                  groups.add(evalAssignGroup);
                  groupListing.append(evalAssignGroup.getEvalGroupId() + ":");
               }
            }
         }

         dao.deleteMixedSet(new Set[] {eahs, groups});
         log.info("User (" + userId + ") deleted existing hierarchy assignments ("
               + ArrayUtils.arrayToString(assignHierarchyIds) + ") and groups ("+groupListing.toString()+")");
         return;

      }
      // should not get here so die if we do
      throw new RuntimeException("User (" + userId + ") could NOT delete hierarchy assignments ("
            + ArrayUtils.arrayToString(assignHierarchyIds) + ")");
   }

   @SuppressWarnings("unchecked")
   public List<EvalAssignHierarchy> getAssignHierarchyByEval(Long evaluationId) {
      List<EvalAssignHierarchy> l = dao.findByProperties(EvalAssignHierarchy.class,
            new String[] { "evaluation.id" }, 
            new Object[] { evaluationId });
      return l;
   }

   public EvalAssignHierarchy getAssignHierarchyById(Long assignHierarchyId) {
      return (EvalAssignHierarchy) dao.findById(EvalAssignHierarchy.class, assignHierarchyId);
   }

   @SuppressWarnings("unchecked")
   public List<EvalAssignGroup> getAssignGroupsByEval(Long evaluationId) {
      List<EvalAssignGroup> l = dao.findByProperties(EvalAssignGroup.class,
            new String[] { "evaluation.id", "nodeId" }, 
            new Object[] { evaluationId, null },
            new int[] {ByPropsFinder.EQUALS, ByPropsFinder.NULL},
            new String[] {"id"});
      return l;
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
         return checkCreateAssignGroup(userId, eval);
      } catch (RuntimeException e) {
         log.info(e.getMessage());
      }
      return false;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#canDeleteAssignGroup(java.lang.String, java.lang.Long)
    */
   public boolean canDeleteAssignGroup(String userId, Long assignGroupId) {
      log.debug("userId: " + userId + ", assignGroupId: " + assignGroupId);

      // get AC
      EvalAssignGroup assignGroup = (EvalAssignGroup) dao.findById(EvalAssignGroup.class, assignGroupId);
      if (assignGroup == null) {
         throw new IllegalArgumentException("Cannot find assign evalGroupId with this id: " + assignGroupId);
      }

      try {
         return checkRemoveAssignGroup(userId, assignGroup);
      } catch (RuntimeException e) {
         log.info(e.getMessage());
      }
      return false;
   }

   // PRIVATE METHODS

   /**
    * Check if user can control this AC
    * @param userId
    * @param assignGroup
    * @return true if can, false otherwise
    */
   private boolean checkControlAssignGroup(String userId, EvalAssignHierarchy assignGroup) {
      log.debug("userId: " + userId + ", assignGroup: " + assignGroup.getId());

      // check user permissions (just owner and super at this point)
      if ( userId.equals(assignGroup.getOwner()) ||
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
   private boolean checkCreateAssignGroup(String userId, EvalEvaluation eval) {
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
            throw new SecurityException("User ("+userId+") cannot create assign evalGroupId in evaluation ("+eval.getId()+"), do not have permission");
         }
      } else {
         throw new IllegalStateException("User ("+userId+") cannot create assign evalGroupId in evaluation ("+eval.getId()+"), invalid eval state");
      }
   }

   /**
    * Check if user can remove an AC
    * @param userId
    * @param assignGroup
    * @return true if they can, throw exceptions otherwise
    */
   private boolean checkRemoveAssignGroup(String userId, EvalAssignHierarchy assignGroup) {
      log.debug("userId: " + userId + ", assignGroupId: " + assignGroup.getId());

      // get evaluation from AC
      EvalEvaluation eval = assignGroup.getEvaluation();
      String state = EvalUtils.getEvaluationState(eval);
      if (EvalConstants.EVALUATION_STATE_INQUEUE.equals(state)) {

         // check user permissions (just owner and super at this point)
         if ( checkControlAssignGroup(userId, assignGroup) ) {
            return true;
         } else {
            throw new SecurityException("User ("+userId+") cannot remove assign evalGroupId ("+assignGroup.getId()+"), do not have permission");
         }
      } else {
         throw new IllegalStateException("User ("+userId+") cannot remove this assign evalGroupId ("+assignGroup.getId()+"), invalid eval state");
      }
   }

   /**
    * Check for existing AC which matches this ones linkage
    * @param ac
    * @return true if duplicate found
    */
   @SuppressWarnings("unchecked")
   private boolean checkRemoveDuplicateAssignGroup(EvalAssignGroup ac) {
      log.debug("assignContext: " + ac.getId());

      List<EvalAssignGroup> l = dao.findByProperties(EvalAssignGroup.class, 
            new String[] {"evalGroupId", "evaluation.id"}, 
            new Object[] {ac.getEvalGroupId(), ac.getEvaluation().getId()});
      if ( (ac.getId() == null && l.size() >= 1) || 
            (ac.getId() != null && l.size() >= 2) ) {
         // there is an existing AC which does the same mapping
//       EvalAssignContext eac = (EvalAssignContext) l.get(0);
         return true;
      }
      return false;
   }


}
