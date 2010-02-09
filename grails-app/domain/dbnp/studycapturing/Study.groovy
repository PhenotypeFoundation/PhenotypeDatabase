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
						samplingEvents: SamplingEvent,
                        persons: StudyPerson,
                        publications: Publication
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

	def giveAllFields() {
		return template.studyFields;
	}
}
