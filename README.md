Sakai Evaluation System (EVALSYS)

This version of Evaluation is only compatible with Sakai 11 and Java 8

Please see [branches in subversion](https://source.sakaiproject.org/contrib/evaluation/branches/) for older versions 

To see a demo of this please go to [nightly experimental](https://experimental.nightly.sakaiproject.org/)
* Login as admin
* And add the Evaluation tool to any site (You can use Mercury and just click Site Info-> Manage Tools -> Evaluation System)

Please submit any pull requests directly here. Issues submitted here would probably be noticed faster than issues submitted against the existing project in [jira](https://jira.sakaiproject.org/browse/EVALSYS)

In Sakai 11 no external dependencies should be needed since hierarchy is included, just drop it in the source and compile.

`mvn clean install`

Or to deploy to your sakai instance at ${SAKAI_DIRECTORY}

`mvn clean install sakai:deploy -Dmaven.tomcat.home=${SAKAI_DIRECTORY}`

Most of the properties are defined in the Administrate UI except for a few used by quartz jobs, including a new export feature [EVALSYS-1453](https://jira.sakaiproject.org/browse/EVALSYS-1453)

To configure that feature you have to set up the property

`evaluation.exportjob.outputlocation={location on disk that's writable by the process Sakai is running as}`

*Important note* If you are upgrading for earlier versions and not running auto.ddl you need to run the [conversion scripts for your database](https://github.com/sakaicontrib/evaluation/tree/e4a1a448b812fbdff3ee823cce9e8fccb560259c/sakai-evaluation-impl/src/ddl/conversion).
