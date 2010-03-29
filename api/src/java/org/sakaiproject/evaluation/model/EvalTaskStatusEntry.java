package org.sakaiproject.evaluation.model;

/**
 * Model object representing a task status entry
 * @author rwellis
 */
public class EvalTaskStatusEntry {

	private String created;
	private String entryTag;
	private String payload;
	private String status;
	private String taskStatusEntryID;
	private String taskStatusEntryURL;
	private String taskStatusStreamID;

	public EvalTaskStatusEntry() {
	}
	
	public String getTaskStatusStreamID() {
		return taskStatusStreamID;
	}

	public void setTaskStatusStreamID(String taskStatusStreamID) {
		this.taskStatusStreamID = taskStatusStreamID;
	}

	public String getTaskStatusEntryID() {
		return taskStatusEntryID;
	}

	public void setTaskStatusEntryID(String taskStatusEntryID) {
		this.taskStatusEntryID = taskStatusEntryID;
	}

	public String getTaskStatusEntryURL() {
		return taskStatusEntryURL;
	}

	public void setTaskStatusEntryURL(String taskStatusEntryURL) {
		this.taskStatusEntryURL = taskStatusEntryURL;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getEntryTag() {
		return entryTag;
	}

	public void setEntryTag(String entryTag) {
		this.entryTag = entryTag;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}
	

}
	/*  http://localhost:8666/taskstatus?depth=2&otype=.TQContainer

	<taskStreamContainer>
	  	<streamCount>1</streamCount>
		 <streams>
			<taskStatusStream>
				<taskStatusStreamID>TSS20100325133341590</taskStatusStreamID>
	  			<created>Mar 25, 2010 1:33:41 PM</created>
	  			<time>Mar 25, 2010 1:33:41 PM</time>
	  			<status>CREATED</status>
	  	 		<streamTag></streamTag>
	  	   		<entryCount>1</entryCount>
		 		<statusEntries>
					<taskStatusEntry>
						<taskStatusStreamID>TSS20100325133341590</taskStatusStreamID>
						<taskStatusEntryID>TSE20100325133341596</taskStatusEntryID>
						<taskStatusEntryURL>http://localhost:8666/taskstatus/TSS20100325133341590/TSE20100325133341596/TSE20100325133341596</taskStatusEntryURL>
	  					<status>CREATED</status>
	 					<created>Mar 25, 2010 1:33:41 PM</created>
	  					<entryTag>no value</entryTag>
	  					<payload>no value</payload>
					</taskStatusEntry>
	    		</statusEntries>
			</taskStatusStream>
	    </streams>
	</taskStreamContainer>
 */


