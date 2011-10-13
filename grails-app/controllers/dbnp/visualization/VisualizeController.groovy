/**
 * Visualize Controller
 *
 * This controller enables the user to visualize his data
 *
 * @author  robert@thehyve.nl
 * @since	20110825
 * @package	dbnp.visualization
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.visualization

import dbnp.studycapturing.*;
import grails.converters.JSON

import org.dbnp.gdt.*

class VisualizeController {
	def authenticationService
	def moduleCommunicationService
    def infoMessage = []
    def offlineModules = []
    def infoMessageOfflineModules = []
    final int CATEGORICALDATA = 0
    final int NUMERICALDATA = 1
    final int RELTIME = 2
    final int DATE = 3

	/**
	 * Shows the visualization screen
	 */
	def index = {
		[ studies: Study.giveReadableStudies( authenticationService.getLoggedInUser() )]
	}

	def getStudies = {
		def studies = Study.giveReadableStudies( authenticationService.getLoggedInUser() );
        return sendResults(studies)
	}

	/**
	 * Based on the study id contained in the parameters given by the user, a list of 'fields' is returned. This list can be used to select what data should be visualized
	 * @return List containing fields
     * @see parseGetDataParams
	 * @see getFields
	 */
    def getFields = {
		def input_object
		def studies

		try{
			input_object = parseGetDataParams();
		} catch(Exception e) {
			log.error("VisualizationController: getFields: "+e)
            return returnError(400, "An error occured while retrieving the user input.")
		}

        // Check to see if we have enough information
        if(input_object==null || input_object?.studyIds==null){
            setInfoMessage("Please select a study.")
            return sendInfoMessage()
        } else {
            studies = input_object.studyIds[0]
        }

		def fields = [];

        /*
         Gather fields related to this study from GSCF.
         This requires:
         - a study.
         - a category variable, e.g. "events".
         - a type variable, either "domainfields" or "templatefields".
         */
        // TODO: Handle multiple studies
        def study = Study.get(studies)

        if(study!=null){
            fields += getFields(study, "subjects", "domainfields")
            fields += getFields(study, "subjects", "templatefields")
            fields += getFields(study, "events", "domainfields")
            fields += getFields(study, "events", "templatefields")
            fields += getFields(study, "samplingEvents", "domainfields")
            fields += getFields(study, "samplingEvents", "templatefields")
            fields += getFields(study, "assays", "domainfields")
            fields += getFields(study, "assays", "templatefields")
            fields += getFields(study, "samples", "domainfields")
            fields += getFields(study, "samples", "templatefields")

            /*
            Gather fields related to this study from modules.
            This will use the getMeasurements RESTful service. That service returns measurement types, AKA features.
            It does not actually return measurements (the getMeasurementData call does).
            The getFields method (or rather, the getMeasurements service) requires one or more assays and will return all measurement
            types related to these assays.
            So, the required variables for such a call are:
              - a source variable, which can be obtained from AssayModule.list() (use the 'name' field)
              - an assay, which can be obtained with study.getAssays()
             */
            study.getAssays().each { assay ->
                def list = []
                if(!offlineModules.contains(assay.module.id)){
                    list = getFields(assay.module.id, assay)
                    if(list!=null){
                        if(list.size()!=0){
                            fields += list
                        }
                    }
                }
            }
            offlineModules = []

            // Make sure any informational messages regarding offline modules are submitted to the client
            setInfoMessageOfflineModules()


            // TODO: Maybe we should add study's own fields
        } else {
            log.error("VisualizationController: getFields: The requested study could not be found. Id: "+studies)
            return returnError(404, "The requested study could not be found.")
        }

        fields.unique() // Todo: find out root cause of why some fields occur more than once
        fields.sort { a, b ->
            def sourceEquality = a.source.toString().toLowerCase().compareTo(b.source.toString().toLowerCase())
            if( sourceEquality == 0 ) {
                def categoryEquality = a.category.toString().toLowerCase().compareTo(b.category.toString().toLowerCase())
                if( categoryEquality == 0 ){
                    a.name.toString().toLowerCase().compareTo(b.name.toString().toLowerCase())
                } else return categoryEquality
            } else return sourceEquality
        }
		return sendResults(['studyIds': studies, 'fields': fields])
	}

	/**
	 * Based on the field ids contained in the parameters given by the user, a list of possible visualization types is returned. This list can be used to select how data should be visualized.
	 * @return List containing the possible visualization types, with each element containing
     *          - a unique id
     *          - a unique name
     *         For example: ["id": "barchart", "name": "Barchart"]
     * @see parseGetDataParams
	 * @see determineFieldType
     * @see determineVisualizationTypes
	 */
	def getVisualizationTypes = {
        def inputData = parseGetDataParams();

        if(inputData.columnIds == null || inputData.columnIds == [] || inputData.columnIds[0] == null || inputData.columnIds[0] == ""){
            setInfoMessage("Please select a data source for the x-axis.")
            return sendInfoMessage()
        }
        if(inputData.rowIds == null || inputData.rowIds == [] ||  inputData.rowIds[0] == null ||   inputData.rowIds[0] == ""){
            setInfoMessage("Please select a data source for the y-axis.")
            return sendInfoMessage()
        }

        // TODO: handle the case of multiple fields on an axis
        // Determine data types
        log.trace "Determining rowType: "+inputData.rowIds[0]
        def rowType = determineFieldType(inputData.studyIds[0], inputData.rowIds[0])
        
		log.trace "Determining columnType: "+inputData.columnIds[0]
        def columnType = determineFieldType(inputData.studyIds[0], inputData.columnIds[0])

        // Determine possible visualization types
        def types = determineVisualizationTypes(rowType, columnType)

        log.trace  "types: "+types+", determined this based on "+rowType+" and "+columnType
        return sendResults(['types':types,'rowIds':inputData.rowIds[0],'columnIds':inputData.columnIds[0]])
	}

    /**
     * Gather fields related to this study from modules.
        This will use the getMeasurements RESTful service. That service returns measurement types, AKA features.
        getMeasurements does not actually return measurements (the getMeasurementData call does).
     * @param source    The id of the module that is the source of the requested fields, as can be obtained from AssayModule.list() (use the 'id' field)
     * @param assay     The assay that the source module and the requested fields belong to
     * @return  A list of map objects, containing the following:
     *           - a key 'id' with a value formatted by the createFieldId function
     *           - a key 'source' with a value equal to the input parameter 'source'
     *           - a key 'category' with a value equal to the 'name' field of the input paramater 'assay'
     *           - a key 'name' with a value equal to the name of the field in question, as determined by the source value
     */
    def getFields(source, assay){
        def fields = []
        def callUrl = ""

        // Making a different call for each assay
        def urlVars = "assayToken="+assay.assayUUID
        try {
            callUrl = ""+assay.module.url + "/rest/getMeasurements/query?"+urlVars
            def json = moduleCommunicationService.callModuleRestMethodJSON( assay.module.url /* consumer */, callUrl );
            def collection = []
            json.each{ jason ->
                collection.add(jason)
            }
            // Formatting the data
            collection.each { field ->
                // For getting this field from this assay
                fields << [ "id": createFieldId( id: field, name: field, source: assay.id, type: ""+assay.name), "source": source, "category": ""+assay.name, "name": field ]
            }
        } catch(Exception e){
            //returnError(404, "An error occured while trying to collect field data from a module. Most likely, this module is offline.")
            offlineModules.add(assay.module.id)
            infoMessageOfflineModules.add(assay.module.name)
            log.error("VisualizationController: getFields: "+e)
        }

        return fields
    }

    /**
     * Gather fields related to this study from GSCF.
     * @param study The study that is the source of the requested fields
     * @param category  The domain that a field (a property in this case) belongs to, e.g. "subjects", "samplingEvents"
     * @param type A string that indicates the type of field, either "domainfields" or "templatefields".
     * @return A list of map objects, formatted by the formatGSCFFields function
     */
    def getFields(study, category, type){
        // Collecting the data from it's source
        def collection = []
        def fields = []
        def source = "GSCF"
		
		if( type == "domainfields" ) 
			collection = domainObjectCallback( category )?.giveDomainFields();
		else
			collection = templateObjectCallback( category, study )?.template?.fields

        collection?.unique()

        // Formatting the data
        fields += formatGSCFFields(type, collection, source, category)

        // Here we will remove those fields, whose set of datapoints only contain null
        def fieldsToBeRemoved = []
        fields.each{ field ->
            def fieldData = getFieldData( study, study.samples, field.id )
            fieldData.removeAll([null])
            if(fieldData==[]){
                // Field only contained nulls, so don't show it as a visualization option
                fieldsToBeRemoved << field
            }
        }
        fields.removeAll(fieldsToBeRemoved)

        return fields
    }

    /**
     * Format the data contained in the input parameter 'collection' for use as so-called fields, that will be used by the user interface to allow the user to select data from GSCF for visualization
     * @param type A string that indicates the type of field, either "domainfields" or "templatefields".
     * @param collectionOfFields A collection of fields, which could also contain only one item
     * @param source Likely to be "GSCF"
     * @param category The domain that a field (a property in this case) belongs to, e.g. "subjects", "samplingEvents"
     * @return A list containing list objects, containing the following:
     *           - a key 'id' with a value formatted by the createFieldId function
     *           - a key 'source' with a value equal to the input parameter 'source'
     *           - a key 'category' with a value equal to the input parameter 'category'
     *           - a key 'name' with a value equal to the name of the field in question, as determined by the source value
     */
    def formatGSCFFields(type, collectionOfFields, source, category){

        if(collectionOfFields==null || collectionOfFields == []){
            return []
        }
        def fields = []
        if(collectionOfFields instanceof Collection){
            // Apparently this field is actually a list of fields.
            // We will call ourselves again with the list's elements as input.
            // These list elements will themselves go through this check again, effectively flattening the original input
            for(int i = 0; i < collectionOfFields.size(); i++){
                fields += formatGSCFFields(type, collectionOfFields[i], source, category)
            }
            return fields
        } else {
            // This is a single field. Format it and return the result.
            if(type=="domainfields"){
                fields << [ "id": createFieldId( id: collectionOfFields.name, name: collectionOfFields.name, source: source, type: category ), "source": source, "category": category, "name": collectionOfFields.name ]
            }
            if(type=="templatefields"){
                fields << [ "id": createFieldId( id: collectionOfFields.id, name: collectionOfFields.name, source: source, type: category ), "source": source, "category": category, "name": collectionOfFields.name ]
            }
            return fields
        }
    }

	/**
	 * Retrieves data for the visualization itself.
     * Returns, based on the field ids contained in the parameters given by the user, a map containing the actual data and instructions on how the data should be visualized.
     * @return A map containing containing (at least, in the case of a barchart) the following:
     *           - a key 'type' containing the type of chart that will be visualized
     *           - a key 'xaxis' containing the title and unit that should be displayed for the x-axis
     *           - a key 'yaxis' containing the title and unit that should be displayed for the y-axis*
     *           - a key 'series' containing a list, that contains one or more maps, which contain the following:
     *                - a key 'name', containing, for example, a feature name or field name
     *                - a key 'y', containing a list of y-values
     *                - a key 'error', containing a list of, for example, standard deviation or standard error of the mean values, each having the same index as the 'y'-values they are associated with
	 */
	def getData = {
		// Extract parameters
		// TODO: handle erroneous input data
		def inputData = parseGetDataParams();

        if(inputData.columnIds == null || inputData.rowIds == null){
            infoMessage = "Please select data sources for the y- and x-axes."
            return sendInfoMessage()
        }

		// TODO: handle the case that we have multiple studies
		def studyId = inputData.studyIds[ 0 ];
		def study = Study.get( studyId as Integer );

		// Find out what samples are involved
		def samples = study.samples

		// Retrieve the data for both axes for all samples
		// TODO: handle the case of multiple fields on an axis
		def fields = [ "x": inputData.columnIds[ 0 ], "y": inputData.rowIds[ 0 ] ];
		def data = getAllFieldData( study, samples, fields );

		// Group data based on the y-axis if categorical axis is selected
        def groupedData
        if(inputData.visualizationType=='horizontal_barchart'){
            groupedData = groupFieldData( inputData.visualizationType, data, "y", "x" ); // Indicate non-standard axis ordering
        } else {
            groupedData = groupFieldData( inputData.visualizationType, data ); // Don't indicate axis ordering, standard <"x", "y"> will be used
        }
        // Format data so it can be rendered as JSON
        def returnData
        if(inputData.visualizationType=='horizontal_barchart'){
            def valueAxisType = determineFieldType(inputData.studyIds[0], inputData.rowIds[0], groupedData["x"])
            def groupAxisType = determineFieldType(inputData.studyIds[0], inputData.columnIds[0], groupedData["y"])
            returnData = formatData( inputData.visualizationType, groupedData, fields, groupAxisType, valueAxisType , "y", "x" ); // Indicate non-standard axis ordering
        } else {
            def valueAxisType = determineFieldType(inputData.studyIds[0], inputData.rowIds[0], groupedData["y"])
            def groupAxisType = determineFieldType(inputData.studyIds[0], inputData.columnIds[0], groupedData["x"])
            returnData = formatData( inputData.visualizationType, groupedData, fields, groupAxisType, valueAxisType ); // Don't indicate axis ordering, standard <"x", "y"> will be used
        }
        return sendResults(returnData)
	}

	/**
	 * Parses the parameters given by the user into a proper list
	 * @return Map with 4 keys:
	 * 		studyIds: 	list with studyIds selected
	 * 		rowIds:		list with fieldIds selected for the rows
	 * 		columnIds:	list with fieldIds selected for the columns
	 * 		visualizationType:	String with the type of visualization required
	 * @see getFields
	 * @see getVisualizationTypes
	 */
	def parseGetDataParams() {
		def studyIds, rowIds, columnIds, visualizationType;
		
		studyIds = params.list( 'study' );
		rowIds = params.list( 'rows' );
		columnIds = params.list( 'columns' );
		visualizationType = params.get( 'types')

		return [ "studyIds" : studyIds, "rowIds": rowIds, "columnIds": columnIds, "visualizationType": visualizationType ];
	}

	/**
	 * Retrieve the field data for the selected fields
	 * @param study		Study for which the data should be retrieved
	 * @param samples	Samples for which the data should be retrieved
	 * @param fields	Map with key-value pairs determining the name and fieldId to retrieve data for. Example:
	 * 						[ "x": "field-id-1", "y": "field-id-3" ]
	 * @return			A map with the same keys as the input fields. The values in the map are lists of values of the
	 * 					selected field for all samples. If a value could not be retrieved for a sample, null is returned. Example:
	 * 						[ "x": [ 3, 6, null, 10 ], "y": [ "male", "male", "female", "female" ] ]
	 */
	def getAllFieldData( study, samples, fields ) {
		def fieldData = [:]
		fields.each{ field ->
			fieldData[ field.key ] = getFieldData( study, samples, field.value );
		}
		
		return fieldData;
	}
	
	/**
	* Retrieve the field data for the selected field
	* @param study		Study for which the data should be retrieved
	* @param samples	Samples for which the data should be retrieved
	* @param fieldId	ID of the field to return data for
	* @return			A list of values of the selected field for all samples. If a value 
	* 					could not be retrieved for a sample, null is returned. Examples:
	* 						[ 3, 6, null, 10 ] or [ "male", "male", "female", "female" ]
	*/
	def getFieldData( study, samples, fieldId ) {
		// Parse the fieldId as given by the user
		def parsedField = parseFieldId( fieldId );
		
		def data = []
		
		if( parsedField.source == "GSCF" ) {
			// Retrieve data from GSCF itself
			def closure = valueCallback( parsedField.type )

			if( closure ) {
				samples.each { sample ->
					// Retrieve the value for the selected field for this sample
					def value = closure( sample, parsedField.name );
					
					if( value != null ) {
						data << value;
					} else {
						// Return null if the value is not found
						data << null
					}
				}
			} else {
				// TODO: Handle error properly
				// Closure could not be retrieved, probably because the type is incorrect
				data = samples.collect { return null }
                log.error("VisualizationController: getFieldData: Requested wrong field type: "+parsedField.type+". Parsed field: "+parsedField)
			}
		} else {
			// Data must be retrieved from a module
			data = getModuleData( study, samples, parsedField.source, parsedField.name );
		}
		
		return data
	}
	
	/**
	 * Retrieve data for a given field from a data module
	 * @param study			Study to retrieve data for
	 * @param samples		Samples to retrieve data for
	 * @param source_module	Name of the module to retrieve data from
	 * @param fieldName		Name of the measurement type to retrieve (i.e. measurementToken)
	 * @return				A list of values of the selected field for all samples. If a value 
	 * 						could not be retrieved for a sample, null is returned. Examples:
	 * 							[ 3, 6, null, 10 ] or [ "male", "male", "female", "female" ]
	 */
	def getModuleData( study, samples, assay_id, fieldName ) {
		def data = []
		
		// TODO: Handle values that should be retrieved from multiple assays
        def assay = Assay.get(assay_id);

        if( assay ) {
            // Request for a particular assay and a particular feature
            def urlVars = "assayToken=" + assay.assayUUID + "&measurementToken="+fieldName
            urlVars += "&" + samples.collect { "sampleToken=" + it.sampleUUID }.join( "&" );

            def callUrl
            try {
                callUrl = assay.module.url + "/rest/getMeasurementData"
                def json = moduleCommunicationService.callModuleMethod( assay.module.url, callUrl, urlVars, "POST" );

                if( json ) {
                    // First element contains sampletokens
                    // Second element contains the featurename
                    // Third element contains the measurement value
                    def sampleTokens = json[ 0 ]
                    def measurements = json[ 2 ]

                    // Loop through the samples
                    samples.each { sample ->
                        // Search for this sampletoken
                        def sampleToken = sample.sampleUUID;
                        def index = sampleTokens.findIndexOf { it == sampleToken }

                        if( index > -1 ) {
                            data << measurements[ index ];
                        } else {
                            data << null
                        }
                    }
                } else {
                    // TODO: handle error
                    // Returns an empty list with as many elements as there are samples
                    data = samples.collect { return null }
                }

            } catch(Exception e){
                log.error("VisualizationController: getFields: "+e)
                //return returnError(404, "An error occured while trying to collect data from a module. Most likely, this module is offline.")
                return returnError(404, "Unfortunately, "+assay.module.name+" could not be reached. As a result, we cannot at this time visualize data contained in this module.")
            }
        } else {
            // TODO: Handle error correctly
            // Returns an empty list with as many elements as there are samples
            data = samples.collect { return null }
        }

        //println "\t data request: "+data
		return data
	}

	/**
	 * Group the field data on the values of the specified axis. For example, for a bar chart, the values 
	 * on the x-axis should be grouped. Currently, the values for each group are averaged, and the standard
	 * error of the mean is returned in the 'error' property
     * @param visualizationType Some types require a different formatting/grouping of the data, such as 'table'
	 * @param data		Data for both group- and value axis. The output of getAllFieldData fits this input
	 * @param groupAxis	Name of the axis to group on. Defaults to "x"
	 * @param valueAxis	Name of the axis where the values are. Defaults to "y"
	 * @param errorName	Key in the output map where 'error' values (SEM) are stored. Defaults to "error" 
	 * @param unknownName	Name of the group for all null groups. Defaults to "unknown"
	 * @return			A map with the keys 'groupAxis', 'valueAxis' and 'errorName'. The values in the map are lists of values of the
	 * 					selected field for all groups. For example, if the input is
	 * 						[ "x": [ "male", "male", "female", "female", null, "female" ], "y": [ 3, 6, null, 10, 4, 5 ] ]
	 * 					the output will be:
	 * 						[ "x": [ "male", "female", "unknown" ], "y": [ 4.5, 7.5, 4 ], "error": [ 1.5, 2.5, 0 ] ]
	 *
	 * 					As you can see: null values in the valueAxis are ignored. Null values in the 
	 * 					group axis are combined into a 'unknown' category.
	 */
	def groupFieldData( visualizationType, data, groupAxis = "x", valueAxis = "y", errorName = "error", unknownName = "unknown" ) {
		// TODO: Give the user the possibility to change this value in the user interface
		def showEmptyCategories = false;
		
		// Create a unique list of values in the groupAxis. First flatten the list, since it might be that a
		// sample belongs to multiple groups. In that case, the group names should not be the lists, but the list
		// elements. A few lines below, this case is handled again by checking whether a specific sample belongs
		// to this group. 
		// After flattening, the list is uniqued. The closure makes sure that values with different classes are
		// always treated as different items (e.g. "" should not equal 0, but it does if using the default comparator)
		def groups = data[ groupAxis ]
						.flatten()
						.unique { it == null ? "null" : it.class.name + it.toString() }
						
		// Make sure the null category is last
		groups = groups.findAll { it != null } + groups.findAll { it == null }
		
		// Generate the output object
		def outputData = [:]
		outputData[ valueAxis ] = [];
		outputData[ errorName ] = [];
		outputData[ groupAxis ] = [];
		
		// Loop through all groups, and gather the values for this group
        // A visualization of type 'table' is a special case. There, the counts of two combinations of 'groupAxis'
		// and 'valueAxis' items are computed
        if( visualizationType=='table' ){
            // For each 'valueAxis' item and 'groupAxis' item combination, count how often they appear together.
            def counts = [:]
			
            // The 'counts' list uses keys like this: ['item1':group, 'item2':value]
            // The value will be an integer (the count)
            data[ groupAxis ].eachWithIndex { group, index ->
                def value =  data[ valueAxis ][index]
                if(!counts.get(['item1':group, 'item2':value])){
                    counts[['item1':group, 'item2':value]] = 1
                } else {
                    counts[['item1':group, 'item2':value]] = counts[['item1':group, 'item2':value]] + 1
                }
            }
            
			def valueData =  data[ valueAxis ]
                                        .flatten()
                                        .unique { it == null ? "null" : it.class.name + it.toString() }
													
			// Now we will first check whether any of the categories is empty. If some of the rows
			// or columns are empty, don't include them in the output
			if( !showEmptyCategories ) {
				groups.eachWithIndex { group, index ->
					if( counts.findAll { it.key.item1 == group } )
						outputData[groupAxis] << group
				}
				
				valueData.each { value ->
					if( counts.findAll { it.key.item2 == value } )
						outputData[valueAxis] << value
				}
			} else {
				outputData[groupAxis] = groups.collect { it != null ? it : unknownName }
				ouputData[valueAxis] = valueData
			}
																
            // Because we are collecting counts, we do not set the 'errorName' item of the 'outputData' map.
            // We do however set the 'data' map to contain the counts. We set it in such a manner that it has 
			// a 'table' layout with respect to 'groupAxis' and 'valueAxis'.
            def rows = []
            outputData[groupAxis].each{ group ->
                def row = []
                outputData[valueAxis].each{ value ->
                    row << counts[['item1':group, 'item2':value]]
                }
                while(row.contains(null)){
                    row[row.indexOf(null)] = 0
                } // 'null' should count as '0'. Items of count zero have never been added to the 'counts' list and as such will appear as a 'null' value.
                if(row!=[]) rows << row
            }
            outputData['data']= rows
			
			// Convert groups to group names
			outputData[ groupAxis ] = outputData[ groupAxis ].collect { it != null ? it : unknownName }
        } else {
            groups.each { group ->
                // Find the indices of the samples that belong to this group. if a sample belongs to multiple groups (i.e. if
                // the samples groupAxis contains multiple values, is a collection), the value should be used in all groups.
                def indices = data[ groupAxis ].findIndexValues { it instanceof Collection ? it.contains( group ) : it == group };
                def values  = data[ valueAxis ][ indices ]

				// The computation for mean and error will return null if no (numerical) values are found
				// In that case, the user won't see this category
                def dataForGroup = null;
                switch( params.get( 'aggregation') ) {
			        case "average":
                        dataForGroup = computeMeanAndError( values );
                        break;
                    case "count":
                        dataForGroup = computeCount( values );
                        break;
                    case "median":
                        dataForGroup = computeMedian( values );
                        break;
                    case "none":
                        // Currently disabled, create another function
                        dataForGroup = computeMeanAndError( values );
                        break;
                    case "sum":
                        dataForGroup = computeSum( values );
                        break;
                    default:
                        // Default is "average"
                        dataForGroup = computeMeanAndError( values );
                }


				if( showEmptyCategories || dataForGroup.value != null ) {
					// Gather names for the groups. Most of the times, the group names are just the names, only with
					// a null value, the unknownName must be used
					outputData[ groupAxis ] << ( group != null ? group : unknownName )
	                outputData[ valueAxis ] << dataForGroup.value ?: 0
	                outputData[ errorName ] << dataForGroup.error ?: 0
				}
            }
        }
		
		return outputData
	}
	
	/**
	 * Formats the grouped data in such a way that the clientside visualization method 
	 * can handle the data correctly.
	 * @param groupedData	Data that has been grouped using the groupFields method
	 * @param fields		Map with key-value pairs determining the name and fieldId to retrieve data for. Example:
	 * 							[ "x": "field-id-1", "y": "field-id-3" ]
     * @param groupAxisType Integer, either CATEGORICAL or NUMERIACAL
     * @param valueAxisType Integer, either CATEGORICAL or NUMERIACAL
	 * @param groupAxis		Name of the axis to with group data. Defaults to "x"
	 * @param valueAxis		Name of the axis where the values are stored. Defaults to "y"
	 * @param errorName		Key in the output map where 'error' values (SEM) are stored. Defaults to "error" 	 * 
	 * @return				A map like the following:
	 * 
	  		{
				"type": "barchart",
				"xaxis": { "title": "quarter 2011", "unit": "" },
				"yaxis": { "title": "temperature", "unit": "degrees C" },
				"series": [
					{
						"name": "series name",
						"y": [ 5.1, 3.1, 20.6, 15.4 ],
                        "x": [ "Q1", "Q2", "Q3", "Q4" ],
						"error": [ 0.5, 0.2, 0.4, 0.5 ]
					},
				]
			}
	 * 
	 */
	def formatData( type, groupedData, fields, groupAxisType, valueAxisType, groupAxis = "x", valueAxis = "y", errorName = "error" ) {
		// We want to sort the data based on the group-axis, but keep the values on the value-axis in sync.
		// The only way seems to be to combine data from both axes.
        def combined = []
        if(type=="table"){
            groupedData[ groupAxis ].eachWithIndex { group, i ->
                combined << [ "group": group, "data": groupedData[ 'data' ][ i ] ]
            }
            combined.sort { it.group.toString() }
            groupedData[groupAxis] = renderTimesAndDatesHumanReadable(combined*.group, groupAxisType)
            groupedData[valueAxis] = renderTimesAndDatesHumanReadable(groupedData[valueAxis], valueAxisType)
            groupedData["data"] = combined*.data
        } else {
            groupedData[ groupAxis ].eachWithIndex { group, i ->
                combined << [ "group": group, "value": groupedData[ valueAxis ][ i ] ]
            }
            combined.sort { it.group.toString() }
            groupedData[groupAxis] = renderTimesAndDatesHumanReadable(combined*.group, groupAxisType)
            groupedData[valueAxis] = renderTimesAndDatesHumanReadable(combined*.value, valueAxisType)
        }
        // TODO: Handle name and unit of fields correctly
        def valueAxisTypeString = (valueAxisType==CATEGORICALDATA || valueAxisType==DATE || valueAxisType==RELTIME ? "categorical" : "numerical")
        def groupAxisTypeString = (groupAxisType==CATEGORICALDATA || groupAxisType==DATE || groupAxisType==RELTIME ? "categorical" : "numerical")

        if(type=="table"){
            def return_data = [:]
            return_data[ "type" ] = type
            return_data.put("yaxis", ["title" : parseFieldId( fields[ valueAxis ] ).name, "unit" : "", "type":valueAxisTypeString ])
            return_data.put("xaxis", ["title" : parseFieldId( fields[ groupAxis ] ).name, "unit": "", "type":groupAxisTypeString ])
            return_data.put("series", [[
                    "x": groupedData[ groupAxis ].collect { it.toString() },
                    "y": groupedData[ valueAxis ].collect { it.toString() },
                    "data": groupedData["data"]
            ]])
            return return_data;
        } else {
            def xAxis = groupedData[ groupAxis ].collect { it.toString() };
            def yName = parseFieldId( fields[ valueAxis ] ).name;

            def return_data = [:]
            return_data[ "type" ] = type
            return_data.put("yaxis", ["title" : yName, "unit" : "", "type":valueAxisTypeString ])
            return_data.put("xaxis", ["title" : parseFieldId( fields[ groupAxis ] ).name, "unit": "", "type":groupAxisTypeString  ])
            return_data.put("series", [[
                "name": yName,
                "x": xAxis,
                "y": groupedData[ valueAxis ],
                "error": groupedData[ errorName ]
            ]])

            return return_data;
        }
	}

    /**
     * If the input variable 'data' contains dates or times according to input variable 'fieldInfo', these dates and times are converted to a human-readable version.
     * @param data  The list of items that needs to be checked/converted
     * @param axisType As determined by determineFieldType
     * @return The input variable 'data', with it's date and time elements converted.
     * @see determineFieldType
     */
    def renderTimesAndDatesHumanReadable(data, axisType){
        if(axisType==RELTIME){
            data = renderTimesHumanReadable(data)
        }
        if(axisType==DATE){
           data = renderDatesHumanReadable(data)
        }
        return data
    }

    /**
     * Takes a one-dimensional list, returns the list with the appropriate items converted to a human readable string
     * @param data
     * @return
     */
    def renderTimesHumanReadable(data){
        def tmpTimeContainer = []
        data. each {
            if(it instanceof Number) {
                try{
                    tmpTimeContainer << new RelTime( it ).toPrettyString()
                } catch(IllegalArgumentException e){
                    tmpTimeContainer << it
                }
            } else {
                tmpTimeContainer << it // To handle items such as 'unknown'
            }
        }
        return tmpTimeContainer
    }

    /**
     * Takes a one-dimensional list, returns the list with the appropriate items converted to a human readable string
     * @param data
     * @return
     */
    def renderDatesHumanReadable(data) {
        def tmpDateContainer = []
        data. each {
            if(it instanceof Number) {
                try{
                    tmpDateContainer << new java.util.Date( (Long) it ).toString()
                } catch(IllegalArgumentException e){
                    tmpDateContainer << it
                }
            } else {
                tmpDateContainer << it // To handle items such as 'unknown'
            }
        }
        return tmpDateContainer
    }
	/**
	 * Returns a closure for the given entitytype that determines the value for a criterion
	 * on the given object. The closure receives two parameters: the sample and a field.
	 *
	 * For example:
	 * 		How can one retrieve the value for subject.name, given a sample? This can be done by 
	 * 		returning the field values sample.parentSubject:
	 * 			{ sample, field -> return getFieldValue( sample.parentSubject, field ) }
	 * @return	Closure that retrieves the value for a field and the given field
	 */
	protected Closure valueCallback( String entity ) {
		switch( entity ) {
			case "Study":
			case "studies":
				return { sample, field -> return getFieldValue( sample.parent, field ) }
			case "Subject":
			case "subjects":
				return { sample, field -> return getFieldValue( sample.parentSubject, field ); }
			case "Sample":
			case "samples":
				return { sample, field -> return getFieldValue( sample, field ) }
			case "Event":
			case "events":
				return { sample, field ->
					if( !sample || !sample.parentEventGroup || !sample.parentEventGroup.events || sample.parentEventGroup.events.size() == 0 )
						return null

					return sample.parentEventGroup.events?.collect { getFieldValue( it, field ) };
				}
			case "SamplingEvent":
			case "samplingEvents":
				return { sample, field -> return getFieldValue( sample.parentEvent, field ); }
			case "Assay":
			case "assays":
				return { sample, field ->
					def sampleAssays = Assay.findByParent( sample.parent ).findAll { it.samples?.contains( sample ) };
					if( sampleAssays && sampleAssays.size() > 0 )
						return sampleAssays.collect { getFieldValue( it, field ) }
					else
						return null
				}
		}
	}
	
	/**
	* Returns the domain object that should be used with the given entity string 
	*
	* For example:
	* 		What object should be consulted if the user asks for "studies"
	* 		Response: Study
	* @return	Domain object that should be used with the given entity string
	*/
   protected def domainObjectCallback( String entity ) {
	   switch( entity ) {
		   case "Study":
		   case "studies":
			   return Study
		   case "Subject":
		   case "subjects":
			   return Subject
		   case "Sample":
		   case "samples":
			   return Sample
		   case "Event":
		   case "events":
		        return Event
		   case "SamplingEvent":
		   case "samplingEvents":
			   return SamplingEvent
		   case "Assay":
		   case "assays":
		   		return Assay
	   }
   }

    /**
    * Returns the objects within the given study that should be used with the given entity string
    *
    * For example:
    * 		What object should be consulted if the user asks for "samples"
    * 		Response: study.samples
    * @return	List of domain objects that should be used with the given entity string
    */
    protected def templateObjectCallback( String entity, Study study ) {
      switch( entity ) {
          case "Study":
          case "studies":
              return study
          case "Subject":
          case "subjects":
              return study?.subjects
          case "Sample":
          case "samples":
              return study?.samples
          case "Event":
          case "events":
               return study?.events
          case "SamplingEvent":
          case "samplingEvents":
              return study?.samplingEvents
          case "Assay":
          case "assays":
                  return study?.assays
      }
    }
	
	/**
	 * Computes the mean value and Standard Error of the mean (SEM) for the given values
	 * @param values	List of values to compute the mean and SEM for. Strings and null 
	 * 					values are ignored 
	 * @return			Map with two keys: 'value' and 'error'
	 */
	protected Map computeMeanAndError( values ) {
		// TODO: Handle the case that one of the values is a list. In that case,
		// all values should be taken into account.	
		def mean = computeMean( values );
		def error = computeSEM( values, mean );
		
		return [ 
			"value": mean,
			"error": error
		]
	}
	
	/**
	 * Computes the mean of the given values. Values that can not be parsed to a number
	 * are ignored. If no values are given, null is returned.
	 * @param values	List of values to compute the mean for
	 * @return			Arithmetic mean of the values
	 */
	protected def computeMean( List values ) {
		def sumOfValues = 0;
		def sizeOfValues = 0;
		values.each { value ->
			def num = getNumericValue( value );
			if( num != null ) {
				sumOfValues += num;
				sizeOfValues++
			}
		}

		if( sizeOfValues > 0 )
			return sumOfValues / sizeOfValues;
		else
			return null;
	}

	/**
	* Computes the standard error of mean of the given values. 
	* Values that can not be parsed to a number are ignored. 
	* If no values are given, null is returned.
	* @param values		List of values to compute the standard deviation for
	* @param mean		Mean of the list (if already computed). If not given, the mean 
	* 					will be computed using the computeMean method
	* @return			Standard error of the mean of the values or 0 if no values can be used.
	*/
    protected def computeSEM( List values, def mean = null ) {
       if( mean == null )
            mean = computeMean( values )

       def sumOfDifferences = 0;
       def sizeOfValues = 0;
       values.each { value ->
           def num = getNumericValue( value );
           if( num != null ) {
               sumOfDifferences += Math.pow( num - mean, 2 );
               sizeOfValues++
           }
       }

       if( sizeOfValues > 0 ) {
           def std = Math.sqrt( sumOfDifferences / sizeOfValues );
           return std / Math.sqrt( sizeOfValues );
       } else {
           return null;
       }
    }

    /**
	 * Computes the median of the given values. Values that can not be parsed to a number
	 * are ignored. If no values are given, null is returned.
	 * @param values	List of values to compute the median for
	 * @return			Median of the values
	 */
	protected def computeMedian( List values ) {
		def listOfValues = [];
		values.each { value ->
			def num = getNumericValue( value );
			if( num != null ) {
				listOfValues << num;
			}
		}

        listOfValues.sort();

        def listSize = listOfValues.size();

		if( listSize > 0 ) {
            def listHalf = (int) Math.abs(listSize/2);
            if(listSize%2==0) {
                // If the list is of an even size, take the mean of the middle two value's
                return ["value": (listOfValues.get(listHalf)+listOfValues.get(listHalf-1))/2];
            } else {
                // If the list is of an odd size, take the middle value
                return ["value": listOfValues.get(listHalf-1)];
            }
        } else
			return ["value": null];
	}

    /**
	 * Computes the count of the given values. Values that can not be parsed to a number
	 * are ignored. If no values are given, null is returned.
	 * @param values	List of values to compute the count for
	 * @return			Count of the values
	 */
	protected def computeCount( List values ) {
		def sumOfValues = 0;
		def sizeOfValues = 0;
		values.each { value ->
			def num = getNumericValue( value );
			if( num != null ) {
				sumOfValues += num;
				sizeOfValues++
			}
		}

		if( sizeOfValues > 0 )
			return ["value": sizeOfValues];
		else
			return ["value": null];
	}

    /**
	 * Computes the sum of the given values. Values that can not be parsed to a number
	 * are ignored. If no values are given, null is returned.
	 * @param values	List of values to compute the sum for
	 * @return			Arithmetic sum of the values
	 */
	protected def computeSum( List values ) {
		def sumOfValues = 0;
		def sizeOfValues = 0;
		values.each { value ->
			def num = getNumericValue( value );
			if( num != null ) {
				sumOfValues += num;
				sizeOfValues++
			}
		}

		if( sizeOfValues > 0 )
			return ["value": sumOfValues];
		else
			return ["value": null];
	}
    
	/**
	 * Return the numeric value of the given object, or null if no numeric value could be returned
	 * @param 	value	Object to return the value for
	 * @return			Number that represents the given value
	 */
	protected Number getNumericValue( value ) {
		// TODO: handle special types of values
		if( value instanceof Number ) {
			return value;
		} else if( value instanceof RelTime ) {
			return value.value;
		}
		
		return null
	}

	/** 
	 * Returns a field for a given templateentity
	 * @param object	TemplateEntity (or subclass) to retrieve data for
	 * @param fieldName	Name of the field to return data for.
	 * @return			Value of the field or null if the value could not be retrieved
	 */
	protected def getFieldValue( TemplateEntity object, String fieldName ) {
		if( !object || !fieldName )
			return null;
		
		try {
			return object.getFieldValue( fieldName );
		} catch( Exception e ) {
			return null;
		}
	}

	/**
	 * Parses a fieldId that has been created earlier by createFieldId
	 * @param fieldId	FieldId to parse
	 * @return			Map with attributes of the selected field. Keys are 'name', 'id', 'source' and 'type'
	 * @see createFieldId
	 */
	protected Map parseFieldId( String fieldId ) {
		def attrs = [:]
		
		def parts = fieldId.split(",")
		
		attrs = [
			"id": parts[ 0 ],
			"name": parts[ 1 ],
			"source": parts[ 2 ],
			"type": parts[ 3 ]
		]
	}
	
	/**
	 * Create a fieldId based on the given attributes
	 * @param attrs		Map of attributes for this field. Keys may be 'name', 'id', 'source' and 'type'
	 * @return			Unique field ID for these parameters
	 * @see parseFieldId
	 */
	protected String createFieldId( Map attrs ) {
		// TODO: What if one of the attributes contains a comma?
		def name = attrs.name;
		def id = attrs.id ?: name;
		def source = attrs.source;
		def type = attrs.type ?: ""
		
		return id + "," + name + "," + source + "," + type;
	}

    /**
     * Set the response code and an error message
     * @param code HTTP status code
     * @param msg Error message, string
     */
    protected void returnError(code, msg){
        response.sendError(code , msg)
    }

    /**
     * Determines what type of data a field contains
     * @param studyId An id that can be used with Study.get/1 to retrieve a study from the database
     * @param fieldId The field id as returned from the client, will be used to retrieve the data required to determine the type of data a field contains
     * @param inputData Optional parameter that contains the data we are computing the type of. When including in the function call we do not need to request data from a module, should the data belong to a module
     * @return Either CATEGORICALDATA, NUMERICALDATA, DATE or RELTIME
     */
    protected int determineFieldType(studyId, fieldId, inputData = null){
		def parsedField = parseFieldId( fieldId );
        def study = Study.get(studyId)
		def data = []

        try{
            if( parsedField.source == "GSCF" ) {
                if(parsedField.id.isNumber()){
                        return determineCategoryFromTemplateFieldId(parsedField.id)
                } else { // Domainfield or memberclass
                    def callback = domainObjectCallback( parsedField.type )

                    // Can the field be found in the domainFields as well? If so, treat it as a template field, so that dates and times can be properly rendered in a human-readable fashion
                    if(callback?.giveDomainFields().name.contains(parsedField.name.toString())){
                        // Use the associated templateField to determine the field type
                        return determineCategoryFromTemplateField(
                                callback?.giveDomainFields()[
                                    callback?.giveDomainFields().name.indexOf(parsedField.name.toString())
                                ]
                        )
                    }
                    // Apparently it is not a templatefield as well as a memberclass

                    def field = callback?.declaredFields.find { it.name == parsedField.name };
                    if( field ) {
                        return determineCategoryFromClass( field.getType() )
                    } else {
                        // TODO: how do we communicate this to the user? Do we allow the process to proceed?
                        log.error( "The user asked for field " + parsedField.type + " - " + parsedField.name + ", but it doesn't exist." );
                    }
                }
            } else {
                if(inputData == null){ // If we did not get data, we need to request it from the module first
                    data = getModuleData( study, study.getSamples(), parsedField.source, parsedField.name );
                    return determineCategoryFromData(data)
                } else {
                    return determineCategoryFromData(inputData)
                }
            }
        } catch(Exception e){
            log.error("VisualizationController: determineFieldType: "+e)
            e.printStackTrace()
            // If we cannot figure out what kind of a datatype a piece of data is, we treat it as categorical data
            return CATEGORICALDATA
        }
    }

    /**
     * Determines a field category, based on the input parameter 'classObject', which is an instance of type 'class'
     * @param classObject
     * @return Either CATEGORICALDATA of NUMERICALDATA
     */
    protected int determineCategoryFromClass(classObject){
        log.trace "Determine category from class: "+classObject+", of class: "+classObject?.class
        if(classObject==java.lang.String){
            return CATEGORICALDATA
        } else {
            return NUMERICALDATA
        }
    }

    /**
     * Determines a field category based on the actual data contained in the field. The parameter 'inputObject' can be a single item with a toString() function, or a collection of such items.
     * @param inputObject Either a single item, or a collection of items
     * @return Either CATEGORICALDATA of NUMERICALDATA
     */
    protected int determineCategoryFromData(inputObject){
        def results = []
		
        if(inputObject instanceof Collection){
            // This data is more complex than a single value, so we will call ourselves again so we c
            inputObject.each {
				if( it != null )
                	results << determineCategoryFromData(it)
            }
        } else {
			// Unfortunately, the JSON null object doesn't resolve to false or equals null. For that reason, we 
			// exclude those objects explicitly here.
			if( inputObject != null && inputObject?.class != org.codehaus.groovy.grails.web.json.JSONObject$Null ) {
	            if(inputObject.toString().isDouble()){
	                results << NUMERICALDATA
	            } else {
	                results << CATEGORICALDATA
	            }
			}
        }

        results.unique()

        if(results.size() > 1) {
            // If we cannot figure out what kind of a datatype a piece of data is, we treat it as categorical data
            results[0] = CATEGORICALDATA
        } else if( results.size() == 0 ) {
			// If the list is empty, return the numerical type. If it is the only value, if will
			// be discarded later on. If there are more entries (e.g part of a collection)
			// the values will be regarded as numerical, if the other values are numerical	
			results[ 0 ] = NUMERICALDATA
    	}

		return results[0]
    }

    /**
     * Determines a field category, based on the TemplateFieldId of a Templatefield
     * @param id A database ID for a TemplateField
     * @return Either CATEGORICALDATA of NUMERICALDATA
     */
    protected int determineCategoryFromTemplateFieldId(id){
        TemplateField tf = TemplateField.get(id)
        return determineCategoryFromTemplateField(tf)
    }

    /**
     * Determines a field category, based on the TemplateFieldType of a Templatefield
     * @param id A database ID for a TemplateField
     * @return Either CATEGORICALDATA of NUMERICALDATA
     */
    protected int determineCategoryFromTemplateField(tf){
        if(tf.type==TemplateFieldType.DOUBLE || tf.type==TemplateFieldType.LONG){
            log.trace "GSCF templatefield: NUMERICALDATA ("+NUMERICALDATA+") (based on "+tf.type+")"
            return NUMERICALDATA
        }
        if(tf.type==TemplateFieldType.DATE){
            log.trace "GSCF templatefield: DATE ("+DATE+") (based on "+tf.type+")"
            return DATE
        }
        if(tf.type==TemplateFieldType.RELTIME){
            log.trace "GSCF templatefield: RELTIME ("+RELTIME+") (based on "+tf.type+")"
            return RELTIME
        }
        log.trace "GSCF templatefield: CATEGORICALDATA ("+CATEGORICALDATA+") (based on "+tf.type+")"
        return CATEGORICALDATA
    }
    /**
     * Properly formats the object that will be returned to the client. Also adds an informational message, if that message has been set by a function. Resets the informational message to the empty String.
     * @param returnData The object containing the data
     * @return results A JSON object
     */
    protected void sendResults(returnData){
        def results = [:]
        if(infoMessage.size()!=0){
            results.put("infoMessage", infoMessage)
            infoMessage = []
        }
        results.put("returnData", returnData)
        render results as JSON
    }

    /**
     * Properly formats an informational message that will be returned to the client. Resets the informational message to the empty String.
     * @param returnData The object containing the data
     * @return results A JSON object
     */
    protected void sendInfoMessage(){
        def results = [:]
        results.put("infoMessage", infoMessage)
        infoMessage = []
        render results as JSON
    }

    /**
     * Adds a new message to the infoMessage
     * @param message The information that needs to be added to the infoMessage
     */
    protected void setInfoMessage(message){
        infoMessage.add(message)
        log.trace "setInfoMessage: "+infoMessage
    }

    /**
     * Adds a message to the infoMessage that gives the client information about offline modules
     */
    protected void setInfoMessageOfflineModules(){
        infoMessageOfflineModules.unique()
        if(infoMessageOfflineModules.size()>0){
            String message = "Unfortunately"
            infoMessageOfflineModules.eachWithIndex{ it, index ->
                if(index==(infoMessageOfflineModules.size()-2)){
                    message += ', the '+it+' and '
                } else {
                    if(index==(infoMessageOfflineModules.size()-1)){
                        message += ' the '+it
                    } else {
                        message += ', the '+it
                    }
                }
            }
            message += " could not be reached. As a result, we cannot at this time visualize data contained in "
            if(infoMessageOfflineModules.size()>1){
                message += "these modules."
            } else {
                message += "this module."
            }
            setInfoMessage(message)
        }
        infoMessageOfflineModules = []
    }

    /**
     * Combine several blocks of formatted data into one. These blocks have been formatted by the formatData function.
     * @param inputData Contains a list of maps, of the following format
     *          - a key 'series' containing a list, that contains one or more maps, which contain the following:
     *            - a key 'name', containing, for example, a feature name or field name
     *            - a key 'y', containing a list of y-values
     *            - a key 'error', containing a list of, for example, standard deviation or standard error of the mean values,
     */
    protected def formatCategoryData(inputData){
        // NOTE: This function is no longer up to date with the current inputData layout.
        def series = []
        inputData.eachWithIndex { it, i ->
            series << ['name': it['yaxis']['title'], 'y': it['series']['y'][0], 'error': it['series']['error'][0]]
        }
        def ret = [:]
        ret.put('type', inputData[0]['type'])
        ret.put('x', inputData[0]['x'])
        ret.put('yaxis',['title': 'title', 'unit': ''])
        ret.put('xaxis', inputData[0]['xaxis'])
        ret.put('series', series)
        return ret
    }

    /**
     * Given two objects of either CATEGORICALDATA or NUMERICALDATA
     * @param rowType The type of the data that has been selected for the row, either CATEGORICALDATA or NUMERICALDATA
     * @param columnType The type of the data that has been selected for the column, either CATEGORICALDATA or NUMERICALDATA
     * @return
     */
    protected def determineVisualizationTypes(rowType, columnType){
         def types = []
        if(rowType==CATEGORICALDATA || DATE || RELTIME){
            if(columnType==CATEGORICALDATA || DATE || RELTIME){
                types = [ [ "id": "table", "name": "Table"] ];
            }
            if(columnType==NUMERICALDATA){
                types = [ [ "id": "horizontal_barchart", "name": "Horizontal barchart"] ];
            }
        }
        if(rowType==NUMERICALDATA){
            if(columnType==CATEGORICALDATA || DATE || RELTIME){
                types = [ [ "id": "barchart", "name": "Barchart"], [ "id": "linechart", "name": "Linechart"] ];
            }
            if(columnType==NUMERICALDATA){
                types = [ [ "id": "scatterplot", "name": "Scatterplot"], [ "id": "linechart", "name": "Linechart"] ];
            }
        }
        return types
    }
}
