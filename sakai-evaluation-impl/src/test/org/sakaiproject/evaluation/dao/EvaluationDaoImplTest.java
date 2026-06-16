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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.model.EvalAdmin;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalConfig;
import org.sakaiproject.evaluation.model.EvalEmailProcessingData;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalGroupNodes;
import org.sakaiproject.evaluation.model.EvalHierarchyRule;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestDataImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import lombok.extern.slf4j.Slf4j;


/**
 * DAO regression tests for EvaluationDaoImpl.
 */
@Slf4j
public class EvaluationDaoImplTest extends AbstractEvaluationDaoTest {

    @Test
    public void testLockScale() {

        // check that locked scale gets unlocked (no locking item)
        Assert.assertTrue(scaleLocked.getLocked() );
        Assert.assertTrue( evaluationDao.lockScale( scaleLocked, Boolean.FALSE ) );
        Assert.assertFalse(scaleLocked.getLocked() );
        // check that unlocking an unlocked scale is not a problem
        Assert.assertFalse( evaluationDao.lockScale( scaleLocked, Boolean.FALSE ) );

        // check that locked scale that is locked by an item cannot be unlocked
        EvalScale scale1 = (EvalScale) evaluationDao.findById(EvalScale.class, etdl.scale1.getId());
        Assert.assertTrue(scale1.getLocked() );
        Assert.assertFalse( evaluationDao.lockScale( scale1, Boolean.FALSE ) );
        Assert.assertTrue(scale1.getLocked() );
        // check that locking a locked scale is not a problem
        Assert.assertFalse( evaluationDao.lockScale( scale1, Boolean.TRUE ) );

        // check that new scale cannot be unlocked
        try {
            evaluationDao.lockScale( 
                    new EvalScale(EvalTestDataLoad.ADMIN_USER_ID, 
                            "new scale", EvalConstants.SCALE_MODE_SCALE, 
                            EvalConstants.SHARING_PRIVATE, Boolean.FALSE),
                            Boolean.FALSE
            );
            Assert.fail("Should have thrown an exception");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#lockItem(org.sakaiproject.evaluation.model.EvalItem, java.lang.Boolean)}.
     */
    @Test
    public void testLockItem() {

        // check that unlocked item gets locked (no scale)
        Assert.assertFalse(etdl.item7.getLocked() );
        Assert.assertTrue( evaluationDao.lockItem( etdl.item7, Boolean.TRUE ) );
        Assert.assertTrue(etdl.item7.getLocked() );

        // check that locked item does nothing bad if locked again (no scale, not used)
        Assert.assertTrue(itemLocked.getLocked() );
        Assert.assertFalse( evaluationDao.lockItem( itemLocked, Boolean.TRUE ) );
        Assert.assertTrue(itemLocked.getLocked() );

        // check that locked item gets unlocked (no scale, not used)
        Assert.assertTrue(itemLocked.getLocked() );
        Assert.assertTrue( evaluationDao.lockItem( itemLocked, Boolean.FALSE ) );
        Assert.assertFalse( itemLocked.getLocked() );

        // check that locked item that is locked by a template cannot be unlocked
        Assert.assertTrue( etdl.item1.getLocked() );
        Assert.assertFalse( evaluationDao.lockItem( etdl.item1, Boolean.FALSE ) );
        Assert.assertTrue( etdl.item1.getLocked() );

        // check that locked item that is locked by a template can be locked without exception
        Assert.assertTrue( etdl.item1.getLocked() );
        Assert.assertFalse( evaluationDao.lockItem( etdl.item1, Boolean.TRUE ) );
        Assert.assertTrue( etdl.item1.getLocked() );

        // verify that associated scale is unlocked
        Assert.assertFalse( itemUnlocked.getScale().getLocked() );

        // check that unlocked item gets locked (scale)
        Assert.assertFalse( itemUnlocked.getLocked() );
        Assert.assertTrue( evaluationDao.lockItem( itemUnlocked, Boolean.TRUE ) );
        Assert.assertTrue( itemUnlocked.getLocked() );

        // verify that associated scale gets locked
        Assert.assertTrue( itemUnlocked.getScale().getLocked() );

        // check that locked item gets unlocked (scale)
        Assert.assertTrue( itemUnlocked.getLocked() );
        Assert.assertTrue( evaluationDao.lockItem( itemUnlocked, Boolean.FALSE ) );
        Assert.assertFalse( itemUnlocked.getLocked() );

        // verify that associated scale gets unlocked
        Assert.assertFalse( itemUnlocked.getScale().getLocked() );

        // check that locked item gets unlocked (scale locked by another item)
        Assert.assertTrue( etdl.item4.getScale().getLocked() );
        Assert.assertTrue( evaluationDao.lockItem( etdl.item4, Boolean.TRUE ) );
        Assert.assertTrue( etdl.item4.getLocked() );

        Assert.assertTrue( evaluationDao.lockItem( etdl.item4, Boolean.FALSE ) );
        Assert.assertFalse( etdl.item4.getLocked() );

        // verify that associated scale does not get unlocked
        Assert.assertTrue( etdl.item4.getScale().getLocked() );

        // check that new item cannot be locked/unlocked
        try {
            evaluationDao.lockItem(
                    new EvalItem( EvalTestDataLoad.ADMIN_USER_ID, "something", 
                            EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_HEADER, 
                            Boolean.FALSE),
                            Boolean.TRUE);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#lockTemplate(org.sakaiproject.evaluation.model.EvalTemplate, java.lang.Boolean)}.
     */
    @Test
    public void testLockTemplate() {

        // check that unlocked template gets locked (no items)
        Assert.assertFalse( etdl.templateAdminNoItems.getLocked() );
        Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateAdminNoItems, Boolean.TRUE ) );
        Assert.assertTrue( etdl.templateAdminNoItems.getLocked() );

        // check that locked template is ok with getting locked again (no problems)
        Assert.assertTrue( etdl.templateAdminNoItems.getLocked() );
        Assert.assertFalse( evaluationDao.lockTemplate( etdl.templateAdminNoItems, Boolean.TRUE ) );
        Assert.assertTrue( etdl.templateAdminNoItems.getLocked() );

        // check that locked template gets unlocked (no items)
        Assert.assertTrue( etdl.templateAdminNoItems.getLocked() );
        Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateAdminNoItems, Boolean.FALSE ) );
        Assert.assertFalse( etdl.templateAdminNoItems.getLocked() );

        // check that locked template that is locked by an evaluation cannot be unlocked
        Assert.assertTrue( etdl.templateUser.getLocked() );
        Assert.assertFalse( evaluationDao.lockTemplate( etdl.templateUser, Boolean.FALSE ) );
        Assert.assertTrue( etdl.templateUser.getLocked() );

        // check that locked template that is locked by an evaluation can be locked without exception
        Assert.assertTrue( etdl.templateUser.getLocked() );
        Assert.assertFalse( evaluationDao.lockTemplate( etdl.templateUser, Boolean.TRUE ) );
        Assert.assertTrue( etdl.templateUser.getLocked() );

        // check that unlocked template gets locked (items)
        Assert.assertFalse( etdl.item6.getLocked() );
        Assert.assertFalse( etdl.templateUserUnused.getLocked() );
        Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateUserUnused, Boolean.TRUE ) );
        Assert.assertTrue( etdl.templateUserUnused.getLocked() );

        // verify that related items are locked also
        Assert.assertTrue( etdl.item6.getLocked() );

        // check that locked template gets unlocked (items)
        Assert.assertTrue( etdl.templateUserUnused.getLocked() );
        Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateUserUnused, Boolean.FALSE ) );
        Assert.assertFalse( etdl.templateUserUnused.getLocked() );

        // verify that related items are unlocked also
        Assert.assertFalse( etdl.item6.getLocked() );

        // check unlocked template with locked items can be locked
        Assert.assertFalse( etdl.templateUnused.getLocked() );
        Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateUnused, Boolean.TRUE ) );
        Assert.assertTrue( etdl.templateUnused.getLocked() );

        // check that locked template gets unlocked (items locked by another template)
        Assert.assertTrue( etdl.item3.getLocked() );
        Assert.assertTrue( etdl.item5.getLocked() );
        Assert.assertTrue( etdl.templateUnused.getLocked() );
        Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateUnused, Boolean.FALSE ) );
        Assert.assertFalse( etdl.templateUnused.getLocked() );

        // verify that associated items locked by other template do not get unlocked
        Assert.assertTrue( etdl.item3.getLocked() );
        Assert.assertTrue( etdl.item5.getLocked() );

        // check that new template cannot be locked/unlocked
        try {
            evaluationDao.lockTemplate(
                    new EvalTemplate(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.TEMPLATE_TYPE_STANDARD, 
                            "new template one", "description", 
                            EvalConstants.SHARING_PRIVATE, EvalTestDataLoad.NOT_EXPERT, "expert desc", 
                            null, EvalTestDataLoad.LOCKED, false),
                            Boolean.TRUE);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#lockEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)}.
     */
    @Test
    public void testLockEvaluation() {

        // check that unlocked evaluation gets locked
        Assert.assertFalse( etdl.templatePublicUnused.getLocked() );
        Assert.assertFalse( evalUnLocked.getLocked() );
        Assert.assertTrue( evaluationDao.lockEvaluation( evalUnLocked, true ) );
        Assert.assertTrue( evalUnLocked.getLocked() );

        // verify that associated template gets locked
        Assert.assertTrue( etdl.templatePublicUnused.getLocked() );

        // now unlock the evaluation
        Assert.assertTrue( evalUnLocked.getLocked() );
        Assert.assertTrue( evaluationDao.lockEvaluation( evalUnLocked, false ) );
        Assert.assertFalse( evalUnLocked.getLocked() );

        // verify that associated template gets unlocked
        Assert.assertFalse( etdl.templatePublicUnused.getLocked() );

        // check that new evaluation cannot be locked
        try {
            evaluationDao.lockEvaluation(
                    new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, EvalTestDataLoad.MAINT_USER_ID, "Eval new", null, 
                            etdl.tomorrow, etdl.threeDaysFuture, etdl.threeDaysFuture, etdl.fourDaysFuture, false, null,
                            false, null, 
                            EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.SHARING_VISIBLE, EvalConstants.INSTRUCTOR_OPT_IN, 1, null, null, null, null,
                            etdl.templatePublic, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                            EvalTestDataLoad.UNLOCKED, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null, null),
                            true
            );
            Assert.fail("Should have thrown an exception");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#isUsedScale(java.lang.Long)}.
     */
    @Test
    public void testIsUsedScale() {
        Assert.assertTrue( evaluationDao.isUsedScale( etdl.scale1.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedScale( etdl.scale2.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedScale( etdl.scale3.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedScale( etdl.scale4.getId() ) );
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#isUsedItem(java.lang.Long)}.
     */
    @Test
    public void testIsUsedItem() {
        Assert.assertTrue( evaluationDao.isUsedItem( etdl.item1.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedItem( etdl.item2.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedItem( etdl.item3.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedItem( etdl.item4.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedItem( etdl.item5.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedItem( etdl.item6.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedItem( etdl.item7.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedItem( etdl.item8.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedItem( etdl.item9.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedItem( etdl.item10.getId() ) );
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#isUsedTemplate(java.lang.Long)}.
     */
    @Test
    public void testIsUsedTemplate() {
        Assert.assertTrue( evaluationDao.isUsedTemplate( etdl.templateAdmin.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedTemplate( etdl.templateAdminBlock.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedTemplate( etdl.templateAdminComplex.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedTemplate( etdl.templateAdminNoItems.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedTemplate( etdl.templatePublic.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedTemplate( etdl.templatePublicUnused.getId() ) ); // used in this file
        Assert.assertFalse( evaluationDao.isUsedTemplate( etdl.templateUnused.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedTemplate( etdl.templateUser.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedTemplate( etdl.templateUserUnused.getId() ) );
    }

    @Test
    public void testObtainLock() {
        // check I can get a lock
        Assert.assertTrue( evaluationDao.obtainLock("AZ.my.lock", "AZ1", 100) );

        // check someone else cannot get my lock
        Assert.assertFalse( evaluationDao.obtainLock("AZ.my.lock", "AZ2", 100) );

        // check I can get my own lock again
        Assert.assertTrue( evaluationDao.obtainLock("AZ.my.lock", "AZ1", 100) );

        // allow the lock to expire
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // nothing here but a Assert.fail
            Assert.fail("sleep interrupted?");
        }

        // check someone else can get my lock
        Assert.assertTrue( evaluationDao.obtainLock("AZ.my.lock", "AZ2", 100) );

        // check invalid arguments cause Assert.failure
        try {
            evaluationDao.obtainLock("AZ.my.lock", null, 1000);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        try {
            evaluationDao.obtainLock(null, "AZ1", 1000);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testReleaseLock() {

        // check I can get a lock
        Assert.assertTrue( evaluationDao.obtainLock("AZ.R.lock", "AZ1", 1000) );

        // check someone else cannot get my lock
        Assert.assertFalse( evaluationDao.obtainLock("AZ.R.lock", "AZ2", 1000) );

        // check I can release my lock
        Assert.assertTrue( evaluationDao.releaseLock("AZ.R.lock", "AZ1") );

        // check someone else can get my lock now
        Assert.assertTrue( evaluationDao.obtainLock("AZ.R.lock", "AZ2", 1000) );

        // check I cannot get the lock anymore
        Assert.assertFalse( evaluationDao.obtainLock("AZ.R.lock", "AZ1", 1000) );

        // check they can release it
        Assert.assertTrue( evaluationDao.releaseLock("AZ.R.lock", "AZ2") );

        // check invalid arguments cause Assert.failure
        try {
            evaluationDao.releaseLock("AZ.R.lock", null);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        try {
            evaluationDao.releaseLock(null, "AZ1");
            Assert.fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetConsolidatedEmailMapping() {

        // when no emails have been sent, selecting email recipients in any of several ways should return 1 
        int count = this.evaluationDao.selectConsolidatedEmailRecipients(true, (Date) null, false, (Date) null, EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE);
        Assert.assertEquals(1, count);
        int deletions = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(1, deletions);
        count = this.evaluationDao.selectConsolidatedEmailRecipients(true, new Date(), false, (Date) null, EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE);
        Assert.assertEquals(1, count);
        deletions = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(1, deletions);

        // there should be two new evals ready to send announcements to, selected because the value of availableEmailSent is null
        int count1 = this.evaluationDao.selectConsolidatedEmailRecipients(true, (Date) null, false, (Date) null, EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE);
        Assert.assertEquals(1,count1);
        List<Map<String,Object>> mapping1 = this.evaluationDao.getConsolidatedEmailMapping(true, 100, 0);
        Assert.assertNotNull(mapping1);
        Assert.assertEquals(1, mapping1.size());
        int deletions1 = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(1, deletions1);

        // Since those announcements have been sent, there should be none yet to be sent
        int count2 = this.evaluationDao.selectConsolidatedEmailRecipients(true, (Date) null, false, (Date) null, EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE);
        Assert.assertEquals(0,count2);
        List<Map<String,Object>> mapping2 = this.evaluationDao.getConsolidatedEmailMapping(true, 100, 0);
        Assert.assertNotNull(mapping2);
        Assert.assertEquals(0, mapping2.size());
        int deletions2 = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(0, deletions2);

        // if we search for notices to be sent and ignore the date, we should find them again
        int count3 = this.evaluationDao.selectConsolidatedEmailRecipients(false, (Date) null, false, (Date) null, EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE);
        Assert.assertEquals(1,count3);
        List<Map<String,Object>> mapping3 = this.evaluationDao.getConsolidatedEmailMapping(true, 100, 0);
        Assert.assertNotNull(mapping3);
        Assert.assertEquals(1, mapping3.size());
        int deletions3 = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(1, deletions3);

        // if we search for evals needing reminders based on whether an announcement or reminder has been sent in the past day, we should find none 
        int count4 = this.evaluationDao.selectConsolidatedEmailRecipients(true, new Date(), true, new Date(), EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER);
        Assert.assertEquals(0,count4);
        List<Map<String,Object>> mapping4 = this.evaluationDao.getConsolidatedEmailMapping(false, 100, 0);
        Assert.assertNotNull(mapping4);
        Assert.assertEquals(0, mapping4.size());
        int deletions4 = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(0, deletions4);

        // if we search for evals needing reminders based on whether a reminder has been sent in the past day (ignoring when announcements were sent) we find 2
        int count5 = this.evaluationDao.selectConsolidatedEmailRecipients(false, (Date) null, true, new Date(), EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER);
        Assert.assertEquals(1,count5);
        List<Map<String,Object>> mapping5 = this.evaluationDao.getConsolidatedEmailMapping(false, 100, 0);
        Assert.assertNotNull(mapping5);
        Assert.assertEquals(1, mapping5.size());
        int deletions5 = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(1, deletions5);

        // if we do the same search again, we find 0 because they have just been sent 
        int count6 = this.evaluationDao.selectConsolidatedEmailRecipients(false, (Date) null, true, new Date(), EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER);
        Assert.assertEquals(0,count6);
        List<Map<String,Object>> mapping6 = this.evaluationDao.getConsolidatedEmailMapping(false, 100, 0);
        Assert.assertNotNull(mapping6);
        Assert.assertEquals(0, mapping6.size());
        int deletions6 = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(0, deletions6);

        // if we search for evals needing reminders as if it were tomorrow, we should find 1
        int count7 = this.evaluationDao.selectConsolidatedEmailRecipients(false, (Date) null, true, new Date(System.currentTimeMillis() + MILLISECONDS_PER_DAY), EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER);
        Assert.assertEquals(1,count7);
        List<Map<String,Object>> mapping7 = this.evaluationDao.getConsolidatedEmailMapping(false, 100, 0);
        Assert.assertNotNull(mapping7);
        Assert.assertEquals(1, mapping7.size());
        int deletions7 = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(1, deletions7);

        // if we search for evals needing reminders as if it were tomorrow, we should find 1
        int count8 = this.evaluationDao.selectConsolidatedEmailRecipients(false, (Date) null, true, new Date(System.currentTimeMillis() + MILLISECONDS_PER_DAY), EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER);
        Assert.assertEquals(1,count8);
        List<EvalEmailProcessingData> list = evaluationDao.findAll(EvalEmailProcessingData.class);
        Assert.assertNotNull(list);
        Assert.assertEquals(1,list.size());

        // now if we reset the queue and update "completedDate" for the EvalAssignUser associated with that user and eval, we should find no evals needing notifications
        this.evaluationDao.resetConsolidatedEmailRecipients();
        EvalEmailProcessingData eepd = list.get(0);
        // update EvalAssignUser with completed time
        EvalEvaluation evaluation = evaluationDao.findById(EvalEvaluation.class, eepd.getEvalId());
        List<EvalAssignUser> eaus = evaluationDao.findAll(EvalAssignUser.class);
        Assert.assertNotNull(eaus);
        List<EvalAssignUser> matchingAssignUsers = evaluationDao.getParticipantsForEval(evaluation.getId(), eepd.getUserId(),
                new String[] {eepd.getGroupId()}, null, EvalEvaluationService.STATUS_ANY, null, null);
        Assert.assertEquals(1, matchingAssignUsers.size());
        EvalAssignUser eau = matchingAssignUsers.get(0);
        Assert.assertNotNull(eau);
        Long eauId = eau.getId();
        Assert.assertNotNull(eauId);
        eau.setCompletedDate(new Date());
        evaluationDao.update(eau);
        EvalAssignUser eau0 = evaluationDao.findById(EvalAssignUser.class, eau.getId());
        Assert.assertNotNull(eau0);
        Assert.assertNotNull(eau0.getCompletedDate());
        int count9 = this.evaluationDao.selectConsolidatedEmailRecipients(false, (Date) null, true, new Date(System.currentTimeMillis() + MILLISECONDS_PER_DAY), EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER);
        Assert.assertEquals(0,count9);
        List<Map<String,Object>> mapping9 = this.evaluationDao.getConsolidatedEmailMapping(false, 100, 0);
        Assert.assertNotNull(mapping9);
        Assert.assertEquals(0, mapping9.size());
    }

    /**
     * testResponsesSavedInProgress checks to see how many responses have been saved but not submitted
     * for both active and inactive evaluations.  This depends on evaluations being open or closed and 
     * having an incomplete response associated with it.
     */
    @Test
    public void testGetResponsesSavedInProgress() {
        // add an incomplete response to an active evaluation
        EvalResponse responseActive = etdl.response1;
        EvalEvaluation evalActive = etdl.evaluationActive;
        responseActive.setEndTime(null);
        responseActive.setEvaluation(evalActive);
        this.evaluationDao.save(responseActive);    	

        // add an incomplete response to a closed evaluation
        EvalResponse responseInactive = etdl.response2;
        EvalEvaluation evalInactive = etdl.evaluationClosed;
        responseInactive.setEndTime(null);
        responseInactive.setEvaluation(evalInactive);
        this.evaluationDao.save(responseInactive);

        List<EvalResponse> evalOpenResponses = this.evaluationDao.getResponsesSavedInProgress(true);
        Assert.assertNotNull(evalOpenResponses);
        Assert.assertEquals(1, evalOpenResponses.size());
        Assert.assertEquals(evalOpenResponses.get(0).getId(), responseActive.getId());

        List<EvalResponse> evalClosedResponses = this.evaluationDao.getResponsesSavedInProgress(false);
        Assert.assertNotNull(evalClosedResponses);
        Assert.assertEquals(1, evalClosedResponses.size());
        Assert.assertEquals(evalClosedResponses.get(0).getId(), responseInactive.getId());
    }

    /**
     * Add anything that supports the unit tests below here
     */
}
