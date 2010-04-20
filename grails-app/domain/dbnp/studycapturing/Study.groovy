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
	Date dateCreated
	Date lastUpdated
	Date startDate

	// TODO: The following 4 fields should be moved into templates
	//String code
	//String researchQuestion
	//String description
	//String ecCode


	static hasMany = [
		editors: nimble.User,
		readers: nimble.User,
		subjects: Subject,
		events: Event,
		samplingEvents: SamplingEvent,
		eventGroups: EventGroup,
		samples: Sample,
		assays: Assay,
		persons: StudyPerson,
		publications: Publication
	]

	static constraints = {
		owner(nullable: true, blank: true)
		title(nullable: false, blank: false)
		template(nullable: false, blank: false)
	}

	static mapping = {
		researchQuestion type: 'text'
		description type: 'text'
		autoTimestamp true
	}

	def String toString() {
		return title;
	}

	/**
	 * Return the unique Subject templates that are used in this study
	 */
	def Set<Template> giveSubjectTemplates() {
		TemplateEntity.giveTemplates(subjects);
	}


	/**
	 * Returns the template of all subjects in the study
	 * Throws an error if there are no or multiple subject templates
	 */
	// outcommented, we shouldn't make it too easy for ourselves by introducing uncertain assumptions (1 distinct template)
	//def Template giveSubjectTemplate() {
	//	TemplateEntity.giveTemplate(subjects);
	//}

	/**
	 * Returns the unique Sample templates that are used in the study
	 */
	def Template giveSampleTemplates() {
		TemplateEntity.giveTemplates(samples);
	}
	/**
	 * Returns the template of the study
	 */
	def Template giveStudyTemplate() {
		return this.template;
	}
}
