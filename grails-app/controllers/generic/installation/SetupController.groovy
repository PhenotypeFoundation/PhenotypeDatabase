package generic.installation

import grails.plugins.springsecurity.Secured
import dbnp.authentication.SecUser
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import groovy.sql.Sql

/**
 * ajaxflow Controller
 *
 * @author	Jeroen Wesbeek
 * @since	20110318
 *
 * Revision information:
 * $Rev:  66849 $
 * $Author:  duh $
 * $Date:  2010-12-08 15:12:54 +0100 (Wed, 08 Dec 2010) $
 */
@Secured(['ROLE_ADMIN'])
class SetupController {
	// the pluginManager is used to check if the Grom
	// plugin is available so we can 'Grom' development
	// notifications to the unified notifications daemon
	// (see http://www.grails.org/plugin/grom)
	def pluginManager
	
	/**
	 * index method, redirect to the webflow
	 * @void
	 */
	def index = {
		// Grom a development message
		if (pluginManager.getGrailsPlugin('grom')) "redirecting into the webflow".grom()

		/**
		 * Do you believe it in your head?
		 * I can go with the flow
		 * Don't say it doesn't matter (with the flow) matter anymore
		 * I can go with the flow (I can go)
		 * Do you believe it in your head?
		 */
		redirect(action: 'pages')
	}

	/**
	 * WebFlow definition
	 * @void
	 */
	def pagesFlow = {
		def authenticationService

		// start the flow
		onStart {
			// Grom a development message
			if (pluginManager.getGrailsPlugin('grom')) "entering the WebFlow".grom()

			// get configuration
			def config = ConfigurationHolder.config

			// define variables in the flow scope which is availabe
			// throughout the complete webflow also have a look at
			// the Flow Scopes section on http://www.grails.org/WebFlow
			//
			// The following flow scope variables are used to generate
			// wizard tabs. Also see common/_tabs.gsp for more information
			flow.page = 0
			flow.pages = [
				[title: 'Configuration Location'],
				[title: 'Database'],
				[title: 'Email / URL'],
				[title: 'Summary'],
				[title: 'Apache Configuration'],
				[title: 'Done']
			]
			flow.cancel = true
			flow.quickSave = true

			// define famfamfam icons
			flow.icons = [
			    true	: 'accept',
				false	: 'cancel'
			]

			success()
		}

		// render the main wizard page which immediately
		// triggers the 'next' action (hence, the main
		// page dynamically renders the study template
		// and makes the flow jump to the study logic)
		mainPage {
			render(view: "/setup/index")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the main Ajaxflow page (index.gsp)".grom()

				// let the view know we're in page 1
				flow.page = 1
				success()
			}
			on("next").to "configuration"
		}

