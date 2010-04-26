package dbnp.studycapturing

import dbnp.data.*

/**
 * Wizard Controler
 *
 * The wizard controller handles the handeling of pages and data flow
 * through the study capturing wizard.
 *
 * TODO: refactor the 'handle*' methods to work as subflows instead
 * 		 of methods outside of the flow
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
				//[title: 'Templates'],			// templates
				[title: 'Start'],				// load or create a study
				[title: 'Study'],				// study
				[title: 'Subjects'],			// subjects
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
			on("next").to "start"
		}

		// create or modify a study
		start {
			render(view: "_start")
			onRender {
				flow.page = 1
			}
			on("next").to "study"
			on("modify").to "modify"
		}

		// load a study to modify
		modify {
			render(view: "_modify")
			onRender {
				flow.page = 1
				flash.cancel = true
			}
			on("cancel") {
				flow.study = null
			}.to "start"
			on("next") {
				// TODO: loading a study is not yet implemented
				//       create a error stating this feature is
				//       not yet implemented
				flash.errors = [:]
				this.appendErrorMap(
					['study': 'Loading a study and modifying it has not yet been implemented. Please press \'cancel\' to go back to the initial page...'],
					flash.errors
				)
			}.to "modify"
		}

		// render and handle the study page
		// TODO: make sure both template as well as logic will
		//       handle Study templates as well!!!
		study {
			render(view: "_study")
			onRender {
				flow.page = 2
			}
			on("refresh") {
				flash.values = params

				// handle study data
				this.handleStudy(flow, flash, params)

				// remove errors as we don't want any warnings now
				flash.errors = [:]				
			}.to "study"
			on("switchTemplate") {
				flash.values = params

				// handle study data
				this.handleStudy(flow, flash, params)

				// remove errors as we don't want any warnings now
				flash.errors = [:]
			}.to "study"
			on("previous") {
				flash.errors = [:]

				// handle the study
				this.handleStudy(flow, flash, params)

				// reset errors
				flash.errors = [:]

				success()
			}.to "start"
			on("next") {
				flash.errors = [:]

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
					flow.subjectTemplates = [:]
				}
			}
			on("refresh") {
				flash.values = params
			}.to "subjects"
			on("add") {
				flash.values = params
				def speciesTerm = Term.findByName(params.species);
				def subjectTemplateName = params.get('template');
				def subjectTemplate = Template.findByName(subjectTemplateName);

				// add this subject template to the subject template array
				if (!flow.subjectTemplates[ subjectTemplateName ]) {
					flow.subjectTemplates[ subjectTemplateName ] = [
						name: subjectTemplateName,
						template: subjectTemplate,
						subjects: []
					]
				}

				// add x subjects of species y
				(params.addNumber as int).times {
					def increment = flow.subjects.size()
					def subject = new Subject(
						name: 'Subject ' + (increment + 1),
						species: speciesTerm,
						template: subjectTemplate
					)

					// instantiate a new Subject
					flow.subjects[ increment ] = subject

					// and remember the subject id with the template
					def subjectsSize = flow.subjectTemplates[ subjectTemplateName ]['subjects'].size()
					flow.subjectTemplates[ subjectTemplateName ]['subjects'][ subjectsSize ] = increment
				}
			}.to "subjects"
			on("next") {
				flash.errors = [:]

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
			}.to "events"
			on("delete") {
				flash.errors = [:]
				def delete = params.get('do') as int;

				// remove subject
				if (flow.subjects[ delete ] && flow.subjects[ delete ] instanceof Subject) {
					flow.subjectTemplates.each() { templateName, templateData ->
						templateData.subjects.remove(delete)
					}

					flow.subjects.remove( delete )
				}
			}.to "subjects"
			on("previous") {
				flash.errors = [:]

				// handle form data
				if (!this.handleSubjects(flow, flash, params)) {
					error()
				} else {
					success()
				}
			}.to "study"
		}

		// render events page
		events {
			render(view: "_events")
			onRender {
				flow.page = 4

				if (!flow.events) {
					flow.events = []
				}

				if (!flow.eventGroups) {
					flow.eventGroups = []
					flow.eventGroups[0] = new EventGroup(name: 'Group 1')	// 1 group by default
				}
println flow.events
			}
			on("add") {
				def startTime	= (params.get('startTime')) ? params.startTime = new Date().parse("d/M/yyyy HH:mm", params.get('startTime').toString()) : null
				def endTime		= (params.get('endTime')) ? new Date().parse("d/M/yyyy HH:mm", params.get('endTime').toString()) : null
				def template	= params.get('template')

				// handle template
				if (template instanceof String && template.size() > 0) {
					template = Template.findByName(template)
				} else if (!template instanceof Template) {
					template = null
				}

				// handle data
				if (template && startTime && endTime) {
					// add an event instance
					def event = new Event(
						template	: template,
						startTime	: startTime,
						endTime		: endTime
					)

					// validate event
					if (event.validate()) {
						// add event to event list
						flow.events[ flow.events.size() ] = event
						success()
					} else {
						// validation failed, feedback errors
						flash.errors = [:]
						flash.values = params
						this.appendErrors(event, flash.errors)
						error()
					}
				} else {
					// validation failed, feedback errors
					flash.errors	= [:]
					flash.values	= params

					if (!template)	this.appendErrorMap(['template': 'You need to select an event template'], flash.errors)
					if (!startTime) this.appendErrorMap(['startTime': 'You need to define the start time of your study event'], flash.errors)
					if (!endTime)	this.appendErrorMap(['endTime': 'You need to define the end time of your study event'], flash.errors)
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
			}.to "subjects"
			on("next") {
				flash.values = params
				flash.errors = [:]

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
			}.to "confirm"
		}

		confirm {
			render(view: "_confirmation")
			onRender {
				flow.page = 5
			}
			on("toStudy").to "study"
			on("toSubjects").to "subjects"
			on("toEvents").to "events"
			on("previous").to "events"
			on("next").to "save"
		}

		// store all study data
		save {
			action {
				println "saving..."
				flash.errors = [:]

				// start transaction
				def transaction = sessionFactory.getCurrentSession().beginTransaction()

				// persist data to the database
				try {
					// save EventDescriptions
					flow.eventDescriptions.each() {
						if (!it.save(flush:true)) {
							this.appendErrors(it, flash.errors)
							throw new Exception('error saving eventDescription')
						}
						println "saved eventdescription "+it
					}

					// TODO: eventDescriptions that are not linked to an event are currently
					//		 stored but end up in a black hole. We should either decide to
					//		 NOT store these eventDescriptions, or add "hasmany eventDescriptions"
					//		 to Study domain class

					// save events
					flow.events.each() {
						if (!it.save(flush:true)) {
							this.appendErrors(it, flash.errors)
							throw new Exception('error saving event')
						}
						println "saved event "+it

						// add to study
						if (it instanceof SamplingEvent) {
							flow.study.addToSamplingEvents(it)
						} else {
							flow.study.addToEvents(it)
						}
					}

					// save eventGroups
					flow.eventGroups.each() {
						if (!it.save(flush:true)) {
							this.appendErrors(it, flash.errors)
							throw new Exception('error saving eventGroup')
						}
						println "saved eventGroup "+it

						// add to study
						flow.study.addToEventGroups(it)
					}
					
					// save subjects
					flow.subjects.each() {
						if (!it.save(flush:true)) {
							this.appendErrors(it, flash.errors)
							throw new Exception('error saving subject')
						}
						println "saved subject "+it

						// add this subject to the study
						flow.study.addToSubjects(it)
					}

					// save study
					if (!flow.study.save(flush:true)) {
						this.appendErrors(flow.study, flash.errors)
						throw new Exception('error saving study')
					}
					println "saved study "+flow.study+" (id: "+flow.study.id+")"

					// commit transaction
					println "commit"
					transaction.commit()
					success()
				} catch (Exception e) {
					// rollback
					this.appendErrorMap(['exception': e.toString() + ', see log for stacktrace' ], flash.errors)

					// stacktrace in flash scope
					flash.debug = e.getStackTrace()

					println "rollback"
					transaction.rollback()
					error()
				}
			}
			on("error").to "error"
			on(Exception).to "error"
			on("success").to "done"
		}

		// error storing data
		error {
			render(view: "_error")
			onRender {
				flow.page = 6
			}
			on("next").to "save"
			on("previous").to "events"
		}

		// render page three
		done {
			render(view: "_done")
			onRender {
				flow.page = 7
			}
			on("previous") {
				// TODO
			}.to "confirm"
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
		def template = params.remove('template')
		if (template instanceof String && template.size() > 0) {
			params.template = Template.findByName(template)
		} else if (template instanceof Template) {
			params.template = template
		}

		// update study instance with parameters
		params.each() { key, value ->
			if (flow.study.hasProperty(key)) {
				flow.study.setProperty(key, value);
			}
		}

		// walk through template fields
		if (params.template) {
			params.template.fields.each() { field ->
				flow.study.setFieldValue(field.name, params.get(field.escapedName()))
			}
		}

		// validate study
		if (flow.study.validate()) {
			return true
		} else {
			// validation failed, feedback errors
			flash.errors = [:]
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
		def names = [:]
		def errors = false
		def id = 0

		flow.eventDescriptions.each() {
			it.name = params.get('eventDescription_' + id + '_name')
			it.description = params.get('eventDescription_' + id + '_description')
			it.protocol = Protocol.findByName(params.get('eventDescription_' + id + '_protocol'))
			//it.classification = Term.findByName(params.get('eventDescription_' + id + '_classification'))
			it.isSamplingEvent = (params.containsKey('eventDescription_' + id + '_isSamplingEvent'))

			// validate eventDescription
			if (!it.validate()) {
				errors = true
				this.appendErrors(it, flash.errors, 'eventDescription_' + id + '_')
			}

			id++
		}

		return !errors
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
	 * re-usable code for handling subject form data in a web flow
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def handleSubjects(flow, flash, params) {
		def names = [:];
		def errors = false;
		def id = 0;

		// iterate through subject templates
		flow.subjectTemplates.each() {
			def subjectTemplate = it.getValue().template
			def templateFields	= subjectTemplate.fields

			// iterate through subjects
			it.getValue().subjects.each() { subjectId ->
				flow.subjects[ subjectId ].name = params.get('subject_' + subjectId + '_name')
				flow.subjects[ subjectId ].species = Term.findByName(params.get('subject_' + subjectId + '_species'))

				// remember name and check for duplicates
				if (!names[ flow.subjects[ subjectId ].name ]) {
					names[ flow.subjects[ subjectId ].name ] = [count: 1, first: 'subject_' + subjectId + '_name', firstId: subjectId]
				} else {
					// duplicate name found, set error flag
					names[ flow.subjects[ subjectId ].name ]['count']++

					// second occurence?
					if (names[ flow.subjects[ subjectId ].name ]['count'] == 2) {
						// yeah, also mention the first
						// occurrence in the error message
						this.appendErrorMap(name: 'The subject name needs to be unique!', flash.errors, 'subject_' + names[ flow.subjects[ subjectId ].name ]['firstId'] + '_')
					}

					// add to error map
					this.appendErrorMap([name: 'The subject name needs to be unique!'], flash.errors, 'subject_' + subjectId + '_')
					errors = true
				}

				// iterate through template fields
				templateFields.each() { subjectField ->
					flow.subjects[ subjectId ].setFieldValue(
						subjectField.name,
						params.get( 'subject_' + subjectId + '_' + subjectField.escapedName() )
					)
				}

				// validate subject
				if (!flow.subjects[ subjectId ].validate()) {
					errors = true
					this.appendErrors(flow.subjects[ subjectId ], flash.errors, 'subject_' + subjectId + '_')
				}
			}
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
		def errors = [:]
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