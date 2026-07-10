# AGENTS.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

**Standard Build:**
```bash
mvn clean install
```

**Deploy to Sakai Instance:**
```bash
mvn clean install sakai:deploy -Dmaven.tomcat.home=${SAKAI_DIRECTORY}
```

**Profile-Based Builds:**
- Full build (default): `mvn clean install`
- API only: `mvn clean install -Papi`
- Tool only: `mvn clean install -Ptool`
- DDL only: `mvn clean install -Pddl`

**License Management:**
```bash
mvn license:format  # Fix license headers on all files
mvn license:check   # Verify license headers
```

## Project Architecture

This is the **Sakai Evaluation System (EVALSYS)** - a multi-module Maven project for conducting course and instructor evaluations within the Sakai LMS.

### Module Structure
- **sakai-evaluation-api**: Public interfaces, model classes, constants, and contracts
- **sakai-evaluation-impl**: Service implementations, DAO layer, business logic
- **sakai-evaluation-tool**: Web UI layer (Spring MVC + Thymeleaf)

### Core Service Architecture

**Primary Services (all in `org.sakaiproject.evaluation.logic`):**
- `EvalAuthoringService` - Template/item/scale authoring and management
- `EvalEvaluationService` - Core evaluation retrieval and operations
- `EvalDeliveryService` - Response saving and evaluation delivery to users
- `EvalEvaluationSetupService` - Evaluation creation and assignment management
- `EvalCommonLogic` - Internal operations (users, groups, security, email)
- `EvalSettings` - System configuration and settings management

**External Integration Pattern:**
Services use `External*` interfaces (in `logic.externals` package) to abstract Sakai-specific functionality, enabling platform independence.

### Domain Model

**Core Entities:**
- `EvalEvaluation` - The evaluation instance users complete
- `EvalTemplate` - Reusable evaluation structure/layout
- `EvalItem` - Reusable questions with different types (scaled, text, choice)
- `EvalScale` - Rating scales for scaled questions
- `EvalResponse` - User's complete response to an evaluation
- `EvalAnswer` - Individual answers within responses

**Assignment System:**
- `EvalAssignGroup` - Groups assigned to evaluations
- `EvalAssignUser` - Individual user assignments
- `EvalAssignHierarchy` - Institutional hierarchy assignments

### State Management
Evaluations follow a defined lifecycle: Partial → InQueue → Active → GracePeriod → Closed → Viewable. State transitions are date-driven and determine permitted operations.

## Sakai Development Discipline

### Service boundaries
- Put business rules, parsing, import/copy semantics, workflow decisions, and cross-object coordination in services, not controllers, entity providers, repositories, or UI glue.
- Keep controllers and entity providers focused on request/response handling, navigation, model setup, and framework integration; delegate behavior to services.
- Keep DAOs focused on persistence of mapped entities and explicit queries. Do not move DTO assembly, adapters, or tool workflow decisions into DAO implementations.
- Prefer existing Sakai services and utilities before adding local helpers or infrastructure, including `ServerConfigurationService`, Sakai locale/timezone services, `SiteService`, `UserDirectoryService`, scheduler services, and standard Sakai XML/Jackson utilities.

### State and context
- Do not store request-specific state such as user id, site id, locale, timezone, or request parameters in singleton Spring beans/controllers.
- Do not use `ThreadLocal`, static fields, or singleton bean fields to pass request/import/user/site-specific state through service calls. Sakai runs on pooled application-server threads, so thread-bound state can leak across requests if cleanup is missed.
- Prefer explicit return values, operation-scoped helper objects, DTO/result objects, or method parameters.
- For background work, verify session, user, site, and security-context assumptions explicitly. Prefer Sakai scheduler/executor services over creating new thread pools.

### Locale, timezone, and formatting
- Use Sakai's centrally resolved locale and timezone, not browser/request defaults such as `Accept-Language`, `HttpServletRequest#getLocale()`, browser timezone, or framework default resolvers.
- Respect the effective locale for the current context, including site locale when configured and user preferences otherwise.
- Format UI messages, numbers, dates, and times with the same Sakai-resolved locale; display dates/times in the site or user's preferred timezone.

