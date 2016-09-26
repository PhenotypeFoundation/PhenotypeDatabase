package org.dbxp.sam
import dbnp.studycapturing.Sample
import dbnp.studycapturing.Assay

// This class saves additional information on GSCF samples specifically for SAM
// It does not extend Sample because that would generated a very complicated Hibernate mapping
// Instead, we store the parent sample in a field called parentSample and the parent (SAM) Assay in a field called parentAssay
class SAMSample {

    static belongsTo = [
            // Define the relation to the parent sample
            parentSample: Sample,
            // And also remember the parent assay that generated this sample
            parentAssay: Assay
    ]

    static hasMany = [measurements: Measurement]
	
	static mapping = {
		measurements cascade: "all-delete-orphan"
	}

    public String getName() {
        parentSample.name
    }

    public String toString() {
        return parentSample.name
    }

	/**
	 * Sets the properties of this object, based on the JSON object given by GSCF
	 * @param jsonObject	Object with sample data from GSCF
	 */
	public void setPropertiesFromGscfJson( jsonObject ) {
		super.setPropertiesFromGscfJson( jsonObject );

		if( !jsonObject.subjectObject || !jsonObject.subjectObject.name  || jsonObject.subjectObject.name == "null" ) {
			this.subjectName = null
		} else {
			this.subjectName = jsonObject.subjectObject.name.toString();
		}

		if( jsonObject.eventObject == null || jsonObject.eventObject.startTime == null || jsonObject.eventObject.startTime == "null" ) {
			this.eventStartTime = null
		} else {
			if( jsonObject.eventObject.startTime.toString().isLong() ) {
				this.eventStartTime = Long.valueOf( jsonObject.eventObject.startTime.toString() );
			} else {
				this.eventStartTime = null;
			}
		}

	}

	/**
	 * Return all samples this user may read
	 * @param user
	 * @return
	 */
	public static giveReadableSamples( user ) {
		def assays = Assay.giveReadableAssays( user );
		if( !assays )
			return []

		return SAMSample.findAll( "FROM SAMSample s WHERE s.parentAssay IN (:assays)", [ "assays": assays ] )
	}

	/**
	 * Return all samples this user may write
	 * @param user
	 * @return
	 */
	public static giveWritableSamples( user ) {
		def assays = Assay.giveWritableAssays( user );
		if( !assays )
			return []

		return SAMSample.findAll( "FROM SAMSample s WHERE s.parentAssay IN (:assays)", [ "assays": assays ] )
	}

	/**
	 * Deletes all samples for an assay
	 * @param user
	 * @return
	 */
	public static deleteByAssay( Assay a ) {
	try {
		SAMSample.executeUpdate("delete SAMSample s where s.parentAssay = :assay", [ assay: a ] )
		return true;
	} catch( Exception e ) {
		e.printStackTrace();
		return false;
	}
}
}
