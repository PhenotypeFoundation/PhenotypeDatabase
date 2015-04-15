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

import org.dbnp.gdt.*
import grails.util.Holders
import java.text.SimpleDateFormat

import dbnp.authentication.*

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
	 * Description of this search. Defaults to 'Search <id>'
	 */
	public String description;
	
	/**
	 * URL to view the results of this search
	 */
	public String url;

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
	 * Constructor of this search object. Sets the user field to the 
	 * currently logged in user
	 * @see #user
	 */
	public Search() {
		def ctx = Holders.grailsApplication.getMainContext();
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

		// Execute the search
                log.debug "Executing search"
		executeSearch();
                log.debug "Finished executing search"

		// Save the value of this results for later use
                log.debug "Saving result fields"
		saveResultFields();
                log.debug "Finished saving result fields"
	}

	/**
	 * Executes a query
	 */
	protected void executeSearch() {
		// Create HQL query for criteria for the entity being sought
		def selectClause = "" 
		def fullHQL = createHQLForEntity( this.entity );

		// Create SQL for other entities, by executing a subquery first, and
		// afterwards selecting the study based on the entities found
		def resultsFound

		def entityNames = [ "Study", "Subject", "Sample", "Assay", "Event", "SamplingEvent" ];
		for( entityToSearch in entityNames ) {
			// Add conditions for all criteria for the given entity. However,
			// the conditions for the 'main' entity (the entity being sought) are already added
			if( entity != entityToSearch ) {
				resultsFound = addEntityConditions( 
					entityToSearch,															// Name of the entity to search in 
					TemplateEntity.parseEntity( 'dbnp.studycapturing.' + entityToSearch ), 	// Class of the entity to search in
					elementName( entityToSearch ), 											// HQL name of the collection to search in 
					entityToSearch[0].toLowerCase() + entityToSearch[1..-1], 				// Alias for the entity to search in
					fullHQL 																// Current HQL statement
				)
				
				// If no results are found, and we are searching 'inclusive', there will be no 
				// results whatsoever. So we can quit this method now.
				if( !resultsFound && searchMode == SearchMode.and ) {
					return
				}
			}
		}
		
		// Search in all entities
		resultsFound = addWildcardConditions( fullHQL, entityNames )
		if( !resultsFound && searchMode == SearchMode.and ) {
			return
		}
		
		// Generate where clause
		def whereClause = "";
		if( fullHQL.where ) {
			whereClause += " ( " + fullHQL.where.join( " " + searchMode.toString() + " "  ) + " ) "
			whereClause += " AND ";
		} 
		
		// Add a filter such that only readable studies are returned
		def studyName = elementName( "Study" );
		if( this.user == null ) {
			// Anonymous readers are only given access when published and public
			whereClause +=  " ( " + studyName + ".publicstudy = true AND " + studyName + ".published = true )"
		} else if( this.user.hasAdminRights() ) {
			// Administrators are allowed to read every study
			whereClause += " (1 = 1)"
		} else {
			// Owners and writers are allowed to read this study
			// Readers are allowed to read this study when it is published
			whereClause += " ( " + studyName + ".owner = :sessionUser OR :sessionUser member of " + studyName + ".writers OR ( :sessionUser member of " + studyName + ".readers AND " + studyName + ".published = true ) )"
			fullHQL.parameters[ "sessionUser" ] = this.user
		}
		
		// Combine all parts to generate a full HQL query
		def hqlQuery = selectClause + " " + fullHQL.from + ( whereClause ? " WHERE " + whereClause : "" );
		
		// Find all objects 
		def entities = entityClass().findAll( hqlQuery, fullHQL.parameters );
		
		// Find criteria that match one or more 'complex' fields
		// These criteria must be checked extra, since they are not correctly handled
		// by the HQL criteria. See also Criterion.manyToManyWhereCondition and
		// http://opensource.atlassian.com/projects/hibernate/browse/HHH-4615
		entities = filterForComplexCriteria( entities, getEntityCriteria( this.entity ) );
		
                log.debug "Filtered on local criteria."
        
		// Filter on module criteria. If the search is 'and', only the entities found until now
		// should be queried in the module. Otherwise, all entities are sent, in order to retrieve
		// data (to show on screen) for all entities
		if( hasModuleCriteria() ) {
                        log.debug "Starting to filter on module criteria."
			if( searchMode == SearchMode.and ) {
				entities = filterOnModuleCriteria( entities );
			} else {
				entities = filterOnModuleCriteria( entityClass().list().findAll { this.isAccessible( it ) } )
			}
		}
		
		// Determine which studies can be read
		results = entities;
		
	}
		
	/************************************************************************
	 * 
	 * These methods are used in querying and can be overridden by subclasses
	 * in order to provide custom searching
	 * 
	 ************************************************************************/

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
	
	/**
	* Returns the HQL name for the element or collections to be searched in, for the given entity name
	* For example: when searching for Subject.age > 50 with Study results, the system must search in all study.subjects for age > 50.
	* But when searching for Sample results, the system must search in sample.parentSubject for age > 50
	* 
	* This method should be overridden in child classes
	*
	* @param entity	Name of the entity of the criterion
	* @return			HQL name for this element or collection of elements
	*/
   protected String elementName( String entity ) {
	   switch( entity ) {
		   case "Study": 			
		   case "Subject":			
		   case "Sample":			
		   case "Event":			
		   case "SamplingEvent":	
		   case "Assay":			
		   		return entity[ 0 ].toLowerCase() + entity[ 1 .. -1 ]
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
	* This method can be overridden in child classes to enable specific behaviour
	*
	* @param entity		Name of the entity of the criterion
	* @return			HQL where clause for this element or collection of elements
	*/
   protected String entityClause( String entity ) {
	   return ' EXISTS( FROM %1$s %2$s WHERE %2$s IN (:%3$s) )'
   }
   
   /**
    * Returns true iff the given entity is accessible by the user currently logged in
    * 
    * This method should be overridden in child classes, since the check is different for every type of search
    * 
    * @param entity		Entity to determine accessibility for. The entity is of the type 'this.entity'
    * @return			True iff the user is allowed to access this entity
    */
   protected boolean isAccessible( def entity ) {
	   return false
   }
   
   /**
    * Returns a list of entities from the database, based on the given UUIDs
    * 
    * @param uuids      A list of UUIDs for the entities to retrieve
    */
   protected List getEntitiesByUUID( List uuids ) {
       return []
   }

	/****************************************************
	 * 
	 * Helper methods for generating HQL statements
	 * 
	 ****************************************************/
	
	/**
	 * Add all conditions for criteria for a specific entity
	 *
	 * @param entityName	Name of the entity to search in
	 * @param entityClass	Class of the entity to search
	 * @param from			Name of the HQL collection to search in (e.g. study.subjects)
	 * @param alias			Alias of the HQL collection objects (e.g. 'subject')
	 * @param fullHQL		Original HQL map to be extended (fields 'from', 'where' and 'parameters')
	 * @param determineParentId	Closure to determine the id of the final entity to search, based on these objects
	 * @param entityCriteria	(optional) list of criteria to create the HQL for. If no criteria are given, all criteria for the entity are found
	 * @return				True if one ore more entities are found, false otherwise
	 */
	protected boolean addEntityConditions( String entityName, def entityClass, String from, String alias, def fullHQL, def entityCriteria = null ) {
		if( entityCriteria == null )
			entityCriteria = getEntityCriteria( entityName )
		
		// Create HQL for these criteria
		def entityHQL = createHQLForEntity( entityName, entityCriteria );
		
		// If any clauses are generated for these criteria, find entities that match these criteria
		def whereClauses = entityHQL.where?.findAll { it && it?.trim() != "" }
		if( whereClauses ) {
			// First find all entities that match these criteria
			def hqlQuery = entityHQL.from + " WHERE " + whereClauses.join( searchMode == SearchMode.and ? " AND " : " OR " );			
			def entities = entityClass.findAll( hqlQuery, entityHQL.parameters )

			// If there are entities matching these criteria, put a where clause in the full HQL query 
			if( entities ) {
				// Find criteria that match one or more 'complex' fields
				// These criteria must be checked extra, since they are not correctly handled
				// by the HQL criteria. See also Criterion.manyToManyWhereCondition and
				// http://opensource.atlassian.com/projects/hibernate/browse/HHH-4615
				entities = filterForComplexCriteria( entities, entityCriteria );
				
				if( entities ) {
					def paramName = from.replaceAll( /\W/, '' );
					fullHQL.where << sprintf( entityClause( entityName ), from, alias, paramName );
					fullHQL.parameters[ paramName ] = entities
					return true;
				} 
			}
			
			// No results are found.
			results = [];
			return false
		}
		
		return true;
	}
	
	/**
	 * Add all conditions for a wildcard search (all fields in a given entity)
	 * @param fullHQL	Original HQL map to be extended (fields 'from', 'where' and 'parameters')
	 * @return			True if the addition worked
	 */
	protected boolean addWildcardConditions( def fullHQL, def entities) {
		// Append study criteria
		def entityCriteria = getEntityCriteria( "*" );
		
		// If no wildcard criteria are found, return immediately
		if( !entityCriteria )
			return true
			
		// Wildcards should be checked within each entity
		def wildcardHQL = createHQLForEntity( this.entity, entityCriteria, false );
		
		// Create SQL for other entities, by executing a subquery first, and
		// afterwards selecting the study based on the entities found
		entities.each { entityToSearch ->
			// Add conditions for all criteria for the given entity. However,
			// the conditions for the 'main' entity (the entity being sought) are already added
			if( entity != entityToSearch ) {
				addEntityConditions(
					entityToSearch,															// Name of the entity to search in
					TemplateEntity.parseEntity( 'dbnp.studycapturing.' + entityToSearch ), 	// Class of the entity to search in
					elementName( entityToSearch ), 											// HQL name of the collection to search in
					entityToSearch[0].toLowerCase() + entityToSearch[1..-1], 				// Alias for the entity to search in
					wildcardHQL, 															// Current HQL statement
					entityCriteria															// Only create HQL for these criteria
				)
			}
		}
		
		// Add these clauses to the full HQL statement
		def whereClauses = wildcardHQL.where.findAll { it };

		if( whereClauses ) {
			fullHQL.from += wildcardHQL.from
			fullHQL.where << whereClauses.findAll { it }.join( " OR " )
			 
			wildcardHQL[ "parameters" ].each {
				fullHQL.parameters[ it.key ] = it.value
			}
		}
		
		return true;
	}
	
	/**
	 * Create HQL statement for the given criteria and a specific entity
	 * @param entityName		Name of the entity
	 * @param entityCriteria	(optional) list of criteria to create the HQL for. If no criteria are given, all criteria for the entity are found
	 * @param includeFrom		(optional) If set to true, the 'FROM entity' is prepended to the from clause. Defaults to true
	 * @return
	 */
	def createHQLForEntity( String entityName, def entityCriteria = null, includeFrom = true ) {
		def fromClause = includeFrom ? "FROM " + entityName + " " + entityName.toLowerCase() : ""
		def whereClause = []
		def parameters = [:]
		def criterionNum = 0;
		
		// Append study criteria
		if( entityCriteria == null )
			entityCriteria = getEntityCriteria( entityName );
		
		entityCriteria.each {
			def criteriaHQL = it.toHQL( "criterion" +entityName + criterionNum++, entityName.toLowerCase() );
			
			if( criteriaHQL[ "join" ] )
				fromClause += " " + criteriaHQL[ "join" ]
				
			if( criteriaHQL[ "where" ] )
				whereClause << criteriaHQL[ "where" ]
				
			criteriaHQL[ "parameters" ].each {
				parameters[ it.key ] = it.value
			}
		}
		
		return [ "from": fromClause, "where": whereClause, "parameters": parameters ]
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
					return new SimpleDateFormat( "yyyy-MM-dd" ).parse( value.toString() )
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

	/*****************************************************
	*
	* Methods for filtering lists based on specific (GSCF) criteria
	*
	*****************************************************/

	
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
	 * Filters an entity list manually on complex criteria found in the criteria list.
	 * This method is needed because hibernate contains a bug in the HQL INDEX() function.
	 * See also Criterion.manyToManyWhereCondition and
	 * http://opensource.atlassian.com/projects/hibernate/browse/HHH-4615
	 * 
	 * @param entities			List of entities
	 * @param entityCriteria	List of criteria that apply to the type of entities given	(e.g. Subject criteria for Subjects)
	 * @return					Filtered entity list
	 */
	protected filterForComplexCriteria( def entities, def entityCriteria ) {
		def complexCriteria = entityCriteria.findAll { it.isComplexCriterion() }
		
		if( complexCriteria ) {
			def checkCallback = { entity, criterion ->
				def value = criterion.getFieldValue( entity )
				
				if( value == null ) {
					return false
				}

				if( value instanceof Collection ) {
					return value.any { criterion.match( it ) }
				} else {
					return criterion.match( value );
				}
			}
			
			entities = filterEntityList( entities, complexCriteria, checkCallback );
		}
		
		return entities;
	}

	/********************************************************************
	 * 
	 * Methods for filtering object lists on module criteria
	 * 
	 ********************************************************************/

	protected boolean hasModuleCriteria() {
		
		return AssayModule.list().any { module ->
			// Remove 'module' from module name
			def moduleName = module.name.replace( 'module', '' ).trim()
			def moduleCriteria = getEntityCriteria( moduleName );
			return moduleCriteria?.size() > 0
		}
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
		def ctx = Holders.grailsApplication.getMainContext();
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
                                                log.info "Matching module criteria: " + moduleCriteria
						def callUrl = moduleCriteriaUrl( module );
						def callArgs = moduleCriteriaArguments( module, entities, moduleCriteria );
						
						try {
                                                        log.debug "Retrieving module data from " + module
							def moduleEntityUUIDs = moduleCommunicationService.callModuleMethod( module.baseUrl, callUrl, callArgs, "POST" );
                            
                                                        log.debug "Filtering entity list for " + module
                                                        entities = entities.findAll { it.UUID in moduleEntityUUIDs }
						} catch( Exception e ) {
							//log.error( "Error while retrieving data from " + module.name + ": " + e.getMessage() )
							e.printStackTrace()
							throw e
						}
					}
				}
		
				return entities;
			case SearchMode.or:
				def resultingEntities = entities
				
				// Loop through all modules and check whether criteria have been given
				// for that module
				AssayModule.list().each { module ->
                                        log.info "Matching module criteria: " + moduleCriteria
                                        
					// Remove 'module' from module name
					def moduleName = module.name.replace( 'module', '' ).trim()
					def moduleCriteria = getEntityCriteria( moduleName );
		
					if( moduleCriteria && moduleCriteria.size() > 0 ) {
						def callUrl = moduleCriteriaUrl( module );
						def callArgs = moduleCriteriaArguments( module, entities, moduleCriteria );
						
						try {
                                                    log.debug "Retrieving module data from " + module
                                                    def moduleEntityUUIDs = moduleCommunicationService.callModuleMethod( module.baseUrl, callUrl, callArgs, "POST" );

                                                    // See which entities are already selected
                                                    log.debug "Filtering entity list for " + module
                                                    def existingEntityUUIDs = resultingEntities*.UUID
                                                    def resultingEntityUUIDs = moduleEntityUUIDs.findAll { !existingEntityUUIDs.contains( it ) }
                                                    
                                                    // Add the entities not yet selected 
                                                    resultingEntities += getEntitiesByUUID( resultingEntityUUIDs )
                        			} catch( Exception e ) {
							//log.error( "Error while retrieving data from " + module.name + ": " + e.getMessage() )
							e.printStackTrace()
							throw e
						}
					}
				}
		
				return resultingEntities;
			default:
				return [];
		}
	}
	
	/**
	 * Returns a closure for determining the value of a module field 
	 * @param json Should be a map, with the entity UUIDs as key, and the values being a map with keys 
	 *             being the field name and the values the value for that entity and field.
	 * @return Closure to see whether a specific entity matches a criterion. Gets two arguments:
         *               element         The element to check
         *               criterion       The criterion to check on.
         *         Returns true if the criterion holds, false otherwise
	 */
	protected Closure moduleCriterionClosure( def json ) {
		return { entity, criterion ->
			// Find the value of the field in this sample. That value is still in the
			// JSON object
			def token = entity.UUID
			def value
			
			if( criterion.field == '*' ) {
				// Collect the values from all fields
				value = [];
				json[ token ].each { field ->
					if( field.value instanceof Collection ) {
						field.value.each { value << it }
					} else {
						value << field.value;
					}
				}
			} else {
				if( !json[ token ] || json[ token ][ criterion.field ] == null )
					return false;
	
				// Check whether a list or string is given
				value = json[ token ][ criterion.field ];
	
				// Save the value of this entity for later use
				saveResultField( entity.id, criterion.entity + " " + criterion.field, value )
	
				if( !( value instanceof Collection ) ) {
					value = [ value ];
				}
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
	
	protected String moduleCriteriaUrl( module ) {
		def callUrl = module.baseUrl + '/rest/search'
		return callUrl;
	}
	
	protected String moduleCriteriaArguments( module, entities, moduleCriteria ) {
            // Make the module search its data as well. Specify the module criteria in the URL
            // The module will return a list of UUIDs
            def parameters = 'entity=' + this.entity
            
            def criterionNum = 0
            moduleCriteria.each { criterion ->
                parameters += "&criteria." + criterionNum + ".entityfield=" + criterion.entityField().encodeAsURL()
                parameters += "&criteria." + criterionNum + ".operator=" + criterion.operator.toString().encodeAsURL()
                parameters += "&criteria." + criterionNum + ".value=" + criterion.value.toString().encodeAsURL()
                
                criterionNum++
            }

            parameters
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
			if( criterion.field && criterion.field != '*' ) {
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
				if( criterion.field && criterion.field != '*' )
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
		
		if( fieldName == "*" )
			return;
			
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

	
	/************************************************************************
	 * 
	 * Getters and setters
	 * 
	 ************************************************************************/
	
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
   public List filterResults( List selectedTokens ) {
	   if( !selectedTokens || !results )
		   return results

	   return results.findAll {
		   selectedTokens.contains( it.UUID )
	   }
   }

   /**
	* Returns a list of fields for the results of this query. The fields returned are those
	* fields that the query searched for.
	*/
   public Map getResultFields() { return resultFields; }
	
	public String toString() {
		if( this.description ) {
			return this.description
		} else if( this.entity ) {
			return this.entity + " search " + this.id;
		} else {
			return "Search " + this.id
		}
	}
	
	public boolean equals( Object o ) {
		if( o == null )
			return false
		
		if( !( o instanceof Search ) )  
			return false
			
		Search s = (Search) o;
		
		// Determine criteria equality
		def criteriaEqual = false;
		if( !criteria && !s.criteria ) {
			criteriaEqual = true;
		} else if( criteria && s.criteria ) {
			criteriaEqual =	criteria.size()== s.criteria.size() && 
							s.criteria.containsAll( criteria ) && 
							criteria.containsAll( s.criteria ) 
		}
			
		return (	searchMode		== s.searchMode && 
					entity 			== s.entity &&
					criteriaEqual 
		);
	}
	
	/**
	* Returns the class for the entity being searched
	* @return
	*/
	public Class entityClass() {
		if( !this.entity )
			return null;
			
		try {
			return TemplateEntity.parseEntity( 'dbnp.studycapturing.' + this.entity)
		} catch( Exception e ) {
			throw new Exception( "Unknown entity for criterion " + this, e );
		}
	}
	
	/**
	 * Registers a query that has been performed somewhere else, but used in GSCF (e.g. refined)
	 * 
	 * @param description	Description of the search	
	 * @param url			Url to view the search results
	 * @param entity		Entity that has been sought
	 * @param results		List of 
	 * @return
	 */
	public static Search register( String description, String url, String entity, def results ) {
		Search s;
		
		// Determine entity
		switch( entity ) {
			case "Study":
				s = new StudySearch();
				break;
			case "Assay":
				s = new AssaySearch();
				break;
			case "Sample":
				s = new SampleSearch();
				break;
			default:
				return null;
		}
		
		// Set properties
		s.description = description;
		s.url = url
		s.results = results
		
		return s;
	}
}
