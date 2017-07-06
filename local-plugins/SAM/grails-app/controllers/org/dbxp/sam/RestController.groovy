package org.dbxp.sam

import grails.converters.JSON
import dbnp.studycapturing.*
import org.dbnp.gdt.*
import org.dbxp.sam.query.*
import dbnp.query.SearchMode
import dbnp.query.Criterion
import groovy.sql.Sql

class RestController {
    def dataSource
    def moduleService

    /****************************************************************/
    /* REST resources for providing basic data to the GSCF          */
    /****************************************************************/

    /**
     * Return a list of simple assay measurements matching the querying text.
     * If no assay is given, a set of all features for the selected module is returned
     *
     * @param assayToken
     * @return list of feature names for assay.
     *
     * Example REST call:
     * http://localhost:8184/metagenomics/rest/getMeasurements/query?assayToken=16S-5162
     *
     * Resulting JSON object:
     *
     * [ "# sequences", "average quality" ]
     *
     */
    def getMeasurements = {
        log.trace( "rest.getMeasurement called" )

        def assayToken = params.assayToken;
        def assay = getAssay( assayToken );
        def features

        if( assayToken && !assay ) {
            response.sendError(404)
            return false
        }

        if( assay ) {
            // Return all features for the given assay
            features = Feature.executeQuery( "SELECT DISTINCT f FROM Feature f, Measurement m, SAMSample s WHERE m.feature = f AND m.sample = s AND s.parentAssay = :assay", [ "assay": assay ] )
        } else if(moduleService.validateModule(params?.module)) {
            // Return all features for the given module
            features = Feature.executeQuery( "SELECT DISTINCT f FROM Feature f LEFT JOIN f.platform p WHERE p.platformtype = :type", ["type": params.module]  )
        } else {
            response.sendError(404)
            return false
        }

        render features.collect { it.name } as JSON
    }

    /**
     * Searches for studies or samples based on the user parameters.
     * 
     * @param   entity          The entity to search for ( 'Study' or 'Sample' or 'Assay' )
     * @param   criteria        HashMap with the values being hashmaps with field, operator and value. The fields must start with [modulename].
     *                                          [ 0: [ field: 'SAM.qPCR1', operator: 'equals', value: 'term' ], 1: [..], .. ]
     * @param   filter          (optional) Map with a filter to apply on the entities to return. Could be [ Study: [ 'uuid1', 'uuid2'... ], Sample: [ 'uuid5', ....] ]
     * @param   searchMode      Whether the conditions should be combined with AND or OR 
     */
    def search() {
        def entity = params.entity
        def filter = params.filter
        
        // Check validity
        if( !( entity in [ 'Sample', 'Study', 'Assay' ] ) ) {
            response.sendError(400)
            return false
        }
        
        // Determine the criteria
        def search = new SamSearch(entity, params.module)
        def criteria = parseCriteria( params.criteria )
        
        // Choose between AND and OR search. Default is given by the Search class itself.
        switch( params.operator?.toString()?.toLowerCase() ) {
            case "or":
                search.searchMode = SearchMode.or;
                break;
            case "and":
                search.searchMode = SearchMode.and;
                break;
        }
        
        log.trace "Start searching in SAM"
        search.execute( parseCriteria( params.criteria ) );
        log.trace "Finished executing search in SAM"
        
        render search.results as JSON
    }
    
    /**
     * Parses the criteria from the query form given by the user
     * @param   c       Data from the input form and had a form like
     *
     *  [
     *          0: [entityfield:a.b, operator: b, value: c],
     *          0.entityfield: a.b,
     *          0.operator: b,
     *          0.field: c
     *          1: [entityfield:f.q, operator: e, value: d],
     *          1.entityfield: f.q,
     *          1.operator: e,
     *          1.field: d
     *  ]
     * @param parseSearchIds    Determines whether searches are returned instead of their ids
     * @return                                  List with Criterion objects
     */
    protected List parseCriteria( def formCriteria ) {
        ArrayList list = [];
        flash.error = "";

        // Loop through all keys of c and remove the non-numeric ones
        for( c in formCriteria ) {
            if( c.key ==~ /[0-9]+/ && c.value.entityfield ) {
                def formCriterion = c.value;

                SamCriterion criterion = new SamCriterion();

                // Split entity and field
                def field = formCriterion.entityfield?.split( /\./ );
                if( field.size() > 1 ) {
                    criterion.entity = field[0].toString();
                    criterion.field = field[1].toString();
                } else {
                    criterion.entity = field[0];
                    criterion.field = null;
                }

                // Convert operator string to Operator-enum field
                try {
                    criterion.operator = Criterion.parseOperator( formCriterion.operator );
                } catch( Exception e) {
                    log.debug "Operator " + formCriterion.operator + " could not be parsed: " + e.getMessage();
                    flash.error += "Criterion could not be used: operator " + formCriterion.operator + " is not valid.<br />\n";
                    continue;
                }

                // Copy value
                criterion.value = formCriterion.value;

                // Only add criteria for the current module
                if( criterion.entity == params.module )
                    list << criterion
            }
        }

        return list;
    }

