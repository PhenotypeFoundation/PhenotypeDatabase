package dbnp.studycapturing

import dbnp.studycapturing.*
import dbnp.data.*
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
				[title: 'Study'],		// study
				[title: 'Subjects'],	// subjects
				[title: 'Form elements demo page']
			]

		}

		// render the main wizard page which immediately
		// triggers the 'next' action (hence, the main
		// page dynamically renders the study template
		// and makes the flow jump to the study logic)
		mainPage {
			render(view: "/wizard/index")
			onRender {
				flow.page = 1
			}
			on("next").to "study"
		}

		// render the study page and handle study logic
		study {
			render(view: "_study")
			onRender {
				println "render page one"
				flow.page = 1
			}
			on("next") {
				// create date instance from date string?
				// @see WizardTagLibrary::dateElement{...}
				if (params.get('startDate')) {
					params.startDate = new Date().parse("d/M/yyyy", params.get('startDate').toString())
				}

				// create a study instance
				flow.study = new Study(params)

				// validate study
				if (flow.study.validate()) {
					println "ok"
					success()
				} else {
					// validation failed, feedback errors
					flash.errors = new LinkedHashMap()
					this.appendErrors(flow.study,flash.errors)
					error()
				}
			}.to "subjects"
		}

		// render page two
		subjects {
			render(view: "_subjects")
			onRender {
				flow.page = 2

				if (!flow.subjects) {
					flow.subjects = new LinkedHashMap()
				}
			}
			on ("add") {
				def speciesTerm = Term.findByName(params.addSpecies)
				
				// add x subject of species y
				(params.addNumber as int).times {
					def increment = flow.subjects.size()
					flow.subjects[ increment ] = new Subject(
						name: 'Subject ' + (increment+1),
						species: speciesTerm
					)
				}
			}.to "subjects"
			on("next") {
				// got one or more subjects?
				if (flow.subjects.size() < 1) {
					error()
				}
			}.to "pageThree"
			on("previous") {
				// handle data?
				// go to study page
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
			}.to "subjects"
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
