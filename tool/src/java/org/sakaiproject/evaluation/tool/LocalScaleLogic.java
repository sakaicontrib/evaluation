/*
 * Created on 02nd Mar 2007
 */
package org.sakaiproject.evaluation.tool;

import java.util.Date;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalScalesLogic;
import org.sakaiproject.evaluation.model.EvalScale;

/*
 * A "Local DAO" to focus dependencies and centralise fetching logic 
 * for the Scales views.
 */
public class LocalScaleLogic {

	private EvalScalesLogic scalesLogic;
	public void setScalesLogic(EvalScalesLogic scalesLogic) {
		this.scalesLogic = scalesLogic;
	}

	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	public EvalScale fetchScale(Long scaleId) {
		return scalesLogic.getScaleById(scaleId);
	}
  
	public void saveScale(EvalScale tosave) {
		scalesLogic.saveScale(tosave, external.getCurrentUserId());
	}
  
	public void deleteScale(Long id) {
		scalesLogic.deleteScale(id, external.getCurrentUserId());
	}
  
	public EvalScale newScale() {
		EvalScale currScale = new EvalScale(new Date(), 
				external.getCurrentUserId(), null, "private", Boolean.FALSE);
		currScale.setOptions(new String[]{"", ""});
		return currScale;
	}
}
