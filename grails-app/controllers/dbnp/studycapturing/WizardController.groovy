package dbnp.studycapturing

import dbnp.studycapturing.*
import grails.converters.*

/**
 * Wizard Controler
 *
 * The wizard controller handles the handeling of pages and data flow
 * through the study capturing wizard.
 *
 * @author Jeroen Wesbeek
 * @since 20100107
 * @package studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class WizardController {
	/**
	 * index method, redirect to the webflow
	 * @void
	 */
	def index = {
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
	 * @see http://grails.org/WebFlow
	 * @void
	 */
	def pagesFlow = {
		// start the flow
		onStart {
			println "wizard started"

			// define flow variables
			flow.page = 0
			flow.pages = [
				[title: 'Study'],
				[title: 'Twoooo'],
				[title: 'Trois']
			]

		}

		// render the main wizard page
		mainPage {
			render(view: "/wizard/index")
			onRender {
				flow.page = 1
			}
			on("next").to "study"
		}

		study {
			render(view: "_study")
			onRender {
				println "render page one"
				flow.page = 1
			}
			on("next") {
				// create a study instance
				println params
				flow.study = new Study(params)

				// validate study
				if (flow.study.validate()) {
					println "study validates"
				} else {
					// validation failed, feedback errors
					flash.errors = new LinkedHashMap()
					this.appendErrors(flow.study,flash.errors)
					println "errorrrs"
					println flash.errors
					error()
				}
			}.to "pageTwo"
		}

		// render page two
		pageTwo {
			render(view: "_two")
			onRender {
				println "render page two"
				flow.page = 2
			}
			on("next") {
				println "next page!"
			}.to "pageThree"
			on("previous") {
				println "previous page!"
			}.to "study"
		}

		// render page three
		pageThree {
			render(view: "_three")
			onRender {
				println "render page three"
				flow.page = 3
			}
			on("previous") {
				println "previous page!"
			}.to "pageTwo"
		}
	}

	/**
	 * transform domain class validation errors into a human readable
	 * linked hash map
	 * @param object validated domain class
	 * @returns object  linkedHashMap
	 */
	def getHumanReadableErrors(object) {
		def errors = new LinkedHashMap()

		object.errors.getAllErrors().each() {
			errors[it.getArguments()[0]] = it.getDefaultMessage()
		}

		return errors
	}

	/**
	 * append errors of a particular object to a map
	 * @param object
	 * @param map linkedHashMap
	 * @void
	 */
	def appendErrors(object, map) {
		this.appendErrorMap(this.getHumanReadableErrors(object), map)
	}

	/**
	 * append errors of one map to another map
	 * @param map linkedHashMap
	 * @param map linkedHashMap
	 * @void
	 */
	def appendErrorMap(map, mapToExtend) {
		map.each() {key, value ->
			mapToExtend[key] = value
		}
	}

}
