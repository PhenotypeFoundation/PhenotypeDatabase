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
package org.dbxp.sam.query

import org.dbnp.gdt.*
import java.text.SimpleDateFormat
import dbnp.authentication.*
import org.dbxp.sam.Measurement

class SamSearch extends dbnp.query.Search {
    String module
    
    /**
     * Constructor of this search object. Sets the user field to the 
     * currently logged in user
     * @see #user
     */
    public SamSearch(String entity, String module = null) {
        super()
        
        this.entity = entity
        this.module = module
    }

    /**
     * Executes a query
     */
    protected void executeSearch() {
        if( !criteria ) {
            log.warn "Query without criteria should not be executed"
            results = []
            return
        }
        
        // Create HQL query for criteria for the entity being sought
        def selectClause = createSelectClauseForEntity( this.entity )
        def fullHQL = createHQLForEntity( this.entity );

        // Create where clause
        def whereClause = ""
        def parameters = fullHQL.parameters
        
        // Start with filtering on module
        if( this.module ) {
            whereClause += "( measurement.feature.platform.platformtype = :module )"
            parameters.module = module
        }

        if( fullHQL.where ) {
            // TODO: Handle more complex scenarios like OR search or combining multiple features
            def fullHQLclause = "(" + fullHQL.where.join( ") OR (" ) + ") "
            
            if( whereClause )
                whereClause += " AND ( " + fullHQLclause + ") "
            else
                whereClause = fullHQLclause
        }
        
        // Combine all parts to generate a full HQL query
        def hqlQuery = "SELECT DISTINCT " + selectClause + " " + fullHQL.from + ( whereClause ? " WHERE " + whereClause : "" );

        log.debug "HQL Query in SAM: " + hqlQuery
        log.debug "Parameters in SAM: " + parameters
        
        results = Measurement.executeQuery( hqlQuery, parameters )
    }

    /**
     * Create HQL statement for the given criteria and a specific entity
     * @param entityName		Name of the entity
     * @param entityCriteria	list of criteria to create the HQL for.
     * @param includeFrom		(optional) If set to true, the 'FROM entity' is prepended to the from clause. Defaults to true
     * @return
     */
    def createHQLForEntity( String entityName, def entityCriteria = null, includeFrom = true ) {
        def fromClause = "FROM Measurement measurement"

        def whereClause = []
        def parameters = [:]
        def criterionNum = 0;

        if( !entityCriteria )
            entityCriteria = this.criteria
        
        entityCriteria.each {
            def criteriaHQL = it.toHQL( "criterion" + entityName + criterionNum++ );

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

    /**
     * Determines the select clause for the given entity
     */
    def createSelectClauseForEntity( String entityName ) {
        switch( entityName ) {
            case 'Study':
                return "measurement.sample.parentSample.parent.UUID"
            case 'Sample':
                return "measurement.sample.parentSample.UUID"
            case 'Assay':
                return "measurement.sample.parentAssay.UUID"
        }

        return null
    }
    
    /**
     * Saves data about template entities to use later on. This data is copied to a special
     * structure to make it compatible with data fetched from other modules.
     * @see #saveResultField()
     */
    protected void saveResultFields() {
        // Don't save result fields for now
        return
    }
}
