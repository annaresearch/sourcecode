### AnnaStudy
We conduct the first comprehensive study of annotation-induced faults on static analyzers. In this work, we collect 238 issues from five popular and analyze their root causes, symptoms, and fix strategies. We also implement a metamorphic testing framework AnnaTester to detect usual annotation-induced faults based on our findings. AnnaTester identified 33 faults, 15 of which have been fixed.

This is the source code repo of AnnaTester. The source code files are located in the folder src, tool folder contains some auxiliary programs, seed folder contains the downloaded seed files. 

### Usage guideline

#### Project Description

1. Src folder includes source code and test cases for our project.
2. Seeds folder includes the initial input programs for our project.
3. Tools folder incldues some tools used in our development process.
4. The pom.xml defines the third-party dependency libraries used in our project.

#### Install MVN Dependency

We use Maven 3.8.5 to build our project. Users should first install the dependency libraries using the following command:

> mvn -f pom.xml dependency:copy-dependencies

#### Install Static Analyzer

Download different static analyzers by the following links:

PMD: https://github.com/pmd/pmd/releases/tag/pmd_releases/6.55.0

SpotBugs: https://github.com/spotbugs/spotbugs/releases

CheckStyle: https://github.com/checkstyle/checkstyle/releases/

Infer: https://github.com/facebook/infer/releases/tag/v1.1.0

SonarQube: https://www.sonarqube.org/downloads/

#### Config property & Execution

Create a config file called "config.properties" in the root folder of AnnaTester. We have provided a template file in this folder.

We have provided a shell script for running the project, you can directly use this file `./run.sh` to run AnnaTester after configuration.