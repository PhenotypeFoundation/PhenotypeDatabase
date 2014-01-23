package dbnp.studycapturing

import grails.converters.JSON

import org.dbnp.gdt.RelTime
import org.dbnp.gdt.Template

import dbnp.authentication.SecUser

class StudyEditDesignController {
	def authenticationService
	def datatablesService
	def studyEditService
	
	def index() {
		def study = getStudyFromRequest( params )
		if( !study ) {
			redirect action: "add"
			return
		}

		[
			study: study,
			templates: [
				event: Template.findAllByEntity( Event.class ),
				samplingEvent:  Template.findAllByEntity( SamplingEvent.class )
			]
		]

	}

	/**
	 * Adds a subject eventgroup with new properties from the form
	 * @return
	 */
	def subjectEventGroupAdd() {
		def subjectEventGroup = new SubjectEventGroup();
		def study = getStudyFromRequest( params )

		subjectEventGroup.parent = study

		if( params.long( "start" ) ) {
			subjectEventGroup.startTime = ( params.long( "start" ) - study.startDate.time ) / 1000;
		}

		def subjectGroupName = params.get( "subjectGroup" )
		if( subjectGroupName ) {
			def subjectGroup = subjectEventGroup.parent.subjectGroups.find { it.name == subjectGroupName }

			if( subjectGroup ) {
				subjectEventGroup.subjectGroup = subjectGroup
			}
		}

		def eventGroupId = params.long( "eventGroupId" )
		if( eventGroupId )
			subjectEventGroup.eventGroup = EventGroup.read( eventGroupId )

		def result
		if( subjectEventGroup.save() ) {
			study.addToSubjectEventGroups( subjectEventGroup );
			studyEditService.generateSamples( subjectEventGroup )
			
			result = [ status: "OK", id: subjectEventGroup.id, group: subjectGroupName, subjectGroupId: subjectEventGroup.subjectGroup?.id, eventGroupId: subjectEventGroup.eventGroup?.id ]
		} else {
			response.status = 500
			result = [ status: "Error", errors: subjectEventGroup.errors.allErrors ]
		}

		render result as JSON

	}

	/**
	 * Updates a subject eventgroup with new properties from the form
	 * @return
	 */
	def subjectEventGroupUpdate() {
		def subjectEventGroup = SubjectEventGroup.read( params.long( "id" ) );

		if( !subjectEventGroup ) {
			response.status = 404
			render "Not found"
			return
		}

		if( params.long( "start" ) ) {
			subjectEventGroup.setAbsoluteStartTime( params.long( "start" ) / 1000 );
		}

		def subjectGroupName = params.get( "subjectGroup" )
		if( subjectGroupName && subjectGroupName != subjectEventGroup.subjectGroup?.name ) {
			def subjectGroup = subjectEventGroup.parent.subjectGroups.find { it.name == subjectGroupName }

			if( subjectGroup ) {
				// Remove the samples belonging to this subjectEventGroup, because other subjects will be involved
				( [] + subjectEventGroup.samples ).each {
					subjectEventGroup.parent.deleteSample( it )
				}
				
				subjectEventGroup.subjectGroup = subjectGroup
			}
		}

		def result
		if( subjectEventGroup.save() ) {
			result = [ status: "OK", subjectGroupId: subjectEventGroup.subjectGroup?.id, eventGroupId: subjectEventGroup.eventGroup?.id ]
		} else {
			response.status = 500
			result  = [ status: "Error" ]
		}
		
		render result as JSON
	}

	/**
	 * Removes a subject eventgroup from the system
	 * @return
	 */
	def subjectEventGroupDelete() {
		def subjectEventGroup = SubjectEventGroup.read( params.long( "id" ) );

		if( !subjectEventGroup ) {
			response.status = 404
			render "Not found"
			return
		}

		// Delete subjectEventgroup and associated samples
		subjectEventGroup.parent.deleteSubjectEventGroup( subjectEventGroup )
		
		def result = [ status: "OK" ]
		render result as JSON
	}