    /**
     * Return measurement metadata for measurement
     *
     * @param assayToken
     * @param measurementTokens. List of measurements for which the metadata is returned.
     *                           If this is not given, then return metadata for all
     *                           measurements belonging to the specified assay.
     * @return list of measurements
     *
     * Example REST call:
     * http://localhost:8184/metagenomics/rest/getMeasurementMetadata/query?assayToken=16S-5162
     *      &measurementToken=# sequences
     *		&measurementToken=average quality
     *
     * Example resulting JSON object:
     *
     * [ {"name":"# sequences","type":"raw"},
     *   {"name":"average quality", "unit":"Phred"} ]
     */
    def getMeasurementMetaData = {
        log.trace( "rest.getMeasurementMetaData called" )

        def assayToken = params.assayToken;
        def assay = getAssay( assayToken );
        if( !assay ) {
            response.sendError(404)
            return false
        }

        def measurementTokens = params.list( 'measurementToken' );
        def features

        if( measurementTokens ) {
            // Return all requested features for the given assay
            features = Feature.executeQuery( "SELECT DISTINCT f FROM Feature f, Measurement m, SAMSample s WHERE m.sample = s AND m.feature = f AND s.parentAssay = :assay AND f.name IN (:measurementTokens)", [ "assay": assay, "measurementTokens": measurementTokens ] )
        } else {
            // If no measurement tokens are given, return values for all features
            features = Feature.executeQuery( "SELECT DISTINCT f FROM Feature f, Measurement m, SAMSample s WHERE m.sample = s AND m.feature = f AND s.parentAssay = :assay", [ "assay": assay ] )
        }

        // Return all fields, including template fields
        render features.collect { feature -> 
            feature.giveFields().collectEntries { field ->
                def value = feature.getFieldValue(field.name);
                if( value instanceof TemplateFieldListItem || value instanceof Term || value instanceof Template || value instanceof AssayModule )
                    value = value.toString();
                    
                [ (field.name): value ]
            }
        } as JSON
    }
    
    /**
     * Retrieves a list of actions that can be performed on data with a specific entity. This includes actions that
     * refine the search result.
     *
     * The module is allowed to return different fields when the user searches for different entities
     *
     * Example call:        [moduleurl]/rest/getPossibleActions?entity=Assay&entity=Sample
     * Example response:    { "Assay": [ { name: "excel", description: "Export as excel" } ],
     *                        "Sample": [ { name: "excel", description: "Export as excel" } ]
     *                      }
     *
     * @param               params.entity   Entity that is searched for. Might be more than one. If no entity is given,
     *                                                      a list of searchable fields for all entities is given
     * @return              JSON            Hashmap with keys being the entities and the values are lists with the action this module can
     *                                                      perform on this entity. The actions as hashmaps themselves, with keys
     * 'name'               Unique name of the action, as used for distinguishing actions
     * 'description'        Human readable description
     * 'url'                URL to send the user to when performing this action. The user is sent there using POST with
     *
     * the following parameters:
     *
     * actionName:          Name of the action to perform
     * name:                Name of the search that the action resulted from
     * url:                 Url of the search that the action resulted from
     * entity:              Type of entity being returned
     * tokens:              List of entity tokens
     * 'type'               (optional) Determines what type of action it is. Possible values: 'default', 'refine', 'export', ''
     */
    def getPossibleActions = {
            def entities = params.entity ?: []
            
            if( entities instanceof String )
                    entities = [entities]
            else
                    entities = entities.toList()

            if( !entities )
                    entities = [ "Study", "Assay", "Sample" ]

            def actions = [:];
            entities.unique().each { entity ->
                    switch( entity ) {
                            case "Study":
                            case "Assay":
                            case "Sample":
                                    actions[ entity ] = []
                                    break;
                            default:
                                    // Do nothing
                                    break;
                    }
            }
            
            render actions as JSON
    }

