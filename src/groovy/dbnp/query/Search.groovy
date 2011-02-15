/**
 * Search Domain Class
 *
 * Abstract class containing search criteria and search results when querying.
 * Should be subclassed in order to enable searching for different entities.
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

import nl.grails.plugins.gdt.*
import java.util.List;
import java.text.DateFormat;
import java.text.SimpleDateFormat
import java.util.List;

import org.springframework.context.ApplicationContext
import org.springframework.web.context.request.RequestContextHolder;
import org.codehaus.groovy.grails.commons.ApplicationHolder;

import dbnp.authentication.*

import org.dbnp.gdt.*

/**
 * Available boolean operators for searches
 * @author robert
 *
 */
enum SearchMode {
	and, or
}

class Search {
	/**
	 * User that is performing this search. This has impact on the search results returned.
	 */
	public SecUser user;

	/**
	 * Date of execution of this search
	 */
	public Date executionDate;

	/**
	 * Public identifier of this search. Is only used when this query is saved in session
	 */
	public int id;

	/**
	 * Human readable entity name of the entities that can be found using this search
	 */
	public String entity;

	/**
	 * Mode to search: OR or AND.
	 * @see SearchMode
	 */
	public SearchMode searchMode = SearchMode.and

	protected List criteria;
	protected List results;
	protected Map resultFields = [:];

	/**
	 * Returns a list of Criteria
	 */
	public List getCriteria() { return criteria; }

	/**
	 * Sets a new list of criteria
	 * @param c	List with criteria objects
	 */
	public void setCriteria( List c ) { criteria = c; }

	/**
	 * Adds a criterion to this query
	 * @param c	Criterion
	 */
	public void addCriterion( Criterion c ) {
		if( criteria )
			criteria << c;
		else
			criteria = [c];
	}

	/**
	 * Retrieves the results found using this query. The result is empty is 
	 * the query has not been executed yet.
	 */
	public List getResults() { return results; }

	/**
	 * Returns the results found using this query, filtered by a list of ids.
	 * @param selectedIds	List with ids of the entities you want to return.
	 * @return	A list with only those results for which the id is in the selectedIds
	 */
	public List filterResults( List selectedIds ) {
		if( !selectedIds || !results )
			return results

		return results.findAll {
			selectedIds.contains( it.id )
		}
	}

	/**
	 * Returns a list of fields for the results of this query. The fields returned are those
	 * fields that the query searched for. 
	 */
	public Map getResultFields() { return resultFields; }

	/**
	 * Constructor of this search object. Sets the user field to the 
	 * currently logged in user
	 * @see #user
	 */
	public Search() {
		def ctx = ApplicationHolder.getApplication().getMainContext();
		def authenticationService = ctx.getBean("authenticationService");
		def sessionUser = authenticationService?.getLoggedInUser();

		if( sessionUser )
			this.user = sessionUser;
		else
			this.user = null
	}

	/**
	 * Returns the number of results found by this search
	 * @return
	 */
	public int getNumResults() {
		if( results )
			return results.size();

		return 0;
	}

	/**
	 * Executes a search based on the given criteria. Should be filled in by
	 * subclasses searching for a specific entity
	 * 
	 * @param	c	List with criteria to search on
	 */
	public void execute( List c ) {
		setCriteria( c );
		execute();
	}

	/**
	 * Executes a search based on the given criteria. 
	 */
	public void execute() {
		this.executionDate = new Date();

		switch( searchMode ) {
			case SearchMode.and:
				executeAnd();
				break;
			case SearchMode.or:
				executeOr();
				break;
		}

		// Save the value of this results for later use
		saveResultFields();
	}

	/**
	 * Executes an inclusive (AND) search based on the given criteria. Should be filled in by
	 * subclasses searching for a specific entity
	 */
	public void executeAnd() {

	}

	/**
	 * Executes an exclusive (OR) search based on the given criteria. Should be filled in by
	 * subclasses searching for a specific entity
	 */
	public void executeOr() {

	}

	/************************************************************************
	 * 
	 * These methods are used in querying and should be overridden by subclasses
	 * in order to provide custom searching
	 * 
	 */

