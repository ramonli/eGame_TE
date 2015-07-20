TABLE OF CONTENT
----------------------------------------------------------------   
    1. Description
    2. Installation Guide
    3. Directory Structure
    
1. Description
----------------------------------------------------------------  
The TE subsystem of M.Lottery system.

2. Installation Guide
----------------------------------------------------------------
2.1 Prerequisite
   - Ant 1.7.X. Download ant1.7.X from http://ant.apache.org, and extract it to a directory.
   - JDK 1.6.X or above. Download jdk from http://java.sun.com.
   - Tomcat6.X. Download tomcat from http://tomcat.apache.org.

2.2 ANT Build Project(NO Maintainance any more)
1) set build environment.
   - open the build script(bin/build.bat or bin/build.sh) in a text editor.
   - modify the 'ANT_HOME' and 'JAVA_HOME' to your settings.
2) Open bin/build.properties, modify the building setting.
   - If you want to deploy TE to tomcat, you can modify entry 'tomcat.home'.
   - Or you don't need to do any change.
3) Run build script(build.bat for window, build.sh for linux/unix) to build.
   - you can run 'build.bat -p' to get all available target.
   - run 'build.bat auto-deploy' will deploy TE to tomcat automatically.
   - run 'build.bat auto-dist' will generate a war file in build/dist.
   - run 'build.bat auto-bin' will generate a zip file for distribution, also you can find 
     it in build/dist.
   For these target, you can specify '-Dsubversion=true' to retrieve version number from 
   subversion. Also you can specify '-Dbootstrap=true' which will direct the  building process
   to rebuild the database structure and reload all master data. 

2.3 Build by Gradle
We use Gradle v1.12, the newer version 2.X may can't work

1) Package the war
>> gradle -x test 
* use '-x test' to filter out 'test' task.
2) build with test
>> gradle
* Also you can specify '-DconnectSubversion=false' to disable subversion connection, by default the build 
will retrieve revision number from remote subversion repository.
3) Startup TE
>> gradle teRun -- this command will run TE in a embedded tomcat.
4) Startup 3rd party system for manually debug
>> gradle thirdpartyRun -DjettyDaemon=false
This command will launch a third party service in foreground for integration test, such as player account system.
5) build a package for release
In general, if wanna build a package for release, you must first run a default build which will build classes, 
run integration tests etc, and then run 'dist' task to build the package(this task will create svn tag as well)
>> gradle
then
>> gradle dist

Furthermore you can import the whole project into eclipse.

3. Directory Structure
----------------------------------------------------------------
3.1 Introduction to the Standard Directory Layout
Having a common directory layout would allow for users familiar with one Maven project to 
immediately feel at home in another Maven project. The advantages are analogous to adopting 
a site-wide look-and-feel.

The next section documents the directory layout expected by Maven and the directory layout 
created by Maven. Please try to conform to this structure as much as possible; however, if 
you can't these settings can be overridden via the project descriptor.

src/main/java 	      Application/Library sources
src/main/resources 	  Application/Library resources
src/main/filters 	  Resource filter files
src/main/assembly 	  Assembly descriptors
src/main/config 	  Configuration files
src/main/webapp 	  Web application sources
src/test/java 	      Test sources
src/test/resources 	  Test resources
src/test/filters 	  Test resource filter files
src/site 	          Site
bin/                  All build/test scripts.
LICENSE.txt 	      Project's license
README.txt 	          Project's readme

At the top level files descriptive of the project: a pom.xml file (and any properties, maven.xml 
or build.xml if using Ant). In addition, there are textual documents meant for the user to be 
able to read immediately on receiving the source: README.txt , LICENSE.txt , etc.

There are just two subdirectories of this structure: src and target . The only other directories 
that would be expected here are metadata like CVS or .svn , and any subprojects in a multiproject 
build (each of which would be laid out as above).

The target directory is used to house all output of the build.

The src directory contains all of the source material for building the project, its site and so 
on. It contains a subdirectory for each type: main for the main build artifact, test for the 
unit test code and resources, site and so on.

Within artifact producing source directories (ie. main and test ), there is one directory for 
the language java (under which the normal package hierarchy exists), and one for resources 
(the structure which is copied to the target classpath given the default resource definition).

If there are other contributing sources to the artifact build, they would be under other 
subdirectories: for example src/main/antlr would contain Antlr grammar definition files.

TBD


----------------------------
Gradle Reference

run the thirdparty
gradle thirdpartyRun -DjettyDaemon=false

----------------------------
