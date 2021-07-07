[![Build Status](https://github.com/CycloneDX/cyclonedx-linux-generator/workflows/Maven%20CI/badge.svg)](https://github.com/CycloneDX/cyclonedx-linux-generator/actions?workflow=Maven+CI)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.cyclonedx.contrib.com.lmco.efoss.unix.sbom/cyclonedx-linux-generator/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.cyclonedx.contrib.com.lmco.efoss.unix.sbom/cyclonedx-linux-generator)
[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)][License]
[![Website](https://img.shields.io/badge/https://-cyclonedx.org-blue.svg)](https://cyclonedx.org/)
[![Slack Invite](https://img.shields.io/badge/Slack-Join-blue?logo=slack&labelColor=393939)](https://cyclonedx.org/slack/invite)
[![Group Discussion](https://img.shields.io/badge/discussion-groups.io-blue.svg)](https://groups.io/g/CycloneDX)
[![Twitter](https://img.shields.io/twitter/url/http/shields.io.svg?style=social&label=Follow)](https://twitter.com/CycloneDX_Spec)


# cyclonedx-linux-generator
Lockheed Martin developed utility to generate CycloneDX SBOMs for Linux distributions

This project creates a utility that users can utilize in generating Software Bill of Materials (SBom) file for Unix Operating Systems. It currently will create an SBOM for Alpine, Debian, Centos, Redhat and Ubuntu. 

This utility can also work well with docker containers who runs Alpine(*), Debian, Centos, Redhat or Ubuntu.

Note:  For Alpine you must have bash and java installed to run.

## Prerequisites
- Open JDK11
- Apache Maven 3.6.3 or greater installed 
- (Recommended) java IDE Eclipse with Subclipse 4.3.0 plug-in
- Unix Based Operating System.

## Usage:

### To Build this project into an artifact via maven.
### Maven Command
<pre>
    mvn clean package
</pre>

### To Run
Run Start script provided (start.sh).


### Logging
### Logs
    "start.sh" script will create a directory for the logs (logs).
    
### Output
### bom.xml
    "start.sh" will create a directory (output) for the bom.xml file.  

Copyright & License
-------------------

CycloneDX Linux Generator is Copyright (c) Lockheed Martin Corporation. All Rights Reserved.

Permission to modify and redistribute is granted under the terms of the Apache 2.0 license. See the [License] file for the full license.

[License]: https://github.com/CycloneDX/cyclonedx-linux-generator/blob/master/LICENSE
