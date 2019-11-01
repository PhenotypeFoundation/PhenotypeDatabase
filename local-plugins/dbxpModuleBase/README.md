dbxpModuleBase
====

A Grails plugin that can serve as the basis for a dbXP module.
dbxpModuleBase provides basic module services such as synchronisation of a proxy Study-Sample-Assay cache with GSCF.

## How to publish changes
To create a new version of dbxpModuleBase and publish it on nexus.nmcdsp.org:

### Make sure that ~/.grails/settings.groovy contains username and password for Nexus:
```
~/.grails $ cat settings.groovy
grails.project.dependency.distribution = {
 remoteRepository(id:"pluginReleases", url:"http://nexus.nmcdsp.org/content/repositories/releases/") {
     authentication username: "admin", password: "** ask Jeroen or Kees **"
 }
}
```

### Make sure maven-publisher plugin is installed
```
grails install-plugin maven-publisher
```

### Deploy the upgrade
```
grails maven-deploy --repository=pluginReleases
```