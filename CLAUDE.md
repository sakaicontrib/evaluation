# CLAUDE.md

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
- **sakai-evaluation-tool**: Web UI layer (RSF-based tool interface)

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

## Database
- Hibernate ORM with mapping files in `sakai-evaluation-api/src/java/org/sakaiproject/evaluation/dao/hbm/`
- DDL scripts for multiple databases in `sakai-evaluation-impl/src/ddl/`
- Database conversion scripts available for version upgrades

## Testing
Tests are located in `sakai-evaluation-impl/src/test/` and use:
- Spring Test framework for integration testing
- HSQLDB for in-memory testing
- Mock implementations in `test.mocks` package

## Key Configuration
- System properties managed through `EvalSettings` service
- Email templates configurable via admin UI
- Export job location: set `evaluation.exportjob.outputlocation` property
- Most configuration available through admin interface

## Dependencies
- Requires Sakai 11+ and Java 8+
- Uses Sakai Hierarchy service for institutional structure
- EntityBroker for REST API endpoints
- Spring/Hibernate for core framework
- FreeMarker for email templating