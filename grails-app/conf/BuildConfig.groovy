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
        grailsCentral()
        grailsRepo "http://grails.org/plugins"

        mavenCentral()
        mavenRepo "http://nexus.nmcdsp.org/content/repositories/releases"
        mavenRepo "http://repository.springsource.com/maven/bundles/release"
        mavenRepo "http://repository.springsource.com/maven/bundles/external"
        mavenRepo "http://repository.springsource.com/maven/libraries/release"
        mavenRepo "http://repository.springsource.com/maven/libraries/external"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // runtime 'mysql:mysql-connector-java:5.1.5'
		runtime 'postgresql:postgresql:9.1-901.jdbc3'
		compile 'org.apache.poi:poi:3.7'
		compile 'org.apache.poi:poi-ooxml:3.7'
		compile 'org.apache.poi:poi-ooxml-schemas:3.7'

        // quartz jar is not packaged in the war properly
        // make sure to pull it in
        compile 'org.quartz-scheduler:quartz:1.8.4'
    }
	plugins {
		compile(
                ":hibernate:$grailsVersion",
                ":tomcat:$grailsVersion",
                ":jquery:latest.integration",

                ":grom:latest.integration",

                ":webflow:1.3.8",
                ":ajaxflow:latest.integration",

                ":crypto:2.0",
                ":spring-security-core:1.1.2",

                ":gdt:0.3.0",
                ":gdtimporter:0.5.0",

                ":famfamfam:1.0.1",

                ":mail:1.0",

//                ":grails-melody:1.13",
//                ":trackr:0.6.4",

                ":jumpbar:0.1.5",

                ":quartz:1.0-RC2"
        )

        // add { transative = false } to ignore dependency transition

        runtime ':grails-melody:1.11'
	}
}

//grails.plugin.location.'grom' = '../grom'
//grails.plugin.location.'grom' = '../../4np/grails-grom'
//grails.plugin.location.'ajaxflow' = '../ajaxflow'
//grails.plugin.location.'ajaxflow' = '../../4np/grails-ajaxflow'
//grails.plugin.location.'gdt' = '../GDT'
//grails.plugin.location.'jumpbar' = '../jumpbar'
//grails.plugin.location.'gdtimporter' = '../GDTImporter'
