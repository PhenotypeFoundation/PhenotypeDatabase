import dbnp.studycapturing.*

import dbnp.data.Ontology
import dbnp.data.Term
import dbnp.rest.common.CommunicationManager
import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.util.GrailsUtil

// Imports for Nimble
import grails.plugins.nimble.InstanceGenerator
import grails.plugins.nimble.core.LevelPermission
import grails.plugins.nimble.core.Role
import grails.plugins.nimble.core.Group
import grails.plugins.nimble.core.AdminsService
import grails.plugins.nimble.core.UserService

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

	// Injections for Nimble
	def grailsApplication
	def nimbleService
	def userService
	def adminsService

	def init = {servletContext ->
		// define timezone
		System.setProperty('user.timezone', 'CET')

		// If there are no users yet in the database
		println "Executing Nimble bootstrap..."

	    // The following must be executed
	    nimbleService.init()

	    // Add users
		def user

		if (dbnp.user.User.count() == 0) {
			println "Adding example user..."

			// Create example User account
			user = InstanceGenerator.user()
			user.username = "user"
			user.pass = 'useR123!'
			user.passConfirm = 'useR123!'
			user.enabled = true

			def userProfile = InstanceGenerator.profile()
			userProfile.fullName = "Test User"
			userProfile.owner = user
			user.profile = userProfile

			def savedUser = userService.createUser(user)
			if (savedUser.hasErrors()) {
			  savedUser.errors.each {
				log.error(it)
			  }
			  throw new RuntimeException("Error creating example user")
			}

			println "Adding example admin user..."

			// Create example Administrative account
			def admins = Role.findByName(AdminsService.ADMIN_ROLE)
			def admin = InstanceGenerator.user()
			admin.username = "admin"
			admin.pass = "admiN123!"
			admin.passConfirm = "admiN123!"
			admin.enabled = true

			def adminProfile = InstanceGenerator.profile()
			adminProfile.fullName = "Administrator"
			adminProfile.owner = admin
			admin.profile = adminProfile

			def savedAdmin = userService.createUser(admin)
			if (savedAdmin.hasErrors()) {
			  savedAdmin.errors.each {
				log.error(it)
			  }
			  throw new RuntimeException("Error creating administrator")
			}

			adminsService.add(admin)
		}
		else {
			user = dbnp.user.User.findByUsername("user")
		}

		println "Done with Nimble bootstrap"

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
			if (Study.count() == 0 && grails.util.GrailsUtil.environment == GrailsApplication.ENV_PRODUCTION) {
				// check if special file is present in project directory
				if ((new File(System.properties['user.dir']+"/.skip-studies").exists())) {
					// yes it is, skip study bootstrapping
					println ".skipping study bootstrapping"

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
					// general study boostrapping
					BootStrapStudies.addExampleStudies(user)
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
