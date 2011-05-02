package dbnp.query

import java.text.SimpleDateFormat
import org.dbnp.gdt.*
import org.apache.commons.logging.LogFactory;

/**
 * Available operators for criteria
 * @author robert
 *
 */
enum Operator {
	equals( "=" ), contains( "contains" ), gte( ">="), gt( ">" ), lte( "<=" ), lt( "<" ), insearch( "in" )
	Operator(String name) { this.name = name }
	private final String name;
	public String toString() { return name }
}

/**
 * Represents a criterion to search on
 * @author robert
 *
 */
class Criterion {
	private static final log = LogFactory.getLog(this);
	public String entity
	public String field
	public Operator operator
	public def value

	/**
	 * Retrieves a combination of the entity and field
	 * @return
	 */
	public String entityField() {
		return entity.toString() + ( field ? "." + field.toString() : "" );
	}
	
	/**
	 * Retrieves a human readable description of the combination of the entity and field
	 * @return
	 */
	public String humanReadableEntityField() {
		if( field == '*' ) {
			if( entity == '*' ) {
				return "any field in any object"
			} else {
				return "any field in " + entity.toString();
			}
		} else {
			return entityField();
		}
	}
	
	/**
	 * Retrieves the correct value for this criterion in the given object (with template)
	 *
	 * @param entity		Entity to check for value. Should be a child of template entity
	 * @param criterion		Criterion to match on
	 * @return				Value of the given field or null if the field doesn't exist
	 */
	public def getFieldValue( TemplateEntity entity ) {
		if( entity == null )
			return null;

		try {
			def fieldValue
			if( !field ) {
				fieldValue = entity
			} else if( field == "Template" ) {
				fieldValue = entity.template?.name
			} else if( field == "*" ) {
				fieldValue = entity.giveFields().collect{ 
					if( it && it.name ) {
						Search.prepare( entity.getFieldValue( it.name ), entity.giveFieldType( it.name ) ) 
					}
				}
			} else {
				fieldValue = Search.prepare( entity.getFieldValue( field ), entity.giveFieldType( field ) )
			}
			
			return fieldValue
		} catch( Exception e ) {
			// An exception occurs if the given field doesn't exist. In that case, this criterion will fail.
			// TODO: Maybe give the user a choice whether he want's to include these studies or not
			return null;
		}
	}

	/**
	 * Checks if the given object (with template) that satisfies the given criterion.
	 *
	 * @param entity		Entity to check for criterion satisfaction. Should be a child of template entity
	 * @param criterion	Criterion to match on
	 * @return			True iff there the entity satisfies the given criterion.
	 */
	public boolean matchOneEntity( TemplateEntity entity ) {
		def fieldValue = this.getFieldValue( entity );

		// Null is returned, the given field doesn't exist. In that case, this criterion will fail.
		// TODO: Maybe give the user a choice whether he want's to include these studies or not
		if( fieldValue == null )
			return false;

		return this.match( fieldValue );
	}

	/**
	 * Checks for all entities in the given entityList, if there is any object that satisfies the given criterion.
	 *
	 * @param entityList	List with entities. The entities should be child classes of TemplateEntity
	 * @param criterion		Criterion to match on
	 * @return				True iff there is any entity in the list that satisfies the given criterion.
	 */
	public boolean matchAnyEntity( List<TemplateEntity> entityList ) {
		for( entity in entityList ) {
			if( matchOneEntity( entity ) )
				return true;
		}
		return false;
	}

	/**
	 * Checks for all entities in the given entityList, if all objects satisfy the given criterion.
	 *
	 * @param entityList	List with entities. The entities should be child classes of TemplateEntity
	 * @param criterion		Criterion to match on
	 * @return				True iff all entities satisfy the given criterion.
	 */
	public boolean matchAllEntities( List<TemplateEntity> entityList ) {
		for( entity in entityList ) {
			if( !matchOneEntity( entity ) )
				return false;
		}
		return true;
	}

