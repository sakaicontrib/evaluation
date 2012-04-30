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
package org.sakaiproject.evaluation.logic.imports;

import java.util.List;

/**
 * Handle the importing of external data into the Evaluation System.
 * <ul>
 * <li>EvalScale</li>
 * <li>EvalItem</li>
 * <li>EvalTemplate</li>
 * <li>EvalTemplateItem</li>
 * <li>EvalEvaluation</li>
 * <li>EvalAssignGroup</li>
 * </ul>
 * By default, processing occurs in the current interactive session. The
 * session is periodically set active to avoid timing out during a long-running
 * import. Property eval.qrtzImport=true in sakai.properties causes processing
 * in a Quartz job rather than the current interactive session but this is
 * currently not working - see EVALSYS-273.
 * 
 * @author rwellis
 */
public interface EvalImportLogic {

	/**
	 * Parse XML content in a ContentResource and persist the contained evaluation data
	 * 
	 * @param id of the Reference that identifies the ContentResource
	 * @return a List of String messages for the user
	 */
	public List<String> load(String id);
}
