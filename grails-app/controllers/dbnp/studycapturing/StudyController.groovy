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

    /*def create = {
        def studyInstance = new Study()
        studyInstance.properties = params
        return [studyInstance: studyInstance]
    }

    def save = {
        def studyInstance = new Study(params)
        if (studyInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'study.label', default: 'Study'), studyInstance.id])}"
            redirect(action: "show", id: studyInstance.id)
        }
        else {
            render(view: "create", model: [studyInstance: studyInstance])
        }
    }*/

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

    /**
     * Gives the events for one eventgroup in JSON format
     *
     */
    def events = {
        def eventGroup = EventGroup.get(params.id)

        // This parameter should give the startdate of the study in milliseconds
        // since 1-1-1970
        long startDate  = Long.parseLong( params.startDate )
        
        if (!eventGroup) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'eventgroup.label', default: 'Eventgroup'), params.id])}"
            redirect(action: "list")
        }
        else {

            // Create JSON object
            def json = [ 'dateTimeFormat': 'iso8601', events: [] ];

            // Add the start of the study as event
            /*
            json.events << [
                'start':    startDate,
                'durationEvent': false,
                'title': "Start date study",
                'color': 'red'
            ]
            */
           
            // Add all other events
            for( event in eventGroup.events ) {
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
