/**
 * ScaledUtils.java - evaluation - Feb 19, 2007 11:35:56 AM - antranig
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.stringutil.StringUtil;

/**
 * Utilities for manipulating scales.
 * 
 * @author Antranig Basman (amb26@ponder.org.uk)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ScaledUtils {

   private static Log log = LogFactory.getLog(ScaledUtils.class);

	public static String[] idealKeys = {
		EvalConstants.SCALE_IDEAL_NONE,
		EvalConstants.SCALE_IDEAL_LOW, 
		EvalConstants.SCALE_IDEAL_MID, 
		EvalConstants.SCALE_IDEAL_HIGH, 
		EvalConstants.SCALE_IDEAL_OUTSIDE};

	public static String[] startColours = {
		EvalToolConstants.BLUE_COLOR, 
		EvalToolConstants.GREEN_COLOR,
		EvalToolConstants.RED_COLOR,
		EvalToolConstants.RED_COLOR,
		EvalToolConstants.GREEN_COLOR};

	public static String[] endColours = {
		EvalToolConstants.BLUE_COLOR, 
		EvalToolConstants.RED_COLOR,
		EvalToolConstants.RED_COLOR,
		EvalToolConstants.GREEN_COLOR,
		EvalToolConstants.GREEN_COLOR};

	public static int idealIndex(EvalScale scale) {
      int index = -1;
      for (int i = 0; i < idealKeys.length; ++ i) {
         if (StringUtil.equals(scale.getIdeal(), idealKeys[i])) {
            index = i;
            break;
         }
      }
      if (index == -1) {
         // Fix for http://www.caret.cam.ac.uk/jira/browse/CTL-562 - added to ensure this will not cause a failure
         log.warn("Could not find index for scale ("+scale.getId()+") for ideal setting: " + scale.getIdeal() + ", setting to default of 0 (no ideal)");
         index = 0;
      }
      return index;
	}

	public static String getIdealImageURL(EvalScale scale) {
		return EvalToolConstants.COLORED_IMAGE_URLS[idealIndex(scale)];
	}

	public static Color getStartColor(EvalScale scale) {
		return Color.decode(startColours[idealIndex(scale)]);
	}

	public static Color getEndColor(EvalScale scale) {
		return Color.decode(endColours[idealIndex(scale)]);
	}

	/**
	 * Produce scale labels for a list of scales that can be used in a pulldown menu
	 * 
	 * @param scales a list of {@link EvalScale}
	 * @return an array of labels for the passed in scales
	 */
	public static String[] getScaleLabels(List<EvalScale> scales) {
		List<String> scaleLabels = new ArrayList<String>();
		for (EvalScale scale : scales) {
		    // ensure only real scales are included
            if (scale.getId() != null) {
                scaleLabels.add( makeScaleText(scale, 120) );
            }
		}
		return (String[]) scaleLabels.toArray(new String[scaleLabels.size()]);
	}

	/**
	 * Produce values for a list of scales that can be used in a pulldown menu
	 * 
	 * @param scales a list of {@link EvalScale}
	 * @return an array of values for the passed in scales
	 */
	public static String[] getScaleValues(List<EvalScale> scales) {
		List<String> scaleValues = new ArrayList<String>();
        for (EvalScale scale : scales) {
            // ensure only real scales are included
			if (scale.getId() != null) {
			    scaleValues.add(scale.getId().toString());
			}
		}
		return (String[]) scaleValues.toArray(new String[] {});
	}

   /**
    * Turns a scale in user displayable text,
    * works for any scale (not just for scaled mode)
    * 
    * @param scale any persisted scale
    * @param maxLength the maximum length of this text
    * @return a string suitable for display to the user
    */
   public static String makeScaleText(EvalScale scale, int maxLength) {
      StringBuilder scaleText = new StringBuilder();
      if (EvalConstants.SCALE_MODE_SCALE.equals(scale.getMode())) {
         scaleText.append( scale.getOptions().length );
         scaleText.append( " pt - " ); // I18n?
         scaleText.append( scale.getTitle() );
         scaleText.append( " (" );
      } else {
         scaleText.append( "Options: " ); // I18n?
      }
      for (int j = 0; j < scale.getOptions().length; j++) {
         scaleText.append( (j==0 ? "" : ",") );
         scaleText.append( scale.getOptions()[j] );
      }
      if (EvalConstants.SCALE_MODE_SCALE.equals(scale.getMode())) {
         scaleText.append( ")" );
      }
      return EvalUtils.makeMaxLengthString(scaleText.toString(), maxLength);
   }

}
