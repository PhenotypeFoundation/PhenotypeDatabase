package dbnp.studycapturing

/**
 * Domain class describing the basic entity in the study capture part: the Study class.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Study implements Serializable {
	static searchable = true
	nimble.User owner
	String title
	String code
	String researchQuestion
	String description
	String ecCode
	Date dateCreated
	Date lastUpdated
	Date startDate
	Template template

	static hasMany = [	editors: nimble.User,
						readers: nimble.User,
						subjects: Subject,
						groups: SubjectGroup,
						events: Event,
						samplingEvents: SamplingEvent
	]

	static constraints = {
		owner(nullable: true, blank: true)
		title(nullable: false, blank: false)
		template(nullable: true, blank: true)
	}

	static mapping = {
		researchQuestion type: 'text'
		description type: 'text'
		autoTimestamp true
	}

	def String toString() {
		return title;
	}

	Map giveAllFields() {
		def result = [:]

		// Using reflection here gives way too many properties, like searchable, hasMany,
		// and it will probably extend when we use new plugins
		// It is probably best
		// - to either hardcode the above given properties
		// - or to move all fields to the template
		
		this.properties.each{ //public fields only
		        println it.name
			result[it.name] = it.type.name //name of field and name of type
		}

		return result;
	}
}
