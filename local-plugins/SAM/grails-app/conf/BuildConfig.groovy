grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
	    grailsCentral()
	    mavenCentral()
        grailsHome()
        
	    // other maven repo's
	    mavenRepo "http://nexus.dbnp.org/content/repositories/releases"
//	    mavenRepo "http://repository.springsource.com/maven/bundles/release"
//	    mavenRepo "http://repository.springsource.com/maven/bundles/external"
//	    mavenRepo "http://repository.springsource.com/maven/libraries/release"
//	    mavenRepo "http://repository.springsource.com/maven/libraries/external"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.13'
	// we seem to be needing XStream at compile time in some cases
	// compile("com.thoughtworks.xstream:xstream:1.3.1")
    }
    plugins {

        // Plugins for the build system only
        build ":tomcat:7.0.70"
        build ":release:2.2.1"
        //Temporary static version of Rest Client Builder due to compile error
        build ":rest-client-builder:1.0.3"

        // Plugins needed at runtime but not for compilation
        runtime ":hibernate4:4.3.10"
        runtime ":jquery:1.11.1"
        runtime ":resources:1.2.14"

        // Plugins needed for compilation
        compile ':jquery-ui:1.10.4'
        compile ":famfamfam:1.0.1"
        compile ":ajaxflow:0.2.4"
        compile ":jquery-datatables:1.7.5"

        if (System.getProperty("grails.env") == "development") {
            // development mode only Plugins
            compile(":console:1.5.4")
        }

        // Now included as local plugins
//        compile ":matrix-importer:0.2.5.0"
//        compile ":dbxp-module-base:0.6.2.0"
    }
}

grails.plugin.location.'gdt' = '../GDT'
grails.plugin.location.'dbxpBase' = '../dbxpBase'

//grails.plugin.location.'dbxpModuleBase' = '../dbxpModuleBase'
grails.plugin.location.'matrixImporter' = '../matrixImporter'

//grails.server.port.http = "8182"  // The modern way of setting the server port
