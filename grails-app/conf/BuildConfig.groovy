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
        grailsCentral()

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
		mavenCentral()
		mavenRepo 'http://repository.jboss.org/maven2/'
		mavenRepo "http://nexus.nmcdsp.org/content/repositories/releases"
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // runtime 'mysql:mysql-connector-java:5.1.5'
		build 'org.codehaus.gpars:gpars:0.11'
    }
}

//grails.plugin.location.'grom' = '../grom'
//grails.plugin.location.'ajaxflow' = '../ajaxflow'
//grails.plugin.location.'gdt' = '../gdt'
//grails.plugin.location.'jumpbar' = '../jumpbar'