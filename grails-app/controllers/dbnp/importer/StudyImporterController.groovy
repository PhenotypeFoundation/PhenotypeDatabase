package dbnp.importer

import org.dbxp.matriximporter.MatrixImporter
import grails.converters.JSON

class StudyImporterController extends ImporterController {
    static layout = "main"
    static defaultAction = "chooseType"
    
    /**
     * Returns a list of importers to be shown in the chooseType step
     */
    protected def getListOfImporters() {
        def importerFactory = ImporterFactory.getInstance()
        importerFactory.getImportersForType("study")
    }
    
}
