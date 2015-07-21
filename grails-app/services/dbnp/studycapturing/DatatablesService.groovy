package dbnp.studycapturing

import grails.converters.JSON

import org.dbnp.gdt.*
import org.hibernate.ObjectNotFoundException

class DatatablesService {
	
	/**
	 * Returns a proper list of data to generate a datatable with templated entities.
	 * @param params	Parameters from the request. Should be a map with at least:
			int			iDisplayStart	Display start point in the current data set.
			int			iDisplayLength	Number of records that the table can display in the current draw. It is expected that the number of records returned will be equal to this number, unless the server has fewer records to return.
			
			string		sSearch			Global search field
			int			iSortingCols	Number of columns to sort on
			
			int			iSortCol_(int)	Column being sorted on (you will need to decode this number for your database)
			string		sSortDir_(int)	Direction to be sorted - "desc" or "asc".

			string		sEcho			Information for DataTables to use for rendering.
	 * @return	Map		With all parameters, as a input-independent map
			int			offset			Display start point in the current data set.
			int			max				Number of records that the table can display in the current draw. It is expected that the number of records returned will be equal to this number, unless the server has fewer records to return.
			
			string		search			Global search field
			
			int			sortColumn		Column being sorted on (you will need to decode this number for your database)
			string		sortDirection	Direction to be sorted - "desc" or "asc".
	 */
	def parseParams( params ) { 
		def returnMap = [:]
		
		returnMap.offset = params.int( "iDisplayStart" ) ?: 0
		returnMap.max = params.int( "iDisplayLength" ) ?: -1
		returnMap.search = params.sSearch ?: ""
		returnMap.sortColumn = params.int( "iSortCol_0" ) != null ? params.int( "iSortCol_0" )  : null
		returnMap.sortDirection = params.sSortDir_0 ?: "asc"
		
		returnMap
	}
	
	/**
	 *	Returns a map with data that could be sent to a serverside datatable.
	 * 	@param entitiesMap	
	  		List		entities		List with all entities
	 		int			total			Total number of records in the whole dataset (without taking search, offset and max into account)
	 		int			totalFiltered	Total number of records in the search (without taking offset and max into account)
	 		int			ids				Total list of filtered ids
	 * @param params	Request parameters for this request
	 * @return Map
			{
				"sEcho": 3,
				"iTotalRecords": 57,
				"iTotalDisplayRecords": 57,
				"aaData": [
					[
						"Gecko",
						"Firefox 1.0",
						"Win 98+ / OSX.2+",
						"1.7",
						"A"
					],
					[
						"Gecko",
						"Firefox 1.5",
						"Win 98+ / OSX.2+",
						"1.8",
						"A"
					],
					...
				],
				"aIds": [ ]
			}
	 * 
	 */
	def createDatatablesOutputForEntities( entitiesMap, params ) {
		createDatatablesOutput(entitiesMap, params, defaultEntityFormatter)
	}
    
        /**
         * Default closure to format an entity for usage in the datatable
         * This closure returns an array with the first element being the id, and 
         * after that, all fields for this element are returned
         */
        public Closure getDefaultEntityFormatter() {
             return { entity ->
                def data = [
                        entity.id
                ]
                
                entity.giveFields().each { field ->
                        def value = entity.getFieldValue( field.name )
                        
                        try {
                                if( field.type == TemplateFieldType.DATE ) {
                                        // transform date instance to formatted string (dd/mm/yyyy)
                                        data << ( value ? String.format('%tY-%<tm-%<td', value) : "" )
                                } else if ( field.type == TemplateFieldType.RELTIME ) {
                                        data << ( value == null ? "" : new RelTime( value ).toString())
                                } else {
                                        data << ( value == null ? "" : value.toString() )
                                }
                        } catch( ObjectNotFoundException e ) {
                                // An ObjectNotFoundException occurs if the field references an object that doesn't
                                // exist anymore. For example, if a template-field references a template, that has
                                // been deleted.
                                data << ""
                        }
                }
                
                data
             }
        } 
	
	/**
	 *	Returns a map with data that could be sent to a serverside datatable.
	 * 	@param entitiesMap
			  List		entities		List with all entities
			 int			total			Total number of records in the whole dataset (without taking search, offset and max into account)
			 int			totalFiltered	Total number of records in the search (without taking offset and max into account)
			 int			ids				Total list of filtered ids
	 * @param params	Request parameters for this request
	 * @return Map
			{
				"sEcho": 3,
				"iTotalRecords": 57,
				"iTotalDisplayRecords": 57,
				"aaData": [
					[
						"Gecko",
						"Firefox 1.0",
						"Win 98+ / OSX.2+",
						"1.7",
						"A"
					],
					[
						"Gecko",
						"Firefox 1.5",
						"Win 98+ / OSX.2+",
						"1.8",
						"A"
					],
					...
				],
				"aIds": [ ]
			}
	 *
	 */
	def createDatatablesOutput( entitiesMap, params, converter = null) {
		def output = [
			sEcho: params.sEcho,
			iTotalRecords: entitiesMap.total,
			iTotalDisplayRecords: entitiesMap.totalFiltered,
			aIds: entitiesMap.ids
		]
		
		if( !converter )
			converter = { data -> data }
		
		
		output.aaData = entitiesMap.entities.collect converter
		
		output
	}
}
