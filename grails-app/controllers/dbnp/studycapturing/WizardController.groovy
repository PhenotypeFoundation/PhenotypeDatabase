package dbnp.studycapturing

import dbnp.data.*

// Grails convertors is imported in order to create JSON objects
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
		def jump = [:]

		// allow quickjumps to:
		//	edit a study	: /wizard?jump=edit&id=1
		//	create a study	: /wizard?jump=create
		if (params.get('jump')) {
			switch (params.get('jump')) {
				case 'create':
					jump = [
					    action: 'create'
					]
					break
				case 'edit':
					jump = [
					    action	: 'edit',
						id		: params.get('id')
					]
					break
				default:
					break
			}
		}

		// store in session
		session.jump = jump

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
				//[title: 'Event Groups'],		// groups
				[title: 'Samples'],				// samples
				[title: 'Assays'],				// assays
				//[title: 'Assay Groups'],		// assays
				[title: 'Confirmation'],		// confirmation page
				[title: 'Done']					// finish page
			]
			flow.jump = session.jump
			success()
		}

		// render the main wizard page which immediately
		// triggers the 'next' action (hence, the main
		// page dynamically renders the study template
		// and makes the flow jump to the study logic)
		mainPage {
			render(view: "/wizard/index")
			onRender {
				flow.page = 1
				success()
			}
			on("next").to "handleJump"
		}

		// handle the jump parameter
		//
		// I came to get down [2x]
		// So get out your seats and jump around
		// Jump around [3x]
		// Jump up Jump up and get down
		// Jump [18x]
		handleJump {
			action {
				if (flow.jump && flow.jump.action == 'edit' && flow.jump.id) {
					// load study
					if (this.loadStudy(flow, flash, [studyid:flow.jump.id])) {
						toStudyPage()
					} else {
						toStartPage()
					}
				} else if (flow.jump && flow.jump.action == 'create') {
					toStudyPage()
				} else {
					toStartPage()
				}
			}
			on("toStartPage").to "start"
			on("toStudyPage").to "study"
		}

		// create or modify a study
		start {
			render(view: "_start")
			onRender {
				flow.page = 1
				success()
			}
			on("next") {
				// clean the flow scope
				flow.remove('study')

				// set 'quicksave' variable to false
				flow.quickSave = false
			}.to "study"
			on("modify").to "modify"
			on("import").to "redirectToImport"
		}

		// redirect to the import wizard
		redirectToImport {
			render(view: "_redirect")
			onRender {
				flash.uri = "/importer/index"
			}
			on("next").to "start"
		}

		// load a study to modify
		modify {
			render(view: "_modify")
			onRender {
				flow.page = 1
				flash.cancel = true
				success()
			}
			on("cancel") {
				flow.remove('study')

				success()
			}.to "start"
			on("next") {
				// load study
				if (this.loadStudy(flow, flash, params)) {
					success()
				} else {
					error()
				}
			}.to "study"
		}

		// render and handle the study page
		study {
			render(view: "_study")
			onRender {
				flow.page = 2
				success()
			}
			on("refresh") {
				// handle form data
				studyPage(flow, flash, params)

				// force refresh of the template
				if (flow.study.template && flow.study.template instanceof Template) {
					flow.study.template.refresh()
				}

				// reset errors
				flash.errors = [:]
				success()
			}.to "study"
            on("switchTemplate") {
				// handle form data
				studyPage(flow, flash, params)

				// reset errors
				flash.errors = [:]
				success()
			}.to "study"
			on("previous") {
				// handle form data
				studyPage(flow, flash, params)

				// reset errors
				flash.errors = [:]
				success()
			}.to "start"
			on("next") {
				studyPage(flow, flash, params) ? success() : error()
			}.to "subjects"
			on("quickSave") {
				studyPage(flow, flash, params) ? success() : error()
			}.to "waitForSave"
		}

		// render and handle subjects page
		subjects {
			render(view: "_subjects")
			onRender {
				flow.page = 3

				if (!flash.values || !flash.values.addNumber) flash.values = [addNumber:1]

				success()
			}
			on("refresh") {
				// remember the params in the flash scope
				flash.values = params

				// refresh templates
				if (flow.study.subjects) {
					flow.study.giveSubjectTemplates().each() {
						it.refresh()
					}
				}

				success()
			}.to "subjects"
			on("add") {
				// handle form data
				addSubjects(flow, flash, params) ? success() : error()
			}.to "subjects"
			on("delete") {
				// handle form data
				subjectPage(flow, flash, params)

				// reset errors
				flash.errors = [:]

				// remove subject
				def subjectToRemove = flow.study.subjects.find { it.identifier == (params.get('do') as int) }
				if (subjectToRemove) {
					flow.study.deleteSubject( subjectToRemove )
				}
			}.to "subjects"
			on("previous") {
				// handle form data
				subjectPage(flow, flash, params)

				// reset errors
				flash.errors = [:]
				success()
			}.to "study"
			on("next") {
				// handle form data
				subjectPage(flow, flash, params) ? success() : error()
			}.to "events"
			on("quickSave") {				
				// handle form data
				subjectPage(flow, flash, params) ? success() : error()
			}.to "waitForSave"
		}

		// render events page
		events {
			render(view: "_events")
			onRender {
				flow.page = 4

				// add initial eventGroup to study
				if (!flow.study.eventGroups?.size()) {
					flow.study.addToEventGroups(
						new EventGroup(name: 'Group 1')
					)
				}

				success()
			}
			on("clear") {
				// remove all events
				(flow.study.events + flow.study.samplingEvents).each() { event ->
					if (event instanceof SamplingEvent) {
						flow.study.deleteSamplingEvent( event )
					} else {
						flow.study.deleteEvent( event )
					}
				}

				success()
			}.to "events"
			on("switchTemplate") {
				// handle form data
				eventPage(flow, flash, params)

				// get template
				def type	= params.get('eventType')
				def template= Template.findByName( params.get( type + 'Template' ) )

				// change template and/or instance?
				if (!flow.event || (flow.event instanceof Event && type == "sample") || (flow.event instanceof SamplingEvent && type == "event")) {
					// create new instance
					flow.event = (type == "event") ? new Event(template: template) : new SamplingEvent(template: template)
				} else {
					flow.event.template = template
				}

				// reset errors
				flash.errors = [:]
				success()

			}.to "events"
			on("refresh") {
				// handle form data
				eventPage(flow, flash, params)

				// refresh templates
				flow.study.giveEventTemplates().each() {
					it.refresh()
				}

				// refresh event template
				if (flow.event?.template) flow.event.template.refresh()

				// reset errors
				flash.errors = [:]
				success()
			}.to "events"
			on("add") {
				// handle form data
				eventPage(flow, flash, params)

				// reset errors
				flash.errors = [:]

				// add event to study
				if (flow.event instanceof SamplingEvent) {
					flow.study.addToSamplingEvents( flow.event )
				} else {
					flow.study.addToEvents( flow.event )
				}

				// validate event
				if (flow.event.validate()) {
					// remove event from the flowscope
					flow.remove('event')
					
					success()
				} else {
					// event does not validate
					// remove from study
					if (flow.event instanceof SamplingEvent) {
						flow.study.removeFromSamplingEvents( flow.event )
					} else {
						flow.study.removeFromEvents( flow.event )
					}

					// append errors
					this.appendErrors(flow.event, flash.errors)
					error()
				}
			}.to "events"
			on("deleteEvent") {
				// handle form data
				eventPage(flow, flash, params)

				// reset errors
				flash.errors = [:]

				// find matching (sampling) event
				def event 			= flow.study.events.find { it.getIdentifier() == (params.get('do') as int) }
				def samplingEvent	= flow.study.samplingEvents.find { it.getIdentifier() == (params.get('do') as int) }

				// perform delete
				if (event) flow.study.deleteEvent( event )
				if (samplingEvent) flow.study.deleteSamplingEvent( samplingEvent )
			}.to "events"
			on("addEventGroup") {
				// handle form data
				eventPage(flow, flash, params)

				// set work variables
				def groupName = 'Group '
				def tempGroupIterator = 1
				def tempGroupName = groupName + tempGroupIterator

				// make sure group name is unique
				if (flow.study.eventGroups) {
					while (flow.study.eventGroups.find { it.name == tempGroupName }) {
						tempGroupIterator++
						tempGroupName = groupName + tempGroupIterator
					}
				}
				groupName = tempGroupName

				// add a new eventGroup
				flow.study.addToEventGroups(
					new EventGroup(
						name	: groupName
					)
				)

				// reset errors
				flash.errors = [:]
				success()
			}.to "events"
			on("deleteEventGroup") {
				// handle form data
				eventPage(flow, flash, params)

				// reset errors
				flash.errors = [:]

				// remove eventGroup
				def eventGroupToRemove = flow.study.eventGroups.find { it.getIdentifier() == (params.get('do') as int) }
				if (eventGroupToRemove) {
					println flow.study.deleteEventGroup( eventGroupToRemove )
				}
			}.to "events"
			on("previous") {
				// handle form data
				eventPage(flow, flash, params)

				// reset errors
				flash.errors = [:]
				success()
			}.to "subjects"
			on("next") {
				// handle form data
				eventPage(flow, flash, params) ? success() : error()
			}.to "eventsNext"
			on("quickSave") {
				// handle form data
				eventPage(flow, flash, params) ? success() : error()
			}.to "waitForSave"
		}

		// decide to show a warning page or not
		eventsNext {
			action {
				def assigned = false

				// check if all sampling events are in an eventGroup
				flow.study.samplingEvents.each() { samplingEvent ->
					// iterate through eventGroups
					flow.study.eventGroups.each() { eventGroup ->
						if ( eventGroup.samplingEvents.find { it.equals(samplingEvent) } ) {
							assigned = true
						}
					}
				}

				if (assigned) {
					toGroupsPage()
				} else {
					toWarningPage()
				}
			}
			on("toWarningPage").to "unassignedSamplingEventWarning"
			on("toGroupsPage").to "groups"
		}

		// warning page for unassigned samplingEvent
		unassignedSamplingEventWarning {
			render(view: "_unassigned_samplingEvent_warning")
			onRender {
				flow.page = 4
				success()
			}
			on("next").to "groups"
			on("previous").to "events"
		}

		// groups page
		groups {
			render(view: "_groups")
			onRender {
				flow.page = 4
				success()
			}
			on("previous") {
				// handle form data
				groupPage(flow, flash, params) ? success() : error()
			}.to "events"
			on("next") {
				// handle form data
				groupPage(flow, flash, params) ? success() : error()
			}.to "samples"
			on("quickSave") {
				// handle form data
				groupPage(flow, flash, params) ? success() : error()
			}.to "waitForSave"
		}

		// sample 'previous' page with warning
		samplePrevious {
			render(view: "_samples_previous_warning")
			onRender {
				flow.page = 5

				// TEMPORARY FIX TO REMOVE ALL SAMPLES AND REGENERATE THEM
				// THEN USER BROWSED BACK
				println ".removing samples from study"

				// remove samples from study
				flow.samples.each() {
					flow.study.removeFromSamples(it.sample)
				}

				// remove samples from flow
				flow.remove('samples')
				// END FIX
			}
			on("next").to "samples"
			on("previous").to "groups"
		}

		// samples page
		samples {
			render(view: "_samples")
			onRender {
				flow.page = 5
				success()
			}
			on("switchTemplate") {
				// handle form data
				samplePage(flow, flash, params)

				// ignore errors
				flash.errors = [:]
				
				succes()
			}.to "samples"
			on("refresh") {
				// handle samples
				samplePage(flow, flash, params)

				// refresh all sample templates
				flow.study.giveSampleTemplates().each() {
					it.refresh()
				}

				// ignore errors
				flash.errors = [:]

				success()
			}.to "samples"
			on("regenerate") {
				// handle samples
				samplePage(flow, flash, params)

				// remove all samples from the study
				flow.study.samples.findAll{true}.each() { sample ->
					flow.study.removeFromSamples(sample)
				}

				// ignore errors
				flash.errors = [:]

				success()
			}.to "samples"
			on("previous") {
				// handle samples
				samplePage(flow, flash, params)

				// ignore errors
				flash.errors = [:]

				success()
			}.to "samplePrevious"
			on("next") {
				// handle form data
				samplePage(flow, flash, params) ? success() : error()
			}.to "assays"
			on("quickSave") {
				// handle form data
				samplePage(flow, flash, params) ? success() : error()
			}.to "waitForSave"
		}

		// assays page
		assays {
			render(view: "_assays")
			onRender {
				flow.page = 6
			}
			on("refresh") {
				// handle form data
				assayPage(flow, flash, params)

				// force refresh of the template
				if (flow.assay && flow.assay.template && flow.assay.template instanceof Template) {
					flow.assay.template.refresh()
				}

				// reset errors
				flash.errors = [:]
				success()
			}.to "assays"
            on("switchTemplate") {
				// handle form data
				assayPage(flow, flash, params)

				// find assay template
				def template = Template.findByName( params.get('template') )

				if (flow.assay) {
					// set template
					flow.assay.template = template
				} else {
					// create a new assay instance
					flow.assay = new Assay(template: template)
				}

				// reset errors
				flash.errors = [:]
				success()
			}.to "assays"
			on("add") {
				// handle form data
				assayPage(flow, flash, params)

				// reset errors
				flash.errors = [:]

				// add assay to study
				flow.study.addToAssays( flow.assay )

				// validate assay
				if (flow.assay.validate()) {
					// remove assay from the flowscope
					flow.remove('assay')
					success()
				} else {
					// assay does not validate
					// remove from study
					flow.study.removeFromAssays( flow.assay )

					// append errors
					this.appendErrors(flow.assay, flash.errors)
					error()
				}
			}.to "assays"
			on("deleteAssay") {
				println params
				
				// handle form data
				assayPage(flow, flash, params)

				// reset errors
				flash.errors = [:]

				// find this assay
				def assay = flow.study.assays.find { it.getIdentifier() == (params.get('do') as int) }

				// perform delete
				if (assay) flow.study.deleteAssay( assay )
			}.to "assays"
			on("previous") {
				// handle form data
				assayPage(flow, flash, params)

				// ignore errors
				flash.errors = [:]

				success()
			}.to "samples"
			on("next") {
				// handle form data
				assayPage(flow, flash, params) ? success() : error()
			}.to "assayNext"
			on("quickSave") {
				// handle form data
				assayPage(flow, flash, params) ? success() : error()
			}.to "waitForSave"
		}

		// assayNext
		assayNext {
			action {
				// have we got samples and assays?
				if (flow.study.assays && flow.study.samples) {
					// yes, go to the group page
					toAssayGroups()
				} else {
					// no need to show the group page as
					// there's nothing to group
					toConfirm()
				}
			}
			on("toAssayGroups").to "assayGroups"
			on("toConfirm").to "confirm"
		}

		// assay grouping page
		assayGroups {
			render(view: "_assay_groups")
			onRender {
				flow.page = 6
			}
			on("previous") {
				// handle form data
				assayGroupPage(flow, flash, params)

				// ignore errors
				flash.errors = [:]

				success()
			}.to "assays"
			on("next") {
				// handle form data
				assayGroupPage(flow, flash, params) ? success() : error()
			}.to "confirm"
			on("quickSave") {
				// handle form data
				assayGroupPage(flow, flash, params) ? success() : error()
			}.to "waitForSave"
		}

		// confirm Previous
		confirmPrevious {
			action {
				// have we got samples and assays?
				if (flow.study.assays && flow.study.samples) {
					// yes, go to the group page
					toAssayGroups()
				} else {
					// no need to show the group page as
					// there's nothing to group
					toAssays()
				}
			}
			on("toAssayGroups").to "assayGroups"
			on("toAssays").to "assays"
		}

		// confirmation
		confirm {
			render(view: "_confirmation")
			onRender {
				flow.page = 7
			}
			on("toStudy").to "study"
			on("toSubjects").to "subjects"
			on("toEvents").to "events"
			on("toGroups").to "groups"
			on("toSamples").to "samples"
			on("toAssays").to "assays"
			on("toAssayGroups").to "assayGroups"
			on("previous").to "confirmPrevious"
			on("next").to "waitForSave"
			on("quickSave").to "waitForSave"
		}

		waitForSave {
			render(view: "_wait")
			onRender {
				flow.page = 8
			}
			on("next").to "save"
		}

		// store all study data
		save {
			action {
				println "saving..."
				flash.errors = [:]

				// persist data to the database
				try {
					// save study
					println ".saving study"
					if (!flow.study.save(flush:true)) {
						this.appendErrors(flow.study, flash.errors)
						throw new Exception('error saving study')
					}
					println ".saved study "+flow.study+" (id: "+flow.study.id+")"

					success()
				} catch (Exception e) {
					// rollback
					this.appendErrorMap(['exception': e.toString() + ', see log for stacktrace' ], flash.errors)

					// stacktrace in flash scope
					flash.debug = e.getStackTrace()

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
				flow.page = 7
			}
			on("next").to "waitForSave"
			on("previous").to "samples"
		}

		// render finish page
		done {
			render(view: "_done")
			onRender {
				flow.page = 8
			}
			onEnd {
				// clean flow scope
				flow.clear()
			}
		}
	}

	/**
	 * load a study
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def loadStudy(flow, flash, params) {
		// load study
		try {
			// load study
			flow.study = (params.studyid) ? Study.findById( params.studyid ) : Study.findByTitle( params.study )

			// set 'quicksave' variable
			flow.quickSave = true

			return true
		} catch (Exception e) {
			// rollback
			this.appendErrorMap(['exception': e.toString() + ', see log for stacktrace'], flash.errors)

			return false
		}
	}

	/**
	 * Handle the wizard study page
	 * 
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def studyPage(flow, flash, params) {
		// remember the params in the flash scope
		flash.values = params
		
		// instantiate study of it is not yet present
		if (!flow.study) flow.study = new Study()

		// did the study template change?
		if (params.get('template').size() && flow.study.template?.name != params.get('template')) {
			println ".change study template!"

			// yes, was the template already set?
			if (flow.study.template instanceof Template) {
				// yes, first make sure all values are unset?
				println "!!! check the database fields if data of a previous template remains in the database or is deleted by GORM!"
			}

			// set the template
			flow.study.template = Template.findByName(params.remove('template'))
		}

		// does the study have a template set?
		if (flow.study.template && flow.study.template instanceof Template) {
			// yes, iterate through template fields
			flow.study.giveFields().each() {
				// and set their values
				flow.study.setFieldValue(it.name, params.get(it.escapedName()))
			}
		}

		// handle publications
		handlePublications(flow, flash, params)

		// handle contacts
		handleContacts(flow, flash, params)

		// validate the study
		if (flow.study.validate()) {
			// instance is okay
			return true
		} else {
			// validation failed
			flash.errors = [:]
			this.appendErrors(flow.study, flash.errors)
			return false
		}
	}

	/**
	 * re-usable code for handling publications form data in a web flow
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def handlePublications(flow, flash, params) {
		if (!flow.study.publications) flow.study.publications = []

		// Check the ids of the pubblications that should be attached
		// to this study. If they are already attached, keep 'm. If
		// studies are attached that are not in the selected (i.e. the
		// user deleted them), remove them
		def publicationIDs = params.get('publication_ids')
		if (publicationIDs) {
			// Find the individual IDs and make integers
			publicationIDs = publicationIDs.split(',').collect { Integer.parseInt(it, 10) }

			// First remove the publication that are not present in the array
			flow.study.publications.removeAll { publication -> !publicationIDs.find { id -> id == publication.id } }

			// Add those publications not yet present in the database
			publicationIDs.each { id ->
				if (!flow.study.publications.find { publication -> id == publication.id }) {
					def publication = Publication.get(id)
					if (publication) {
						flow.study.addToPublications(publication)
					} else {
						println('.publication with ID ' + id + ' not found in database.')
					}
				}
			}

		} else {
			println('.no publications selected.')
			flow.study.publications.clear()
		}

	}

	/**
	 * re-usable code for handling contacts form data in a web flow
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @return boolean
	 */
	def handleContacts(flow, flash, params) {
		if (!flow.study.persons) flow.study.persons = []

		// Check the ids of the contacts that should be attached
		// to this study. If they are already attached, keep 'm. If
		// studies are attached that are not in the selected (i.e. the
		// user deleted them), remove them

		// Contacts are saved as [person_id]-[role_id]
		def contactIDs = params.get('contacts_ids')
		if (contactIDs) {
			// Find the individual IDs and make integers
			contactIDs = contactIDs.split(',').collect {
				def parts = it.split('-')
				return [person: Integer.parseInt(parts[0]), role: Integer.parseInt(parts[1])]
			}

			// First remove the contacts that are not present in the array
			flow.study.persons.removeAll {
				studyperson -> !contactIDs.find { ids -> (ids.person == studyperson.person.id) && (ids.role == studyperson.role.id) }
			}

			// Add those contacts not yet present in the database
			contactIDs.each { ids ->
				if (!flow.study.persons.find { studyperson -> (ids.person == studyperson.person.id) && (ids.role == studyperson.role.id) }) {
					def person = Person.get(ids.person)
					def role = PersonRole.get(ids.role)
					if (person && role) {
						// Find a studyperson object with these parameters
						def studyPerson = StudyPerson.findAll().find { studyperson -> studyperson.person.id == person.id && studyperson.role.id == role.id }

						// If if does not yet exist, save the example
						if (!studyPerson) {
							studyPerson = new StudyPerson(
								person: person,
								role: role
							)
							studyPerson.save(flush: true)
						}

						flow.study.addToPersons(studyPerson)
					} else {
						println('.person ' + ids.person + ' or Role ' + ids.role + ' not found in database.')
					}
				}
			}
		} else {
			println('.no persons selected.')
			flow.study.persons.clear()
		}

	}
	                                         
	/**
	 * Handle the wizard subject page
	 *
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def subjectPage(flow, flash, params) {
		def errors = false
		flash.errors = [:]

		// remember the params in the flash scope
		flash.values = params

		// iterate through subjects
		flow.study.subjects.each() { subject ->
			// iterate through (template and domain) fields
			subject.giveFields().each() { field ->
				// set field
				subject.setFieldValue(
					field.name,
					params.get('subject_' + subject.getIdentifier() + '_' + field.escapedName())
				)
			}

			// validate subject
			if (!subject.validate()) {
				errors = true
				this.appendErrors(subject, flash.errors, 'subject_' + subject.getIdentifier() + '_')
			}
		}

		return !errors
	}

	/**
	 * Add a number of subjects to a study
	 *
	 * required params entities:
	 * -addNumber (int)
	 * -species   (string)
	 * -template  (string)
	 *
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def addSubjects(flow, flash, params) {
		// remember the params in the flash scope
		flash.values = params

		// handle the subject page
		subjectPage(flow, flash, params)

		// (re)set error message
		flash.errors = [:]

		// set work variables
		def errors		= false
		def number		= params.get('addNumber') as int
		def species		= Term.findByName(params.get('species'))
		def template	= Template.findByName(params.get('template'))

		// can we add subjects?
		if (number > 0 && species && template) {
			// add subjects to study
			number.times {
				// work variables
				def subjectName = 'Subject '
				def subjectIterator = 1
				def tempSubjectName = subjectName + subjectIterator

				// make sure subject name is unique
				if (flow.study.subjects) {
					while (flow.study.subjects.find { it.name == tempSubjectName }) {
						subjectIterator++
						tempSubjectName = subjectName + subjectIterator
					}
				}
				subjectName = tempSubjectName
				
				// create a subject instance
				def subject = new Subject(
					name		: subjectName,
					species		: species,
					template	: template
				)

				// add it to the study
				flow.study.addToSubjects( subject )

				// validate subject
				if (subject.validate()) {
					println ".added subject "+subject
				} else {
					// whoops?
					flow.study.removeFromSubjects( subject )

					// append errors
					this.appendErrors(subject, flash.errors)
					errors = true
				}
			}
		} else {
			// add feedback
			errors = true
			if (number < 1)	this.appendErrorMap(['addNumber': 'Enter a positive number of subjects to add'], flash.errors)
			if (!species)	this.appendErrorMap(['species': 'You need to select a species, or add one if it is not yet present'], flash.errors)
			if (!template)	this.appendErrorMap(['template': 'You need to select a template, or add one if it is not yet present'], flash.errors)
		}

		return !errors
	}

	/**
	 * Handle the wizard event page
	 *
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def eventPage(flow, flash, params) {
		def errors = false
		flash.errors = [:]

		// remember the params in the flash scope
		flash.values = params

		// handle the 'add event' form
		if (flow.event) {
			flow.event.giveFields().each() { field ->
				// set field
				flow.event.setFieldValue(
					field.name,
					params.get(field.escapedName())
				)
			}
		}

		// handle the eventGroup names and grouping
		def name	= ""
		def tempName= ""
		flow.study.eventGroups.each() { eventGroup ->
			// iterate through templates
			flow.study.giveAllEventTemplates().each() { template ->
				tempName = params.get( 'eventGroup_' + eventGroup.getIdentifier() + '_' + template.getIdentifier() )

				// is the name different?
				if (tempName != eventGroup.name) {
					name = tempName
				}
			}

			// should the name change?
			if (name) {
				// yes, change it
				eventGroup.name = name
				name = ""
			}

			// handle eventGrouping
			( ((flow.study.events) ? flow.study.events : []) + ((flow.study.samplingEvents) ? flow.study.samplingEvents : []) ) .each() { event ->
				if (params.get( 'event_' + event.getIdentifier() + '_group_' + eventGroup.getIdentifier() )) {
					// add to eventGroup
					if (event instanceof SamplingEvent) {
						// check if we are already in this eventGroup
						if (!eventGroup.samplingEvents.find { it.equals(event) }) {
							// no, add it
							eventGroup.addToSamplingEvents(event)

							// iterate through subjects for this eventGroup
							eventGroup.subjects.each() { subject ->
								// instantiate a sample for this subject / event
								def samplingEventName = this.ucwords(event.template.name)
								def sampleName = (this.ucwords(subject.name) + '_' + samplingEventName + '_' + new RelTime(event.startTime).toString()).replaceAll("([ ]{1,})", "")
								def tempSampleIterator = 0
								def tempSampleName = sampleName

								// make sure sampleName is unique
								if (flow.study.samples) {
									while (flow.study.samples.find { it.name == tempSampleName }) {
										tempSampleIterator++
										tempSampleName = sampleName + "_" + tempSampleIterator
									}
									sampleName = tempSampleName
								}

								// instantiate a sample
								flow.study.addToSamples(
									new Sample(
										parentSubject: subject,
										parentEvent: event,
										name: sampleName,
										template: (event.sampleTemplate) ? event.sampleTemplate : ''
									)
								)
							}
						}
					} else {
						eventGroup.addToEvents(event)
					}
				} else {
					// remove from eventGroup
					if (event instanceof SamplingEvent) {
						// iterate through subjects (if we have them)
						eventGroup.subjects.each() { subject ->
							// find all samples for this subject / event
							flow.study.samples.findAll { (it.parentEvent.equals(event) && it.parentSubject.equals(subject) ) }.each() {
								// delete this sample
								flow.study.removeFromSamples( it )
								it.delete()
							}
						}
						
						eventGroup.removeFromSamplingEvents(event)
					} else {
						eventGroup.removeFromEvents(event)
					}
				}
			}
		}

		// handle the (sampling) events
		( ((flow.study.events) ? flow.study.events : []) + ((flow.study.samplingEvents) ? flow.study.samplingEvents : []) ) .each() { event ->
			event.giveFields().each() { field ->
				event.setFieldValue(
					field.name,
					params.get( 'event_' + event.getIdentifier() + '_' + field.escapedName() )
				)
			}

			// validate event
			if (!event.validate()) {
				errors = true
				this.appendErrors(event, flash.errors)
			}
		}

		return !errors
	}

	/**
	 * Handle the wizard group page
	 *
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def groupPage(flow, flash, params) {
		def errors = false
		flash.errors = [:]

		// remember the params in the flash scope
		flash.values = params

		// iterate through groups
		flow.study.eventGroups.each() { eventGroup ->
			// iterate through subjects
			flow.study.subjects.each() { subject ->
				if (params.get('subject_' + subject.getIdentifier() + '_group_' + eventGroup.getIdentifier() )) {
					// check if this subject is already part of this eventGroup
					if ( !eventGroup.subjects.find { it.equals(subject) } ) {
						// add to eventGroup
						eventGroup.addToSubjects(subject)

						// iterate through samplingEvents
						eventGroup.samplingEvents.each() { samplingEvent ->
							def samplingEventName = this.ucwords(samplingEvent.template.name)
							def sampleName = (this.ucwords(subject.name) + '_' + samplingEventName + '_' + new RelTime(samplingEvent.startTime).toString()).replaceAll("([ ]{1,})", "")
							def tempSampleIterator = 0
							def tempSampleName = sampleName

							// make sure sampleName is unique
							if (flow.study.samples) {
								while (flow.study.samples.find { it.name == tempSampleName }) {
									tempSampleIterator++
									tempSampleName = sampleName + "_" + tempSampleIterator
								}
								sampleName = tempSampleName
							}

							// instantiate a sample
							flow.study.addToSamples(
								new Sample(
									parentSubject: subject,
									parentEvent: samplingEvent,
									name: sampleName,
									template: (samplingEvent.sampleTemplate) ? samplingEvent.sampleTemplate : ''
								)
							)
						}
					}
				} else {
					// remove from eventGroup
					eventGroup.removeFromSubjects(subject)

					// iterate through samplingEvents
					eventGroup.samplingEvents.each() { samplingEvent ->
						flow.study.samples.findAll { ( it.parentEvent.equals(samplingEvent) && it.parentSubject.equals(subject) ) }.each() {
							// delete this sample
							flow.study.removeFromSamples( it )
							it.delete()
						}
					}
				}
			}
		}
	}

	/**
	 * Handle the wizard samples page
	 *
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def samplePage(flow, flash, params) {
		def errors = false
		flash.errors = [:]

		// remember the params in the flash scope
		flash.values = params

		// iterate through samples
		flow.study.samples.each() { sample ->
			// iterate through sample fields
			sample.giveFields().each() { field ->
				def value = params.get('sample_'+sample.getIdentifier()+'_'+field.escapedName())

				// set field value
				if (!(field.name == 'name' && !value)) {
					println "setting "+field.name+" to "+value
					sample.setFieldValue(field.name, value)
				}
			}

			// has the template changed?
			def templateName = params.get('template_' + sample.getIdentifier())
			if (templateName && sample.template?.name != templateName) {
				sample.template = Template.findByName(templateName)
			}

			// validate sample
			if (!sample.validate()) {
				errors = true
				this.appendErrors(sample, flash.errors, 'sample_' + sample.getIdentifier() + '_' )
				println 'error-> sample_'+sample.getIdentifier()
			}
		}

		return !errors
	}

	/**
	 * Handle the wizard assays page
	 *
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def assayPage(flow, flash, params) {
		def errors = false
		flash.errors = [:]

		// remember the params in the flash scope
		flash.values = params

		// handle the 'add assay' form
		if (flow.assay) {
			flow.assay.giveFields().each() { field ->
				// set field
				flow.assay.setFieldValue(
					field.name,
					params.get(field.escapedName())
				)
			}
		}

		// handle the assay data
		flow.study.assays.each() { assay ->
			// set data
			assay.giveFields().each() { field ->
				assay.setFieldValue(
					field.name,
					params.get( 'assay_' + assay.getIdentifier() + '_' + field.escapedName() )
				)
			}

			// validate assay
			if (!assay.validate()) {
				errors = true
				this.appendErrors(assay, flash.errors, 'assay_' + assay.getIdentifier() + '_')
			}
		}

		return !errors
	}

	/**
	 * Handle the wizard assayGroups page
	 *
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @returns boolean
	 */
	def assayGroupPage(flow, flash, params) {
		def errors = false
		flash.errors = [:]

		// remember the params in the flash scope
		flash.values = params

		// iterate through samples
		flow.study.samples.each() { sample ->
			// iterate through assays
			flow.study.assays.each() { assay ->
				if (params.get( 'sample_' + sample.getIdentifier() + '_assay_' + assay.getIdentifier() )) {
					println "add sample "+sample.getIdentifier()+" to assay "+assay.getIdentifier()
					// add sample to assay
					assay.addToSamples( sample )
				} else {
					// remove sample from assay
					assay.removeFromSamples( sample )
				}
				println assay.samples
			}
		}

		return !errors
	}

	/**
	 * groovy / java equivalent of php's ucwords function
	 *
	 * Capitalize all first letters of separate words
	 *
	 * @param String
	 * @return String
	 */
	def ucwords(String text) {
		def newText = ''

		// change case to lowercase
		text = text.toLowerCase()

		// iterate through words
		text.split(" ").each() {
			newText += it[0].toUpperCase() + it.substring(1) + " "
		}

		return newText.substring(0, newText.size()-1)
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
	 * @return object  linkedHashMap
	 */
	def getHumanReadableErrors(object) {
		def errors = [:]
		object.errors.getAllErrors().each() {
			def message = it.toString()

			//errors[it.getArguments()[0]] = it.getDefaultMessage()
			errors[it.getArguments()[0]] = message.substring(0, message.indexOf(';'))
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

	/**
	 * Parses a RelTime string and returns a nice human readable string
	 *
	 * @return Human Readable string or a HTTP response code 400 on error
	 */
	def ajaxParseRelTime = {
		if (params.reltime == null) {
			response.status = 400
			render('reltime parameter is expected')
		}

		try {
			def reltime = RelTime.parseRelTime(params.reltime)
			render reltime.toPrettyString()
		} catch (IllegalArgumentException e) {
			response.status = 400
			render(e.getMessage())
		}
	}

	/**
	 * Proxy for searching PubMed articles (or other articles from the Entrez DB).
	 *
	 * This proxy is needed because it is not allowed to fetch XML directly from a different
	 * domain using javascript. So we have the javascript call a function on our own domain
	 * and the proxy will fetch the data from Entrez
	 *
	 * @since	20100609
	 * @param	_utility	The name of the utility, without the complete path. Example: 'esearch.fcgi'
	 * @return	XML
	 */
	def entrezProxy = {
		// Remove unnecessary parameters
		params.remove( "action" )
		params.remove( "controller" )

		def url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils";
		def util = params.remove( "_utility" )
		def paramString = params.collect { k, v -> k + '=' + v.encodeAsURL() }.join( '&' );

		def fullUrl = url + '/' + util + '?' + paramString;

		// Return the output of the request
		// render fullUrl;
		render(
                    text:           new URL( fullUrl ).getText(),
                    contentType:    "text/xml",
                    encoding:       "UTF-8"
                );
	}
}