	/**
	 * Adds a eventgroup with new properties from the form
	 * @return
	 */
	def eventGroupAdd() {
		def eventGroup = new EventGroup();
		def study = getStudyFromRequest( params )

		eventGroup.parent = study

		def name = params.get( "name" )
		def result
		if( name ) {
			eventGroup.name = name
			
			println "New eventgroup, study: "
			
			eventGroup.parent = study
			if( eventGroup.validate() ) {
				study.addToEventGroups( eventGroup );
				eventGroup.save( flush: true )
				result = [ status: "OK", id: eventGroup.id, name: eventGroup.name, duration: 0, url: g.createLink( action: "eventGroupDetails", id: eventGroup.id ) ]
			} else {
				response.status = 500
				result = [ status: "Error", errors: eventGroup.errors.allErrors ]
			}
		} else {
			response.status = 400
			result = [ status: "Error", errors: "Please specify a name" ] 
		}

		render result as JSON

	}

	/**
	 * Updates a subject eventgroup with new properties from the form
	 * @return
	 */
	def eventGroupUpdate() {
		def eventGroup = EventGroup.read( params.long( "id" ) );

		if( !eventGroup ) {
			response.status = 404
			render "Not found"
			return
		}

		def name = params.get( "name" )
		def result = [ "OK" ]
		
		if( name && name != eventGroup.name ) {
			eventGroup.name = name

			if( !eventGroup.save() ) {
				response.status = 500
				result  = [ status: "Error" ]
			}
		}
		
		render result as JSON
	}

	/**
	 * Removes a subject eventgroup from the system
	 * @return
	 */
	def eventGroupDelete() {
		def eventGroup = EventGroup.read( params.long( "id" ) );

		if( !eventGroup ) {
			response.status = 404
			render "Not found"
			return
		}

		eventGroup.parent.deleteEventGroup( eventGroup )
		
		def result = [ status: "OK" ]
		render result as JSON
	}
	
	/**
	 * Returns a list of details about
	 * @return	JSON array with keys
	 * 		events
	 */
	def eventGroupDetails( long id ) {
		def eventGroup = EventGroup.read( id );

		if( !eventGroup ) {
			response.status = 404
			render "Not found"
			return
		}

		def studyStart = eventGroup.parent?.startDate?.getTime() / 1000
		
		def resultData = [
			id: eventGroup.id,
			name: eventGroup.name,
			start: studyStart * 1000,
			duration: eventGroup.duration.value,
			end: ( studyStart + eventGroup.duration.value ) * 1000,
			events: []
		]

		// Add events
		eventGroup.eventInstances?.each { event ->
			def start = ( studyStart + event.startTime ) * 1000
			resultData.events << [
				start: start,
				end: start + event.duration * 1000,
				content: event.event.name?.trim() ?: '[event without name]',
				className: 'event event-id-' + event.id,
				data: [
					id: event.id,
					eventId: event.event.id,
					type: 'event'	
				]
			]
		}

		eventGroup.samplingEventInstances?.each { event ->
			def start = ( studyStart + event.startTime ) * 1000
			resultData.events << [
				start: start,
				content: event.event.name?.trim() ?: '[samplingevent without name]',
				className: 'samplingEvent samplingEvent-id-' + event.id,
				data: [
					id: event.id,
					eventId: event.event.id,
					type: 'samplingEvent'	
				]
			]
		}

		render resultData as JSON
	}

	/**
	 * Adds a eventInEventGroup with new properties from the form
	 * @return
	 */
	def eventInEventGroupAdd() {
		def eventInEventGroup = new EventInEventGroup();
		def study = getStudyFromRequest( params )
		
		if( params.long( "start" ) ) {
			eventInEventGroup.startTime = ( params.long( "start" ) - study.startDate.time ) / 1000;
			
			if( params.long( "end" ) ) {
				eventInEventGroup.duration = ( params.long( "start" ) - params.long( "end" ) ) / 1000;
			} else if( params.duration ) {
				// The duration can be given as seconds, or as reltime string
				def duration
				if( params.duration.isLong() ) {
					duration = new RelTime( params.duration.toLong() )
				} else {
					duration = new RelTime( params.duration )
				}
				
				eventInEventGroup.duration = duration.value
			}
		}

		def eventId = params.long( "eventId" )
		if( eventId )
			eventInEventGroup.event = Event.read( eventId )

		def eventGroupId = params.long( "eventGroupId" )
		if( eventGroupId )
			eventInEventGroup.eventGroup = EventGroup.read( eventGroupId )
	
		def result
		if( eventInEventGroup.save() ) {

			// Return the start and end times
			def studyStart = study?.startDate?.getTime() / 1000
			def start = ( studyStart + eventInEventGroup.startTime ) * 1000
			def end = start + eventInEventGroup.duration * 1000
			
			result = [ status: "OK", id: eventInEventGroup.id, start: start, end: end, duration: eventInEventGroup.duration, eventGroupId: eventGroupId, eventId: eventId, type: "event" ]
		} else {
			response.status = 500
			result = [ status: "Error" ]
		}

		render result as JSON
	}

