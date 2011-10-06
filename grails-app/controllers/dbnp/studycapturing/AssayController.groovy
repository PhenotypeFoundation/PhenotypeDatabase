package dbnp.studycapturing

import grails.plugins.springsecurity.Secured

@Secured(['IS_AUTHENTICATED_REMEMBERED'])
class AssayController {

	def assayService
	def authenticationService
	def fileService

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

	def assayExportFlow = {
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
				if (!flow.assay) throw new Exception("No assay found with id: ${params.assayId}")

				// obtain fields for each category
				flow.fieldMap = assayService.collectAssayTemplateFields(flow.assay, null)

				flash.errorMessage = flow.fieldMap.remove('Module Error')
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

				// collect the assay data according to user selecting
				def assayData           = assayService.collectAssayData(flow.assay, fieldMapSelection, measurementTokens)

				flash.errorMessage      = assayData.remove('Module Error')

				flow.rowData            = assayService.convertColumnToRowStructure(assayData)

				// prepare the assay data preview
				def previewRows         = Math.min(flow.rowData.size()    as int, 5) - 1
				def previewCols         = Math.min(flow.rowData[0].size() as int, 5) - 1

				flow.assayDataPreview   = flow.rowData[0..previewRows].collect{ it[0..previewCols] as ArrayList }

				// store the selected file type in the flow
				flow.exportFileType = params.exportFileType

			}.to "compileExportData"

