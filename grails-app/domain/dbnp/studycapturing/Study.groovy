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

	/**
	 * return the domain fields for this domain class
	 * @return List
	 */
	List<TemplateField> giveDomainFields() {
		[
			new TemplateField(
				name: 'title',
				type: TemplateFieldType.STRING),
			new TemplateField(
				name: 'startDate',
				type: TemplateFieldType.DATE)
		]
	}

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
	}

	static mapping = {
		researchQuestion type: 'text'
		description type: 'text'
		autoTimestamp true
	}

	/**
	 * return the title of this study
	 */
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
	 * Return the unique Event templates that are used in this study
	 */
	def Set<Template> giveEventTemplates() {
		TemplateEntity.giveTemplates(events);
	}

	/**
	 * Return the unique SamplingEvent templates that are used in this study
	 */
	def Set<Template> giveSamplingEventTemplates() {
		TemplateEntity.giveTemplates(events);
	}

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
