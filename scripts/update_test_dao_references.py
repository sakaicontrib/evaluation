#!/usr/bin/env python3
"""Fix evaluationDao references in test sources."""
import os
import re

ROOT = "/Users/samo/dev/evaluation/sakai-evaluation-impl/src/test"

METHOD_TO_FIELD = {}
for field, methods in {
    "persistence": [
        "forceCommit", "forceRollback", "fixupDatabase", "findById", "findAll",
        "countAll", "create", "save", "update", "delete",
    ],
    "settingsDao": [
        "countEvalConfigs", "getEvalConfigByName", "getAllEvalConfigs", "countEvalConfigsByNames",
    ],
    "emailTemplateDao": [
        "countDefaultEmailTemplates", "getDefaultEmailTemplates", "getEmailTemplates",
        "getDefaultEmailTemplate", "getEmailTemplateByEid", "getEvaluationsUsingEmailTemplate",
        "countEvaluationsUsingEmailTemplate", "deleteEmailTemplates",
    ],
    "authoringDao": [
        "countEvalScales", "getScaleByEid", "getScalesByIds", "getScalesWithNullMode",
        "getScalesForUser", "saveScales", "deleteScales", "countEvalItems", "getItemByEid",
        "getItemsByAutoUseTag", "getItemsForUser", "getItemsByIds", "getItemsUsingScale",
        "saveItems", "deleteItems", "countEvalItemGroups", "getItemGroupByTitle",
        "countTemplateById", "getTemplateByEid", "getTemplatesByAutoUseTag", "getTemplatesForUser",
        "countTemplatesForUser", "getTemplatesUsingItem", "getTemplateItemByEid",
        "getTemplateItemsByAutoUseTag", "getTemplateItemsByIds", "getTemplateItemsByHierarchyNodeId",
        "getOrphanedTemplateItems", "countTopLevelTemplateItems", "countBlockChildTemplateItems",
        "getBlockChildTemplateItems", "saveTemplateItemWithLinks", "saveTemplateItems",
        "deleteTemplateItems", "countEvaluationsByTitle", "getEvaluationsByTitle",
        "getEvalGroupNodesByNodeIds", "removeTemplateItems", "getItemGroups",
        "getItemGroupIdByItemId", "getTemplateItemsByTemplate", "getTemplateItemsByEvaluation",
    ],
    "adminSupportDao": [
        "getAllEvalAdmins", "getEvalAdminByUserId", "getAllHierarchyRules", "getHierarchyRuleById",
        "getHierarchyRulesByNodeId", "deleteHierarchyRules", "getAdhocUserByUsername",
        "getAdhocUserByEmail", "getAllAdhocUsers", "getAdhocUsersByIds", "getAdhocGroupsForOwner",
        "getEvalAdhocGroupsByUserAndPerm", "isUserAllowedInAdhocGroup",
    ],
    "responseDao": [
        "getEvaluationResponsesForUserAndGroup", "getEvaluationResponsesForUser", "countResponses",
        "getEvaluationResponses", "countEvaluationResponses", "saveResponseAndAnswers", "getAnswers",
        "getResponseIds", "removeResponses", "getResponseUserIds", "getResponsesSavedInProgress",
    ],
    "assignmentDao": [
        "getAssignUserByEid", "countEvaluationGroups", "getAssignGroupByEid", "countParticipantsForEval",
        "getApprovedAssignGroupsForEvaluation", "countApprovedAssignGroupsForEvaluation",
        "getAssignGroupByEvalAndGroupId", "getAssignHierarchyByEval", "getAssignGroupsForEvals",
        "countAssignGroupsByEvalAndGroupId", "deleteAssignmentsForEvaluation", "saveAssignHierarchyAndGroups",
        "getAssignHierarchiesByIds", "getAssignGroupsByEvalAndNodeIds", "deleteAssignHierarchyAndGroups",
        "saveAssignUsers", "deleteAssignUsersByIds", "deleteAssignUsersByAssignGroupIdExcludingStatus",
        "getEvalsWithoutUserAssignments", "getParticipantsForEval", "getViewableEvalGroupIds",
    ],
    "queryDao": [
        "countEvaluationsByIds", "countEvaluationById", "getEvaluationByEid", "countEvaluationsByTemplateId",
        "getEvaluationsByTemplateId", "getEvaluationsByTermId", "getEvaluationsByState",
        "getEvaluationsNotViewableOrDeleted", "getEvaluationsByCategory", "getEvalsUserCanTake",
        "getEvaluationsByEvalGroups", "getEvaluationsForOwnerAndGroups", "getEvalCategories",
        "getNodeIdForEvalGroup",
    ],
    "lockDao": [
        "lockScale", "lockItem", "lockTemplate", "lockEvaluation", "isUsedScale", "isUsedItem",
        "isUsedTemplate", "obtainLock", "releaseLock",
    ],
    "consolidatedEmailDao": [
        "getConsolidatedEmailMapping", "selectConsolidatedEmailRecipients", "resetConsolidatedEmailRecipients",
        "countDistinctGroupsInConsolidatedEmailMapping", "getAllSiteIDsMatchingSectionTitle",
        "getAllSiteIDsMatchingSiteTitle",
    ],
}.items():
    for method in methods:
        METHOD_TO_FIELD[method] = field

