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

	static belongsTo = [parent : Study]

	String name

	static hasMany = [
		subjects: Subject,
		events: Event,
		samplingEvents: SamplingEvent
	]

	static constraints = {
	}
}
