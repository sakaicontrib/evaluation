#!/usr/bin/env python3
"""Replace EvaluationDao monolith usage with narrow port fields."""
import os
import re

ROOT = "/Users/samo/dev/evaluation"

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

IMPORTS = {
    "persistence": "EvaluationDaoBase",
    "settingsDao": "EvaluationSettingsDao",
    "emailTemplateDao": "EvaluationEmailTemplateDao",
    "authoringDao": "EvaluationAuthoringDao",
    "adminSupportDao": "EvaluationAdminSupportDao",
    "responseDao": "EvaluationResponseDao",
    "assignmentDao": "EvaluationAssignmentDao",
    "queryDao": "EvaluationQueryDao",
    "lockDao": "EvaluationLockDao",
    "consolidatedEmailDao": "EvaluationConsolidatedEmailDao",
}

JAVA_FILES = []
for dirpath, _, filenames in os.walk(ROOT):
    if "target" in dirpath.split(os.sep):
        continue
    for name in filenames:
        if name.endswith(".java"):
            JAVA_FILES.append(os.path.join(dirpath, name))

for path in JAVA_FILES:
    with open(path) as f:
        content = f.read()
    if "EvaluationDao" not in content and "dao." not in content:
        continue
    original = content

    content = content.replace("import org.sakaiproject.evaluation.dao.EvaluationDao;\n", "")
    content = re.sub(
        r"private EvaluationDao dao;\s*public void setDao\(EvaluationDao dao\) \{\s*this\.dao = dao;\s*\}",
        "",
        content,
        flags=re.MULTILINE,
    )
    content = re.sub(
        r"protected EvaluationDao evaluationDao;",
        "protected EvaluationDaoBase persistence;",
        content,
    )
    content = re.sub(
        r"EvaluationDao evaluationDao",
        "EvaluationDaoBase persistence",
        content,
    )
    content = re.sub(
        r"\(EvaluationDao\) applicationContext\.getBean\(\"org\.sakaiproject\.evaluation\.dao\.EvaluationDao\"\)",
        '(EvaluationDaoBase) applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationDaoBase")',
        content,
    )
    content = re.sub(
        r"EvalTestDataLoad\(EvaluationDao dao\)",
        "EvalTestDataLoad(EvaluationDaoBase persistence)",
        content,
    )
    content = re.sub(
        r"EvaluationTestDataDao\(EvaluationDao dao\)",
        "EvaluationTestDataDao(EvaluationDaoBase persistence)",
        content,
    )
    content = re.sub(
        r"private final EvaluationDao dao;",
        "private final EvaluationDaoBase persistence;",
        content,
    )

    used_fields = set()
    def replacer(match):
        method = match.group(1)
        field = METHOD_TO_FIELD.get(method)
        if field:
            used_fields.add(field)
            return f"{field}.{method}("
        return match.group(0)

    content = re.sub(r"\bdao\.([a-zA-Z0-9]+)\(", replacer, content)
    content = re.sub(r"\bevaluationDao\.([a-zA-Z0-9]+)\(", replacer, content)

    if used_fields and "setDao" not in original and "EvaluationDao " in original:
        # inject fields + setters after package/imports block for service-style classes
        field_block = []
        for field in sorted(used_fields):
            iface = IMPORTS[field]
            field_block.append(f"    private {iface} {field};")
            field_block.append(f"    public void set{field[0].upper()}{field[1:]}({iface} {field}) {{")
            field_block.append(f"        this.{field} = {field};")
            field_block.append("    }")
            field_block.append("")
        insertion = "\n".join(field_block)
        if insertion.strip() and insertion not in content:
            content = re.sub(
                r"(public class \w+[^{]+\{)",
                r"\1\n\n" + insertion,
                content,
                count=1,
            )

    # add imports
    import_lines = []
    for field in sorted(used_fields):
        iface = IMPORTS[field]
        line = f"import org.sakaiproject.evaluation.dao.{iface};"
        if line not in content:
            import_lines.append(line)
    if import_lines and "package " in content:
        content = re.sub(
            r"(package [^;]+;\n)",
            r"\1\n" + "\n".join(import_lines) + "\n",
            content,
            count=1,
        )

    if content != original:
        with open(path, "w") as f:
            f.write(content)
        print(f"Updated {path}")
