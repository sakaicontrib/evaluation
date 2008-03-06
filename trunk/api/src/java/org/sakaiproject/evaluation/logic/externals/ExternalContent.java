package org.sakaiproject.evaluation.logic.externals;

/** 
 * This inteface provides methods to get content from Sakai's Resource and File
 * system.
 * @see EvalExternalLogic
 * 
 * @author Steven Githens (swgithen at mtu dot edu)
 */
public interface ExternalContent {

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
}
