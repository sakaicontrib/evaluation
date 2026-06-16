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

import java.util.Date;
import java.util.List;

import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalLock;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

import lombok.extern.slf4j.Slf4j;

/**
 * Hibernate-backed implementation methods for the matching evaluation DAO port.
 */
@Slf4j
abstract class EvaluationDaoLockMethods extends EvaluationDaoQueryMethods {

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
                Long lockedItemCount = currentSession().createQuery(
                                "select count(item.id) from EvalItem item where item.locked = true and item.scale.id = :scaleId",
                                Long.class)
                        .setParameter("scaleId", scale.getId())
                        .uniqueResult();
                if (lockedItemCount != null && lockedItemCount > 0) {
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
                if (countLockedTemplatesUsingItem(item.getId()) > 0) {
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
                if (countLockedEvaluationsUsingTemplate(template.getId()) > 0) {
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
        log.debug("evaluation:" + evaluation.getId() + ", lockState:" + lockState);
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
                currentSession().merge(evaluation);
                currentSession().evict(evaluation);
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
                currentSession().merge(evaluation);
                currentSession().evict(evaluation);

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
        List<Long> itemIds = getHibernateTemplate().execute(session -> session
                .createQuery("select item.id from EvalItem item join item.scale itemScale where itemScale.id = :scaleid order by item.id")
                .setParameter("scaleid", scaleId)
                .list());
        return itemIds.toArray(new Long[] {});
    }

    /**
     * @param scaleId
     * @return true if this scale is used in any items
     */
    public boolean isUsedScale(Long scaleId) {
        if (scaleId != null) {
            log.debug("scaleId: " + scaleId);
            if (countItemsUsingScale(scaleId) > 0) {
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
            log.debug("itemId: " + itemId);
            if (countTemplateItemsUsingItem(itemId) > 0) {
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
            log.debug("templateId: " + templateId);
            if (countEvaluationsUsingTemplate(templateId) > 0) {
                // this is used by something
                return true;
            }
        }
        return false;
    }

    private int countLockedTemplatesUsingItem(Long itemId) {
        Long count = currentSession().createQuery(
                "select count(templateItem.id) from EvalTemplateItem templateItem "
                + "where templateItem.item.id = :itemId and templateItem.template.locked = true",
                Long.class)
                .setParameter("itemId", itemId)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    private int countLockedEvaluationsUsingTemplate(Long templateId) {
        Long count = currentSession().createQuery(
                "select count(evaluation.id) from EvalEvaluation evaluation "
                + "where evaluation.template.id = :templateId and evaluation.locked = true",
                Long.class)
                .setParameter("templateId", templateId)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    private int countItemsUsingScale(Long scaleId) {
        Long count = currentSession().createQuery(
                "select count(item.id) from EvalItem item where item.scale.id = :scaleId",
                Long.class)
                .setParameter("scaleId", scaleId)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    private int countTemplateItemsUsingItem(Long itemId) {
        Long count = currentSession().createQuery(
                "select count(templateItem.id) from EvalTemplateItem templateItem where templateItem.item.id = :itemId",
                Long.class)
                .setParameter("itemId", itemId)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    private int countEvaluationsUsingTemplate(Long templateId) {
        Long count = currentSession().createQuery(
                "select count(evaluation.id) from EvalEvaluation evaluation where evaluation.template.id = :templateId",
                Long.class)
                .setParameter("templateId", templateId)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
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
            List<EvalLock> locks = getLocksByName(lockId);
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
            log.error("Lock obtaining failure for lock ("+lockId+"): " + e.getMessage(), e);
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
            List<EvalLock> locks = getLocksByName(lockId);
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
            log.error("Lock releasing failure for lock ("+lockId+"): " + e.getMessage(), e);
        }

        return releasedLock;
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
            List<EvalLock> locks = getLocksByName(lockId);
            getHibernateTemplate().deleteAll(locks);
            getHibernateTemplate().flush();
        } catch (Exception ex) {
            log.error("Could not cleanup the lock ("+lockId+") after failure: " + ex.getMessage(), ex);
        }
    }

    private List<EvalLock> getLocksByName(String lockId) {
        return currentSession().createQuery(
                "select lock from EvalLock lock where lock.name = :lockId",
                EvalLock.class)
                .setParameter("lockId", lockId)
                .list();
    }

}
