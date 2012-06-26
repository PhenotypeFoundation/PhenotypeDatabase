/**
 * DataService Service
 * 
 * Description of my service
 *
 * @author  your email (+name?)
 * @since    2010mmdd
 * @package    ???
 *
 * Revision information:
 * $Rev: 1430 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
 */
package dbnp.generic

import java.util.List;
import java.util.Map;

import dbnp.studycapturing.*;
import grails.converters.JSON
import groovy.lang.Closure;

import org.dbnp.gdt.*

class DataService {

    static transactional = true
    def moduleCommunicationService

    
    /**
     * Gather fields related to this study from modules.
        This will use the getMeasurements RESTful service. That service returns measurement types, AKA features.
        getMeasurements does not actually return measurements (the getMeasurementData call does).
     * @param source    The id of the module that is the source of the requested fields, as can be obtained from AssayModule.list() (use the 'id' field)
     * @param assay     The assay that the source module and the requested fields belong to
     * @return  A map with 
     *              key "fields": contains a list of map objects, containing the following:
     *                  - a key 'id' with a value formatted by the createFieldId function
     *                  - a key 'source' with a value equal to the input parameter 'source'
     *                  - a key 'category' with a value equal to the 'name' field of the input parameter 'assay'
     *                  - a key 'name' with a value equal to the name of the field in question, as determined by the source value
     *              key "offlineModules": contains a list with the modules that did not respond. 
     *                  This is just for convenience. The caller can use this to keep response time low by not checking an offline
     *                  module more than once.
     */
    def getFieldsFromModules(source, assay) {
        def fields = []
		def offlineModules = []
        def callUrl = ""

        // Making a different call for each assay
        def urlVars = "assayToken="+assay.assayUUID
        try {
            callUrl = ""+assay.module.url + "/rest/getMeasurementMetaData/query?"+urlVars
            def json = moduleCommunicationService.callModuleRestMethodJSON( assay.module.url /* consumer */, callUrl );

            def collection = []
            json.each{ jason ->
                collection.add(jason)
            }
            // Formatting the data
            collection.each { field ->
                // For getting this field from this assay
                fields << [ "id": createFieldId( id: field.name, name: field.name, source: ""+assay.id, type: ""+assay.name, unit: (field.unit?:"")), "source": source, "category": ""+assay.name, "name": field.name + (field.unit?" ("+field.unit+")":"")  ]
            }
        } catch(Exception e){
            // An error occured while trying to collect field data from a module. Most likely, this module is offline
            offlineModules.add([id: assay.module.id, name: assay.module.name])
            log.error("DataService: getFields: "+e)
        }

        return [fields: fields, offlineModules: offlineModules]
    }