	/**
	 * Returns a closure for the given entitytype that determines the value for a criterion
	 * on the given object. The closure receives two parameters: the object and a criterion.
	 * 
	 * For example: when searching for studies, the object given to the closure is a Study. 
	 * Also, when searching for samples, the object given is a Sample. When you have the criterion
	 *
	 * 	sample.name equals 'sample 1'
	 * 
	 * and searching for samples, it is easy to determine the value of the object for this criterion:
	 * 	
	 * 	object.getFieldValue( "name" )
	 * 
	 * 
	 * However, when searching for samples with the criterion
	 * 
	 * 	study.title contains 'nbic'
	 * 
	 * this determination is more complex:
	 * 
	 * 	object.parent.getFieldValue( "title" )
	 * 
	 * 
	 * The other way around, when searching for studies with 
	 * 
	 * 	sample.name equals 'sample 1'
	 * 
	 * the value of the 'sample.name' property is a list:
	 * 
	 *  	object.samples*.getFieldValue( "name" )
	 *  
	 * The other search methods will handle the list and see whether any of the values 
	 * matches the criterion.
	 * 
	 * NB. The Criterion object has a convenience method to retrieve the field value on a
	 * specific (TemplateEntity) object: getFieldValue. This method also handles 
	 * non-existing fields and casts the value to the correct type.
	 *
	 * This method should be overridden by all searches
	 * 
	 * @see Criterion.getFieldValue()
	 *
	 * @return	Closure having 2 parameters: object and criterion
	 */
	protected Closure valueCallback( String entity ) {
		switch( entity ) {
			case "Study":
			case "Subject":
			case "Sample":
			case "Event":
			case "SamplingEvent":
			case "Assay":
				return { object, criterion -> return criterion.getFieldValue( object ); }
			default:
				return null;
		}
	}

	/*****************************************************
	 * 
	 * The other methods are helper functions for the execution of queries in subclasses
	 * 
	 *****************************************************/

	/**
	 * Returns a list of criteria targeted on the given entity
	 * @param entity	Entity to search criteria for
	 * @return			List of criteria
	 */
	protected List getEntityCriteria( String entity ) {
		return criteria?.findAll { it.entity == entity }
	}

	/**
	 * Filters a list with entities, based on the given criteria and a closure to check whether a criterion is matched
	 * 
	 * @param entities	Original list with entities to check for these criteria
	 * @param criteria	List with criteria to match on
	 * @param check		Closure to see whether a specific entity matches a criterion. Gets two arguments:
	 * 						element		The element to check 
	 * 						criterion	The criterion to check on.
	 * 					Returns true if the criterion holds, false otherwise
	 * @return			The filtered list of entities
	 */
	protected List filterEntityList( List entities, List<Criterion> criteria, Closure check ) {
		if( !entities || !criteria || criteria.size() == 0 ) {
			if( searchMode == SearchMode.and )
				return entities;
			else if( searchMode == SearchMode.or )
				return []
		}

		return entities.findAll { entity ->
			if( searchMode == SearchMode.and ) {
				for( criterion in criteria ) {
					if( !check( entity, criterion ) ) {
						return false;
					}
				}
				return true;
			} else if( searchMode == SearchMode.or ) {
				for( criterion in criteria ) {
					if( check( entity, criterion ) ) {
						return true;
					}
				}
				return false;
			}
		}
	}

	/**
	 * Prepares a value from a template entity for comparison, by giving it a correct type
	 *
	 * @param value		Value of the field 
	 * @param type		TemplateFieldType	Type of the specific field
	 * @return			The value of the field in the correct entity
	 */
	public static def prepare( def value, TemplateFieldType type ) {
		if( value == null )
			return value

		switch (type) {
			case TemplateFieldType.DATE:
				try {
					return new SimpleDateFormat( "yyyy-MM-dd" ).parse( value )
				} catch( Exception e ) {
					return value.toString();
				}
			case TemplateFieldType.RELTIME:
				try {
					if( value instanceof Number ) {
						return new RelTime( value );
					} else if( value.toString().isNumber() ) {
						return new RelTime( Long.parseLong( value.toString() ) )
					} else {
						return new RelTime( value );
					}
				} catch( Exception e ) {
					try {
						return Long.parseLong( value )
					} catch( Exception e2 ) {
						return value.toString();
					}
				}
			case TemplateFieldType.DOUBLE:
				try {
					return Double.valueOf( value )
				} catch( Exception e ) {
					return value.toString();
				}
			case TemplateFieldType.BOOLEAN:
				try {
					return Boolean.valueOf( value )
				} catch( Exception e ) {
					return value.toString();
				}
			case TemplateFieldType.LONG:
				try {
					return Long.valueOf( value )
				} catch( Exception e ) {
					return value.toString();
				}
			case TemplateFieldType.STRING:
			case TemplateFieldType.TEXT:
			case TemplateFieldType.STRINGLIST:
			case TemplateFieldType.TEMPLATE:
			case TemplateFieldType.MODULE:
			case TemplateFieldType.FILE:
			case TemplateFieldType.ONTOLOGYTERM:
			default:
				return value.toString();
		}

	}

