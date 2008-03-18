/**
 * $Id$
 * $URL$
 * AdhocSupportLogicImplTest.java - evaluation - Mar 5, 2008 4:30:07 PM - azeckoski
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvalAdhocSupportImpl;
import org.sakaiproject.evaluation.logic.BaseTestEvalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;




/**
 * Test storing and retrieving adhoc users and groups
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalAdhocSupportImplTest extends BaseTestEvalLogic {

   EvalAdhocSupportImpl adhocSupportLogic;

   @Override
   // run this before each test starts
   protected void onSetUpBeforeTransaction() throws Exception {
      super.onSetUpBeforeTransaction();

      // load up any other needed spring beans
      EvalSettings settings = (EvalSettings) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalSettings");
      if (settings == null) {
         throw new NullPointerException("EvalSettings could not be retrieved from spring context");
      }

      // setup the mock objects if needed

      // create and setup the object to be tested
      adhocSupportLogic = new EvalAdhocSupportImpl();
      adhocSupportLogic.setDao(evaluationDao);
      adhocSupportLogic.setSettings(settings);

      // enable these for the tests
      settings.set(EvalSettings.ENABLE_ADHOC_GROUPS, true);
      settings.set(EvalSettings.ENABLE_ADHOC_USERS, true);

   }


   /**
    * ADD unit tests below here, use testMethod as the name of the unit test,
    * Note that if a method is overloaded you should include the arguments in the
    * test name like so: testMethodClassInt (for method(Class, int);
    */

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.EvalAdhocSupportImpl#getAdhocUserById(java.lang.Long)}.
    */
   public void testGetAdhocUserById() {
      EvalAdhocUser user = null;

      user = adhocSupportLogic.getAdhocUserById(etdl.user1.getId());
      assertNotNull(user);
      assertEquals(user.getUserId(), etdl.user1.getUserId());

      user = adhocSupportLogic.getAdhocUserById(EvalTestDataLoad.INVALID_LONG_ID);
      assertNull(user);

      user = adhocSupportLogic.getAdhocUserById(null);
      assertNull(user);
   }

   public void testGetAdhocUserByEmail() {
      EvalAdhocUser user = null;

      user = adhocSupportLogic.getAdhocUserByEmail(etdl.user1.getEmail());
      assertNotNull(user);
      assertEquals(etdl.user1.getId(), user.getId());
      assertEquals(etdl.user1.getEmail(), user.getEmail());

      user = adhocSupportLogic.getAdhocUserByEmail(EvalTestDataLoad.INVALID_CONSTANT_STRING);
      assertNull(user);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.EvalAdhocSupportImpl#getAdhocUsersByIds(java.lang.Long[])}.
    */
   public void testGetAdhocUsersByIds() {
      List<EvalAdhocUser> l = null;
      List<Long> ids = null;

      l = adhocSupportLogic.getAdhocUsersByIds(new Long[] {etdl.user1.getId(), etdl.user2.getId()});
      assertNotNull(l);
      assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);      
      assertTrue(ids.contains(etdl.user1.getId()));
      assertTrue(ids.contains(etdl.user2.getId()));

      l = adhocSupportLogic.getAdhocUsersByIds(new Long[] {etdl.user1.getId(), EvalTestDataLoad.INVALID_LONG_ID});
      assertNotNull(l);
      assertEquals(1, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains(etdl.user1.getId()));

      // get no users with empty
      l = adhocSupportLogic.getAdhocUsersByIds(new Long[] {});
      assertNotNull(l);
      assertEquals(0, l.size());

      // get all users with a null
      l = adhocSupportLogic.getAdhocUsersByIds(null);
      assertNotNull(l);
      assertEquals(3, l.size());

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.EvalAdhocSupportImpl#getAdhocUsersByUserIds(java.lang.String[])}.
    */
   public void testGetAdhocUsersByUserIds() {
      Map<String, EvalAdhocUser> m = null;
      List<Long> ids = null;
      Set<String> userIds = null;

      // first try with 2 internal users
      m = adhocSupportLogic.getAdhocUsersByUserIds( new String[] {etdl.user1.getUserId(), etdl.user3.getUserId()} );
      assertNotNull(m);
      assertEquals(2, m.size());
      ids = EvalTestDataLoad.makeIdList(m.values());
      assertTrue(ids.contains(etdl.user1.getId()));
      assertTrue(ids.contains(etdl.user3.getId()));
      userIds = m.keySet();
      assertNotNull(userIds);
      assertTrue(userIds.contains(etdl.user1.getUserId()));
      assertTrue(userIds.contains(etdl.user3.getUserId()));

      // mix of internal and external
      m = adhocSupportLogic.getAdhocUsersByUserIds( new String[] {etdl.user1.getUserId(), EvalTestDataLoad.USER_ID} );
      assertNotNull(m);
      assertEquals(1, m.size());
      ids = EvalTestDataLoad.makeIdList(m.values());
      assertTrue(ids.contains(etdl.user1.getId()));

      // only external
      m = adhocSupportLogic.getAdhocUsersByUserIds( new String[] {EvalTestDataLoad.USER_ID, EvalTestDataLoad.STUDENT_USER_ID} );
      assertNotNull(m);
      assertEquals(0, m.size());

      // empty array
      m = adhocSupportLogic.getAdhocUsersByUserIds( new String[] {} );
      assertNotNull(m);
      assertEquals(0, m.size());

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.EvalAdhocSupportImpl#saveAdhocUser(org.sakaiproject.evaluation.model.EvalAdhocUser, java.lang.String)}.
    */
   public void testSaveAdhocUser() {

      EvalAdhocUser user1 = new EvalAdhocUser(EvalTestDataLoad.MAINT_USER_ID, "aaron@caret.cam.ac.uk", null, "Aaron", null);
      adhocSupportLogic.saveAdhocUser(user1);
      assertNotNull( user1.getId() );
      assertNotNull( user1.getType() );
      assertNotNull( user1.getEmail() );
      assertNotNull( user1.getLastModified() );

      // check that defaults work
      EvalAdhocUser user2 = new EvalAdhocUser(EvalTestDataLoad.ADMIN_USER_ID, "billy@caret.cam.ac.uk");
      adhocSupportLogic.saveAdhocUser(user2);
      assertNotNull( user2.getId() );
      assertNotNull( user2.getType() );
      assertNotNull( user2.getEmail() );
      assertNotNull( user2.getLastModified() );

      // check that trying to save a user that already exists simply sets the input object to the persisted user
      EvalAdhocUser user3 = new EvalAdhocUser(EvalTestDataLoad.MAINT_USER_ID, "aaron@caret.cam.ac.uk", null, "Aaron Z", null);
      adhocSupportLogic.saveAdhocUser(user3);
      assertNotNull( user3.getId() );
      assertEquals(user3.getId(), user1.getId());
      assertEquals(user3.getUserId(), user1.getUserId());

      // test saving an existing user
      EvalAdhocUser existing = adhocSupportLogic.getAdhocUserById(etdl.user2.getId());
      existing.setDisplayName("Jimmy Joe Bob");
      adhocSupportLogic.saveAdhocUser(existing);

      EvalAdhocUser check = adhocSupportLogic.getAdhocUserById(etdl.user2.getId());
      assertEquals("Jimmy Joe Bob", check.getDisplayName());

      // test trying to save an existing user with a new email that matches another existing one,
      // the email address should not actually end up changing
      user2.setEmail( user1.getEmail() );
      user2.setUsername( "aaron" );
      adhocSupportLogic.saveAdhocUser(user2);
      
      // exception if try to save without setting the email
      try {
         EvalAdhocUser user4 = new EvalAdhocUser(EvalTestDataLoad.MAINT_USER_ID, null);
         adhocSupportLogic.saveAdhocUser(user4);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.EvalAdhocSupportImpl#getAdhocGroupById(java.lang.Long)}.
    */
   public void testGetAdhocGroupById() {
      EvalAdhocGroup group = null;

      group = adhocSupportLogic.getAdhocGroupById(etdl.group1.getId());
      assertNotNull(group);
      assertEquals(group.getEvalGroupId(), etdl.group1.getEvalGroupId());

      group = adhocSupportLogic.getAdhocGroupById(EvalTestDataLoad.INVALID_LONG_ID);
      assertNull(group);

      group = adhocSupportLogic.getAdhocGroupById(null);
      assertNull(group);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.EvalAdhocSupportImpl#getAdhocGroupsForOwner(java.lang.String)}.
    */
   public void testGetAdhocGroupsForOwner() {
      List<EvalAdhocGroup> l = null;
      EvalAdhocGroup group = null;

      l = adhocSupportLogic.getAdhocGroupsForOwner(EvalTestDataLoad.MAINT_USER_ID);
      assertNotNull(l);
      assertEquals(1, l.size());
      group = l.get(0);
      assertNotNull(group);
      assertEquals(etdl.group2.getId(), group.getId());

      l = adhocSupportLogic.getAdhocGroupsForOwner(EvalTestDataLoad.ADMIN_USER_ID);
      assertNotNull(l);
      assertEquals(1, l.size());
      group = l.get(0);
      assertNotNull(group);
      assertEquals(etdl.group1.getId(), group.getId());

      l = adhocSupportLogic.getAdhocGroupsForOwner(EvalTestDataLoad.USER_ID);
      assertNotNull(l);
      assertEquals(0, l.size());
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.EvalAdhocSupportImpl#saveAdhocGroup(org.sakaiproject.evaluation.model.EvalAdhocGroup, java.lang.String)}.
    */
   public void testSaveAdhocGroup() {

      EvalAdhocGroup group1 = new EvalAdhocGroup(EvalTestDataLoad.MAINT_USER_ID, "new group");
      adhocSupportLogic.saveAdhocGroup(group1);
      assertNotNull( group1.getId() );
      assertNotNull( group1.getOwner() );
      assertNotNull( group1.getTitle() );
      assertNotNull( group1.getLastModified() );

      EvalAdhocGroup group2 = new EvalAdhocGroup(EvalTestDataLoad.MAINT_USER_ID, "another new group", 
            new String[] { EvalTestDataLoad.STUDENT_USER_ID, EvalTestDataLoad.USER_ID }, null);
      adhocSupportLogic.saveAdhocGroup(group2);
      assertNotNull( group2.getId() );
      assertNotNull( group2.getOwner() );
      assertNotNull( group2.getTitle() );
      assertNotNull( group2.getLastModified() );

      // test retrieving a group and editing it
      EvalAdhocGroup existing = adhocSupportLogic.getAdhocGroupById(etdl.group2.getId());
      existing.setTitle("new title 2");
      adhocSupportLogic.saveAdhocGroup(existing);

      EvalAdhocGroup check = adhocSupportLogic.getAdhocGroupById(etdl.group2.getId());
      assertEquals("new title 2", check.getTitle());

      // exception if try to save without setting the title
      try {
         EvalAdhocGroup group5 = new EvalAdhocGroup(EvalTestDataLoad.MAINT_USER_ID, null);
         adhocSupportLogic.saveAdhocGroup(group5);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }

   }

   public void testGetEvalAdhocGroupsByUserAndPerm() {
      List<EvalAdhocGroup> l = null;
      List<Long> ids = null;

      l = adhocSupportLogic.getAdhocGroupsByUserAndPerm(etdl.user3.getUserId(), EvalConstants.PERM_TAKE_EVALUATION);
      assertNotNull(l);
      assertEquals(1, l.size());
      assertEquals(etdl.group2.getId(), l.get(0).getId());

      l = adhocSupportLogic.getAdhocGroupsByUserAndPerm(EvalTestDataLoad.STUDENT_USER_ID, EvalConstants.PERM_TAKE_EVALUATION);
      assertNotNull(l);
      assertEquals(1, l.size());
      assertEquals(etdl.group1.getId(), l.get(0).getId());

      l = adhocSupportLogic.getAdhocGroupsByUserAndPerm(etdl.user1.getUserId(), EvalConstants.PERM_TAKE_EVALUATION);
      assertNotNull(l);
      assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains(etdl.group1.getId()));
      assertTrue(ids.contains(etdl.group2.getId()));

      l = adhocSupportLogic.getAdhocGroupsByUserAndPerm(etdl.user2.getUserId(), EvalConstants.PERM_TAKE_EVALUATION);
      assertNotNull(l);
      assertEquals(0, l.size());

   }

   public void testIsUserAllowedInAdhocGroup() {
      boolean allowed = false;

      allowed = adhocSupportLogic.isUserAllowedInAdhocGroup(EvalTestDataLoad.USER_ID, EvalConstants.PERM_TAKE_EVALUATION, etdl.group2.getEvalGroupId());
      assertTrue(allowed);

      allowed = adhocSupportLogic.isUserAllowedInAdhocGroup(EvalTestDataLoad.USER_ID, EvalConstants.PERM_BE_EVALUATED, etdl.group2.getEvalGroupId());
      assertFalse(allowed);

      allowed = adhocSupportLogic.isUserAllowedInAdhocGroup(etdl.user1.getUserId(), EvalConstants.PERM_TAKE_EVALUATION, etdl.group1.getEvalGroupId());
      assertTrue(allowed);

      allowed = adhocSupportLogic.isUserAllowedInAdhocGroup(etdl.user1.getUserId(), EvalConstants.PERM_BE_EVALUATED, etdl.group1.getEvalGroupId());
      assertFalse(allowed);

      allowed = adhocSupportLogic.isUserAllowedInAdhocGroup(etdl.user2.getUserId(), EvalConstants.PERM_TAKE_EVALUATION, etdl.group1.getEvalGroupId());
      assertFalse(allowed);

      allowed = adhocSupportLogic.isUserAllowedInAdhocGroup(etdl.user2.getUserId(), EvalConstants.PERM_BE_EVALUATED, etdl.group1.getEvalGroupId());
      assertFalse(allowed);
   }

}
