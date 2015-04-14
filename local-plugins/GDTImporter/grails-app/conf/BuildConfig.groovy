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
	legacyResolve true
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

        // runtime 'mysql:mysql-connector-java:5.1.5'
    }

    plugins {
        build(
                ":rest-client-builder:1.0.3"
        ) {
            // plugin only plugin, should not be transitive to the application
            export = false
        }

        compile(
        ":ajaxflow:0.2.4",
        ":jquery:1.11.1",
        ":crypto:2.0",
        ":gdt:0.5.0.4"
        ) {
            export = false
        }

        compile(
            ":webflow:2.0.8.1",
            ":rest:0.8"
        )
    }
}

//grails.plugin.location.'gdt' = '../GDT'