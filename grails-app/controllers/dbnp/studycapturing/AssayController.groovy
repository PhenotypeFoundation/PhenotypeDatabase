package dbnp.studycapturing

class AssayController {

    def assayService
    def authenticationService
    
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

	def showByToken = {
	    def assayInstance = Assay.findByAssayUUID(params.id)
	    if (!assayInstance) {
	        flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'assay.label', default: 'Assay'), params.id])}"
	        redirect(action: "list")
	    }
	    else {
		    redirect(action: "show", id: assayInstance.id)
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

    def excelExportFlow = {
        entry {
            action{
                def user            = authenticationService.getLoggedInUser()
                flow.userStudies    = Study.findAllByOwner(user)
            }
            on("success").to "selectAssay"
        }

        selectAssay {
            on ("submit"){
                flow.assay = Assay.get(params.assayId)

                // check if assay exists
                if (!flow.assay) throw new Exception("No assay found with id: ${flow.assay.id}")

                // obtain fields for each category
                flow.fieldMap = assayService.collectAssayTemplateFields(flow.assay)

                flow.measurementTokens = flow.fieldMap.remove('Module Measurement Data')*.name
            }.to "selectFields"

            on(Exception).to "handleError"
        }

        selectFields {
            on ("submit"){
                def fieldMapSelection = [:]

                flow.fieldMap.eachWithIndex { cat, cat_i ->

                    if (params."cat_$cat_i" == 'on') {
                        fieldMapSelection[cat.key] = []

                        cat.value.eachWithIndex { field, field_i ->

                            if (params."cat_${cat_i}_${field_i}" == 'on')
                                fieldMapSelection[cat.key] += field
                        }

                        if (fieldMapSelection[cat.key] == [])
                            fieldMapSelection.remove(cat.key)
                    }
                }

                def measurementTokensSelection = []

                if (params."cat_4" == 'on') {

                    def measurementToken = params.measurementToken

                    if (measurementToken)
                        if (measurementToken instanceof String)
                            measurementTokensSelection = [[name: measurementToken]]
                        else
                            measurementTokensSelection = measurementToken.collect{[name: it]}
                }

                def assayData           = assayService.collectAssayData(flow.assay, fieldMapSelection, measurementTokensSelection)
                flow.rowData            = assayService.convertColumnToRowStructure(assayData)
                flow.assayDataPreview   = flow.rowData[0..4].collect{ it[0..4] as ArrayList }

            }.to "compileExportData"

            on(Exception).to "handleError"
        }

        compileExportData {
            on ("ok"){session.rowData = flow.rowData}.to "export"
            on ("cancel").to "selectAssay"
        }

        export {
            redirect(action: 'doExport')
        }

        handleError() {
            render(view: 'errorPage')
        }
    }

    def doExport = {

        def filename = 'export.xlsx'
        response.setHeader("Content-disposition", "attachment;filename=\"${filename}\"")
        response.setContentType("application/octet-stream")
        try {

            assayService.exportRowWiseDataToExcelFile(session.rowData, response.outputStream)
            response.outputStream.flush()

        } catch (Exception e) {

            flash.errorMessage = e.message
            redirect action: 'errorPage'

        }
    }

    def errorPage = {
        render(view: 'excelExport/errorPage')
    }
}
