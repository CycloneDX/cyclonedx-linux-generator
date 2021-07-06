#!/bin/bash

if [ -z "$JAVA_HOME" ]
then
	echo "\$JAVA_HOME is empty setting it now."
	export JAVA_HOME=./jdk-11
	export CLASSPATH=$JAVA_HOME
	export PATH=/bin:/usr/bin:$JAVA_HOME/bin
else
	echo "\$JAVA_HOME is already set."
fi

echo $JAVA_HOME

$JAVA_HOME/bin/java -Xms768m -Xmx8192m -Dlog4j.configuration=file:./logging/log4j.xml -jar ./target/UnixSbomGenerator-[0-9].[0-9].[0-9].jar
