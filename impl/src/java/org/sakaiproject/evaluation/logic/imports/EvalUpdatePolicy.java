/**
 * 
 */
package org.sakaiproject.evaluation.logic.imports;

/**
 * EvalUpdatePolicy defines acceptable values for the optional "update_policy"
 * attribute of the "EVAL_DATA" element on import of evaluation entities. The
 * value of this attribute determines the policy that will apply to import of
 * elements whose EID duplicates the EID of an existing element of the same
 * class. Allowed values are "ignore", "reject", "replace" and "cascade".
 * 
 */
public enum EvalUpdatePolicy {

	/**
	 * A value of "ignore" indicates that attempts to import a duplicate element
	 * will be ignored silently. No changes will be made to the existing
	 * element, and no new element will be created.
	 */
	IGNORE("ignore"),

	/**
	 * A value of "reject" indicates that attempts to import a duplicate element
	 * will be rejected. No changes will be made to the existing element, and no
	 * new element will be created. An error will be reported (TODO: how will
	 * the error be reported?)
	 */
	REJECT("reject"),

	/**
	 * A value of "replace" indicates that attempts to import an element with an
	 * EID that is already in use for elements of that type will result in
	 * updates of the existing entity in the system. The existing values of all
	 * properties and attributes of the object, as well as any elements
	 * contained by it, will be removed and replaced with values supplied in the
	 * newly imported element. If the element is an eval_template or
	 * eval_templateitem, no changes will be made to any existing
	 * eval_evaluation or eval_item that were previously created based on the
	 * previous values of the element.
	 */
	REPLACE("replace"),

	/**
	 * A value of "cascade" indicates that attempts to import an element with an
	 * EID that is already in use for elements of that type will result in
	 * updates of the existing entity in the system and the changes in the
	 * element's properties and any contained elements will be cascaded to any
	 * elements that were previously created based on earlier values for the
	 * element.
	 */
	CASCADE("cascade");

	private String strVal;

	private EvalUpdatePolicy(String str) {
		this.strVal = str;
	}

	/**
	 * Get an EvalUpdatePolicy object corresponding to a string. 
	 * @param str
	 * @return
	 * @throws IllegalArgumentException if string parameter is null
	 * or does not identify a valid EvalUpdatePolicy.
	 */
	public static EvalUpdatePolicy fromString(String str) {
		EvalUpdatePolicy val = null;
		for (EvalUpdatePolicy eup : EvalUpdatePolicy.values()) {
			if (str != null && eup.strVal.equalsIgnoreCase(str.trim())) {
				val = eup;
				break;
			}
		}
		if (val == null) {
			throw new IllegalArgumentException();
		}
		return val;
	}

	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	public String toString() {
		return strVal;
	}
}
