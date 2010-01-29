package dbnp.studycapturing
import dbnp.data.Term

class EventDescriptionController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [eventDescriptionInstanceList: EventDescription.list(params), eventDescriptionInstanceTotal: EventDescription.count()]
    }

    def create = {
        def eventDescriptionInstance = new EventDescription()
        eventDescriptionInstance.properties = params
        return [eventDescriptionInstance: eventDescriptionInstance]
    }

    def save = {
        def eventDescriptionInstance = new EventDescription(params)
        if (eventDescriptionInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), eventDescriptionInstance.id])}"
            redirect(action: "show", id: eventDescriptionInstance.id)
        }
        else {
            render(view: "create", model: [eventDescriptionInstance: eventDescriptionInstance])
        }
    }

    def show = {

        def eventDescriptionInstance = EventDescription.get(params.id)
        if (!eventDescriptionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
            redirect(action: "list")
        }

        else {
            [eventDescriptionInstance: eventDescriptionInstance, params:params]
        }
    }


    def showMyProtocol = {

        def protocol = ( params.protocolid) ?  Protocol.get(params.protocolid) : Protocol.find("for Protocol id")
        def event = Event.get(params.id)
	def protocolInstance = event.eventDescription.protocol

        if( protocolInstance && protocolInstance && protocol.id==protocolInstance.id ) {
	    def parameterStringValues = event.parameterStringValues
            render( view:"showMyProtocolFilled", model:["protocolInstance":protocolInstance,"event":event,"parameterStringValues":parameterStringValues] )
        }
        else {
	    if(protocol!=null) render( view: "showMyProtocolEmpty", model:["protocolInstance":protocol] )
	    else               render( "NULL" )
        }
}


    def edit = {
        def eventDescriptionInstance = EventDescription.get(params.id)
        if (!eventDescriptionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [eventDescriptionInstance: eventDescriptionInstance]
        }
    }

    def update = {
        def eventDescriptionInstance = EventDescription.get(params.id)
        if (eventDescriptionInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (eventDescriptionInstance.version > version) {
                    
                    eventDescriptionInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'eventDescription.label', default: 'EventDescription')] as Object[], "Another user has updated this EventDescription while you were editing")
                    render(view: "edit", model: [eventDescriptionInstance: eventDescriptionInstance])
                    return
                }
            }
            eventDescriptionInstance.properties = params
            if (!eventDescriptionInstance.hasErrors() && eventDescriptionInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), eventDescriptionInstance.id])}"
                redirect(action: "show", id: eventDescriptionInstance.id)
            }
            else {
                render(view: "edit", model: [eventDescriptionInstance: eventDescriptionInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def eventDescriptionInstance = EventDescription.get(params.id)
        if (eventDescriptionInstance) {
            try {
                eventDescriptionInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
            redirect(action: "list")
        }
    }


    def test = { println "test"
	    render("test") }
}
