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
	 * Returns the class for the entity of this criterion
	 * @return	
	 */
	public Class entityClass() {
		if( this.entity == '*' )
			return null;
		
			
		try {
			return TemplateEntity.parseEntity( 'dbnp.studycapturing.' + this.entity)
		} catch( Exception e ) {
			throw new Exception( "Unknown entity for criterion " + this, e );
		}
	}
	
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
	 * Returns the type of criterion when searching. Multiple types can be returned, since fields
	 * with the same name might have different types.
	 *  
	 * @return	List of strings determining the type of this criterion. Possibilities are:
	 * 		[STRING,BOOLEAN,..]:The criterion references a template field that contains a 'simple' 
	 * 							value (boolean, double, long, string, reltime, date)
	 * 		[STRINGLIST,...]: 	The criterion references a template field that contains a 'complex'
	 * 							value (listitem, ontologyterm, template, module) referencing another
	 * 							database table
	 * 		Wildcard:			The criterion references all fields
	 */
	protected List<String> criterionType() {
		if( this.entity == '*' || this.field == '*' ) {
			return [
				 'String',
				 'Text',
				 'File',
				 'Date',
				 'RelTime',
				 'Double',
				 'Long',
				 'Boolean',
				 'StringList',
				 'ExtendableStringList',
				 'Term',
				 'Template',
				 'Module'
			]
		}

		// Template fields are string fields
		if( this.field == 'Template' ) 
			return [ "String" ]
		
		// Determine domain fields of the entity
		def domainFields = entityClass().giveDomainFields();
		def domainField = domainFields.find { it.name == this.field };
		if( domainField )
			return [domainField.type?.casedName];

		// If this field is not a domain field, search for the field in the database
		def entityClass = entityClass()

		if( !entityClass || !this.field )
			return null;

		// Find all fields with this name and entity
		def fields = TemplateField.findAllByName( this.field ).findAll { it.entity == entityClass };

		// If the field is not found, return null
		if( !fields )
			return null

		// Return the (unique) String value of the types
		return fields*.type.unique()*.casedName;
	}

	/**
	 * Determines whether the field in this criterion is a domain field
	 *
	 * @return	True iff the field is a domain field, false otherwise
	 */
	protected boolean isDomainCriterion() {
		def entityClass = entityClass()
		
		if( !entityClass )
			return false;
		
		// Template fields should be handled as domain criteria
		if( this.field == "Template" )
			return true;
			
		// Determine domain fields of the entity
		def domainFields = entityClass.giveDomainFields();
		def domainField = domainFields.find { it.name == this.field };

		return (domainField ? true : false)
	}
	
	/**
	 * Determines whether this criterion references a 'complex' field (i.e. a field that 
	 * contains a complex type like Term, ListItem etc.)
	 * 
	 * @return
	 */
	public boolean isComplexCriterion() {
		if( this.field == '*' )
			return false;

		if( isDomainCriterion() )
			return false;
			
		def types = criterionType();
		
		return types.any { type -> 
			switch( type ) {
				case 'StringList':
				case 'ExtendableStringList':
				case 'Term':
				case 'Template':
				case 'Module':
					return true;
			}
			
			return false;
		}
	}

	/**
	 * Case the field value to search on to the given type
	 * @param fieldType	Name of the template field type
	 * @return			Value casted to the right value
	 */
	protected def castValue( String fieldType ) {
		switch( fieldType ) {

			case 'String':
			case 'Text':
			case 'StringList':
			case 'ExtendableStringList':
			case 'Term':
			case 'Template':
			case 'Module':
				return value?.toString();
			case 'File':
				return null; // Never search in filenames, since they are not very descriptive
			case 'Date':
				// The comparison with date values should only be performed iff the value
				// contains a parsable date
				// and the operator is equals, gte, gt, lt or lte
				if( operator == Operator.insearch || operator == Operator.contains )
					return null

				try {
					Date dateCriterion = new SimpleDateFormat( "yyyy-MM-dd" ).parse( value );
					return dateCriterion
				} catch( Exception e ) {
					return null;
				}

			case 'RelTime':
				// The comparison with date values should only be performed iff the value
				// contains a long number
				// and the operator is equals, gte, gt, lt or lte
				if( operator == Operator.insearch || operator == Operator.contains )
					return null

				try {
					RelTime rt

					// Numbers are taken to be seconds, if a non-numeric value is given, try to parse it
					if( value.toString().isLong() ) {
						rt = new RelTime( Long.parseLong( value.toString() ) );
					} else {
						rt = new RelTime( value.toString() );
					}

					return rt.getValue()
				} catch( Exception e ) {
					return null;
				}
			case 'Double':
				// The comparison with date values should only be performed iff the value
				// contains a double number
				// and the operator is equals, gte, gt, lt or lte
				if( operator == Operator.insearch || operator == Operator.contains )
					return null

				if( value.isDouble() ) {
					return Double.parseDouble( value )
				} else {
					return null;
				}
			case 'Long':
				// The comparison with date values should only be performed iff the value
				// contains a long number
				// and the operator is equals, gte, gt, lt or lte
				if( operator == Operator.insearch || operator == Operator.contains )
					return null

				if( value.isLong() ) {
					return Long.parseLong( value )
				} else {
					return null;
				}
			case 'Boolean':
				// The comparison with boolean values should only be performed iff the value
				// contains 'true' or 'false' (case insensitive)
				// and the operator is equals
				if( operator != Operator.equals )
					return null

				def lowerCaseValue = value.toString().toLowerCase();
				if( lowerCaseValue == 'true' || lowerCaseValue == 'false' ) {
					return Boolean.parseBoolean( this.value )
				} else {
					return null
				}
		}
	}

	/**
	 * Create a HQL where clause from this criterion, in order to be used within a larger HQL statement
	 * 
	 * @param	objectToSearchIn	HQL name of the object to search in
	 * @return	Map with 3 keys:   'join' and'where' with the HQL join and where clause for this criterion and 'parameters' for the query named parameters
	 */
	public Map toHQL( String prefix, String objectToSearchIn = "object" ) {
		List whereClause = []
		String joinClause = "";
		Map parameters = [:];
		def emptyCriterion = [ "join": null, "where": null, "parameters": null ];

		// If this criterion is used to search within another search result, we use a special piece of HQL
		if( this.operator == Operator.insearch ) {
			if( this.value?.results ) {
				parameters[ prefix + "SearchResults" ] = this.value?.results

				return [ "join": "", "where": "( " + objectToSearchIn + " in (:" + prefix + "SearchResults) )" , "parameters": parameters ];
			} else {
				return emptyCriterion;
			}
		}
		
		// If no value is given, don't do anything
		if( !value )
			return emptyCriterion;
		
		// Check whether the field is a domain field
		if( isDomainCriterion() ) {
			// Determine the types of this criterion, but there will be only 1 for a domain field
			def criterionType = criterionType()[0];
			
			// Some domain fields don't contain a value, but a reference to another table
			// These should be handled differently
			def fieldName = this.field

			// Make sure the Template field is referenced as lowercase
			if( fieldName == "Template" )
				fieldName = "template";
							
			if( 
				( fieldName == "template" ) ||
				( objectToSearchIn.toLowerCase() == "subject" && fieldName.toLowerCase() == "species" ) || 
				( objectToSearchIn.toLowerCase() == "sample" && fieldName.toLowerCase() == "material" ) ||
				( objectToSearchIn.toLowerCase() == "assay" && fieldName.toLowerCase() == "module" ) ||
				( objectToSearchIn.toLowerCase() == "samplingevent" && fieldName.toLowerCase() == "sampletemplate" ) ) {
				fieldName += ".name"
			}
				
			def query = extendWhereClause( "( %s )", objectToSearchIn + "." + fieldName, prefix, criterionType, castValue( criterionType ) );
			return [ "join": "", "where": query.where, "parameters": query.parameters  ]
		}

		// Determine the type of this criterion
		def criterionTypes = criterionType();
		
		if( !criterionTypes )
			return emptyCriterion;			
		
		// Several types of criteria are handled differently.
		// The 'wildcard' is handled by searching for all types.
		// The 'simple' types (string, double) are handled by searching in the associated table 
		// The 'complex' types (stringlist, template etc., referencing another
		// database table) can't be handled correctly, since the HQL INDEX() function doesn't work on those relations.
		// We do a search for these types to see whether any field with that type fits this criterion, in order to 
		// filter out false positives later on.	
		criterionTypes.findAll { it }.each { criterionType ->
			// Cast criterion value to the right type
			def currentValue = castValue( criterionType );

			// Determine field name
			def fieldName = "template" + criterionType + 'Fields'
			
			switch( criterionType ) {
				case "Wildcard":
					// Wildcard search is handled by 
					break;

				case 'String':
				case 'Text':
				case 'File':
				case 'Date':
				case 'RelTime':
				case 'Double':
				case 'Long':
				case 'Boolean':
					// 'Simple' field types
					if( currentValue != null ) {
						joinClause += " left join " + objectToSearchIn + "." + fieldName + " as " + prefix + "_" + fieldName + " ";
	
						def condition = this.oneToManyWhereCondition( prefix + "_" + fieldName, prefix, criterionType, currentValue )
						whereClause += condition[ "where" ];
	
						condition[ "parameters" ].each {
							parameters[ it.key ] = it.value;
						}
					}
					break;
					
				case 'StringList':
				case 'ExtendableStringList':
				case 'Term':
				case 'Template':
				case 'Module':
					// 'Complex' field types
					def condition = this.manyToManyWhereCondition( objectToSearchIn, fieldName, prefix, "name", currentValue )
					whereClause += condition[ "where" ];
	
					condition[ "parameters" ].each {
						parameters[ it.key ] = it.value;
					}
				default:
					break;
			}
		}

		def where = whereClause?.findAll { it } ? "( " + whereClause.join( " OR " ) + " )" : ""
		
		return [ "join": joinClause, "where": where , "parameters": parameters ];
	}

	/**
	 * Extends a given condition with a where clause of this criterion. If you supply "select * from Study where %s", %s will
	 * be replaced by the where clause for the given field. Also, the parameters map will be extended (if needed)
	 * 
	 * @param hql			Initial HQL string where the clause will be put into
	 * @param fieldName		Name of the field that should be referenced 
	 * @param uniquePrefix	Unique prefix for this criterion
	 * @param fieldType		Type of field value to search for
	 * @param fieldValue	Field value to search for
	 * @return				Map with 'where' key referencing the extended where clause and 'parameters' key referencing a map with parameters.
	 */
	protected Map extendWhereClause( String hql, String fieldName, String uniquePrefix, String fieldType, def fieldValue ) {
		def parameters = [:]
		def textFieldTypes = [ 'String', 'Text', 'File', 'StringList', 'ExtendableStringList', 'Term', 'Template', 'Module' ];
		
		switch( this.operator ) {
			case Operator.contains:
				// Text fields should be handled case insensitive
				if( textFieldTypes.contains( fieldType ) ) {
					hql = sprintf( hql, "lower( " + fieldName + ") like lower( :" + uniquePrefix + "ValueLike )" );
				} else {
					hql = sprintf( hql, fieldName + " like :" + uniquePrefix + "ValueLike" );
				}
				parameters[ uniquePrefix + "ValueLike" ] = "%" + fieldValue + "%"
				break;
			case Operator.equals:
			case Operator.gte:
			case Operator.gt:
			case Operator.lte:
			case Operator.lt:
				if( textFieldTypes.contains( fieldType ) ) {
					hql = sprintf( hql, "lower( " + fieldName + " ) "  + this.operator.name + " lower( :" + uniquePrefix + "Value" + fieldType + ")" );
				} else {
					hql = sprintf( hql, fieldName + " "  + this.operator.name + " :" + uniquePrefix + "Value" + fieldType );
				}
				parameters[ uniquePrefix + "Value" + fieldType ] = fieldValue
				break;
		}

		return [ "where": hql, "parameters": parameters]
	}

	/**
	 * Creates a condition for this criterion, for a given fieldName and value. The fieldName should reference a collection that has a one-to-many
	 * relation with the object being sought
	 *  
	 * @param fieldName		Name to search in
	 * @param uniquePrefix	Unique prefix for this criterion
	 * @param currentValue	Map with 'value' referencing the value being sought and 'type' referencing 
	 * 						the type of the value as string. The value should be be casted to the right class for this field.
	 * @return				Map with 'where' key referencing the where clause and 'parameters' key referencing a map with parameters.
	 */
	protected Map oneToManyWhereCondition( String fieldName, String uniquePrefix, String fieldType, def fieldValue ) {
		// Create the where condition for checking the value
		// First check the name of the field, if needed
		def condition
		def parameters = [:]

		if( this.field != '*' ) {
			condition = "( %s AND index(" + fieldName + ") = :" + uniquePrefix + "Field )"
			parameters[ uniquePrefix + "Field" ] = this.field
		} else {
			condition = "%s";
		}

		def whereClause = extendWhereClause( condition, fieldName, uniquePrefix, fieldType, fieldValue );
		parameters.each {
			whereClause.parameters[ it.key ] = it.value;
		}

		return whereClause;
	}

	/**
	 * Creates a condition for this criterion, for a given fieldName and value. The fieldName should 
	 * reference a collection that has a many-to-many relation with the object being sought (e.g. templateTermFields).
	 * 
	 * Unfortunately, there is no way to determine the name of the field in HQL for this many-to-many collections, since the
	 * INDEX() function in HQL doesn't work for many-to-many collections.
	 * @see http://opensource.atlassian.com/projects/hibernate/browse/HHH-4879
	 * @see http://opensource.atlassian.com/projects/hibernate/browse/HHH-4615
	 *  
	 * @param fieldName		Name to search in
	 * @param uniquePrefix	Unique prefix for this criterion
	 * @param currentValue	Map with 'value' referencing the value being sought and 'type' referencing 
	 * 						the type of the value as string. The value should be be casted to the right class for this field.
	 * @return				Map with 'where' key referencing the where clause and 'parameters' key referencing a map with parameters.
	 */
	protected Map manyToManyWhereCondition( String objectToSearchIn, String collection, String uniquePrefix, String searchField, def value ) {
		// exists( FROM [objectToSearchIn].[collection] as [uniquePrefix][collection] WHERE [searchField] LIKE [value] )
		// Create the where condition for checking the value
		def condition = "exists ( FROM " + objectToSearchIn + "." + collection + " as " + uniquePrefix + "_" + collection + " WHERE %s )";

		return extendWhereClause( condition, uniquePrefix + "_" + collection + "." + searchField, uniquePrefix, "STRING", value );
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
