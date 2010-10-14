package dbnp.studycapturing

import grails.converters.*

/**
 * Controller class for studies
 */
class StudyController {

    //static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [studyInstanceList: Study.list(params), studyInstanceTotal: Study.count()]
    }

    /**
     * Shows a comparison of multiple studies using the show view
     * 
     */
    def list_extended = {
        def startTime = System.currentTimeMillis()
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        
        def studyList = Study.list(params)
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
            // The study instance is packed into an array, to be able to
            // use the same view for showing the study and comparing multiple
            // studies
            [studyList: [ studyInstance ], multipleStudies: false ]
        }
    }

	def showByToken = {
        def studyInstance = Study.findByCode(params.id)
        if (!studyInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'study.label', default: 'Study'), params.id])}"
            redirect(action: "list")
        }
        else {
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
    }*/
}
