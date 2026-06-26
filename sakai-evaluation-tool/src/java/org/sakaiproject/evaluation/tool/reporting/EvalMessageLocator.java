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

/**
 * Simple message locator replacing RSF's MessageLocator dependency.
 * Provides the same convenience methods as the original RSF class.
 */
public abstract class EvalMessageLocator {

    /** Implement this to resolve messages via Spring MessageSource or similar. */
    public abstract Object[] getMessages(String[] codes, Object[] args);

    public String getMessage(String code) {
        return getMessage(code, (Object[]) null);
    }

    public String getMessage(String code, Object arg) {
        return getMessage(code, new Object[]{ arg });
    }

    public String getMessage(String code, Object[] args) {
        Object[] results = getMessages(new String[]{ code }, args);
        return results != null && results.length > 0 ? results[0].toString() : code;
    }
}
