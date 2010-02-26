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
				[title: 'Templates'],			// templates
				[title: 'Study'],				// study
				[title: 'Subjects'],			// subjects
				[title: 'Event Descriptions'],	// event descriptions
				[title: 'Events'],				// events and event grouping
				[title: 'Confirmation'],		// confirmation page
				[title: 'Done']					// finish page
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
			on("next").to "templates"
		}

		// select the templates to use for this study
		templates {
			render(view: "_templates")
			onRender {
				flow.page = 1
			}
			on("next") {
				// if we don't have a study, instantiate a study with dummy values
				if (!flow.study) {
					flow.study = new Study(
						title: "my study",
						code: "",
						ecCode: "",
						researchQuestion: "",
						description: "",
						startDate: new Date()
					)
				}

				// assign template to study
				flow.study.template = Template.findByName(params.get('template'));

				// validate study
				if (flow.study.validate()) {
					success()
				} else {
					// validation failed, feedback errors
					flash.errors = new LinkedHashMap()
					this.appendErrors(flow.study, flash.errors)
					error()
				}
			}.to "study"
		}

		// render and handle the study page
		study {
			render(view: "_study")
			onRender {
				flow.page = 2
			}
			on("previous") {
				flash.errors = new LinkedHashMap()

				if (this.handleStudy(flow, flash, params)) {
					success()
				} else {
					error()
				}
			}.to "templates"
			on("next") {
				flash.errors = new LinkedHashMap()

				if (this.handleStudy(flow, flash, params)) {
					success()
				} else {
					error()
				}
			}.to "subjects"
		}

		// render and handle subjects page
		subjects {
			render(view: "_subjects")
			onRender {
				flow.page = 3

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

				// check if we have at least one subject
				// and check form data
				if (flow.subjects.size() < 1) {
					// append error map
					this.appendErrorMap(['subjects': 'You need at least to create one subject for your study'], flash.errors)
					error()
				} else if (!this.handleSubjects(flow, flash, params)) {
					error()
				} else {
					success()
				}
			}.to "eventDescriptions"
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

		// render page three
		eventDescriptions {
			render(view: "_eventDescriptions")
			onRender {
				flow.page = 4

				if (!flow.eventDescriptions) {
					flow.eventDescriptions = []
				}
			}
			on("add") {
				// fetch classification by name (as posted by the form)
				params.classification = Term.findByName(params.classification)

				// transform checkbox form value to boolean
				params.isSamplingEvent = (params.containsKey('isSamplingEvent'))

				// instantiate EventDescription with parameters
				def eventDescription = new EventDescription(params)

				// validate
				if (eventDescription.validate()) {
					def increment = flow.eventDescriptions.size()
					flow.eventDescriptions[increment] = eventDescription
					success()
				} else {
					// validation failed, feedback errors
					flash.errors = new LinkedHashMap()
					flash.values = params
					this.appendErrors(eventDescription, flash.errors)
					error()
				}
			}.to "eventDescriptions"
			on("delete") {
				def delete = params.get('do') as int;

				// handle form data
				if (!this.handleEventDescriptions(flow, flash, params)) {
					flash.values = params
					error()
				} else {
					success()
				}

				// remove eventDescription
				if (flow.eventDescriptions[ delete ] && flow.eventDescriptions[ delete ] instanceof EventDescription) {
					// remove all events based on this eventDescription
					for ( i in flow.events.size()..0 ) {
						if (flow.events[ i ] && flow.events[ i ].eventDescription == flow.eventDescriptions[ delete ]) {
							flow.events.remove(i)
						}
					}

					flow.eventDescriptions.remove(delete)
				}
			}.to "eventDescriptions"
			on("previous") {
				flash.errors = new LinkedHashMap()

				// handle form data
				if (!this.handleEventDescriptions(flow, flash, params)) {
					flash.values = params
					error()
				} else {
					success()
				}
			}.to "subjects"
			on("next") {
				flash.errors = new LinkedHashMap()

				// check if we have at least one subject
				// and check form data
				if (flow.eventDescriptions.size() < 1) {
					// append error map
					flash.values = params
					this.appendErrorMap(['eventDescriptions': 'You need at least to create one eventDescription for your study'], flash.errors)
					error()
				} else if (!this.handleEventDescriptions(flow, flash, params)) {
					flash.values = params
					error()
				} else {
					success()
				}
			}.to "events"
		}

		// render events page
		events {
			render(view: "_events")
			onRender {
				flow.page = 5

				if (!flow.events) {
					flow.events = []
				}

				if (!flow.eventGroups) {
					flow.eventGroups = []
					flow.eventGroups[0] = new EventGroup(name: 'Group 1')	// 1 group by default
				}
			}
			on("add") {
				// create date instances from date string?
				// @see WizardTagLibrary::timeElement{...}
				if (params.get('startTime')) {
					params.startTime = new Date().parse("d/M/yyyy HH:mm", params.get('startTime').toString())
				}
				if (params.get('endTime')) {
					params.get('endTime').toString()
					params.endTime = new Date().parse("d/M/yyyy HH:mm", params.get('endTime').toString())
				}

				// get eventDescription instance by name
				params.eventDescription = this.getObjectByName(params.get('eventDescription'), flow.eventDescriptions)

				// instantiate Event with parameters
				def event = new Event(params)

				// handle event groupings
				this.handleEventGrouping(flow, flash, params)

				// validate event
				if (event.validate()) {
					def increment = flow.events.size()
					flow.events[increment] = event
					success()
				} else {
					// validation failed, feedback errors
					flash.errors = new LinkedHashMap()
					flash.values = params
					this.appendErrors(event, flash.errors)

					flash.startTime = params.startTime
					flash.endTime = params.endTime
					flash.eventDescription = params.eventDescription

					error()
				}
			}.to "events"
			on("deleteEvent") {
				flash.values = params
				def delete = params.get('do') as int;

				// handle event groupings
				this.handleEventGrouping(flow, flash, params)

				// remove event
				if (flow.events[ delete ] && flow.events[ delete ] instanceof Event) {
					flow.events.remove(delete)
				}
			}.to "events"
			on("addEventGroup") {
				flash.values = params
				
				// handle event groupings
				this.handleEventGrouping(flow, flash, params)

				def increment = flow.eventGroups.size()
				def groupName = "Group " + (increment + 1)

				// check if group name exists
				def nameExists = true
				def u = 0

				// make sure a unique name is generated
				while (nameExists) {
					u++
					def count = 0
					
					flow.eventGroups.each() {
						if (it.name == groupName) {
							groupName = "Group " + (increment + 1) + "," + u
						} else {
							count++
						}
					}

					nameExists = !(count == flow.eventGroups.size())
				}

				flow.eventGroups[increment] = new EventGroup(name: groupName)
			}.to "events"
			on("deleteEventGroup") {
				flash.values = params
				
				def delete = params.get('do') as int;

				// handle event groupings
				this.handleEventGrouping(flow, flash, params)

				// remove the group with this specific id
				if (flow.eventGroups[delete] && flow.eventGroups[delete] instanceof EventGroup) {
					// remove this eventGroup
					flow.eventGroups.remove(delete)
				}
			}.to "events"
			on("previous") {
				// handle event groupings
				this.handleEventGrouping(flow, flash, params)
			}.to "eventDescriptions"
			on("next") {
				flash.values = params
				
				flash.errors = new LinkedHashMap()

				// handle event groupings
				this.handleEventGrouping(flow, flash, params)

				// check if we have at least one subject
				// and check form data
				if (flow.events.size() < 1) {
					// append error map
					flash.values = params
					this.appendErrorMap(['events': 'You need at least to create one event for your study'], flash.errors)
					error()
				}
			}.to "events"
		}

		confirm {
			render(view: "_confirmation")
			onRender {
				flow.page = 6
			}
			on("previous") {
				// do nothing
			}.to "events"
			on("next") {
				// store everything in the database!
				success()
			}.to "confirm"
		}

		// render page three
		done {
			render(view: "_done")
			onRender {
				flow.page = 6
			}
			on("previous") {
				// TODO
			}.to "confirm"
		}
	}

	/**
	 * re-usable code for handling event grouping in a web flow
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def handleEventGrouping(flow, flash, params) {
		// walk through eventGroups
		def g = 0
		flow.eventGroups.each() {
			def e = 0
			def eventGroup = it

			// reset events
			eventGroup.events = new HashSet()

			// walk through events
			flow.events.each() {
				if (params.get('event_' + e + '_group_' + g) == 'on') {
					eventGroup.addToEvents(it)
				}
				e++
			}
			g++
		}
	}

	/**
	 * re-usable code for handling study form data in a web flow
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def handleStudy(flow, flash, params) {
		// create study instance if we have none
		if (!flow.study) flow.study = new Study();

		// create date instance from date string?
		// @see WizardTagLibrary::dateElement{...}
		if (params.get('startDate')) {
			params.startDate = new Date().parse("d/M/yyyy", params.get('startDate').toString())
		} else {
			params.remove('startDate')
		}

		// if a template is selected, get template instance
		if (params.get('template')) {
			params.template = Template.findByName(params.get('template'))
		}

		// update study instance with parameters
		params.each() {key, value ->
			if (flow.study.hasProperty(key)) {
				flow.study.setProperty(key, value);
			}
		}

		// validate study
		if (flow.study.validate()) {
			return true
		} else {
			// validation failed, feedback errors
			flash.errors = new LinkedHashMap()
			this.appendErrors(flow.study, flash.errors)
			return false
		}
	}

	/**
	 * re-usable code for handling eventDescription form data in a web flow
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def handleEventDescriptions(flow, flash, params) {
		def names = new LinkedHashMap()
		def errors = false
		def id = 0

		flow.eventDescriptions.each() {
			it.name = params.get('eventDescription_' + id + '_name')
			it.description = params.get('eventDescription_' + id + '_description')
			it.classification = Term.findByName(params.get('eventDescription_' + id + '_classification'))
			it.isSamplingEvent = (params.containsKey('eventDescription_' + id + '_isSamplingEvent'))

			// validate eventDescription
			if (!it.validate()) {
				errors = true
				this.appendErrors(it, flash.errors, 'eventDescription_' + id + '_')
			}

			id++
		}

		return !(errors)
	}

	/**
	 * re-usable code for handling subject form data in a web flow
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def handleSubjects(flow, flash, params) {
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
			if (!names[it.name]) {
				names[it.name] = [count: 1, first: 'subject_' + id + '_name', firstId: id]
			} else {
				// duplicate name found, set error flag
				names[it.name]['count']++

				// second occurence?
				if (names[it.name]['count'] == 2) {
					// yeah, also mention the first
					// occurrence in the error message
					this.appendErrorMap(name: 'The subject name needs to be unique!', flash.errors, 'subject_' + names[it.name]['firstId'] + '_')
				}

				// add to error map
				this.appendErrorMap([name: 'The subject name needs to be unique!'], flash.errors, 'subject_' + id + '_')
				errors = true
			}

			// clear lists
			def stringList = new LinkedHashMap();
			def intList = new LinkedHashMap();
			def floatList = new LinkedHashMap();
			def termList = new LinkedHashMap();

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
							stringList[it.name] = value
							break;
						case 'INTEGER':
							intList[it.name] = value
							break;
						case 'FLOAT':
							floatList[it.name] = value
							break;
						default:
							// unsupported type?
							throw new NoSuchFieldException("Field type ${it.type} not recognized")
							break;
					}
				}
			}

			// set field data
			it.templateStringFields = stringList
			it.templateIntegerFields = intList
			it.templateFloatFields = floatList
			it.templateTermFields = termList

			// validate subject
			if (!it.validate()) {
				errors = true
				this.appendErrors(it, flash.errors)
			}

			id++;
		}

		return !errors
	}

	/**
	 * return the object from a map of objects by searching for a name
	 * @param String name
	 * @param Map map of objects
	 * @return Object
	 */
	def getObjectByName(name, map) {
		def result = null
		map.each() {
			if (it.name == name) {
				result = it
			}
		}

		return result
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

	def appendErrors(object, map, prepend) {
		this.appendErrorMap(this.getHumanReadableErrors(object), map, prepend)
	}

	/**
	 * append errors of one map to another map
	 * @param map linkedHashMap
	 * @param map linkedHashMap
	 * @void
	 */
	def appendErrorMap(map, mapToExtend) {
		map.each() {key, value ->
			mapToExtend[key] = ['key': key, 'value': value, 'dynamic': false]
		}
	}

	def appendErrorMap(map, mapToExtend, prepend) {
		map.each() {key, value ->
			mapToExtend[prepend + key] = ['key': key, 'value': value, 'dynamic': true]
		}
	}
}