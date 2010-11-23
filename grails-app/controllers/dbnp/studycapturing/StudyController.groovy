package dbnp.studycapturing

import grails.converters.*
import grails.plugins.springsecurity.Secured


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

        def c = Study.createCriteria()

        def studies
        if( user == null ) {
            studies = c.list {
                maxResults(max)
                and {
                    eq( "published", true )
                    eq( "publicstudy", true )
                }
            }
        } else {
            studies = c.list {
                maxResults(max)
                or {
                    eq( "owner", user )
                    writers {
                        eq( "id", user.id )
                    }
                    and {
                        readers {
                            eq( "id", user.id )
                        }
                        eq( "published", true )
                    }
                }
            }
        }
        
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
            [studyList: [ studyInstance ], multipleStudies: false, loggedInUser: loggedInUser ]
        }
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
            events = eventGroup?.events;
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
                    parameters << templateField.name + " = " + value;
                }
            }

             json.events << [
                'start':    new Date( startDate + event.startTime * 1000 ),
                'end':      new Date( startDate + event.endTime * 1000 ),
                'durationEvent': !event.isSamplingEvent(),
                'title': event.template.name + " (" + parameters.join( ', ' ) + ")",
                'description': parameters
            ]
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

    /*def edit = {
        def studyInstance = Study.get(params.id)
        if (!studyInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'study.label', default: 'Study'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [studyInstance: studyInstance]
        }
    }

    def update = {
        def studyInstance = Study.get(params.id)
        if (studyInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (studyInstance.version > version) {
                    
                    studyInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'study.label', default: 'Study')] as Object[], "Another user has updated this Study while you were editing")
                    render(view: "edit", model: [studyInstance: studyInstance])
                    return
                }
            }
            studyInstance.properties = params
            if (!studyInstance.hasErrors() && studyInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'study.label', default: 'Study'), studyInstance.id])}"
                redirect(action: "show", id: studyInstance.id)
            }
            else {
                render(view: "edit", model: [studyInstance: studyInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'study.label', default: 'Study'), params.id])}"
            redirect(action: "list")
        }
    }
*/
}
