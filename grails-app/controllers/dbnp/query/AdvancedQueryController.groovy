package dbnp.query

import dbnp.modules.*
import org.dbnp.gdt.*

// TODO: Make use of the searchable-plugin or Lucene possibilities instead of querying the database directly

/**
 * Basic web interface for searching within studies
 *
 * @author Robert Horlings (robert@isdat.nl)
 */
class AdvancedQueryController {
	def moduleCommunicationService;
	def authenticationService

	def entitiesToSearchFor = [ 'Study': 'Studies', 'Sample': 'Samples', 'Assay': 'Assays']

	/**
	 * Shows search screen
	 */
	def index = {
		// Check whether criteria have been given before
		def criteria = [];
		if( params.criteria ) {
			criteria = parseCriteria( params.criteria, false )
		}
		[searchModes: SearchMode.values(), entitiesToSearchFor: entitiesToSearchFor, searchableFields: getSearchableFields(), criteria: criteria]
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
		Search search = determineSearch( params.entity );
		String view = determineView( params.entity );

		// Choose between AND and OR search. Default is given by the Search class itself.
		switch( params.operator?.toString()?.toLowerCase() ) {
			case "or":
				search.searchMode = SearchMode.or;
				break;
			case "and":
				search.searchMode = SearchMode.and;
				break;
		}

		search.execute( parseCriteria( params.criteria ) );

		// Save search in session
		def queryId = saveSearch( search );
		render( view: view, model: [search: search, queryId: queryId, actions: determineActions(search)] );
	}

	/**
	 * Removes a specified search from session
	 * @param 	id	queryId of the search to discard
	 */
	def discard = {
		def queryIds = params.list( 'id' );
		queryIds = queryIds.findAll { it.isInteger() }.collect { Integer.valueOf( it ) }

		if( queryIds.size() == 0 ) {
			flash.error = "Incorrect search ID given to discard"
			redirect( action: "index" );
			return
		}

		queryIds.each { queryId ->
			discardSearch( queryId );
		}

		if( queryIds.size() > 1 ) {
			flash.message = "Searches have been discarded"
		} else {
			flash.message = "Search has been discarded"
		}
		redirect( action: "list" );
	}

