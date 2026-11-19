# Sakai Evaluation System (EVALSYS)

## Supported branches

| Branch | Sakai target | UI | Maintenance |
| --- | --- | --- | --- |
| `master` | Sakai 27 | Spring MVC + Thymeleaf | Active development |
| `25.x` | Sakai 25 | Spring MVC + Thymeleaf | Applicable fixes backported from master |
| `25.x-rsf` | Sakai 25 | RSF | Archived; reference only |

Use Java 21 and Tomcat 10.1 for master. Use Java 17 or later and Tomcat 9 for `25.x`.
Develop fixes on master, then backport applicable fixes to `25.x`. Adapt Jakarta,
Spring 6, and Hibernate 6 changes to Sakai 25's APIs when needed.

## Update an existing Sakai 25 checkout

On November 19, 2026, `25.x` was replaced with the current Thymeleaf code from
master plus Sakai 25 compatibility changes. The previous RSF history is preserved
in `25.x-rsf`. The `25.x-thymeleaf` branch has been removed; use `25.x` instead.

If your checkout tracks either previous branch, commit or stash uncommitted work
first. The following commands assume the upstream repository is named `upstream`:

```bash
git fetch upstream --prune
git branch backup/sakai-25-before-refresh
git switch -C 25.x upstream/25.x
git branch --set-upstream-to=upstream/25.x 25.x
```

The backup branch preserves your previous HEAD. Review any local commits before
cherry-picking them onto the replacement branch. If you need the old RSF version,
check out `upstream/25.x-rsf`.

## Build and deploy

To see a demo of this please go to [nightly experimental](https://experimental.nightly.sakaiproject.org/)
* Login as admin
* And add the Evaluation tool to any site (You can use Mercury and just click Site Info-> Manage Tools -> Evaluation System)

Please submit any pull requests directly here. Issues submitted here would probably be noticed faster than issues submitted against the existing project in [jira](https://jira.sakaiproject.org/browse/EVALSYS)

Build against the Sakai version listed for your branch:

`mvn clean install`

Or to deploy to your sakai instance at ${SAKAI_DIRECTORY}

`mvn clean install sakai:deploy -Dmaven.tomcat.home=${SAKAI_DIRECTORY}`

Most of the properties are defined in the Administrate UI except for a few used by quartz jobs, including a new export feature [EVALSYS-1453](https://jira.sakaiproject.org/browse/EVALSYS-1453)

To configure that feature you have to set up the property

`evaluation.exportjob.outputlocation={location on disk that's writable by the process Sakai is running as}`

*Important note* If you are upgrading for earlier versions and not running auto.ddl you need to run the [conversion scripts for your database](https://github.com/sakaicontrib/evaluation/tree/e4a1a448b812fbdff3ee823cce9e8fccb560259c/sakai-evaluation-impl/src/ddl/conversion).
