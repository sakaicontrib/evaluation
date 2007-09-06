/*
 * Created on 18 Feb 2007
 */
package org.sakaiproject.evaluation.tool.utils;

import uk.org.ponder.rsf.components.UIComponent;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;

/**
 * A set of low-level utilities for working with RSF.
 * Goal is that this file is perpetually empty, but since we are working with
 * public repo JARs for this project now there will in reality be some lag 
 * before they are folded into the framework.
 * 
 * @author Antranig Basman (amb26@ponder.org.uk)
 *
 */

public class RSFUtils {
 
  public static void targetLabel(UIComponent label, UIComponent target) {
    label.decorators = new DecoratorList(new UILabelTargetDecorator(target));
  }
}
