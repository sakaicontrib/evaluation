/******************************************************************************
 * TestEGProviderProducer.java - created by aaronz on 12 Mar 2007
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.providers.EvalGroupsProvider;
import org.sakaiproject.evaluation.tool.viewparams.AdminTestEGViewParameters;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handles the choose existing items view
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class AdminTestEGProviderProducer implements ViewComponentProducer, ViewParamsReporter, ApplicationContextAware {

    /**
     * Used for navigation within the system, this must match with the template name
     */
    public static final String VIEW_ID = "admin_test_evalgroup_provider";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    // pulling the spring app context, bad bad -AZ
    private ApplicationContext applicationContext;
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        String warningMessage = "";
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
        if (! userAdmin) {
            // Security check and denial
            throw new SecurityException("Non-admin users may not access this page");
        }

        // get provider
        EvalGroupsProvider evalGroupsProvider;
        String providerBeanName = EvalGroupsProvider.class.getName();
        if (applicationContext.containsBean(providerBeanName)) {
            evalGroupsProvider = (EvalGroupsProvider) applicationContext.getBean(providerBeanName);
        } else {
            // no provider, die horribly
            UIOutput.make(tofill, "warning-message", "No EvalGroupsProvider found... cannot test...");
            return;
        }

        AdminTestEGViewParameters testViewParameters = (AdminTestEGViewParameters) viewparams;
        String username = testViewParameters.username;
        String evalGroupId = testViewParameters.evalGroupId;
        if (evalGroupId == null || evalGroupId.equals("")) {
            warningMessage += " No evalGroupId set";
            UIOutput.make(tofill, "warning-message", warningMessage);
        } else {
            String title = commonLogic.getDisplayTitle(evalGroupId);
            if ("--------".equals(title)) {
                warningMessage += " Invalid evalGroupId ("+evalGroupId+"): cannot find title, reset to null";
                UIOutput.make(tofill, "warning-message", warningMessage);
                evalGroupId = null;
            }
        }
        String userId = currentUserId;
        if (username == null || username.equals("")) {
            username = commonLogic.getUserUsername(userId);
            warningMessage += " No username set: using current user";
            UIOutput.make(tofill, "warning-message", warningMessage);
        } else {
            userId = commonLogic.getUserId(username);
            if (userId == null) {
                warningMessage += " Invalid username ("+username+"): cannot find id, setting to current user id";
                UIOutput.make(tofill, "warning-message", warningMessage);
                userId = currentUserId;
            }
        }

        UIMessage.make(tofill, "page-title", "admintesteg.page.title");
        UIInternalLink.make(tofill, "summary-link", UIMessage.make("summary.page.title"), 
                new SimpleViewParameters(SummaryProducer.VIEW_ID));
        UIInternalLink.make(tofill, "administrate-link", UIMessage.make("administrate.page.title"), 
                new SimpleViewParameters(AdministrateProducer.VIEW_ID));

        UIMessage.make(tofill, "current_test_header", "admintesteg.current.test.header");
        UIMessage.make(tofill, "current_test_user_header", "admintesteg.current.test.user.header");
        UIOutput.make(tofill, "current_test_user", username);
        UIMessage.make(tofill, "current_test_group_header", "admintesteg.current.test.group.header");
        UIOutput.make(tofill, "current_test_group", evalGroupId);

        // create a copy of the VP (to avoid corrupting the original)
        AdminTestEGViewParameters ategvp = (AdminTestEGViewParameters) testViewParameters.copyBase();
        ategvp.username = username;
        ategvp.evalGroupId = evalGroupId;

        UIForm searchForm = UIForm.make(tofill, "current_test_form", ategvp);
        UIInput.make(searchForm, "current_test_user_input", "#{username}");
        UIInput.make(searchForm, "current_test_group_input", "#{evalGroupId}");
        UIMessage.make(searchForm, "current_test_form_command", "admintesteg.test.command" );

        UIMessage.make(tofill, "major_test_header", "admintesteg.major.test.header");
        UIMessage.make(tofill, "test_method_header", "admintesteg.test.method.header");
        UIMessage.make(tofill, "test_result_header", "admintesteg.test.result.header");
        UIMessage.make(tofill, "test_runtime_header", "admintesteg.test.runtime.header");

        long startTime = 0;
        long total = 0;
        Set<String> s = null;
        List<EvalGroup> l = null;

        // now run the tests
        UIBranchContainer tests1 = UIBranchContainer.make(tofill, "tests_list:", "getUserIdsForEvalGroups.PERM_BE_EVALUATED");
        UIOutput.make(tests1, "test_method", tests1.localID);
        if (evalGroupId == null) {
            UIMessage.make(tests1, "test_result", "admintesteg.test.result.skipped.nogroup");
            UIMessage.make(tests1, "test_runtime", "admintesteg.test.runtime.message", new Object[] {0+""});
        } else {
            startTime = new Date().getTime();
            s = evalGroupsProvider.getUserIdsForEvalGroups(new String[] {evalGroupId}, EvalGroupsProvider.PERM_BE_EVALUATED);
            UIOutput.make(tests1, "test_result", collectionToString(s));
            total = new Date().getTime() - startTime;
            UIMessage.make(tests1, "test_runtime", "admintesteg.test.runtime.message", new Object[] {new Long(total)});
        }

        UIBranchContainer tests1b = UIBranchContainer.make(tofill, "tests_list:", "getUserIdsForEvalGroups.PERM_TAKE_EVALUATION");
        UIOutput.make(tests1b, "test_method", tests1b.localID);
        if (evalGroupId == null) {
            UIMessage.make(tests1b, "test_result", "admintesteg.test.result.skipped.nogroup");
            UIMessage.make(tests1b, "test_runtime", "admintesteg.test.runtime.message", new Object[] {0+""});
        } else {
            startTime = new Date().getTime();
            s = evalGroupsProvider.getUserIdsForEvalGroups(new String[] {evalGroupId}, EvalGroupsProvider.PERM_TAKE_EVALUATION);
            UIOutput.make(tests1b, "test_result", collectionToString(s));
            total = new Date().getTime() - startTime;
            UIMessage.make(tests1b, "test_runtime", "admintesteg.test.runtime.message", new Object[] {new Long(total)});
        }

        UIBranchContainer tests2 = UIBranchContainer.make(tofill, "tests_list:", "countUserIdsForEvalGroups.PERM_BE_EVALUATED");
        UIOutput.make(tests2, "test_method", tests2.localID);
        if (evalGroupId == null) {
            UIMessage.make(tests2, "test_result", "admintesteg.test.result.skipped.nogroup");
            UIMessage.make(tests2, "test_runtime", "admintesteg.test.runtime.message", new Object[] {0+""});
        } else {
            startTime = new Date().getTime();
            int count = evalGroupsProvider.countUserIdsForEvalGroups(new String[] {evalGroupId}, EvalGroupsProvider.PERM_BE_EVALUATED);
            UIOutput.make(tests2, "test_result", count+"");
            total = new Date().getTime() - startTime;
            UIMessage.make(tests2, "test_runtime", "admintesteg.test.runtime.message", new Object[] {new Long(total)});
        }

        UIBranchContainer tests2b = UIBranchContainer.make(tofill, "tests_list:", "countUserIdsForEvalGroups.PERM_TAKE_EVALUATION");
        UIOutput.make(tests2b, "test_method", tests2b.localID);
        if (evalGroupId == null) {
            UIMessage.make(tests2b, "test_result", "admintesteg.test.result.skipped.nogroup");
            UIMessage.make(tests2b, "test_runtime", "admintesteg.test.runtime.message", new Object[] {0+""});
        } else {
            startTime = new Date().getTime();
            int count = evalGroupsProvider.countUserIdsForEvalGroups(new String[] {evalGroupId}, EvalGroupsProvider.PERM_TAKE_EVALUATION);
            UIOutput.make(tests2b, "test_result", count+"");
            total = new Date().getTime() - startTime;
            UIMessage.make(tests2b, "test_runtime", "admintesteg.test.runtime.message", new Object[] {new Long(total)});
        }

        UIBranchContainer tests3 = UIBranchContainer.make(tofill, "tests_list:", "getEvalGroupsForUser.PERM_BE_EVALUATED");
        UIOutput.make(tests3, "test_method", tests3.localID);
        startTime = new Date().getTime();
        l = evalGroupsProvider.getEvalGroupsForUser(userId, EvalGroupsProvider.PERM_BE_EVALUATED);
        UIOutput.make(tests3, "test_result", collectionToString(l));
        total = new Date().getTime() - startTime;
        UIMessage.make(tests3, "test_runtime", "admintesteg.test.runtime.message", new Object[] {new Long(total)});

        UIBranchContainer tests3b = UIBranchContainer.make(tofill, "tests_list:", "getEvalGroupsForUser.PERM_TAKE_EVALUATION");
        UIOutput.make(tests3b, "test_method", tests3b.localID);
        startTime = new Date().getTime();
        l = evalGroupsProvider.getEvalGroupsForUser(userId, EvalGroupsProvider.PERM_TAKE_EVALUATION);
        UIOutput.make(tests3b, "test_result", collectionToString(l));
        total = new Date().getTime() - startTime;
        UIMessage.make(tests3b, "test_runtime", "admintesteg.test.runtime.message", new Object[] {new Long(total)});

        UIBranchContainer tests4 = UIBranchContainer.make(tofill, "tests_list:", "countEvalGroupsForUser.PERM_BE_EVALUATED");
        UIOutput.make(tests4, "test_method", tests4.localID);
        startTime = new Date().getTime();
        int count4 = evalGroupsProvider.countEvalGroupsForUser(userId, EvalGroupsProvider.PERM_BE_EVALUATED);
        UIOutput.make(tests4, "test_result", count4+"");
        total = new Date().getTime() - startTime;
        UIMessage.make(tests4, "test_runtime", "admintesteg.test.runtime.message", new Object[] {new Long(total)});

        UIBranchContainer tests4b = UIBranchContainer.make(tofill, "tests_list:", "countEvalGroupsForUser.PERM_TAKE_EVALUATION");
        UIOutput.make(tests4b, "test_method", tests4b.localID);
        startTime = new Date().getTime();
        int count4b = evalGroupsProvider.countEvalGroupsForUser(userId, EvalGroupsProvider.PERM_TAKE_EVALUATION);
        UIOutput.make(tests4b, "test_result", count4b+"");
        total = new Date().getTime() - startTime;
        UIMessage.make(tests4b, "test_runtime", "admintesteg.test.runtime.message", new Object[] {new Long(total)});

        UIBranchContainer tests5 = UIBranchContainer.make(tofill, "tests_list:", "getGroupByGroupId");
        UIOutput.make(tests5, "test_method", tests5.localID);
        if (evalGroupId == null) {
            UIMessage.make(tests5, "test_result", "admintesteg.test.result.skipped.nogroup");
            UIMessage.make(tests5, "test_runtime", "admintesteg.test.runtime.message", new Object[] {0+""});
        } else {
            startTime = new Date().getTime();
            EvalGroup result5 = evalGroupsProvider.getGroupByGroupId(evalGroupId);
            UIOutput.make(tests5, "test_result", result5.toString());
            total = new Date().getTime() - startTime;
            UIMessage.make(tests5, "test_runtime", "admintesteg.test.runtime.message", new Object[] {new Long(total)});
        }

        UIBranchContainer tests6 = UIBranchContainer.make(tofill, "tests_list:", "isUserAllowedInGroup.PERM_BE_EVALUATED");
        UIOutput.make(tests6, "test_method", tests6.localID);
        if (evalGroupId == null) {
            UIMessage.make(tests6, "test_result", "admintesteg.test.result.skipped.nogroup");
            UIMessage.make(tests6, "test_runtime", "admintesteg.test.runtime.message", new Object[] {0+""});
        } else {
            startTime = new Date().getTime();
            boolean result = evalGroupsProvider.isUserAllowedInGroup(userId, EvalGroupsProvider.PERM_BE_EVALUATED, evalGroupId);
            UIOutput.make(tests6, "test_result", result+"");
            total = new Date().getTime() - startTime;
            UIMessage.make(tests6, "test_runtime", "admintesteg.test.runtime.message", new Object[] {new Long(total)});
        }

        UIBranchContainer tests6b = UIBranchContainer.make(tofill, "tests_list:", "isUserAllowedInGroup.PERM_TAKE_EVALUATION");
        UIOutput.make(tests6b, "test_method", tests6b.localID);
        if (evalGroupId == null) {
            UIMessage.make(tests6b, "test_result", "admintesteg.test.result.skipped.nogroup");
            UIMessage.make(tests6b, "test_runtime", "admintesteg.test.runtime.message", new Object[] {0+""});
        } else {
            startTime = new Date().getTime();
            boolean result = evalGroupsProvider.isUserAllowedInGroup(userId, EvalGroupsProvider.PERM_TAKE_EVALUATION, evalGroupId);
            UIOutput.make(tests6b, "test_result", result+"");
            total = new Date().getTime() - startTime;
            UIMessage.make(tests6b, "test_runtime", "admintesteg.test.runtime.message", new Object[] {new Long(total)});
        }

    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new AdminTestEGViewParameters();
    }


    /**
     * Turn a collection into something that can be viewed easily onscreen
     * @param c any Collection
     * @return a String representing that collection
     */
    private String collectionToString(Collection<?> c) {
        StringBuilder sb = new StringBuilder();
        sb.append("(#:" + c.size() + ")");
        for (Iterator<?> iter = c.iterator(); iter.hasNext();) {
            sb.append( ", " + iter.next().toString() );
        }
        return sb.toString();
    }

}
