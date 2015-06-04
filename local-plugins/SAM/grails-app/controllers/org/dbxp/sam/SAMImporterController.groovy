package org.dbxp.sam

class SAMImporterController extends dbnp.importer.ImporterController {
    static layout = "sammain"
    
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