    def getFeaturesForAssay = {
        log.trace( "rest.getFeaturesForAssay called" )

        def assayToken = params.assayToken
        def assay = getAssay( assayToken )
        if( !assay ) {
            response.sendError(404)
            return false
        }

        def sql = new Sql(dataSource)

        def features = sql.rows("SELECT f.name, f.unit, fs.template_string_fields_idx, fs.template_string_fields_elt, ft.template_text_fields_idx, ft.template_text_fields_elt, p.name AS platform, p.platformtype, p.platformversion FROM feature AS f LEFT JOIN feature_template_string_fields fs ON (f.id = fs.feature_id) LEFT JOIN feature_template_text_fields ft ON (f.id = ft.feature_id) LEFT JOIN platform p ON (f.platform_id = p.id) WHERE f.id IN (SELECT DISTINCT feature_id FROM measurement WHERE sample_id IN (SELECT id FROM samsample WHERE parent_assay_id = ${assay.id})) ORDER BY f.name ASC;")

        def fMap = [:]
        def propertyMap = [:]
        def i = 0
        features.each() { f ->
            i++
            if (f.template_string_fields_idx) {
                propertyMap.put(f.template_string_fields_idx, f.template_string_fields_elt)
            }
            if (f.template_text_fields_idx) {
                propertyMap.put(f.template_text_fields_idx, f.template_text_fields_elt)
            }
            if (!f.name.equals(features[i]?.name)) {
                propertyMap.put("unit", f.unit)
                propertyMap.put("platform", f.platform)
                propertyMap.put("platformtype", f.platformtype)
                propertyMap.put("platformversion", f.platformversion)
                fMap.put(f.name, propertyMap)
                propertyMap = [:]
            }
        }

        features.clear()
        propertyMap.clear()

        render fMap as JSON
    }

    /**
     * Retrieves an assay from the database, based on a given assay token.
     * @param assayToken	Assaytoken for the assay to retrieve
     * @return				Map containing for each feature another map that
     *                      maps samples to values.
     */
    def getMeasurementData = {
        log.trace( "rest.getMeasurementData called" )

        def assay = getAssay( params.assayToken )

        if( !assay ) {
            response.sendError(404)
            return false
        }

        def sql = new Sql(dataSource)

        def pMeasurements = sql.rows("SELECT m.feature_id AS feature, m.value AS value, m.comments AS comments, y.parent_sample_id as sample FROM measurement m, samsample y WHERE m.sample_id = y.id AND y.parent_assay_id = ${assay.id}")

        def featureMap = sql.rows("SELECT DISTINCT m.feature_id, f.name FROM measurement m JOIN feature f ON m.feature_id = f.id WHERE m.sample_id IN (SELECT id FROM samsample s WHERE s.parent_assay_id = ${assay.id});").collectEntries{ [it.feature_id, it.name]}

        Map result = [:]
        pMeasurements.each {
            def key = featureMap.get(it.feature)
            if (!result.containsKey(key)) {
                result.put(key, [:])
            }
            Map temp = result.get(key)
            temp[it.sample] = it.value != null ? it.value : it.comments
            result.put(key, temp)
        }

        render result as JSON
    }

    /**
     * Retrieves an assay from the database, based on a given assay token.
     * @param assayToken	Assaytoken for the assay to retrieve
     * @return				Assay or null if assayToken doesn't exist
     */
    private def getAssay( def assayToken ) {
        if( !assayToken || assayToken == null ) {
            return null
        }
        def list = []
        def assay = Assay.findWhere(UUID: assayToken )

        return assay;
    }


    /* helper function for getMeasurementData
     *
     * Return compact JSON object for data. The format of the returned array is as follows.
     *
     * The list contains three elements:
     *
     * (1) a list of sampleTokens,
     * (2) a list of measurementTokens,
     * (3) a list of values.
     *
     * The list of values is a matrix represented as a list. Each row of the matrix
     * contains the values of a measurementToken (in the order given in the measurement
     * token list, (2)). Each column of the matrix contains the values for the sampleTokens
     * (in the order given in the list of sampleTokens, (1)).
     */
    private def compactTable( results ) {
        def sampleTokens = results.collect( { it['sampleToken'] } ).unique()
        def measurementTokens = results.collect( { it['measurementToken'] } ).unique()

        def data = []
        measurementTokens.each{ m ->
            sampleTokens.each{ s ->
                def item = results.find{ it['sampleToken']==s && it['measurementToken']==m }
                data.push item ? item['value'] : null
            }
        }

        return [
            sampleTokens,
            measurementTokens,
            data
        ]
    }
}