	/**
	 * Updates a eventInEventGroup with new properties from the form
	 * @return
	 */
	def eventInEventGroupUpdate() {
		def eventInEventGroup = EventInEventGroup.read( params.long( "id" ) );

		if( !eventInEventGroup ) {
			response.status = 404
			render "Not found"
			return
		}

		if( params.long( "start" ) ) {
			eventInEventGroup.setAbsoluteStartTime( params.long( "start" ) / 1000 );
		}
		if( params.long( "end" ) ) {
			eventInEventGroup.duration = ( params.long( "end" ) - params.long( "start" ) ) / 1000;
		}
		
		def result
		if( eventInEventGroup.save() ) {
			result = [ status: "OK", id: eventInEventGroup.id, eventGroupId: eventInEventGroup.eventGroup.id, eventId: eventInEventGroup.event.id, type: "event" ]
		} else {
			response.status = 500
			result  = [ status: "Error" ]
		}
		
		render result as JSON
	}

	/**
	 * Removes a eventInEventGroup from the system
	 * @return
	 */
	def eventInEventGroupDelete() {
		def eventInEventGroup = EventInEventGroup.read( params.long( "id" ) );

		if( !eventInEventGroup ) {
			response.status = 404
			render "Not found"
			return
		}

		eventInEventGroup.delete()
		
		def result = [ status: "OK" ]
		render result as JSON
	}
	
	
	/**
	 * Adds a eventInEventGroup with new properties from the form
	 * @return
	 */
	def samplingEventInEventGroupAdd() {
		def samplingEventInEventGroup = new SamplingEventInEventGroup();
		def study = getStudyFromRequest( params )
		
		if( params.long( "start" ) ) {
			samplingEventInEventGroup.startTime = ( params.long( "start" ) - study.startDate.time ) / 1000;
		}

		def eventId = params.long( "eventId" )
		if( eventId )
			samplingEventInEventGroup.event = SamplingEvent.read( eventId )

		def eventGroupId = params.long( "eventGroupId" )
		if( eventGroupId )
			samplingEventInEventGroup.eventGroup = EventGroup.read( eventGroupId )
	
		def result
		if( samplingEventInEventGroup.save() ) {
			// Generate new samples for this eventGroup
			studyEditService.generateSamples( samplingEventInEventGroup )
			
			result = [ status: "OK", id: samplingEventInEventGroup.id, eventGroupId: eventGroupId, eventId: eventId, type: "samplingEvent" ]
		} else {
			response.status = 500
			result = [ status: "Error" ]
		}

		render result as JSON
	}

	/**
	 * Updates a eventInEventGroup with new properties from the form
	 * @return
	 */
	def samplingEventInEventGroupUpdate() {
		def samplingEventInEventGroup = SamplingEventInEventGroup.read( params.long( "id" ) );

		if( !samplingEventInEventGroup ) {
			response.status = 404
			render "Not found"
			return
		}

		if( params.long( "start" ) ) {
			samplingEventInEventGroup.setAbsoluteStartTime( params.long( "start" ) / 1000 );
		}
		
		def result
		if( samplingEventInEventGroup.save() ) {
			result = [ status: "OK", id: samplingEventInEventGroup.id, eventGroupId: samplingEventInEventGroup.eventGroup.id, eventId: samplingEventInEventGroup.event.id, type: "samplingEvent" ]
		} else {
			response.status = 500
			result  = [ status: "Error" ]
		}
		
		render result as JSON
	}

