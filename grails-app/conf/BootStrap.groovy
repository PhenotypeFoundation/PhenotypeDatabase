import dbnp.authentication.SecRole
import dbnp.authentication.SecUser
import org.codehaus.groovy.grails.commons.GrailsApplication
import dbnp.data.Ontology
import dbnp.studycapturing.Template
import dbnp.studycapturing.Study
import dbnp.studycapturing.TemplateEntity
import dbnp.studycapturing.Subject
import dbnp.studycapturing.Sample
import dbnp.rest.common.CommunicationManager
import org.codehaus.groovy.grails.commons.*

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

	def init = { servletContext ->
		// grom what's happening
		"bootstrapping application".grom()

		// get configuration
		def config = ConfigurationHolder.config

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
			// register SAM REST methods
			"Registering SAM REST methods".grom()
			CommunicationManager.registerModule('gscf', config.grails.serverURL, config.modules)
			CommunicationManager.registerRestWrapperMethodsFromSAM()
		}

		// developmental bootstrapping:
		//      - templates
		//      - ontologies
		//      - and/or studies
		if (grails.util.GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT || grails.util.GrailsUtil.environment == "dbnpdemo") {
			// add ontologies?
			if (!Ontology.count()) BootStrapTemplates.initTemplateOntologies()

			// add templates?
			if (!Template.count()) BootStrapTemplates.initTemplates()

			// add example studies?
			if (!Study.count() && grails.util.GrailsUtil.environment != "demo") BootStrapStudies.addExampleStudies(SecUser.findByUsername('user'), SecUser.findByUsername('admin'))
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
		"stopping application...".grom()
	}
} 
