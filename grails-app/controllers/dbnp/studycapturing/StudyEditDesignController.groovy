package dbnp.studycapturing

import grails.converters.JSON

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
			result = [ status: "OK", id: subjectEventGroup.id, group: subjectGroupName, subjectGroupId: subjectEventGroup.subjectGroup?.id, eventGroupId: subjectEventGroup.eventGroup?.id ]
		} else {
			response.status = 500
			result = [ status: "Error" ]
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
		if( name ) {
			eventGroup.name = name
		}


		def result
		if( eventGroup.save() ) {
			study.addToEventGroups( eventGroup );
			result = [ status: "OK", id: eventGroup.id, name: eventGroup.name ]
		} else {
			response.status = 500
			result = [ status: "Error" ]
		}

		render result as JSON

	}

	/**
	 * Updates a subject eventgroup with new properties from the form
	 * @return
	 */
	def eventGroupUpdate() {
		def eventGroup = EventGroup.read( params.long( "id" ) );

		if( !EventGroup ) {
			response.status = 404
			render "Not found"
			return
		}

		def name = params.get( "name" )
		def result = [ "OK" ]
		
		if( name && name != eventGroup.name ) {
			eventGroup.name = name

			if( !subjectEventGroup.save() ) {
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
			name: eventGroup.name,
			start: studyStart * 1000,
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
				className: 'event event-id-' + event.id
			]
		}

		eventGroup.samplingEventInstances?.each { event ->
			def start = ( studyStart + event.startTime ) * 1000
			resultData.events << [
				start: start,
				content: event.event.name?.trim() ?: '[samplingevent without name]',
				className: 'samplingEvent samplingEvent-id-' + event.id
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
			result = [ status: "OK", id: eventInEventGroup.id, eventGroupId: eventGroupId, eventId: eventId ]
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
			eventInEventGroup.setAbsoluteEndTime( params.long( "end" ) / 1000 );
		}
		
		def result
		if( eventInEventGroup.save() ) {
			result = [ status: "OK", id: eventInEventGroup.id, eventGroupId: eventGroupId, eventId: eventId ]
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