	/**
	 * Removes a eventInEventGroup from the system
	 * @return
	 */
	def samplingEventInEventGroupDelete() {
		def samplingEventInEventGroup = SamplingEventInEventGroup.read( params.long( "id" ) );

		if( !samplingEventInEventGroup ) {
			response.status = 404
			render "Not found"
			return
		}

		samplingEventInEventGroup.delete()
		
		def result = [ status: "OK" ]
		render result as JSON
	}
	
	/**
	 * Shows a list of events in a given study
	 * @return
	 */
	def eventList() {
		def study = getStudyFromRequest( params )
		
		if( !study ) {
			response.status = 404
			render "Not found"
			return
		}
		
		render template: "eventList", model: [ study: study ]
	}
	
	def eventAdd() {
		def entity  = new Event()
		studyEditService.putParentIntoEntity( entity, params )
		
		if( request.post ) {
			studyEditService.putParamsIntoEntity( entity, params ) 
	
			if( params._action == "save" ) {
				if( entity.validate() ) {
					// If the entity validates, make sure it is added properly to the study
					entity.parent.addToEvents( entity )
					entity.save( flush: true );
					
					// Tell the frontend the save has succeeded
					response.status = 210
					render ""
					return
				} else {
					flash.validationErrors = entity.errors.allErrors
				}
			}
		}
		
		render template: "event", model: [entity: entity]
	}
	
	def eventUpdate() {
		def entity = Event.read( params.long( "id" ) )
		
		if( !entity ) {
			response.status = 404
			render "Not found"
			return
		}
		
		if( request.post ) {
			putParamsIntoEntity( entity ) 
	
			if( params._action == "save" ) {
				if( entity.validate() ) {
					// Tell the frontend the save has succeeded
					entity.save( flush: true );
					
					// Tell the frontend the save has succeeded
					response.status = 210
					render ""
					return
				} else {
					flash.validationErrors = entity.errors
				}
			}
		}
		
		render template: "event", model: [entity: entity]
	}
	
	/**
	 * Removes an event from the system
	 * @return
	 */
	def eventDelete() {
		def event = Event.read( params.long( "id" ) );

		if( !event ) {
			response.status = 404
			render "Not found"
			return
		}

		event.parent.deleteEvent( event )
		
		def result = [ status: "OK" ]
		render result as JSON
	}
	
	/**
	 * Shows a list of sampling events in a given study
	 * @return
	 */
	def samplingEventList() {
		def study = getStudyFromRequest( params )
		
		if( !study ) {
			response.status = 404
			render "Not found"
			return
		}
		
		render template: "samplingEventList", model: [ study: study ]
	}


	def samplingEventAdd() {
		def entity  = new SamplingEvent()
		studyEditService.putParentIntoEntity( entity, params )
		
		if( request.post ) {
			studyEditService.putParamsIntoEntity( entity, params )
	
			if( params._action == "save" ) {
				if( entity.validate() ) {
					// If the entity validates, make sure it is added properly to the study
					entity.parent.addToSamplingEvents( entity )
					entity.save( flush: true );
					
					// Tell the frontend the save has succeeded
					response.status = 210
					render ""
					return
				} else {
					flash.validationErrors = entity.errors.allErrors
				}
			}
		}
		
		render template: "sampling_event", model: [entity: entity]
	}
	
	def samplingEventUpdate() {
		def entity = SamplingEvent.read( params.long( "id" ) )
		
		if( !entity ) {
			response.status = 404
			render "Not found"
			return
		}
		
		if( request.post ) {
			putParamsIntoEntity( entity )
	
			if( params._action == "save" ) {
				if( entity.validate() ) {
					// Tell the frontend the save has succeeded
					entity.save( flush: true );
					
					// Tell the frontend the save has succeeded
					response.status = 210
					render ""
					return
				} else {
					flash.validationErrors = entity.errors
				}
			}
		}
		
		render template: "sampling_event", model: [entity: entity]
	}
	
	/**
	 * Removes an event from the system
	 * @return
	 */
	def samplingEventDelete() {
		def samplingEvent = SamplingEvent.read( params.long( "id" ) );

		if( !samplingEvent ) {
			response.status = 404
			render "Not found"
			return
		}

		samplingEvent.parent.deleteSamplingEvent( samplingEvent )
		
		def result = [ status: "OK" ]
		render result as JSON
	}
	
