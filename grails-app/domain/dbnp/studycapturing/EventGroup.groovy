package dbnp.studycapturing

/**
 * EventGroup groups events
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class EventGroup implements Serializable {
	String name

	static hasMany = [
		subjects: Subject,
		events: Event
	]

	static constraints = {
	}
}
