package dbnp.importer

class ImporterController {
    static defaultAction = "chooseType"
    
    /**
     * Shows the initial screen to select the type of importer
     */
    def chooseType() {
        def importerFactory = new ImporterFactory()
        [ importers: importerFactory.allImporters*.getIdentifier() ]
    }
    
    /**
     * Screen to upload the file and select the parameters
     */
    def upload() {
        def importerFactory = new ImporterFactory()
        def importerIdentifier = params.importer
        def importer = importerFactory.getImporter(importerIdentifier)
        
        if( !importer ) {
            flash.error = "The importer you specified cannot be found. Please select another importer from the list"
            redirect action: "index"
        }
        
    } 
    
}
