#!/bin/sh
if [ -z "$JAVA_HOME" ]
then
  JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
fi
cd "$(dirname "$(readlink -f "$0")")"
JAR=$(ls -1 XMageLauncher*.jar | tail -n1)
"$JAVA_HOME/bin/java" -jar "$JAR"
