/**
 * Api Controler
 *
 * API for third party applications to interact
 * with GSCF
 *
 * @author  Jeroen Wesbeek <work@osx.eu>
 * @since	20120328
 * @package api
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package api

import grails.plugins.springsecurity.Secured
import grails.converters.JSON
import dbnp.studycapturing.Study
import dbnp.studycapturing.Assay

class ApiController {
    def authenticationService
    def apiService

	/**
	 * index closure
	 */
    def index = {
        render(view:'index')
    }

    /**
     * authenticate with the api using HTTP_BASIC authentication
     *
     * This means
     * 1. the client should send the HTTP_BASIC authentication header
     *    which is an md5 hash of the username + password concatenated:
     *
     *    Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
     *
     * 2. the user used to authenticate with the API should have
     *    the ROLE_CLIENT role
     *
     * @param string deviceID
     */
    @Secured(['ROLE_CLIENT', 'ROLE_ADMIN'])
    def authenticate = {
        println "api::authenticate: ${params}"

        // see if we already have a token on file for this device id
        String deviceID = (params.containsKey('deviceID')) ? params.deviceID : ''
        def token = Token.findByDeviceID(deviceID)
        
        // generate a new token if we don't have a token on file
        def result = [:]
        try {
            // TODO - check if token belongs to current user?
            if (!token) {
                // generate a token for this device
                token = new Token(
                        deviceID    : params.deviceID,
                        deviceToken : UUID.randomUUID().toString(),
                        user        : authenticationService.getLoggedInUser(),
                        sequence    : 0
                ).save(failOnError: true)
            }
            
            result = ['token':token.deviceToken,'sequence':token.sequence]

            // set output headers
            response.status = 200
        } catch (Exception e) {
            // caught an error
            response.status = 500
            result = ['error':e.getMessage()]
        }

        response.contentType = 'application/json;charset=UTF-8'

        if (params.containsKey('callback')) {
            render "${params.callback}(${result as JSON})"
        } else {
            render result as JSON
        }
    }

    /**
     * get all readable studies
     *
     * @param string deviceID
     * @param string validation md5 sum
     */
    def getStudies = {
        println "api::getStudies: ${params}"

        String deviceID = (params.containsKey('deviceID')) ? params.deviceID : ''
        String validation = (params.containsKey('validation')) ? params.validation : ''

        // check
        if (!apiService.validateRequest(deviceID,validation)) {
            response.sendError(401, 'Unauthorized')
        } else {
            def user = Token.findByDeviceID(deviceID)?.user
            def readableStudies = Study.giveReadableStudies(user)
            def studies = []
            
            // iterate through studies and define resultset
            readableStudies.each { study ->
                // get result data
                studies[ studies.size() ] = [
                        'token'                 : study.getToken(),
                        'title'                 : study.title,
                        'description'           : study.description,
                        'subjects'              : study.subjects.size(),
                        'species'               : study.subjects.species.collect { it.name }.unique(),
                        'assays'                : study.assays.collect { it.name }.unique(),
                        'modules'               : study.assays.collect { it.module.name }.unique(),
                        'events'                : study.events.size(),
                        'uniqueEvents'          : study.events.collect { it.toString() }.unique(),
                        'samplingEvents'        : study.samplingEvents.size(),
                        'uniqueSamplingEvents'  : study.samplingEvents.collect { it.toString() }.unique(),
                        'eventGroups'           : study.eventGroups.size(),
                        'uniqueEventGroups'     : study.eventGroups.collect { it.name }.unique(),
                        'samples'               : study.samples.size()
                ]
            }

            def result = [
                    'count'     : studies.size(),
                    'studies'   : studies
            ]

            // set output headers
            response.status = 200
            response.contentType = 'application/json;charset=UTF-8'

            if (params.containsKey('callback')) {
                render "${params.callback}(${result as JSON})"
            } else {
                render result as JSON
            }
        }
    }

    /**
     * get all subjects for a study
     *
     * @param string deviceID
     * @param string studyToken
     * @param string validation md5 sum
     */
    def getSubjectsForStudy = {
        println "api::getSubjectsForStudy: ${params}"

        // fetch study
        String studyToken   = (params.containsKey('studyToken')) ? params.studyToken : ''
        def study           = Study.findByStudyUUID(studyToken)

        // wrap result in api call validator
        apiService.executeApiCall(params,response,'study',study,{
            def subjects = apiService.flattenDomainData( study.subjects )

            // define result
            def result = [
                    'count'     : subjects.size(),
                    'subjects'  : subjects
            ]

            // set output headers
            response.status = 200
            response.contentType = 'application/json;charset=UTF-8'

            if (params.containsKey('callback')) {
                render "${params.callback}(${result as JSON})"
            } else {
                render result as JSON
            }
        })
    }

    /**
     * get all assays for a study
     *
     * @param string deviceID
     * @param string studyToken
     * @param string validation md5 sum
     */
    def getAssaysForStudy = {
        println "api::getAssaysForStudy: ${params}"

        // fetch study
        String studyToken   = (params.containsKey('studyToken')) ? params.studyToken : ''
        def study           = Study.findByStudyUUID(studyToken)

        // wrap result in api call validator
        apiService.executeApiCall(params,response,'study',study,{
            def assays = apiService.flattenDomainData( study.assays )

            // define result
            def result = [
                    'count'     : assays.size(),
                    'assays'    : assays
            ]

            // set output headers
            response.status = 200
            response.contentType = 'application/json;charset=UTF-8'

            if (params.containsKey('callback')) {
                render "${params.callback}(${result as JSON})"
            } else {
                render result as JSON
            }
        })
    }

    /**
     * get all samples for an assay
     *
     * @param string deviceID
     * @param string assayToken
     * @param string validation md5 sum
     */
    def getSamplesForAssay = {
        println "api::getSamplesForAssay: ${params}"

        // fetch assay
        String assayToken   = (params.containsKey('assayToken')) ? params.assayToken : ''
        def assay           = Assay.findByAssayUUID(assayToken)

        // wrap result in api call validator
        apiService.executeApiCall(params,response,'assay',assay,{
            def samples = apiService.flattenDomainData( assay.samples )

            // define result
            def result = [
                    'count'     : samples.size(),
                    'samples'   : samples
            ]

            // set output headers
            response.status = 200
            response.contentType = 'application/json;charset=UTF-8'

            if (params.containsKey('callback')) {
                render "${params.callback}(${result as JSON})"
            } else {
                render result as JSON
            }
        })
    }

    /**
     * get all measurement data from a linked module for an assay
     *
     * @param string deviceID
     * @param string assayToken
     * @param string validation md5 sum
     */
    def getMeasurementDataForAssay = {
        println "api:getMeasurementDataForAssay: ${params}"

        // fetch assay
        String assayToken   = (params.containsKey('assayToken')) ? params.assayToken : ''
        def assay           = Assay.findByAssayUUID(assayToken)

        // fetch user based on deviceID
        String deviceID     = (params.containsKey('deviceID')) ? params.deviceID : ''
        def user            = Token.findByDeviceID(deviceID)?.user

        // wrap result in api call validator
        apiService.executeApiCall(params,response,'assay',assay,{
            // define sample measurement data matrix
            def matrix = [:]
            def measurementData = apiService.getMeasurementData(assay, user)
            //def measurementMetaData = apiService.getMeasurementData(assay, user)

            // iterate through measurementData and build data matrix
            try {
                measurementData.each { data ->
                    if (!matrix.containsKey(data.sampleToken)) matrix[data.sampleToken] = [:]
                    matrix[data.sampleToken][data.measurementToken] = data.value
                }

                // define result
                def result = [
                        'count'         : matrix.size(),
                        'measurements'  : matrix
                ]

                // set output headers
                response.status = 200
                response.contentType = 'application/json;charset=UTF-8'

                if (params.containsKey('callback')) {
                    render "${params.callback}(${result as JSON})"
                } else {
                    render result as JSON
                }
            } catch (Exception e) {
                println "getMeasurementDataForAssay exception: ${e.getMessage()}"
                response.sendError(500, "module '${assay.module}' does not properly implement getMeasurementData REST specification (${e.getMessage()})")
            }
        })
    }

    // ---- debugging -----

    def moduleCommunicationService
    def debugModuleDataForAssay = {
        println "api:debugModuleDataForAssay: ${params}"

        // fetch assay
        String assayToken   = (params.containsKey('assayToken')) ? params.assayToken : ''
        def assay           = Assay.findByAssayUUID(assayToken)

        // fetch user based on deviceID
        String deviceID     = (params.containsKey('deviceID')) ? params.deviceID : ''
        def user            = Token.findByDeviceID(deviceID)?.user

        // wrap result in api call validator
        apiService.executeApiCall(params,response,'assay',assay,{
            def serviceURL = "${assay.module.url}/rest/getMeasurementData"
            def serviceArguments = "assayToken=${assay.assayUUID}&verbose=false"

            // call module method
            def json = moduleCommunicationService.callModuleMethod(
                    assay.module.url,
                    serviceURL,
                    serviceArguments,
                    "POST",
                    user
            );

            // define result
            def result = [
                    'measurements'              : apiService.getMeasurements(assay, user),
                    'verboseMeasurementData'    : apiService.getMeasurementData(assay, user),
                    'nonVerboseMeasurementData' : json,
                    'measurementMetaData'       : apiService.getMeasurementMetaData(assay, user)
            ]

            // set output headers
            response.status = 200
            response.contentType = 'application/json;charset=UTF-8'

            if (params.containsKey('callback')) {
                render "${params.callback}(${result as JSON})"
            } else {
                render result as JSON
            }
        })
    }
}