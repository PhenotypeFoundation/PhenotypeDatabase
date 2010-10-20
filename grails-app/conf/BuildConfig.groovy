/**
 * Project build configuration
 * @Author  Jeroen Wesbeek
 * @Since   20091027
 * @Description
 *
 * Specific build configuration for the GSCF application
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

/**
 * add the nimble repository
 * @see http://sites.google.com/site/nimbledoc/nimble-installation
 */
//grails.plugin.repos.discovery.intient = "http://intient.com/downloads/grails/"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsPlugins()
        grailsHome()
//        mavenRepo "http://nmcdsp.org:8080/nexus-webapp-1.7.2/content/repositories/releases/"
        mavenRepo "http://nexus.nmcdsp.org/content/repositories/releases"
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
        //mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.5'
    }
}

//grails.plugin.location.'aaaa' = '/home/tjeerd/NetBeansProjects/nmcdsp/nmcdsp-grailsPlugins/aaaa'
//grails.plugin.location.'grom' = '/Users/jeroen/Workspace/grails/grom'
