package org.dbxp.sam.query

import java.text.SimpleDateFormat
import org.dbnp.gdt.*
import dbnp.studycapturing.*
import org.apache.commons.logging.LogFactory;

/**
 * Represents a criterion to search on
 * @author robert
 *
 */
class SamCriterion extends dbnp.query.Criterion {
	private static final log = LogFactory.getLog(this);

	/**
	 * Create a HQL where clause from this criterion, in order to be used within a larger HQL statement
	 * 
	 * @param	objectToSearchIn    HQL name of the object to search in
	 * @return	Map with 3 keys:   'join' and'where' with the HQL join and where clause for this criterion and 'parameters' for the query named parameters
	 */
	public Map toHQL( String prefix, String objectToSearchIn = "measurement" ) {
		def emptyCriterion = [ "join": null, "where": "(1 = 2)", "parameters": null ];

                // Retrieve the proper value. The type we compare against is a Double
                def criterionType = 'Double'
                def castValue = castValue(criterionType)

                // If no or invalid value is given, don't do anything
                if( castValue == null )
                    return emptyCriterion;

                // Create basic where clause and extend it with the filter for the value
                def basicWhereClause = "( " + objectToSearchIn + ".feature.name = :" + prefix + "FeatureName AND %s )"
                def whereClause = extendWhereClause( basicWhereClause, objectToSearchIn + ".value", prefix, criterionType, castValue );
                
                // Add a parameter for the featurename
                whereClause.parameters[ prefix + 'FeatureName' ] = field
                
                whereClause
	}
	
}
