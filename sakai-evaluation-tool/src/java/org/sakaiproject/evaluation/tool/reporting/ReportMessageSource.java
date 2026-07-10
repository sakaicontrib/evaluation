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
package org.sakaiproject.evaluation.tool.reporting;

import java.util.Locale;

import org.springframework.context.MessageSource;

/**
 * Report export message resolver backed by the tool message bundle.
 */
@FunctionalInterface
public interface ReportMessageSource {

    String getMessage(String code, Object[] args);

    default String getMessage(String code) {
        return getMessage(code, (Object[]) null);
    }

    default String getMessage(String code, Object arg) {
        return getMessage(code, new Object[] { arg });
    }

    static ReportMessageSource from(MessageSource messageSource) {
        return (code, args) -> messageSource.getMessage(code, args, code, Locale.getDefault());
    }

    static ReportMessageSource from(MessageSource messageSource, Locale locale) {
        return (code, args) -> messageSource.getMessage(code, args, code, locale);
    }
}
