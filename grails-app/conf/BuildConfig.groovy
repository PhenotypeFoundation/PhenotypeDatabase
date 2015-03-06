grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
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
        mavenRepo "http://nexus.dbnp.org/content/repositories/releases"
        mavenRepo "http://repository.springsource.com/maven/bundles/release"
        mavenRepo "http://repository.springsource.com/maven/bundles/external"
        mavenRepo "http://repository.springsource.com/maven/libraries/release"
        mavenRepo "http://repository.springsource.com/maven/libraries/external"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
    }

    plugins {
        build(  ":tomcat:$grailsVersion",
                ":release:2.2.1",
                //Temporary static version of Rest Client Builder due to compile error
                ":rest-client-builder:1.0.3"
        ) {
            // plugin only plugin, should not be transitive to the application
            export = false
        }

        compile(":hibernate:$grailsVersion",
		":jquery:1.8.3",
		":webflow:2.0.8.1",
		":ajaxflow:latest.integration",
        ":rest:0.8"
        ) {
            export = false
        }
		
		compile ':webflow:2.0.0', {
			exclude 'grails-webflow'
		}

    }
}
