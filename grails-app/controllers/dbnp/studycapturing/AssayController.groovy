package dbnp.studycapturing

class AssayController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [assayInstanceList: Assay.list(params), assayInstanceTotal: Assay.count()]
    }

    def create = {
        def assayInstance = new Assay()
        assayInstance.properties = params
        return [assayInstance: assayInstance]
    }

    def save = {
        def assayInstance = new Assay(params)

	// The following lines deviate from the generate-all generated code.
	// See http://jira.codehaus.org/browse/GRAILS-3783 for why we have this shameful workaround...
	def study = assayInstance.parent
	study.addToAssays(assayInstance)

        if (assayInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'assay.label', default: 'Assay'), assayInstance.id])}"
            redirect(action: "show", id: assayInstance.id)
        }
        else {
            render(view: "create", model: [assayInstance: assayInstance])
        }
    }

    def show = {
        def assayInstance = Assay.get(params.id)
        if (!assayInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'assay.label', default: 'Assay'), params.id])}"
            redirect(action: "list")
        }
        else {
            [assayInstance: assayInstance]
        }
    }

    def edit = {
        def assayInstance = Assay.get(params.id)
        if (!assayInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'assay.label', default: 'Assay'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [assayInstance: assayInstance]
        }
    }

    def update = {
        def assayInstance = Assay.get(params.id)
        if (assayInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (assayInstance.version > version) {
                    
                    assayInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'assay.label', default: 'Assay')] as Object[], "Another user has updated this Assay while you were editing")
                    render(view: "edit", model: [assayInstance: assayInstance])
                    return
                }
            }
            assayInstance.properties = params
            if (!assayInstance.hasErrors() && assayInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'assay.label', default: 'Assay'), assayInstance.id])}"
                redirect(action: "show", id: assayInstance.id)
            }
            else {
                render(view: "edit", model: [assayInstance: assayInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'assay.label', default: 'Assay'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def assayInstance = Assay.get(params.id)
        if (assayInstance) {
            try {
                assayInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'assay.label', default: 'Assay'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'assay.label', default: 'Assay'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'assay.label', default: 'Assay'), params.id])}"
            redirect(action: "list")
        }
    }
}
