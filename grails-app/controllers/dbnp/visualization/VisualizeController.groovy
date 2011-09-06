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
    def infoMessage = ""
    final int categoricalData = 0
    final int numericalData = 1

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
            infoMessage = "Please select a study."
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
            fields += getFields(study, "samples", "domainfields")

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
                list = getFields(assay.module.id, assay)
                if(list!=null){
                    if(list.size()!=0){
                        fields += list

                    }
                }
            }

            // TODO: Maybe we should add study's own fields
        } else {
            log.error("VisualizationController: getFields: The requested study could not be found. Id: "+studies)
            return returnError(404, "The requested study could not be found.")
        }

		return sendResults(fields)
	}

	def getVisualizationTypes = {
        def inputData = parseGetDataParams();
        println "inputData: "
        inputData.each{
            println "\t"+it
        }

        if(inputData.columnIds == null || inputData.columnIds == [] || inputData.columnIds[0] == null || inputData.columnIds[0] == ""){
            infoMessage = "Please select columndata."
            return sendInfoMessage()
        }
        if(inputData.rowIds == null || inputData.rowIds == [] ||  inputData.rowIds[0] == null ||   inputData.rowIds[0] == ""){
            infoMessage = "Please select rowdata."
            return sendInfoMessage()
        }


        // TODO: handle the case of multiple fields on an axis
        // Determine data types
        println "Checking type of row data ("+inputData.rowIds[0]+")"
        def rowType = determineFieldType(inputData.studyIds[0], inputData.rowIds[0])
        println "Checking type of column data ("+inputData.columnIds[0]+")"
        def columnType = determineFieldType(inputData.studyIds[0], inputData.columnIds[0])

        println "getVisualizationTypes: row contains data of type "+rowType+" and column contains data of type "+columnType


        // Determine possible visualization types
        // TODO: Determine possible visualization types

		def types = [ [ "id": "barchart", "name": "Barchart"] ];
		return sendResults(types)
	}

    def getFields(source, assay){
        /*
        Gather fields related to this study from modules.
        This will use the getMeasurements RESTful service. That service returns measurement types, AKA features.
        getMeasurements does not actually return measurements (the getMeasurementData call does).
        The getFields method (or rather, the getMeasurements service) requires one or more assays and will return all measurement
        types related to these assays.
        So, the required variables for such a call are:
          - a source variable, which can be obtained from AssayModule.list() (use the 'name' field)
          - a list of assays, which can be obtained with study.getAssays()

        Output is a list of items. Each item contains
          - an 'id'
          - a 'source', which is a module identifier
          - a 'category', which indicates where the field can be found. When dealing with module data as we are here, this is the assay name(s)
          - a 'name', which is the the name of the field
         */
        def fields = []
        def callUrl = ""

        // Making a different call for each assay
        // TODO: Change this to one call that requests fields for all assays, when you get that to work (in all cases)

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
            infoMessage = "An error occured while trying to collect field data from a module. Most likely, this module is offline."
            log.error("VisualizationController: getFields: "+e)
        }

        return fields
    }

   def getFields(study, category, type){
        /*
        Gather fields related to this study from GSCF.
        This requires:
          - a study.
          - a category variable, e.g. "events".
          - a type variable, either "domainfields" or "templatefields".

        Output is a list of items, which are formatted by the formatGSCFFields function.
        */

        // Collecting the data from it's source
        def collection = []
        def fields = []
        def source = "GSCF"

        // Gathering the data
        if(category=="subjects"){
            if(type=="domainfields"){
                collection = Subject.giveDomainFields()
            }
            if(type=="templatefields"){
                collection = study?.samples?.parentSubject?.template?.fields
            }
        }
        if(category=="events"){
            if(type=="domainfields"){
                collection = Event.giveDomainFields()
            }
            if(type=="templatefields"){
                collection = study?.samples?.parentEventGroup?.events?.template?.fields
            }
        }
        if(category=="samplingEvents"){
            if(type=="domainfields"){
                collection = SamplingEvent.giveDomainFields()
            }
            if(type=="templatefields"){
                collection = study?.samples?.parentEventGroup?.samplingEvents?.template?.fields
            }
        }
        if(category=="samples"){
            if(type=="domainfields"){
                collection = Sample.giveDomainFields()
            }
            if(type=="templatefields"){
                collection = study?.samples?.template?.fields
            }
        }
        if(category=="assays"){
            if(type=="domainfields"){
                collection = Assay.giveDomainFields()
            }
            if(type=="templatefields"){
                collection = study?.assays?.template?.fields
            }
        }

        collection.unique()

        // Formatting the data
        fields += formatGSCFFields(type, collection, source, category)

        return fields
    }

    def formatGSCFFields(type, inputObject, source, category){
        /*  The formatGSCFFields function can receive both lists of fields and single fields. If it receives a list, it calls itself again for each item in the list. This way, the original formatGSCFFields call will return single fields regardless of it's input.

        Output is a list of items. Each item contains
          - an 'id'
          - a 'source', which in this case will be "GSCF"
          - a 'category', which indicates where the field can be found, e.g. "subjects", "samplingEvents"
          - a 'name', which is the the name of the field
         */
        if(inputObject==null || inputObject == []){
            return []
        }
        def fields = []
        if(inputObject instanceof Collection){
            // Apparently this field is actually a list of fields.
            // We will call ourselves again with the list's elements as input.
            // These list elements will themselves go through this check again, effectively flattening the original input
            for(int i = 0; i < inputObject.size(); i++){
                fields += formatGSCFFields(type, inputObject[i], source, category)
            }
            return fields
        } else {
            // This is a single field. Format it and return the result.
            if(type=="domainfields"){
                fields << [ "id": createFieldId( id: inputObject.name, name: inputObject.name, source: source, type: category ), "source": source, "category": category, "name": inputObject.name ]
            }
            if(type=="templatefields"){
                fields << [ "id": createFieldId( id: inputObject.id, name: inputObject.name, source: source, type: category ), "source": source, "category": category, "name": inputObject.name ]
            }
            return fields
        }
    }

	/**
	 * Retrieves data for the visualization itself.
	 */
	def getData = {
		// Extract parameters
		// TODO: handle erroneous input data
		def inputData = parseGetDataParams();

        println "getData's inputData: "+inputData
        if(inputData.columnIds == null || inputData.rowIds == null){
            infoMessage = "Please select row and columndata."
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
		// TODO: handle categories and continuous data
		def groupedData = groupFieldData( data );

		// Format data so it can be rendered as JSON
		def returnData = formatData( groupedData, fields );

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
		
		def inputData = params.get( 'data' );
		try{
			def input_object = JSON.parse(inputData)
			
			studyIds = input_object.get('studies')*.id
			rowIds = input_object.get('rows')*.id
			columnIds = input_object.get('columns')*.id
			visualizationType = "barchart"
		} catch(Exception e) {
            /* TODO: Find a way to handle exceptions without breaking the user interface.
                Doing things in this way results in the user interface getting a 400.
			returnError(400, "An error occured while retrieving the user input")
            infoMessage = "An error occured while retrieving the user input."
			log.error("VisualizationController: parseGetDataParams: "+e)
            return sendInfoMessage()
            */
		}

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
					
					if( value ) {
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
	def getModuleData( study, samples, source_module, fieldName ) {
		def data = []
		
		// TODO: Handle values that should be retrieved from multiple assays
		def assay = Assay.get(source_module);
		
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
                send returnError(404, "An error occured while trying to collect data from a module. Most likely, this module is offline.")
			}
		} else {
			// TODO: Handle error correctly
			// Returns an empty list with as many elements as there are samples
			data = samples.collect { return null }
		}
		
		return data

	}

	/**
	 * Group the field data on the values of the specified axis. For example, for a bar chart, the values 
	 * on the x-axis should be grouped. Currently, the values for each group are averaged, and the standard
	 * error of the mean is returned in the 'error' property 
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
	def groupFieldData( data, groupAxis = "x", valueAxis = "y", errorName = "error", unknownName = "unknown" ) {
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
		
		// Gather names for the groups. Most of the times, the group names are just the names, only with
		// a null value, the unknownName must be used
		def groupNames = groups.collect { it != null ? it : unknownName }
		
		// Generate the output object
		def outputData = [:]
		outputData[ valueAxis ] = [];
		outputData[ errorName ] = [];
		outputData[ groupAxis ] = groupNames;
		
		// Loop through all groups, and gather the values for this group
		groups.each { group ->
			// Find the indices of the samples that belong to this group. if a sample belongs to multiple groups (i.e. if
			// the samples groupAxis contains multiple values, is a collection), the value should be used in all groups.
			def indices= data[ groupAxis ].findIndexValues { it instanceof Collection ? it.contains( group ) : it == group };
			def values = data[ valueAxis ][ indices ]
			
			def dataForGroup = computeMeanAndError( values );
			
			outputData[ valueAxis ] << dataForGroup.value
			outputData[ errorName ] << dataForGroup.error 
		}

		return outputData
	}
	
	/**
	 * Formats the grouped data in such a way that the clientside visualization method 
	 * can handle the data correctly.
	 * @param groupedData	Data that has been grouped using the groupFields method
	 * @param fields		Map with key-value pairs determining the name and fieldId to retrieve data for. Example:
	 * 							[ "x": "field-id-1", "y": "field-id-3" ]
	 * @param groupAxis		Name of the axis to with group data. Defaults to "x"
	 * @param valueAxis		Name of the axis where the values are stored. Defaults to "y"
	 * @param errorName		Key in the output map where 'error' values (SEM) are stored. Defaults to "error" 	 * 
	 * @return				A map like the following:
	 * 
	  		{
				"type": "barchart",
				"x": [ "Q1", "Q2", "Q3", "Q4" ],
				"xaxis": { "title": "quarter 2011", "unit": "" },
				"yaxis": { "title": "temperature", "unit": "degrees C" },
				"series": [
					{
						"name": "series name",
						"y": [ 5.1, 3.1, 20.6, 15.4 ],
						"error": [ 0.5, 0.2, 0.4, 0.5 ]
					},
				]
			}
	 * 
	 */
	def formatData( groupedData, fields, groupAxis = "x", valueAxis = "y", errorName = "error" ) {
		// TODO: Handle name and unit of fields correctly
		
		def return_data = [:]
		return_data[ "type" ] = "barchart"
		return_data[ "x" ] = groupedData[ groupAxis ].collect { it.toString() }
		return_data.put("yaxis", ["title" : parseFieldId( fields[ valueAxis ] ).name, "unit" : "" ])
		return_data.put("xaxis", ["title" : parseFieldId( fields[ groupAxis ] ).name, "unit": "" ])
		return_data.put("series", [[
			"name": "Y",
			"y": groupedData[ valueAxis ],
			"error": groupedData[ errorName ]
		]])
		
		return return_data;
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
	 * are ignored. If no values are given, the mean of 0 is returned.
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
			return 0; 
	}

	/**
	* Computes the standard error of mean of the given values. 
	* Values that can not be parsed to a number are ignored. 
	* If no values are given, the standard deviation of 0 is returned.
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
		   return 0;
	   }
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

    protected void returnError(code, msg){
        response.sendError(code , msg)
    }

    protected def determineFieldType(studyId, fieldId){
        // Parse the fieldId as given by the user
		def parsedField = parseFieldId( fieldId );

        def study = Study.get(studyId)
        println "study: "+study+", parsedField: "+parsedField

		def data = []

		if( parsedField.source == "GSCF" ) {
            if(parsedField.id.isNumber()){
                // Templatefield
                // ask for tf by id, ask for .type
                println "GSCF TF, dunno yet. input: "+studyId+", "+parsedField
                try{
                    TemplateField tf = TemplateField.get(parsedField.id)
                    println "tf.type: "+tf.type
                    if(tf.type=="DOUBLE" || tf.type=="LONG" || tf.type=="DATE" || tf.type=="RELTIME"){
                        return numericalData
                    } else {
                        return categoricalData
                    }
                } catch(Exception e){
                    log.error("VisualizationController: determineFieldType: "+e)
                }
            } else {
                // Domainfield or memberclass
                try{
                    switch( parsedField.type ) {
                        case "Study":
                        case "studies":
                            return determineCategoryFromClass(Study[parsedField.name].class)
                            break
                        case "Subject":
                        case "subjects":
                            return determineCategoryFromClass(Subject[parsedField.name].class)
                            break
                        case "Sample":
                        case "samples":
                            return determineCategoryFromClass(Sample[parsedField.name].class)
                            break
                        case "Event":
                        case "events":
                            return determineCategoryFromClass(Event.getField(parsedField.name).class)
                            break
                        case "SamplingEvent":
                        case "samplingEvents":
                            return determineCategoryFromClass(SamplingEvent[parsedField.name].class)
                            break
                        case "Assay":
                        case "assays":
                            return determineCategoryFromClass(Assay[parsedField.name].class)
                            break
                    }
                } catch(Exception e){
                    return categoricalData
                }
            }

            // Check parsedField.id == number
		} else {
            data = getModuleData( study, study.getSamples(), parsedField.source, parsedField.name );
            def cat = determineCategoryFromData(data)
            return cat
		}
    }

    protected String determineCategoryFromClass(inputObject){
        println "determineCategoryFromClass: "+inputObject+", class: "+inputObject.class
        if(inputObject==java.lang.String){
            return categoricalData
        } else {
            return numericalData
        }
    }

    protected String determineCategoryFromData(inputObject){
        def results = []
        if(inputObject instanceof Collection){
            // More complex datatype, call outselves again
            inputObject.each {
                results << determineCategoryFromData(it)
            }
        } else {
            if(inputObject.toString().isDouble()){
                results << numericalData
            } else {
                results << categoricalData
            }
        }

        results.unique()

        if(results.size()>1){
            //log.error("VisualizeController: determineCategoryFromData: Category list contains more than one category! List: "+results+", inputObject: "+inputObject)
            results[0] = categoricalData
        }

        return results[0]
    }

    protected void sendResults(returnData){
        def results = [:]
        if(infoMessage!=""){
            results.put("infoMessage", infoMessage)
            infoMessage = ""
        }
        results.put("returnData", returnData)
        println "Returning "+results
        render results as JSON
    }

    protected void sendInfoMessage(){
        def results = [:]
        results.put("infoMessage", infoMessage)
        infoMessage = ""
        println "Returning "+results
        render results as JSON
    }
}
