import dbnp.studycapturing.*

import dbnp.data.Ontology
import dbnp.data.Term
import dbnp.rest.common.CommunicationManager
import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.util.GrailsUtil
import dbnp.authentication.*


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
    def springSecurityService

	def init = {servletContext ->
		"Bootstrapping application".grom()
		
		// define timezone
		System.setProperty('user.timezone', 'CET')

                def adminRole = SecRole.findByAuthority( 'ROLE_ADMIN' ) ?: new SecRole( authority: 'ROLE_ADMIN' ).save()

                def user = SecUser.findByUsername('user') ?: new SecUser(
                           username: 'user',
                           password: springSecurityService.encodePassword( 'useR123!', 'user' ),
                           email: 'user@dbnp.org',
                           userConfirmed: true, adminConfirmed: true).save(failOnError: true)

                def userAdmin = SecUser.findByUsername('admin') ?: new SecUser(
                                username: 'admin',
                                password: springSecurityService.encodePassword( 'admiN123!', 'admin' ),
                                email: 'admin@dbnp.org',
                                userConfirmed: true, adminConfirmed: true).save(failOnError: true)

                // Make the admin user an administrator
                SecUserSecRole.create userAdmin, adminRole, true

                def userTest = SecUser.findByUsername('test') ?: new SecUser(
                                username: 'test',
                                password: springSecurityService.encodePassword( 'useR123!', 'test' ),
                                email: 'test@dbnp.org',
                            userConfirmed: true, adminConfirmed: true).save(failOnError: true)

                println "Done with SpringSecurity bootstrap, created [user, admin, test]."

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
			if (Study.count() == 0 && grails.util.GrailsUtil.environment != GrailsApplication.ENV_TEST) {
				// check if special file is present in project directory
				if ((new File(System.properties['user.dir']+"/.skip-studies").exists())) {
					// yes it is, skip study bootstrapping
					"Skipping study bootstrapping".grom()

					// get species ontology
					def speciesOntology = Ontology.getOrCreateOntologyByNcboId(1132)

					// add terms
					def mouseTerm = new Term(
						name: 'Mus musculus',
						ontology: speciesOntology,
						accession: '10090'
					).with { if (!validate()) { errors.each { println it} } else save(flush:true)}

					def humanTerm = new Term(
						name: 'Homo sapiens',
						ontology: speciesOntology,
						accession: '9606'
					).with { if (!validate()) { errors.each { println it} } else save(flush:true)}
				} else {
					"Bootstrapping studies".grom()
					
					// general study boostrapping
					BootStrapStudies.addExampleStudies(user, userAdmin)
				}
			}

			println "Finished adding templates and studies"
		}

		/**
		 * attach ontologies in runtime. Possible problem is that you need
		 * an internet connection when bootstrapping though.
		 * @see dbnp.studycapturing.Subject
		 * @see dbnp.studycapturing.Sample
		 */
		TemplateEntity.getField(Subject.domainFields, 'species').ontologies = [Ontology.getOrCreateOntologyByNcboId(1132)]
		TemplateEntity.getField(Sample.domainFields, 'material').ontologies = [Ontology.getOrCreateOntologyByNcboId(1005)]

		println "Registering SAM REST methods"
		// register methods for accessing SAM's Rest services 
		if (grails.util.GrailsUtil.environment == GrailsApplication.ENV_PRODUCTION) {
			CommunicationManager.SAMServerURL = 'http://sam.nmcdsp.org'
		}
		else {
			CommunicationManager.SAMServerURL = 'http://localhost:8182/sam'
		}
		CommunicationManager.registerRestWrapperMethodsSAMtoGSCF()

		println "Done with BootStrap"
	}

	def destroy = {
	}
} 
