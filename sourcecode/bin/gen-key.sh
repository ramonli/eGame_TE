#!/bin/sh

# you can modify JAVA_HOME to a absolute path, if the varialble doesn't exist
if [ -z "$JAVA_HOME" ]; then
  JAVA_HOME=/home/ramonli/tool/jdk1.5.0_19
fi
PATH=$JAVA_HOME/bin:$PATH

#  check environment.
if [ -r "$JAVA_HOME"/bin/keytool ]; then
  echo "Use JAVA_HOME: $JAVA_HOME"
else
  echo "can NOT find $JAVA_HOME/bin/keytool"
  exit 0
fi

if [ $# != 1 ]; then
  echo Usage:  certs  [IP address of publisher]
  echo Arguments:
  echo        [IP address of TE]: the IP address of publisher.
  exit 0
fi

publisherIP=$1

echo - STEP1: generate keystore
keytool -genkey -dname "cn=$publisherIP" -alias $publisherIP -validity 3650 -keyalg RSA -keysize 2048 -keypass 111111 -keystore "$publisherIP".jks -storepass 111111

echo - STEP2: export public certificate
keytool -export -alias $publisherIP -file "$publisherIP"_public.crt -keystore "$publisherIP".jks -storepass 111111

