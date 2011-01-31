package dbnp.query

import java.text.SimpleDateFormat
import org.dbnp.gdt.*

/**
 * Available operators for criteria
 * @author robert
 *
 */
enum Operator {
	equals, contains, gte, gt, lte, lt
}

/**
 * Represents a criterion to search on
 * @author robert
 *
 */
class Criterion {
	public String entity
	public String field
	public Operator operator
	public def value

	/**
	 * Retrieves the correct value for this criterion in the given object (with template)
	 *
	 * @param entity		Entity to check for value. Should be a child of template entity
	 * @param criterion	Criterion to match on
	 * @return			Value of the given field or null if the field doesn't exist
	 */
	public def getFieldValue( TemplateEntity entity ) {
		if( entity == null )
			return null;

		try {
			def fieldValue
			if( field == "Template" ) {
				fieldValue = entity.template?.name
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
	public boolean matchEntity( TemplateEntity entity ) {
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

		def classname = fieldValue.class.getName();
		classname = classname[classname.lastIndexOf( '.' ) + 1..-1].toLowerCase();

		println "Match " + fieldValue + " of class " + classname + " with " + this

		try {
			switch( classname ) {
				case "integer":					return longCompare( new Long( fieldValue.longValue() ) );
				case "long":					return longCompare( fieldValue );
				case "float":					return doubleCompare( new Long( fieldValue.doubleValue() ) );
				case "double":					return doubleCompare( fieldValue );
				case "boolean":					return booleanCompare( fieldValue );
				case "date":					return dateCompare( fieldValue);
				case "reltime":					return relTimeCompare( fieldValue );
				case "assaymodule":
				case "template":
				case "term":
				case "templatefieldlistitem":
				case "string":
				default:						return compareValues( fieldValue.toString().trim().toLowerCase(), this.operator, value.toString().toLowerCase().trim() );
			}
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
				return fieldValue.contains( criterionValue );
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
				log.error e2.class.getName() + ": " + e2.getMessage();
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
			log.error e.class.getName() + ": " + e.getMessage();
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
			Boolean booleanCriterion = Boolean.parseBoolean( value );
			return compareValues( fieldValue, this.operator, booleanCriterion );
		} catch( Exception e ) {
			log.error e.class.getName() + ": " + e.getMessage();
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
			log.error e.class.getName() + ": " + e.getMessage();
			return false;
		}
	}

	public String toString() {
		return "[Criterion " + entity + "." + field + " " + operator + " " + value + "]";
	}
}
