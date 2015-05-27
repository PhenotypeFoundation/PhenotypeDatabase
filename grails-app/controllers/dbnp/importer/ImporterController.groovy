package dbnp.importer

class ImporterController {
    static defaultAction = "chooseType"

    def authenticationService
    
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
