package dbnp.studycapturing
import dbnp.data.Term

class ProtocolController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]


    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [protocolInstanceList: Protocol.list(params), protocolInstanceTotal: Protocol.count()]
    }

    def create = {
        def protocolInstance = new Protocol()
        protocolInstance.properties = params
        return [protocolInstance: protocolInstance]
    }

    def save = {

        def protocolInstance =  new Protocol(params)

        if (protocolInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'protocol.label', default: 'Protocol'), protocolInstance.id])}"
            redirect(action:show, id: protocolInstance.id)
        }
        else {
            render(view: "create", model: [protocolInstance: protocolInstance])
        }
    }

    def show = {

        println params

        def protocolInstance = Protocol.get(params.id)
        if (!protocolInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'protocol.label', default: 'Protocol'), params.id])}"
            redirect(action: "list")
        }
        else { [protocolInstance: protocolInstance] }
    }

    def edit = {
        def protocolInstance = Protocol.get(params.id)
        if (!protocolInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'protocol.label', default: 'Protocol'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [protocolInstance: protocolInstance]
        }
    }

    def update = {
        def protocolInstance = Protocol.get(params.id)
        if (protocolInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (protocolInstance.version > version) {
                    
                    protocolInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'protocol.label', default: 'Protocol')] as Object[], "Another user has updated this Protocol while you were editing")
                    render(view: "edit", model: [protocolInstance: protocolInstance])
                    return
                }
            }
            protocolInstance.properties = params
            if (!protocolInstance.hasErrors() && protocolInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'protocol.label', default: 'Protocol'), protocolInstance.id])}"
                redirect(action: "show", id: protocolInstance.id)
            }
            else {
                render(view: "edit", model: [protocolInstance: protocolInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'protocol.label', default: 'Protocol'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def protocolInstance = Protocol.get(params.id)
        if (protocolInstance) {
            try {
                protocolInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'protocol.label', default: 'Protocol'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'protocol.label', default: 'Protocol'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'protocol.label', default: 'Protocol'), params.id])}"
            redirect(action: "list")
        }
    }

    def createDummies =
    {
	 def newInstance = new Protocol()
	 newInstance.name = "Hugo (dummy #1)"
	 newInstance.reference = Term.find("from Term t")

	 if( newInstance.save() )
	 { redirect( action:show, id: newInstance.id ) }
         else { chain( action:list ) }
    }

}