### Java and XML
- Do not use Java local variable type inference (`var`); always declare explicit types.
- For DOM XML parsing, use `org.sakaiproject.util.Xml.createSecureDocumentBuilderFactory()` rather than manually configuring `DocumentBuilderFactory`.

## Web UI Layer (Spring MVC + Thymeleaf)

The tool UI was fully migrated from RSF to Spring MVC + Thymeleaf. There is no RSF code remaining.

### Layout
- **Controllers**: `sakai-evaluation-tool/src/java/org/sakaiproject/evaluation/tool/controllers/`
- **Templates**: `sakai-evaluation-tool/src/webapp/WEB-INF/templates/` (`.html`)
- **Fragments**: `sakai-evaluation-tool/src/webapp/WEB-INF/templates/fragments/` — `nav.html`, `eval_item.html`, `take_eval_item.html`
- **Spring MVC config**: `sakai-evaluation-tool/src/webapp/WEB-INF/evaluation-mvc-servlet.xml`
- **App context**: `sakai-evaluation-tool/src/webapp/WEB-INF/applicationContext.xml`

### SakaiSkinInterceptor
`SakaiSkinInterceptor` runs after every request and adds these model attributes to all views:
- `skinRepo`, `skinDefault` — CSS paths for Sakai skin
- `mainFrameId` — iframe ID for `setMainFrameHeight()`
- `sakaiHtmlHead` — Sakai portal head HTML (required for CKEditor, etc.)
- `navItems` — list of navigation tabs (role-dependent)

### Template conventions
Every template must include:
```html
<script th:utext="${sakaiHtmlHead}"></script>
<script src="/library/js/headscripts.js"></script>
<link th:href="${skinRepo + '/tool_base.css'}" rel="stylesheet"/>
<link th:href="${skinRepo + '/' + skinDefault + '/tool.css'}" rel="stylesheet"/>
<link th:href="@{/content/css/evaluation_base.css}" rel="stylesheet"/>
```

The root element must use `<div class="portletBody evaluation">`.

All new `.java` and `.html` files require an ECL-2.0 license header or the build fails (`mvn license:check`).

### Thymeleaf restrictions
- No `T()` expressions or type casts in templates — move logic to the controller.
- No duplicate `th:text` on the same element.
- Use `@{${url}}` (not `${url}`) for context-relative URLs built in the controller.
- Spring Security is active; SpEL expressions in templates are restricted.

### Modals
Bootstrap 5 Modal is used throughout. There is no Facebox dependency. Do not add Facebox.

### Transactions in controllers
Controllers that load an entity and then save it must wrap the operation in `daoInvoker.invokeTransactionalAccess()` to avoid `NonUniqueObjectException`.

### Report export
Downloads are served by `ReportViewController` at `GET /report_view/download` with parameters `evaluationId`, `groupIds[]`, `type`, `filename`, and optionally `userId` (for individual PDF). Export types are defined as constants in `EvalEvaluationService` (e.g. `XLS_RESULTS_REPORT`, `PDF_RESULTS_REPORT_INDIVIDUAL`).

### CSS
- Main stylesheet: `sakai-evaluation-tool/src/webapp/content/css/scss/evaluation/index.scss`
- `_sakai25-compat.scss` contains classes removed in the upstream style overhaul that are still needed for Sakai 25 deployment.

### Shared JavaScript
Evalsys helpers are split across `evalsys-core.js`, `evalsys-items.js`, and `evalsys-pages.js`. Templates include them via the `fragments/evalsys_scripts :: evalsys` fragment (load core, then items, then pages). Do not use `document.write` loaders or a single pseudo-bundle. Each template loads only the scripts it needs; pages that do not call `evalsys` should omit the fragment entirely.

## Database
- Hibernate ORM with mapping files in `sakai-evaluation-api/src/java/org/sakaiproject/evaluation/dao/hbm/`
- DDL scripts for multiple databases in `sakai-evaluation-impl/src/ddl/`
- Database conversion scripts available for version upgrades

