package dbnp.studycapturing

/**
 * The Event class describes an actual event, as it has happened to a certain subject. Often, the same event occurs
 * to multiple subjects at the same time. That is why the actual description of the event is factored out into a more
 * general EventDescription class. Events that also lead to sample(s) should be instantiated as SamplingEvents.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Event extends TemplateEntity {

	static belongsTo = [parent : Study]	

	/** Start time of the event, relative to the start time of the study */
	long startTime
	/** end time of the event, relative to the start time of the study */
	long endTime

	/**
	 * Constraints
	 */
	static constraints = {
		endTime(validator: { fields, obj, errors ->
			def error = false

			// endTime must be >= the startTime
			if (fields && fields.compareTo(obj.startTime) < 0) {
				error = true
				errors.rejectValue(
					'endTime',
					'event.endTime.greaterThanStartTime',
					['endTime', fields] as Object[],
					'End time should be greater than or equal to the Start Time'
				)
			}

			return (!error)
		})
	}

	static mapping = {

		// Specify that subclasses for Event should have their own database table.
		// This is done because otherwise we run into troubles with the SamplingEvent references from Study.
		tablePerHierarchy false
	}

	/**
	 * return the domain fields for this domain class
	 * @return List<TemplateField>
	 */
	static List<TemplateField> giveDomainFields() { return Event.domainFields }

	// To improve performance, the domain fields list is stored as a static final variable in the class.
	static final List<TemplateField> domainFields = [
		new TemplateField(
			name: 'startTime',
			type: TemplateFieldType.RELTIME,
			comment: "Please enter the start time as a relative time from study start date. "+RelTime.getHelpText(),
			required: true),
		new TemplateField(
			name: 'endTime',
			type: TemplateFieldType.RELTIME,
			comment: "Please enter the end time as a relative time from study start date. "+RelTime.getHelpText(),
			required: true)
	]

	/**
	 * Get the duration of the event as RelTime
	 * @return RelTime
	 */
	def getDuration() {
		return new RelTime(startTime, endTime)
	}

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
		return new RelTime(startTime, endTime).toPrettyString();
	}

	/**
	 * Get human readable string representing the duration between startTime and endTime, rounded to one unit (weeks/days/hours etc.) 
     *
	 * @return String
	 */
	def getRoundedDuration() {
		return new RelTime(startTime, endTime).toPrettyRoundedString();
	}

	/**
	 *    Return whether this is SamplingEvent
	 * @return    boolean
	 */
	def isSamplingEvent() {
		return (this instanceof SamplingEvent)
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

	def String toString() {
		return fieldExists('Description') ? getFieldValue('Description') : ""
	}
}
