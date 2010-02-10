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
			// define flow variables
			flow.page = 0
			flow.pages = [
				[title: 'Study'],		// study
				[title: 'Subjects'],	// subjects
				[title: 'Groups'],		// groups
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

		// render and handle the study page
		study {
			render(view: "_study")
			onRender {
				flow.page = 1
			}
			on("next") {
				// create date instance from date string?
				// @see WizardTagLibrary::dateElement{...}
				if (params.get('startDate')) {
					params.startDate = new Date().parse("d/M/yyyy", params.get('startDate').toString())
				}

				// if a template is selected, get template instance
				if (params.get('template')) {
					params.template = Template.findByName(params.get('template'))
				}

				// create a study instance
				flow.study = new Study(params)

				// validate study
				if (flow.study.validate()) {
					success()
				} else {
					// validation failed, feedback errors
					flash.errors = new LinkedHashMap()
					this.appendErrors(flow.study, flash.errors)
					error()
				}
			}.to "subjects"
		}

		// render and handle subjects page
		subjects {
			render(view: "_subjects")
			onRender {
				flow.page = 2

				if (!flow.subjects) {
					flow.subjects = []
				}
			}
			on("add") {
				// fetch species by name (as posted by the form)
				def speciesTerm = Term.findByName(params.addSpecies)

				// add x subject of species y
				(params.addNumber as int).times {
					def increment = flow.subjects.size()
					flow.subjects[increment] = new Subject(
						name: 'Subject ' + (increment + 1),
						species: speciesTerm,
						template: flow.study.template
					)
				}
			}.to "subjects"
			on("next") {
				flash.errors = new LinkedHashMap()

				// handle form data
				if (!this.handleSubjects(flow, flash, params)) {
					error()
				} else {
					success()
				}
			}.to "groups"
			on("previous") {
				flash.errors = new LinkedHashMap()

				// handle form data
				if (!this.handleSubjects(flow, flash, params)) {
					error()
				} else {
					success()
				}
			}.to "study"
		}

		// render and handle group page
		groups {
			render(view: "_groups")
			onRender {
				flow.page = 3

				if (!flow.groups) {
					flow.groups = []
				}
			}
			on("add") {
				def increment = flow.groups.size()
				flow.groups[increment] = new SubjectGroup(params)
			}.to "groups"
			on("next") {
				// TODO
			}.to "groups"
			on("previous") {
				// TODO
			}.to "subjects"
		}

		// render page three
		demo {
			render(view: "_three")
			onRender {
				flow.page = 4
			}
			on("previous") {
				// TODO
			}.to "groups"
		}
	}

	/**
	 * re-usable code for handling subject form data in a web flow
	 * @param Map	LocalAttributeMap (the flow scope)
	 * @param Map	localAttributeMap (the flash scope)
	 * @param Map	GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def handleSubjects(flow, flash, params) {
		if (flow.subjects.size() < 1) {
			return false
		} else {
			def names = new LinkedHashMap();
			def errors = false;
			def id = 0;

			// iterate through subjects
			flow.subjects.each() {
				// store subject properties
				def name = params.get('subject_' + id + '_name')
				it.name = params.get('subject_' + id + '_name')
				it.species = Term.findByName(params.get('subject_' + id + '_species'))

				// remember name and check for duplicates
				if (!names[ it.name ]) {
					names[ it.name ] = [count: 1, first: 'subject_' + id + '_name']	
				} else {
					// duplicate name found, set error flag
					names[ it.name ]['count']++

					// second occurence?
					if (names[ it.name ]['count'] == 2) {
						// yeah, also mention the first
						// occurrence in the error message
						this.appendErrorMap([[names[ it.name ]['first']]: 'The subject name needs to be unique!'], flash.errors)
					}

					// add to error map
					this.appendErrorMap([['subject_' + id + '_name']: 'The subject name needs to be unique!'], flash.errors)
					errors = true
				}

				// clear lists
				def stringList	= new LinkedHashMap();
				def intList		= new LinkedHashMap();
				def floatList	= new LinkedHashMap();
				def termList	= new LinkedHashMap();

				// get all template fields
				flow.study.template.subjectFields.each() {
					// valid type?
					if (!it.type) throw new NoSuchFieldException("Field name ${fieldName} not recognized")

					// get value
					def value = params.get('subject_' + id + '_' + it.name);
					if (value) {
						// add to template parameters
						switch (it.type) {
							case 'STRINGLIST':
								stringList[it.name]	= value
								break;
							case 'INTEGER':
								intList[it.name]	= value
								break;
							case 'FLOAT':
								floatList[it.name]	= value
								break;
							default:
								// unsupported type?
								throw new NoSuchFieldException("Field type ${it.type} not recognized")
								break;
						}
					}
				}

				// set field data
				it.templateStringFields		= stringList
				it.templateIntegerFields	= intList
				it.templateFloatFields		= floatList
				it.templateTermFields		= termList

				// validate subject
				if (!it.validate()) {
					errors = true
					println id + ' :: ' + it.errors.getAllErrors()
					this.appendErrors(it, flash.errors)
				}

				id++;
			}

			return !errors
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