	/**
	 * Filters the given list of studies on the study criteria
	 * @param studies		Original list of studies
	 * @param entity		Name of the entity to check the criteria for
	 * @param valueCallback	Callback having a study and criterion as input, returning the value of the field to check on
	 * @return				List with all studies that match the Criteria
	 */
	protected List filterOnTemplateEntityCriteria( List studies, String entityName, Closure valueCallback ) {
		def criteria = getEntityCriteria( entityName );

		def checkCallback = { study, criterion ->
			def value = valueCallback( study, criterion );

			if( value == null ) {
				return false
			}

			if( value instanceof Collection ) {
				return criterion.matchAny( value )
			} else {
				return criterion.match( value );
			}
		}

		return filterEntityList( studies, criteria, checkCallback);
	}

	/**
	 * Filters the given list of studies on the study criteria
	 * @param studies	Original list of studies
	 * @return			List with all studies that match the Study criteria
	 */
	protected List filterOnStudyCriteria( List studies ) {
		def entity = "Study"
		return filterOnTemplateEntityCriteria(studies, entity, valueCallback( entity ) )
	}

	/**
	 * Filters the given list of studies on the subject criteria
	 * @param studies	Original list of studies
	 * @return			List with all studies that match the Subject-criteria
	 */
	protected List filterOnSubjectCriteria( List studies ) {
		def entity = "Subject"
		return filterOnTemplateEntityCriteria(studies, entity, valueCallback( entity ) )
	}

	/**
	 * Filters the given list of studies on the sample criteria
	 * @param studies	Original list of studies
	 * @return			List with all studies that match the sample-criteria
	 */
	protected List filterOnSampleCriteria( List studies ) {
		def entity = "Sample"
		return filterOnTemplateEntityCriteria(studies, entity, valueCallback( entity ) )
	}

	/**
	 * Filters the given list of studies on the event criteria
	 * @param studies	Original list of studies
	 * @return			List with all studies that match the event-criteria
	 */
	protected List filterOnEventCriteria( List studies ) {
		def entity = "Event"
		return filterOnTemplateEntityCriteria(studies, entity, valueCallback( entity ) )
	}

	/**
	 * Filters the given list of studies on the sampling event criteria
	 * @param studies	Original list of studies
	 * @return			List with all studies that match the event-criteria
	 */
	protected List filterOnSamplingEventCriteria( List studies ) {
		def entity = "SamplingEvent"
		return filterOnTemplateEntityCriteria(studies, entity, valueCallback( entity ) )
	}

	/**
	 * Filters the given list of studies on the assay criteria
	 * @param studies	Original list of studies
	 * @return			List with all studies that match the assay-criteria
	 */
	protected List filterOnAssayCriteria( List studies ) {
		def entity = "Assay"
		return filterOnTemplateEntityCriteria(studies, entity, valueCallback( entity ) )
	}

	/**
	 * Filters the given list of entities on the module criteria
	 * @param entities	Original list of entities. Entities should expose a giveUUID() method to give the token.
	 * @return			List with all entities that match the module criteria
	 */
	protected List filterOnModuleCriteria( List entities ) {
		// An empty list can't be filtered more than is has been now
		if( !entities || entities.size() == 0 )
			return [];

		// Determine the moduleCommunicationService. Because this object
		// is mocked in the tests, it can't be converted to a ApplicationContext object
		def ctx = ApplicationHolder.getApplication().getMainContext();
		def moduleCommunicationService = ctx.getBean("moduleCommunicationService");

		switch( searchMode ) {
			case SearchMode.and:
				// Loop through all modules and check whether criteria have been given
				// for that module
				AssayModule.list().each { module ->
					// Remove 'module' from module name
					def moduleName = module.name.replace( 'module', '' ).trim()
					def moduleCriteria = getEntityCriteria( moduleName );
		
					if( moduleCriteria && moduleCriteria.size() > 0 ) {
						def callUrl = moduleCriteriaUrl( module, entities, moduleCriteria );
						
						try {
							def json = moduleCommunicationService.callModuleRestMethodJSON( module.url, callUrl );
							Closure checkClosure = moduleCriterionClosure( json );
							entities = filterEntityList( entities, moduleCriteria, checkClosure );
						} catch( Exception e ) {
							log.error( "Error while retrieving data from " + module.name + ": " + e.getMessage() )
						}
					}
				}
		
				return entities;
			case SearchMode.or:
				def resultingEntities = []
				
				// Loop through all modules and check whether criteria have been given
				// for that module
				AssayModule.list().each { module ->
					// Remove 'module' from module name
					def moduleName = module.name.replace( 'module', '' ).trim()
					def moduleCriteria = getEntityCriteria( moduleName );
		
					if( moduleCriteria && moduleCriteria.size() > 0 ) {
						def callUrl = moduleCriteriaUrl( module, entities, moduleCriteria );
						
						try {
							def json = moduleCommunicationService.callModuleRestMethodJSON( module.url, callUrl );
							Closure checkClosure = moduleCriterionClosure( json );
							
							resultingEntities += filterEntityList( entities, moduleCriteria, checkClosure );
							resultingEntities = resultingEntities.unique();
							
						} catch( Exception e ) {
							log.error( "Error while retrieving data from " + module.name + ": " + e.getMessage() )
						}
					}
				}
		
				println this.resultFields;
				
				return resultingEntities;
			default:
				return [];
		}
	}
	
