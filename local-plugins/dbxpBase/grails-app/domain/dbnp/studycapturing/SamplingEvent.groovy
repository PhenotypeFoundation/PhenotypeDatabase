package dbnp.studycapturing
import org.dbnp.gdt.*

/**
 * The SamplingEvent class describes a sampling event, an event that also results in one or more samples.
 *
 * NOTE: according to Grails documentation, super classes and subclasses share the same table.
 *       thus, we could merge the sampling with the Event super class and include a boolean
 *       However, using a separate class makes it more clear in the code that Event and SamplingEvent are treated differently
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class SamplingEvent extends TemplateEntity {
	// A SamplingEvent always belongs to one study.
	// Although this is technically inherited from Event, we have to specify it here again.
	// Otherwise, Grails expects the SamplingEvent to be referenced in Study.events,
	// where it is actually referenced in Study.samplingEvents
	static belongsTo = [parent : Study]
	static hasMany = [
		eventGroupInstances: SamplingEventInEventGroup 
	]


	// define what template samples should have
	String name
	Template sampleTemplate

	// define domain constraints
	static constraints = {
		name nullable: true
		sampleTemplate(nullable: false, blank: false)
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
			comment: "Please enter the name of the sampling event.",
			required: true),
		new TemplateField(
			name: 'sampleTemplate',
			type: TemplateFieldType.TEMPLATE,
			entity: dbnp.studycapturing.Sample,
			comment: "Please select the template of the resulting samples",
			required: true)
	]

       static mapping = {
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
			if (!eventFound && eventgroup.samplingEventInstances) {
				eventFound = (that.id in eventgroup.samplingEventInstances*.event.id);
			}
		}

		return eventFound;
	}

	def String toString() {
        return name
	}
	
}