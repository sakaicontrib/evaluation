/**
 * RSFUtils.java - evaluation - Feb 18, 2007 11:35:56 AM - antranig
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

import uk.org.ponder.rsf.components.UIBound;
import uk.org.ponder.rsf.components.UIComponent;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIDisabledDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;

/**
 * A set of low-level utilities for working with RSF.
 * Goal is that this file is perpetually empty, but since we are working with
 * public repo JARs for this project now there will in reality be some lag 
 * before they are folded into the framework.
 * 
 * @author Antranig Basman (amb26@ponder.org.uk)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@SuppressWarnings("deprecation")
public class RSFUtils {
 
  public static void targetLabel(UIComponent label, UIComponent target) {
    label.decorators = new DecoratorList(new UILabelTargetDecorator(target));
  }

  /**
   * This method is used to make things appear disabled where ever needed,
   * should work correctly with most components
   * 
   * @param component any RSF component
   */
  public static void disableComponent(UIComponent component) {
     component.decorate( new UIDisabledDecorator(true) );
     if (component instanceof UISelect) {
        component = ((UISelect)component).selection;
     }
     if (component instanceof UIBound) {
        ((UIBound)component).fossilize = false;
        ((UIBound)component).willinput = false;
     }
  }

}
