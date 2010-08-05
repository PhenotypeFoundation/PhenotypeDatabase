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

	// keep an internal identifier for use in dynamic forms
	private int identifier = 0
	static int iterator = 0

	static transients = [ "identifier", "iterator" ]
	static belongsTo = [parent : Study]
	static hasMany = [
		subjects: Subject,
		events: Event,
		samplingEvents: SamplingEvent
	]

	static constraints = {
	}

	/**
	 * Class constructor increments that static iterator
	 * and sets the object's identifier (used in dynamic webforms)
	 * @void
	 */
	public EventGroup() {
		if (!identifier) identifier = iterator++
	}

	/**
	 * Return the identifier
	 * @return int
	 */
	final public int getIdentifier() {
		return identifier
	}
}
