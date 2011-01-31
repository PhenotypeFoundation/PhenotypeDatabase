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

import java.text.SimpleDateFormat
import org.dbnp.gdt.*

class Search {
	public String entity;

	protected List criteria;
	protected List results;

	public List getCriteria() { return criteria; }
	public void setCriteria( List c ) { criteria = c; }

	public List getResults() { return results; }
	public void setResults( List r ) { results = r; }

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
	 * Executes a search based on the given criteria. Should be filled in by
	 * subclasses searching for a specific entity
	 */
	public void execute() {}

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
			return entities;
		}

		return entities.findAll { entity ->
			for( criterion in criteria ) {
				if( !check( entity, criterion ) ) {
					return false;
				}
			}
			return true;
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
					println e.getMessage();
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
}
