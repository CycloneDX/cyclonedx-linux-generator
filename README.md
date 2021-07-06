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

## License
[licenses](./LICENSE)
