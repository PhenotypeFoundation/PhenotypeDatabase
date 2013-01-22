grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

// uncomment (and adjust settings) to fork the JVM to isolate classpaths
//grails.project.fork = [
//   run: [maxMemory:1024, minMemory:64, debug:false, maxPerm:256]
//]

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
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

	    // Repository for ISATAB tools
	    mavenRepo "http://frog.oerc.ox.ac.uk:8080/nexus-2.1.2/content/repositories/releases"
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
        //compile('org.quartz-scheduler:quartz:1.8.4') {
            // resolve SLF4J version conflict:
            // SLF4J: The requested version 1.5.8 by your slf4j binding is not compatible with [1.6]
            //excludes([ group: 'org.slf4j', name: 'slf4j-api', version: '1.5.8'])
        //}

	    /*compile('org.isatools:ISAcreator:1.7.0') {
		    transitive = false
	    } */
		// dependency for ISATAB schema
		compile 'net.sourceforge.collections:collections-generic:4.01'

	    // we seem to be needing XStream in some cases
	    compile("com.thoughtworks.xstream:xstream:1.3.1")

	    //runtime 'hsqldb:hsqldb:1.8.0.10'

        // webflow is borked, see:
        // http://jira.grails.org/browse/GRAILS-9783
        compile "org.grails:grails-webflow:2.2.0"
    }

	plugins {
		// plugins required in development, but not in the WAR file
		provided(
				":tomcat:$grailsVersion",
                ":grom:latest.integration",
                ":trackr:latest.integration",
                ":console:1.2"
        )

        // compile time dependencies
		compile(
                ":hibernate:$grailsVersion",
                ":jquery:latest.integration",
                ":ajaxflow:latest.integration",
                ":spring-security-core:1.2.7.3",
                ":gdt:latest.integration",
                ":famfamfam:1.0.1",
                ":mail:1.0",
                ":quartz:1.0-RC5",
                ":cache:1.0.1"
        )

        // webflow is borked, see:
        // http://jira.grails.org/browse/GRAILS-9783
        compile ":webflow:2.0.0", {
            exclude 'grails-webflow'
        }

        // no transitive plugins for gdtImporter
        compile(":gdtimporter:0.5.6.1") {
            transitive = false
        }

//        // define environment specific plugins
//        if (System.getProperty("grails.env") == "development") {
//            // development mode only Plugins
//            compile (
//                    ":grom:latest.integration",
//                    ":trackr:latest.integration",
//                    ":console:1.2"
//            )
//        }
	}
}

//grails.plugin.location.'grom' = '../grom'
//grails.plugin.location.'grom' = '../../4np/grails-grom'
//grails.plugin.location.'ajaxflow' = '../ajaxflow'
//grails.plugin.location.'ajaxflow' = '../../4np/grails-ajaxflow'
//grails.plugin.location.'gdt' = '../GDT'
//grails.plugin.location.'jumpbar' = '../jumpbar'
//grails.plugin.location.'gdtimporter' = '../GDTImporter'
