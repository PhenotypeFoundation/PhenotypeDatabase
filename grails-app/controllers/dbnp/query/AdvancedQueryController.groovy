package dbnp.query
import dbnp.modules.*
import org.dbnp.gdt.*

// TODO: Make use of the searchable-plugin possibilities instead of querying the database directly

/**
 * Basic web interface for searching within studies
 *
 * @author Robert Horlings (robert@isdat.nl)
 */
class AdvancedQueryController {
	def moduleCommunicationService;
	def authenticationService

	def entitiesToSearchFor = [ 'Study': 'Studies', 'Sample': 'Samples']
	
	/**
	 * Shows search screen
	 */
	def index = {
		// Check whether criteria have been given before
		def criteria = [];
		if( params.criteria ) {
			criteria = parseCriteria( params.criteria, false )
		}
		[entitiesToSearchFor: entitiesToSearchFor, searchableFields: getSearchableFields(), criteria: criteria]
	}

	/**
	 * Searches for studies or samples based on the user parameters.
	 * 
	 * @param	entity		The entity to search for ( 'Study' or 'Sample' )
	 * @param	criteria	HashMap with the values being hashmaps with field, operator and value.
	 * 						[ 0: [ field: 'Study.name', operator: 'equals', value: 'term' ], 1: [..], .. ]
	 */
	def search = {
		if( !params.criteria ) {
			flash.error = "No criteria given to search for. Please try again.";
			redirect( action: 'index' )
		}

		if( !params.entity || !entitiesToSearchFor*.key.contains( params.entity ) ) {
			flash.error = "No or incorrect entity given to search for. Please try again.";
			redirect( action: 'index', params: [ criteria: parseCriteria( params.criteria ) ] )
		}

		// Create a search object and let it do the searching
		Search search;
		String view = determineView( params.entity );
		switch( params.entity ) {
			case "Study":	search = new StudySearch();	break;
			case "Sample":	search = new SampleSearch(); break;

			// This exception will only be thrown if the entitiesToSearchFor contains more entities than
			// mentioned in this switch structure.
			default:		throw new Exception( "Can't search for entities of type " + params.entity );
		}
		search.execute( parseCriteria( params.criteria ) );

		// Save search in session
		def queryId = saveSearch( search );
		render( view: view, model: [search: search, queryId: queryId] );
	}

	/**
	 * Removes a specified search from session
	 * @param 	id	queryId of the search to discard
	 */
	def discard = {
		Integer queryId
		try {
			queryId = params.id as Integer
		} catch( Exception e ) {
			flash.error = "Incorrect search ID given to discard"
			redirect( action: "index" );
			return
		}

		discardSearch( queryId );
		flash.message = "Search has been discarded"
		redirect( action: "list" );
	}

	/**
	 * Shows a specified search from session
	 * @param 	id	queryId of the search to show
	 */
	def show = {
		Integer queryId
		try {
			queryId = params.id as Integer
		} catch( Exception e ) {
			flash.error = "Incorrect search ID given to show"
			redirect( action: "index" );
			return
		}

		// Retrieve the search from session
		Search s = retrieveSearch( queryId );
		if( !s ) {
			flash.message = "Specified search could not be found"
			redirect( action: "index" );
			return;
		}

		// Determine which view to show
		def view = determineView( s.entity );
		render( view: view, model: [search: s, queryId: queryId] );
	}

	/**
	 * Shows a list of searches that have been saved in session
	 * @param 	id	queryId of the search to show
	 */
	def list = {
		def searches = listSearches();

		if( !searches || searches.size() == 0 ) {
			flash.message = "No previous searches found";
			redirect( action: "index" );
			return;
		}
		[searches: searches]
	}
	
	/**
	 * Shows a search screen where the user can search within the results of another search
	 * @param	id	queryId of the search to search in
	 */
	def searchIn = {
		Integer queryId
		try {
			queryId = params.id as Integer
		} catch( Exception e ) {
			flash.error = "Incorrect search ID given to show"
			redirect( action: "index" );
			return
		}

		// Retrieve the search from session
		Search s = retrieveSearch( queryId );
		if( !s ) {
			flash.message = "Specified search could not be found"
			redirect( action: "index" );
			return;
		}

		redirect( action: "index", params: [ "criteria.0.entityfield": s.entity, "criteria.0.operator": "in", "criteria.0.value": queryId ])
	}

	protected String determineView( String entity ) {
		switch( entity ) {
			case "Study":	return "studyresults"; 	break;
			case "Sample":	return "sampleresults";	break;
			default:		return "results"; break;
		}
	}

	/**
	 * Returns a map of entities with the names of the fields the user can search on
	 * @return
	 */
	protected def getSearchableFields() {
		def fields = [:];

		// Retrieve all local search fields
		getEntities().each {
			def entity = getEntity( 'dbnp.studycapturing.' + it );

			if( entity ) {
				def domainFields = entity.giveDomainFields();
				def templateFields = TemplateField.findAllByEntity( entity )

				def fieldNames = ( domainFields + templateFields ).collect { it.name }.unique() + 'Template'

				fields[ it ] = fieldNames.sort { a, b -> a[0].toUpperCase() + a[1..-1] <=> b[0].toUpperCase() + b[1..-1] };
			}
		}

		// Loop through all modules and check which fields are searchable
		// Right now, we just combine the results for different entities
		AssayModule.list().each { module ->
			def callUrl = module.url + '/rest/getQueryableFields'
			try {
				def json = moduleCommunicationService.callModuleRestMethodJSON( module.url, callUrl );
				def moduleFields = [];
				entitiesToSearchFor.each { entity ->
					if( json[ entity.key ] ) {
						json[ entity.key ].each { field ->
							moduleFields << field.toString();
						}
					}
				}

				// Remove 'module' from module name
				def moduleName = module.name.replace( 'module', '' ).trim()

				fields[ moduleName ] = moduleFields.unique();
			} catch( Exception e ) {
				log.error( "Error while retrieving queryable fields from " + module.name + ": " + e.getMessage() )
			}
		}

		return fields;
	}