		// first wizard page
		configuration {
			render(view: "_configuration_location")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial: pages/_configuration.gsp".grom()

				flow.page = 1

				// try to load config
				loadPropertiesFile(flow)

				success()
			}
			on("next") {
				if (flow.configInfo.pathSummary && flow.configInfo.fileSummary) { success() } else { error() }
			}.to "database"
			on("toPageTwo") {
				if (flow.configInfo.pathSummary && flow.configInfo.fileSummary) { success() } else { error() }
			}.to "database"
			on("toPageThree") {
				if (flow.configInfo.pathSummary && flow.configInfo.fileSummary) { success() } else { error() }
			}.to "email"
			on("toPageFour") {
				if (flow.configInfo.pathSummary && flow.configInfo.fileSummary) { success() } else { error() }
			}.to "summary"
			on("toPageFive") {
				if (flow.configInfo.pathSummary && flow.configInfo.fileSummary) { success() } else { error() }
			}.to "apache"
			on("toPageSix") {
				if (flow.configInfo.pathSummary && flow.configInfo.fileSummary) {
					flow.page = 6
					success()
				} else {
					error()
				}
			}.to "save"
			on("toConfigurationPath").to "configurationPath"
			on("toConfigurationFile").to "configurationFile"
		}

		// create the configuration path
		configurationPath {
			action {
				// does the path exist?
				if (!flow.configInfo.pathExists) {
					// no, attempt to create it
					if (flow.configInfo.path.mkdirs()) {
						// Grom a development message
						if (pluginManager.getGrailsPlugin('grom')) ".created ${flow.configInfo.path}".grom()

						// success!
						success()
					} else {
						error()
					}
				} else {
					error()
				}
			}
			on("success").to "configuration"
			on("error").to "configurationPathError"
		}

		// show manual procedure for creating configuration path
		configurationPathError {
			render(view: "_configuration_path_error")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial: pages/_configuration_path_error.gsp".grom()

				flow.page = 1
			}
			on("next").to "configuration"
		}

		// create the configuration file
		configurationFile {
			action {
				// does the file exist?
				if (!flow.configInfo.fileExists) {
					// no, attempt to create it
					try {
						flow.configInfo.file << "# ${meta(name: 'app.name')} ${grails.util.GrailsUtil.environment} configuration\n"
						flow.configInfo.file << "#\n"
						flow.configInfo.file << "# \$Author\$\n"
						flow.configInfo.file << "# \$Date\$\n"
						flow.configInfo.file << "# \$Rev\$\n"

						// grom debug message
						if (pluginManager.getGrailsPlugin('grom')) ".created ${flow.configInfo.file}".grom()

						// success!
						success()
					} catch (Exception e) {
						error()
					}
				} else {
					error()
				}
			}
			on("success").to "configuration"
			on("error").to "configurationFileError"
		}

		// show manual procedure for creating configuration path
		configurationFileError {
			render(view: "_configuration_file_error")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial: pages/_configuration_file_error.gsp".grom()

				flow.page = 1
			}
			on("next").to "configuration"
		}

		// database page
		database {
			render(view: "_database")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial: pages/_database.gsp".grom()

				flow.page = 2

				success()
			}
			on("next") {
				// store form values
				databasePage(flow, flash, params) ? success() : error()
			}.to "email"
			on("previous") {
				// store form values
				databasePage(flow, flash, params) ? success() : error()
			}.to "configuration"
			on("toPageOne") {
				// store form values
				databasePage(flow, flash, params) ? success() : error()
			}.to "configuration"
			on("toPageThree") {
				// store form values
				databasePage(flow, flash, params) ? success() : error()
			}.to "email"
			on("toPageFour") {
				// store form values
				databasePage(flow, flash, params) ? success() : error()
			}.to "summary"
			on("toPageFive") {
				// store form values
				databasePage(flow, flash, params) ? success() : error()
			}.to "apache"
			on("toPageSix") {
				// store form values
				if (databasePage(flow, flash, params)) {
					flow.page = 6
					success()
				} else {
					error()
				}
			}.to "save"
		}

		// email configuration page
		email {
			render(view: "_email")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_email.gsp".grom()

				flow.page = 3
				success()
			}
			on("next") {
				emailPage(flow, flash, params) ? success() : error()
			}.to "summary"
			on("previous") {
				emailPage(flow, flash, params) ? success() : error()
			}.to "database"
			on("toPageOne") {
				emailPage(flow, flash, params) ? success() : error()
			}.to "configuration"
			on("toPageTwo") {
				emailPage(flow, flash, params) ? success() : error()
			}.to "database"
			on("toPageFour") {
				emailPage(flow, flash, params) ? success() : error()
			}.to "summary"
			on("toPageFive") {
				emailPage(flow, flash, params) ? success() : error()
			}.to "apache"
			on("toPageSix") {
				if (emailPage(flow, flash, params)) {
					flow.page = 6
					success()
				} else {
					error()
				}
			}.to "save"
		}

		// summary page
		summary {
			render(view: "_summary")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_summary.gsp".grom()

				// write properties file
				writePropertiesFile(flow)

				flow.page = 4
				success()
			}
			on("next") {
				// put some logic in here
				flow.page = 6
			}.to "save"
			on("previous").to "pageThree"
			on("toPageOne").to "configuration"
			on("toPageTwo").to "database"
			on("toPageThree").to "email"
			on("toPageFive").to "apache"
			on("toPageSix") {
				flow.page = 6
			}.to "save"
		}

		// apache page
		apache {
			render(view: "_apache")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_apache.gsp".grom()

				flow.page = 5

				flash.context= org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().contextPath
				flash.domain = flow.configInfo.properties.getProperty('grails.serverURL').replaceFirst(/http:\/\//,"").split(":|/").first()

				success()
			}
			on("next") {
				// put some logic in here
				flow.page = 6
			}.to "save"
			on("previous").to "pageThree"
			on("toPageOne").to "configuration"
			on("toPageTwo").to "database"
			on("toPageThree").to "email"
			on("toPageFour").to "summary"
			on("toPageSix") {
				flow.page = 6
			}.to "save"
		}

		// save action
		save {
			action {
				// here you can validate and save the
				// instances you have created in the
				// ajax flow.
				try {
					// Grom a development message
					if (pluginManager.getGrailsPlugin('grom')) ".persisting instances to the database...".grom()

					// put your bussiness logic in here
					success()
				} catch (Exception e) {
					// put your error handling logic in
					// here
					flow.page = 4
					error()
				}
			}
			on("error").to "error"
			on(Exception).to "error"
			on("success").to "finalPage"
		}

		// render errors
		error {
			render(view: "_error")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_error.gsp".grom()

				// set page to 4 so that the navigation
				// works (it is disabled on the final page)
				flow.page = 4
			}
			on("next").to "save"
			on("previous").to "pageFour"
			on("toPageOne").to "configuration"
			on("toPageTwo").to "database"
			on("toPageThree").to "email"
			on("toPageFour").to "summary"
			on("toPageFive").to "apache"
			on("toPageSix").to "save"

		}

		// last wizard page
		finalPage {
			render(view: "_final_page")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_final_page.gsp".grom()
				
				success()
			}
		}
	}

	/**
	 * handle database configuration
	 *
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def databasePage(flow, flash, params) {
		// update database properties
		params.dataSource.each { name, value ->
			flow.configInfo.properties.setProperty("dataSource.${name}", value)
		}

		// try to connect to the database
		try {
			def sql = Sql.newInstance(
				flow.configInfo.properties.getProperty('dataSource.url'),
				flow.configInfo.properties.getProperty('dataSource.username'),
				flow.configInfo.properties.getProperty('dataSource.password'),
				flow.configInfo.properties.getProperty('dataSource.driverClassName')
			)

			writePropertiesFile(flow)

			flash.connection=true
		} catch (Exception e) {
			flash.connection=false
		}

		return flash.connection
	}

	/**
	 * handle email and url configuration
	 *
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def emailPage(flow, flash, params) {
		// update properties
		flow.configInfo.properties.setProperty('grails.plugins.springsecurity.ui.forgotPassword.emailFrom', params['grails.plugins.springsecurity.ui.forgotPassword.emailFrom'])
		flow.configInfo.properties.setProperty('grails.serverURL', params['grails.serverURL'])

		writePropertiesFile(flow)

		return true
	}

	/**
	 * load the configuration properties
	 *
	 * @param flow
	 * @return
	 */
	def loadPropertiesFile(flow) {
		// config
		def configPath = new File("${System.getProperty("user.home")}/etc/${meta(name: 'app.name')}/")
		def configFile = new File("${System.getProperty("user.home")}/etc/${meta(name: 'app.name')}/${grails.util.GrailsUtil.environment}.properties")

		// add configuration information to the flow scope
		flow.configInfo = [
			path: configPath,
			pathExists: configPath.exists(),
			pathCanRead: configPath.canRead(),
			pathCanWrite: configPath.canWrite(),
			pathSummary: (configPath.exists() && configPath.canRead() && configPath.canWrite()),
			file: configFile,
			fileExists: configFile.exists(),
			fileCanRead: configFile.canRead(),
			fileCanWrite: configFile.canWrite(),
			fileSummary: (configFile.exists() && configFile.canRead() && configFile.canWrite()),
			properties: null
		]

		// parse properties
		if (flow.configInfo.pathSummary && flow.configInfo.fileSummary) {
			def file = new FileInputStream(flow.configInfo.file.toString())
			def properties = new Properties()
			properties.load(file)

			// and store in flowscope
			flow.configInfo.properties = properties
		}
	}

	/**
	 * save the configuration properties
	 *
	 * @param flow
	 * @return
	 */
	def writePropertiesFile(flow) {
		// write properties
		def file = new FileOutputStream(flow.configInfo.file.toString())
		flow.configInfo.properties.store(file, " ${meta(name: 'app.name')} ${grails.util.GrailsUtil.environment} configuration\n#\n# \$Author\$\n# \$Date\$\n# \$Rev\$\n")

		// and load them back into the flow
		loadPropertiesFile(flow)
	}
}
