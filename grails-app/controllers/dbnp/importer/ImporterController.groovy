package dbnp.importer

import org.dbxp.matriximporter.MatrixImporter
import grails.converters.JSON

class ImporterController {
    static defaultAction = "chooseType"

    def authenticationService
    def fileService
    def fuzzySearchService
    
    /**
     * Shows the initial screen to select the type of importer
     */
    def chooseType() {
        if( request.post && params.importer ) {
            redirect action: "upload", params: [ "importer": params.importer ]
        }
        
        def importerFactory = new ImporterFactory()
        [ importers: importerFactory.allImporters*.getIdentifier() ]
    }
    
    /**
     * Screen to upload the file and select the parameters
     */
    def upload() {
        def importer = getImporterFromRequest()
        
        if( request.post && params.importer ) {
            // Only do something if a file has been specified
            if( params.file && params.file != "existing*" ) {
                def sessionKey = generateSessionKey()
                storeInSession(sessionKey, parseParams())
                redirect action: 'match', params: [key: sessionKey]
            }
        }
        
        [ importer: importer ]
    }
    
    /**
     * Screen to match headers and upload the file and select the parameters
     */
    def match() {
        // Retrieve information from request and from session
        def importInfo = getFromSession(params.key)
        def importer = getImporter(importInfo.importer)
        
        // Parse the excel file
        def importedMatrix = parseFile(importInfo)
        
        // Determine the headers to match against
        def headerOptions = importer.getHeaderOptions(importInfo.parameter)
        
        [ 
            importer: importer, 
            importInfo: importInfo,
            matrix: importedMatrix, 
            headerOptions: headerOptions,
            sessionKey: params.key 
        ]
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
            importInfo = parseParams()
        }
        
        def importedMatrix = parseFile(importInfo)
        
        if( !importedMatrix ) {
            log.error ".importer doesn't recognize the uploaded file, try a supported format like XLS(X)"
            response.status = 400
            render "Bad Request"
            return
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
        def importedMatrix = parseFile(importInfo, 0)
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
    protected def parseParams() {
        [
            file: params.file,
            importer: params.importer,

            upload: [
                sheetIndex: params.int( 'upload.sheetIndex' ),
                dateFormat: params.upload.dateFormat,
                headerRow: params.int( 'upload.headerRow' ),
            ],
            parameter: params.parameter
        ]
    }
    
    /**
     * Parses a provided file, using the parameters given
     * If the file could not be found, a 404 error is given
     * @param data Map with information on the file and the way to parse it
     */
    protected def parseFile(data, numLines = 10) {

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
            endRow: data.upload.headerRow + numLines
        ]
        
        MatrixImporter.getInstance().importFile(importedFile, importOptions, false);
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
        session.importer[sessionKey] ?: [:]
    }
    
    /**
     * Returns an importer instance, based on the parameter specified in the request
     */
    protected Importer getImporterFromRequest() {
        def importer = getImporter(params.importer)

        if( !importer ) {
            flash.error = "The importer you specified (" + importerIdentifier + ") cannot be found. Please select another importer from the list"
            redirect action: "chooseType"
        }
        
        return importer
    }
    
    /**
     * Retrieves an importer, based on the identifier given
     */
    protected getImporter(importerIdentifier) {
        def user = authenticationService.getLoggedInUser()
        def importerFactory = new ImporterFactory()
        return importerFactory.getImporter(importerIdentifier, user)
    }
    
}