	/**
	 * Shows a specified search from session
	 * @param 	id	queryId of the search to show
	 */
	def show = {
		def queryId = params.int( 'id' );

		if( !queryId ) {
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
		
		// Attach all objects to the current hibernate thread, because the
		// object might be attached to an old thread, since the results are
		// saved in session
		s.getResults().each {
			it.attach();
		}

		// Determine which view to show
		def view = determineView( s.entity );
		render( view: view, model: [search: s, queryId: queryId, actions: determineActions(s)] );
	}

	/**
	 * Performs an action on specific searchResults 
	 * @param 	queryId		queryId of the search to show
	 * @param	id			list with the ids of the results to perform the action on
	 * @param	actionName	Name of the action to perform
	 */
	def performAction = {
		def queryId = params.int( 'queryId' );
		def selectedIds = params.list( 'id' ).findAll { it.isLong() }.collect { Long.parseLong(it) }
		def actionName = params.actionName;
		def moduleName = params.moduleName;

		if( !queryId ) {
			flash.error = "Incorrect search ID given to show"
			redirect( action: "index" );
			return
		}
		
		// Retrieve the search from session
		Search s = retrieveSearch( queryId );
		if( !s ) {
			flash.message = "Specified search could not be found"
			redirect( action: "list" );
			return;
		}

		// Determine the possible actions and build correct urls
		def actions = determineActions(s, selectedIds );

		// Find the right action to perform
		def redirectUrl;
		for( action in actions ) {
			if( action.module == moduleName && action.name == actionName ) {
				redirectUrl = action.url;
				break;
			}
		}
		
		if( !redirectUrl ) {
			flash.error = "No valid action is given to perform";
			redirect( action: "show", id: queryId );
			return;
		}
		
		redirect( url: redirectUrl );
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
		def queryIds = params.list( 'id' );
		queryIds = queryIds.findAll { it.isInteger() }.collect { Integer.valueOf( it ) }

		if( queryIds.size() == 0 ) {
			flash.error = "Incorrect search ID given to search in"
			redirect( action: "list" );
			return
		}

		// Retrieve the searches from session
		def params = [:]
		queryIds.eachWithIndex { queryId, idx ->
			Search s = retrieveSearch( queryId );
			if( !s ) {
				flash.message = "Specified search " + queryId + " could not be found"
				return;
			} else {
				params[ "criteria." + idx + ".entityfield" ] = s.entity;
				params[ "criteria." + idx + ".operator" ] = "in";
				params[ "criteria." + idx + ".value" ] = queryId;
			}
		}

		redirect( action: "index", params: params)
	}

	/**
	 * Combines the results of multiple searches 
	 * @param	id	queryIds of the searches to combine 
	 */
	def combine = {
		def queryIds = params.list( 'id' );
		queryIds = queryIds.findAll { it.isInteger() }.collect { Integer.valueOf( it ) }

		if( queryIds.size() == 0 ) {
			flash.error = "Incorrect search ID given to combine"
			redirect( action: "index" );
			return
		}

		// First determine whether the types match
		def searches = [];
		def type = "";
		flash.error = "";
		queryIds.eachWithIndex { queryId, idx ->
			Search s = retrieveSearch( queryId );
			if( !s ) {
				return;
			}

			if( type ) {
				if( type != s.entity ) {
					flash.error = type + " and " + s.entity.toLowerCase() + " queries can't be combined. Selected queries of one type.";
					return
				}
			} else {
				type = s.entity
			}
		}

		if( flash.error ) {
			redirect( action: "list" );
			return;
		}

		if( !type ) {
			flash.error = "No correct query ids were given."
			redirect( action: "list" );
			return;
		}

		// Retrieve the searches from session
		Search combined = determineSearch( type );
		combined.searchMode = SearchMode.or;

		queryIds.eachWithIndex { queryId, idx ->
			Search s = retrieveSearch( queryId );
			if( s ) {
				combined.addCriterion( new Criterion( entity: type, field: null, operator: Operator.insearch, value: s ) );
			}
		}

		// Execute search to combine the results
		combined.execute();

		def queryId = saveSearch( combined );
		redirect( action: "show", id: queryId );
	}

	protected String determineView( String entity ) {
		switch( entity ) {
			case "Study":	return "studyresults"; 	break;
			case "Sample":	return "sampleresults";	break;
			case "Assay":	return "assayresults";	break;
			default:		return "results"; break;
		}
	}

	/**
	 * Returns the search object used for searching
	 */
	protected Search determineSearch( String entity ) {
		switch( entity ) {
			case "Study":	return new StudySearch();
			case "Sample":	return new SampleSearch();
			case "Assay":	return new AssaySearch();
			
			// This exception will only be thrown if the entitiesToSearchFor contains more entities than
			// mentioned in this switch structure.
			default:		throw new Exception( "Can't search for entities of type " + entity );
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
				def json = moduleCommunicationService.callModuleMethod( module.url, callUrl );
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
			if( c.key ==~ /[0-9]+/ && c.value.entityfield ) {
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
		def previousSearch = retrieveSearch( s );

		def id
		if( previousSearch ) {
			id = previousSearch.id;
		} else {
			// Determine unique id
			id = ( session.queries*.key.max() ?: 0 ) + 1;
		}

		s.id = id;
		session.queries[ id ] = s;

		return id;
	}

	/**
	 * Retrieves a search from session with the same criteria as given
	 * @param s			Search that is used as an example to search for
	 * @return			Search that has this criteria, or null if no such search is found.
	 */
	protected Search retrieveSearch( Search s ) {
		if( !session.queries )
			return null

		for( query in session.queries ) {
			def value = query.value;

			if( s.equals( value ) )
				return value
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

	/**
	 * Determine a list of actions that can be performed on specific entities
	 * @param entity		Name of the entity that the actions could be performed on
	 * @param selectedIds	List with ids of the selected items to perform an action on
	 * @return
	 */
	protected List determineActions( Search s, def selectedIds = null ) {
		return gscfActions( s, selectedIds ) + moduleActions( s, selectedIds );
	}

	/**
	 * Determine a list of actions that can be performed on specific entities by GSCF
	 * @param entity	Name of the entity that the actions could be performed on
	 * @param selectedIds	List with ids of the selected items to perform an action on
	 */
	protected List gscfActions(Search s, def selectedIds = null) {
		switch(s.entity) {
			case "Study":
				def exportParams = [:]
				s.filterResults(selectedIds).each {
					exportParams[ it.code ] = it.id;
				}
				return [[
						module: "gscf",
						name:"simpletox",
						description: "Export as SimpleTox",
						url: createLink( controller: "exporter", action: "export", params: exportParams )
					]]
			case "Sample":
				return []
			default:
				return [];
		}
	}

	/**
	 * Determine a list of actions that can be performed on specific entities by other modules
	 * @param entity	Name of the entity that the actions could be performed on
	 */
	protected List moduleActions(Search s, def selectedIds = null) {
		def actions = []

		if( !s.getResults() || s.getResults().size() == 0 )
			return []

		// Loop through all modules and check which actions can be performed on the
		AssayModule.list().each { module ->
			// Remove 'module' from module name
			def moduleName = module.name.replace( 'module', '' ).trim()
			try {
				def callUrl = module.url + "/rest/getPossibleActions?entity=" + s.entity
				def json = moduleCommunicationService.callModuleRestMethodJSON( module.url, callUrl );

				// Check whether the entity is present in the return value
				if( json[ s.entity ] ) {
					json[ s.entity ].each { action ->
						def url = action.url ?: module.url + "/action/" + action.name
						
						if( url.find( /\?/ ) )
							url += "&"
						else
							url += "?"
						
						url += "entity=" + s.entity
						url += "&" + s.filterResults(selectedIds).collect { "tokens=" + it.giveUUID() }.join( "&" )
						actions << [
									module: moduleName,
									name: action.name,
									description: action.description + " (" + moduleName + ")",
									url: url
								];
					}
				}
			} catch( Exception e ) {
				// Exception is thrown when the call to the module fails. No problems though.
				//log.error "Error while fetching possible actions from " + module.name + ": " + e.getMessage()
				e.printStackTrace()
				throw e
			}
		}

		return actions;
	}

}