			on(Exception).to "handleError"
		}

		compileExportData {
			on ("ok"){
				session.rowData = flow.rowData
				session.exportFileType = flow.exportFileType
			}.to "export"
			on ("cancel").to "selectAssay"
		}

		export {
			redirect(action: 'doExport')
		}

		handleError() {
			render(view: 'errorPage')
		}
	}
	
	/**
	 * This method is called when a user wants to fetch data from GSCF
	 */
	def galaxyExport = {
		def galaxy_url = params.get( "GALAXY_URL" );
		def tool_id = params.get( "tool_id" );
		
		// Without galaxy url, we can't do anything
		if( !galaxy_url || !tool_id ) {
			redirect( action: "assayExport" );
		}
		
		def user            = authenticationService.getLoggedInUser()
		def userStudies    = Study.giveReadableStudies(user)
		
		[ galaxy_url: galaxy_url, tool_id: tool_id, userStudies: userStudies ]
	}
	
	def sendToGalaxy = {
		// Generate the url to redirect the user to
		def galaxy_url = params.get( "GALAXY_URL" );
		def tool_id = params.get( "tool_id" );
		
		// Without galaxy url, we can't do anything
		if( !galaxy_url || !tool_id  ) {
			redirect( action: "assayExport" );
		}
		
		def assayId = params.long( "assayId" );
		def assay = assayId ? Assay.get( assayId ) : null;
		
		// If no assay is given, return to galaxyExport screen
		if( !assay ) {
			redirect( action: "galaxyExport", params: [ "GALAXY_URL": galaxy_url ] );
		}
		
		// create a random session token that will be used to allow to module to
		// sync with gscf prior to presenting the measurement data
		def sessionToken = UUID.randomUUID().toString()
		def consumer = "galaxy";

		// put the session token to work
		authenticationService.logInRemotely( consumer, sessionToken, authenticationService.getLoggedInUser() )
		
		// Create a url where the data can be fetched
		def fetchUrl = g.createLink( absolute: true, controller: "assay", action: "fetchGalaxyData", params: [ assayToken: assay.assayUUID, sessionToken: sessionToken ] );
		
		// Generate url to redirect the user to
		def url = galaxy_url + "?tool_id=" + URLEncoder.encode( tool_id.toString(), "UTF-8" ) + "&URL=" + URLEncoder.encode( fetchUrl.toString(), "UTF-8" ) 
		
		log.trace "Redirecting galaxy user back to " + url;
		
		redirect( url: url );
	}
	
	// This method is accessible for each user. However, he should return with a valid
	// session token
	@Secured(['true'])
	def fetchGalaxyData = {
		// Check accessibility
		def consumer = "galaxy";
		def remoteUser = authenticationService.getRemotelyLoggedInUser( consumer, params.sessionToken );
		if( !remoteUser ) {
			response.status = 401;
			render "You must be logged in";
			return
		}

		// retrieve assay
		def assay = Assay.findByAssayUUID( params.assayToken );
		
		if( !assay ) {
			response.status = 404;
			render "No assay found";
			return
		}
		
		// Return assay data
		def fieldMap = assayService.collectAssayTemplateFields( assay, null, remoteUser )

		def measurementTokens = fieldMap.remove('Module Measurement Data');
		def assayData = assayService.collectAssayData(assay, fieldMap, measurementTokens, null, remoteUser)

		def rowData   = assayService.convertColumnToRowStructure(assayData)

		// Invalidate session token
		authenticationService.logOffRemotely( consumer, params.sessionToken );
				
		def outputDelimiter = '\t'
		def outputFileExtension = 'txt'

		def filename = "export.$outputFileExtension"
		response.setHeader("Content-disposition", "attachment;filename=\"${filename}\"")
		response.setContentType("application/octet-stream")
		try {
			assayService.exportRowWiseDataToCSVFile(rowData, response.outputStream, outputDelimiter, java.util.Locale.US)
		} catch (Exception e) {
			flash.errorMessage = e.message
			redirect action: 'errorPage'
		}
	}

	/**
	 * Export the row data in session.rowData to the outputStream of the http
	 * response.
	 */
	def doExport = {

		// make sure we're coming from the export flow, otherwise redirect there
		if (!(session.rowData && session.exportFileType))
			redirect(action: 'assayExportFlow')

		// process requested output file type
		def outputDelimiter, outputFileExtension, locale = java.util.Locale.US

		switch(session.exportFileType) {
			case '2': // Comma delimited csv
				outputDelimiter = ','
				outputFileExtension = 'csv'
				break
			case '3': // Semicolon delimited csv
				outputDelimiter = ';'
				outputFileExtension = 'csv'
				locale = java.util.Locale.GERMAN // force use of comma as decimal separator
				break
			default: // Tab delimited with .txt extension
				outputDelimiter = '\t'
				outputFileExtension = 'txt'
		}

		def filename = "export.$outputFileExtension"
		response.setHeader("Content-disposition", "attachment;filename=\"${filename}\"")
		response.setContentType("application/octet-stream")
		try {

			assayService.exportRowWiseDataToCSVFile(session.rowData, response.outputStream, outputDelimiter, locale)

			// clear the data from the session
			session.removeAttribute('rowData')
			session.removeAttribute('exportFileType')

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
		def assays = getAssaysFromParams( params );

		if( !assays )
			return;

		// Send headers to the browser so the user can download the file
		def filename = 'export.xlsx'
		response.setHeader("Content-disposition", "attachment;filename=\"${filename}\"")
		response.setContentType("application/octet-stream")

		try {
			// Loop through all assays to collect the data
			def rowWiseAssayData = [];

			assays.each { assay ->
				// Determine which fields should be exported for this assay
				def fieldMap = assayService.collectAssayTemplateFields(assay, null)
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
		def assays = getAssaysFromParams( params );

		if( !assays )
			return;

		// Send headers to the browser so the user can download the file
		def filename = 'export.csv'
		response.setHeader("Content-disposition", "attachment;filename=\"${filename}\"")
		response.setContentType("application/octet-stream")

		try {
			// Loop through all assays to collect the data
			def columnWiseAssayData = [];

			assays.each { assay ->
				// Determine which fields should be exported for this assay
				def fieldMap = assayService.collectAssayTemplateFields(assay, null)
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
			assayService.exportRowWiseDataToCSVFile( rowData, response.getOutputStream() )

			response.outputStream.flush()

		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Method to export one or more samples to csv in separate sheets.
	 *
	 * @param	params.ids		One or more sample ids to export
	 */
	def exportSamplesToCsv = {
		def samples = getSamplesFromParams( params );

		if( !samples ) {
			return;
		}

		// Determine a list of assays these samples have been involved in. That way, we can
		// retrieve the data for that assay once, and save precious time doing HTTP calls 
		def assays = [:];
		
		samples.each { sample ->
			def thisAssays = sample.getAssays();
			
			// Loop through all assays. If it already exists, add the sample it to the list
			thisAssays.each { assay ->
				if( !assays[ assay.id ] ) {
					assays[ assay.id ] = [ 'assay': assay, 'samples': [] ]
				}
				
				assays[ assay.id ].samples << sample
			}
		}
		
		// Now collect data for all assays
		try {
			// Loop through all assays to collect the data
			def columnWiseAssayData = [];

			assays.each { assayInfo ->
				def assay = assayInfo.value.assay;
				def assaySamples = assayInfo.value.samples;
				
				// Determine which fields should be exported for this assay
				def fieldMap = assayService.collectAssayTemplateFields(assay, null)
				def measurementTokens = fieldMap.remove('Module Measurement Data')
				
				// Retrieve row based data for this assay
				def assayData = assayService.collectAssayData( assay, fieldMap, measurementTokens, assaySamples );
				
				// Prepend study and assay data to the list
				assayData = assayService.prependAssayData( assayData, assay, assaySamples.size() )
				assayData = assayService.prependStudyData( assayData, assay, assaySamples.size() )
				
				// Make sure the assay data can be distinguished later
				assayData.put( "Assay data - " + assay.name, assayData.remove( "Assay Data") )
				assayData.put( "Module measurement data - " + assay.name, assayData.remove( "Module Measurement Data") )
				
				// Add the sample IDs to the list, in order to be able to combine
				// data for a sample that has been processed in multiple assays
				assayData[ "Sample Data" ][ "id" ] = assaySamples*.id;
				
				println "Assay data"
				assayData.each { println it }

				columnWiseAssayData << assayData;
			}
			
			def mergedColumnWiseData = assayService.mergeColumnWiseDataOfMultipleStudiesForASetOfSamples( columnWiseAssayData );

			def rowData   = assayService.convertColumnToRowStructure(mergedColumnWiseData)
			
			// Send headers to the browser so the user can download the file
			def filename = 'export.csv'
			response.setHeader("Content-disposition", "attachment;filename=\"${filename}\"")
			response.setContentType("application/octet-stream")
	
			assayService.exportRowWiseDataToCSVFile( rowData, response.getOutputStream() )

			response.outputStream.flush()

		} catch (Exception e) {
			throw e;
		}
	}


	def getAssaysFromParams( params ) {
		def ids = params.list( 'ids' ).findAll { it.isLong() }.collect { Long.valueOf( it ) };
		def tokens = params.list( 'tokens' );

		if( !ids && !tokens ) {
			flash.errorMessage = "No assay ids given";
			redirect( action: "errorPage" );
			return [];
		}

		// Find all assays for the given ids
		def assays = [];
		ids.each { id ->
			def assay = Assay.get( id );
			if( assay )
				assays << assay;
		}

		// Also accept tokens for defining studies
		tokens.each { token ->
			def assay = Assay.findByAssayUUID( token );
			if( assay )
				assays << assay;
		}

		if( !assays ) {
			flash.errorMessage = "No assays found";
			redirect( action: "errorPage" );
			return [];
		}

		return assays.unique();
	}

	def getSamplesFromParams( params ) {
		def ids = params.list( 'ids' ).findAll { it.isLong() }.collect { Long.valueOf( it ) };
		def tokens = params.list( 'tokens' );

		if( !ids && !tokens ) {
			flash.errorMessage = "No sample ids given";
			redirect( action: "errorPage" );
			return [];
		}

		// Find all assays for the given ids
		def samples = [];
		ids.each { id ->
			def sample = Sample.get( id );
			if( sample )
				samples << sample;
		}

		// Also accept tokens for defining studies
		tokens.each { token ->
			def sample = Sample.findBySampleUUID( token );
			if( sample )
				samples << sample;
		}

		if( !samples ) {
			flash.errorMessage = "No assays found";
			redirect( action: "errorPage" );
			return [];
		}

		return samples.unique();
	}
	
	def errorPage = {
		render(view: 'assayExport/errorPage')
	}
}
