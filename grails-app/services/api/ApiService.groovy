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
import dbnp.studycapturing.Assay
import dbnp.authentication.SecUser
import grails.converters.JSON
import org.dbnp.gdt.TemplateEntity

class ApiService implements Serializable {
    // inject the module communication service
    def moduleCommunicationService

    // the shared secret used to validate api calls
    static final String API_SECRET = "th!s_sH0uld^Pr0bab7y_m0v3_t%_th3_uSeR_d0Ma!n_ins7ead!"

    // transactional
    static transactional = false

    // characters to split on when converting a string to camelCased format
    static camelCaseSeperators = " |-|_"

    // hasMany keys to ignore when flattening domain data
    static ignoreHasManyKeys = [
            "systemFields",
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

    /**
     * validate a client request by checking the validation checksum
     * @param deviceID
     * @param validation
     * @return
     */
    def validateRequest(String deviceID, String validation) {
        // disable validation check on development and ci
        if (['development', 'ci'].contains(grails.util.GrailsUtil.environment)) {
            return true
        }

        // get token for this device ID
        Token token = Token.findByDeviceID(deviceID)

        // increase sequence
        if (token) {
            token.sequence = token.sequence+1
            token.save()

            // generate the validation checksum
            MessageDigest digest = MessageDigest.getInstance("MD5")
            String validationSum = new BigInteger(1,digest.digest("${token.deviceToken}${token.sequence}${API_SECRET}".getBytes())).toString(16).padLeft(32,"0")

            // check if the validation confirms
            return (validation == validationSum)
        } else {
            // no such token, re-authenticate
            return false
        }
    }

    /**
     * flatten domain data to relevant data to return in an api
     * call and not to expose domain internals
     *
     * @param elements (List or Set)
     * @return
     */
    def flattenDomainData(elements) {
        def items = []

        // iterate through elements
        elements.each {
            def fields  = it.giveFields()
            def item    = [:]

            // add token
            if (it.respondsTo('getToken')) {
                // some domain methods implement getToken...
                item['token'] = it.getToken()
            } else if (it.respondsTo('giveUUID')) {
                // ...while others implement giveUUID
                item['token'] = it.giveUUID()
            } else {
                // and others don't at all, so far
                // the consistency...
                item['id'] = it.id
            }

            // add subject field values
            fields.each { field ->
                // get a camelCased version of the field name
                def name = field.name.split(camelCaseSeperators).collect {it[0].toUpperCase() + it.substring(1)}.join('')
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
     * get the measurement tokens from the remote module
     *
     * @param assay
     * @param user
     * @return
     */
    def getMeasurements(Assay assay, SecUser user) {
        def serviceURL = "${assay.module.url}/rest/getMeasurements"
        def serviceArguments = "assayToken=${assay.assayUUID}"
        def json

        // call module method
        try {
            json = moduleCommunicationService.callModuleMethod(
                    assay.module.url,
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
        def serviceURL = "${assay.module.url}/rest/getMeasurementData"
        def serviceArguments = "assayToken=${assay.assayUUID}&verbose=true"
        def json

        // call module method
        try {
            json = moduleCommunicationService.callModuleMethod(
                    assay.module.url,
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
        def serviceURL = "${assay.module.url}/rest/getMeasurementMetaData"
        def serviceArguments = "assayToken=${assay.assayUUID}"
        def json

        // call module method
        try {
            json = moduleCommunicationService.callModuleMethod(
                    assay.module.url,
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
}
