package org.sakaiproject.evaluation.logic;

public interface EvalSingleEmailLogic {
	
	 /**
	  * Build the email to be sent
	  * 
	  * @param serverId
	  * @param holdLock
	  * 		the length of time to hold the lock (purposely very long)
	  */
	 public void buildSingleEmail(String serverId, long holdLock);
	 
	 /**
	  * Send email using throttling if set
	  * 
	  * @param serverId
	  * 			the identity of the server sending email
	  * @param holdLock
	  * 			the length of time to hold the lock (purposely very long)
	  */
	 public void sendSingleEmail(String serverId, long holdLock);
}
