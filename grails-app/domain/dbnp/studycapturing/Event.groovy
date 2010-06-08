package dbnp.studycapturing

import groovy.time.*

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
class Event extends TemplateEntity implements Serializable {
	long startTime
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

	/**
	 * return the domain fields for this domain class
	 * @return List
	 */
	static List<TemplateField> giveDomainFields() { return Event.domainFields }

	// TODO: Kees start documenting your code
	static final List<TemplateField> domainFields = [
		new TemplateField(
			name: 'startTime',
			type: TemplateFieldType.RELTIME),
		new TemplateField(
			name: 'endTime',
			type: TemplateFieldType.RELTIME)
	]

	def getDuration() {
		return new RelTime(startTime, endTime);
	}

	/**
	 * get a prettified duration
	 * @return String
	 */
	static def getPrettyDuration(RelTime duration) {
		return duration.toPrettyRoundedString();
	}

	// TODO: Kees start documenting your code
	def getPrettyDuration() {
		getPrettyDuration(getDuration())
	}

	// TODO: Kees start documenting your code
	def getDurationString() {
		def d = getDuration()
		return getDuration().toPrettyString();
	}

	// TODO: Kees start documenting your code
	def getShortDuration() {
		def d = getDuration()
		return getDuration().toString();
	}

	// TODO: Kees start documenting your code
	def isSamplingEvent() {
		return (this instanceof SamplingEvent)
	}

	// TODO: Kees start documenting your code
	def belongsToGroup(Set<EventGroup> groups) {
		def eventFound = false;
		def that = this;
		groups.each { eventgroup ->
			if (!eventFound) {
				eventFound = (that.id in eventgroup.events.id);
			}
		}

		return eventFound;
	}

	// TODO: Kees start documenting your code
	def String toString() {
		return fieldExists('Description') ? getFieldValue('Description') : ""
	}
}