/*
 * Created on 18 Feb 2007
 */
package org.sakaiproject.evaluation.tool.utils;

import uk.org.ponder.messageutil.MessageLocator;
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
  /** Fetches an array of message keys into an array of resolved messages. 
   * Primarily useful for internationalised selection controls. 
   */
  public static String[] fetchMessages(MessageLocator locator, String[] keys) {
    String[] togo = new String[keys.length];
    for (int i = 0; i < keys.length; ++ i) {
      togo[i] = locator.getMessage(keys[i]);
    }
    return togo;
  }
  
  public static void targetLabel(UIComponent label, UIComponent target) {
    label.decorators = new DecoratorList(new UILabelTargetDecorator(target));
  }
}
