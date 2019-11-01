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
     run: [maxMemory: 4560, minMemory: 64, debug: false, maxPerm: 256], // configure settings for the run-app JVM
     war: [maxMemory: 4560, minMemory: 64, debug: false, maxPerm: 256], // configure settings for the run-war JVM
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
        grailsHome()
        grailsPlugins()
        grailsCentral()
        mavenCentral()
        mavenLocal()

        // Repository for ISATAB tools
        //mavenRepo "http://frog.oerc.ox.ac.uk:8080/nexus-2.1.2/content/repositories/releases"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
        // runtime 'mysql:mysql-connector-java:5.1.5'
        runtime 'org.postgresql:postgresql:9.3-1101-jdbc41'
        compile 'org.apache.poi:poi:3.15'
        compile 'org.apache.poi:poi-ooxml:3.15'
        compile 'org.apache.poi:ooxml-schemas:1.3'

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
        // Plugins for the build system only
        build ":tomcat:7.0.70"

        // Plugins needed at runtime but not for compilation
        runtime ":hibernate4:4.3.10"
        runtime ":jquery:1.11.1"
        runtime ":resources:1.2.14"

        // Plugins needed for compilation
        compile ":spring-security-core:2.0.0"
        compile ':jquery-ui:1.10.4'
        compile ":mail:1.0.7"
        compile ":famfamfam:1.0.1"
        compile ":quartz:1.0.2"
        compile ":ajaxflow:0.2.4"
        compile ":webflow:2.1.0"
        compile ":scaffolding:2.1.2"
        compile ":yui-war-minify:1.5"
        
        if (System.getProperty("grails.env") == "development") {
            // development mode only Plugins
            compile(":console:1.5.4")
        }

        // Now included as local plugins
//        compile ":matrix-importer:0.2.5.0"
//        compile ":dbxp-module-base:0.6.2.0"

        // add { transative = false } to ignore dependency transition
    }
}

grails.plugin.location.'gdt' = './local-plugins/GDT'
grails.plugin.location.'dbxpSam' = './local-plugins/SAM'
grails.plugin.location.'dbxp-base' = './local-plugins/dbxpBase'

grails.plugin.location.'dbxpModuleBase' = './local-plugins/dbxpModuleBase'
grails.plugin.location.'matrix-importer' = './local-plugins/matrixImporter'
