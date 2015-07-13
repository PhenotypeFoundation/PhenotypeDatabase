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

grails.project.fork = [
     test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true], // configure settings for the test-app JVM
     run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256], // configure settings for the run-app JVM
     war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256], // configure settings for the run-war JVM
     console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]// configure settings for the Console UI JVM
]

grails.project.dependency.resolver = "maven"
 
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    legacyResolve true
    repositories {
        
        grailsPlugins()
        grailsHome()
        mavenLocal()
        grailsCentral()
        mavenCentral()
        
        // grails 1.3.9 does not seem to properly inherit maven repo's from plugins
        // so explicitely put mavenrepos in here. When upgraded to Grails 2.x this can
        // probably be removed
        mavenRepo "http://repo.grails.org/grails/plugins/"
        mavenRepo "http://nexus.dbnp.org/content/repositories/releases"
        //mavenRepo "http://repository.springsource.com/maven/bundles/release"
        //mavenRepo "http://repository.springsource.com/maven/bundles/external"
        //mavenRepo "http://repository.springsource.com/maven/libraries/release"
        //mavenRepo "http://repository.springsource.com/maven/libraries/external"

        // Repository for ISATAB tools
        //mavenRepo "http://frog.oerc.ox.ac.uk:8080/nexus-2.1.2/content/repositories/releases"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // runtime 'mysql:mysql-connector-java:5.1.5'
        runtime 'org.postgresql:postgresql:9.3-1101-jdbc41'
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
        //compile 'net.sourceforge.collections:collections-generic:4.01'

        // we seem to be needing XStream in some cases
        compile("com.thoughtworks.xstream:xstream:1.3.1")

        //runtime 'hsqldb:hsqldb:1.8.0.10'

        compile('org.codehaus.groovy.modules.http-builder:http-builder:0.5.0') { excludes "commons-logging", "xml-apis", "groovy" }
    }
    plugins {
        // plugins for the build system only
        build ":tomcat:7.0.55"
        
        compile(
                ":hibernate4:4.3.6.1",

                ":jquery:1.11.1",
                ':jquery-ui:1.10.4',

                ":resources:1.2.14",
                ":spring-security-core:2.0-RC4",
                
                ':matrix-importer:0.2.4.0',
                ':dbxp-module-base:0.6.2.0',
                
                ":famfamfam:1.0.1",
                ":mail:1.0.7",
                ":quartz:1.0.2",
                ":ajaxflow:0.2.4",
                ":webflow:2.1.0",
                ":scaffolding:2.1.1"
                )

        compile ":yui-war-minify:1.5"
        
        if (System.getProperty("grails.env") == "development") {
            // development mode only Plugins
            compile(":console:1.5.4")
        }

        // add { transative = false } to ignore dependency transition
    }
}

grails.plugin.location.'gdt' = './local-plugins/GDT'
grails.plugin.location.'dbxpSam' = './local-plugins/SAM'
grails.plugin.location.'dbxp-base' = './local-plugins/dbxpBase'

//grails.plugin.location.'dbxpModuleBase' = '../dbxpModuleBase'
//grails.plugin.location.'matrix-importer' = '../matrixImporter'
