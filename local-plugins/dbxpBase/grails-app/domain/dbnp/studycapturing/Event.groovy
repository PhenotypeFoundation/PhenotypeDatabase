package dbnp.studycapturing
import org.dbnp.gdt.*

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
	String name
	
	static belongsTo = [parent : Study]
	static hasMany = [ eventGroupInstances: EventInEventGroup ]

	/**
	 * Constraints
	 */
	static constraints = {
		name nullable: true
	}

	/**
	 * return the domain fields for this domain class
	 * @return List<TemplateField>
	 */
	@Override
	List<TemplateField> giveDomainFields() { return domainFields }

	// To improve performance, the domain fields list is stored as a static final variable in the class.
	static final List<TemplateField> domainFields = [
		new TemplateField(
			name: 'name',
			type: TemplateFieldType.STRING,
			comment: "Please enter name of this event.",
			required: true),
	]

	static mapping = {
		sort "name"

		// Workaround for bug http://jira.codehaus.org/browse/GRAILS-6754
		templateTextFields type: 'text'
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
			if (!eventFound && eventgroup.events) {
				eventFound = (that.id in eventgroup.events.id);
			}
		}

		return eventFound;
	}

	def String toString() {
		return name
	}
}
