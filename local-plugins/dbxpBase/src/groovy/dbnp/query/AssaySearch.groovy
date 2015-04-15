/**
 * AssaySearch Domain Class
 *
 * This class provides querying capabilities for searching for assays 
 *
 * @author  Robert Horlings (robert@isdat.nl)
 * @since	20110118
 * @package	dbnp.query
 *
 * Revision information:
 * $Rev: 1524 $
 * $Author: robert@isdat.nl $
 * $Date: 2011-02-15 15:05:23 +0100 (Tue, 15 Feb 2011) $
 */
package dbnp.query

import groovy.lang.Closure;

import java.util.Map;

import dbnp.studycapturing.*
import org.dbnp.gdt.*
import org.apache.commons.logging.LogFactory;

class AssaySearch extends Search {
	private static final log = LogFactory.getLog(this);

	public AssaySearch() {
		super();

		this.entity = "Assay";
	}

	/**
	 * Returns a closure for the given entitytype that determines the value for a criterion
	 * on the given object. The closure receives two parameters: the object and a criterion.
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
				return { assay, criterion -> return criterion.getFieldValue( assay.parent ) }
			case "Subject":
				return { assay, criterion -> return assay.samples?.parentSubject?.collect { criterion.getFieldValue( it ) } }
			case "Sample":
				return { assay, criterion -> return assay.samples?.collect { criterion.getFieldValue( it ) } }
			case "Event":
				return { assay, criterion ->
					def values = []
					assay.samples?.each { sample ->
						if( sample && sample.parentEventGroup && sample.parentEventGroup.events && sample.parentEventGroup.events.size() > 0 ) {
							values << sample.parentEventGroup.events.collect { criterion.getFieldValue( it ) };
						}
					}
					return values;
				}
			case "SamplingEvent":
				return { assay, criterion -> return assay.samples?.parentEvent?.collect { criterion.getFieldValue( it ) } }
			case "Assay":
				return { assay, criterion -> return criterion.getFieldValue( assay ) }
			default:
				return super.valueCallback( entity );
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
			case "Assay":			return "assay"
			case "Sample":			return "assay.samples"
			case "Study": 			return "assay.parent"
			
			case "Subject":			return "assay.samples.parentSubject"			// Will not be used, since entityClause() is overridden
			case "SamplingEvent":	return "assay.samples.parentEvent"				// Will not be used, since entityClause() is overridden
			case "Event":			return "assay.samples.parentEventGroup.events"	// Will not be used, since entityClause() is overridden
			default:				return null;
		}
	}

	/**
	 * Returns the a where clause for the given entity name
	 * For example: when searching for Subject.age > 50 with Study results, the system must search
	 *
	 * 	WHERE EXISTS( FROM study.subjects subject WHERE subject IN (...)
	 *
	 * The returned string is fed to sprintf with 3 string parameters:
	 * 		from (in this case 'study.subjects'
	 * 		alias (in this case 'subject'
	 * 		paramName (in this case '...')
	 *
	 * @param entity		Name of the entity of the criterion
	 * @return			HQL where clause for this element or collection of elements
	 */
	protected String entityClause( String entity ) {
		switch( entity ) {
			case "Subject":
				return 'EXISTS( FROM assay.samples sample WHERE sample.parentSubject IN (:%3$s) )'
			case "SamplingEvent":
				return 'EXISTS( FROM assay.samples sample WHERE sample.parentEvent IN (:%3$s) )'
			case "Event":
				return 'EXISTS( FROM assay.samples sample WHERE EXISTS( FROM sample.parentEventGroup.events event WHERE event IN (:%3$s) ) )'
			default:
				return super.entityClause( entity );
		}
	}

	/**
	 * Returns true iff the given entity is accessible by the user currently logged in
	 *
	 * @param entity		Study to determine accessibility for.
	 * @return			True iff the user is allowed to access this study
	 */
	protected boolean isAccessible( def entity ) {
		return entity?.parent?.canRead( this.user );
	}

	
	/**
	 * Returns the saved field data that could be shown on screen. This means, the data 
	 * is filtered to show only data of the query results. Also, the study title and assay
	 * name are filtered out, in order to be able to show all data on the screen without
	 * checking further
	 *
	 * @return	Map with the entity id as a key, and a field-value map as value
	 */
	public Map getShowableResultFields() {
		Map showableFields = super.getShowableResultFields()
		showableFields.each { sampleElement ->
			sampleElement.value = sampleElement.value.findAll { fieldElement ->
				fieldElement.key != "Study title" && fieldElement.key != "Assay name"
			}
		}
	}
    
    
    /**
     * Returns a list of entities from the database, based on the given UUIDs
     *
     * @param uuids      A list of UUIDs for the entities to retrieve
     */
    protected List getEntitiesByUUID( List uuids ) {
        if( !uuids )
            return []
            
        return Assay.findAll( "FROM Assay WHERE UUID in (:uuids)", [ 'uuids': uuids ] )
    }

}