SET_DAO_REPLACEMENTS = [
    (r"evaluationService\.setDao\(evaluationDao\);",
     "DaoTestWiring.wireEvalEvaluationService(evaluationService, daoPorts);"),
    (r"authoringService\.setDao\(evaluationDao\);",
     "DaoTestWiring.wireEvalAuthoringService(authoringService, daoPorts);"),
    (r"authoringServiceImpl\.setDao\(evaluationDao\);",
     "DaoTestWiring.wireEvalAuthoringService(authoringServiceImpl, daoPorts);"),
    (r"evaluationSetupService\.setDao\(evaluationDao\);",
     "DaoTestWiring.wireEvalEvaluationSetupService(evaluationSetupService, daoPorts);"),
    (r"deliveryService\.setDao\(evaluationDao\);",
     "DaoTestWiring.wireEvalDeliveryService(deliveryService, daoPorts);"),
    (r"evalSettings\.setDao\(evaluationDao\);",
     "DaoTestWiring.wireEvalSettings(evalSettings, daoPorts);"),
    (r"reportingPermissions\.setDao\(evaluationDao\);",
     "DaoTestWiring.wireReportingPermissions(reportingPermissions, daoPorts);"),
    (r"hierarchyLogicImpl\.setDao\(evaluationDao\);",
     "DaoTestWiring.wireExternalHierarchyLogic(hierarchyLogicImpl, daoPorts);"),
    (r"adhocSupportLogic\.setDao\(evaluationDao\);",
     "DaoTestWiring.wireEvalAdhocSupport(adhocSupportLogic, daoPorts);"),
]

for dirpath, _, filenames in os.walk(ROOT):
    for name in filenames:
        if not name.endswith(".java"):
            continue
        path = os.path.join(dirpath, name)
        with open(path) as f:
            content = f.read()
        original = content

        def replacer(match):
            method = match.group(1)
            field = METHOD_TO_FIELD.get(method, "persistence")
            return f"{field}.{method}("

        content = re.sub(r"\bevaluationDao\.([a-zA-Z0-9]+)\(", replacer, content)
        content = content.replace("Assert.assertNotNull(evaluationDao);", "Assert.assertNotNull(persistence);")
        content = re.sub(
            r"evaluationDao = \(EvaluationDaoBase\) applicationContext\.getBean\(\"org\.sakaiproject\.evaluation\.dao\.EvaluationDaoBase\"\);",
            "loadDaoPorts();",
            content,
        )

        for pattern, replacement in SET_DAO_REPLACEMENTS:
            content = re.sub(pattern, replacement, content)

        if content != original:
            if "DaoTestWiring" in content and "import org.sakaiproject.evaluation.test.DaoTestWiring;" not in content:
                content = re.sub(
                    r"(package [^;]+;\n)",
                    r"\1\nimport org.sakaiproject.evaluation.test.DaoTestWiring;\n",
                    content,
                    count=1,
                )
            with open(path, "w") as f:
                f.write(content)
            print(f"Updated {path}")
