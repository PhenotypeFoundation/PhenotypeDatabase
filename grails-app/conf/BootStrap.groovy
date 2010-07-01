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

		if (Template.count() == 0) {
			println "No templates in the current database.";
			// Add example study, subject, event etc. templates
			BootStrapTemplates.initTemplates()

			// Add example studies
			if (Study.count() == 0 && grails.util.GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT) {

				// When the code is properly refactored, BootStrapStudies.addExampleStudies() may be called here
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
		CommunicationManager.SAMServerURL = 'nbx5.nugo.org/sam'
    	CommunicationManager.registerRestWrapperMethodsSAMtoGSCF()
    	CommunicationManager.registerRestWrapperMethodsSAMtoGSCF()
	}

	def destroy = {
	}
} 
