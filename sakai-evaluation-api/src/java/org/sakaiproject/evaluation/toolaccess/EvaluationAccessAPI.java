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

package org.sakaiproject.evaluation.toolaccess;

import java.io.OutputStream;

import org.sakaiproject.evaluation.model.EvalEvaluation;

public interface EvaluationAccessAPI {

	public void setToolApi (ToolApi t);
	public void exportReport(EvalEvaluation evaluation, String groupIds, OutputStream outputStream, String exportType);
	public void exportReport(EvalEvaluation evaluation, String[] groupIds, String evaluateeId,OutputStream outputStream, String exportType);
}