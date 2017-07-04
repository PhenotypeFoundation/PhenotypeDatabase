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

import grails.plugin.springsecurity.annotation.Secured
import grails.converters.JSON
import dbnp.authentication.SecUser
import dbnp.studycapturing.*
import org.codehaus.groovy.grails.web.json.JSONObject
import org.dbnp.gdt.*
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib

class ApiController {
    def grailsApplication
    def authenticationService
    def apiService
    def dataSource
	def validationTagLib = new ValidationTagLib()

	/**
	 * index closure
	 */
    def index() {
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
    def authenticate() {
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
    def getStudies() {
        println "api::getStudies: ${params}"

        String deviceID = (params.containsKey('deviceID')) ? params.deviceID : ''
        String validation = (params.containsKey('validation')) ? params.validation : ''

        // check
        if (!apiService.validateRequest(deviceID,validation)) {
            response.sendError(401, 'Unauthorized: please check your validation hash and make sure you have the Client role assigned to your account')
        } else {
            def user = getUser()
            def readableStudies = Study.giveReadableStudies(user)
            def studies = []

            // iterate through studies and define resultset
            readableStudies.each { Study study ->
                // get result data
                studies[ studies.size() ] = [
                        'token'                     : study.UUID,
                        'code'                      : study.code,
                        'title'                     : study.title,
                        'description'               : study.description,
                        'subjectCount'              : study.subjectCount,
                        'species'                   : study.subjects.species.unique().name,
                        'subjectGroups'             : study.subjectGroups.name,
                        'sampleAndTreatmentGroups'  : study.eventGroups.name,
                        'treatmentTypes'            : study.events.name,
                        'sampleTypes'               : study.samplingEvents.name,
                        'assays'                    : study.assays.name,
                        'modules'                   : study.assays.collect { it.module.name }.unique(),
                        'sampleCount'               : study.sampleCount,
                ]
            }

            def result = [
                    'count'     : studies.size(),
                    'studies'   : studies
            ]

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
    def getSubjectsForStudy() {
        println "api::getSubjectsForStudy: ${params}"

        Study study = getStudy()

        // wrap result in api call validator
        apiService.executeApiCall(params,response,'study',study,{
            def subjects = apiService.flattenDomainData( study.subjects )

            // define result
            def result = [
                    'count'     : subjects.size(),
                    'subjects'  : subjects
            ]

            if (params.containsKey('callback')) {
                render "${params.callback}(${result as JSON})"
            } else {
                render result as JSON
            }
        })

    }

    /**
     * get all subjectGroups for a study
     *
     * @param string deviceID
     * @param string studyToken
     * @param string validation md5 sum
     */
    def getSubjectGroupsForStudy() {
        Study study = getStudy()

        apiService.executeApiCall(params,response,'study',study,{
            def studySubjectGroups = study.subjectGroups.findAll()

            def result = [
                    'count'         : studySubjectGroups.size(),
                    'subjectGroups' : getSubjectGroups(studySubjectGroups)
            ]

            if (params.containsKey('callback')) {
                render "${params.callback}(${result as JSON})"
            } else {
                render result as JSON
            }
        })
    }

    /**
     * get all sampleAndTreatmentGroupsForStudy (eventGroups) for a study
     *
     * @param string deviceID
     * @param string studyToken
     * @param string validation md5 sum
     */
    def getSampleAndTreatmentGroupsForStudy() {
        println "api::getEventGroupsForStudy: ${params}"

        Study study = getStudy()

	    // wrap result in api call validator
        apiService.executeApiCall(params,response,'study',study,{
            def eventGroups = apiService.flattenDomainData( study.eventGroups )

            // define result
            def result = [
                'count'         : eventGroups.size(),
                'sampleAndTreatmentGroups'   : eventGroups
            ]

            if (params.containsKey('callback')) {
                render "${params.callback}(${result as JSON})"
            } else {
                render result as JSON
            }
        })
    }

    /**
     * get all treatmentTypes (events) for a study
     *
     * @param string deviceID
     * @param string studyToken
     * @param string validation md5 sum
     */
    def getTreatmentTypesForStudy() {
        println "api::getEventsForStudy: ${params}"

        Study study = getStudy()

	    // wrap result in api call validator
        apiService.executeApiCall(params,response,'study',study,{
            def events = apiService.flattenDomainData( study.events )

            // define result
            def result = [
                'count' : events.size(),
                'treatmentTypes': events
            ]

            if (params.containsKey('callback')) {
                render "${params.callback}(${result as JSON})"
            } else {
                render result as JSON
            }
        })
    }

    /**
     * get all sampleTypes (samplingEvents) for a study
     *
     * @param string deviceID
     * @param string studyToken
     * @param string validation md5 sum
     */
    def getSampleTypesForStudy() {
        println "api::getSamplingEventsForStudy: ${params}"

        Study study = getStudy()

	    // wrap result in api call validator
        apiService.executeApiCall(params,response,'study',study,{
            def samplingEvents = apiService.flattenDomainData( study.samplingEvents )

            // define result
            def result = [
                    'count': samplingEvents.size(),
                    'sampleTypes': samplingEvents
            ]

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
    def getAssaysForStudy() {
        println "api::getAssaysForStudy: ${params}"

        Study study = getStudy()

        // wrap result in api call validator
        apiService.executeApiCall(params,response,'study',study,{
            def assays = apiService.flattenDomainData( study.assays )

            // define result
            def result = [
                    'count'     : assays.size(),
                    'assays'    : assays
            ]

            if (params.containsKey('callback')) {
                render "${params.callback}(${result as JSON})"
            } else {
                render result as JSON
            }
        })
    }

	/**
	 * get all samples for a study
	 *
	 * @param string deviceID
	 * @param string studyToken
	 * @param string validation md5 sum
	 */
	def getSamplesForStudy() {
		println "api::getSamplesForStudy: ${params}"

        Study study = getStudy()

		// wrap result in api call validator
		apiService.executeApiCall(params,response,'study',study,{

			def studySamples = study.samples

			def samples = apiService.flattenDomainData( studySamples )

			def result = [
					'count'     : samples.size(),
					'samples'   : samples
			]

            if (params.containsKey('callback')) {
                render "${params.callback}(${result as JSON})"
            } else {
                render result as JSON
            }
		})
	}

    /**
     * get all subjects for a assay
     *
     * @param string deviceID
     * @param string studyToken
     * @param string validation md5 sum
     */
    def getSubjectsForAssay() {
        println "api::getSubjectsForAssay: ${params}"

        Assay assay = getAssay()

        // wrap result in api call validator
        apiService.executeApiCall(params,response,'assay',assay,{
            def subjects = apiService.flattenDomainData( assay.samples.parentSubject.unique() )

            // define result
            def result = [
                    'count'     : subjects.size(),
                    'subjects'  : subjects
            ]

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
    def getSamplesForAssay() {
        println "api::getSamplesForAssay: ${params}"

        Assay assay = getAssay()

        // wrap result in api call validator
        apiService.executeApiCall(params,response,'assay',assay,{
            def samples = apiService.flattenDomainData( assay.samples )

            def result = [
                    'count'     : samples.size(),
                    'samples'   : samples
            ]

            if (params.containsKey('callback')) {
                render "${params.callback}(${result as JSON})"
            } else {
                render result as JSON
            }
        })
    }

    /**
     * get all features for an assay
     *
     * @param string deviceID
     * @param string assayToken
     * @param string validation md5 sum
     */
    def getFeaturesForAssay() {
        println "api::getFeaturesForAssay: ${params}"

        Assay assay = getAssay()
        // fetch user based on deviceID
        SecUser user = getUser()

        // wrap result in api call validator
        apiService.executeApiCall(params,response,'assay',assay,{
            def features = apiService.getFeaturesForAssay(assay, user)

            def result = [
                    'count': features.size(),
                    'features': features
            ]

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
    def getMeasurementDataForAssay() {
        println "api::getMeasurementDataForAssay: ${params}"

        Assay assay = getAssay()
        // fetch user based on deviceID
        SecUser user = getUser()

        // wrap result in api call validator
        apiService.executeApiCall(params,response,'assay',assay,{
            // iterate through measurementData and build data matrix
            try {

                def count = 0
                def measurements =  [:]

                apiService.getMeasurementDataForAssay(assay, user).each() { feature, featureMeasurements ->
                    featureMeasurements.each() { sampleId, value ->
                        Sample sample = Sample.read(sampleId)

                        String featureName = feature.intern()
                        String eventGroupName = sample.getParentEventGroupName().intern()
                        String subjectEventGroupStartTime = sample.getParentSubjectEventGroupStartTimeString().intern()
                        String sampleRelativeStartTime = sample.getSampleRelativeStartTimeString().intern()
                        String subjectName = sample.getParentSubjectName().intern()

                        if ( !measurements[featureName] ) {
                            measurements[featureName] = [:]
                        }

                        if ( !measurements[featureName][eventGroupName] ) {
                            measurements[featureName][eventGroupName] = [:]
                        }

                        if ( !measurements[featureName][eventGroupName][subjectEventGroupStartTime] ) {
                            measurements[featureName][eventGroupName][subjectEventGroupStartTime] = [:]
                        }

                        if ( !measurements[featureName][eventGroupName][subjectEventGroupStartTime][sampleRelativeStartTime] ) {
                            measurements[featureName][eventGroupName][subjectEventGroupStartTime][sampleRelativeStartTime] = [:]
                        }

                        measurements[featureName][eventGroupName][subjectEventGroupStartTime][sampleRelativeStartTime].put(subjectName, value)
                        count += 1
                    }
                }

                def result = [
                        'count': count,
                        'measurements': measurements
                ]

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
	def getModules() {
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
	def getEntityTypes() {
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
	def getTemplatesForEntity() {
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
	def getFieldsForEntity() {
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
	def getFieldsForEntityWithTemplate() {
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
     * STILL TESTING
     *
     * Export an assay to an Opal (obiba.org/pages/products/opal/) instance on the same server.
     *
     * @param string deviceID
     * @param string validation md5 sum
     * @param string assayToken
     */
    def exportAssayToOpal() {
        println "api::ExportAssayToOpal: ${params}"

        Assay assay = getAssay()
        // fetch user based on deviceID
        SecUser user = getUser()

        String opalUrl = grailsApplication.config?.opalUrl
        String opalUser = grailsApplication.config?.opalUser
        String opalPassword = grailsApplication.config?.opalPassword

        if ( !opalUrl || !opalUser || !opalPassword ) {
            response.sendError(500, "opalUrl, opalUser and opalPassword not configured (in external config)" )
            return
        }

        String table = "${assay.parent.code.replace(' ','_')}-${assay.name.replace(' ','_')}".toString()

        String deleteTableCommand = "opal delete-table --opal ${opalUrl} --user ${opalUser} --password ${opalPassword} --project phenotypedatabase-exported --tables \"${table}\""

        // Delete table if it already exists within the phenotypedatabase-exported project
        deleteTableCommand.execute()

        def result = [ 'status': "Export to Opal (${opalUrl}) failed" ]

        //wrap result in api call validator
        apiService.executeApiCall(params,response,'assay',assay,{
            // iterate through measurementData and build data matrix
            try {

                def subjectMap = [:]
                def featureList = []

                apiService.flattenDomainData( assay.parent.subjects ).each() { subjectList ->

                    String subjectName = ''

                    subjectList.each() { item, value ->

                        switch (item) {
                            case 'name':
                                subjectName = value.toString()
                                subjectMap[subjectName] = [:]
                                break
                            case ['species', 'token']:
                                break
                            default:

                                String featureName = featureConverter(item).intern()

                                subjectMap[subjectName][featureName] = value

                                if (!featureList.contains(featureName)) {
                                    featureList << featureName
                                }

                                break
                        }
                    }
                }

                apiService.getMeasurementDataForAssay(assay, user).each() { feature, featureMeasurements ->
                    featureMeasurements.each() { sampleId, value ->
                        Sample sample = Sample.read(sampleId)

                        String featureName = featureConverter(feature).intern()
                        String subjectName = sample.getParentSubjectName().intern()

                        subjectMap[subjectName][featureName] = value

                        if (!featureList.contains(featureName)) {
                            featureList << featureName
                        }
                    }
                }

                //Clean up all-null features
                def featuresToRemoveFromList = []
                featureList.each() { String featureName ->
                    def uniqueValues = subjectMap.collect { it.value[featureName] }.unique()

                    if ( uniqueValues.size() == 1 && uniqueValues[0].toString().equals('null') ) {
                        featuresToRemoveFromList << featureName
                    }
                }

                featureList.removeAll(featuresToRemoveFromList)

                File opalImport = new File("/tmp/opalImport-${assay.UUID}.csv")

                opalImport << "ID,"+featureList.join(',')+"\n"
                subjectMap.each() { String subjectId, HashMap valueMap ->
                    String row = subjectId

                    featureList.each() { featureName ->

                        def value

//                        value = valueMap.getOrDefault(featureName, '')

                        if ( valueMap.containsKey(featureName) ) {
                            value = valueMap[featureName].toString()
                        }

                        if ( value ) {
                            if ( value.contains('.0') ) {
                                def split = value.split("\\.")

                                if (split[1].size() == 1) {
                                    value = split[0]
                                }
                            }
                        }
                        else {
                            value = ''
                        }

                        row += ','+value
                    }

                    opalImport << row+'\n'
                }

                String fileCommand = "opal file --opal ${opalUrl} --user ${opalUser} --password ${opalPassword} -up /tmp/opalImport-${assay.UUID}.csv /home/${opalUser}".toString()
                String importCommand = "opal import-csv --opal ${opalUrl} --user ${opalUser} --password ${opalPassword} --destination phenotypedatabase-exported --table ${table} --path /home/${opalUser}/opalImport-${assay.UUID}.csv --type Participant --json".toString()
                String removeCommand = "rm /tmp/opalImport-${assay.UUID}.csv".toString()

                fileCommand.execute()
                String importCommandExecuteText = importCommand.execute().text
                removeCommand.execute()

                String dataCommand = "opal dict phenotypedatabase-exported.${table} --opal ${opalUrl} --user ${opalUser} --password ${opalPassword} --json".toString()

                def i = 0
                def assayInOpal = false

                // Check if assay becomes available in Opal so the response does not come back while Opal is still processing
                while( !assayInOpal && i < 30 ) {

                    String dataCommandExecuteText = dataCommand.execute().text

                    if ( !dataCommandExecuteText.contains("404 Not Found") ) {
                        assayInOpal = true
                    }
                    else {
                        // Delay next try for two seconds
                        sleep(2000)
                    }

                    i ++
                }

                if (assayInOpal) {
                    result = [ 'status': "Export to Opal succeeded", 'project': 'phenotypedatabase-exported', 'table': table ]
                }
                else {
                    log.error("ExportToOpal not successful after 1 minute for assayId ${assay.id}: ${importCommandExecuteText}")
                }

                if (params.containsKey('callback')) {
                    render "${params.callback}(${result as JSON})"
                    return
                } else {
                    render result as JSON
                    return
                }

            } catch (Exception e) {
                println "exportAssayToOpal exception: ${e.getMessage()}"
                response.sendError(500, "There seems to be something wrong with either data retrieval or Opal connection")
            }
        })
    }

	/**
	 * create a new entity
	 *
	 * @param string deviceID
	 * @param string validation md5 sum
	 * @param string entityType
	 */
	def createEntity() {
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
	def createEntityWithTemplate() {
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
                    }, 'create')
                }
                else {
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
	def teapot() {
		// ask, and the Mad Hatter will reply...
		response.sendError(418, "'Twas brillig, and the slithy toves Did gyre and gimble in the wabe: All mimsy were the borogoves, And the mome raths outgrabe.")
	}

    private Study getStudy() {
        return Study.findWhere(UUID: getStudyToken())
    }

    private String getStudyToken() {
        return (params.containsKey('studyToken')) ? params.studyToken : ''
    }

    private Assay getAssay() {
        return Assay.findWhere(UUID: getAssayToken())
    }

    private String getAssayToken() {
        return (params.containsKey('assayToken')) ? params.assayToken : ''
    }

    private SecUser getUser() {
        return Token.findByDeviceID(getDeviceId())?.user
    }

    private String getDeviceId() {
        return (params.containsKey('deviceID')) ? params.deviceID : ''
    }

    private ArrayList<Map<String, ArrayList<Map<String, String, Integer>>>> getSubjectGroups(studySubjectGroup) {
        studySubjectGroup.collect {
            [name: it.name, subjectEventGroups: it.subjectEventGroups.collect {
                [startTime: it.startTime, description: it.description, eventGroupId: it.eventGroup.id]
            }]
        }
    }

    private String featureConverter(String inputName) {

        String outputName = ''

        switch(inputName) {
            case 'age(years)':
                outputName = 'AGE'
                break
            case 'bodyWeight(kg)':
                outputName = 'WEIGHT'
                break
            case 'bodyHeight(cm)':
                outputName = 'HEIGHT'
                break
            default:
                outputName = inputName

        }

        outputName = outputName.replace("(","_")
        outputName = outputName.replace(")","")

        return outputName
    }
}