### DAO persistence conventions
- GenericDAO has been removed from this project. Do not add `org.sakaiproject.genericdao` dependencies, `Search`/`Restriction`/`Order` query objects, GenericDAO batch helpers, or local GenericDAO-style query helpers.
- Port interfaces live in `sakai-evaluation-api/src/java/org/sakaiproject/evaluation/dao/`. There is no `EvaluationDao` facade.
- Each port has one Spring bean and one `*DaoImpl` class extending `EvaluationDaoHibernateSupport` directly (no inheritance chain between ports).
- Add new persistence behavior to the narrow domain port first (`EvaluationSettingsDao`, `EvaluationAuthoringDao`, `EvaluationAssignmentDao`, `EvaluationResponseDao`, `EvaluationQueryDao`, etc.), then implement it in the matching `*DaoImpl`.
- Services inject only the ports they use; wire them in Spring XML (`spring-hibernate.xml`, `logic-support.xml`, `components.xml`).
- Cross-port calls inside the DAO layer use explicit collaborator injection (for example `EvaluationAssignmentDaoImpl` delegates to `EvaluationResponseDao`), not inheritance.
- Keep query semantics readable at the call site: prefer domain method names like `getEvaluationsUsingEmailTemplate` or `deleteAssignmentsForEvaluation` over local generic query abstractions.
- Use `EvaluationGroupQuery` for `getEvaluationsByEvalGroups`; do not reintroduce nullable-boolean overloads or sentinel IDs.
- Keep HQL construction and parameter binding explicit in the owning `*DaoImpl`.
- `EvaluationDaoBase` / `EvaluationDaoBaseImpl` owns the small shared persistence surface: `findById`, `findAll`, `countAll`, `create`, `save`, `update`, and `delete`.
- `EvaluationDaoHibernateSupport` is the Hibernate helper superclass for all `*DaoImpl` beans.
- `SakaiComponentBeanNameAutoProxyCreator` is allowed as narrow Spring/Sakai classloader infrastructure for transactional proxies; do not expand it into DAO/query helper behavior.
- If a new transitive dependency disappears while removing persistence libraries, declare the directly used dependency explicitly rather than relying on unrelated libraries to provide it.

## Testing
Tests are located in `sakai-evaluation-impl/src/test/` and use:
- Spring Test framework for integration testing
- HSQLDB for in-memory testing
- Mock implementations in `test.mocks` package

### Test conventions
- Test public behavior through the public service/API that production code uses; do not use reflection to reach private methods.
- Prefer loading the real Spring wiring for this module instead of constructing a small mocked object graph by hand. The class or service under test should be the real Spring bean whenever practical.
- Do not mock, spy, or partially mock the class, service, controller, or tool component whose behavior the test is meant to verify.
- Mock boundaries, not internals. Mockito is appropriate for external systems, unavailable infrastructure, expensive integrations, or unavoidable Sakai boundary services; avoid mocking DAOs/services from this same module just to avoid wiring.
- When behavior depends on persistence, transactions, Hibernate mappings, permissions, or Sakai component wiring, prefer a Spring/Hibernate service test over a tiny isolated unit test.
- When changing user-visible UI flows, add or update a Playwright/e2e test where practical. If not practical, document why in the PR description.

## Review Heuristics
- Check links, entity providers, background jobs, imports, site copy, and user/site operations for explicit permissions and trustworthy user/site/session context.
- Preserve established evaluation semantics around availability, state transitions, assignments, hierarchy, response locking, report visibility, and email behavior unless the requested change explicitly alters them.
- Use parameterized logging with useful operation/object context. Do not include class or method names when the logger already supplies them. Use `debug` for routine trace, `warn` for recoverable unexpected state, and include the exception object when the stack trace matters.
- Do not swallow exceptions silently. Collapse duplicate catch blocks, remove unreachable catches, and use try-with-resources for streams/files/resources.
- Prefer small, concrete changes that fit existing Sakai/EVALSYS patterns. When an approach is unclear, ask which strategy is intended before adding more code.

## Key Configuration
- System properties managed through `EvalSettings` service
- Email templates configurable via admin UI
- Export job location: set `evaluation.exportjob.outputlocation` property
- Most configuration available through admin interface

## Dependencies
- Requires Sakai 11+ and Java 8+
- Uses Sakai Hierarchy service for institutional structure
- EntityBroker for REST API endpoints
- Spring MVC + Thymeleaf for the web UI
- Spring/Hibernate for core framework
- FreeMarker for email templating
