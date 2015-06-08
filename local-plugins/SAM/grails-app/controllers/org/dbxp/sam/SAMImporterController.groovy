package org.dbxp.sam

import dbnp.importer.*

class SAMImporterController extends ImporterController {
    static layout = "sammain"
    
    /**
     * Returns a list of importers to be shown in the chooseType step
     * For SAM, this includes only the SAM importers
     */
    protected def getListOfImporters() {
        def importerFactory = ImporterFactory.getInstance()
        importerFactory.getImportersForType( "SAM")
    }

    /**
     * Make sure that the module is used as the initial setting for the parameter
     */
    def beforeInterceptor = {
        if( !params.initial )
            params.initial = [:]
            
        params.initial.module = params.module 
    }
    
        
    /**
     * Make sure that the module is passed to the view, as well as the default parameters
     */
    def afterInterceptor = { model ->
        model.module = params.module
        model.defaultParams = defaultParams
    }
    
    /**
     * Returns default parameters for each URL
     */
    protected Map getDefaultParams() {
        [module: params.module]
    }

}