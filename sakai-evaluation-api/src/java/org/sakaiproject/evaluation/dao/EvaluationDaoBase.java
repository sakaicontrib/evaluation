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
import java.util.List;

public interface EvaluationDaoBase {

    public void forceCommit();

    public void forceRollback();

    public void fixupDatabase();

    public <T> T findById(Class<T> type, Serializable id);

    public <T> List<T> findAll(Class<T> type);

    public <T> int countAll(Class<T> type);

    public void create(Object object);

    public void save(Object object);

    public void update(Object object);

    public void delete(Object object);

    public <T> boolean delete(Class<T> entityClass, Serializable id);
}
