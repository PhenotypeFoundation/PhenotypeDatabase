Generic Study Capture Framework
====

## Download a war file
We have a couple of ```war``` files available that can be deployed on an application container (e.g. Apache Tomcat).  

What | War file | Grails environment | Configuration file
--- | --- | --- | --- | ---
Latest Production Build | [gscf.war](http://download.dbnp.org/production/gscf.war) | ```production``` | _~/.gscf/production.properties_
Latest Test Build | [gscf.war](http://download.dbnp.org/dbnptest/gscf.war) | ```dbnptest``` | _~/.gscf/dbnptest.properties_
Continuous Integration ([HEAD](https://github.com/PhenotypeFoundation/GSCF)) | [gscf.war](http://download.dbnp.org/ci/gscf.war) | ```ci``` | _~/.gscf/ci.properties_
Continuous Integration ([Events Refactoring Branch](https://github.com/PhenotypeFoundation/GSCF/tree/events_refactoring)) | [gscf.war](http://download.dbnp.org/ci2/gscf.war) | ```ci2``` | _~/.gscf/ci2.properties_

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
### GSCF

![build status](http://old.jenkins.dbnp.org/jenkins/job/ci-gscf/badge/icon)

### SAM

![build status](http://old.jenkins.dbnp.org/jenkins/job/ci-sam/badge/icon)

### metabolomicsModule

![build status](http://old.jenkins.dbnp.org/jenkins/job/ci-metabolomicsModule/badge/icon)

### grails-uploadr plugin

![build status](http://jenkins.osx.eu/job/ci-uploadr/badge/icon)

### GSCF - events_refactoring branch

![build status](http://old.jenkins.dbnp.org/jenkins/job/ci2-gscf/badge/icon)

