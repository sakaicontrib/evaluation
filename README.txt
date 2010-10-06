BUILDING EVALUATION

The evaluation project can be built and deployed with Sakai 2.5.x, 2.6.x, 2.7.x, or 2.8.x. 
This is enabled by use of maven profiles, which are described in maven documentation:

http://maven.apache.org/guides/introduction/introduction-to-profiles.html

To do a full build of evaluation for sakai 2.5.x, it is not necessary to specify a profile. 
The "full" profile provides default dependencies that are compatible with 2.5.x. The "full" 
profile is activated by default.

To do a full build for sakai 2.6.x, you can specify the "sakai2.6" profile on the command 
line.

To do a full build for sakai 2.7.x or 2.8.x, you can specify the "sakai2.7" profile on the 
command line.

To do an "api" or "tool" build, it is necessary to specify the "api" or "tool" profile *AND* 
a sakai-version profile ("sakai2.5", "sakai2.6" or "sakai2.7") on the command line. The "api" 
and "tool" builds would be missing dependencies if you do not specify a sakai-version profile.

To do a "ddl" build, you simply specify the "ddl" profile on the command line (since this does 
not depend on the sakai version).
   
For example, to do a full build of evaluation with Sakai 2.7, you might enter the following 
maven command on the command line: 

	mvn clean install -P sakai2.7

You can specify multiple profiles. These are used by various contrib projects in sakai to avoid 
having to have patches or attempt to maintain multiple branches of the same code if the only 
thing that it needs to take care of are projects that change groupId/artifactId. Occasionally 
API's change and a patch may be required for different versions as well, but that is not the 
case for evaluation yet. 

In addition to specifying the profile when building/deploying, you will need to change the 
hard-coded version of sakai in the parent element of evalsys's root pom. By default, it looks 
like this:

    <parent>
        <artifactId>master</artifactId>
        <groupId>org.sakaiproject</groupId>
        <version>2.5.4</version>
        <relativePath>../master/pom.xml</relativePath>
    </parent>

If you want to build with sakai 2.6.3 instead of sakai 2.5.4, you would change that as follows 
before building/deploying:

    <parent>
        <artifactId>master</artifactId>
        <groupId>org.sakaiproject</groupId>
        <version>2.6.3</version>
        <relativePath>../master/pom.xml</relativePath>
    </parent>
