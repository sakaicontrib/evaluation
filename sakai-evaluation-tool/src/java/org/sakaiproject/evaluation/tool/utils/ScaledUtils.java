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

   private static final Log LOG = LogFactory.getLog(ScaledUtils.class);

	public static String[] idealKeys = {
		EvalConstants.SCALE_IDEAL_NONE,
		EvalConstants.SCALE_IDEAL_LOW, 
		EvalConstants.SCALE_IDEAL_MID, 
		EvalConstants.SCALE_IDEAL_HIGH, 
		EvalConstants.SCALE_IDEAL_OUTSIDE};

	public static String[] scaleStartClass = {
        "compactDisplayNeutral",
        "compactDisplayPositive",
        "compactDisplayNegative",
        "compactDisplayNegative",
        "compactDisplayPositive",
	};

	public static String[] scaleEndClass = {
        "compactDisplayNeutral",
        "compactDisplayNegative",
        "compactDisplayNegative",
        "compactDisplayPositive",
        "compactDisplayPositive",
	};

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
         LOG.info("Could not find index for scale ("+scale.getId()+") for ideal setting: " + scale.getIdeal() + ", setting to default of 0 (no ideal)");
         index = 0;
      }
      return index;
	}

	public static String getIdealImageURL(EvalScale scale) {
		return EvalToolConstants.COLORED_IMAGE_URLS[idealIndex(scale)];
	}

	public static String getStartClass(EvalScale scale) {
		return scaleStartClass[idealIndex(scale)];
	}

	public static String getEndClass(EvalScale scale) {
		return scaleEndClass[idealIndex(scale)];
	}

	/**
	 * Produce scale labels for a list of scales that can be used in a pulldown menu
	 * 
	 * @param scales a list of {@link EvalScale}
	 * @return an array of labels for the passed in scales
	 */
	public static String[] getScaleLabels(List<EvalScale> scales) {
		List<String> scaleLabels = new ArrayList<>();
		for (EvalScale scale : scales) {
		    // ensure only real scales are included
            if (scale.getId() != null) {
                scaleLabels.add( makeScaleText(scale, 90) );
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
		List<String> scaleValues = new ArrayList<>();
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
