package org.dbxp.sam

class SAMIdmapperController {
    def grailsApplication

    static defaultAction = 'listPlatforms'

    def listPlatforms() {

        def apiKeyConfigured = false
        if ( grailsApplication.config.idmapper.apikey ) {
            apiKeyConfigured = true
        }

        def platformList = Platform.findAllByPlatformtype( params.module )

        [ platformList: platformList, module: params.module, apiKeyConfigured: apiKeyConfigured ]
    }

    def listFeatures() {

        def featureList = Feature.findAllByPlatform( Platform.read( params.platformId ) )

        [ featureList: featureList, ontology: params.ontology, module: params.module, apiKey: grailsApplication.config.idmapper.apikey ]
    }

    def submitFeatures() {

        def features = []

        params.each() { param ->
            if ( !['controller','action','module'].contains(param.key) && param.key.isNumber() && param.value ) {
                def feature = Feature.get(param.key as Long)
                
                feature.externalIdentifier = param.value
                feature.save()

                features << feature
            }
        }

        [ featureList: features, module: params.module ]
    }
}