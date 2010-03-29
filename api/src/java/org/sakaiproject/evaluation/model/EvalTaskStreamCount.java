package org.sakaiproject.evaluation.model;

/**
 * A wrapper for count of task status streams
 *	<taskStreamCount>
		<streamCount>1</streamCount>
	</taskStreamCount>
 * when ?otype=.taskStreamCount
 * 
 * @author rwellis
 *
 */
public class EvalTaskStreamCount {
	
	private Integer count;

	// default constructor
	public EvalTaskStreamCount() {	
	}
	
	// getters and setters
	public EvalTaskStreamCount(Integer count) {
		this.count = count;
	}
	
	public Integer getCount() {
		return this.count;
	}
	
	public void setCount(Integer count) {
		this.count = count;
	}
}