	/**
	 * Returns a closure for determining the value of a module field 
	 * @param json
	 * @return
	 */
	protected Closure moduleCriterionClosure( def json ) {
		return { entity, criterion ->
			// Find the value of the field in this sample. That value is still in the
			// JSON object
			def token = entity.giveUUID()
			if( !json[ token ] || json[ token ][ criterion.field ] == null )
				return false;

			// Check whether a list or string is given
			def value = json[ token ][ criterion.field ];

			// Save the value of this entity for later use
			saveResultField( entity.id, criterion.entity + " " + criterion.field, value )

			if( !( value instanceof Collection ) ) {
				value = [ value ];
			}

			// Convert numbers to a long or double in order to process them correctly
			def values = value.collect { val ->
				val = val.toString();
				if( val.isLong() ) {
					val = Long.parseLong( val );
				} else if( val.isDouble() ) {
					val = Double.parseDouble( val );
				}
				return val;
			}

			// Loop through all values and match any
			for( val in values ) {
				if( criterion.match( val ) )
					return true;
			}

			return false;
		}
	}
	
	protected String moduleCriteriaUrl( module, entities, moduleCriteria ) {
		// Retrieve the data from the module
		def tokens = entities.collect { it.giveUUID() }.unique();
		def fields = moduleCriteria.collect { it.field }.unique();
	
		def callUrl = module.url + '/rest/getQueryableFieldData?entity=' + this.entity
		tokens.sort().each { callUrl += "&tokens=" + it.encodeAsURL() }
		fields.sort().each { callUrl += "&fields=" + it.encodeAsURL() }

		return callUrl;
	}

	/*********************************************************************
	 * 
	 * These methods are used for saving information about the search results and showing the information later on.
	 * 
	 *********************************************************************/

	/**
	 * Saves data about template entities to use later on. This data is copied to a special
	 * structure to make it compatible with data fetched from other modules. 
	 * @see #saveResultField()
	 */
	protected void saveResultFields() {
		if( !results || !criteria )
			return

		criteria.each { criterion ->
			if( criterion.field ) {
				def valueCallback = valueCallback( criterion.entity );
				
				if( valueCallback != null ) {
					def name = criterion.entity + ' ' + criterion.field
	
					results.each { result ->
						saveResultField( result.id, name, valueCallback( result, criterion ) );
					}
				}
			}
		}
	}

	/**
	 * Saves data about template entities to use later on. This data is copied to a special
	 * structure to make it compatible with data fetched from other modules.
	 * @param entities			List of template entities to find data in
	 * @param criteria			Criteria to search for
	 * @param valueCallback		Callback to retrieve a specific field from the entity
	 * @see #saveResultField()
	 */
	protected void saveResultFields( entities, criteria, valueCallback ) {
		for( criterion in criteria ) {
			for( entity in entities ) {
				if( criterion.field )
					saveResultField( entity.id, criterion.entity + ' ' + criterion.field, valueCallback( entity, criterion ) )
			}
		}
	}


	/**
	 * Saves a specific field of an object to use later on. Especially useful when looking up data from other modules.
	 * @param id		ID of the object
	 * @param fieldName	Field name that has been searched
	 * @param value		Value of the field
	 */
	protected void saveResultField( id, fieldName, value ) {
		if( resultFields[ id ] == null )
			resultFields[ id ] = [:]

		// Handle special cases
		if( value == null )
			value = "";

		if( value instanceof Collection ) {
			value = value.findAll { it != null }
		}

		resultFields[ id ][ fieldName ] = value;
	}

	/** 
	 * Removes all data from the result field map
	 */
	protected void clearResultFields() {
		resultFields = [:]
	}

	/**
	 * Returns the saved field data that could be shown on screen. This means, the data is filtered to show only data of the query results.
	 * 
	 * Subclasses could filter out the fields they don't want to show on the result screen (e.g. because they are shown regardless of the 
	 * query.)
	 * @return	Map with the entity id as a key, and a field-value map as value
	 */
	public Map getShowableResultFields() {
		def resultIds = getResults()*.id;
		return getResultFields().findAll {
			resultIds.contains( it.key )
		}
	}
	
	/**
	 * Returns the field names that are found in the map with showable result fields
	 * 
	 * @param fields	Map with showable result fields
	 * @see getShowableResultFields
	 * @return
	 */
	public List getShowableResultFieldNames( fields ) {
		return fields.values()*.keySet().flatten().unique();
	}

	public String toString() {
		return ( this.entity ? this.entity + " search" : "Search" ) + " " + this.id
	}
}
