#!/bin/bash

java -Xms768m -Xmx8192m -Dlog4j.configuration=file:./logging/log4j.xml -jar ./target/linux-sbom-generator-[0-9].[0-9].[0-9]*.jar "$@"
