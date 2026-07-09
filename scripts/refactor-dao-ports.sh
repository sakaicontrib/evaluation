#!/bin/bash
# Flatten DAO inheritance, move port interfaces to api, remove EvaluationDao facade.
set -euo pipefail
ROOT="/Users/samo/dev/evaluation"
API_DAO="$ROOT/sakai-evaluation-api/src/java/org/sakaiproject/evaluation/dao"
IMPL_DAO="$ROOT/sakai-evaluation-impl/src/java/org/sakaiproject/evaluation/dao"

PORTS=(
  EvaluationDaoBase
  EvaluationSettingsDao
  EvaluationEmailTemplateDao
  EvaluationAuthoringDao
  EvaluationAdminSupportDao
  EvaluationResponseDao
  EvaluationAssignmentDao
  EvaluationQueryDao
  EvaluationLockDao
  EvaluationConsolidatedEmailDao
)

for port in "${PORTS[@]}"; do
  if [[ -f "$IMPL_DAO/${port}.java" ]]; then
    cp "$IMPL_DAO/${port}.java" "$API_DAO/${port}.java"
  fi
done

# Remove compatibility facade
rm -f "$IMPL_DAO/EvaluationDao.java" "$IMPL_DAO/EvaluationDaoImpl.java"

declare -A SLICE_MAP=(
  [EvaluationDaoSettingsMethods]=EvaluationSettingsDao
  [EvaluationDaoEmailTemplateMethods]=EvaluationEmailTemplateDao
  [EvaluationDaoAuthoringMethods]=EvaluationAuthoringDao
  [EvaluationDaoAdminSupportMethods]=EvaluationAdminSupportDao
  [EvaluationDaoResponseMethods]=EvaluationResponseDao
  [EvaluationDaoAssignmentMethods]=EvaluationAssignmentDao
  [EvaluationDaoQueryMethods]=EvaluationQueryDao
  [EvaluationDaoLockMethods]=EvaluationLockDao
  [EvaluationDaoConsolidatedEmailMethods]=EvaluationConsolidatedEmailDao
)

for methods in "${!SLICE_MAP[@]}"; do
  port="${SLICE_MAP[$methods]}"
  impl="${port}Impl"
  src="$IMPL_DAO/${methods}.java"
  dst="$IMPL_DAO/${impl}.java"
  if [[ ! -f "$src" ]]; then
    echo "Missing $src" >&2
    exit 1
  fi
  sed \
    -e "s/abstract class ${methods} extends [A-Za-z]*/public class ${impl} extends EvaluationDaoHibernateSupport implements ${port}/" \
    -e "s/abstract class ${methods} extends EvaluationDaoHibernateSupport/public class ${impl} extends EvaluationDaoHibernateSupport implements ${port}/" \
    "$src" > "$dst"
  rm "$src"
done

# Base persistence bean implementation
cat > "$IMPL_DAO/EvaluationDaoBaseImpl.java" <<'EOF'
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

/**
 * Hibernate-backed base persistence operations for evaluation entities.
 */
public class EvaluationDaoBaseImpl extends EvaluationDaoHibernateSupport implements EvaluationDaoBase {
}
EOF

# Remove duplicate port interfaces from impl (now in api)
for port in "${PORTS[@]}"; do
  rm -f "$IMPL_DAO/${port}.java"
done

echo "DAO slice refactor complete."
