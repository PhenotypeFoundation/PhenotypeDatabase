/**
 * StudySearch Domain Class
 *
 * This class provides querying capabilities for searching for studies 
 *
 * @author  Robert Horlings (robert@isdat.nl)
 * @since	20110118
 * @package	dbnp.query
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.query

import groovy.lang.Closure;

import java.util.List;
import java.util.Map;

import dbnp.studycapturing.*
import org.dbnp.gdt.*
import org.apache.commons.logging.LogFactory;

class StudySearch extends Search {
	private static final log = LogFactory.getLog(this);

	public StudySearch() {
		super();
		this.entity = "Study";
	}

	/**
	* Returns a closure for the given entitytype that determines the value for a criterion
	* on the given object. The closure receives two parameters: the object and a criterion.
	*
	* This method should be implemented by all searches
	*
	* For example:
	* 		For a study search, the object given is a study. How to determine the value for that study of
	* 		the criterion field of type sample? This is done by returning the field values for all
	* 		samples in the study
	* 			{ study, criterion -> return study.samples?.collect { criterion.getFieldValue( it ); } }
	* @return
	*/
	protected Closure valueCallback( String entity ) {
		switch( entity ) {
			case "Study":
				return { study, criterion -> return criterion.getFieldValue( study ) }
			case "Subject":
				return { study, criterion -> return study.subjects?.collect { criterion.getFieldValue( it ); } }
			case "Sample":
				return { study, criterion -> return study.samples?.collect { criterion.getFieldValue( it ); } }
			case "Event":
				return { study, criterion -> return study.events?.collect { criterion.getFieldValue( it ); } }
			case "SamplingEvent":
				return { study, criterion -> return study.samplingEvents?.collect { criterion.getFieldValue( it ); } }
			case "Assay":
				return { study, criterion -> return study.assays?.collect { criterion.getFieldValue( it ); } }
			default:
				return null;
		}
	}
	
	/**
	 * Returns the HQL name for the element or collections to be searched in, for the given entity name
	 * For example: when searching for Subject.age > 50 with Study results, the system must search in all study.subjects for age > 50.
     * But when searching for Sample results, the system must search in sample.parentSubject for age > 50
	 * 
     * @param entity	Name of the entity of the criterion
	 * @return			HQL name for this element or collection of elements
	 */
	protected String elementName( String entity ) {
		switch( entity ) {
			case "Study": 			return "study"
			case "Subject":			return "study.subjects"
			case "Sample":			return "study.samples"
			case "Event":			return "study.events"
			case "SamplingEvent":	return "study.samplingEvents"
			case "Assay":			return "study.assays"
			default:				return null;
		}
	} 
	
	/**
	 * Returns true iff the given entity is accessible by the user currently logged in
	 *
	 * @param entity		Study to determine accessibility for. 
	 * @return			True iff the user is allowed to access this study
	 */
	protected boolean isAccessible( def entity ) {
		return entity?.canRead( this.user );
	}
	
	/**
	 * Returns the saved field data that could be shown on screen. This means, the data
	 * is filtered to show only data of the query results. Also, the study title and sample
	 * name are filtered out, in order to be able to show all data on the screen without
	 * checking further
	 *
	 * @return	Map with the entity id as a key, and a field-value map as value
	 */
	public Map getShowableResultFields() {
		Map showableFields = super.getShowableResultFields()
		showableFields.each { sampleElement ->
			sampleElement.value = sampleElement.value.findAll { fieldElement ->
				fieldElement.key != "Study title" && fieldElement.key != "Subject species"
			}
		}
		return showableFields
	}
    
    /**
     * Returns a list of entities from the database, based on the given UUIDs
     *
     * @param uuids      A list of UUIDs for the entities to retrieve
     */
    protected List getEntitiesByUUID( List uuids ) {
        if( !uuids )
            return []
            
        return Study.findAll( "FROM Study WHERE UUID in (:uuids)", [ 'uuids': uuids ] )
    }
}
