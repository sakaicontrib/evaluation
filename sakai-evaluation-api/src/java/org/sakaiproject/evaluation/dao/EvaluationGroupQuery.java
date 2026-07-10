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
 * Typed criteria for evaluation list queries scoped to assignment groups.
 */
public final class EvaluationGroupQuery {

    public enum ActiveFilter {
        ACTIVE_ONLY,
        INACTIVE_ONLY,
        ALL
    }

    public enum ApprovalFilter {
        APPROVED_ONLY,
        UNAPPROVED_ONLY,
        ALL
    }

    public enum AnonymousFilter {
        ANONYMOUS_ONLY,
        NON_ANONYMOUS_ONLY,
        ALL
    }

    private final String[] evalGroupIds;
    private final ActiveFilter activeFilter;
    private final ApprovalFilter approvalFilter;
    private final AnonymousFilter anonymousFilter;

    private EvaluationGroupQuery(String[] evalGroupIds, ActiveFilter activeFilter,
            ApprovalFilter approvalFilter, AnonymousFilter anonymousFilter) {
        this.evalGroupIds = evalGroupIds;
        this.activeFilter = activeFilter;
        this.approvalFilter = approvalFilter;
        this.anonymousFilter = anonymousFilter;
    }

    public static EvaluationGroupQuery of(String[] evalGroupIds, Boolean activeOnly, Boolean approvedOnly,
            Boolean includeAnonymous) {
        return new EvaluationGroupQuery(
                evalGroupIds,
                toActiveFilter(activeOnly),
                toApprovalFilter(approvedOnly),
                toAnonymousFilter(includeAnonymous));
    }

    public String[] getEvalGroupIds() {
        return evalGroupIds;
    }

    public ActiveFilter getActiveFilter() {
        return activeFilter;
    }

    public ApprovalFilter getApprovalFilter() {
        return approvalFilter;
    }

    public AnonymousFilter getAnonymousFilter() {
        return anonymousFilter;
    }

    private static ActiveFilter toActiveFilter(Boolean activeOnly) {
        if (activeOnly == null) {
            return ActiveFilter.ALL;
        }
        return activeOnly ? ActiveFilter.ACTIVE_ONLY : ActiveFilter.INACTIVE_ONLY;
    }

    private static ApprovalFilter toApprovalFilter(Boolean approvedOnly) {
        if (approvedOnly == null) {
            return ApprovalFilter.ALL;
        }
        return approvedOnly ? ApprovalFilter.APPROVED_ONLY : ApprovalFilter.UNAPPROVED_ONLY;
    }

    private static AnonymousFilter toAnonymousFilter(Boolean includeAnonymous) {
        if (includeAnonymous == null) {
            return AnonymousFilter.ALL;
        }
        return includeAnonymous ? AnonymousFilter.ANONYMOUS_ONLY : AnonymousFilter.NON_ANONYMOUS_ONLY;
    }
}
