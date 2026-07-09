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
import java.util.Map;
import java.util.Set;

public interface EvaluationConsolidatedEmailDao {

    public List<Map<String,Object>> getConsolidatedEmailMapping(boolean sendingAvailableEmails, int pageSize, int page);

    public int selectConsolidatedEmailRecipients(boolean useAvailableEmailSent, Date availableEmailSent,
            boolean useReminderEmailSent, Date reminderEmailSent, String emailTemplateType);

    public int resetConsolidatedEmailRecipients();

    public int countDistinctGroupsInConsolidatedEmailMapping();

    public Set<String> getAllSiteIDsMatchingSectionTitle(String sectionTitleWithWildcards);

    public Set<String> getAllSiteIDsMatchingSiteTitle(String siteTitleWithWildcards);
}
