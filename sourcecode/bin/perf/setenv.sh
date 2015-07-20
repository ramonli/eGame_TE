#! /bin/bash
##author: ramonli

export JAVA_HOME=/opt/tools/jdk1.5.0_14
export JYTHON_HOME=/opt/tools/jython2.2.1
export PROJECT_HOME=/opt/projects/M.Lottery/sourcecode

export WEBLIB_DIR=$PROJECT_HOME/src/main/www/WEB-INF/lib
export PATH=$JAVA_HOME/bin:$PATH

# check JAVA_HOME
if [ ! -d $JAVA_HOME ]
then
        echo "**You must specify JAVA_HOME={java.home}"
        exit 0
fi

# check if the jython.home has been set
if [ ! -f $JYTHON_HOME/jython.jar ]
then
        echo "**JYTHON_HOME={jython.home} doesn't exist!"
        exit 0
fi

# check if the project.home has been set
if [ ! -d $PROJECT_HOME ]
then
        echo "**The directory of PROJECT_HOME($PROJECT_HOME) doesn't exist."
        exit 0
fi

# add grinder to classpath
CP=.:$PROJECT_HOME/lib/dev/grinder.jar:$PROJECT_HOME/lib/dev/grinder-j2se5.jar
CP=$CP:$PROJECT_HOME/lib/dev/picocontainer-1.3.jar:$JYTHON_HOME/jython.jar
CP=$CP:$WEBLIB_DIR/commons-logging-1.1.jar
CP=$CP:$PROJECT_HOME/build/classes

