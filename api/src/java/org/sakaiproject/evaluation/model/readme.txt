These POJOs were generated using the Hibernate Tools available here:
http://tools.hibernate.org/

Steps to generate are as follows:
1) Make sure you have a hibernate properties file
(use the one that already exists in your project if there is one)
* It is best to get one from another project if possible, here is sample text:
# This properties file defines the connection to the HSQLDB database
hibernate.connection.driver_class=org.hsqldb.jdbcDriver

hibernate.connection.url=jdbc:hsqldb:.
hibernate.connection.username=sa
hibernate.connection.password=

hibernate.dialect=org.hibernate.dialect.HSQLDialect

#hibernate.show_sql=true
hibernate.show_sql=false
hibernate.hbm2ddl.auto=create
hibernate.cache.provider_class=org.hibernate.cache.EhCacheProvider

2) Make sure you have a Hibernate config file in the eclipse project somewhere
* Create one using File -> New -> Other -> Hibernate -> Hibernate Config File
(Use the one that already exists if you already have one)
3) Make sure you have a Hibernate console config in the eclipse project somewhere
* Create one using File -> New -> Other -> Hibernate -> Hibernate Console Config
** Select your created property file
** Select your created configuration file
** Add all the mapping files in the project to the list of mapping files
** Add in the HSQL driver jar (probably in your maven repository)
4) Run -> Hibernate Code Generation -> Hibernate Code Generation
5) Create a new generator (call it evaluation_generator or something like that)
6) Set the output directory to the root source directory of the model directory
* Example: model/src/java
7) Check the Domain code box under the exporters tab
8) Run the code generator to create the POJOs

*Instructions by Aaron Zeckoski (aaronz@vt.edu)*