	/**
	 * Adds a subjectgroup with new properties from the form
	 * @return
	 */
	def subjectGroupAdd() {
		def subjectGroup = new SubjectGroup();
		def study = getStudyFromRequest( params )

		subjectGroup.parent = study

		def name = params.get( "name" )
		def result
		if( name ) {
			subjectGroup.name = name
			
			if( subjectGroup.validate() ) {
				study.addToSubjectGroups( subjectGroup );
				subjectGroup.save( flush: true )
				
				handleSubjectsInSubjectGroup( params[ "subjects[]" ], subjectGroup )
				result = [ status: "OK", id: subjectGroup.id, name: subjectGroup.name ]
			} else {
				response.status = 500
				result = [ status: "Error", errors: subjectGroup.errors.allErrors ]
			}
		} else {
			response.status = 400
			result = [ status: "Error", errors: "Please specify a name" ]
		}

		render result as JSON

	}

	/**
	 * Updates a subjectGroup  with new properties from the form
	 * @return
	 */
	def subjectGroupUpdate() {
		def subjectGroup = SubjectGroup.read( params.long( "id" ) );

		if( !subjectGroup ) {
			response.status = 404
			render "Not found"
			return
		}

		def name = params.get( "name" )
		def result = [ "OK" ]
		
		if( name && name != subjectGroup.name ) {
			subjectGroup.name = name

			if( subjectGroup.save() ) {
				handleSubjectsInSubjectGroup( params[ "subjects[]" ], subjectGroup )
				studyEditService.generateSamples( subjectGroup )
			} else {
				response.status = 500
				result  = [ status: "Error" ]
			}
			
		} else {
			handleSubjectsInSubjectGroup( params[ "subjects[]" ], subjectGroup )
			studyEditService.generateSamples( subjectGroup )
		}
		
		render result as JSON
	}

	/**
	 * Removes a subjectgroup from the system
	 * @return
	 */
	def subjectGroupDelete() {
		def subjectGroup = SubjectGroup.read( params.long( "id" ) );

		if( !subjectGroup ) {
			response.status = 404
			render "Not found"
			return
		}

		subjectGroup.parent.deleteSubjectGroup( subjectGroup )
		
		def result = [ status: "OK" ]
		render result as JSON
	}
	
	/**
	 * Returns a list of details about a subjectgroup
	 * @return	JSON array with keys
	 * 		name
	 * 		subjects
	 */
	def subjectGroupDetails( long id ) {
		def subjectGroup = SubjectGroup.read( id );

		if( !subjectGroup ) {
			response.status = 404
			render "Not found"
			return
		}

		def resultData = [
			id: subjectGroup.id,
			name: subjectGroup.name,
			subjects: subjectGroup.subjects ? subjectGroup.subjects.collect { [ id: it.id, name: it.name ] } : []
		]

		render resultData as JSON
	}
	
	protected void handleSubjectsInSubjectGroup( subjectIds, subjectGroup ) {
		if( !subjectGroup ) 
			return
		
		if( !subjectIds ) {
			subjectGroup.subjects.clear()
			return
		}
		
		// Loop through all subjects
		if( subjectGroup.subjects ) {
			def subjects = [] + subjectGroup.subjects
			subjects.each { subject ->
				if( subjectIds.contains( subject.id.toString() ) ) {
					subjectIds -= subject.id.toString()
				} else {
					subjectGroup.removeFromSubjects( subject )
				}
			}
		}
		
		// Add other subjects
		subjectIds.each { subjectId ->
			def subject = Subject.read( subjectId )
			if( subject ) {
				subjectGroup.addToSubjects( subject )
			} 
		}

	}

	
	/**
	 * Retrieves the required study from the database or return an empty Study object if
	 * no id is given
	 *
	 * @param params	Request parameters with params.id being the ID of the study to be retrieved
	 * @return			A study from the database or an empty study if no id was given
	 */
	protected Study getStudyFromRequest(params) {
		SecUser user = authenticationService.getLoggedInUser();
		Study study  = (params.containsKey('id')) ? Study.findById(params.get('id')) : new Study(title: "New study", owner: user);

		// got a study?
		if (!study) {
			flash.error = "No study found with given id";
		} else if(!study.canWrite(user)) {
			flash.error = "No authorization to edit this study."
			study = null;
		}

		return study;
	}
}
