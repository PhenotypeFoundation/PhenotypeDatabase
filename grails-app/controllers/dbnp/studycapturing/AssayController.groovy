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
				flow.userStudies    = Study.giveReadableStudies(user)
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

				flow.measurementTokens = flow.fieldMap.remove('Module Measurement Data')
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

				def measurementTokens = []

				if (params."cat_4" == 'on') {
					measurementTokens = params.list( "measurementToken" )
				}

				def assayData           = assayService.collectAssayData(flow.assay, fieldMapSelection, measurementTokens)
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

	/**
	 * Method to export one or more assays to excel in separate sheets.
	 * 
	 * @param	params.ids		One or more assay IDs to export
	 * @param	params.format	"list" in order to export all assays in one big excel sheet
	 * 							"sheets" in order to export every assay on its own sheet (default)
	 */
	def exportToExcel = {
		def format = params.get( 'format', 'sheets' );
		if( format == 'list' ) {
			exportToExcelAsList( params );
		} else {
			exportToExcelAsSheets( params );
		}
	}

	/**
	 * Method to export one or more assays to excel in separate sheets.
	 * 
	 * @param	params.ids		One or more assay IDs to export
	 */
	def exportToExcelAsSheets = {
		def ids = params.list( 'ids' ).findAll { it.isLong() }.collect { Long.valueOf( it ) };

		if( !ids ) {
			flash.errorMessage = "No assay ids given";
			redirect( action: "errorPage" );
			return;
		}

		// Find all assays for the given ids
		def assays = ids.unique().collect { id -> Assay.get( id ) }.findAll { it }

		// Send headers to the browser so the user can download the file
		def filename = 'export.xlsx'
		response.setHeader("Content-disposition", "attachment;filename=\"${filename}\"")
		response.setContentType("application/octet-stream")

		try {
			// Loop through all assays to collect the data
			def rowWiseAssayData = [];

			assays.each { assay ->
				// Determine which fields should be exported for this assay
				def fieldMap = assayService.collectAssayTemplateFields(assay)
				def measurementTokens = fieldMap.remove('Module Measurement Data')

				// Retrieve row based data for this assay
				def assayData = assayService.collectAssayData( assay, fieldMap, measurementTokens );
				def rowData   = assayService.convertColumnToRowStructure(assayData)

				// Put each assay on another sheet
				rowWiseAssayData << rowData;
			}

			assayService.exportRowWiseDataForMultipleAssaysToExcelFile( rowWiseAssayData, response.getOutputStream() )

			response.outputStream.flush()

		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Method to export one or more assays to excel.
	 *
	 * @param	params.ids		One or more assay IDs to export
	 */
	def exportToExcelAsList = {
		def ids = params.list( 'ids' ).findAll { it.isLong() }.collect { Long.valueOf( it ) };

		if( !ids ) {
			flash.errorMessage = "No assay ids given";
			redirect( action: "errorPage" );
			return;
		}

		// If only 1 assay is asked for, don't bother with merging multiple assays.
		// In that case just use the export method to export one assay per sheet
		if( ids.size() == 1 )
			return exportToExcelAsSheets( params );

		// Find all assays for the given ids
		def assays = ids.unique().collect { id -> Assay.get( id ) }.findAll { it }

		// Send headers to the browser so the user can download the file
		def filename = 'export.xlsx'
		response.setHeader("Content-disposition", "attachment;filename=\"${filename}\"")
		response.setContentType("application/octet-stream")

		try {
			// Loop through all assays to collect the data
			def columnWiseAssayData = [];

			assays.each { assay ->
				// Determine which fields should be exported for this assay
				def fieldMap = assayService.collectAssayTemplateFields(assay)
				def measurementTokens = fieldMap.remove('Module Measurement Data')

				// Retrieve row based data for this assay
				def assayData = assayService.collectAssayData( assay, fieldMap, measurementTokens );
				
				// Prepend study and assay data to the list
				assayData = assayService.prependAssayData( assayData, assay, assay.samples?.size() )
				assayData = assayService.prependStudyData( assayData, assay, assay.samples?.size() )
				
				// Put each assay on another sheet
				columnWiseAssayData << assayData;
			}
			
			// Merge data from all assays
			def mergedColumnWiseData = assayService.mergeColumnWiseDataOfMultipleStudies( columnWiseAssayData );

			def rowData   = assayService.convertColumnToRowStructure(mergedColumnWiseData)
			assayService.exportRowWiseDataToExcelFile( rowData, response.getOutputStream() )

			response.outputStream.flush()

		} catch (Exception e) {
			throw e;
		}
	}


	def errorPage = {
		render(view: 'excelExport/errorPage')
	}
}
