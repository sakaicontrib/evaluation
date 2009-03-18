/**
 * ImportBean.java - evaluation - 9 Mar 2007 11:35:56 AM - rwellis
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
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

    private static Log log = LogFactory.getLog(ImportBean.class);

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
        List<Reference> refs = null;
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
        log.debug("INIT");
    }

}
