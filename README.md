Generic Study Capture Framework
====

## Download a war file
We have a couple of ```war``` files available that can be deployed on an application container (e.g. Apache Tomcat).  

What | War file | Grails environment | Build Status 
--- | --- | --- | --- | --- 
Latest Production Build | [gscf.war](http://download.dbnp.org/production/gscf.war) | ```production``` | ![configuration file: ~/.gscf/production.properties](http://jenkins.dbnp.org/job/production-gscf/badge/icon)
 | [metabolomicsModule](http://download.dbnp.org/production/metabolomicsModule.war) | ```production``` | ![configuration file: ~/.metabolomicsModule/production.properties](http://jenkins.dbnp.org/job/production-metabolomicsModule/badge/icon)
 | [SAM](http://download.dbnp.org/production/sam.war) | ```production``` | ![configuration file: ~/.dbxp/production-sam.properties](http://jenkins.dbnp.org/job/production-sam/badge/icon)
 | ~~[questionnaireModule.war](http://download.dbnp.org/production/questionnaireModule.war)~~ | ~~```production```~~ | ![configuration file: ~/.dbxp/production-questionnaireModule.properties](http://jenkins.dbnp.org/job/production-questionnaireModule/badge/icon)
Latest Test Build | [gscf.war](http://download.dbnp.org/dbnptest/gscf.war) | ```dbnptest``` | ![configuration file: ~/.gscf/dbnptest.properties](http://old.jenkins.dbnp.org/jenkins/job/test-gscf/badge/icon)
Continuous Integration Build ([HEAD](https://github.com/PhenotypeFoundation/GSCF)) | [gscf.war](http://download.dbnp.org/ci/gscf.war) | ```ci``` | ![configuration file: ~/.gscf/ci.properties](http://old.jenkins.dbnp.org/jenkins/job/ci-gscf/badge/icon) 
 | [metabolomicsModule.war](http://download.dbnp.org//ci/metabolomicsModule.war) | ```ci``` | ![configuration file: ~/.metabolomicsModule/ci.properties](http://old.jenkins.dbnp.org/jenkins/job/ci-metabolomicsModule/badge/icon) |
 | [sam.war](http://download.dbnp.org//ci/sam.war) | ```ci``` | ![configuration file: ~/.dbxp/production-sam.properties](http://old.jenkins.dbnp.org/jenkins/job/ci-sam/badge/icon) 
Continuous Integration Build ([Events Refactoring Branch](https://github.com/PhenotypeFoundation/GSCF/tree/events_refactoring)) | [gscf.war](http://download.dbnp.org/ci2/gscf.war) | ```ci2``` | ![configuration file: ~/.gscf/ci2.properties](http://old.jenkins.dbnp.org/jenkins/job/ci2-gscf/badge/icon) 

_Note: each project / environment requires a specific configuration file. You can see the required path by hovering over the build status icons_

## Configuration file
Example configuration file (e.g. ~/.gscf/ci.properties where ~ is de homedir of the user running Tomcat - generally user tomcat or tomcat7):

```groovy
# DATABASE
dataSource.driverClassName=org.postgresql.Driver
dataSource.dialect=org.hibernate.dialect.PostgreSQLDialect
dataSource.url=jdbc:postgresql://localhost:5432/gscf-ci
dataSource.dbCreate=update
dataSource.username=yourUsername
dataSource.password=yourPassword
#dataSource.logSql=true

// override application title
application.title=GSCF CI
```

_Note the PostgreSQL database configuration_


## Continuous Integration Build status
Build status of related modules / projects

### SAM

![build status](http://old.jenkins.dbnp.org/jenkins/job/ci-sam/badge/icon)

### metabolomicsModule

![build status](http://old.jenkins.dbnp.org/jenkins/job/ci-metabolomicsModule/badge/icon)

### grails-uploadr plugin

![build status](http://jenkins.osx.eu/job/ci-uploadr/badge/icon)

