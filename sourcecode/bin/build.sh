#!/bin/sh
# ----------------------------------------------------------------
# Start Script for the ANT BUILD
# ----------------------------------------------------------------

# modify below to your setting
ANT_HOME=/home/ramonli/tool/apache-ant-1.7.1
JAVA_HOME=/home/ramonli/tool/jdk1.6.0_15

# DO NOT modify below statements
if [ -d "$ANT_HOME" ]; then
    ANT_HOME="$ANT_HOME"
    echo "Use NT_HOME=$ANT_HOME"
else
    echo "You MUST set ANT_HOME."
    exit 1
fi

if [ -d "$JAVA_HOME" ]; then
    JAVA_HOME="$JAVA_HOME"
    echo "Use JAVA_HOME=$JAVA_HOME"
    PATH=$JAVA_HOME/bin:$PATH
else
    echo "You MUST set JAVA_HOME."
    exit 1
fi

echo ************************************
# call ant script
$ANT_HOME/bin/ant $@


