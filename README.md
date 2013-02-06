Generic Study Capture Framework
====

## Download a war file
We have a couple of ```war``` files available that can be deployed on an application container (e.g. Apache Tomcat).  

War file | Build Environment | Build Status | Source | Config Location
--- | --- | --- | --- | --- | ---
[gscf.war](http://download.dbnp.org/production/gscf.war) | ```production``` | ![configuration file: ~/.gscf/production.properties](http://jenkins.dbnp.org/job/production-gscf/badge/icon) | [![github logo](https://raw.github.com/PhenotypeFoundation/GSCF/master/web-app/images/github-logo.png)](https://github.com/PhenotypeFoundation/GSCF) | ~/.gscf/production.properties [?](https://github.com/PhenotypeFoundation/GSCF/blob/master/grails-app/conf/default.properties)
[metabolomicsModule](http://download.dbnp.org/production/metabolomicsModule.war) | ```production``` | ![configuration file: ~/.metabolomicsModule/production.properties](http://jenkins.dbnp.org/job/production-metabolomicsModule/badge/icon) | [![github logo](https://raw.github.com/PhenotypeFoundation/GSCF/master/web-app/images/github-logo.png)](https://github.com/PhenotypeFoundation/metabolomicsModule) | ~/.metabolomicsModule/production.properties [?](https://github.com/PhenotypeFoundation/metabolomicsModule/blob/master/grails-app/conf/default.properties)
[SAM](http://download.dbnp.org/production/sam.war) | ```production``` | ![configuration file: ~/.dbxp/production-sam.properties](http://jenkins.dbnp.org/job/production-sam/badge/icon) | [![github logo](https://raw.github.com/PhenotypeFoundation/GSCF/master/web-app/images/github-logo.png)](https://github.com/TheHyve/SAM) | ~/.dbxp/production-sam.properties [?](https://github.com/thehyve/SAM/blob/master/grails-app/conf/default.properties)
~~[questionnaireModule.war](http://download.dbnp.org/production/questionnaireModule.war)~~ | ~~```production```~~ | ![configuration file: ~/.dbxp/production-questionnaireModule.properties](http://jenkins.dbnp.org/job/production-questionnaireModule/badge/icon) | [![github logo](https://raw.github.com/PhenotypeFoundation/GSCF/master/web-app/images/github-logo.png)](https://github.com/TNO/QuestionnaireModule) | ~/.dbxp/production-questionnaireModule.properties [?](https://github.com/TNO/QuestionnaireModule/blob/master/grails-app/conf/default.properties)
[gscf.war](http://download.dbnp.org/dbnptest/gscf.war) | ```dbnptest``` | ![configuration file: ~/.gscf/dbnptest.properties](http://old.jenkins.dbnp.org/jenkins/job/test-gscf/badge/icon) | [![github logo](https://raw.github.com/PhenotypeFoundation/GSCF/master/web-app/images/github-logo.png)](https://github.com/PhenotypeFoundation/GSCF) | ~/.gscf/dbnptest.properties [?](https://github.com/PhenotypeFoundation/GSCF/blob/master/grails-app/conf/default.properties)
[gscf.war](http://download.dbnp.org/ci/gscf.war) | ```ci``` | ![configuration file: ~/.gscf/ci.properties](http://old.jenkins.dbnp.org/jenkins/job/ci-gscf/badge/icon) | [![github logo](https://raw.github.com/PhenotypeFoundation/GSCF/master/web-app/images/github-logo.png)](https://github.com/PhenotypeFoundation/GSCF) | ~/.gscf/ci.properties [?](https://github.com/PhenotypeFoundation/GSCF/blob/master/grails-app/conf/default.properties)
[metabolomicsModule.war](http://download.dbnp.org//ci/metabolomicsModule.war) | ```ci``` | ![configuration file: ~/.metabolomicsModule/ci.properties](http://old.jenkins.dbnp.org/jenkins/job/ci-metabolomicsModule/badge/icon) | [![github logo](https://raw.github.com/PhenotypeFoundation/GSCF/master/web-app/images/github-logo.png)](https://github.com/PhenotypeFoundation/metabolomicsModule) | ~/.metabolomicsModule/ci.properties [?](https://github.com/PhenotypeFoundation/metabolomicsModule/blob/master/grails-app/conf/default.properties)
[sam.war](http://download.dbnp.org//ci/sam.war) | ```ci``` | ![configuration file: ~/.dbxp/production-sam.properties](http://old.jenkins.dbnp.org/jenkins/job/ci-sam/badge/icon) | [![github logo](https://raw.github.com/PhenotypeFoundation/GSCF/master/web-app/images/github-logo.png)](https://github.com/TheHyve/SAM) | ~/.dbxp/production-sam.properties [?](https://github.com/thehyve/SAM/blob/master/grails-app/conf/default.properties)
[gscf.war](http://download.dbnp.org/ci2/gscf.war) | ```ci2``` | ![configuration file: ~/.gscf/ci2.properties](http://old.jenkins.dbnp.org/jenkins/job/ci2-gscf/badge/icon) | [![github logo](https://raw.github.com/PhenotypeFoundation/GSCF/master/web-app/images/github-logo.png)](https://github.com/PhenotypeFoundation/GSCF/tree/events_refactoring) | ~/.gscf/ci2.properties [?](https://github.com/PhenotypeFoundation/GSCF/blob/events_refactoring/grails-app/conf/default.properties)
uploadr plugin | ```ci``` | ![build status](http://jenkins.osx.eu/job/ci-uploadr/badge/icon) | [![github logo](https://raw.github.com/PhenotypeFoundation/GSCF/master/web-app/images/github-logo.png)](https://github.com/4np/grails-uploadr/) | n/a ([documentation](https://github.com/4np/grails-uploadr/blob/master/README.md))

_Note: each project / environment requires a specific configuration file._

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

### grails-uploadr plugin

![build status](http://jenkins.osx.eu/job/ci-uploadr/badge/icon)

