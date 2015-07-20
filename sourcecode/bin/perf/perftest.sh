#! /bin/bash

# set environment variables.
. setenv.sh

# execute grinder agent
echo JAVA_HOME=$JAVA_HOME
echo JYTHON_HOME=$JYTHON_HOME
echo CP=$CP

$JAVA_HOME/bin/java -cp $CP -Dpython.home=$JYTHON_HOME net.grinder.Grinder grinder.properties
