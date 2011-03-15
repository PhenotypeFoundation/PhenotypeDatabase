import dbnp.authentication.SecRole
import dbnp.authentication.SecUser
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.dbnp.gdt.*
import dbnp.studycapturing.Study
import dbnp.studycapturing.Subject
import dbnp.studycapturing.Sample
import dbnp.rest.common.CommunicationManager
import org.codehaus.groovy.grails.commons.*
import dbnp.configuration.*


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

	def init = { servletContext ->
		// Grom a development message
		if (String.metaClass.getMetaMethod("grom")) "bootstrapping application".grom()

		// get configuration
		def config = ConfigurationHolder.config

		// set the mail configuration properties in runtime, as
		// this is not definable in a .properties file due to the
		// fact for the mail properties a map is used
		if (config.grails.mail.username && config.grails.mail.password) {
			// use TLS
			config.grails.mail.port	= 465
			config.grails.mail.props= [
				"mail.smtp.auth": "true",
				"mail.smtp.socketFactory.port": '465',
				"mail.smtp.socketFactory.class": "javax.net.ssl.SSLSocketFactory",
				"mail.smtp.socketFactory.fallback": "false"
			]
		}

		// define timezone
		System.setProperty('user.timezone', 'CET')

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
		DatabaseUpgrade.handleUpgrades(dataSource)

		// developmental/test/demo bootstrapping:
		//      - templates
		//      - ontologies
		//      - and/or studies
		if (    grails.util.GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT ||
                grails.util.GrailsUtil.environment == GrailsApplication.ENV_TEST ||
                grails.util.GrailsUtil.environment == "dbnpdemo") {
			// add ontologies?
			if (!Ontology.count()) ExampleTemplates.initTemplateOntologies()

			// add templates?
			if (!Template.count()) ExampleTemplates.initTemplates()

			// add data required for the webtests?
			if (grails.util.GrailsUtil.environment == GrailsApplication.ENV_TEST) ExampleStudies.addTestData()

			// add example studies?
			if (!Study.count() && grails.util.GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT)
				ExampleStudies.addExampleStudies(SecUser.findByUsername('user'), SecUser.findByUsername('admin'))
		}

		/**
		 * attach ontologies in runtime. Possible problem is that you need
		 * an internet connection when bootstrapping though.
		 * @see dbnp.studycapturing.Subject
		 * @see dbnp.studycapturing.Sample
		 */
		TemplateEntity.getField(Subject.domainFields, 'species').ontologies = [Ontology.getOrCreateOntologyByNcboId(1132)]
		TemplateEntity.getField(Sample.domainFields, 'material').ontologies = [Ontology.getOrCreateOntologyByNcboId(1005)]
	}

	def destroy = {
		// Grom a development message
		if (String.metaClass.getMetaMethod("grom")) "stopping application...".grom()
	}
} 
