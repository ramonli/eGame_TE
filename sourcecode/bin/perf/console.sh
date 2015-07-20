#!/bin/bash

# set environment variables.
. setenv.sh

echo JAVA_HOME=$JAVA_HOME
echo JYTHON_HOME=$JYTHON_HOME

# display console user interface.
java -cp $CP -Duser.language="en" net.grinder.Console 