package dbnp.importer

import org.dbxp.matriximporter.MatrixImporter
import grails.converters.JSON

class ImporterController {
    static layout = "main"
    static defaultAction = "chooseType"

    def authenticationService
    def fileService
    def fuzzySearchService
    
    /**
     * Shows the initial screen to select the type of importer
     */
    def chooseType() {
        if( request.post && params.importer ) {
            doRedirect action: "upload", params: [ "importer": params.importer ]
            return
        }
        
        render( view: "/importer/chooseType", model: [ importers: getListOfImporters()*.identifier ])
    }
    
    /**
     * Screen to upload the file and select the parameters
     */
    def upload() {
        // If the user returns from the match page,
        // data is provided in the session. Parse that data as well
        def importInfo
        if( params.key ) {
            importInfo = getFromSession(params.key)
        } else {
            importInfo = parseUploadParams()
        }
        
        // See if an importer has been set
        def importer = getImporter( importInfo.importer )
        if( !importer ) {
            flash.error = "The importer you specified (" + importInfo.importer + ") cannot be found. Please select another importer from the list"
            doRedirect action: "chooseType", params: defaultParams
            return
        }
        
        // If any of the parameters has been set initially, store them
        // in the parameter value list. This enables calling the importer
        // with preset values (e.g. study id)
        def importerParameters = importer.getParameters(importInfo.initial)
        importerParameters.each { ImporterParameter parameter ->
            // Only store the initial value if the value has not been set by the user
            if( !importInfo.parameter || !importInfo.parameter[parameter.name] ) {
                // Only store the parameter if an initial value has been specified
                if( importInfo.initial && importInfo.initial[parameter.name] ) {
                    if( !importInfo.parameter ) {
                        importInfo.parameter = [:]
                    }
                    importInfo.parameter[parameter.name] = importInfo.initial[parameter.name]
                }
            }
        }
        
        // Handle form submission
        if( request.post && params.importer ) {
            // Only do something if a file has been specified
            if( params.file && params.file != "existing*" ) {
                def sessionKey = generateSessionKey()
                storeInSession(sessionKey, parseUploadParams())
                doRedirect action: 'match', params: defaultParams + [key: sessionKey]
                return
            } else {
                flash.error = "Please upload a valid file"
            }
        }
        
        render( view: "/importer/upload", model: [ 
            importer: importer,
            savedParameters: [
                file: importInfo.file,
                upload: importInfo.upload,
                parameter: importInfo.parameter,
                inital: importInfo.initial
            ],
            importerParameters: importerParameters
        ])
    }
    
    /**
     * Screen to match headers and upload the file and select the parameters
     */
    def match() {
        // Retrieve information from request and from session
        def sessionKey = params.key
        def importInfo = getFromSession(sessionKey)
        def importer = getImporter(importInfo.importer)
        
        // Check if data is correct
        if( !importInfo || !importer ) {
            flash.message = "A problem occurred while retrieving your parameters. Please restart the wizard."
            redirect action: "chooseType"
            return
        }
        
        if( request.post && params.key ) {
            switch( params._action ) {
                case 'previous':
                    doRedirect action: "upload", params: [key: sessionKey]
                    break
                case 'validate':
                case 'import':
                    // Parse parameters and store in session
                    importInfo.mapping = parseMappingParams(importer, importInfo)
                    
                    def data = parseFile(importInfo)
                    
                    if( validateMappingParameters( importInfo ) ) {
                        if( params._action == "validate" ) {
                            // Validate the provided data 
                            importer.validateData(data, importInfo.mapping, importInfo.parameter)
                        } else {
                            // Import the data and ignore entities that fail validation
                            importer.importData(data, importInfo.mapping, importInfo.parameter)
                        }
        
                        // Store provided parameters and validation result in session
                        importInfo.validationErrors = importer.getValidationErrors()
                        importInfo.numLines = data.size()
                        
                        storeInSession(sessionKey, importInfo)
    
                        // Redirect to the validation page
                        doRedirect action: ( params._action == "validate" ? "validation" : "finish" ), params: [key: sessionKey]
                        return
                    } else {
                        flash.error = "Please provide a valid set of mapping parameters. That includes at least one column of data and no duplicates."
                    }

                    break;
            }
        }
        
        // Parse the excel file to show an example
        def importedMatrix = parseFile(importInfo, 10)
        
        // Determine the headers to match against
        def headerOptions = importer.getHeaderOptions(importInfo.parameter)
        
        render( view: "/importer/match", model: [ 
            importer: importer, 
            importInfo: importInfo,
            matrix: importedMatrix, 
            headerOptions: headerOptions,
            sessionKey: sessionKey,
            savedMapping: importInfo.mapping?.collectEntries { [ (it.key): it.value?.field?.id ] }
        ])
    }
    
