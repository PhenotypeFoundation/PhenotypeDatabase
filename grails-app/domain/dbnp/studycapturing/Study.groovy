package dbnp.studycapturing

/**
 * Domain class describing the basic entity in the study capture part: the Study class.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Study extends TemplateEntity implements Serializable {
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

	static hasMany = [
		editors: nimble.User,
		readers: nimble.User,
		subjects: Subject,
		groups: SubjectGroup,
		events: Event,
		samplingEvents: SamplingEvent,
		samples: Sample,
		assays: Assay,
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

}
