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
        grailsHome()
        
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
        // Plugins for the build system only
        build ":tomcat:7.0.70"
        build ":rest-client-builder:1.0.3"

        // Plugins needed at runtime but not for compilation
        runtime ":hibernate4:4.3.10"
        runtime ":jquery:1.11.1"

        // Plugins needed for compilation
        compile ":ajaxflow:0.2.4"
        compile ":webflow:2.1.0"
        compile ":rest:0.8"
    }
}
