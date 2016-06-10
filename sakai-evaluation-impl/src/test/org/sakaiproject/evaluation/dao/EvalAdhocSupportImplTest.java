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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.evaluation.constant.EvalConstants;
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

   // run this before each test starts
   @Override
   @Before
   public void onSetUpBeforeTransaction() throws Exception {
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
   @Test
   public void testGetAdhocUserById() {
      EvalAdhocUser user;

      user = adhocSupportLogic.getAdhocUserById(etdl.user1.getId());
      Assert.assertNotNull(user);
      Assert.assertEquals(user.getUserId(), etdl.user1.getUserId());

      user = adhocSupportLogic.getAdhocUserById(EvalTestDataLoad.INVALID_LONG_ID);
      Assert.assertNull(user);

      user = adhocSupportLogic.getAdhocUserById(null);
      Assert.assertNull(user);
   }

   @Test
   public void testGetAdhocUserByEmail() {
      EvalAdhocUser user;

      user = adhocSupportLogic.getAdhocUserByEmail(etdl.user1.getEmail());
      Assert.assertNotNull(user);
      Assert.assertEquals(etdl.user1.getId(), user.getId());
      Assert.assertEquals(etdl.user1.getEmail(), user.getEmail());

      user = adhocSupportLogic.getAdhocUserByEmail(EvalTestDataLoad.INVALID_CONSTANT_STRING);
      Assert.assertNull(user);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.EvalAdhocSupportImpl#getAdhocUsersByIds(java.lang.Long[])}.
    */
   @Test
   public void testGetAdhocUsersByIds() {
      List<EvalAdhocUser> l;
      List<Long> ids;

      l = adhocSupportLogic.getAdhocUsersByIds(new Long[] {etdl.user1.getId(), etdl.user2.getId()});
      Assert.assertNotNull(l);
      Assert.assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);      
      Assert.assertTrue(ids.contains(etdl.user1.getId()));
      Assert.assertTrue(ids.contains(etdl.user2.getId()));

      l = adhocSupportLogic.getAdhocUsersByIds(new Long[] {etdl.user1.getId(), EvalTestDataLoad.INVALID_LONG_ID});
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains(etdl.user1.getId()));

      // get no users with empty
      l = adhocSupportLogic.getAdhocUsersByIds(new Long[] {});
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

      // get all users with a null
      l = adhocSupportLogic.getAdhocUsersByIds(null);
      Assert.assertNotNull(l);
      Assert.assertEquals(3, l.size());

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.EvalAdhocSupportImpl#getAdhocUsersByUserIds(java.lang.String[])}.
    */
   @Test
   public void testGetAdhocUsersByUserIds() {
      Map<String, EvalAdhocUser> m;
      List<Long> ids;
      Set<String> userIds;

      // first try with 2 internal users
      m = adhocSupportLogic.getAdhocUsersByUserIds( new String[] {etdl.user1.getUserId(), etdl.user3.getUserId()} );
      Assert.assertNotNull(m);
      Assert.assertEquals(2, m.size());
      ids = EvalTestDataLoad.makeIdList(m.values());
      Assert.assertTrue(ids.contains(etdl.user1.getId()));
      Assert.assertTrue(ids.contains(etdl.user3.getId()));
      userIds = m.keySet();
      Assert.assertNotNull(userIds);
      Assert.assertTrue(userIds.contains(etdl.user1.getUserId()));
      Assert.assertTrue(userIds.contains(etdl.user3.getUserId()));

      // mix of internal and external
      m = adhocSupportLogic.getAdhocUsersByUserIds( new String[] {etdl.user1.getUserId(), EvalTestDataLoad.USER_ID} );
      Assert.assertNotNull(m);
      Assert.assertEquals(1, m.size());
      ids = EvalTestDataLoad.makeIdList(m.values());
      Assert.assertTrue(ids.contains(etdl.user1.getId()));

      // only external
      m = adhocSupportLogic.getAdhocUsersByUserIds( new String[] {EvalTestDataLoad.USER_ID, EvalTestDataLoad.STUDENT_USER_ID} );
      Assert.assertNotNull(m);
      Assert.assertEquals(0, m.size());

      // empty array
      m = adhocSupportLogic.getAdhocUsersByUserIds( new String[] {} );
      Assert.assertNotNull(m);
      Assert.assertEquals(0, m.size());

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.EvalAdhocSupportImpl#saveAdhocUser(org.sakaiproject.evaluation.model.EvalAdhocUser, java.lang.String)}.
    */
   @Test
   public void testSaveAdhocUser() {

      EvalAdhocUser user1 = new EvalAdhocUser(EvalTestDataLoad.MAINT_USER_ID, "aaron@caret.cam.ac.uk", null, "Aaron", null);
      adhocSupportLogic.saveAdhocUser(user1);
      Assert.assertNotNull( user1.getId() );
      Assert.assertNotNull( user1.getType() );
      Assert.assertNotNull( user1.getEmail() );
      Assert.assertNotNull( user1.getLastModified() );

      // check that defaults work
      EvalAdhocUser user2 = new EvalAdhocUser(EvalTestDataLoad.ADMIN_USER_ID, "billy@caret.cam.ac.uk");
      adhocSupportLogic.saveAdhocUser(user2);
      Assert.assertNotNull( user2.getId() );
      Assert.assertNotNull( user2.getType() );
      Assert.assertNotNull( user2.getEmail() );
      Assert.assertNotNull( user2.getLastModified() );

      // check that trying to save a user that already exists simply sets the input object to the persisted user
      EvalAdhocUser user3 = new EvalAdhocUser(EvalTestDataLoad.MAINT_USER_ID, "aaron@caret.cam.ac.uk", null, "Aaron Z", null);
      adhocSupportLogic.saveAdhocUser(user3);
      Assert.assertNotNull( user3.getId() );
      Assert.assertEquals(user3.getId(), user1.getId());
      Assert.assertEquals(user3.getUserId(), user1.getUserId());

      // test saving an existing user
      EvalAdhocUser existing = adhocSupportLogic.getAdhocUserById(etdl.user2.getId());
      existing.setDisplayName("Jimmy Joe Bob");
      adhocSupportLogic.saveAdhocUser(existing);

      EvalAdhocUser check = adhocSupportLogic.getAdhocUserById(etdl.user2.getId());
      Assert.assertEquals("Jimmy Joe Bob", check.getDisplayName());

      // test trying to save an existing user with a new email that matches another existing one,
      // the email address should not actually end up changing
      user2.setEmail( user1.getEmail() );
      user2.setUsername( "aaron" );
      adhocSupportLogic.saveAdhocUser(user2);
      
      // exception if try to save without setting the email
      try {
         EvalAdhocUser user4 = new EvalAdhocUser(EvalTestDataLoad.MAINT_USER_ID, null);
         adhocSupportLogic.saveAdhocUser(user4);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
         //Assert.fail("Exception: " + e.getMessage()); // see why Assert.failing
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.EvalAdhocSupportImpl#getAdhocGroupById(java.lang.Long)}.
    */
   @Test
   public void testGetAdhocGroupById() {
      EvalAdhocGroup group;

      group = adhocSupportLogic.getAdhocGroupById(etdl.group1.getId());
      Assert.assertNotNull(group);
      Assert.assertEquals(group.getEvalGroupId(), etdl.group1.getEvalGroupId());

      group = adhocSupportLogic.getAdhocGroupById(EvalTestDataLoad.INVALID_LONG_ID);
      Assert.assertNull(group);

      group = adhocSupportLogic.getAdhocGroupById(null);
      Assert.assertNull(group);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.EvalAdhocSupportImpl#getAdhocGroupsForOwner(java.lang.String)}.
    */
   @Test
   public void testGetAdhocGroupsForOwner() {
      List<EvalAdhocGroup> l;
      EvalAdhocGroup group;

      l = adhocSupportLogic.getAdhocGroupsForOwner(EvalTestDataLoad.MAINT_USER_ID);
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      group = l.get(0);
      Assert.assertNotNull(group);
      Assert.assertEquals(etdl.group2.getId(), group.getId());

      l = adhocSupportLogic.getAdhocGroupsForOwner(EvalTestDataLoad.ADMIN_USER_ID);
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      group = l.get(0);
      Assert.assertNotNull(group);
      Assert.assertEquals(etdl.group1.getId(), group.getId());

      l = adhocSupportLogic.getAdhocGroupsForOwner(EvalTestDataLoad.USER_ID);
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.EvalAdhocSupportImpl#saveAdhocGroup(org.sakaiproject.evaluation.model.EvalAdhocGroup, java.lang.String)}.
    */
   @Test
   public void testSaveAdhocGroup() {

      EvalAdhocGroup group1 = new EvalAdhocGroup(EvalTestDataLoad.MAINT_USER_ID, "new group");
      adhocSupportLogic.saveAdhocGroup(group1);
      Assert.assertNotNull( group1.getId() );
      Assert.assertNotNull( group1.getOwner() );
      Assert.assertNotNull( group1.getTitle() );
      Assert.assertNotNull( group1.getLastModified() );

      EvalAdhocGroup group2 = new EvalAdhocGroup(EvalTestDataLoad.MAINT_USER_ID, "another new group", 
            new String[] { EvalTestDataLoad.STUDENT_USER_ID, EvalTestDataLoad.USER_ID }, null);
      adhocSupportLogic.saveAdhocGroup(group2);
      Assert.assertNotNull( group2.getId() );
      Assert.assertNotNull( group2.getOwner() );
      Assert.assertNotNull( group2.getTitle() );
      Assert.assertNotNull( group2.getLastModified() );

      // test retrieving a group and editing it
      EvalAdhocGroup existing = adhocSupportLogic.getAdhocGroupById(etdl.group2.getId());
      existing.setTitle("new title 2");
      adhocSupportLogic.saveAdhocGroup(existing);

      EvalAdhocGroup check = adhocSupportLogic.getAdhocGroupById(etdl.group2.getId());
      Assert.assertEquals("new title 2", check.getTitle());

      // exception if try to save without setting the title
      try {
         EvalAdhocGroup group5 = new EvalAdhocGroup(EvalTestDataLoad.MAINT_USER_ID, null);
         adhocSupportLogic.saveAdhocGroup(group5);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
         //Assert.fail("Exception: " + e.getMessage()); // see why Assert.failing
      }

   }

   @Test
   public void testGetEvalAdhocGroupsByUserAndPerm() {
      List<EvalAdhocGroup> l;
      List<Long> ids;

      l = adhocSupportLogic.getAdhocGroupsByUserAndPerm(etdl.user3.getUserId(), EvalConstants.PERM_TAKE_EVALUATION);
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      Assert.assertEquals(etdl.group2.getId(), l.get(0).getId());

      l = adhocSupportLogic.getAdhocGroupsByUserAndPerm(EvalTestDataLoad.STUDENT_USER_ID, EvalConstants.PERM_TAKE_EVALUATION);
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      Assert.assertEquals(etdl.group1.getId(), l.get(0).getId());

      l = adhocSupportLogic.getAdhocGroupsByUserAndPerm(etdl.user1.getUserId(), EvalConstants.PERM_TAKE_EVALUATION);
      Assert.assertNotNull(l);
      Assert.assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains(etdl.group1.getId()));
      Assert.assertTrue(ids.contains(etdl.group2.getId()));

      l = adhocSupportLogic.getAdhocGroupsByUserAndPerm(etdl.user2.getUserId(), EvalConstants.PERM_TAKE_EVALUATION);
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

   }

   @Test
   public void testIsUserAllowedInAdhocGroup() {
      boolean allowed;

      allowed = adhocSupportLogic.isUserAllowedInAdhocGroup(EvalTestDataLoad.USER_ID, EvalConstants.PERM_TAKE_EVALUATION, etdl.group2.getEvalGroupId());
      Assert.assertTrue(allowed);

      allowed = adhocSupportLogic.isUserAllowedInAdhocGroup(EvalTestDataLoad.USER_ID, EvalConstants.PERM_BE_EVALUATED, etdl.group2.getEvalGroupId());
      Assert.assertFalse(allowed);

      allowed = adhocSupportLogic.isUserAllowedInAdhocGroup(etdl.user1.getUserId(), EvalConstants.PERM_TAKE_EVALUATION, etdl.group1.getEvalGroupId());
      Assert.assertTrue(allowed);

      allowed = adhocSupportLogic.isUserAllowedInAdhocGroup(etdl.user1.getUserId(), EvalConstants.PERM_BE_EVALUATED, etdl.group1.getEvalGroupId());
      Assert.assertFalse(allowed);

      allowed = adhocSupportLogic.isUserAllowedInAdhocGroup(etdl.user2.getUserId(), EvalConstants.PERM_TAKE_EVALUATION, etdl.group1.getEvalGroupId());
      Assert.assertFalse(allowed);

      allowed = adhocSupportLogic.isUserAllowedInAdhocGroup(etdl.user2.getUserId(), EvalConstants.PERM_BE_EVALUATED, etdl.group1.getEvalGroupId());
      Assert.assertFalse(allowed);
   }

}
