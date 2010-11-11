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

		// define timezone
		System.setProperty('user.timezone', 'CET')

		// set up authentication (if required)
		if (!SecRole.count() || !SecUser.count()) BootStrapAuthentication.initDefaultAuthentication(springSecurityService)

		// developmental bootstrapping:
		//      - templates
		//      - ontologies
		//      - and/or studies
		if (grails.util.GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT) {
			// add ontologies?
			if (!Ontology.count()) BootStrapTemplates.initTemplateOntologies()

			// add templates?
			if (!Template.count()) BootStrapTemplates.initTemplates()

			// add example studies?
			if (!Study.count()) BootStrapStudies.addExampleStudies(SecUser.findByUsername('user'), SecUser.findByUsername('admin'))
		}

		/**
		 * attach ontologies in runtime. Possible problem is that you need
		 * an internet connection when bootstrapping though.
		 * @see dbnp.studycapturing.Subject
		 * @see dbnp.studycapturing.Sample
		 */
		TemplateEntity.getField(Subject.domainFields, 'species').ontologies = [Ontology.getOrCreateOntologyByNcboId(1132)]
		TemplateEntity.getField(Sample.domainFields, 'material').ontologies = [Ontology.getOrCreateOntologyByNcboId(1005)]

		// register SAM REST methods
		"Registering SAM REST methods".grom()
		CommunicationManager.registerRestWrapperMethodsSAMtoGSCF()
	}

	def destroy = {
		println "stopping application..."
	}
} 
