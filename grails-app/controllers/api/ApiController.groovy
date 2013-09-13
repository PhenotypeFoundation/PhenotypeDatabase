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
import org.codehaus.groovy.grails.web.json.JSONObject
import org.dbnp.gdt.*
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib

class ApiController {
    def authenticationService
    def apiService
    def dataSource
	def validationTagLib = new ValidationTagLib()

	/**
	 * index closure
	 */
    def index = {
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
                        'token'                 : study.UUID,
                        'code'                  : study.code,
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
                        matrix[data.sampleToken][data.measurementName] = data.value
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
     * get all measurement data from a linked module for an assay
     *
     * @param string deviceID
     * @param string assayToken
     * @param string validation md5 sum
     */
    def getPlainMeasurementDataForAssay = {
        println "api::getPlainMeasurementDataForAssay: ${params}"

        // fetch output parameter, features: feature metadata, subject: subject metadata
        // measurements: subjectname, starttime, featurename, value, all: all (default)
        String outputOptions = ['all', 'measurements', 'subjects', 'features']
        String output = params.containsKey('dataSelection') ? params.dataSelection : ''

        if(!outputOptions.contains(output)) {
            output = "all"
        }

        // fetch assay
        String assayToken   = (params.containsKey('assayToken')) ? params.assayToken : ''
        def assay           = Assay.findWhere(UUID: assayToken)

        // fetch user based on deviceID
        String deviceID     = (params.containsKey('deviceID')) ? params.deviceID : ''
        def user            = Token.findByDeviceID(deviceID)?.user

        // wrap result in api call validator
        apiService.executeApiCall(params,response,'assay',assay,{
            // define data elements
            def measurements
            def features
            def subjects

            // get subjects (metadata) data for assay
            def subjectMap = [:]
            assay.parent.subjects.each() { Subject subject ->
                def fieldMap = [:]
                subject.giveFields().each() { field ->
                    // skip field 'name' since this is already the key
                    if (!field.name.equals('name')) {
                        //println field.name
                        //println subject.getFieldValue(field.name)
                        fieldMap.put(field.name, subject.getFieldValue(field.name).toString())
                    }
                }
                println fieldMap
                subjectMap.put(subject.name, fieldMap)
            }

            // iterate through measurementData and build data matrix
            try {
                if (output.equals('all') || output.equals('subjects')) {
                    // cast subjectMap to JSON
                    subjects = new JSONObject(subjectMap)
                }

                if (output.equals('all') || output.equals('measurements')) {
                    // get measurements for assay
                    measurements = apiService.getPlainMeasurementData(assay, user)
                }

                if (output.equals('all') || output.equals('features')) {
                    // get features (metadata) for assay
                    features = apiService.getFeaturesForAssay(assay, user)
                }

                // define result
                def result = [:]
                result = [
                        "measurements" : measurements,
                        "features" : features,
                        "subjects" : subjects
                ]

                // set output headers
                response.status = 200
                response.contentType = 'plain/text;charset=UTF-8'
                //response.contentType = 'application/json;charset=UTF-8'

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
	 * get all fields for a specific entity
	 *
	 * @param string deviceID
	 * @param string validation md5 sum
	 * @param string entityType
	 */
	def getFieldsForEntity = {
		println "api::getFieldsForEntity: ${params}"

		// while we pass this call through to getFieldsForEntityWithTemplate,
		// this particular call was designed to be called with no template
		// argument set, so strip it from the parameters so the result data is
		// as expected
		if (params.containsKey('templateToken')) params.remove('templateToken')

		// add a passthrough parameter
		params['passthrough'] = true

		// passthrough to getFieldsForEntityWithTemplate
		getFieldsForEntityWithTemplate(params)
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
		if (!params.containsKey('passthrough')) {
			// entityToken can only be passed by the getFieldsForEntity call
			// so strip it if we're not passing through
			params.remove('entityToken')

			println "api::getFieldsForEntityWithTemplate: ${params}"
		}

		def result = [:]
		String entityType = (params.containsKey('entityType')) ? params.get('entityType') : ''
		String templateToken = (params.containsKey('templateToken')) ? params.get('templateToken') : ''
		String entityToken = (params.containsKey('entityToken')) ? params.get('entityToken') : ''

		if (entityType) {
			try {
				// get entity
				def entity = apiService.getEntity(entityType)

				// got an entity?
				if (!entity) throw new Exception("invalid entity '${entityType}', call getEntityTypes for a list of valid entities. Note that entityType is case sensitive!")

				// instantiate entity
				def entityInstance = entity.newInstance()

				// got an entityToken or a templatetoken?
				if (entityToken) {
					// find the entity with this token
					def foundInstance = entityInstance.findWhere(UUID: entityToken)

					// found an instance?
					if (foundInstance) {
						// yes, use this instance instead
						entityInstance = foundInstance
					} else {
						throw new Exception("no such ${entityType} with token ${entityToken}")
					}
				} else if (templateToken) {
					// no entity token, but we have a template token instead
					def template = Template.findWhere(UUID: templateToken)

					// was a valid template specified?
					if (template && entity.equals(template.entity)) {
						// set template
						entityInstance.setTemplate(template)
					} else {
						throw new Exception("invalid template token specified, call getTemplatesForEntity(${entityType}) for a list of valid templates")
					}
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
			} catch (Exception e) {
				response.sendError(500, "unknown error occured (${e.getMessage()})")
			}
		} else {
			response.sendError(400, "entityType is missing")
		}
	}

	/**
	 * create a new entity
	 *
	 * @param string deviceID
	 * @param string validation md5 sum
	 * @param string entityType
	 */
	def createEntity = {
		println "api::createEntity: ${params}"

		// while we pass this call through to createEntityWithTemplate,
		// this particular call was designed to be called with no template
		// argument set, so strip it from the parameters so the result data is
		// as expected
		if (params.containsKey('templateToken')) params.remove('templateToken')

		// add a passthrough parameter
		params['passthrough'] = true

		// passthrough to getFieldsForEntityWithTemplate
		createEntityWithTemplate(params)
	}

	/**
	 * create a new entity with a specific template defined
	 *
	 * @param string deviceID
	 * @param string validation md5 sum
	 * @param string entityType
	 * @param string templateToken
	 */
	def createEntityWithTemplate = {
		if (!params.containsKey('passthrough')) {
			println "api::createEntityWithTemplate: ${params}"
		}

		String entityType = (params.containsKey('entityType')) ? params.get('entityType') : ''
		String templateToken = (params.containsKey('templateToken')) ? params.get('templateToken') : ''
		Map hasManyRelationships = [:]  // belongsTo relationship
		Map relationships = [:]         // one to one relationship
		Map reverseRelationships = [:]  // non belongsTo relationships (parent hasMany relationship)

		if (entityType) {
			try {
				// get entity
				def entity = apiService.getEntity(entityType)

				// got an entity?
				if (!entity) throw new Exception("invalid entity '${entityType}', call getEntityTypes for a list of valid entities. Note that entityType is case sensitive!")

				// instantiate entity
				def entityInstance = entity.newInstance()

				// got a template token?
				if (templateToken) {
					// no entity token, but we have a template token instead
					def template = Template.findWhere(UUID: templateToken)

					// was a valid template specified?
					if (template && entity.equals(template.entity)) {
						// set template
						entityInstance.setTemplate(template)
					} else {
						throw new Exception("invalid template token specified, call getTemplatesForEntity(${entityType}) for a list of valid templates")
					}
				}

				// iterate through all fields for this instance and try to set them
				entityInstance.giveFields().each {
					if (params.containsKey(it.name)) {
						entityInstance.setFieldValue(it.name, params.get(it.name))
					}
				}

				// try to set the relationships
				def changed = false

                                            if (entityInstance.hasProperty('belongsTo')) {

				entityInstance.belongsTo.each { name, type ->
					def matches	= type.toString() =~ /\.([^\.]+)$/
					def tokenEntity = matches[0][1]
					def tokenName = "${tokenEntity.toLowerCase()[0]}${tokenEntity.substring(1)}Token".toString()
					def uuid = (params.containsKey(tokenName)) ? params.remove(tokenName) : ''

					// does the tokenName exist in the parameters (e.g. studyToken)?
					if (uuid) {
						// yes, find an instance of this entity
						def tokenInstance = apiService.getEntity(tokenEntity)

						// find the entity with this particular token
						def foundEntity = tokenInstance.findWhere(UUID: uuid)

						// did we indeed found the entity we need to set a relationship with?
						if (foundEntity) {
							// check if there is a hasMany relationship for this entity type
							def relationship = foundEntity.hasMany.find { n, t ->
								def m = t.toString() =~ /\.([^\.]+)$/
								return (entityType == m[0][1])
							}

							// found a hasMany relationship?
							if (relationship) {
								// yes, set relationship
								def relationsName = relationship.key.toString()
								foundEntity."addTo${relationsName.toUpperCase()[0]}${relationsName.substring(1)}"( entityInstance )
								hasManyRelationships["${relationsName.toUpperCase()[0]}${relationsName.substring(1)}"] = foundEntity
								changed = true
							} else {
								// no, check if it's a one to one relationship
								relationship = foundEntity.properties.find { n, t ->
									def m = t.toString() =~ /\.([^\.]+)$/
									return (m && entityType == m[0][1])
								}

								// got one?
								if (relationship) {
									// yes, set it
									def relationsName = relationship.key.toString()
									foundEntity."${relationsName}" = entityInstance
									relationships["${relationsName}"] = foundEntity
									changed = true
								}
							}
						} else {
							throw new Exception("A ${tokenEntity} with token ${uuid} does not exist")
						}
					}
				}
                                            }


				// do we have other relationships in the parameter set? E.g.
				// reverse relationships where belongsTo (cascaded deletes) is
				// not defined, yet in a parent object this instance is defined
				// in a hasMany relationship?
				params.findAll{ name, value -> name =~ /Token$/ }.each { name, value ->
					// get the reverse (parent) instance name
					// for example: Sample -> Assay where Assay does have a
					// hasMany relationship to Sample but Sample does not
					// have a belongsTo set for Assay
					def m = name =~ /^([a-zA-Z]+)Token$/
					def reverseInstanceName = m[0][1].toUpperCase()[0] + m[0][1].substring(1)

					// get an instance of this class
					def reverseBaseInstance = apiService.getEntity(reverseInstanceName)
					def reverseInstance = null

					// fetch the reverse instance (if possible)
					try {
						reverseInstance = reverseBaseInstance.findWhere(UUID: value)
					} catch (Exception e) { }

					// got a reverse relationship?
					if (reverseInstance) {
						reverseInstance.hasMany.findAll { hasManyName, hasManyValue ->
							hasManyValue == entity
						}.each { hasManyName, hasManyValue ->
							def functionName = "${hasManyName.toUpperCase()[0]}${hasManyName.substring(1)}"

							// remember relationship for rollback purposes
							reverseRelationships[ functionName ] = reverseInstance

							// add relationship
							reverseInstance."addTo${functionName}"( entityInstance )
						}
					}
				}

				// validate instance
				if (entityInstance.validate()) {
					// wrap result in api call validator
					apiService.executeApiCall(params, response, entityType, entityInstance, {
						// try to save instance
						try {
							// save item, although it may already have been
							// implicitely saved by any addToXyz statement
							// earlier
							if (!changed) entityInstance.save()

							// set output headers
							response.status = 200
							response.contentType = 'application/json;charset=UTF-8'

							// fetch all fields
							def result = [
								'success'   : true,
								'entityType': entityType,
								'token'     : entityInstance.UUID
							]

							if (params.containsKey('callback')) {
								render "${params.callback}(${result as JSON})"
							} else {
								render result as JSON
							}
						} catch (Exception e) {
							response.sendError(500, "unknown error occured (${e.getMessage()})")
						}
					}, {
						// undo relationships - CLEANUP!
						hasManyRelationships.each { name, instance ->
							instance."removeFrom${name}"(entityInstance)
						}
						relationships.each { name, instance ->
							instance[name] = null
						}
						entityInstance.delete(flush: true)
					})
				} else {
					// blast, we've got errors
					// undo relationships - CLEANUP!
					hasManyRelationships.each { name, instance ->
println "   rollback ${instance}::${name}"
						instance."removeFrom${name}"(entityInstance)
					}
					relationships.each { name, instance ->
println "   rollback ${instance}::${name}"
						instance[name] = null
					}
					reverseRelationships.each { name, instance ->
println "   rollback ${instance}::${name}"
						instance."removeFrom${name}"(entityInstance)
					}
					entityInstance.delete(flush: true)

					// propagate errors
					throw new Exception(entityInstance.errors.getAllErrors().collect { validationTagLib.message(error: it) }.join(', '))
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
