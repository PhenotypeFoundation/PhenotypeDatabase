import dbnp.authentication.SecRole
import dbnp.authentication.SecUser
import grails.util.Environment
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.dbnp.gdt.*
import dbnp.studycapturing.Study
import dbnp.studycapturing.Subject
import dbnp.studycapturing.Sample
import dbnp.rest.common.CommunicationManager
import dbnp.configuration.*

import dbnp.importer.impl.*
import dbnp.importer.ImporterFactory

/**
 * Application Bootstrapper
 * @Author Jeroen Wesbeek
 * @Since 20091021
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class BootStrap {
	// user spring security
	def springSecurityService

	// inject the datasource
	def dataSource

	// inject the grails application
	def grailsApplication

	def init = { servletContext ->
		// Grom a development message
		if (String.metaClass.getMetaMethod("grom")) "bootstrapping application".grom()

		// get configuration
		def config = grailsApplication.config

		// define timezone
		System.setProperty('user.timezone', 'CET')

		// set up a client (=external program) role if it does not exist
		def clientRole = SecRole.findByAuthority('ROLE_CLIENT') ?: new SecRole(authority: 'ROLE_CLIENT').save(failOnError: true)
        def templateAdminRole = SecRole.findByAuthority('ROLE_TEMPLATEADMIN') ?: new SecRole(authority: 'ROLE_TEMPLATEADMIN').save(failOnError: true)

		// set up authentication (if required)
		if (!SecRole.count() || !SecUser.count()) BootStrapAuthentication.initDefaultAuthentication(springSecurityService)

		// set up the SAM communication manager
		// this should probably more dynamic and put into the modules
		// section instead of the bootstrap as not all instances will
		// probably run WITH sam. GSCF should be able to run independently
		// from other modules. Part of gscf ticket #185
		if (config.modules) {
			// Grom a development message
			if (String.metaClass.getMetaMethod("grom")) "Registering SAM REST methods".grom()
			CommunicationManager.registerModule('gscf', config.grails.serverURL, config.modules)
			CommunicationManager.registerRestWrapperMethodsFromSAM()
		}

		// automatically handle database upgrades
		DatabaseUpgrade.handleUpgrades(dataSource, grailsApplication)

		// developmental/test template/ontology/study bootstrapping:
//		if ( Environment.current == Environment.DEVELOPMENT ||  Environment.current == Environment.TEST ) {
//			// add ontologies?
//			if (!Ontology.count()) ExampleTemplates.initTemplateOntologies()
//
//			// add templates?
//			if (!Template.count()) ExampleTemplates.initTemplates()
//
//			// add data required for the webtests?
//			if (Environment.current == Environment.TEST) ExampleStudies.addTestData()
//
//            println "Study COUNT"
//            println Study.count()
//
//			// add example studies?
//			if (!Study.count() && Environment.current == Environment.DEVELOPMENT)
//				ExampleStudies.addExampleStudies(SecUser.findByUsername('user'), SecUser.findByUsername('admin'))
//		}

		/**
		 * attach ontologies in runtime. Possible problem is that you need
		 * an internet connection when bootstrapping though.
		 * @see dbnp.studycapturing.Subject
		 * @see dbnp.studycapturing.Sample
		 */
		TemplateEntity.getField(Subject.domainFields, 'species')
                .ontologies = [
            Ontology.getOrCreateOntology("http://data.bioontology.org/ontologies/NCBITAXON"),
            Ontology.getOrCreateOntology("http://data.bioontology.org/ontologies/ENVO")
		]
		TemplateEntity.getField(Sample.domainFields, 'material')
                .ontologies = [
			Ontology.getOrCreateOntology("http://data.bioontology.org/ontologies/BTO")
		]
				
		// Preventing SSL Handshake exception for HTTPS connections java 1.7 
		// See http://stackoverflow.com/questions/7615645/ssl-handshake-alert-unrecognized-name-error-since-upgrade-to-java-1-7-0
		System.setProperty "jsse.enableSNIExtension", "false";
        
                log.info("Register importers with factory")
                def factory = ImporterFactory.getInstance()
                factory.register(new SubjectsImporter() )
                factory.register(new SamplesImporter() )
                factory.register(new EventsImporter() )
                factory.register(new SamplingEventsImporter() )
                factory.register(new AssaysImporter() )
                
                factory.register(new org.dbxp.sam.importer.PlatformsImporter() )
                factory.register(new org.dbxp.sam.importer.FeaturesImporter() )
                factory.register(new org.dbxp.sam.importer.MeasurementsImporter() )
	}

	def destroy = {
		// Grom a development message
		if (String.metaClass.getMetaMethod("grom")) "stopping application...".grom()
	}
} 