	/**
	 * Checks for all values in the given List, if there is any value that satisfies the given criterion.
	 *
	 * @param entityList		List with values.
	 * @param criterion		Criterion to match on
	 * @return				True iff there is any value in the list that satisfies the given criterion.
	 */
	public boolean matchAny( List valueList ) {
		for( value in valueList ) {
			if( match( value ) )
				return true;
		}
		return false;
	}

	/**
	 * Checks for all values in the given List, if all values satisfy the given criterion.
	 *
	 * @param entityList		List with values.
	 * @param criterion		Criterion to match on
	 * @return				True iff all values satisfy the given criterion.
	 */
	public boolean matchAll( List entityList ) {
		for( value in valueList ) {
			if( !match( value ) )
				return false;
		}
		return true;
	}

	/**
	 * Tries to match a value against a criterion and returns true if it matches
	 *
	 * @param value		Value of the field to match
	 * @return			True iff the value matches this criterion, false otherwise
	 */
	public boolean match( def fieldValue ) {
		if( fieldValue == null )
			return false;
		
		// in-search criteria have to be handled separately
		if( this.operator == Operator.insearch ) {
			return this.value?.getResults()?.contains( fieldValue );
		}	
		
		// Other criteria are handled based on the class of the value given.
		def classname = fieldValue.class.getName();
		classname = classname[classname.lastIndexOf( '.' ) + 1..-1].toLowerCase();

		def matches = false;
		try {
			switch( classname ) {
				case "integer":					matches = longCompare( new Long( fieldValue.longValue() ) ); break;
				case "long":					matches = longCompare( fieldValue ); break;
				case "float":					matches = doubleCompare( new Long( fieldValue.doubleValue() ) ); break;
				case "double":					matches = doubleCompare( fieldValue ); break;
				case "boolean":					matches = booleanCompare( fieldValue ); break;
				case "date":					matches = dateCompare( fieldValue); break;
				case "reltime":					matches = relTimeCompare( fieldValue ); break;
				case "assaymodule":
				case "template":
				case "term":
				case "templatefieldlistitem":
				case "string":
				default:						matches = compareValues( fieldValue.toString().trim().toLowerCase(), this.operator, value.toString().toLowerCase().trim() ); break;
			}
			
			return matches;
		} catch( Exception e ) {
			log.error e.class.getName() + ": " + e.getMessage();
			return false;
		}
	}

	/**
	 * Tries to match a value against a criterion and returns true if it matches
	 *
	 * @param fieldValue		Value of the field to match
	 * @param operator			Operator to apply 
	 * @param criterionValue	Value of the criterion
	 * @return					True iff the value matches this criterion value, false otherwise
	 */
	protected boolean compareValues( def fieldValue, Operator operator, def criterionValue ) {
		switch( operator ) {
			case Operator.gte:
				return fieldValue >= criterionValue;
			case Operator.gt:
				return fieldValue > criterionValue;
			case Operator.lt:
				return fieldValue < criterionValue;
			case Operator.lte:
				return fieldValue <= criterionValue;
			case Operator.contains:
				// Contains operator can only be used on string values
				return fieldValue.toString().contains( criterionValue.toString() );
			case Operator.equals:
			default:
				return fieldValue.equals( criterionValue );
		}

	}

	/**
	 * Tries to match a date value against a criterion and returns true if it matches
	 *
	 * @param value		Date value of the field to match
	 * @return			True iff the value matches this criterion, false otherwise
	 */
	protected boolean dateCompare( Date fieldValue ) {
		try {
			Date dateCriterion = new SimpleDateFormat( "yyyy-MM-dd" ).parse( value );
			Date fieldDate = new Date( fieldValue.getTime() );

			// Clear time in order to just compare dates
			dateCriterion.clearTime();
			fieldDate.clearTime();

			return compareValues( fieldDate, this.operator, dateCriterion )
		} catch( Exception e ) {
			log.error e.class.getName() + ": " + e.getMessage();
			return false;
		}
	}

