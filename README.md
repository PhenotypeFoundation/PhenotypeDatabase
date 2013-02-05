Generic Study Capture Framework
====

## Download a war file
We have a couple of ```war``` files available that can be deployed on an application container (e.g. Apache Tomcat).  

What | War file | Grails environment | Configuration file | Build Status | remark
--- | --- | --- | --- | --- | ---
Latest Production Build | [gscf.war](http://download.dbnp.org/production/gscf.war) | ```production``` | _~/.gscf/production.properties_ | ![build status](http://jenkins.dbnp.org/job/production-gscf/badge/icon) | n/a
- | [metabolomicsModule](http://download.dbnp.org/production/metabolomicsModule.war) | ```production``` | _~/.metabolomicsModule/production.properties_ | ![build_status](http://jenkins.dbnp.org/job/production-metabolomicsModule/badge/icon) | n/a
- | [SAM](http://download.dbnp.org/production/sam.war) | ```production``` | _~/.dbxp/production-sam.properties_ | ![build_status](http://jenkins.dbnp.org/job/production-sam/badge/icon) | n/a
- | [questionnaireModule.war](http://download.dbnp.org/production/questionnaireModule.war) | ```production``` | _~/.dbxp/production-questionnaireModule.properties_ | ![build status](http://jenkins.dbnp.org/job/production-questionnaireModule/badge/icon) | *don't use: codebase is wrong*
Latest Test Build | [gscf.war](http://download.dbnp.org/dbnptest/gscf.war) | ```dbnptest``` | _~/.gscf/dbnptest.properties_ | ![build status](http://old.jenkins.dbnp.org/jenkins/job/test-gscf/badge/icon) | n/a
Continuous Integration Build ([HEAD](https://github.com/PhenotypeFoundation/GSCF)) | [gscf.war](http://download.dbnp.org/ci/gscf.war) | ```ci``` | _~/.gscf/ci.properties_ | ![build status](http://old.jenkins.dbnp.org/jenkins/job/ci-gscf/badge/icon) | n/a
- | [metabolomicsModule.war](http://download.dbnp.org//ci/metabolomicsModule.war) | ```ci``` | _~/.metabolomicsModule/ci.properties_ | ![build_status](http://old.jenkins.dbnp.org/jenkins/job/ci-metabolomicsModule/badge/icon) |
- | [sam.war](http://download.dbnp.org//ci/sam.war) | ```ci``` | _~/.dbxp/production-sam.properties_ | ![build_status](http://old.jenkins.dbnp.org/jenkins/job/ci-sam/badge/icon) | n/a
Continuous Integration Build ([Events Refactoring Branch](https://github.com/PhenotypeFoundation/GSCF/tree/events_refactoring)) | [gscf.war](http://download.dbnp.org/ci2/gscf.war) | ```ci2``` | _~/.gscf/ci2.properties_ | ![build status](http://old.jenkins.dbnp.org/jenkins/job/ci2-gscf/badge/icon) | n/a

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

