package generic.installation

import grails.plugins.springsecurity.Secured
import dbnp.authentication.SecUser
import org.codehaus.groovy.grails.commons.ConfigurationHolder

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
			println config.dump()
			println config.dataSource.dump()

			def configPath = new File("/etc/${meta(name: 'app.name')}/")
			if (configPath.exists()) {
				println "path exists"
			} else {
				println "path does not exist"
			}

			if (configPath.canWrite()) {
				println "path is writable"
			} else {
				println "path is not writable"
			}

			def configFile = new File("/etc/${meta(name: 'app.name')}/${grails.util.GrailsUtil.environment}.properties")
			if (configFile.exists()) {
				println "file exists"
			} else {
				println "file does not exist"
			}


			// define variables in the flow scope which is availabe
			// throughout the complete webflow also have a look at
			// the Flow Scopes section on http://www.grails.org/WebFlow
			//
			// The following flow scope variables are used to generate
			// wizard tabs. Also see common/_tabs.gsp for more information
			flow.page = 0
			//flow.config = ConfigurationHolder.config
			flow.pages = [
				[title: 'Configuration Location'],
				[title: 'Database'],
				[title: 'Page Three'],
				[title: 'Page Four'],
				[title: 'Done']
			]
			flow.cancel = true
			flow.quickSave = true

			// define famfamfam icons
			flow.icons = [
			    'true'	: 'accept',
				false	: 'cancel'
			]

			// add configuration information to the flow scope
			flow.configInfo = [
			    path			: configPath,
				pathExists		: configPath.exists(),
				pathCanRead		: configPath.canRead(),
				pathCanWrite	: configPath.canWrite(),
				pathSummary		: (configPath.exists() && configPath.canRead() && configPath.canWrite()),
				file			: configFile,
				fileExists		: configFile.exists(),
				fileCanRead		: configFile.canRead(),
				fileCanWrite	: configFile.canWrite(),
				fileSummary		: (configFile.exists() && configFile.canRead() && configFile.canWrite())
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
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial: pages/_database.gsp".grom()

				flow.page = 1
				success()
			}
			on("next") {
				// put your bussiness logic (if applicable) in here
			}.to "pageTwo"
			on("toPageTwo") {
				// put your bussiness logic (if applicable) in here
			}.to "pageTwo"
			on("toPageThree") {
				// put your bussiness logic (if applicable) in here
			}.to "pageThree"
			on("toPageFour") {
				// put your bussiness logic (if applicable) in here
			}.to "pageFour"
			on("toPageFive") {
				// put your bussiness logic (if applicable) in here
				flow.page = 5
			}.to "save"
			on("toConfigurationPath").to "configurationPath"
			on("toConfigurationFile").to "configurationFile"
		}

		// second wizard page
		database {
			render(view: "_database")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial: pages/_page_two.gsp".grom()

				flow.page = 2
				success()
			}
			on("next").to "pageThree"
			on("previous").to "pageOne"
			on("toPageOne").to "pageOne"
			on("toPageThree").to "pageThree"
			on("toPageFour").to "pageFour"
			on("toPageFive") {
				flow.page = 5
			}.to "save"
		}

		// second wizard page
		pageThree {
			render(view: "_page_three")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_page_three.gsp".grom()

				flow.page = 3
				success()
			}
			on("next").to "pageFour"
			on("previous").to "pageTwo"
			on("toPageOne").to "pageOne"
			on("toPageTwo").to "pageTwo"
			on("toPageFour").to "pageFour"
			on("toPageFive") {
				flow.page = 5
			}.to "save"
		}

		// second wizard page
		pageFour {
			render(view: "_page_four")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".rendering the partial pages/_page_four.gsp".grom()

				flow.page = 4
				success()
			}
			on("next") {
				// put some logic in here
				flow.page = 5
			}.to "save"
			on("previous").to "pageThree"
			on("toPageOne").to "pageOne"
			on("toPageTwo").to "pageTwo"
			on("toPageThree").to "pageThree"
			on("toPageFive") {
				flow.page = 5
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
			on("toPageOne").to "pageOne"
			on("toPageTwo").to "pageTwo"
			on("toPageThree").to "pageThree"
			on("toPageFour").to "pageFour"
			on("toPageFive").to "save"

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
}
