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

import java.io.Serializable;
import java.util.List;

import org.sakaiproject.evaluation.model.EvalEvaluation;


/**
 * Handles entity interactions
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ExternalEntities {

   // ENTITIES

   /**
    * @return the URL directly to the main server portal this tool is installed in
    */
   public String getServerUrl();
   
   /**
    * Get the unique identifier for the server.
    * @return
    */
   public String getServerId();
   
   /**
    * Get a mapping of local servers (e.g. in a cluster) 
	* @return a mapping of servers from the unique identifier for a server to a display name for that server.
	*/
   public List<String> getServers();

   /**
    * Get a full URL to a specific entity inside our system,
    * if this entity has no direct URL then just provide a URL to the sakai server
    * 
    * @param evaluationEntity any entity inside the evaluation tool (e.g. {@link EvalEvaluation})
    * @return a full URL to the entity (e.g. http://sakai.server:8080/access/eval-evaluation/123/)
    */
   public String getEntityURL(Serializable evaluationEntity);

   /**
    * Get a full URL to a specific entity inside our system using just the class type and id,
    * if this entity has no direct URL then just provide a URL to the sakai server
    * 
    * @param entityPrefix an ENTITY_PREFIX constant from an entity provider
    * @param entityId the unique id of this entity (from getId() or similar) (e.g. 123)
    * @return a full URL to the entity (e.g. http://sakai.server:8080/access/eval-evaluation/123/)
    */
   public String getEntityURL(String entityPrefix, String entityId);

   /**
    * Creates a Sakai entity event for any internal entity which is registered with Sakai,
    * does nothing if the passed in entity type is not registered
    * 
    * @param eventName any string representing an event name (e.g. evaluation.created)
    * @param evaluationEntity any entity inside the evaluation tool (e.g. {@link EvalEvaluation})
    */
   public void registerEntityEvent(String eventName, Serializable evaluationEntity);

   /**
    * Creates a Sakai entity event for any internal entity which is registered with Sakai,
    * does nothing if the passed in entity class is not registered
    * 
    * @param eventName any string representing an event name (e.g. evaluation.created)
    * @param entityClass class type of the entity which this event pertains to
    * @param entityId unique id for the entity which this event pertains to
    */
   public void registerEntityEvent(String eventName, Class<? extends Serializable> entityClass, String entityId);

}
