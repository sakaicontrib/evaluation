#!/usr/bin/env python3
import os
import re

IMPL_DAO = "/Users/samo/dev/evaluation/sakai-evaluation-impl/src/java/org/sakaiproject/evaluation/dao"

SLICES = [
    ("EvaluationDaoSettingsMethods", "EvaluationSettingsDaoImpl", "EvaluationSettingsDao"),
    ("EvaluationDaoEmailTemplateMethods", "EvaluationEmailTemplateDaoImpl", "EvaluationEmailTemplateDao"),
    ("EvaluationDaoAuthoringMethods", "EvaluationAuthoringDaoImpl", "EvaluationAuthoringDao"),
    ("EvaluationDaoAdminSupportMethods", "EvaluationAdminSupportDaoImpl", "EvaluationAdminSupportDao"),
    ("EvaluationDaoResponseMethods", "EvaluationResponseDaoImpl", "EvaluationResponseDao"),
    ("EvaluationDaoAssignmentMethods", "EvaluationAssignmentDaoImpl", "EvaluationAssignmentDao"),
    ("EvaluationDaoQueryMethods", "EvaluationQueryDaoImpl", "EvaluationQueryDao"),
    ("EvaluationDaoLockMethods", "EvaluationLockDaoImpl", "EvaluationLockDao"),
    ("EvaluationDaoConsolidatedEmailMethods", "EvaluationConsolidatedEmailDaoImpl", "EvaluationConsolidatedEmailDao"),
]

PORTS = [
    "EvaluationDaoBase",
    "EvaluationSettingsDao",
    "EvaluationEmailTemplateDao",
    "EvaluationAuthoringDao",
    "EvaluationAdminSupportDao",
    "EvaluationResponseDao",
    "EvaluationAssignmentDao",
    "EvaluationQueryDao",
    "EvaluationLockDao",
    "EvaluationConsolidatedEmailDao",
]

for methods, impl, port in SLICES:
    src = os.path.join(IMPL_DAO, f"{methods}.java")
    dst = os.path.join(IMPL_DAO, f"{impl}.java")
    with open(src, "r") as f:
        content = f.read()
    content = re.sub(
        rf"abstract class {methods} extends \w+",
        f"public class {impl} extends EvaluationDaoHibernateSupport implements {port}",
        content,
        count=1,
    )
    with open(dst, "w") as f:
        f.write(content)
    os.remove(src)
    print(f"Created {impl}")

for port in PORTS:
    path = os.path.join(IMPL_DAO, f"{port}.java")
    if os.path.exists(path):
        os.remove(path)
        print(f"Removed impl copy of {port}")

for name in ["EvaluationDao.java", "EvaluationDaoImpl.java"]:
    path = os.path.join(IMPL_DAO, name)
    if os.path.exists(path):
        os.remove(path)
        print(f"Removed {name}")

base_impl = os.path.join(IMPL_DAO, "EvaluationDaoBaseImpl.java")
if not os.path.exists(base_impl):
    with open(base_impl, "w") as f:
        f.write("""/**
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

/**
 * Hibernate-backed base persistence operations for evaluation entities.
 */
public class EvaluationDaoBaseImpl extends EvaluationDaoHibernateSupport implements EvaluationDaoBase {
}
""")
    print("Created EvaluationDaoBaseImpl")
