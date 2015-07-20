#!/bin/sh

#JAVA_HOME=/opt/jdk1.6.0_17
# Set to the path where mlotter_te web application resides 
MLOTTERY_TE_HOME=/opt/apache-tomcat-6.0.29/webapps/mlottery_te

# -------------------------------------------------------
# DO NOT CHANGE BELOW PARTS
# -------------------------------------------------------

#  check environment.
if [ -r "$JAVA_HOME"/bin/java ]; then
  echo "Use JAVA_HOME: $JAVA_HOME"
else
  echo "can NOT find $JAVA_HOME/bin/java"
  exit 0
fi

PATH=$JAVA_HOME/bin:$PATH

#  check environment.
if [ -r "$MLOTTERY_TE_HOME"/WEB-INF/web.xml ]; then
  echo "Use MLOTTERY_TE_HOME: $MLOTTERY_TE_HOME"
else
  echo "can NOT find $MLOTTERY_TE_HOME/WEB-INF/web.xml"
  exit 0
fi

CP=$MLOTTERY_TE_HOME/WEB-INF/classes:$MLOTTERY_TE_HOME/WEB-INF/lib/commons-logging.jar

# run command to generate RSA and DES key for protecting ticket serialNo.
java -cp $CP com.mpos.lottery.te.common.encrypt.KeyStore
