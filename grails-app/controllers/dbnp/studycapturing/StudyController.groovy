package dbnp.studycapturing

import grails.converters.*
import grails.plugins.springsecurity.Secured
import nl.grails.plugins.gdt.TemplateFieldType

/**
 * Controller class for studies
 */
class StudyController {
    def AuthenticationService
    
    //static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    /**
     * Shows all studies where the user has access to
     */
    def list = {

        def user = AuthenticationService.getLoggedInUser()
        def max = Math.min(params.max ? params.int('max') : 10, 100)

        def studies = Study.giveReadableStudies( user, max );

        [studyInstanceList: studies, studyInstanceTotal: studies.count(), loggedInUser: user]
    }

    /**
     * Shows studies for which the logged in user is the owner
     */
    @Secured(['IS_AUTHENTICATED_REMEMBERED'])
    def myStudies = {
        def user = AuthenticationService.getLoggedInUser()
        def max = Math.min(params.max ? params.int('max') : 10, 100)

        def studies = Study.findAllByOwner(user);
        render( view: "list", model: [studyInstanceList: studies, studyInstanceTotal: studies.count(), loggedInUser: user] )
    }

    /**
     * Shows a comparison of multiple studies using the show view
     * 
     */
    def list_extended = {
		// If nothing has been selected, redirect the user
		if( !params.id ) 
			redirect( action: 'list' )

		// Check whether one id has been selected or multiple.
		def ids = params.id
		if( ids instanceof String )
			redirect( action: 'show', id: ids )

		// Parse strings to a long
		def long_ids = []
		ids.each { long_ids.add( Long.parseLong( it ) ) }

		println( long_ids )

        def startTime = System.currentTimeMillis()
		def c = Study.createCriteria()

        def studyList = c {
			maxResults( Math.min(params.max ? params.int('max') : 10, 100) )
			'in'( "id", long_ids )
		}
        render(view:'show',model:[studyList: studyList, studyInstanceTotal: Study.count(), multipleStudies: ( studyList.size() > 1 ) ] )
    }

