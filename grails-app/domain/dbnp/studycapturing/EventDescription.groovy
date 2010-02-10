package dbnp.studycapturing

import dbnp.data.Term

/**
 * Description of an event. Actual events are described by instances of the Event class.
 * For the moment, EventDescription is not linked to a specific study or user.
 * This means that the user can add events of all possible event types as defined by the (global) EventDescription collection.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class EventDescription implements Serializable {
	String name
	String description
	Term classification
	Protocol protocol
	boolean isSamplingEvent

	static constraints = {
		classification(nullable: true, blank: true)
	}
}
