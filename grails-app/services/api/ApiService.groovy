/**
 * ApiService Service
 * 
 * Description of my service
 *
 * @author  Jeroen Wesbeek <work@osx.eu>
 * @since	20120328
 * @package	api
 *
 * Revision information:
 * $Rev: 1430 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
 */
package api

import java.security.MessageDigest
import dbnp.studycapturing.*
import org.dbnp.gdt.*
import dbnp.authentication.SecUser
import org.springframework.context.ApplicationContextAware
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.commons.ApplicationHolder

class ApiService implements Serializable, ApplicationContextAware {
    // inject the module communication service
    def moduleCommunicationService
	def gdtService
	def apiService
    def grailsApplication
    // transactional
    static transactional = false

    // characters to split on when converting a string to camelCased format
    static camelCaseSeparators = " |-|_"

    // hasMany keys to ignore when flattening domain data
    static ignoreHasManyKeys = [
            "templateBooleanFields",
            "templateDateFields",
            "templateDoubleFields",
            "templateExtendableStringListFields",
            "templateFileFields",
            "templateLongFields",
            "templateModuleFields",
            "templateTermFields",
            "templateRelTimeFields",
            "templateStringFields",
            "templateStringListFields",
            "templateTemplateFields",
            "templateTextFields"
    ]

    private ApplicationTagLib g

    void setApplicationContext(ApplicationContext applicationContext) {
        g = applicationContext.getBean(ApplicationTagLib)

        // now you have a reference to g that you can call render() on
    }

    /**
     * validate a client request by checking the validation checksum
     * @param deviceID
     * @param validation
     * @return
     */
    def validateRequest(String deviceID, String validation) {
        def validated = false

        // disable validation check on development and ci
        if (['development', 'ci'].contains(grails.util.GrailsUtil.environment)) {
//            return true
        }

        // get token for this device ID
        Token token = Token.findByDeviceID(deviceID)

        // increase sequence
        if (token && (token.user.hasClientRights() || token.user.hasAdminRights())) {
            token.sequence = token.sequence+1
            token.merge(flush: true)

            // generate the validation checksum
            MessageDigest digest = MessageDigest.getInstance("MD5")
            String validationSum = new BigInteger(1,digest.digest("${token.deviceToken}${token.sequence}${token.user.apiKey}".getBytes())).toString(16).padLeft(32,"0")

            // check if the validation confirms
            validated = (validation == validationSum)
        }

        return validated
    }

	/**
	 * flatten domain data and strip extra elements from resultset
	 *
	 * @param elements (List or Set)
	 * @param elements to strip (List)
	 * @return
	 */
	public flattenDomainData(elements, elementsToStrip) {
		def items = flattenDomainData(elements)

		// remove elements
		items.each { item ->
			elementsToStrip.each {
				item.remove(it)
			}
		}

		// and return the stripped set
		return items
	}

	/**
     * flatten domain data to relevant data to return in an api
     * call and not to expose domain internals
     *
     * @param elements (List or Set)
     * @return
     */
    public flattenDomainData(elements) {
        def items = []

        // iterate through elements
        elements.each {
            def fields  = (it.respondsTo('giveFields')) ? it.giveFields(): []
            def item    = [:]

            // check if element has a name
            ['name','description'].each { checkName ->
                if (it.hasProperty(checkName)) item[checkName] = it[checkName]
            }

            // add token
	        if (it.UUID) {
                // some domain methods implement giveUUID
		        // (and this has system wide been implemented
		        //  in GDT 1.3.1)...
                item['token'] = it.UUID

            // add parent event token for samples
            if(it.hasProperty('parentEvent')) {
                item['parentEventToken'] = it.parentEvent?.UUID
            }

            } else {
                // and others don't at all, so far
                // the consistency...
                item['id'] = it.id
            }

            // add subject field values
            fields.each { field ->
                // get a camelCased version of the field name
                def name = field.name.split(camelCaseSeparators).collect {it[0].toUpperCase() + it.substring(1)}.join('')
                    name = name[0].toLowerCase() + name.substring(1)

                // get the value for this field
                def value = it.getFieldValue( field.name )

                // add value
                if (value.hasProperty('name')) {
                    item[ name ] = value.name
                } else {
                    item[ name ] = value
                }
            }

            // list hasMany sizes
            it.properties.hasMany.each { hasManyItem ->
	            if (!ignoreHasManyKeys.contains(hasManyItem.key)) {
                    // add count for this hasMany item
                    item[ hasManyItem.key ] = it[ hasManyItem.key ].size()
                }
            }

            // add item to resultset
            items[ items.size() ] = item
        }

        return items
    }

	/**
	 * flatten template field information
	 */
	public flattenTemplateField(TemplateField field) {
		def info = [
			'name'          : field.name,
			'comment'       : field.comment,
			'type'          : field.type.name
		]

		// listEntries
		if (field.type == TemplateFieldType.STRINGLIST || field.type == TemplateFieldType.EXTENDABLESTRINGLIST) {
			info.terms = field.listEntries.collect { it.name }
		}

		// ontologies
		if (field.type == TemplateFieldType.ONTOLOGYTERM) {
			def ontologies = []

			field.ontologies.each {
				ontologies.add([
					'name'          : it.name,
					'description'   : it.description,
					'ncboId'        : it.ncboId,
					'url'           : it.url
				])
			}

			info.ontologies = ontologies
		}

		return info
	}

