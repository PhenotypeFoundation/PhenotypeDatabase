package org.dbxp.sam

import dbnp.studycapturing.Assay

class Measurement {

    static belongsTo = [sample:SAMSample, feature:Feature]
    String operator // '<', '>' or '='
    Double value
    String comments
    static final validOperators = ['<', '>','',null]

    static mapping = {
        comments type: 'text'
    }

    static constraints = {
        value( blank: true, nullable: true, validator: { val, obj -> 
			if( val == null && !obj.comments )
				return 'Value or comments fields should be filled'
			else
				return true 
        })
        operator(blank:true,nullable:true,validator:{val,obj->val in obj.validOperators})
        comments(blank:true,nullable:true)
        sample(blank:false, index: 'sample_and_feature')
        feature(blank:false, index: 'sample_and_feature')
    }
	
	/**
	 * Return all measurements this user may read
	 * @param user
	 * @return
	 */
	public static giveReadableMeasurements( user ) {
		def assays = Assay.giveReadableAssays( user );
		if( !assays )
			return []
			
		return Measurement.findAll( "FROM Measurement m WHERE m.sample.parentAssay IN (:assays)", [ "assays": assays ] )
	}
	
	public static deleteByAssay( Assay a ) {
		try {
			Measurement.executeUpdate( "delete Measurement m WHERE m.sample IN( FROM SAMSample s where s.parentAssay = :assay )", [ assay: a ] );
			SAMSample.executeUpdate("delete SAMSample s where s.parentAssay = :assay", [ assay: a ] )
			return true;
		} catch( Exception e ) {
			e.printStackTrace();
			return false;
		}
	}
}
