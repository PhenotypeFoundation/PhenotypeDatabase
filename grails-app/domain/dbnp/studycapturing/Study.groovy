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
	static searchable = {
    	[only: ['title', 'Description']]
    }

	nimble.User owner
	String title
	String code 		// also enables referencing to studies from the Simple Assay Module
	Date dateCreated
	Date lastUpdated
	Date startDate
    List subjects
	List events
	List samplingEvents
	List eventGroups
	List samples
	List assays

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
		code(nullable:false, blank:true,unique:true) 
	}

	static mapping = {
		researchQuestion type: 'text'
		description type: 'text'
		autoTimestamp true
	}

	/**
	 * return the domain fields for this domain class
	 * @return List
	 */
	static List<TemplateField> giveDomainFields() { return Study.domainFields }

	static final List<TemplateField> domainFields = [
		new TemplateField(
			name: 'title',
			type: TemplateFieldType.STRING),
		new TemplateField(
			name: 'code',
			type: TemplateFieldType.STRING,
			preferredIdentifier:true,
			comment: 'Fill out the code by which many people will recognize your study'),
		new TemplateField(
			name: 'startDate',
			type: TemplateFieldType.DATE,
			comment: 'Fill out the official start date or date of first action')
	]

	/**
	 * return the title of this study
	 */
	def String toString() {
		return title
	}

	/**
	 * returns all events and sampling events that do not belong to a group
	 */
	def Set<Event> getOrphanEvents() {
		def orphans =	events.findAll { event -> !event.belongsToGroup(eventGroups) } +
						samplingEvents.findAll { event -> !event.belongsToGroup(eventGroups) }

		return orphans
	}

	/**
	 * Return the unique Subject templates that are used in this study
	 */
	def Set<Template> giveSubjectTemplates() {
		TemplateEntity.giveTemplates(subjects)
	}

	/**
	 * Return the unique Event and SamplingEvent templates that are used in this study
	 */
	Set<Template> giveAllEventTemplates() {
		// For some reason, giveAllEventTemplates() + giveAllSamplingEventTemplates()
		// gives trouble when asking .size() to the result
		// So we also use giveTemplates here
		TemplateEntity.giveTemplates(events + samplingEvents)
	}

	/**
	 * Return the unique Event templates that are used in this study
	 */
	Set<Template> giveEventTemplates() {
		TemplateEntity.giveTemplates(events)
	}

	/**
	 * Return the unique SamplingEvent templates that are used in this study
	 */
	Set<Template> giveSamplingEventTemplates() {
		TemplateEntity.giveTemplates(samplingEvents)
	}

	/**
	 * Returns the unique Sample templates that are used in the study
	 */
	Set<Template> giveSampleTemplates() {
		TemplateEntity.giveTemplates(samples)
	}
	/**
	 * Returns the template of the study
	 */
	Template giveStudyTemplate() {
		return this.template
	}
}
