/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.logic.externals;

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
    * Get the ID referencing to the content collection. 
    * @param siteId The site ID
    * @return The content collection ID in the form: "/group/{Site ID}"
    */
   public String getContentCollectionId(String siteId);
}
