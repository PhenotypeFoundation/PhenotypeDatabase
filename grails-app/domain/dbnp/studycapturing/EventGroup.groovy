package dbnp.studycapturing
import org.dbnp.gdt.*

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
		// Ensure that the event group name is unique within the study
		name(unique:['parent'])
	}
}
