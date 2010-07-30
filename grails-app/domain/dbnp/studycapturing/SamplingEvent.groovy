package dbnp.studycapturing

/**
 * The SamplingEvent class describes a sampling event, an event that also results in one or more samples.
 *
 * NOTE: according to Grails documentation, super classes and subclasses share the same table.
 *       thus, we could merge the sampling with the Event super class and include a boolean
 *       However, using a separate class makes it more clear in the code that Event and SamplingEvent are treated differently
 */
class SamplingEvent extends TemplateEntity {

	// A SamplingEvent always belongs to one study.
	// Although this is technically inherited from Event, we have to specify it here again.
	// Otherwise, Grails expects the SamplingEvent to be referenced in Study.events,
	// where it is actually referenced in Study.samplingEvents
	static belongsTo = [parent : Study]

	static hasMany = [samples : Sample]

	/** Start time of the event, relative to the start time of the study */
	long startTime

	/** Duration of the sampling event, if it has any (default is 0) */
	long duration

	static constraints = {
		duration(default: 0L)
	}

	/**
	 * return the domain fields for this domain class
	 * @return List<TemplateField>
	 */
	static List<TemplateField> giveDomainFields() { return SamplingEvent.domainFields }

	// To improve performance, the domain fields list is stored as a static final variable in the class.
	static final List<TemplateField> domainFields = [
		new TemplateField(
			name: 'startTime',
			type: TemplateFieldType.RELTIME,
			comment: "Please enter the start time as a relative time from study start date."+RelTime.getHelpText()),
		new TemplateField(
			name: 'duration',
			type: TemplateFieldType.RELTIME,
			comment: "Please enter the duration of the sampling action, if applicable. "+RelTime.getHelpText())
	]

	/**
	 * Get the duration of the event as RelTime
	 * @return RelTime
	 */
	/*RelTime getDuration() {
		return new RelTime(duration)
	}*/

	 /**
	  * Return the start time of the event, which should be relative to the start of the study
	  * @return String a human readable representation of the start time of the event
	 */
	def getStartTimeString() {
		return new RelTime(startTime).toPrettyString();
	}

	/**
	 * Get extended, human readable string representing the duration between startTime and endTime
     *
	 * @return String
	 */
	def getDurationString() {
		return new RelTime(duration).toPrettyString();
	}

	/**
	 * Checks whether this Event is part of one or more of the given EventGroups
	 * @param groups
	 * @return
	 */
	def belongsToGroup(Collection<EventGroup> groups) {
		def eventFound = false;
		def that = this;
		groups.each { eventgroup ->
			if (!eventFound) {
				eventFound = (that.id in eventgroup.events.id);
			}
		}

		return eventFound;
	}	

	/**
	 * Get all samples that have this sampling event as a parent
 	 */
	def getSamples() {

		def samples = Sample.findAll("from Sample as s where s.parentEvent.id = ${this.id}")
		samples.collect { it.class == SamplingEvent.class }
		samples.collect { it != null }
		return samples == null ? [] : samples
	}

}