    /**
     * Shows a page with the result of validation
     */
    def validation() {
        // Retrieve information from request and from session
        def sessionKey = params.key
        def importInfo = getFromSession(sessionKey)
        def importer = getImporter(importInfo.importer)
        
        // Check if data is correct
        if( !importInfo || !importer ) {
            flash.message = "A problem occurred while retrieving your parameters. Please restart the wizard."
            redirect action: "chooseType"
            return
        }
        
        if( request.post && params.key ) {
            switch( params._action ) {
                case 'import':
                    // Import the data and ignore entities that fail validation
                    def data = parseFile(importInfo)
                    importer.importData(data, importInfo.mapping, importInfo.parameter)
    
                    // Store provided parameters and validation result in session
                    importInfo.validationErrors = importer.getValidationErrors()
                    importInfo.numLines = data.size()
                    storeInSession(sessionKey, importInfo)

                    // Redirect to the last page
                    doRedirect action: "finish", params: [key: sessionKey]

                    break;
            }
        }
        
        // Show validation errors
        render( view: "/importer/validation", model: [
            sessionKey: sessionKey,
            validationErrors: importInfo.validationErrors
        ])
    }
    
    /**
     * Shows a page with the result of importing
     */
    def finish() {
        // Retrieve information from request and from session
        def sessionKey = params.key
        def importInfo = getFromSession(sessionKey)
        def importer = getImporter(importInfo.importer)
        
        // Determine import results
        def groupedErrors = importInfo.validationErrors?.groupBy { it.line }
        def numLinesImported = importInfo.numLines - 1 - groupedErrors.size() 
        
        // Show results of importing
        render( view: "/importer/finish", model: [
            sessionKey: sessionKey,
            validationErrors: importInfo.validationErrors,
            groupedErrors: groupedErrors,
            numLinesImported: numLinesImported,
            
            resultLink: importer.getLinkToResults(importInfo.parameter),
            
            importInfo: importInfo
        ])
    }
    
    /**
     * AJAX Method to return a data preview, based on the uploaded file
     * @param importFileName Excel file to return as JSON
     * @param sheetIndex sheet to read
     */
    def datapreview() {
        // Parameters can be specified either by providing a session key
        // or by providing the parameters in the URL itself. Is a session 
        // key is given, it is used.
        def importInfo
        
        if( params.key ) {
            importInfo = getFromSession(params.key)
        } else {
            importInfo = parseUploadParams()
        }
        
        // Parse the file to show an example
        def importedMatrix = parseFile(importInfo, 10)
        
        if( !importedMatrix ) {
            log.error ".importer doesn't recognize the uploaded file, try a supported format like XLS(X)"
            response.status = 400
            render "Bad Request"
            return
        }
        
        if(importedMatrix.size() < 2) {
            log.error "Selected sheet from uploaded file doesn't contain at least two lines."
            response.status = 400
            render "Provided sheet should contain at least two lines"
            return
        }
        
        // Truncate each cells content to max 30 characters
        // This ensures the visibility of multiple rows, while the data can still be used to validate the settings
        def maxCharacters = 30
        for(def row = 1; row < importedMatrix.size(); row++) {
            for(def col = 0; col < importedMatrix[row].size(); col++) {
                def value = importedMatrix[row][col]
                
                // Only do the check for strings
                if(value instanceof String && value.size() > maxCharacters) {
                    importedMatrix[row][col] = value.substring(0, maxCharacters - 3) + "..."
                }
            }
        }
        
        // Convert the data into a header and the real data
        def data = [
            aoColumns: importedMatrix[0].collect { [sTitle: it ] },
            aaData: importedMatrix[1..-1]
        ]
        
        render data as JSON
    }
    
    /**
     * AJAX method to match 
     */
    def matchHeaders() {
        // Parse the file and retrieve only the headers
        def importInfo = getFromSession(params.key)
        def importedMatrix = parseFile(importInfo, 1)
        def fileHeaders = importedMatrix[0]
        
        // Retrieve the list of possible matches
        def importer = getImporter(importInfo.importer)
        def headerOptions = importer.getHeaderOptions(importInfo.parameter)
        
        // Perform the match itself
        def matches = fuzzySearchService.mostSimilarUnique( fileHeaders, headerOptions*.name );
        
        // Convert the data into a proper format, that can be used by the javascript
        // That is: a map with the key being the index of the header and the value being the value (=id) of the matched option
        def matchMap = [:]
        matches.eachWithIndex { match, idx ->
            matchMap[idx] = match.candidate ? headerOptions[ match.index ].id : null
        }
        
        render matchMap as JSON
    }
    
    
    /**
     * Returns a map with the parameters set by the user
     */
    protected def parseUploadParams() {
        // The file could be prefixed with 'existing*' due to the way
        // the ajaxupload widget works
        def filename = params.file?.replace( "existing*", "" )
        
        [
            file: filename,
            importer: params.importer,

            upload: [
                sheetIndex: params.int( 'upload.sheetIndex' ),
                dateFormat: params.upload?.dateFormat,
                headerRow: params.int( 'upload.headerRow' ),
                separator: params.upload?.separator
            ],
            parameter: params.parameter,
            initial: params.initial
        ]
    }
    
