/**
 * $Id: EvalAssignsLogicImpl.java 1000 Dec 28, 2006 10:07:31 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * EvalAssignsLogicImpl.java - evaluation - Dec 28, 2006 10:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

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
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.utils.ArrayUtils;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;


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

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalSecurityChecks securityChecks;
   public void setSecurityChecks(EvalSecurityChecks securityChecks) {
      this.securityChecks = securityChecks;
   }


   // for scheduleReminder and checking if reminder already scheduled (this should happen in the same method)
   private EvalJobLogic evalJobLogic;
   public void setEvalJobLogic(EvalJobLogic evalJobLogic) {
      this.evalJobLogic = evalJobLogic;
   }

   // for sendEvalAvailableGroupNotification, this should be taken care of by a central method
   private EvalEmailsLogic emails;
   public void setEmails(EvalEmailsLogic emails) {
      this.emails = emails;
   }


   // INIT method
   public void init() {
      log.debug("Init");
   }



   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#saveAssignContext(org.sakaiproject.evaluation.model.EvalAssignContext, java.lang.String)
    */
   public void saveAssignGroup(EvalAssignGroup assignGroup, String userId) {
      log.debug("userId: " + userId + ", evalGroupId: " + assignGroup.getEvalGroupId());

      // set the date modified
      assignGroup.setLastModified( new Date() );

      EvalEvaluation eval = assignGroup.getEvaluation();
      if (eval == null || eval.getId() == null) {
         throw new IllegalStateException("Evaluation (" + eval.getId() + ") is not set or not saved for assignContext (" + 
               assignGroup.getId() + "), evalgroupId: " + assignGroup.getEvalGroupId() );
      }

      if (assignGroup.getId() == null) {
         // creating new AC
         if (securityChecks.checkCreateAssignGroup(userId, eval)) {
            // check for duplicate AC first
            if ( checkRemoveDuplicateAssignGroup(assignGroup) ) {
               throw new IllegalStateException("Duplicate mapping error, there is already an AC that defines a link from evalGroupId: " + 
                     assignGroup.getEvalGroupId() + " to eval: " + eval.getId());
            }

            dao.save(assignGroup);
            log.info("User ("+userId+") created a new AC ("+assignGroup.getId()+"), " +
                  "linked evalGroupId ("+assignGroup.getEvalGroupId()+") with eval ("+eval.getId()+")");
         }
      } else {
         // updating an existing AC

         // fetch the existing AC out of the DB to compare it
         EvalAssignGroup existingAC = (EvalAssignGroup) dao.findById(EvalAssignGroup.class, assignGroup.getId());
         //log.info("AZQ: current AC("+existingAC.getId()+"): ctxt:" + existingAC.getContext() + ", eval:" + existingAC.getEvaluation().getId());

         // check the user control permissions
         if (! securityChecks.checkControlAssignGroup(userId, assignGroup) ) {
            throw new SecurityException("User ("+userId+") attempted to update existing AC ("+existingAC.getId()+") without permissions");
         }

         // cannot change the evaluation or evalGroupId so fail if they have been changed
         if (! existingAC.getEvalGroupId().equals(assignGroup.getEvalGroupId())) {
            throw new IllegalArgumentException("Cannot update evalGroupId ("+assignGroup.getEvalGroupId()+
                  ") for an existing AC, evalGroupId ("+existingAC.getEvalGroupId()+")");
         } else if (! existingAC.getEvaluation().getId().equals(eval.getId())) {
            throw new IllegalArgumentException("Cannot update eval ("+eval.getId()+
                  ") for an existing AC, eval ("+existingAC.getEvaluation().getId()+")");
         }

         // fill in defaults
         if (assignGroup.getInstructorApproval() == null) {
            if ( EvalConstants.INSTRUCTOR_OPT_IN.equals(eval.getInstructorOpt()) ) {
               assignGroup.setInstructorApproval( Boolean.FALSE );
            } else {
               assignGroup.setInstructorApproval( Boolean.TRUE );
            }
         }

         /* if a late instructor opt-in, notify students in this group that an evaluation is available,
          * and schedule a reminder if there isn't a reminder going to all groups already scheduled
          */
         if(EvalConstants.INSTRUCTOR_OPT_IN.equals(eval.getInstructorOpt()) && 
               assignGroup.getInstructorApproval().booleanValue() && 
               assignGroup.getEvaluation().getStartDate().before(new Date())) {
            // FIXME Everything in this if statement should be factored out into a method by itself
            emails.sendEvalAvailableGroupNotification(assignGroup.getEvaluation().getId(), assignGroup.getEvalGroupId());
            if(!evalJobLogic.isJobTypeScheduled(assignGroup.getEvaluation().getId(), EvalConstants.JOB_TYPE_REMINDER)) {
               //we need to also schedule a reminder
               evalJobLogic.scheduleReminder(assignGroup.getEvaluation().getId());
            }
         }

         if (assignGroup.getInstructorsViewResults() == null) {
            if (eval.getInstructorsDate() != null) {
               assignGroup.setInstructorsViewResults( Boolean.TRUE );
            } else {
               assignGroup.setInstructorsViewResults( Boolean.FALSE );
            }
         }
         if (assignGroup.getStudentsViewResults() == null) {
            if (eval.getStudentsDate() != null) {
               assignGroup.setStudentsViewResults( Boolean.TRUE );
            } else {
               assignGroup.setStudentsViewResults( Boolean.FALSE );
            }
         }

         // allow any other changes
         dao.save(assignGroup);
         log.info("User ("+userId+") updated existing AC ("+assignGroup.getId()+") properties");
      }
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#deleteAssignGroup(java.lang.Long, java.lang.String)
    */
   public void deleteAssignGroup(Long assignGroupId, String userId) {
      log.debug("userId: " + userId + ", assignGroupId: " + assignGroupId);

      // get AC
      EvalAssignGroup assignGroup = evaluationService.getAssignGroupById(assignGroupId);
      if (assignGroup == null) {
         throw new IllegalArgumentException("Cannot find assign evalGroupId with this id: " + assignGroupId);
      }

      EvalEvaluation eval = evaluationService.getEvaluationById(assignGroup.getEvaluation().getId());
      if (eval == null) {
         throw new IllegalArgumentException("Cannot find evaluation with this id: " + assignGroup.getEvaluation().getId());         
      }

      if ( securityChecks.checkRemoveAssignGroup(userId, assignGroup, eval) ) {
         dao.delete(assignGroup);
         log.info("User ("+userId+") deleted existing assign group ("+assignGroup.getId()+")");
         return;
      }

      // should not get here so die if we do
      throw new RuntimeException("User ("+userId+") could NOT delete assign group ("+assignGroup.getId()+")");
   }
   
	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#getEvalgroupByEid(java.lang.String)
	 */
   @SuppressWarnings("unchecked")
   public EvalAssignGroup getAssignGroupByEid(String eid) {
      return evaluationService.getAssignGroupByEid(eid);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#getAssignGroupById(java.lang.Long)
    */
   public EvalAssignGroup getAssignGroupById(Long assignGroupId) {
      return evaluationService.getAssignGroupById(assignGroupId);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#getAssignGroupId(java.lang.Long, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public Long getAssignGroupId(Long evaluationId, String evalGroupId) {
      return evaluationService.getAssignGroupId(evaluationId, evalGroupId);
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
      EvalEvaluation eval = getEvaluationOrFail(evaluationId);

      // check if this evaluation can be modified
      String userId = externalLogic.getCurrentUserId();
      if (securityChecks.checkCreateAssignGroup(userId, eval)) {

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
            if (evaluationService.canDeleteAssignGroup(userId, evalAssignHierarchy.getId())) {
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
               if (evaluationService.canDeleteAssignGroup(userId, evalAssignGroup.getId())) {
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
      return evaluationService.getAssignHierarchyByEval(evaluationId);
   }

   public EvalAssignHierarchy getAssignHierarchyById(Long assignHierarchyId) {
      return evaluationService.getAssignHierarchyById(assignHierarchyId);
   }



   // PERMISSIONS

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#canCreateAssignEval(java.lang.String, java.lang.Long)
    */
   public boolean canCreateAssignEval(String userId, Long evaluationId) {
      return evaluationService.canCreateAssignEval(userId, evaluationId);
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalAssignsLogic#canDeleteAssignGroup(java.lang.String, java.lang.Long)
    */
   public boolean canDeleteAssignGroup(String userId, Long assignGroupId) {
      return evaluationService.canDeleteAssignGroup(userId, assignGroupId);
   }

   // PRIVATE METHODS

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


   /**
    * @param evaluationId
    * @return
    */
   private EvalEvaluation getEvaluationOrFail(Long evaluationId) {
      EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
      if (eval == null) {
         throw new IllegalArgumentException("Invalid eval id, cannot find evaluation with this id: " + evaluationId);
      }
      return eval;
   }

}