    /**
     * wrapper for performing api calls
     *
     * validates if the user may call this api
     *
     * @param params
     * @param response
     * @param itemName
     * @param item
     * @param block
     */
    def executeApiCall(params,response,itemName,item,Closure block) {
	    executeApiCall(params,response,itemName,item,block,{ })
    }
	def executeApiCall(params,response,itemName,item,Closure block,Closure cleanUpBlock) {
        // get variables from parameters
        String deviceID     = (params.containsKey('deviceID')) ? params.deviceID : ''
        String validation   = (params.containsKey('validation')) ? params.validation : ''

        // fetch user based on deviceID
        def user = Token.findByDeviceID(deviceID)?.user

        // check if api call may be performed
        if (!validateRequest(deviceID,validation)) {
            // validation md5sum does not match predicted hash
	        cleanUpBlock()
            response.sendError(401, "Unauthorized")
        } else if (!item) {
            // no results, invalid 'item'
	        cleanUpBlock()
	        response.sendError(400, "No such ${itemName}")
        } else if (item.respondsTo('canRead') && !item.canRead(user)) {
            // the user cannot read this data
	        cleanUpBlock()
	        response.sendError(401, "Unauthorized")
        } else if (!canWriteBelongsToRelationships(item, user)) {
            // the user cannot read this data
	        cleanUpBlock()
	        response.sendError(401, "Unauthorized")
        } else {
            // allowed api call, execute block / closure
            block()
        }
    }

	/**
	 * Check if a user is allowed to write any instances in the
	 * belongsTo relationship
	 *
	 * @param item
	 * @param user
	 * @return Boolean
	 */
	private canWriteBelongsToRelationships(item, user) {
		Boolean allowed = true

		if (item.hasProperty('belongsTo')) {
			item.belongsTo.each { name, type ->
				def checkEntity = item."${name}"
				if (checkEntity && (checkEntity.respondsTo('canWrite') && !checkEntity.canWrite(user))) {
					allowed = false
				}
			}
		}

		return allowed
	}

    /**
     * get the measurement tokens from the remote module
     *
     * @param assay
     * @param user
     * @return
     */
    def getMeasurements(Assay assay, SecUser user) {
        def serviceURL = "${assay.module.baseUrl}/rest/getMeasurements"
        def serviceArguments = "assayToken=${assay.UUID}"
        def json

        // call module method
        try {
            json = moduleCommunicationService.callModuleMethod(
                    assay.module.baseUrl,
                    serviceURL,
                    serviceArguments,
                    "POST",
                    user
            );
        } catch (Exception e) {
            log.error "api.getMeasurements failed :: ${e.getMessage()}"
            json = new org.codehaus.groovy.grails.web.json.JSONArray()
        }

        return json
    }

    /**
     * get measurement data from the remote module in verbose format
     *
     * @param assay
     * @param user
     * @return
     */
    def getMeasurementData(Assay assay, SecUser user) {
        def serviceURL = "${assay.module.baseUrl}/rest/getMeasurementData"
        def serviceArguments = "assayToken=${assay.UUID}&verbose=true"
        def json

        // call module method
        try {
            json = moduleCommunicationService.callModuleMethod(
                    assay.module.baseUrl,
                    serviceURL,
                    serviceArguments,
                    "POST",
                    user
            );
        } catch (Exception e) {
            log.error "api.getMeasurementData failed :: ${e.getMessage()}"
            json = new org.codehaus.groovy.grails.web.json.JSONArray()
        }

        return json
    }

    /**
     * get the measurement meta data from the remote module
     *
     * @param assay
     * @param user
     * @return
     */
    def getMeasurementMetaData(Assay assay, SecUser user) {
        def serviceURL = "${assay.module.baseUrl}/rest/getMeasurementMetaData"
        def serviceArguments = "assayToken=${assay.UUID}"
        def json

        // call module method
        try {
            json = moduleCommunicationService.callModuleMethod(
                    assay.module.baseUrl,
                    serviceURL,
                    serviceArguments,
                    "POST",
                    user
            );
        } catch (Exception e) {
            log.error "api.getMeasurementMetaData failed :: ${e.getMessage()}"
            json = new org.codehaus.groovy.grails.web.json.JSONArray()
        }

        return json
    }

	/**
	 * get a list of domain classes that extend GDT's TemplateEntity
	 *
	 * @return array
	 */
	def getEntities() {
		def entities = [:]

		// get the names of all domain classes ('entities') that extend GDT's TemplateEntity
		ApplicationHolder.application.getArtefacts("Domain").each {
			def entityInstance = it.clazz

			if (entityInstance.properties.superclass.toString() =~ 'TemplateEntity') {
				// get matches from regular expression
				def matchesClassName = entityInstance.toString() =~ /\.([^\.]+)$/
				def fullName = entityInstance.toString().split(" ")[1]

				// add entity
				entities[ matchesClassName[0][1] ] = fullName
			}
		}

		return entities
	}

	/**
	 * check if an entity is valid
	 *
	 * @param string
	 * @return boolean
	 */
	def isValidEntity(String entityType) {
		return getEntities().containsKey(entityType)
	}

	/**
	 * get an instance of an entity
	 *
	 * @param entityType
	 * @return Object
	 */
	def getEntity(String entityType) {
		def entities = getEntities()
		def entity

		if (isValidEntity(entityType)) {
			entity = gdtService.getInstanceByEntityName(entities[entityType])
		}

		return entity
	}
}
