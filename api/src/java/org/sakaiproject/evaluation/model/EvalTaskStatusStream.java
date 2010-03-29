package org.sakaiproject.evaluation.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model object representing a task status stream
 * @author rwellis
 */
public class EvalTaskStatusStream {
	
	private String created;
	private Integer entryCount;
	private String status;
	private String streamTag;
	private String taskStatusStreamID;
	private String time;
	private List<EvalTaskStatusEntry> statusEntries;
	
	public EvalTaskStatusStream() {
		statusEntries = new ArrayList<EvalTaskStatusEntry>();
	}
	public String getTaskStatusStreamID() {
		return taskStatusStreamID;
	}

	public void setTaskStatusStreamID(String taskStatusStreamID) {
		this.taskStatusStreamID = taskStatusStreamID;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStreamTag() {
		return streamTag;
	}

	public void setStreamTag(String streamTag) {
		this.streamTag = streamTag;
	}

	public Integer getEntryCount() {
		return entryCount;
	}

	public void setEntryCount(Integer entryCount) {
		this.entryCount = entryCount;
	}

	public List<EvalTaskStatusEntry> getStatusEntries() {
		return statusEntries;
	}

	public void setStatusEntries(List<EvalTaskStatusEntry> statusEntries) {
		this.statusEntries = statusEntries;
	}
	
	public void addStatusEntry(EvalTaskStatusEntry entry) {
		this.statusEntries.add(entry);
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

}
