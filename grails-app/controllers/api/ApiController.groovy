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
import dbnp.authentication.SecUser
import dbnp.studycapturing.*
import org.dbnp.gdt.*

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
        SecUser user    = authenticationService.getLoggedInUser()
        Token token     = Token.findByDeviceID(deviceID)
        
        // generate a new token if we don't have a token on file
        def result = [:]
        try {
            if (!token) {
                // generate a token for this device
                token = new Token(
                        deviceID    : deviceID,
                        deviceToken : UUID.randomUUID().toString(),
                        user        : user,
                        sequence    : 0
                ).save(flush: true)

                // create result
                response.status = 200
                result = ['token':token.deviceToken, 'sequence':token.sequence]
            } else if (user != token.user) {
                response.status = 409
                result = ['error':"the deviceID '${deviceID}' is already in use by user '${token.user}', please use user '${token.user}' to authenticate or use another deviceID"]
            } else {
                result = ['token':token.deviceToken, 'sequence':token.sequence]

                // set output headers
                response.status = 200
            }
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
                        'token'                 : study.giveUUID(),
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
	    def study           = Study.findWhere(UUID: studyToken)

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
	    def study           = Study.findWhere(UUID: studyToken)

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
     * get all eventGroups for a study
     *
     * @param string deviceID
     * @param string studyToken
     * @param string validation md5 sum
     */
    def getEventGroupsForStudy = {
        println "api::getEventGroupsForStudy: ${params}"

        // fetch study
        String studyToken   = (params.containsKey('studyToken')) ? params.studyToken : ''
	    def study           = Study.findWhere(UUID: studyToken)

	    // wrap result in api call validator
        apiService.executeApiCall(params,response,'study',study,{
            def eventGroups = apiService.flattenDomainData( study.eventGroups )

            // define result
            def result = [
                'count'         : eventGroups.size(),
                'eventGroups'   : eventGroups
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
     * get all events for a study
     *
     * @param string deviceID
     * @param string studyToken
     * @param string validation md5 sum
     */
    def getEventsForStudy = {
        println "api::getEventsForStudy: ${params}"

        // fetch study
        String studyToken   = (params.containsKey('studyToken')) ? params.studyToken : ''
	    def study           = Study.findWhere(UUID: studyToken)

	    // wrap result in api call validator
        apiService.executeApiCall(params,response,'study',study,{
            def events = apiService.flattenDomainData( study.events )

            // define result
            def result = [
                'count' : events.size(),
                'events': events
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
     * get all samplingEvents for a study
     *
     * @param string deviceID
     * @param string studyToken
     * @param string validation md5 sum
     */
    def getSamplingEventsForStudy = {
        println "api::getSamplingEventsForStudy: ${params}"

        // fetch study
        String studyToken   = (params.containsKey('studyToken')) ? params.studyToken : ''
	    def study           = Study.findWhere(UUID: studyToken)

	    // wrap result in api call validator
        apiService.executeApiCall(params,response,'study',study,{
            def samplingEvents = apiService.flattenDomainData( study.samplingEvents )
            println study.samplingEvents.dump()

            // define result
            def result = [
                    'count'         : samplingEvents.size(),
                    'samplingEvents': samplingEvents
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
	 * get all subjects for a study
	 *
	 * @param string deviceID
	 * @param string studyToken
	 * @param string validation md5 sum
	 */
	def getSamplesForStudy = {
		println "api::getSamplesForStudy: ${params}"

		// fetch study
		String studyToken   = (params.containsKey('studyToken')) ? params.studyToken : ''
		def study           = Study.findWhere(UUID: studyToken)

		// wrap result in api call validator
		apiService.executeApiCall(params,response,'study',study,{

			def studySamples = study.samples

			def samples = apiService.flattenDomainData( studySamples )

			// add info on parent subjects, events etc.
			samples.each { item ->
				println item.token
				Sample sample = studySamples.find { it.UUID == item.token }
				item['subject'] = sample.parentSubject.UUID
				item['samplingEvent'] = sample.parentEvent.id
				item['eventGroup'] = sample.parentEventGroup.id
			}

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
	    def assay           = Assay.findWhere(UUID: assayToken)

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
        println "api::getMeasurementDataForAssay: ${params}"

        // fetch assay
        String assayToken   = (params.containsKey('assayToken')) ? params.assayToken : ''
	    def assay           = Assay.findWhere(UUID: assayToken)

	    // fetch user based on deviceID
        String deviceID     = (params.containsKey('deviceID')) ? params.deviceID : ''
        def user            = Token.findByDeviceID(deviceID)?.user

        // wrap result in api call validator
        apiService.executeApiCall(params,response,'assay',assay,{
            // define sample measurement data matrix
            def matrix = [:]
            def measurementData = apiService.getMeasurementData(assay, user).toArray()
            //def measurementMetaData = apiService.getMeasurementData(assay, user)

            // iterate through measurementData and build data matrix
            try {
                measurementData.each { data ->
                    try {
                        if (!matrix.containsKey(data.sampleToken)) matrix[data.sampleToken] = [:]
                        matrix[data.sampleToken][data.measurementToken] = data.value
                    } catch (Exception e) {
                        // it seems that some measurement data does not contain a sample token?
                        println "getMeasurementDataForAssay error for data of assay '${assay.name}' (token '${assayToken}', module: '${assay.module.name}'): ${e.getMessage()}"
                        println data.dump()
                    }
                }

                // define result
                def result = [:]
                result = [
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

	/**
	 * get all modules connected to this GSCF instance
	 *
	 * @param string deviceID
	 * @param string validation md5 sum
	 */
	def getModules = {
		println "api::getModules: ${params}"

		// get all modules
		def modules = AssayModule.findAll()

		// wrap in api call validator
		apiService.executeApiCall(params, response, 'modules', modules, {
			def result2 = apiService.flattenDomainData(modules)
			def result = [:]
			modules.each {
				result[ result.size() ] = [
				        'name'  : it.name,
						'url'   : it.url,
						'token' : 'to be completed...'
				]
			}

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
	 * get all domain classes that extend GDT's TemplateEntity (entities)
	 *
	 * @param string deviceID
	 * @param string validation md5 sum
	 */
	def getEntityTypes = {
		println "api::getEntityTypes: ${params}"

		// list of entities
		def entities = apiService.getEntities().keySet()

		// wrap result in api call validator
		apiService.executeApiCall(params, response, 'entities', entities, {
			// set output headers
			response.status = 200
			response.contentType = 'application/json;charset=UTF-8'

			if (params.containsKey('callback')) {
				render "${params.callback}(${entities as JSON})"
			} else {
				render entities as JSON
			}
		})
	}

	/**
	 * get all templates for a specific entity
	 *
	 * @param string deviceID
	 * @param string validation md5 sum
	 * @param string entityType
	 */
	def getTemplatesForEntity = {
		println "api::getTemplatesForEntity: ${params}"

		def result = [:]
		String entityType = (params.containsKey('entityType')) ? params.get('entityType') : ''

		try {
			def entity = apiService.getEntity(entityType)
			def templates = Template.findAllByEntity(entity)

			// wrap result in api call validator
			apiService.executeApiCall(params, response, 'templates', templates, {
				// set output headers
				response.status = 200
				response.contentType = 'application/json;charset=UTF-8'

				result = ['templates': apiService.flattenDomainData(templates,['id'])]

				if (params.containsKey('callback')) {
					render "${params.callback}(${result as JSON})"
				} else {
					render result as JSON
				}
			})
		} catch (Exception e) {
			println "getTemplatesForEntity exception: ${e.getMessage()}"
			response.sendError(500, "unknown error occured (${e.getMessage()})")
		}
	}

	/**
	 * get all fields for a specific entity and optionally a template
	 *
	 * @param string deviceID
	 * @param string validation md5 sum
	 * @param string entityType
	 * @param string templateToken
	 */
	def getFieldsForEntityWithTemplate = {
		println "api::getFieldsForEntityWithTemplate: ${params}"

		def result = [:]
		String entityType = (params.containsKey('entityType')) ? params.get('entityType') : ''
		String templateToken = (params.containsKey('templateToken')) ? params.get('templateToken') : ''

		if (entityType) {
			try {
				// instantiate entity
				def entity = apiService.getEntity(entityType)
				def entityInstance = entity.newInstance()

				// got a template?
				if (templateToken) {
					def template = Template.findWhere(UUID: templateToken)

					// was a valid template specified?
					if (template && entity.equals(template.entity)) {
						// set template
						entityInstance.setTemplate(template)
					} else {
						throw new Exception("invalid template token specified, call getTemplatesForEntity(${entityType}) for a list of valid templates")
					}


					// wrap result in api call validator
					apiService.executeApiCall(params, response, entityType, entityInstance, {
						// set output headers
						response.status = 200
						response.contentType = 'application/json;charset=UTF-8'

						// gather data
						def fields = []
						def requiredFields = entityInstance.getRequiredFields().collect { it.name }

						// gather fields
						entityInstance.giveFields().each { field ->
							def flattenedField = apiService.flattenTemplateField(field)

							flattenedField.required = requiredFields.contains(flattenedField.name)

							fields.add(flattenedField)
						}

						// fetch all fields
						result = [
								'fields'        : fields,
								'requiredFields': requiredFields
						]

						if (params.containsKey('callback')) {
							render "${params.callback}(${result as JSON})"
						} else {
							render result as JSON
						}
					})
				}
			} catch (Exception e) {
				response.sendError(500, "unknown error occured (${e.getMessage()})")
			}
		} else {
			response.sendError(400, "entityType is missing")
		}
	}

	/**
	 * Implementation of RFC 2324
	 */
	def teapot = {
		// ask, and the Mad Hatter will reply...
		response.sendError(418, "'Twas brillig, and the slithy toves Did gyre and gimble in the wabe: All mimsy were the borogoves, And the mome raths outgrabe.")
	}
}