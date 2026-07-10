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
package org.sakaiproject.evaluation.tool.utils;

import java.util.List;

import org.sakaiproject.evaluation.tool.utils.ScaleOptionsBuilder.OptionData;
import org.sakaiproject.evaluation.tool.utils.ScaleOptionsBuilder.SteppedRow;

import lombok.Data;

/**
 * Shared view model for evaluation item rendering in take and preview flows.
 */
@Data
public class EvalItemViewData implements ScaleOptionsBuilder.ItemScaleOptionsTarget {
    String itemType;
    String itemText;
    int displayNumber;
    int displayRows;
    boolean usesNA;
    boolean usesComment;
    boolean compulsory;

    String scaleDisplaySetting;
    List<OptionData> options;
    String startLabel;
    String endLabel;
    String startClass;
    String endClass;
    String idealImageUrl;
    String matrixLabelStart;
    String matrixLabelEnd;
    String matrixLabelMiddle;
    List<SteppedRow> steppedRows;
    List<EvalItemViewData> childItems;

    boolean odd;
}
