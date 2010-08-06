package dbnp.studycapturing

/**
 * EventGroup groups events
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class EventGroup extends Identity {
	String name

	static belongsTo = [parent : Study]
	static hasMany = [
		subjects: Subject,
		events: Event,
		samplingEvents: SamplingEvent
	]

	static constraints = {
	}
}
