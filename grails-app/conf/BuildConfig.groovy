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

	    // grails 1.3.9 does not seem to properly inherit maven repo's from plugins
	    // so explicitely put ontocat in here. When upgraded to Grails 2.x this can
	    // probably be removed
	    mavenRepo "http://ontocat.sourceforge.net/maven/repo"

		// other maven repo's
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
        compile('org.quartz-scheduler:quartz:1.8.4') {
            // resolve SLF4J version conflict:
            // SLF4J: The requested version 1.5.8 by your slf4j binding is not compatible with [1.6]
            excludes([ group: 'org.slf4j', name: 'slf4j-api', version: '1.5.8'])
        }

	    // we seem to be needing XStream in some cases
	    compile("com.thoughtworks.xstream:xstream:1.3.1")
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

<<<<<<< HEAD
                ":gdt:0.3.2",
                ":gdtimporter:0.5.2",
=======
                ":gdt:0.3.1",
                ":gdtimporter:0.5.3",
>>>>>>> upgraded gdtimporter 0.5.3

                ":famfamfam:1.0.1",

                ":mail:1.0",

//                ":grails-melody:1.13",
                ":trackr:0.7.3",

                ":jumpbar:0.1.5",

                ":quartz:1.0-RC2"
        )

        // add { transative = false } to ignore dependency transition

//        runtime ':grails-melody:1.11'
	}
}

//grails.plugin.location.'grom' = '../grom'
//grails.plugin.location.'grom' = '../../4np/grails-grom'
//grails.plugin.location.'ajaxflow' = '../ajaxflow'
//grails.plugin.location.'ajaxflow' = '../../4np/grails-ajaxflow'
//grails.plugin.location.'gdt' = '../GDT'
//grails.plugin.location.'jumpbar' = '../jumpbar'
//grails.plugin.location.'gdtimporter' = '../GDTImporter'
