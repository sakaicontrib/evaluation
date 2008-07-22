package org.sakaiproject.evaluation.logic.externals;

import java.io.InputStream;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;

/** 
 * This inteface provides methods to get content from Sakai's Resource and File
 * system.
 * @see EvalCommonLogic
 * 
 * @author Steven Githens (swgithen at mtu dot edu)
 */
public interface ExternalContent {
	
	/**
	 * Check that a content resource can be accessed.
	 * 
	 * @param resourceId the identifier of the content resource
	 * @return true if accessible, false if not accessible
	 */
	public boolean checkResource(String resourceId);

   /**
    * Get the contents of a file as a byte array. You should be fairly certain
    * the file you are fetching is not very large, as this is loading the entire
    * file into memory. Typical Sakai permissions
    * are in effect here, so for a large number of cases you may want to make 
    * your content publicly accessible.
    * 
    * @param abspath The absolute path to the file.  In a traditional Sakai 
    * setting, this would typically look something like "/group/mysiteid/mylogo.gif", 
    * to fetch a file that belongs to a particular site. 
    * @return The byte array for the hopefully not large file. Returns null if 
    * the file doesn't exist or there are errors.
    */
   public byte[] getFileContent(String abspath);
   
   /**
    * Get the resource id of the content resource that was imported.
    * 
    * @return the id
    */
   public String getImportedResourceId();
   
   /**
    * Get the contents of a content resource as a stream.
    * 
    * @param resourceId the identifier of the content resource
    * @return the input stream
    */
   public InputStream getStreamContent(String resourceId);
   
   /**
    * Remove the identified resource.
    * 
    * @param resourceId the resource identifier
    * @return true if successful, false otherwise
    */
   public boolean removeResource(String resourceId);
   
   /**
    * Set the attributes of the imported content resource.
    */
   public void setImportedResourceAttributes();
}
