# BPMN20OntologyTester

This project represents a Tool to test an previous created BPMN2.0 Ontology with workflow patterns and shows test results. 
Furthermore it includes the possibility to convert an BPMN-File (.bpmn) to an Ontology(.OWL) with the information of the BPMN2.0 Ontology.

## Requirements
- Java JRE 1.8 or higher (https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
- For Developers: 
    - JDK 1.8 or higher ( https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
    -Eclipse with JavaFX and Maven Support (Tested with Eclipse Jee Oxygen)
       - JavaFX-Plugin: https://www.eclipse.org/efxclipse/install.html

## Project Structure
The project structure represents a MAVEN project which can be integrated immediatley in an IDE wich maven support.
The main direcotries contain:
 - bin: Contains the application in executable form to run it immediatly as user
 - resource: Contains all required resources of the application (BPMN2.0-Ontology, pictures, ...)
 - src: Contains the source code of the project

## Start Application as User
 - Download this Respository to your local workspace (https://github.com/hoedlale16/BPMN20OntologyTester/archive/master.zip)
 - Unzip project 
 - Run application(.JAR) in direcotry **bin** with the highes version number
     - If the operating system does not recognize how to start try to start via command line using **java -jar /path/to/BPMN20OntologyTester/bin/BPMN20OntologyTester-<the.highest.version>.jar**
     - If command java not recognized, no JRE installed - see Requirements
     - If application not startable check your installed java version (**java -version**)

## Setup Project as Developer (Eclipse)
 - Clone this Respority to your local Eclispe workspace
 - Open Eclipse: File -> Import -> Select **Maven -> Exisiting Maven Projects** -> Select local repository -> Finish
 - After import, maven might need some time to download all required third party libraries

### Run Project in Eclipse
 - Open Navigator and Select Project
 - Right click on Project -> Run As -> Java Application -> Select BPMN20OntologyTester (at.fh.BPMN20OntologyTester) -> Confirm
 
### Create new executable Version
  - Do you changes in the source and geht a compileable version
  - To create an executable Version compile the project with following steps:
      - Open a command line and switch to the root directory of the project (.../BPMN20OntologyTester)
      - Enter command: **mvn clean package**
  - A new File created in  target/BPMN20OntologyTester-1.0.0-jar-with-dependencies.jar
  - Try to start the application with: **java -jar  target/BPMN20OntologyTester-1.0.0-jar-with-dependencies.jar**
  - If the application works, copy the newcly created application to the bin directory of the repository and check in
      - cp  target/BPMN20OntologyTester-1.0.0-jar-with-dependencies.jar bin/BPMN20OntologyTester-<Enter.Your.Version>.jar
      - git add bin/BPMN20OntologyTester-<Enter.Your.Version>.jar
      - git commit -m "New Version created" bin/BPMN20OntologyTester-<Enter.Your.Version>.jar
      - git push origin master 
