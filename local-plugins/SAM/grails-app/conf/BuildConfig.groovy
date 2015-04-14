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
	    grailsRepo "http://grails.org/plugins"
	    mavenCentral()

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

        build(  ":tomcat:$grailsVersion",
                ":rest-client-builder:1.0.3"
        ) {
            // plugin only plugin, should not be transitive to the application
            export = false
        }

        compile(
            ":dbxp-base:0.2.0.5",
            ":hibernate:$grailsVersion",
            ":tomcat:$grailsVersion",
            ":grom:0.3.0",
            ':crypto:2.0',
            ':famfamfam:1.0.1') {
                export = false
        }

        compile(
	        ':matrix-importer:0.2.3.8',
            ':ajaxflow:0.2.4',
            ':dbxp-module-base:0.6.1.3',
            ':resources:1.2',
            ':jquery:1.11.1',
            ':jquery-datatables:1.7.5',
            ':jquery-ui:1.10.4') {
            export = true
        }
    }
}

//grails.plugin.location.'dbxpModuleBase' = '../dbxpModuleBase'
//grails.plugin.location.'matrixImporter' = '../MatrixImporter'
//grails.plugin.location.'gdt' = '../GDT'
//grails.plugin.location.'dbxpBase' = '../dbxpBase'

//grails.server.port.http = "8182"  // The modern way of setting the server port
