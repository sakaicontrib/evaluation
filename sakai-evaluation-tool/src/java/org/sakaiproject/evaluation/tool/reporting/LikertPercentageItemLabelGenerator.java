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

import java.text.NumberFormat;

import org.jfree.chart.labels.AbstractCategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

/**
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class LikertPercentageItemLabelGenerator extends AbstractCategoryItemLabelGenerator
        implements CategoryItemLabelGenerator {

    private int totalItems;
    private double doubleTotalItems;

    public LikertPercentageItemLabelGenerator(int totalItems) {
        super("", NumberFormat.getInstance());
        this.totalItems = totalItems;
        this.doubleTotalItems = totalItems;
    }

    public String generateLabel(CategoryDataset dataset, int series, int category) {
        Number value = dataset.getValue(series, category);
        if (value == null)
            return "";
        double doubleVal = value.doubleValue();
        int intVal = value.intValue();
        if (doubleVal == 0.0) {
            return "0 % (0)";
        } else if (totalItems != 0) {

            double percentage = doubleVal / doubleTotalItems * 100;
            // return percentage + " % (" + intVal + ")";
            // return String.format("%.2f %s (%s)", percentage,"%", intVal+"");
            return String.format("%.0f %s (%s)", percentage, "%", intVal + "");
        } else if (value != null) {
            return value.toString();
        }
        return "";
    }

}
