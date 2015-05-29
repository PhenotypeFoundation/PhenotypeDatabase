package dbnp.importer

import org.dbxp.matriximporter.MatrixImporter
import grails.converters.JSON

class ImporterController {
    static defaultAction = "chooseType"

    def authenticationService
    def fileService
    
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
                redirect action: 'match', params: [key: sessionKey, importer: params.importer]
            }
        }
        
        [ importer: importer ]
    }
    
    /**
     * Screen to match headers and upload the file and select the parameters
     */
    def match() {
        def importer = getImporterFromRequest()
        def importInfo = getFromSession(params.key)
        
        def importedMatrix = parseFile(importInfo)
        
        [ importer: importer, matrix: importedMatrix, sessionKey: params.sessionKey ]
    }
    
    /**
     * Method to return a data preview, based on the uploaded file
     * @param importFileName Excel file to return as JSON
     * @param sheetIndex sheet to read
     */
    def datapreview() {
        def importedMatrix = parseFile(parseParams())
        
        if( !importedMatrix ) {
            log.error ".importer doesn't recognize the uploaded file, try a supported format like XLS(X)"
            response.status = 400
            render "Bad Request"
            return
        }
        
        // Convert the data into a header and the real data
        def header = []
        importedMatrix[0].size().times { header << [sTitle: getExcelColumnName(it) ] }
        def data = [
            aoColumns: importedMatrix[0].collect { [sTitle: it ] },
            aaData: importedMatrix[1..-1]
        ]
        
        render data as JSON
    }
    
    /**
     * Returns a map with the parameters set by the user
     */
    def parseParams() {
        [
            file: params.file,

            upload: [
                sheetIndex: params.int( 'upload.sheetIndex' ),
                dateFormat: params.upload.dateFormat,
                headerRow: params.int( 'upload.headerRow' )
            ],
            parameter: params.parameter
        ]
    }
    
    /**
     * Parses a provided file, using the parameters given
     * If the file could not be found, a 404 error is given
     * @param data Map with information on the file and the way to parse it
     */
    def parseFile(data) {

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
            endRow: data.upload.headerRow + 10
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
     * Return excel column name for example data matrix
     */
    protected String getExcelColumnName (int columnNumber)
    {
        int dividend = columnNumber;
        int i;
        String columnName = "";
        int modulo;
        while (dividend > 0)
        {
            modulo = (dividend - 1) % 26;
            i = 65 + modulo;
            columnName = new Character((char)i).toString() + columnName;
            dividend = (int)((dividend - modulo) / 26);
        }
        return columnName;
    }
    
    protected Importer getImporterFromRequest() {
        def user = authenticationService.getLoggedInUser()
        def importerFactory = new ImporterFactory()
        def importerIdentifier = params.importer
        def importer = importerFactory.getImporter(importerIdentifier, user)

        if( !importer ) {
            flash.error = "The importer you specified (" + importerIdentifier + ") cannot be found. Please select another importer from the list"
            redirect action: "chooseType"
        }
        
        return importer
    }
    
}
