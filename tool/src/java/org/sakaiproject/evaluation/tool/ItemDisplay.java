/******************************************************************************
 * ItemDisplay.java - created by fengr@vt.edu on Aug 21, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool;

import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * This ItemDisplay file is to display various kind of item with scale value/labels
 * TODO: to be removed , and just use Item object instead a wrapper of ItemDisplay
 * 
 * @author: Rui Feng (fengr@vt.edu)
 * @author: Kapil Ahuja (kahuja@vt.edu)
 * @deprecated This class will be removed before release -AZ
 */

public class ItemDisplay  {
	
	private EvalItem item = null;
	private String [] scaleValues = null;
	private String [] scaleLabels = null;
	
	private String compactDisplayStart = "";
	private String compactDisplayEnd = "";
	
	public ItemDisplay(EvalItem item) {
		
		super();

		this.item = item;
		
		if(this.item.getClassification().equals(EvalConstants.ITEM_TYPE_SCALED) 
				||this.item.getClassification().equals(EvalConstants.ITEM_TYPE_BLOCK)){
			//"Scaled/Survey","Question Block"
			String scaleDisplaySetting = item.getScaleDisplaySetting();	
			EvalScale scale = item.getScale();
			String[] scaleOptions = scale.getOptions();
			int optionCount = scaleOptions.length;
		
			//for compact display
			if (scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_COMPACT) || 
					scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED)) {
			//"Compact","Compact Colored"
				//TODO: Check if the order here is same as in SCALE_OPTIONS_INDEX in SCALE_OPTIONS table.
				compactDisplayStart = scaleOptions[0];		
				compactDisplayEnd = scaleOptions[optionCount - 1];

				//the value and label of radio buttons is just empty strings here
				scaleValues = new String[optionCount];
				scaleLabels = new String[optionCount];
				for (int count = 0; count < optionCount; count++) {
					
					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = " ";
				}
			}else if (scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_FULL) ||
					scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED) 
				|| scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL) ) {
			//"Full","Full Colored","Vertical"
			/*
			 * For full and vertical displays the value is just empty strings here
			 */
				scaleValues = new String[optionCount];
				scaleLabels = new String[optionCount];
				for (int count = 0; count < optionCount; count++) {

					//TODO: Check if the order here is same as in SCALE_OPTIONS_INDEX in SCALE_OPTIONS table.
					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = scaleOptions[count];	
				}
			}
			//scaleDisplaySetting.equals("Stepped") || scaleDisplaySetting.equals("Stepped Colored")
			else {
			
			/*
			 * For stepped display the value is just empty strings and the order of 
			 * scale labels is reversed (for ease of display).
			 */
				scaleValues = new String[optionCount];
				scaleLabels = new String[optionCount];
				for (int count = 1; count <= optionCount; count++) {
					
					//TODO: Check if the order here is same as in SCALE_OPTIONS_INDEX in SCALE_OPTIONS table.
					/*
					 * Scale labels are added in reverse order because for the stepped display the last 
					 * option is displayed in the first row, second last option in second row and so on.
					 * Scale values are added in correct order because the finally the labels line up
					 * from left to right with first label being to the very left and last label being
					 * to the very right.  
					 */
					scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
					scaleLabels[optionCount - count] = scaleOptions[count-1];
				}
			
			}
		}
			
	}
	
	public EvalItem getItem() {
		return item;
	}
	public void setItem(EvalItem item) {
		this.item = item;
	}
	public String[] getScaleLabels() {
		return scaleLabels;
	}
	public void setScaleLabels(String[] scaleLabels) {
		this.scaleLabels = scaleLabels;
	}
	public String[] getScaleValues() {
		return scaleValues;
	}
	public void setScaleValues(String[] scaleValues) {
		this.scaleValues = scaleValues;
	}

	public String getCompactDisplayStart() {
		return compactDisplayStart;
	}
	public void setCompactDisplayStart(String compactDisplayStart) {
		this.compactDisplayStart = compactDisplayStart;
	}
	public String getCompactDisplayEnd() {
		return compactDisplayEnd;
	}
	public void setCompactDisplayEnd(String compactDisplayEnd) {
		this.compactDisplayEnd = compactDisplayEnd;
	}
}

