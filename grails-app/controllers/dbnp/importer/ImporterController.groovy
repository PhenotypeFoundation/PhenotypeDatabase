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
        
        [ importer: importer ]
    }
    
    /**
     * Method to return a data preview, based on the uploaded file
     * @param importFileName Excel file to return as JSON
     * @param sheetIndex sheet to read
     */
    def datapreview() {
        // Retrieve the file
        def importedFile = fileService.get(params.file)
        
        if(!importedFile.exists()) {
            response.status = 404
            render "File not found"
            return
        }
        
        // Parse the separator that was given. 
        def delimiter = params.upload.separator
        if( delimiter == "\\t" ) {
            delimiter = "\t"
        }

        // Read the start of the file using the matrix importer
        def importOptions = [
            delimiter: delimiter,
            sheetIndex: params.int('upload.sheetIndex'),
            dateFormat: params.upload.dateFormat,
            startRow: params.int('upload.headerRow'),
            endRow: params.int('upload.headerRow') + 10
        ]
        
        def importedMatrix = MatrixImporter.getInstance().importFile(importedFile, importOptions, false);
        
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
