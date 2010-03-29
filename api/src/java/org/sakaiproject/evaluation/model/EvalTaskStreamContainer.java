package org.sakaiproject.evaluation.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model object representing the task status container
 * @author rwellis
 */
public class EvalTaskStreamContainer {
	private Integer streamCount;
	private List<EvalTaskStatusStream> streams;
	
	// default constructor
	public EvalTaskStreamContainer() {
		streams = new ArrayList<EvalTaskStatusStream>();
	}
	
	// getters and setters
	public Integer getStreamCount() {
		return this.streamCount;
	}
	
	public void setStreamCount(Integer streamCount) {
		this.streamCount = streamCount;
	}
	
	public void addStream(EvalTaskStatusStream stream) {
		this.streams.add(stream);
	}
	
	public List<EvalTaskStatusStream> getStreams() {
		return this.streams;
	}
	
	public void setStreams(List<EvalTaskStatusStream> streams) {
		this.streams = streams;
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
