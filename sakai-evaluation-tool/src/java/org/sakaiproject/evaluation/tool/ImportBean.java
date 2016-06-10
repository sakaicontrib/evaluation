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
package org.sakaiproject.evaluation.tool;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.evaluation.logic.imports.EvalImportLogic;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;

/**
 * This is the backing bean of the XML data import process.
 * 
 * @author Dick Ellis (rwellis@umich.edu)
 * FIXME - DO NOT use sakai services directly
 */
public class ImportBean {

    private static final Log LOG = LogFactory.getLog(ImportBean.class);

    // injection
    private SessionManager sessionManager;
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    private EvalImportLogic evalImportLogic;
    public void setEvalImportLogic(EvalImportLogic evalImportLogic) {
        this.evalImportLogic = evalImportLogic;
    }

    private ContentHostingService contentHostingService;
    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    /**
     * Parse and load selected XML data file
     * 
     * @return String that is used to determine the place where control is to be sent in
     *         ControlImportProducer (reportNavigationCases method)
     * @throws SecurityException
     */
    @SuppressWarnings("unchecked")
    public String process() throws SecurityException {
        ToolSession toolSession = sessionManager.getCurrentToolSession();
        List<Reference> refs;
        String id = null;
        if (toolSession.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null
                && toolSession.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) {
            refs = (List<Reference>) toolSession.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
            if (refs == null || refs.size() != 1) {
                return "no-reference";
            }
            Reference ref = (Reference) refs.get(0);
            id = ref.getId();
        }
        try {
            contentHostingService.checkResource(id);
        } catch (PermissionException e) {
            return "permission-exception";
        } catch (IdUnusedException e) {
            return "idunused-exception";
        } catch (TypeException e) {
            return "type-exception";
        }
        try {
            evalImportLogic.load(id);
        } catch (Exception e) {
            return "exception";
        }

        toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
        toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
        return "importing";
    }

    /*
     * INITIALIZATION
     */
    public void init() {
        LOG.debug("INIT");
    }

}
