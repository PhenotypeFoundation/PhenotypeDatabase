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

import java.util.List;

class Search {
	public String entity;

	protected List criteria;
	protected List results;
	protected String view = "results";

	public List getCriteria() { return criteria; }
	public void setCriteria( List c ) { criteria = c; }

	public List getResults() { return results; }
	public void setResults( List r ) { results = r; }

	public String getView() { return view; }
	public void setView( String v) { view = v; }

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
	 * Filters a list with entities, based on the given criteria and a closure to check whether a criterium is matched
	 * 
	 * @param entities	Original list with entities to check for these criteria
	 * @param criteria	List with criteria to match on
	 * @param check		Closure to see whether a specific entity matches a criterium. Gets two arguments:
	 * 						element		The element to check 
	 * 						criterium	The criterium to check on.
	 * 					Returns true if the criterium holds, false otherwise
	 * @return			The filtered list of entities
	 */
	protected List filterEntityList( List entities, List criteria, Closure check ) {
		if( !entities || !criteria || criteria.size() == 0 ) {
			return entities;
		}

		return entities.findAll { entity ->
			for( def criterium in criteria ) {
				println "Check " + entity + " for " + criterium
				if( !check( entity, criterium ) ) {
					return false;
				}
			}
			return true;
		}
	}

	/**
	 * Tries to match a value against a criterium and returns true if it matches
	 * 
	 * @param value		Value of the field to match
	 * @param criterium	Criterium to match on. Should be a map with entries 'operator' and 'value'
	 * @return			True iff the value matches this criterium, false otherwise
	 */
	protected boolean compare( def value, def criterium ) {
		switch( value.class.getName() ) {
			// TODO: Make the search capable of handle RelTime fields
			case "java.lang.Long":			return longCompare( value, criterium );
			case "java.lang.Double":		return doubleCompare( value, criterium );
			case "java.lang.Boolean":		return booleanCompare( value, criterium );
			case "java.lang.Date":			return dateCompare( value, criterium );
			case "AssayModule":
			case "Template":
			case "RelTime":
			case "Term":
			case "TemplateFieldListItem":
			default:						return stringCompare( value.toString(), criterium );
		}
	}

	/**
	 * Tries to match a string value against a criterium and returns true if it matches
	 *
	 * @param value		String value of the field to match
	 * @param criterium	Criterium to match on. Should be a map with entries 'operator' and 'value'
	 * @return			True iff the value matches this criterium, false otherwise
	 */
	protected boolean stringCompare( String value, def criterium ) {
		try {
			String stringCriterium = criterium.value.toString().trim()
			return value.trim().equals( stringCriterium );
		} catch( Exception e ) {
			return false;
		}
	}

	/**
	 * Tries to match a date value against a criterium and returns true if it matches
	 * 
	 * @param value		Date value of the field to match
	 * @param criterium	Criterium to match on. Should be a map with entries 'operator' and 'value'
	 * @return			True iff the value matches this criterium, false otherwise
	 */
	protected boolean dateCompare( Date value, def criterium ) {
		try {
			Date dateCriterium = DateFormat.parse( criterium.value );
			return value.equals( dateCriterium );
		} catch( Exception e ) {
			return false;
		}
	}

	/**
	 * Tries to match a long value against a criterium and returns true if it matches
	 *
	 * @param value		Long value of the field to match
	 * @param criterium	Criterium to match on. Should be a map with entries 'operator' and 'value'
	 * @return			True iff the value matches this criterium, false otherwise
	 */
	protected boolean longCompare( Long value, def criterium ) {
		try {
			Long longCriterium = Long.parseLong( criterium.value );
			return value.equals( longCriterium );
		} catch( Exception e ) {
			return false;
		}
	}

	/**
	 * Tries to match a double value against a criterium and returns true if it matches
	 *
	 * @param value		Double value of the field to match
	 * @param criterium	Criterium to match on. Should be a map with entries 'operator' and 'value'
	 * @return			True iff the value matches this criterium, false otherwise
	 */
	protected boolean doubleCompare( Double value, def criterium ) {
		try {
			Double doubleCriterium = Double.parseDouble( criterium.value );
			return value.equals( doubleCriterium );
		} catch( Exception e ) {
			return false;
		}
	}


	/**
	 * Tries to match a boolean value against a criterium and returns true if it matches
	 *
	 * @param value		Boolean value of the field to match
	 * @param criterium	Criterium to match on. Should be a map with entries 'operator' and 'value'
	 * @return			True iff the value matches this criterium, false otherwise
	 */
	protected boolean booleanCompare( Double value, def criterium ) {
		try {
			Boolean booleanCriterium = Boolean.parseBoolean( criterium.value );
			return value.equals( booleanCriterium );
		} catch( Exception e ) {
			return false;
		}
	}
}