    /**
     * Returns a map with the parsed mapping parameters
     */
    
    protected def parseMappingParams(importer, importInfo) {
        def mappingParams = params.column?.match
        
        if( !mappingParams )
            return [:]
            
        // Loop through each parameter given
        def headerOptions = importer.getHeaderOptions(importInfo.parameter)
        def parameters = [:]
        
        mappingParams.sort { a,b -> a.key.toLong() <=> b.key.toLong() }.each { mapping ->
            parameters[mapping.key] = [
                columnNumber: mapping.key,
                ignore: !mapping.value,
                field: mapping.value ? headerOptions.find { it.id == mapping.value } : null
            ]
        }
        
        return parameters
    }
    
    /**
     * Validates the import parameters
     */
    protected def validateMappingParameters( parameters ) {
       if( !parameters.mapping )
           return false
       
       // Make sure that at least one column will be imported
       def mapping = parameters.mapping.values()
       def nonNullMapping = mapping.findAll { !it.ignore }
       
       if( !nonNullMapping ) 
           return false
       
       // Make sure that there are no duplicates in the mapping
       if( nonNullMapping*.field.id.unique().size() != nonNullMapping.size() ) 
           return false
           
       return true
    }
    
    /**
     * Parses a provided file, using the parameters given
     * If the file could not be found, a 404 error is given
     * @param data Map with information on the file and the way to parse it
     */
    protected def parseFile(data, numLines = 0) {
        // Retrieve the file
        def importedFile = fileService.get(data.file)
        
        if(!importedFile.exists()) {
            response.status = 404
            render "File not found"
            return
        }
        
        // Parse the separator that was given.
        def delimiter = data.upload.separator
        if( delimiter == "\\t" ) {
            delimiter = "\t"
        }

        // Read the start of the file using the matrix importer
        def importOptions = [
            delimiter: delimiter,
            sheetIndex: data.upload.sheetIndex,
            dateFormat: data.upload.dateFormat,
            startRow: data.upload.headerRow,
        ]
        
        // If a certain number of lines is provided, use that. Otherwise, load the whole file
        if( numLines ) {
            importOptions.endRow = data.upload.headerRow + numLines - 1
        }
        
        // Disable logging for apache poi
        System.setProperty("org.apache.poi.util.POILogger", "org.apache.commons.logging.impl.NoOpLog");
        
        log.debug "Start parsing file " + data.file
        def matrix = MatrixImporter.getInstance().importFile(importedFile, importOptions, false);
        log.debug "Finished parsing file " + data.file
        
        return matrix
    }
    
    /**
     * Returns a unique session key
     */
    protected generateSessionKey() {
        org.apache.commons.lang.RandomStringUtils.random(20, true, true)
    }
    
    /**
     * Stores a set of parameters in the session
     */
    protected storeInSession( String sessionKey, Map parameters ) {
        // Store parameters in session and pass the importKey
        // This effectively mimics the webflow functionality
        // but will prevent the overhead and errors coming with it
        // The only thing is to simply pass a simple set of parameters
        if( !session.importer ) 
            session.importer = [:]
            
        if( !session.importer.containsKey(sessionKey) )
            session.importer[sessionKey] = [:]
        
        session.importer[sessionKey] += parameters
    }
    
    /**
     * Returns a map of parameters from the session
     */
    protected getFromSession(String sessionKey) {
        if(!session.importer)
            return [:]
            
        session.importer[sessionKey] ?: [:]
    }
    
    /**
     * Returns an importer instance, based on the parameter specified in the request
     */
    protected AbstractImporter getImporterFromRequest() {
        getImporter(params.importer)
    }
    
    /**
     * Retrieves an importer, based on the identifier given
     */
    protected AbstractImporter getImporter(importerIdentifier) {
        def user = authenticationService.getLoggedInUser()
        def importerFactory = ImporterFactory.getInstance()
        return importerFactory.getImporter(importerIdentifier, user)
    }
    
    /**
     * Returns a list of importers to be shown in the chooseType step
     */
    protected def getListOfImporters() {
        def importerFactory = ImporterFactory.getInstance()
        importerFactory.allImporters
    }
    
    /**
     * Returns default parameters for each URL
     */
    protected Map getDefaultParams() {
        [:]
    }

    /**
     * Redirects the user, also using the default parameters
     */
    protected void doRedirect(parameters) {
        parameters.params = defaultParams + ( parameters.params ?: [:] )
        redirect(parameters) 
    }

    /**
     * Method to put the default parameters into the view
     */
    def afterInterceptor = { model ->
        model.defaultParams = defaultParams
    }
}