	/**
	 * Tries to match a long value against a criterion and returns true if it matches
	 *
	 * @param value		Long value of the field to match
	 * @param criterion	Criterion to match on. Should be a map with entries 'operator' and 'value'
	 * @return			True iff the value matches this criterion, false otherwise
	 */
	protected boolean longCompare( Long fieldValue ) {
		Long longCriterion;
		try {
			longCriterion = Long.parseLong( value );
		} catch( Exception e ) {
			try {
				// If converting to long doesn't work, try converting to double and rounding it
				Double doubleCriterion = Double.parseDouble(value);
				longCriterion = new Long( doubleCriterion.longValue() );
			} catch( Exception e2 ) {
				log.debug "Can't convert value to long for comparison: " + e2.class.getName() + ": " + e2.getMessage();
				return false;
			}
		}
		return compareValues( fieldValue, this.operator, longCriterion );
	}

	/**
	 * Tries to match a double value against a criterion and returns true if it matches
	 *
	 * @param value		Double value of the field to match
	 * @return			True iff the value matches this criterion, false otherwise
	 */
	protected boolean doubleCompare( Double fieldValue ) {
		try {
			Double doubleCriterion = Double.parseDouble( value );
			return compareValues( fieldValue, this.operator, doubleCriterion );
		} catch( Exception e ) {
			log.debug "Can't convert value to double for comparison: " + e.class.getName() + ": " + e.getMessage();
			return false;
		}
	}


	/**
	 * Tries to match a boolean value against a criterion and returns true if it matches
	 *
	 * @param value		Boolean value of the field to match
	 * @return			True iff the value matches this criterion, false otherwise
	 */
	protected boolean booleanCompare( Boolean fieldValue ) {
		try {
			// The comparison should only be performed iff the value
			// contains 'true' or 'false' (case insensitive)
			def lowerCaseValue = value.toString().toLowerCase();
			if( lowerCaseValue != 'true' && lowerCaseValue != 'false' )
				return false;
				
			Boolean booleanCriterion = Boolean.parseBoolean( value );
			return compareValues( fieldValue, this.operator, booleanCriterion );
		} catch( Exception e ) {
			log.debug "Can't convert value to boolean for comparison: " + e.class.getName() + ": " + e.getMessage();
			return false;
		}
	}

	/**
	 * Tries to match a relTime value against a criterion and returns true if it matches
	 *
	 * @param value		relTime value of the field to match
	 * @return			True iff the value matches this criterion, false otherwise
	 */
	protected boolean relTimeCompare( RelTime fieldValue ) {
		try {
			RelTime rt

			// Numbers are taken to be seconds, if a non-numeric value is given, try to parse it
			if( value.toString().isLong() ) {
				rt = new RelTime( Long.parseLong( value.toString() ) );
			} else {
				rt = new RelTime( value.toString() );
			}

			return compareValues( fieldValue, this.operator, rt );
		} catch( Exception e ) {
			log.debug "Can't convert value to reltime for comparison: " + e.class.getName() + ": " + e.getMessage();
			return false;
		}
	}
	
	public static Operator parseOperator( String name ) throws Exception {
		switch( name.trim() ) {
			case "=":  
			case "equals":		return Operator.equals; 
			case "contains": 	return Operator.contains; 
			case ">=": 
			case "gte":			return Operator.gte; 
			case ">": 
			case "gt":			return Operator.gt; 
			case "<=": 
			case "lte":			return Operator.lte; 
			case "<": 
			case "lt":			return Operator.lt; 
			case "in": 			return Operator.insearch;
			default:
				throw new Exception( "Operator not found" ); 
		}
	}

	public String toString() {
		return "[Criterion " + entityField() + " " + operator + " " + value + "]";
	}
	
	public boolean equals( Object o ) {
		if( o == null )
			return false;
		
		if( !( o instanceof Criterion ) ) 
			return false;
			
		Criterion otherCriterion = (Criterion) o;
		return 	this.entity == otherCriterion.entity &&
				this.field == otherCriterion.field && 
				this.operator == otherCriterion.operator &&
				this.value == otherCriterion.value;
	}
}
