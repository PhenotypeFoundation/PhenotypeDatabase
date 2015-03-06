package dbnp.studycapturing

import org.dbnp.gdt.RelTime

import java.io.Serializable;


class SubjectEventGroup implements Serializable {
	/** 
	 * Start time of the eventgroup for this group of subjects, 
	 * relative to the start time of the study as the number of seconds
	 */
	long startTime
	
	/**
	 * Description of this subjectEventGroup
	 */
	String description
	
	/**
	 * Sets the startTime from an absolute date (number of seconds since 1970)
	 */
	public void setAbsoluteStartTime( Number seconds ) {
		startTime = seconds - eventGroup.parent.startDate.time / 1000
	}
	
	/**
	 * Returns the absolute start date for this event group
	 * @return
	 */
	public Date getStartDate() {
		Calendar calendar = Calendar.getInstance();
		
		// Add the startTime to the startdate of the study
		calendar.setTime(parent.startDate);
		calendar.add( Calendar.SECOND, (int) startTime )
		
		calendar.time
	}
	
	/**
	 * Returns the absolute end date for this event group
	 * @return
	 */
	public Date getEndDate() {
		Calendar calendar = Calendar.getInstance();
		
		// Add the startTime and the duration of the eventgroup to the startdate of the study
		calendar.setTime(parent.startDate);
		calendar.add( Calendar.SECOND, (int) startTime + (int) eventGroup?.duration?.value )
		
		calendar.time
	}

    def getStartTimeString() {
        return new RelTime(startTime).toPrettyString();
    }
	
	static belongsTo = [ parent: Study, subjectGroup: SubjectGroup, eventGroup: EventGroup ]
	static hasMany = [ samples: Sample ]
	static constraints = {
		description nullable: true
    }
}
