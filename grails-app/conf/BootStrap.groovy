import dbnp.studycapturing.*

import dbnp.data.Ontology
import dbnp.data.Term
import dbnp.rest.common.CommunicationManager
import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.util.GrailsUtil

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
	def init = {servletContext ->
		// define timezone
		System.setProperty('user.timezone', 'CET')

		// If there are no templates yet in the database
		if (Template.count() == 0) {
			println "No templates in the current database.";

			// If in development or test mode, add the ontologies manually to the database
			// without contacting the BioPortal website, to avoid annoying hiccups when the server is busy
			if (grails.util.GrailsUtil.environment != GrailsApplication.ENV_PRODUCTION) {
				println "Adding ontology descriptors"
				BootStrapTemplates.initTemplateOntologies()
			}

			// Add example study, subject, event etc. templates
			BootStrapTemplates.initTemplates()

			// If in development mode and no studies are present, add example studies
			if (Study.count() == 0 && grails.util.GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT) {
				BootStrapStudies.addExampleStudies()
			}
		}

		/**
		 * attach ontologies in runtime. Possible problem is that you need
		 * an internet connection when bootstrapping though.
		 * @see dbnp.studycapturing.Subject
		 * @see dbnp.studycapturing.Sample
		 */
		TemplateEntity.getField(Subject.domainFields, 'species').ontologies = [Ontology.getOrCreateOntologyByNcboId(1132)]
		TemplateEntity.getField(Sample.domainFields, 'material').ontologies = [Ontology.getOrCreateOntologyByNcboId(1005)]

		// register methods for accessing SAM's Rest services 
		if (grails.util.GrailsUtil.environment == GrailsApplication.ENV_PRODUCTION) {
			CommunicationManager.SAMServerURL = 'http://sam.dbnp.org'
		}
		else {
			CommunicationManager.SAMServerURL = 'http://localhost:8182/sam'
		}
		CommunicationManager.registerRestWrapperMethodsSAMtoGSCF()
	}

	def destroy = {
	}
} 
