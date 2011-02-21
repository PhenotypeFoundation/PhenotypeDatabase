package dbnp.studycapturing

import grails.plugins.springsecurity.Secured

import dbnp.authentication.SecUser
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import org.dbnp.gdt.*
import org.dbnp.bgdt.*

/**
 * ajaxflow Controller
 *
 * @author	Jeroen Wesbeek
 * @since	20101220
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class StudyWizardController {
	def pluginManager
	def authenticationService
	def validationTagLib = new ValidationTagLib()

	/**
	 * index method, redirect to the webflow
	 * @void
	 */
	def index = {
		// Grom a development message
		if (pluginManager.getGrailsPlugin('grom')) "redirecting into the webflow".grom()

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
	 * @void
	 */
	def pagesFlow = {
		// start the flow
		onStart {
			// Grom a development message
			if (pluginManager.getGrailsPlugin('grom')) "entering the WebFlow".grom()

			// define variables in the flow scope which is availabe
			// throughout the complete webflow also have a look at
			// the Flow Scopes section on http://www.grails.org/WebFlow
			//
			// The following flow scope variables are used to generate
			// wizard tabs. Also see common/_tabs.gsp for more information
			// define flow variables
			flow.page = 0
			flow.pages = [
				//[title: 'Templates'],			// templates
				[title: 'Start'],				// load or create a study
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
			render(view: "/studyWizard/index")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the main Ajaxflow page (index.gsp)".grom()

				// let the view know we're in page 1
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
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "entering handleJump".grom()

				if (flow.jump && flow.jump.action == 'edit') {
					if (flow.jump.id) {
						// load study
						if (this.loadStudy(flow, flash, [studyid:flow.jump.id],authenticationService.getLoggedInUser())) {
							toStudyPage()
						} else {
							toStartPage()
						}
					} else {
						toModifyPage()
					}
				} else if (flow.jump && flow.jump.action == 'create') {
					if (!flow.study) flow.study = new Study()
					toStudyPage()
				} else {
					toStartPage()
				}
			}
			on("toStartPage").to "start"
			on("toStudyPage").to "study"
			on("toModifyPage").to "modify"
		}

		// create or modify a study
		start {
			render(view: "_start")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the partial: pages/_start.gsp".grom()

				flow.page = 1
				success()
			}
			on("next") {
				// clean the flow scope
				flow.remove('study')

				// create a new study instance
				if (!flow.study) flow.study = new Study()

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
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the partial: pages/_redirect.gsp".grom()

				flash.uri = "/importer/index"
			}
			on("next").to "start"
		}

		// load a study to modify
		modify {
			render(view: "_modify")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the partial: pages/_modify.gsp".grom()

				flow.page = 1
				flash.showCancel = true
				success()
			}
			on("cancel") {
				flow.remove('study')

				success()
			}.to "start"
			on("next") {
				// load study
				if (this.loadStudy(flow, flash, params, authenticationService.getLoggedInUser())) {
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
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the partial: pages/_study.gsp".grom()

				flow.page = 1

				// since this controller was refactored it's technically
				// safe to enable quicksave throughout the application.
				// However, we must keep an eye on the quality of the
				// studies as this change makes it easier to create dummy
				// studies which will create garbage in out database.
				flow.quickSave = true

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
				flash.wizardErrors = [:]
				success()
			}.to "study"
            on("switchTemplate") {
				// handle form data
				studyPage(flow, flash, params)

				// reset errors
				flash.wizardErrors = [:]
				success()
			}.to "study"
			on("previous") {
				// handle form data
				studyPage(flow, flash, params)

				// reset errors
				flash.wizardErrors = [:]
				success()
			}.to "start"
			on("next") {
				studyPage(flow, flash, params) ? success() : error()
			}.to "subjects"
			on("quickSave") {
				studyPage(flow, flash, params) ? success() : error()
			}.to "save"
			on("toPageTwo") {
				studyPage(flow, flash, params) ? success() : error()
			}.to "subjects"
			on("toPageThree") {
				studyPage(flow, flash, params) ? success() : error()
			}.to "events"
			on("toPageFour") {
				studyPage(flow, flash, params) ? success() : error()
			}.to "samples"
			on("toPageFive") {
				studyPage(flow, flash, params) ? success() : error()
			}.to "assays"
			on("toPageSix") {
				studyPage(flow, flash, params) ? success() : error()
			}.to "confirm"
			on("toPageSeven") {
				studyPage(flow, flash, params) ? success() : error()
			}.to "save"
		}

		// render and handle subjects page
		subjects {
			render(view: "_subjects")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the partial: pages/_subjects.gsp".grom()

				flow.page = 2

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
				flash.wizardErrors = [:]

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
				flash.wizardErrors = [:]
				success()
			}.to "study"
			on("next") {
				// handle form data
				subjectPage(flow, flash, params) ? success() : error()
			}.to "events"
			on("quickSave") {
				// handle form data
				subjectPage(flow, flash, params) ? success() : error()
			}.to "save"
			on("toPageOne") {
				subjectPage(flow, flash, params) ? success() : error()
			}.to "study"
			on("toPageThree") {
				subjectPage(flow, flash, params) ? success() : error()
			}.to "events"
			on("toPageFour") {
				subjectPage(flow, flash, params) ? success() : error()
			}.to "samples"
			on("toPageFive") {
				subjectPage(flow, flash, params) ? success() : error()
			}.to "assays"
			on("toPageSix") {
				subjectPage(flow, flash, params) ? success() : error()
			}.to "confirm"
			on("toPageSeven") {
				subjectPage(flow, flash, params) ? success() : error()
			}.to "save"
		}

		// render events page
		events {
			render(view: "_events")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the partial: pages/_events.gsp".grom()

				flow.page = 3

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
				flash.wizardErrors = [:]
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
				flash.wizardErrors = [:]
				success()
			}.to "events"
			on("add") {
				// handle form data
				eventPage(flow, flash, params)

				// reset errors
				flash.wizardErrors = [:]

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
					this.appendErrors(flow.event, flash.wizardErrors)
					error()
				}
			}.to "events"
			on("deleteEvent") {
				// handle form data
				eventPage(flow, flash, params)

				// reset errors
				flash.wizardErrors = [:]

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
				flash.wizardErrors = [:]
				success()
			}.to "events"
			on("deleteEventGroup") {
				// handle form data
				eventPage(flow, flash, params)

				// reset errors
				flash.wizardErrors = [:]

				// remove eventGroup
				def eventGroupToRemove = flow.study.eventGroups.find { it.getIdentifier() == (params.get('do') as int) }
				if (eventGroupToRemove) {
					flow.study.deleteEventGroup( eventGroupToRemove )
				}
			}.to "events"
			on("duplicate") {
				// handle form data
				eventPage(flow, flash, params)

				// reset errors
				flash.wizardErrors = [:]

				// clone event
				def event = null
				(((flow.study.events) ? flow.study.events : []) + ((flow.study.samplingEvents) ? flow.study.samplingEvents : [])).find { it.getIdentifier() == (params.get('do') as int) }.each {
					event = (it instanceof SamplingEvent) ? new SamplingEvent() : new Event()

					// set template
					event.template = it.template

					// copy data
					it.giveFields().each() { field ->
						event.setFieldValue(
							field.name,
							it.getFieldValue(field.name)
						)
					}

					// assign duplicate event to study
					if (event instanceof SamplingEvent) {
						flow.study.addToSamplingEvents(event)
					} else {
						flow.study.addToEvents(event)
					}
				}

				success()
			}.to "events"
			on("previous") {
				// handle form data
				eventPage(flow, flash, params)

				// reset errors
				flash.wizardErrors = [:]
				success()
			}.to "subjects"
			on("next") {
				// handle form data
				eventPage(flow, flash, params) ? success() : error()
			}.to "eventsNext"
			on("quickSave") {
				// handle form data
				eventPage(flow, flash, params) ? success() : error()
			}.to "save"
			on("toPageOne") {
				eventPage(flow, flash, params) ? success() : error()
			}.to "study"
			on("toPageTwo") {
				eventPage(flow, flash, params) ? success() : error()
			}.to "subjects"
			on("toPageFour") {
				eventPage(flow, flash, params) ? success() : error()
			}.to "samples"
			on("toPageFive") {
				eventPage(flow, flash, params) ? success() : error()
			}.to "assays"
			on("toPageSix") {
				eventPage(flow, flash, params) ? success() : error()
			}.to "confirm"
			on("toPageSeven") {
				eventPage(flow, flash, params) ? success() : error()
			}.to "save"
		}

		// decide to show a warning page or not
		eventsNext {
			action {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) ".entering eventsNext".grom()

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
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the partial: pages/_unnassigned_samplingEvent_warning.gsp".grom()

				flow.page = 3
				success()
			}
			on("next").to "groups"
			on("previous").to "events"
			on("toPageOne") {
				eventPage(flow, flash, params) ? success() : error()
			}.to "study"
			on("toPageTwo") {
				eventPage(flow, flash, params) ? success() : error()
			}.to "subjects"
			on("toPageFour") {
				eventPage(flow, flash, params) ? success() : error()
			}.to "samples"
			on("toPageFive") {
				eventPage(flow, flash, params) ? success() : error()
			}.to "assays"
			on("toPageSix") {
				eventPage(flow, flash, params) ? success() : error()
			}.to "confirm"
			on("toPageSeven") {
				eventPage(flow, flash, params) ? success() : error()
			}.to "save"
		}

		// groups page
		groups {
			render(view: "_groups")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the partial: pages/_groups.gsp".grom()

				flow.page = 3
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
			}.to "save"
			on("toPageOne") {
				groupPage(flow, flash, params) ? success() : error()
			}.to "study"
			on("toPageTwo") {
				groupPage(flow, flash, params) ? success() : error()
			}.to "subjects"
			on("toPageFour") {
				groupPage(flow, flash, params) ? success() : error()
			}.to "samples"
			on("toPageFive") {
				groupPage(flow, flash, params) ? success() : error()
			}.to "assays"
			on("toPageSix") {
				groupPage(flow, flash, params) ? success() : error()
			}.to "confirm"
			on("toPageSeven") {
				groupPage(flow, flash, params) ? success() : error()
			}.to "save"
		}

		// sample 'previous' page with warning
		samplePrevious {
			render(view: "_samples_previous_warning")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the partial: pages/_samples_previous_warning.gsp".grom()

				flow.page = 4
			}
			on("next").to "samples"
			on("previous").to "groups"
			on("toPageOne").to "study"
			on("toPageTwo").to "subjects"
			on("toPageThree").to "events"
			on("toPageFive").to "assays"
			on("toPageSix").to "confirm"
			on("toPageSeven").to "save"
		}

		// samples page
		samples {
			render(view: "_samples")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the partial: pages/_samples.gsp".grom()

				flow.page = 4
				success()
			}
			on("switchTemplate") {
				// handle form data
				samplePage(flow, flash, params)

				// ignore errors
				flash.wizardErrors = [:]

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
				flash.wizardErrors = [:]

				success()
			}.to "samples"
			on("regenerate") {
				// handle samples
				samplePage(flow, flash, params)

				// remove all samples from the study
				flow.study.samples.findAll{true}.each() { sample ->
					flow.study.deleteSample( sample )
				}

				// ignore errors
				flash.wizardErrors = [:]

				success()
			}.to "samples"
			on("previous") {
				// handle samples
				samplePage(flow, flash, params)

				// ignore errors
				flash.wizardErrors = [:]

				success()
			}.to "samplePrevious"
			on("next") {
				// handle form data
				samplePage(flow, flash, params) ? success() : error()
			}.to "assays"
			on("quickSave") {
				// handle form data
				samplePage(flow, flash, params) ? success() : error()
			}.to "save"
			on("toPageOne") {
				samplePage(flow, flash, params) ? success() : error()
			}.to "study"
			on("toPageTwo") {
				samplePage(flow, flash, params) ? success() : error()
			}.to "subjects"
			on("toPageThree") {
				samplePage(flow, flash, params) ? success() : error()
			}.to "events"
			on("toPageFive") {
				samplePage(flow, flash, params) ? success() : error()
			}.to "assays"
			on("toPageSix") {
				samplePage(flow, flash, params) ? success() : error()
			}.to "confirm"
			on("toPageSeven") {
				samplePage(flow, flash, params) ? success() : error()
			}.to "save"
		}

		// assays page
		assays {
			render(view: "_assays")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the partial: pages/_assays.gsp".grom()

				flow.page = 5
			}
			on("refresh") {
				// handle form data
				assayPage(flow, flash, params)

				// force refresh of the template
				if (flow.assay && flow.assay.template && flow.assay.template instanceof Template) {
					flow.assay.template.refresh()
				}

				// reset errors
				flash.wizardErrors = [:]
				success()
			}.to "assays"
            on("switchTemplate") {
				// handle form data
	            assayPage(flow, flash, params)

	            // find assay template
	            def template = Template.findByName(params.get('template'))
	            if (flow.assay) {
		            // set template
		            flow.assay.template = template
		            if (template) {
			            flow.assay.setFieldValue(
				            'externalAssayID',
				            ucwords(flow.study.code).replaceAll("([ ]{1,})", "") + '_' + ucwords(template.name).replaceAll("([ ]{1,})", "")
			            )
		            }
	            } else {
		            // create a new assay instance
		            flow.assay = new Assay(template: template)
		            if (template) {
			            flow.assay.setFieldValue(
				            'externalAssayID',
				            ucwords(flow.study.code).replaceAll("([ ]{1,})", "") + '_' + ucwords(template.name).replaceAll("([ ]{1,})", "")
			            )
		            }
	            }

				// reset errors
				flash.wizardErrors = [:]
				success()
			}.to "assays"
			on("add") {
				// handle form data
				assayPage(flow, flash, params)

				// reset errors
				flash.wizardErrors = [:]

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
					flow.study.deleteAssay( flow.assay )

					// append errors
					this.appendErrors(flow.assay, flash.wizardErrors)
					error()
				}
			}.to "assays"
			on("deleteAssay") {
				// handle form data
				assayPage(flow, flash, params)

				// reset errors
				flash.wizardErrors = [:]

				// find this assay
				def assay = flow.study.assays.find { it.getIdentifier() == (params.get('do') as int) }

				// perform delete
				if (assay) flow.study.deleteAssay( assay )
			}.to "assays"
			on("previous") {
				// handle form data
				assayPage(flow, flash, params)

				// ignore errors
				flash.wizardErrors = [:]

				success()
			}.to "samples"
			on("next") {
				// handle form data
				assayPage(flow, flash, params) ? success() : error()
			}.to "assayNext"
			on("quickSave") {
				// handle form data
				assayPage(flow, flash, params) ? success() : error()
			}.to "save"
			on("toPageOne") {
				assayPage(flow, flash, params) ? success() : error()
			}.to "study"
			on("toPageTwo") {
				assayPage(flow, flash, params) ? success() : error()
			}.to "subjects"
			on("toPageThree") {
				assayPage(flow, flash, params) ? success() : error()
			}.to "events"
			on("toPageFour") {
				assayPage(flow, flash, params) ? success() : error()
			}.to "samples"
			on("toPageSix") {
				assayPage(flow, flash, params) ? success() : error()
			}.to "confirm"
			on("toPageSeven") {
				assayPage(flow, flash, params) ? success() : error()
			}.to "save"
		}

		// assayNext
		assayNext {
			action {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "entering assayNext".grom()

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
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the partial: pages/_assay_groups.gsp".grom()

				flow.page = 5
			}
			on("previous") {
				// handle form data
				assayGroupPage(flow, flash, params)

				// ignore errors
				flash.wizardErrors = [:]

				success()
			}.to "assays"
			on("next") {
				// handle form data
				assayGroupPage(flow, flash, params) ? success() : error()
			}.to "confirm"
			on("quickSave") {
				// handle form data
				assayGroupPage(flow, flash, params) ? success() : error()
			}.to "save"
			on("toPageOne") {
				assayGroupPage(flow, flash, params) ? success() : error()
			}.to "study"
			on("toPageTwo") {
				assayGroupPage(flow, flash, params) ? success() : error()
			}.to "subjects"
			on("toPageThree") {
				assayGroupPage(flow, flash, params) ? success() : error()
			}.to "events"
			on("toPageFour") {
				assayGroupPage(flow, flash, params) ? success() : error()
			}.to "samples"
			on("toPageSix") {
				assayGroupPage(flow, flash, params) ? success() : error()
			}.to "confirm"
			on("toPageSeven") {
				assayGroupPage(flow, flash, params) ? success() : error()
			}.to "save"
		}

		// confirm Previous
		confirmPrevious {
			action {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "entering confirmPrevious".grom()

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
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the partial: pages/_confirmation.gsp".grom()

				flow.page = 6
			}
			on("toStudy").to "study"
			on("toSubjects").to "subjects"
			on("toEvents").to "events"
			on("toGroups").to "groups"
			on("toSamples").to "samples"
			on("toAssays").to "assays"
			on("toAssayGroups").to "assayGroups"
			on("previous").to "confirmPrevious"
			on("next").to "save"
			on("quickSave").to "save"
			on("toPageOne").to "study"
			on("toPageTwo").to "subjects"
			on("toPageThree").to "events"
			on("toPageFour").to "samples"
			on("toPageFive").to "assays"
			on("toPageSeven").to "save"
		}

		// store all study data
		save {
			action {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "entering save".grom()

				flow.page = 7
				flash.wizardErrors = [:]

				// persist data to the database
				try {
					// save study
					// Grom a development message
					if (pluginManager.getGrailsPlugin('grom')) "saving study".grom()

					// Make sure the owner of the study is set right
					if (!flow.study.owner) {
						flow.study.owner = authenticationService.getLoggedInUser()
					}

					if (!flow.study.save(flush:true)) {
						this.appendErrors(flow.study, flash.wizardErrors)
						throw new Exception('error saving study')
					}
					log.info ".saved study "+flow.study+" (id: "+flow.study.id+")"

					success()
				} catch (Exception e) {
					// rollback
					this.appendErrorMap(['exception': e.toString() + ', see log for stacktrace' ], flash.wizardErrors)

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
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the partial: pages/_error.gsp".grom()

				flow.page = 6
			}
			on("next").to "save"
			on("previous").to "samples"
			on("toPageOne").to "study"
			on("toPageTwo").to "subjects"
			on("toPageThree").to "events"
			on("toPageFour").to "samples"
			on("toPageFive").to "assays"
			on("toPageSix").to "confirm"
			on("toPageSeven").to "save"
		}

		// render finish page
		done {
			render(view: "_done")
			onRender {
				// Grom a development message
				if (pluginManager.getGrailsPlugin('grom')) "rendering the partial: pages/_done.gsp".grom()

				flow.page = 7
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
	def loadStudy(flow, flash, params, user) {
		flash.wizardErrors	= [:]

		// load study
		try {
			// load study
			def study = (params.studyid) ? Study.findById( params.studyid ) : Study.findByTitle( params.study )

			// Check whether the user is allowed to edit this study. If it is not allowed
			// the used should had never seen a link to this page, so he should never get
			// here. That's why we just return false
            if (!study.canWrite(user)){
 				return false
			}

			// store study in the flowscope
			flow.study = study

			// set 'quicksave' variable
			flow.quickSave = true

			return true
		} catch (Exception e) {
			// rollback
			this.appendErrorMap(['exception': e.getMessage() + ', see log for stacktrace'], flash.wizardErrors)

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
		flash.values		= params
		flash.wizardErrors	= [:]

		// instantiate study of it is not yet present
		if (!flow.study) flow.study = new Study()

		// did the study template change?
		if (params.get('template').size() && flow.study.template?.name != params.get('template')) {
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

		// handle users (readers, writers)
		handleUsers(flow, flash, params, 'readers')
		handleUsers(flow, flash, params, 'writers')

		// handle public checkbox
		if (params.get("publicstudy")) {
			flow.study.publicstudy = params.get("publicstudy")
		}

		// have we got a template?
		if (flow.study.template && flow.study.template instanceof Template) {
			// validate the study
			if (flow.study.validate()) {
				// instance is okay
				return true
			} else {
				// validation failed
				this.appendErrors(flow.study, flash.wizardErrors)
				return false
			}
		} else {
			// no, return an error that the template is not set
			this.appendErrorMap(['template': g.message(code: 'select.not.selected.or.add', args: ['template'])], flash.wizardErrors)
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
		flash.wizardErrors	= [:]
		handleStudyPublications( flow.study, params );
	}

	/**
	 * re-usable code for handling contacts form data in a web flow
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
	 * @return boolean
	 */
	def handleContacts(flow, flash, params) {
		flash.wizardErrors	= [:]

		handleStudyContacts( flow.study, params );
	}

	/**
	 * re-usable code for handling contacts form data in a web flow
	 * @param Map LocalAttributeMap (the flow scope)
	 * @param Map localAttributeMap (the flash scope)
	 * @param Map GrailsParameterMap (the flow parameters = form data)
     * @param String    'readers' or 'writers'
	 * @return boolean
	 */
	def handleUsers(flow, flash, params, type) {
		flash.wizardErrors = [:]
		handleStudyUsers( flow.study, params, type );
	}
	
	/**
	* re-usable code for handling publications form data
	* @param study	Study object to update
	* @param params GrailsParameterMap (the flow parameters = form data)
	* @returns boolean
	*/
   def handleStudyPublications(Study study,  params) {
	   if (study.publications) study.publications = []

	   // Check the ids of the pubblications that should be attached
	   // to this study. If they are already attached, keep 'm. If
	   // studies are attached that are not in the selected (i.e. the
	   // user deleted them), remove them
	   def publicationIDs = params.get('publication_ids')
	   if (publicationIDs) {
		   // Find the individual IDs and make integers
		   publicationIDs = publicationIDs.split(',').collect { Integer.parseInt(it, 10) }

		   // First remove the publication that are not present in the array
		   if( study.publications )
			   study.publications.removeAll { publication -> !publicationIDs.find { id -> id == publication.id } }

		   // Add those publications not yet present in the database
		   publicationIDs.each { id ->
			   if (!study.publications.find { publication -> id == publication.id }) {
				   def publication = Publication.get(id)
				   if (publication) {
					   study.addToPublications(publication)
				   } else {
					   log.info('.publication with ID ' + id + ' not found in database.')
				   }
			   }
		   }

	   } else {
		   log.info('.no publications selected.')
		   if( study.publications )
			   study.publications.clear()
	   }
   }

   /**
	* re-usable code for handling contacts form data
	* @param study	Study object to update
	* @param Map GrailsParameterMap (the flow parameters = form data)
	* @return boolean
	*/
   def handleStudyContacts(Study study, params) {
	   if (!study.persons) study.persons = []

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
		   if( study.persons ) {
			   study.persons.removeAll {
				   studyperson -> !contactIDs.find { ids -> (ids.person == studyperson.person.id) && (ids.role == studyperson.role.id) }
			   }
		   }

		   // Add those contacts not yet present in the database
		   contactIDs.each { ids ->
			   if (!study.persons.find { studyperson -> (ids.person == studyperson.person.id) && (ids.role == studyperson.role.id) }) {
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

					   study.addToPersons(studyPerson)
				   } else {
					   log.info('.person ' + ids.person + ' or Role ' + ids.role + ' not found in database.')
				   }
			   }
		   }
	   } else {
		   log.info('.no persons selected.')
		   if( study.persons )
			   study.persons.clear()
	   }
   }

   /**
	* re-usable code for handling contacts form data
	* @param study	Study object to update
	* @param Map GrailsParameterMap (the flow parameters = form data)
	* @param String    'readers' or 'writers'
	* @return boolean
	*/
   def handleStudyUsers(Study study, params, type) {
	   def users = []

	   if (type == "readers" && study.readers ) {
		   users += study.readers
	   } else if (type == "writers" && study.writers ) {
		   users += study.writers
	   }

	   // Check the ids of the contacts that should be attached
	   // to this study. If they are already attached, keep 'm. If
	   // studies are attached that are not in the selected (i.e. the
	   // user deleted them), remove them

	   // Users are saved as user_id
	   def userIDs = params.get(type + '_ids')
	   
	   if (userIDs) {
		   // Find the individual IDs and make integers
		   userIDs = userIDs.split(',').collect { Long.valueOf(it, 10) }
		   
		   // First remove the publication that are not present in the array
		   users.removeAll { user -> !userIDs.find { id -> id == user.id } }

		   // Add those publications not yet present in the database
		   userIDs.each { id ->
			   if (!users.find { user -> id == user.id }) {
				   def user = SecUser.get(id)
				   if (user) {
					   users.add(user)
				   } else {
					   log.info('.user with ID ' + id + ' not found in database.')
				   }
			   }
		   }
		   
	   } else {
		   log.info('.no users selected.')
		   users.clear()
	   }
	   
	   if (type == "readers") {
		   if (study.readers)
			   study.readers.clear()
			   
		   users.each { study.addToReaders(it) }
	   } else if (type == "writers") {
		   if (study.writers)
			   study.writers.clear()

		   users.each { study.addToWriters(it) }
		   
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
		flash.wizardErrors = [:]

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
				this.appendErrors(subject, flash.wizardErrors, 'subject_' + subject.getIdentifier() + '_')
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
		flash.wizardErrors = [:]

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
			}
		} else {
			// add feedback
			errors = true
			if (number < 1)	this.appendErrorMap(['addNumber': 'Enter a positive number of subjects to add'], flash.wizardErrors)
			if (!species)	this.appendErrorMap(['species': g.message(code: 'select.not.selected.or.add', args: ['species'])], flash.wizardErrors)
			if (!template)	this.appendErrorMap(['template': g.message(code: 'select.not.selected.or.add', args: ['template'])], flash.wizardErrors)
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
		flash.wizardErrors = [:]

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
								def samplingEventName = ucwords(event.template.name)
								def eventGroupName = ucwords(eventGroup.name).replaceAll("([ ]{1,})", "")
								def sampleName = (ucwords(subject.name) + '_' + samplingEventName + '_' + eventGroupName + '_' + new RelTime(event.startTime).toString()).replaceAll("([ ]{1,})", "")
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
										parentSubject	: subject,
										parentEvent		: event,
										parentEventGroup: eventGroup,
										name			: sampleName,
										template		: (event.sampleTemplate) ? event.sampleTemplate : ''
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
								flow.study.deleteSample( it )
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
				this.appendErrors(event, flash.wizardErrors)
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
		flash.wizardErrors = [:]

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
							def samplingEventName = ucwords(samplingEvent.template.name)
							def eventGroupName = ucwords(eventGroup.name)
							def sampleName = (ucwords(subject.name) + '_' + samplingEventName + '_' + eventGroupName + '_' + new RelTime(samplingEvent.startTime).toString()).replaceAll("([ ]{1,})", "")
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
									parentSubject	: subject,
									parentEvent		: samplingEvent,
									parentEventGroup: eventGroup,
									name			: sampleName,
									template		: (samplingEvent.sampleTemplate) ? samplingEvent.sampleTemplate : ''
								)
							)
						}
					} else {
					}
				} else {
					// check if this subject is a member of this eventGroup
					if (eventGroup.subjects.find { it.equals(subject) }) {
						// remove from eventGroup
						eventGroup.removeFromSubjects(subject)

						// iterate through samplingEvents
						eventGroup.samplingEvents.each() { samplingEvent ->
							flow.study.samples.findAll { (it.parentEvent.equals(samplingEvent) && it.parentSubject.equals(subject) && it.parentEventGroup.equals(eventGroup)) }.each() {
								// delete this sample
								flow.study.deleteSample( it )
							}
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
		flash.wizardErrors = [:]

		// remember the params in the flash scope
		flash.values = params

		// iterate through samples
		flow.study.samples.each() { sample ->
			// iterate through sample fields
			sample.giveFields().each() { field ->
				def value = params.get('sample_'+sample.getIdentifier()+'_'+field.escapedName())

				// set field value
				if (!(field.name == 'name' && !value)) {
					log.info "setting "+field.name+" to "+value
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
				this.appendErrors(sample, flash.wizardErrors, 'sample_' + sample.getIdentifier() + '_' )
				log.info 'error-> sample_'+sample.getIdentifier()
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
		flash.wizardErrors = [:]

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
				this.appendErrors(assay, flash.wizardErrors, 'assay_' + assay.getIdentifier() + '_')
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
		flash.wizardErrors = [:]

		// remember the params in the flash scope
		flash.values = params

		// iterate through samples
		flow.study.samples.each() { sample ->
			// iterate through assays
			flow.study.assays.each() { assay ->
				if (params.get( 'sample_' + sample.getIdentifier() + '_assay_' + assay.getIdentifier() )) {
					// add sample to assay
					assay.addToSamples( sample )
				} else {
					// remove sample from assay
					assay.removeFromSamples( sample )
				}
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
	public static ucwords(String text) {
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
		object.errors.getAllErrors().each() { error ->
			// error.codes.each() { code -> println code }

			// generally speaking g.message(...) should work,
			// however it fails in some steps of the wizard
			// (add event, add assay, etc) so g is not always
			// availably. Using our own instance of the
			// validationTagLib instead so it is always
			// available to us
			errors[ error.getArguments()[0] ] = validationTagLib.message(error: error)
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
		this.appendErrorMap(getHumanReadableErrors(object), map)
	}

	def appendErrors(object, map, prepend) {
		this.appendErrorMap(getHumanReadableErrors(object), map, prepend)
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