    /**
     * Gather fields related to this study from GSCF.
     * @param study The study that is the source of the requested fields
     * @param category  The domain that a field (a property in this case) belongs to, e.g. "subjects", "samplingEvents"
     * @param type A string that indicates the type of field, either "domainfields" or "templatefields".
     * @return A list of map objects, formatted by the formatGSCFFields function
     */
    def getFieldsFromGSCF(study, category, type){
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
                fields << [ "id": createFieldId( id: collectionOfFields.name, name: collectionOfFields.name, source: source, type: category, unit: (collectionOfFields.unit?:"") ), "source": source, "category": category, "name": collectionOfFields.name + (collectionOfFields.unit?" ("+collectionOfFields.unit+")":"") ]
            }
            if(type=="templatefields"){
                fields << [ "id": createFieldId( id: collectionOfFields.id.toString(), name: collectionOfFields.name, source: source, type: category, unit: (collectionOfFields.unit?:"") ), "source": source, "category": category, "name": collectionOfFields.name + (collectionOfFields.unit?" ("+collectionOfFields.unit+")":"")]
            }
            return fields
        }
    }
    
    /**
    * Retrieve the field data for the selected fields
    * @param study      Study for which the data should be retrieved
    * @param samples    Samples for which the data should be retrieved
    * @param fields     Map with key-value pairs determining the name and fieldId to retrieve data for. Example:
    *                       [ "x": "field-id-1", "y": "field-id-3", "group": "field-id-6" ]
    * @return           A map with the same keys as the input fields. The values in the map are lists of values of the
    *                       selected field for all samples. If a value could not be retrieved for a sample, null is returned. Example:
    *                       [ "numValues": 4, "x": [ 3, 6, null, 10 ], "y": [ "male", "male", "female", "female" ], "group": [ "US", "NL", "NL", "NL" ] ]
    */
   def getAllFieldData( study, samples, fields ) {
       def fieldData = [:]
       def numValues = 0;
       fields.each{ field ->
           def fieldId = field.value ?: null;
           fieldData[ field.key ] = getFieldData( study, samples, fieldId );
           
           if( fieldData[ field.key ] )
               numValues = Math.max( numValues, fieldData[ field.key ].size() );
       }
       
       fieldData.numValues = numValues;
       
       return fieldData;
   }
   
   /**
   * Retrieve the field data for the selected field
   * @param study      Study for which the data should be retrieved
   * @param samples    Samples for which the data should be retrieved
   * @param fieldId    ID of the field to return data for
   * @return           A list of values of the selected field for all samples. If a value
   *                       could not be retrieved for a sample, null is returned. Examples:
   *                       [ 3, 6, null, 10 ] or [ "male", "male", "female", "female" ]
   */
   def getFieldData( study, samples, fieldId ) {
       if( !fieldId )
           return null
           
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

                   data << value;
               }
           } else {
               // Closure could not be retrieved, probably because the type is incorrect
               data = samples.collect { return null }
               log.error("DataService: getFieldData: Requested wrong field type: "+parsedField.type+". Parsed field: "+parsedField)
           }
       } else {
           // Data must be retrieved from a module
           data = getModuleData( study, samples, parsedField.source, parsedField.name );
       }
       
       return data
   }
   
   /**
    * Retrieve data for a given field from a data module
    * @param study            Study to retrieve data for
    * @param samples          Samples to retrieve data for
    * @param source_module    Name of the module to retrieve data from
    * @param fieldName        Name of the measurement type to retrieve (i.e. measurementToken)
    * @return                 A list of values of the selected field for all samples. If a value
    *                             could not be retrieved for a sample, null is returned. Examples:
    *                             [ 3, 6, null, 10 ] or [ "male", "male", "female", "female" ]
    */
   def getModuleData( study, samples, assay_id, fieldName ) {
       def data = []
       
       // TODO: Handle values that should be retrieved from multiple assays
       def assay = Assay.get(assay_id);

       if( assay ) {
           // Request for a particular assay and a particular feature
           def urlVars = "assayToken=" + assay.assayUUID + "&measurementToken="+fieldName.encodeAsURL()
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
                       
                       // Store the measurement value if found and if it is not JSONObject$Null
                       // See http://grails.1312388.n4.nabble.com/The-groovy-truth-of-JSONObject-Null-td3661040.html
                       // for this comparison
                       if( index > -1  && !measurements[ index ].equals( null ) ) {
                           data << measurements[ index ];
                       } else {
                           data << null
                       }
                   }
               } else {
                   // We did not get anything back. We fail gracefully
                   // Returns an empty list with as many elements as there are samples
                   data = samples.collect { return null }
               }

           } catch(Exception e){
               log.error("DataService: getFields: "+e)
               return returnError(404, "Unfortunately, "+assay.module.name+" could not be reached. As a result, we cannot at this time visualize data contained in this module.")
           }
       } else {
           // TODO: Handle error correctly
           // Returns an empty list with as many elements as there are samples
           data = samples.collect { return null }
       }
       return data
   }
   
   
   
   /**
   * Returns the domain object that should be used with the given entity string
   *
   * For example:
   *         What object should be consulted if the user asks for "studies"
   *         Response: Study
   * @return    Domain object that should be used with the given entity string
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
          case "EventGroup":
          case "eventGroups":
                  return EventGroup
       
      }
  }

    /**
    * Returns a closure for the given entitytype that determines the value for a criterion
    * on the given object. The closure receives two parameters: the sample and a field.
    *
    * For example:
    *         How can one retrieve the value for subject.name, given a sample? This can be done by
    *         returning the field values sample.parentSubject:
    *             { sample, field -> return getFieldValue( sample.parentSubject, field ) }
    * @return    Closure that retrieves the value for a field and the given field
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
           case "EventGroup":
           case "eventGroups":
               return { sample, field ->
                   if( !sample || !sample.parentEventGroup )
                       return null

                   // For eventgroups only the name is supported
                   if( field == "name" )
                       return sample.parentEventGroup.name
                   else
                       return null
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
    * Returns a field for a given templateentity
    * @param object    TemplateEntity (or subclass) to retrieve data for
    * @param fieldName    Name of the field to return data for.
    * @return            Value of the field or null if the value could not be retrieved
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
    * Returns the objects within the given study that should be used with the given entity string
    *
    * For example:
    *         What object should be consulted if the user asks for "samples"
    *         Response: study.samples
    * @return    List of domain objects that should be used with the given entity string
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
     * Create a fieldId based on the given attributes
     * @param attrs        Map of attributes for this field. Keys may be 'name', 'id', 'source' and 'type'
     * @return            Unique field ID for these parameters
     * @see parseFieldId
     */
    protected String createFieldId( Map attrs ) {
        // TODO: What if one of the attributes contains a comma?
        def name = attrs.name.toString();
        def id = (attrs.id ?: name).toString();
        def source = attrs.source.toString();
        def type = (attrs.type ?: "").toString();
        def unit = (attrs.unit ?: "").toString();

        return id.bytes.encodeBase64().toString() + "," +
                name.bytes.encodeBase64().toString() + "," +
                source.bytes.encodeBase64().toString() + "," +
                type.bytes.encodeBase64().toString() + "," +
                unit.bytes.encodeBase64().toString();
    }

    /**
     * Parses a fieldId that has been created earlier by createFieldId
     * @param fieldId    FieldId to parse
     * @return            Map with attributes of the selected field. Keys are 'name', 'id', 'source' and 'type'
     * @see createFieldId
     */
    protected Map parseFieldId( String fieldId ) {
        def attrs = [:]

        if( !fieldId ) 
            return null;
        
        def parts = fieldId.split(",",5)

        attrs = [
            "id": new String(parts[ 0 ].decodeBase64()),
            "name": new String(parts[ 1 ].decodeBase64()),
            "source": new String(parts[ 2 ].decodeBase64()),
            "type": new String(parts[ 3 ].decodeBase64()),
            "unit": parts.length>4? new String(parts[ 4 ].decodeBase64()) : null,
            "fieldId": fieldId
        ]

        return attrs
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
	* Computes value of a percentile of the given values. Values that can not be parsed to a number
	* are ignored. If no values are given, null is returned.
	* @param values	 List of values to compute the percentile for
	* @param Percentile Integer that indicates which percentile to calculae
	*                   Example: Percentile=50 calculates the median,
	*                            Percentile=25 calculates Q1
	*                            Percentile=75 calculates Q3
	* @return			The value at the Percentile of the values
	*/
   protected def computePercentile( List values, int Percentile ) {
	   def listOfValues = [];
	   values.each { value ->
		   def num = getNumericValue( value );
		   if( num != null ) {
			   listOfValues << num;
		   }
	   }

	   listOfValues.sort();

	   def listSize = listOfValues.size()-1;

	   def objReturn = null;
	   def objMin = null;
	   def objMax = null;

	   def dblFactor = Percentile/100;

	   if( listSize >= 0 ) {
		   def intPointer = (int) Math.abs(listSize*dblFactor);
		   if(intPointer==listSize*dblFactor) {
			   // If we exactly end up at an item, take this item
			   objReturn = listOfValues.get(intPointer);
		   } else {
			   // If we don't exactly end up at an item, take the mean of the 2 adjecent values
			   objReturn = (listOfValues.get(intPointer)+listOfValues.get(intPointer+1))/2;
		   }

		   objMin = listOfValues.get(0);
		   objMax = listOfValues.get(listSize);
	   }

	   return ["value": objReturn, "min": objMin, "max": objMax];
   }
   /**
    * Computes the median (50th percentile)
    * @param values List of values to compute the median for
    * @return The median value
    * @see computePercentile
    */
   protected def computeMedian( List values ){
       return computePercentile( values, 50 )
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
}
