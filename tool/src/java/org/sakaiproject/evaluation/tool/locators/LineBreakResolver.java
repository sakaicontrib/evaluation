/**
 * $Id$
 * $URL$
 * LineBreakResolver.java - evaluation - Feb 29, 2008 6:53:09 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.locators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.org.ponder.beanutil.BeanResolver;


/**
 * This turns normal line breaks into <br/> in a string
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class LineBreakResolver implements BeanResolver {

   Pattern lineBreaks = Pattern.compile("(\r\n|\r|\n|\n\r)");
   String htmlBR = "<br/>";

   /* (non-Javadoc)
    * @see uk.org.ponder.beanutil.BeanResolver#resolveBean(java.lang.Object)
    */
   public String resolveBean(Object bean) {
      String text = bean.toString();
      Matcher m = lineBreaks.matcher(text);
      return m.replaceAll(htmlBR);
   }

}
