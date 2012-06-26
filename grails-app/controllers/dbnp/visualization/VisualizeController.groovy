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
	def dataService
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
            fields += dataService.getFieldsFromGSCF(study, "subjects", "domainfields")
            fields += dataService.getFieldsFromGSCF(study, "subjects", "templatefields")
            fields += dataService.getFieldsFromGSCF(study, "samplingEvents", "domainfields")
            fields += dataService.getFieldsFromGSCF(study, "samplingEvents", "templatefields")
            fields += dataService.getFieldsFromGSCF(study, "assays", "domainfields")
            fields += dataService.getFieldsFromGSCF(study, "assays", "templatefields")
            fields += dataService.getFieldsFromGSCF(study, "samples", "domainfields")
            fields += dataService.getFieldsFromGSCF(study, "samples", "templatefields")

            // Also make sure the user can select eventGroup to visualize
            fields += dataService.formatGSCFFields( "domainfields", [ name: "name" ], "GSCF", "eventGroups" );

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
            def offlineModules = []
            study.getAssays().each { assay ->
                def map = [:]
                if(!offlineModules.contains(assay.module.id)){
                    map = dataService.getFieldsFromModules(assay.module.toString(), assay)
                    // getFields returns a map with two items, one with key "fields" and the other with key "offlineModules"
                    if(map.offlineModules){
                        if(map.offlineModules.size()!=0){
							// An error occured while trying to collect field data from a module. Most likely, this module is offline. Ignore it for now
							map.offlineModules.each{ modVals ->
	                            offlineModules += modVals.id
	                            infoMessageOfflineModules += modVals.name	
							}
                        }
                    }
                    if(map.fields!=null){
                        if(map.fields.size()!=0){
                            fields += map.fields
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

		log.trace "Determining groupType: "+inputData.groupIds[0]
		def groupType = determineFieldType(inputData.studyIds[0], inputData.groupIds[0])
		
		        // Determine possible visualization- and aggregationtypes
        def visualizationTypes = determineVisualizationTypes(rowType, columnType)
		def aggregationTypes = determineAggregationTypes(rowType, columnType, groupType)
		
        log.trace  "visualization types: " + visualizationTypes + ", determined this based on "+rowType+" and "+columnType
		log.trace  "aggregation   types: " + aggregationTypes + ", determined this based on "+rowType+" and "+columnType + " and " + groupType
		
		def fieldData = [ 'x': dataService.parseFieldId( inputData.columnIds[ 0 ] ), 'y': dataService.parseFieldId( inputData.rowIds[ 0 ] ) ];
		
        return sendResults([
			'types': visualizationTypes,
			'aggregations': aggregationTypes,
			
			// TODO: Remove these ids when the view has been updated. Use xaxis.id and yaxis.id instead
			'rowIds':inputData.rowIds[0],
			'columnIds':inputData.columnIds[0],
			
			'xaxis': [ 
				'id': fieldData.x.fieldId,
				'name': fieldData.x.name,
				'unit': fieldData.x.unit,
				'type': dataTypeString( columnType )
			],
			'yaxis': [
				'id': fieldData.y.fieldId,
				'name': fieldData.y.name,
				'unit': fieldData.y.unit,
				'type': dataTypeString( rowType )
			],

		])
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

        // If the user is requesting data that concerns only subjects, then make sure those subjects appear only once
        if(dataService.parseFieldId( inputData.columnIds[ 0 ] ).type=='subjects' && dataService.parseFieldId( inputData.rowIds[ 0 ] ).type=='subjects'){
            samples.unique { it.parentSubject }
        }
        
		// Retrieve the data for both axes for all samples
		// TODO: handle the case of multiple fields on an axis
		def fields = [ "x": inputData.columnIds[ 0 ], "y": inputData.rowIds[ 0 ], "group": inputData.groupIds[ 0 ] ];
		def fieldInfo = [:]
		fields.each { 
			fieldInfo[ it.key ] = dataService.parseFieldId( it.value ) 
			if( fieldInfo[ it.key ] )
				fieldInfo[ it.key ].fieldType = determineFieldType( study.id, it.value );
				
		}
		
		// If the groupAxis is numerical, we should ignore it, unless a table is asked for
		if( fieldInfo.group && fieldInfo.group.fieldType == NUMERICALDATA && inputData.visualizationType != "table" ) {
			fields.group = null;
			fieldInfo.group = null;
		}

        if( fieldInfo.x.fieldType == NUMERICALDATA && fieldInfo.y.fieldType == NUMERICALDATA) {
            if(inputData.visualizationType == "horizontal_barchart") {
                fieldInfo.y.fieldType = CATEGORICALDATA;
            } else if(inputData.visualizationType == "barchart") {
                fieldInfo.x.fieldType = CATEGORICALDATA;
            }
        }

		println "Fields: "
		fieldInfo.each { println it }

		// Fetch all data from the system. data will be in the format:
		// 		[ "x": [ 3, 6, null, 10 ], "y": [ "male", "male", "female", "female" ], "group": [ "US", "NL", "NL", "NL" ]
		//	If a field is not given, the data will be NULL
		def data = getAllFieldData( study, samples, fields );

		println "All Data: "
		data.each { println it }

        // Aggregate the data based on the requested aggregation
		def aggregatedData = aggregateData( data, fieldInfo, inputData.aggregation );
		
		println "Aggregated Data: "
		aggregatedData.each { println it }
		
		// No convert the aggregated data into a format we can use
		def returnData = formatData( inputData.visualizationType, aggregatedData, fieldInfo );

		println "Returndata: " 
		returnData.each { println it }
		
        // Make sure no changes are written to the database
        study.discard()
        samples*.discard()
        
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
		def studyIds = params.list( 'study' );
		def rowIds = params.list( 'rows' );
		def columnIds = params.list( 'columns' );
		def groupIds = params.list( 'groups' ); 
		def visualizationType = params.get( 'types');
		def aggregation = params.get( 'aggregation' );

		return [ "studyIds" : studyIds, "rowIds": rowIds, "columnIds": columnIds, "groupIds": groupIds, "visualizationType": visualizationType, "aggregation": aggregation ];
	}

	
	/**
	 * Aggregates the data based on the requested aggregation on the categorical fields
	 * @param data			Map with data for each dimension as retrieved using getAllFieldData. For example:
	 * 							[ "x": [ 3, 6, 8, 10 ], "y": [ "male", "male", "female", "female" ], "group": [ "US", "NL", "NL", "NL" ] ]
	 * @param fieldInfo		Map with field information for each dimension. For example:
	 * 							[ "x": [ id: "abc", "type": NUMERICALDATA ], "y": [ "id": "def", "type": CATEGORICALDATA ] ]
	 * @param aggregation	Kind of aggregation requested
	 * @return				Data that is aggregated on the categorical fields
	 * 							[ "x": [ 3, 6, null, 9 ], "y": [ "male", "male", "female", "female" ], "group": [ "US", "NL", "US", "NL" ] ]
	 * 
	 */
	def aggregateData( data, fieldInfo, aggregation ) {
		// Filter out null values
		data = filterNullValues( data )
		
		// If no aggregation is requested, we just return the original object (but filtering out null values)
		if( aggregation == "none" ) {
			return sortNonAggregatedData( data, [ 'x', 'group' ] );
		}
		
		// Determine the categorical fields
		def dimensions = [ "categorical": [], "numerical": [] ];
		fieldInfo.each { 
			// If fieldInfo value is NULL, the field is not requested
			if( it && it.value ) {
				if( [ CATEGORICALDATA, RELTIME, DATE ].contains( it.value.fieldType ) ) {
					dimensions.categorical << it.key
				} else {
					dimensions.numerical << it.key
				}
			}
		}

		// Compose a map with aggregated data
		def aggregatedData = [:];
		fieldInfo.each { aggregatedData[ it.key ] = [] }
		
		// Loop through all categorical fields and aggregate the values for each combination
		if( dimensions.categorical.size() > 0 ) {
			return aggregate( data, dimensions.categorical, dimensions.numerical, aggregation, fieldInfo );
		} else {
			// No categorical dimensions. Just compute the aggregation for all values together
			def returnData = [ "count": [ data.numValues ] ];
		 
			// Now compute the correct aggregation for each numerical dimension.
			dimensions.numerical.each { numericalDimension ->
				def currentData = data[ numericalDimension ];
				returnData[ numericalDimension ] = [ computeAggregation( aggregation, currentData ).value ];
			}
			
			return returnData;
		}
	}
	
	/**
	 * Sort the data that has not been aggregated by the given columns
	 * @param data
	 * @param sortBy
	 * @return
	 */
	protected def sortNonAggregatedData( data, sortBy ) {
		if( !sortBy )
			return data;
			
		// First combine the lists within the data
		def combined = [];
		for( int i = 0; i < data.numValues; i++ ) {
			def element = [:]
			data.each {
				if( it.value instanceof Collection && i < it.value.size() ) {
					element[ it.key ] = it.value[ i ];
				}
			}
			
			combined << element;
		}
		
		// Now sort the combined element with a comparator
		def comparator = { a, b ->
			for( column in sortBy ) {
				if( a[ column ] != null ) {
					if( a[ column ] != b[ column ] ) {
						if( a[ column ] instanceof Number )
							return a[ column ] <=> b[ column ];
						else
							return a[ column ].toString() <=> b[ column ].toString();
					}
				}
			}
		
			return 0;
		}
		
		combined.sort( comparator as Comparator );
		
		// Put the elements back again. First empty the original lists
		data.each {
			if( it.value instanceof Collection ) {
				it.value = [];
			}
		}
		
		combined.each { element ->
			element.each { key, value ->
				data[ key ] << value;
			} 
		}

		return data;
	}
	
	/**
	 * Filter null values from the different lists of data. The associated values in other lists will also be removed
	 * @param data	
	 * @return	Filtered data.
	 * 
	 * [ 'x': [0, 2, 4], 'y': [1,null,2] ] will result in [ 'x': [0, 4], 'y': [1, 2] ] 
	 * [ 'x': [0, 2, 4], 'y': [1,null,2], 'z': [4, null, null] ] will result in [ 'x': [0], 'y': [1], 'z': [4] ] 
	 */
	protected def filterNullValues( data ) {
		def filteredData = [:];
		
		// Create a copy of the data object
		data.each {
			filteredData[ it.key ] = it.value;
		}
		
		// Loop through all values and return all null-values (and the same indices on other elements
		int num = filteredData.numValues;
		filteredData.keySet().each { fieldName ->
			// If values are found, do filtering. If not, skip this one
			if( filteredData[ fieldName ] != null && filteredData[ fieldName ] instanceof Collection ) {
				// Find all non-null values in this list
				def indices = filteredData[ fieldName ].findIndexValues { it != null };
				
				// Remove the non-null values from each data object
				filteredData.each { key, value ->
					if( value && value instanceof Collection )
						filteredData[ key ] = value[ indices ];
				}
				
				// Store the number of values
				num = indices.size();
			}
		}
		
		filteredData.numValues = num;
		 
		return filteredData
	}
	
	/**
	 * Aggregates the given data on the categorical dimensions.
	 * @param data					Initial data
	 * @param categoricalDimensions	List of categorical dimensions to group  by
	 * @param numericalDimensions	List of all numerical dimensions to compute the aggregation for
	 * @param aggregation			Type of aggregation requested
	 * @param fieldInfo				Information about the fields requested by the user	(e.g. [ "x": [ "id": 1, "fieldType": CATEGORICALDATA ] ] )
	 * @param criteria				The criteria the current aggregation must keep (e.g. "x": "male")
	 * @param returnData			Initial return object with the same keys as the data object, plus 'count' 
	 * @return
	 */
	protected def aggregate( Map data, Collection categoricalDimensions, Collection numericalDimensions, String aggregation, fieldInfo, criteria = [:], returnData = null ) {
		if( !categoricalDimensions )
			return data;
			
		// If no returndata is given, initialize the map
		if( returnData == null ) {
			returnData = [ "count": [] ]
			data.each { returnData[ it.key ] = [] }
		}
		
		def dimension = categoricalDimensions.head();
		
		// Determine the unique values on the categorical axis and sort by toString method
		def unique = data[ dimension ].flatten()
					.unique { it == null ? "null" : it.class.name + it.toString() }
					.sort {
						// Sort categoricaldata on its string value, but others (numerical, reltime, date) 
						// on its real value
						switch( fieldInfo[ dimension ].fieldType ) {
							case CATEGORICALDATA:
								return it.toString()
							default:
								return it
						} 
					};
					
		// Make sure the null category is last
		unique = unique.findAll { it != null } + unique.findAll { it == null }
		
		unique.each { el ->
			// Use this element to search on
			criteria[ dimension ] = el;
			
			// If the list of categoricalDimensions is empty after this dimension, do the real work
			if( categoricalDimensions.size() == 1 ) {
				// Search for all elements in the numericaldimensions that belong to the current group
				// The current group is defined by the criteria object
				
				// We start with all indices belonging to this group
				def indices = 0..data.numValues;
				criteria.each { criterion ->
					// Find the indices of the samples that belong to this group. if a sample belongs to multiple groups (i.e. if
					// the samples groupAxis contains multiple values, is a collection), the value should be used in all groups.
					def currentIndices = data[ criterion.key ].findIndexValues { it instanceof Collection ? it.contains( criterion.value ) : it == criterion.value };
					indices = indices.intersect( currentIndices );
					
					// Store the value for the criterion in the returnData object
					returnData[ criterion.key ] << criterion.value;
				}
				
				// If no numericalDimension is asked for, no aggregation is possible. For that reason, we 
				// also return counts
				returnData[ "count" ] << indices.size();
				 
				// Now compute the correct aggregation for each numerical dimension.
				numericalDimensions.each { numericalDimension ->
					def currentData = data[ numericalDimension ][ indices ]; 
					returnData[ numericalDimension ] << computeAggregation( aggregation, currentData ).value;
				}
				
			} else {
				returnData = aggregate( data, categoricalDimensions.tail(), numericalDimensions, aggregation, fieldInfo, criteria, returnData );
			}
		}
		
		return returnData;
	}
	
	/**
	 * Compute the aggregation for a list of values
	 * @param aggregation
	 * @param currentData
	 * @return
	 */
	def computeAggregation( String aggregation, List currentData ) {
		switch( aggregation ) {
			case "count":
				return dataService.computeCount( currentData );
				break;
			case "median":
				return dataService.computeMedian( currentData );
				break;
			case "sum":
				return dataService.computeSum( currentData );
				break;
			case "average":
			default:
				// Default is "average"
				return dataService.computeMeanAndError( currentData );
				break;
		}
	}

	/**
	 * Formats the grouped data in such a way that the clientside visualization method 
	 * can handle the data correctly.
	 * @param groupedData	Data that has been grouped using the groupFields method
	 * @param fieldData		Map with key-value pairs determining the name and fieldId to retrieve data for. Example:
	 * 							[ "x": { "id": ... }, "y": { "id": "field-id-3" }, "group": { "id": "field-id-6" } ]
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
	def formatData( type, groupedData, fieldInfo, xAxis = "x", yAxis = "y", serieAxis = "group", errorName = "error" ) {
		// Format categorical axes by setting the names correct
		fieldInfo.each { field, info ->
			if( field && info ) {
				groupedData[ field ] = renderFieldsHumanReadable( groupedData[ field ], info.fieldType)
			}
		}
		
		// TODO: Handle name and unit of fields correctly
		def xAxisTypeString = dataTypeString( fieldInfo[ xAxis ]?.fieldType )
		def yAxisTypeString = dataTypeString( fieldInfo[ yAxis ]?.fieldType )
		def serieAxisTypeString = dataTypeString( fieldInfo[ serieAxis ]?.fieldType )
		
		// Create a return object
		def return_data = [:]
		return_data[ "type" ] = type
		return_data.put("xaxis", ["title" : fieldInfo[ xAxis ]?.name, "unit": fieldInfo[ xAxis ]?.unit, "type": xAxisTypeString ])
		return_data.put("yaxis", ["title" : fieldInfo[ yAxis ]?.name, "unit" : fieldInfo[ yAxis ]?.unit, "type": yAxisTypeString ])
		return_data.put("groupaxis", ["title" : fieldInfo[ serieAxis ]?.name, "unit" : fieldInfo[ serieAxis ]?.unit, "type": serieAxisTypeString ])
		
		if(type=="table"){
			// Determine the lists on both axes. The strange addition is done because the unique() method
			// alters the object itself, instead of only returning a unique list
			def xAxisData = ([] + groupedData[ xAxis ]).unique()
			def yAxisData = ([] + groupedData[ yAxis ]).unique()

			if( !fieldInfo[ serieAxis ] ) {
				// If no value has been chosen on the serieAxis, we should show the counts for only one serie
				def tableData = formatTableData( groupedData, xAxisData, yAxisData, xAxis, yAxis, "count" );
				
				return_data.put("series", [[
					"name": "count",
					"x": xAxisData,
					"y": yAxisData,
					"data": tableData
				]])
			} else if( fieldInfo[ serieAxis ].fieldType == NUMERICALDATA ) {
				// If no value has been chosen on the serieAxis, we should show the counts for only one serie
				def tableData = formatTableData( groupedData, xAxisData, yAxisData, xAxis, yAxis, serieAxis );

				// If a numerical field has been chosen on the serieAxis, we should show the requested aggregation 
				// for only one serie
				return_data.put("series", [[
					"name": fieldInfo[ xAxis ].name,
					"x": xAxisData,
					"y": yAxisData,
					"data": tableData
				]])
			} else {
				// If a categorical field has been chosen on the serieAxis, we should create a table for each serie
				// with counts as data. That table should include all data for that serie
				return_data[ "series" ] = [];
				
				// The strange addition is done because the unique() method
				// alters the object itself, instead of only returning a unique list
				def uniqueSeries = ([] + groupedData[ serieAxis ]).unique();
				
				uniqueSeries.each { serie -> 
					def indices = groupedData[ serieAxis ].findIndexValues { it == serie }
					
					// If no value has been chosen on the serieAxis, we should show the counts for only one serie
					def tableData = formatTableData( groupedData, xAxisData, yAxisData, xAxis, yAxis, "count", indices );
	
					return_data[ "series" ] << [
						"name": serie,
						"x": xAxisData,
						"y": yAxisData,
						"data": tableData,
					]
				}
			}
			
		} else if(type=="boxplot") {
            return_data[ "series" ] = [];
            HashMap dataMap = new HashMap();
			
			List xValues = [];
            groupedData[ xAxis ].eachWithIndex { category, i ->
                if(!dataMap.containsKey(category)) {
                    dataMap.put(category, []);
					xValues << category;
                }
                dataMap.put(category, dataMap.get(category)+groupedData[ yAxis ][i]);
				
            }

            for ( String key : xValues ) {
                def objInfos = computePercentile(dataMap.get(key),50);
                double dblMEDIAN = objInfos.get("value");
                double Q1 = computePercentile(dataMap.get(key),25).get("value");
                double Q3 = computePercentile(dataMap.get(key),75).get("value");

                // Calcultate 1.5* inter-quartile-distance
                double dblIQD = (Q3-Q1)*1.5;

                /* // DEBUG
                println("---");
                println("  dataMap["+key+"]:: "+dataMap.get(key));
                println("  dblMEDIAN:: "+dblMEDIAN);
                println("  dblIQD:: "+dblIQD);
                println("  Q1:: "+Q1);
                println("  Q3:: "+Q3);
                println("---");
                */

                return_data[ "series" ] << [
                        "name": key,
                        "y" : [key, objInfos.get("max"), (dblMEDIAN+dblIQD), Q3, dblMEDIAN, Q1, (dblMEDIAN-dblIQD), objInfos.get("min")]
                ];
            }

            //println(return_data);


        } else {
			// For a horizontal barchart, the two axes should be swapped
			if( type == "horizontal_barchart" ) {
				def tmp = xAxis
				xAxis = yAxis
				yAxis = tmp
			}
		
			if( !fieldInfo[ serieAxis ] ) {
				// If no series field has defined, we return all data in one serie
				return_data.put("series", [[
					"name": "count",
					"x": groupedData[ xAxis ],
					"y": groupedData[ yAxis ],
				]])
			} else if( fieldInfo[ serieAxis ].fieldType == NUMERICALDATA ) {
				// No numerical series field is allowed in a chart. 
				throw new Exception( "No numerical series field is allowed here." );
			} else {
				// If a categorical field has been chosen on the serieAxis, we should create a group for each serie
				// with the correct values, belonging to that serie.
				return_data[ "series" ] = [];
				
				// The unique method alters the original object, so we 
				// create a new object
				def uniqueSeries = ([] + groupedData[ serieAxis ]).unique();
				
				uniqueSeries.each { serie ->
					def indices = groupedData[ serieAxis ].findIndexValues { it == serie }
					return_data[ "series" ] << [
						"name": serie,
						"x": groupedData[ xAxis ][ indices ],
						"y": groupedData[ yAxis ][ indices ]
					]
				}
			}
		}
		
		return return_data;
	}

	/**
	 * Formats the requested data for a table	
	 * @param groupedData
	 * @param xAxisData
	 * @param yAxisData
	 * @param xAxis
	 * @param yAxis
	 * @param dataAxis
	 * @return
	 */
	def formatTableData( groupedData, xAxisData, yAxisData, xAxis, yAxis, dataAxis, serieIndices = null ) {
		def tableData = []
		
		xAxisData.each { x ->
			def colData = []
			
			def indices = groupedData[ xAxis ].findIndexValues { it == x }
			
			// If serieIndices are given, intersect the indices
			if( serieIndices != null )
				indices = indices.intersect( serieIndices );
			
			yAxisData.each { y ->
				def index = indices.intersect( groupedData[ yAxis ].findIndexValues { it == y } );
				
				if( index.size() ) {
					colData << groupedData[ dataAxis ][ (int) index[ 0 ] ]
				}
			}
			tableData << colData;
		}
		
		return tableData;
	}

    /**
     * If the input variable 'data' contains dates or times according to input variable 'fieldInfo', these dates and times are converted to a human-readable version.
     * @param data  The list of items that needs to be checked/converted
     * @param axisType As determined by determineFieldType
     * @return The input variable 'data', with it's date and time elements converted.
     * @see determineFieldType
     */
    def renderFieldsHumanReadable(data, axisType){
        switch( axisType ) {
			case RELTIME:
				return renderTimesHumanReadable(data)
			case DATE:
				return renderDatesHumanReadable(data)
			case CATEGORICALDATA:
				return data.collect { it.toString() }
			case NUMERICALDATA:
			default:
				return data;
		}
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
	 * Returns a string representation of the given fieldType, which can be sent to the userinterface
	 * @param fieldType	CATEGORICALDATA, DATE, RELTIME, NUMERICALDATA
	 * @return	String representation
	 */
	protected String dataTypeString( fieldType ) {
		return (fieldType==CATEGORICALDATA || fieldType==DATE || fieldType==RELTIME ? "categorical" : "numerical")
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
		def parsedField = dataService.parseFieldId( fieldId );
        def study = Study.get(studyId)
		def data = []

		// If the fieldId is incorrect, or the field is not asked for, return
		// CATEGORICALDATA
		if( !parsedField ) {
			return CATEGORICALDATA;
        }
        try{
            if( parsedField.source == "GSCF" ) {
                if(parsedField.id.isNumber()){
                    return determineCategoryFromTemplateFieldId(parsedField.id)
                } else { // Domainfield or memberclass
                    def callback = dataService.domainObjectCallback( parsedField.type )
                    // Can the field be found in the domainFields as well? If so, treat it as a template field, so that dates and times can be properly rendered in a human-readable fashion

                    if(callback.metaClass.methods*.name.contains( "giveDomainFields" ) && callback?.giveDomainFields()?.name?.contains(parsedField.name.toString())){
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
        log.trace "Determine category from class: " + classObject
        switch( classObject ) {
			case java.lang.String:
			case org.dbnp.gdt.Term:
			case org.dbnp.gdt.TemplateFieldListItem:
				return CATEGORICALDATA;
			default:
				return NUMERICALDATA;
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
		
        if(rowType == CATEGORICALDATA || rowType == DATE || rowType == RELTIME){
            if(columnType == CATEGORICALDATA || columnType == DATE || columnType == RELTIME){
				types = [ [ "id": "table", "name": "Table"] ];
            } else {	// NUMERICALDATA
                types = [ [ "id": "horizontal_barchart", "name": "Horizontal barchart"] ];
            }
        } else {	// NUMERICALDATA
            if(columnType == CATEGORICALDATA || columnType == DATE || columnType == RELTIME){
                types = [ [ "id": "barchart", "name": "Barchart"], [ "id": "linechart", "name": "Linechart"], [ "id": "boxplot", "name": "Boxplot"] ];
            } else {
                types = [ [ "id": "scatterplot", "name": "Scatterplot"], [ "id": "linechart", "name": "Linechart"],  [ "id": "barchart", "name": "Barchart"], [ "id": "horizontal_barchart", "name": "Horizontal barchart"] ];
            }
        }
        return types
    }
	
	/**
	* Returns the types of aggregation possible for the given two objects of either CATEGORICALDATA or NUMERICALDATA
	* @param rowType The type of the data that has been selected for the row, either CATEGORICALDATA or NUMERICALDATA
	* @param columnType The type of the data that has been selected for the column, either CATEGORICALDATA or NUMERICALDATA
	* @param groupType The type of the data that has been selected for the grouping, either CATEGORICALDATA or NUMERICALDATA
	* @return
	*/
	protected def determineAggregationTypes(rowType, columnType, groupType = null ){
		// A list of all aggregation types. By default, every item is possible
		def types = [
			[ "id": "average", "name": "Average", "disabled": false ],
			[ "id": "count", "name": "Count", "disabled": false ],
			[ "id": "median", "name": "Median", "disabled": false ],
			[ "id": "none", "name": "No aggregation", "disabled": false ],
			[ "id": "sum", "name": "Sum", "disabled": false ],
		]

		// Normally, all aggregation types are possible, with three exceptions:
		// 		Categorical data on both axes. In that case, we don't have anything to aggregate, so we can only count
		//		Grouping on a numerical field is not possible. In that case, it is ignored
		//			Grouping on a numerical field with categorical data on both axes (table) enabled aggregation,
		//			In that case we can aggregate on the numerical field. 
		
		if(rowType == CATEGORICALDATA || rowType == DATE || rowType == RELTIME){
			if(columnType == CATEGORICALDATA || columnType == DATE || columnType == RELTIME){
				
				if( groupType == NUMERICALDATA ) {
					// Disable 'none', since that can not be visualized
					types.each {
						if( it.id == "none" )
							it.disabled = true
					}
				} else {
					// Disable everything but 'count'
					types.each { 
						if( it.id != "count" )  
							it.disabled = true
					}
				}
			}
		}
		
		return types
   }
}
