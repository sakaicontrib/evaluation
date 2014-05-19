package org.sakaiproject.evaluation.logic.exceptions;

public class InvalidEvalCategoryException extends RuntimeException {
	
	/**
	 * This is the message key used to inform the user of the invalid evaluation category
	 */
	public String messageKey = "evalsettings.invalid.eval.category";
	
	public InvalidEvalCategoryException(String message) {
		super(message);
	}
	
	public InvalidEvalCategoryException(Throwable ex) {
		super(ex);
	}
	
	public InvalidEvalCategoryException(String message, Throwable ex) {
		super(message, ex);
	}
	
}