	/**
	 * Parses the criteria from the query form given by the user
	 * @param	c	Data from the input form and had a form like
	 * 
	 *	[
	 *		0: [entityfield:a.b, operator: b, value: c],
	 *		0.entityfield: a.b,
	 *		0.operator: b,
	 *		0.field: c
	 *		1: [entityfield:f.q, operator: e, value: d],
	 *		1.entityfield: f.q,
	 *		1.operator: e,
	 *		1.field: d
	 *	]
	 * @param parseSearchIds	Determines whether searches are returned instead of their ids
	 * @return					List with Criterion objects
	 */
	protected List parseCriteria( def formCriteria, def parseSearchIds = true ) {
		ArrayList list = [];
		flash.error = "";
		// Loop through all keys of c and remove the non-numeric ones
		for( c in formCriteria ) {
			if( c.key ==~ /[0-9]+/ ) {
				def formCriterion = c.value;
				
				Criterion criterion = new Criterion();

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
					println "Operator " + formCriterion.operator + " could not be parsed: " + e.getMessage();
					flash.error += "Criterion could not be used: operator " + formCriterion.operator + " is not valid.<br />\n";
					continue;
				}
				
				// Special case of the 'in' operator
				if( criterion.operator == Operator.insearch ) {
					Search s
					try {
						s = retrieveSearch( Integer.parseInt( formCriterion.value ) );
					} catch( Exception e ) {}
					
					if( !s ) {
						flash.error += "Can't search within previous query: query not found";
						continue;
					}
					
					if( parseSearchIds ) {
						criterion.value = s
					} else {
						criterion.value = s.id
					}
				} else {
					// Copy value
					criterion.value = formCriterion.value;
				}
				
				list << criterion;
			}
		}

		return list;
	}

	/**
	 * Returns all entities for which criteria can be entered
	 * @return
	 */
	protected def getEntities() {
		return [ 'Study', 'Subject', 'Sample', 'Event', 'SamplingEvent', 'Assay' ]
	}

	/**
	 * Creates an object of the given entity.
	 *
	 * @return False if the entity is not a subclass of TemplateEntity
	 */
	protected def getEntity( entityName ) {
		// Find the templates
		def entity
		try {
			entity = Class.forName(entityName, true, this.getClass().getClassLoader())

			// succes, is entity an instance of TemplateEntity?
			if (entity.superclass =~ /TemplateEntity$/ || entity.superclass.superclass =~ /TemplateEntity$/) {
				return entity;
			} else {
				return false;
			}
		} catch( ClassNotFoundException e ) {
			log.error "Class " + entityName + " not found: " + e.getMessage()
			return null;
		}

	}


	/***************************************************************************
	 * 
	 * Methods for saving results in session
	 * 
	 ***************************************************************************/

	/**
	 * Saves the given search in session. Any search with the same criteria will be overwritten
	 *  
	 * @param s		Search to save
	 * @return		Id of the search for later reference
	 */
	protected int saveSearch( Search s ) {
		if( !session.queries )
			session.queries = [:]

		// First check whether a search with the same criteria is already present
		def previousSearch = retrieveSearchByCriteria( s.getCriteria() );
		
		def id
		if( previousSearch ) {
			id = previousSearch.id;
		} else {
			// Determine unique id
			id = ( session.queries*.key.max() ?: 0 ) + 1;
		}
		
		s.id = id;
		session.queries[ id ] = s;

		println "On saveSearch: " + session.queries;
		return id;
	}

	/**
	 * Retrieves a search from session with the same criteria as given
	 * @param criteria	List of criteria to search for
	 * @return			Search that has this criteria, or null if no such search is found.
	 */
	protected Search retrieveSearchByCriteria( List criteria ) {
		if( !session.queries )
			return null
		
		if( !criteria )
			return null
			
		for( query in session.queries ) {
			def key = query.key;
			def value = query.value;

			if( value.criteria && value.criteria.containsAll( criteria ) && criteria.containsAll( value.criteria ) ) {
				return value;
			}
		}

		return null;
	}


	/**
	 * Retrieves a search from session
	 * @param id	Id of the search
	 * @return		Search that belongs to this ID or null if no search is found
	 */
	protected Search retrieveSearch( int id ) {
		if( !session.queries || !session.queries[ id ] )
			return null

		if( !( session.queries[ id ] instanceof Search ) )
			return null;

		println "On retrieveSearch: " + session.queries;
		return (Search) session.queries[ id ]
	}

	/**
	 * Removes a search from session
	 * @param id	Id of the search
	 * @return	Search that belonged to this ID or null if no search is found
	 */
	protected Search discardSearch( int id ) {
		if( !session.queries || !session.queries[ id ] )
			return null

		def sessionSearch = session.queries[ id ];

		session.queries.remove( id );

		println "On discardSearch: " + session.queries;
		if( !( sessionSearch instanceof Search ) )
			return null;

		return (Search) sessionSearch
	}

	/**
	 * Retrieves a list of searches from session
	 * @return	List of searches from session
	 */
	protected List listSearches() {
		if( !session.queries )
			return []

		return session.queries*.value.toList()
	}
}
