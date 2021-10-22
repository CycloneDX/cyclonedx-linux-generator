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

JAR=$(ls ./target/linux-sbom-generator-[0-9].[0-9].[0-9]-jar-with-dependencies.jar 2> /dev/null)
if [ "$JAR" == "" ]
then
	JAR=$(ls ./target/linux-sbom-generator-[0-9].[0-9].[0-9]-SNAPSHOT-jar-with-dependencies.jar 2> /dev/null)

	if [ "$JAR" == "" ]
	then
		echo "Could not find linux-sbom-generator JAR - did you forget to build it using 'mvn package'?" > /dev/stderr
		exit 1
	fi
fi

echo "Found $JAR"
$JAVA_HOME/bin/java -Xms768m -Xmx8192m -Dlog4j.configuration=file:./logging/log4j.xml -jar "$JAR" "$@"
exit $?
