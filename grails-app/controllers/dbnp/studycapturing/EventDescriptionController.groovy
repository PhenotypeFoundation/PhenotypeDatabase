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
        def description = EventDescription.get(params.id)
        if( description.protocol==null ) {
            protocol = new Protocol()
            render( view:"showMyProtocolEmpty", model:[protocol:protocol,description:description] )
        }
        else {
	    render( view: "showMyProtocolFilled", model:[protocol:description.protocol,description:description] )
        }
    }


    def showMyProtocolEmpty = {
       println "in showMyProtocolEmpty"
    }


    def showMyProtocolFilled = {
       println "in showMyProtocolFilled"
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


    def test = { render(params) }

    def test2 = {
        def eventDescriptionInstance = EventDescription.get(params.id)
        if (!eventDescriptionInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'eventDescription.label', default: 'EventDescription'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [eventDescriptionInstance: eventDescriptionInstance]
        }
    }


    def addProtocolParameter = {
        render( action:"showProtocolParameters", model:['addNew':true] )
    }


    def showProtocolParameters = {
        println params

        def description = EventDescription.get(params.id)
        def protocol =  description.protocol==null ? new Protocol() : description.protocol

        if( params['addOneParameter']=='true') {    // add a new parameter
	    println "adding"
            def parameter = new ProtocolParameter()
	    protocol.addToParameters(parameter)
        }

        def list = []
        protocol.parameters.each {
	    list.add(it)
            list.sort{ a,b -> a.name <=> b.name }
        }

        render( view:"showProtocolParameters", model:[protocol:protocol,description:description,list:list] )
    }

}