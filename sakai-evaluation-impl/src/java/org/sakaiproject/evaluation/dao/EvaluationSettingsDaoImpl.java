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

import org.sakaiproject.evaluation.model.EvalConfig;

/**
 * Hibernate-backed implementation methods for the matching evaluation DAO port.
 */
public class EvaluationSettingsDaoImpl extends EvaluationDaoHibernateSupport implements EvaluationSettingsDao {

    public int countEvalConfigs() {
        Long count = currentSession().createQuery(
                "select count(cfg.id) from EvalConfig cfg", Long.class)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public EvalConfig getEvalConfigByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        List<EvalConfig> configs = currentSession().createQuery(
                "select cfg from EvalConfig cfg where cfg.name = :name",
                EvalConfig.class)
                .setParameter("name", name)
                .setMaxResults(1)
                .list();
        return configs.isEmpty() ? null : configs.get(0);
    }

    public List<EvalConfig> getAllEvalConfigs() {
        return currentSession().createQuery(
                "select cfg from EvalConfig cfg",
                EvalConfig.class)
                .list();
    }

    public int countEvalConfigsByNames(String[] names) {
        if (names == null) {
            throw new IllegalArgumentException("names cannot be null");
        }
        if (names.length == 0) {
            return 0;
        }
        Long count = currentSession().createQuery(
                "select count(cfg.id) from EvalConfig cfg where cfg.name in (:names)",
                Long.class)
                .setParameterList("names", names)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public void saveEvalConfig(EvalConfig config) {
        save(config);
    }
}