    /**
     * Shows one or more studies
     */
    def show = {
        def startTime = System.currentTimeMillis()

        def studyInstance = Study.get( params.long( "id" ) )
        if (!studyInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'study.label', default: 'Study'), params.id])}"
            redirect(action: "list")
        }
        else {
            // Check whether the user may see this study
            def loggedInUser = AuthenticationService.getLoggedInUser()
            if( !studyInstance.canRead(loggedInUser) ) {
                flash.message = "You have no access to this study"
                redirect(action: "list")
            }

            // The study instance is packed into an array, to be able to
            // use the same view for showing the study and comparing multiple
            // studies
            [studyList: [ studyInstance ], multipleStudies: false, loggedInUser: loggedInUser, facebookLikeUrl: studyInstance.getFieldValue('published') ? "/study/show/${studyInstance?.id}" : '' ]
        }
    }

	/**
     * Shows the subjects tab of one or more studies. Is called when opening the subjects-tab
	 * on the study overview screen.
     */
    def show_subjects = {
		def studyList = readStudies( params.id );

		if( !studyList )
			return

		[studyList: studyList, studyInstanceTotal: Study.count(), multipleStudies: ( studyList.size() > 1 ), loggedInUser: AuthenticationService.getLoggedInUser() ]
    }

	/**
     * Shows the events timeline tab of one or more studies. Is called when opening the events timeline-tab
	 * on the study overview screen.
     */
    def show_events_timeline = {
		def studyList = readStudies( params.id );

		if( !studyList )
			return

		[studyList: studyList, studyInstanceTotal: Study.count(), multipleStudies: ( studyList.size() > 1 ), loggedInUser: AuthenticationService.getLoggedInUser() ]
    }

	/**
     * Shows the events table tab of one or more studies. Is called when opening the events table-tab
	 * on the study overview screen.
     */
    def show_events_table = {
		def studyList = readStudies( params.id );

		if( !studyList )
			return

		[studyList: studyList, studyInstanceTotal: Study.count(), multipleStudies: ( studyList.size() > 1 ), loggedInUser: AuthenticationService.getLoggedInUser() ]
    }

	/**
     * Shows the assays tab of one or more studies. Is called when opening the assays tab
	 * on the study overview screen.
     */
    def show_assays = {
		def studyList = readStudies( params.id );

		if( !studyList )
			return

		[studyList: studyList, studyInstanceTotal: Study.count(), multipleStudies: ( studyList.size() > 1 ), loggedInUser: AuthenticationService.getLoggedInUser() ]
    }

	/**
     * Shows the samples tab of one or more studies. Is called when opening the samples-tab
	 * on the study overview screen.
     */
    def show_samples = {
		def studyList = readStudies( params.id );

		if( !studyList )
			return

		[studyList: studyList, studyInstanceTotal: Study.count(), multipleStudies: ( studyList.size() > 1 ), loggedInUser: AuthenticationService.getLoggedInUser() ]
    }

	/**
     * Shows the persons tab of one or more studies. Is called when opening the persons tab
	 * on the study overview screen.
     */
    def show_persons = {
		def studyList = readStudies( params.id );

		if( !studyList )
			return

		[studyList: studyList, studyInstanceTotal: Study.count(), multipleStudies: ( studyList.size() > 1 ), loggedInUser: AuthenticationService.getLoggedInUser() ]
    }

	/**
     * Shows the publications tab of one or more studies. Is called when opening the publications tab
	 * on the study overview screen.
     */
    def show_publications = {
		def studyList = readStudies( params.id );

		if( !studyList )
			return

		[studyList: studyList, studyInstanceTotal: Study.count(), multipleStudies: ( studyList.size() > 1 ), loggedInUser: AuthenticationService.getLoggedInUser() ]
    }

	/**
	 * Creates the javascript for showing the timeline of one or more studies
	 */
	def createTimelineBandsJs = {
		def studyList = readStudies( params.id );

		if( !studyList )
			return

		[studyList: studyList, studyInstanceTotal: Study.count(), multipleStudies: ( studyList.size() > 1 ) ]
	}

    /**
	 * Reads one or more studies from the database and checks whether the logged
	 * in user is allowed to access them.
	 * 
	 * Is used by several show_-methods
	 *
	 * @return List with Study objects or false if an error occurred.
	 */
	private def readStudies( id ) {
		// If nothing has been selected, redirect the user
		if( !id || !( id instanceof String)) {
            response.status = 500;
            render 'No study selected';
            return false
		}

		// Check whether one id has been selected or multiple.
		def ids = URLDecoder.decode( id ).split( "," );

		// Parse strings to a long
		def long_ids = []
		ids.each { long_ids.add( Long.parseLong( it ) ) }

		def c = Study.createCriteria()

        def studyList = c {
			maxResults( Math.min(params.max ? params.int('max') : 10, 100) )
			'in'( "id", long_ids )
		}

		// Check whether the user may see these studies
		def studiesAllowed = []
        def loggedInUser = AuthenticationService.getLoggedInUser()

		studyList.each { studyInstance ->
            if( studyInstance.canRead(loggedInUser) ) {
				studiesAllowed << studyInstance
            }
		}

		// If the user is not allowed to see any of the studies, return 404
		if( studiesAllowed.size() == 0 ) {
            response.status = 404;
            render 'Selected studies not found';
            return false
		}
		
		return studyList
	}

    def showByToken = {
        def studyInstance = Study.findByCode(params.id)
        if (!studyInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'study.label', default: 'Study'), params.id])}"
            redirect(action: "list")
        }
        else {
            // Check whether the user may see this study
            def loggedInUser = AuthenticationService.getLoggedInUser()
            if( !studyInstance.canRead(loggedInUser) ) {
                flash.message = "You have no access to this study"
                redirect(action: "list")
            }

            redirect(action: "show", id: studyInstance.id)
        }
    }

    /**
     * Gives the events for one eventgroup in JSON format
     *
     */
    def events = {
        def eventGroupId = Integer.parseInt( params.id );
        def studyId      = Integer.parseInt( params.study );
        def eventGroup;

        // eventGroupId == -1 means that the orphaned events should be given
        if( eventGroupId == -1 ) {
            def studyInstance = Study.get( studyId )
            
            if (studyInstance == null) {
                flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'study.label', default: 'Study'), studyId])}"
                redirect(action: "list");
                return;
            }

            events = studyInstance.getOrphanEvents();
        } else {
            eventGroup = EventGroup.get(params.id)

            if (eventGroup == null) {
                flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'eventgroup.label', default: 'Eventgroup'), params.id])}"
                redirect(action: "list");
                return;
            }
            events = eventGroup?.events + eventGroup?.samplingEvents;
        }

        // This parameter should give the startdate of the study in milliseconds
        // since 1-1-1970
        long startDate  = Long.parseLong( params.startDate )

        // Create JSON object
        def json = [ 'dateTimeFormat': 'iso8601', events: [] ];

        // Add all other events
        for( event in events ) {
            def parameters = []
            for( templateField in event.giveTemplateFields() ) {
                def value = event.getFieldValue( templateField.name );
				if( value ) {
					if( templateField.type == TemplateFieldType.RELTIME )
						value = new nl.grails.plugins.gdt.RelTime( value ).toString();

	                def param = templateField.name + " = " + value;

					if( templateField.unit )
						param += templateField.unit;

                    parameters << param ;
                }
            }

			def description = parameters.join( '<br />\n' );

			if( event instanceof SamplingEvent ) {
				 json.events << [
					'start':    new Date( startDate + event.startTime * 1000 ),
					'end':      new Date( startDate + event.startTime * 1000 ),
					'durationEvent': false,
					'title': event.template.name,
					'description': description
				]
			} else {
				 json.events << [
					'start':    new Date( startDate + event.startTime * 1000 ),
					'end':      new Date( startDate + event.endTime * 1000 ),
					'durationEvent': true,
					'title': event.template.name,
					'description': description
				]
				
			}
        }
        render json as JSON
    }

    def delete = {
        def studyInstance = Study.get(params.id)
        if (studyInstance) {
            try {
                studyInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'study.label', default: 'Study'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'study.label', default: 'Study'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'study.label', default: 'Study'), params.id])}"
            redirect(action: "list")
        }
    }

    /**
     * Renders assay names and id's as JSON
     */
    def ajaxGetAssays = {

        def study = Study.read(params.id)
        render study?.assays?.collect{[name: it.name, id: it.id]} as JSON
    }


}
