package dbnp.studycapturing
import org.dbnp.gdt.*
import dbnp.authentication.SecUser
import dbnp.authentication.SecUserGroup
import org.dbxp.sam.*

/**
 * Domain class describing the basic entity in the study capture part: the Study class.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Study extends TemplateEntity {
	def moduleNotificationService

	SecUser owner		// The owner of the study. A new study is automatically owned by its creator.
	String title		// The title of the study
	String description	// A brief synopsis of what the study is about
	String code			// currently used as the external study ID, e.g. to reference a study in a SAM module
	Date dateCreated


	Date lastUpdated
	Date startDate

	List subjects
	List subjectGroups

	List events
	List samplingEvents
	List eventGroups

	List samples
	List assays

	boolean publicstudy = false  // Determines whether anonymous users are allowed to see this study. This has only effect when published = true

	// 20120625: published default to true
	boolean published = true // Determines whether a study is private (only accessable by the owner and writers) or published (also visible to readers)

	static hasMany = [
		subjects: Subject,
		subjectGroups: SubjectGroup,

		samplingEvents: SamplingEvent,
		events: Event,
		eventGroups: EventGroup,

		subjectEventGroups: SubjectEventGroup,

		samples: Sample,
		assays: Assay,
		persons: StudyPerson,
		publications: Publication,
		readers: SecUser,
                readerGroups: SecUserGroup,
		writers: SecUser,
                writerGroups: SecUserGroup
	]

	static constraints = {
		title(nullable:false, blank: false, unique:true, maxSize: 255)
		owner(nullable: true, blank: true)
		code(nullable: true, blank: true, unique: true, maxSize: 255, matches: "[^ ]+")
		persons(size:1..1000)
		// TODO: add custom validator for 'published' to assess whether the study meets all quality criteria for publication
		// tested by SampleTests.testStudyPublish
	}

	// see org.dbnp.gdt.FuzzyStringMatchController and Service
	static fuzzyStringMatchable = [
		"title",
		"code"
	]

	static mapping = {
		cache true
		autoTimestamp true
		sort "title"

		// Make sure the TEXT field description is persisted with a TEXT field in the database
		description type: 'text'
		// Workaround for bug http://jira.codehaus.org/browse/GRAILS-6754
		templateTextFields type: 'text'

	}

	/**
	 * return the domain fields for this domain class
	 * @return List
	 */
	@Override
	List<TemplateField> giveDomainFields() { return domainFields }

	static final List<TemplateField> domainFields = [
		new TemplateField(
		name: 'title',
		type: TemplateFieldType.STRING,
		required: true),
		new TemplateField(
		name: 'description',
		type: TemplateFieldType.TEXT,
		comment:'Give a brief synopsis of what your study is about',
		required: true),
		new TemplateField(
		name: 'code',
		type: TemplateFieldType.STRING,
		preferredIdentifier: true,
		comment: 'Fill out the code by which many people will recognize your study',
		required: true),
		new TemplateField(
		name: 'startDate',
		type: TemplateFieldType.DATE,
		comment: 'Fill out the official start date or date of first action',
		required: true),
		//		new TemplateField(
		//		name: 'published',
		//		type: TemplateFieldType.BOOLEAN,
		//		comment: 'Determines whether this study is published (accessible for the study readers and, if the study is public, for anonymous users). A study can only be published if it meets certain quality criteria, which will be checked upon save.',
		//		required: false)
	]

	/**
	 * return the title of this study
	 */
	def String toString() {
		return ( (code) ? code : "[no code]") + " - "+ title
	}

	/**
	 * returns all events and sampling events that do not belong to a group
	 */
	def List<Event> getOrphanEvents() {
		def orphans = events.findAll { event -> !event.belongsToGroup(eventGroups) } +
		samplingEvents.findAll { event -> !event.belongsToGroup(eventGroups) }

		return orphans
	}

	/**
	 * Return a list of distinct templates used for this entity
	 */
	protected List<Template> giveTemplateForChildEntity(def entity) {
		def c = entity.createCriteria()
		c.list {
			createAlias('template', 'template')
			eq("parent.id", this.id)
			projections {
				distinct('template')
			}
			//order( "template.name", "asc")
		}
	}
	
	protected List giveEntitiesForTemplate(Template) {
		template.entity.createCriteria()
		c.list {
			eq("parent.id", this.id)
			eq("template.id", template.id)
		}
	}
	
	/**
	 * Return the unique Subject templates that are used in this study
	 */
	def List<Template> giveSubjectTemplates() {
		giveTemplateForChildEntity(Subject)
	}

	/**
	 * Return all subjects for a specific template
	 * @param Template
	 * @return ArrayList
	 */
	def List<Subject> giveSubjectsForTemplate(Template template) {
		giveEntititesForTemplate(template)
	}

	/**
	 * Return all unique assay templates
	 * @return Set
	 */
	List<Template> giveAllAssayTemplates() {
		giveTemplateForChildEntity(Assay)
	}

	/**
	 * Return all assays for a particular template
	 * @return ArrayList
	 */
	def ArrayList giveAssaysForTemplate(Template template) {
		giveEntititesForTemplate(template)
	}

	/**
	 * Return the unique Event and SamplingEvent templates that are used in this study
	 */
	List<Template> giveAllEventTemplates() {
		// For some reason, giveAllEventTemplates() + giveAllSamplingEventTemplates()
		// gives trouble when asking .size() to the result
		// So we also use giveTemplates here
		TemplateEntity.giveTemplates(((events) ? events : []) + ((samplingEvents) ? samplingEvents : []))
	}

	/**
	 * Return all events and samplingEvenets for a specific template
	 * @param Template
	 * @return ArrayList
	 */
	def ArrayList giveEventsForTemplate(Template template) {
		def events = events.findAll { it.template.equals(template) }
		def samplingEvents = samplingEvents.findAll { it.template.equals(template) }

		return (events) ? events : samplingEvents
	}

	/**
	 * Return the unique Event templates that are used in this study
	 */
	List<Template> giveEventTemplates() {
		giveTemplateForChildEntity(Event)
	}

	/**
	 * Return the unique SamplingEvent templates that are used in this study
	 */
	List<Template> giveSamplingEventTemplates() {
		giveTemplateForChildEntity(SamplingEvent)
	}

	/**
	 * Returns the unique Sample templates that are used in the study
	 */
	List<Template> giveSampleTemplates() {
		giveTemplateForChildEntity(Sample)
	}

	/**
	 * Return all samples for a specific template, sorted by subject name
	 * @param Template
	 * @return ArrayList
	 */
	def ArrayList<Sample> giveSamplesForTemplate(Template template) {
		// sort in a concatenated string as sorting on 3 seperate elements
		// in a map does not seem to work properly
		samples.findAll { it.template.equals(template) }.sort {
			"${it.parentEvent?.template}|${it.parentEvent?.startTime}|${it.parentSubject?.name}".toLowerCase()
		}
	}

	def List giveUsedModules() {
		def c = Assay.createCriteria()
		c.list {
			createAlias('module', 'module')
			eq("parent.id", this.id)
			projections {
				distinct('module')
			}
			//order( "module.name", "asc")
		}
	}
	
	/**
	 * Returns the template of the study
	 */
	Template giveStudyTemplate() {
		return this.template
	}
	
	/**
	 * Returns a map of subject counts per species for this study
	 * @return
	 */
	public Map getSubjectCountsPerSpecies() {
		def c = Subject.createCriteria()
		def result = c.list {
			eq("parent.id", this.id)
			projections {
				groupProperty("species")
				rowCount()
			}
		}
		
		def output = [:]
		
		if( result ) {
			result.each {
				output[it[0]] = it[1]
			}
		}
		
		output
	}

	/**
	 * Returns the subject count for this study
	 * @return
	 */
	public int getSubjectCount() {
		Subject.createCriteria().count {
			eq("parent.id", this.id)
		}
	}
	
	/**
	 * Returns the sample count for this study
	 * @return
	 */
	public int getSampleCount() {
		Sample.createCriteria().count {
			eq("parent.id", this.id)
		}
	}
	
	/**
	 * Returns the assay count for this study
	 * @return
	 */
	public int getAssayCount() {
		Assay.createCriteria().count {
			eq("parent.id", this.id)
		}
	}

	
	/**
	 * Returns the assay count for this study
	 * @return
	 */
	public int getTotalEventCount() {
		Event.createCriteria().count { eq("parent.id", this.id)	} + SamplingEvent.createCriteria().count { eq("parent.id", this.id)	} 
	}
	/**
	 * Delete a specific subject from this study, including all its relations
	 * @param subject The subject to be deleted
	 * @void
	 */
	void deleteSubject(Subject subject) {
		if( subject ) {
			// Delete the subject from the event groups it was referenced in
			if( subject.subjectGroups ) {
				( [] + subject.subjectGroups ).each { subjectGroup ->
					subjectGroup.removeFromSubjects(subject)
					subjectGroup.save( flush: true )
				}
			}

			// Delete the samples that have this subject as parent
			this.samples.findAll { it.parentSubject.equals(subject) }.each {
				this.deleteSample(it)
			}

			// This should remove the subject itself too, because of the cascading belongsTo relation
			this.removeFromSubjects(subject)

			// But apparently it needs an explicit delete() too
			subject.delete( flush: true )
		}
	}

	/**
	 * Delete an assay from the study
	 * @param Assay
	 * @void
	 */
	def deleteAssay(Assay assay) {
		if (assay) {
                	// Delete SAMSample association
                	SAMSample.executeUpdate("delete SAMSample s where s.parentAssay = :assay", [assay: assay])
			// iterate through linked samples
			assay.samples.findAll { true }.each() { sample ->
				assay.removeFromSamples(sample)
			}

			// remove this assay from the study
			this.removeFromAssays(assay)

			// and delete it explicitly
			assay.delete()
		}
	}

    /**
     * Dependencies from SAM have to be dealed with manually, so run this function before deleting a study.
     * @void
     */
    def clearSAMDependencies() {
		// There can only be SAM dependencies if the study has samples and assays
		if( samples && assays ) {
			Measurement.executeUpdate( "DELETE Measurement m where m.id IN( SELECT m2.id FROM Measurement m2 WHERE m2.sample.parentSample IN (:samples ) and m2.sample.parentAssay in (:assays) )", [samples: samples, assays: assays] )
			SAMSample.executeUpdate("delete SAMSample s where s.parentSample IN (:samples) AND s.parentAssay in (:assays)", [samples: samples, assays: assays] )
		}
    }

	/**
	 * Delete an event from the study, including all its relations
	 * @param Event
	 * @void
	 */
	void deleteEvent(Event event) {
		if( !event )
			return

		// remove event from eventGroups
		( [] + event.eventGroupInstances ).each { eventGroupInstance ->
			eventGroupInstance.eventGroup.removeFromEventInstances( eventGroupInstance );
			eventGroupInstance.event.removeFromEventGroupInstances( eventGroupInstance );

			eventGroupInstance.delete();
		}

		// remove event from the study
		this.removeFromEvents(event)

		// and perform a hard delete
		event.delete()
	}


	/**
	 * Delete a samplingEvent from the study, including all its relations
	 * @param SamplingEvent
	 * @void
	 */
	void deleteSamplingEvent(SamplingEvent samplingEvent) {
		if( !samplingEvent )
			return

		// Delete the samples that have this sampling event as parent
		this.samples.findAll { it.parentEvent.event.equals(samplingEvent) }.each {
			// This should remove the sample itself too, because of the cascading belongsTo relation
			this.deleteSample(it)
		}

		// remove event from eventGroups
		( [] + samplingEvent.eventGroupInstances ).each { eventGroupInstance ->
			eventGroupInstance.eventGroup.removeFromSamplingEventInstances( eventGroupInstance );
			eventGroupInstance.event.removeFromEventGroupInstances( eventGroupInstance );

			eventGroupInstance.delete();
		}

		// Remove event from the study
		// This should remove the event group itself too, because of the cascading belongsTo relation
		this.removeFromSamplingEvents(samplingEvent)

		// But apparently it needs an explicit delete() too
		// (Which can be verified by outcommenting this line, then SampleTests.testDeleteViaParentSamplingEvent fails
		samplingEvent.delete()
	}


	/**
	 * Delete a sample from the study, including all its relations
	 * @param Event
	 * @void
	 */
	void deleteSample(Sample sample) {
		// remove the sample from the study
		this.removeFromSamples(sample)

        // Delete Measurements and SAMSample associations
        Measurement.executeUpdate( "delete Measurement m where m.id IN ( SELECT m2.id FROM Measurement m2 WHERE m2.sample.parentSample = :sample )", [sample: sample] )
        SAMSample.executeUpdate("delete SAMSample s where s.parentSample = :sample", [sample: sample] )

		// remove the sample from any sampling events it belongs to
		def parentEvent = sample.parentEvent;
		parentEvent?.removeFromSamples( sample );

		// remove the sample from any subjectEventGroup it belongs to
		def parentSubjectEventGroup = sample.parentSubjectEventGroup;
		parentSubjectEventGroup?.removeFromSamples( sample );

		// remove the sample from any assays it belongs to
		this.assays.findAll { it.samples.any { it == sample }}.each {
			it.removeFromSamples(sample)
		}

		// Also here, contrary to documentation, an extra delete() is needed
		// otherwise date is not properly deleted!
		sample.delete( flush: true )
	}

	/**
	 * Delete a subjectgroup from the study, including all its relations
	 * @param subjectGroup
	 */
	void deleteSubjectGroup(SubjectGroup subjectGroup) {
		log.debug( "Deleting subject group " + subjectGroup + " from " + this )

		// Delete all subjectEventGroups pointing to this subjectGroup
		( [] + subjectGroup.subjectEventGroups ).each {
			deleteSubjectEventGroup( it );
		}

		removeFromSubjectGroups( subjectGroup )

		subjectGroup.delete();
	}

	void deleteSubjectEventGroup( SubjectEventGroup subjectEventGroup ) {
		log.debug( "Deleting subject event group " + subjectEventGroup + " from " + this )
		if( !subjectEventGroup )
			return

		// Remove all samples belonging to this subjectEventGroup
		( [] + subjectEventGroup.samples ).each {
			deleteSample( it );
		}

		// Remove this subject eventgroup from the subjectGroup list and the eventgroup list
		subjectEventGroup.eventGroup?.removeFromSubjectEventGroups( subjectEventGroup )
		subjectEventGroup.subjectGroup?.removeFromSubjectEventGroups( subjectEventGroup )

		// Delete the object itself
		removeFromSubjectEventGroups( subjectEventGroup );
		subjectEventGroup.delete();
	}

	/**
	 * Delete an eventGroup from the study, including all its relations
	 * @param EventGroup
	 * @void
	 */
	void deleteEventGroup(EventGroup eventGroup) {
		log.debug( "Deleting event group " + eventGroup + " from " + this )
		if( !eventGroup )
			return

		// Delete all subjectEventGroups pointing to this subjectGroup
		( [] + eventGroup.subjectEventGroups ).each {
			deleteSubjectEventGroup( it );
		}

		// Delete all eventsInEventGroup and samplingEventsInEventGroup from this group
		( [] + eventGroup.eventInstances ).each { deleteEventInEventGroup( it )	}
		( [] + eventGroup.samplingEventInstances ).each { deleteSamplingEventInEventGroup( it )	}
		
		removeFromEventGroups( eventGroup );

		eventGroup.delete()
	}

	void deleteEventInEventGroup( EventInEventGroup instance ) {
		log.debug( "Deleting event instance " + instance + " from " + this )
		if( !instance )
			return

		instance.event?.removeFromEventGroupInstances( instance )
		instance.eventGroup?.removeFromEventInstances( instance )
		instance.delete()
	}

	void deleteSamplingEventInEventGroup( SamplingEventInEventGroup instance ) {
		log.debug( "Deleting samplingevent instance " + instance + " from " + this )
		if( !instance )
			return

		instance.event?.removeFromEventGroupInstances( instance )
		instance.eventGroup?.removeFromSamplingEventInstances( instance )
		instance.delete()
	}


	/**
	 * Returns true if the given user is allowed to read this study
	 */
	public boolean canRead(SecUser loggedInUser) {
		// Public studies may be read by anyone
		if( this.publicstudy ) {
			return true;
		}

		// Anonymous readers are only given access when published and public
		if (loggedInUser == null) {
			return false;
		}

		// Administrators are allowed to read every study
		if (loggedInUser.hasAdminRights()) {
			return true;
		}

		// Owners and writers are allowed to read this study
		if (this.owner == loggedInUser || this.writers.contains(loggedInUser)) {
			return true
		}

		// Readers are allowed to read this study when it is published
		//		if (this.readers.contains(loggedInUser) && this.published) {
		if (this.readers.contains(loggedInUser)) {
			return true
		}

		return false
	}

	/**
	 * Returns true if the given user is allowed to write this study
	 */
	public boolean canWrite(SecUser loggedInUser) {
		if (loggedInUser == null) {
			return false;
		}

		// Administrators are allowed to write every study
		if (loggedInUser.hasAdminRights()) {
			return true;
		}

		return this.owner.username == loggedInUser.username || this.writers.username.contains(loggedInUser.username)
	}

	/**
	 * Returns true if the given user is the owner of this study
	 */
	public boolean isOwner(SecUser loggedInUser) {
		if (loggedInUser == null) {
			return false;
		}
		return this.owner == loggedInUser
	}

	/**
	 * Returns a list of studies that are writable for the given user
	 */
	public static giveWritableStudies(SecUser user, Integer max = null) {
		// User that are not logged in, are not allowed to write to a study
		if (user == null)
			return [];

		// Administrators are allowed to read everything
		if (user.hasAdminRights()) {
			def c = Study.createCriteria()
			return c.listDistinct {
				if (max != null) maxResults(max)
				order("title", "asc")

			}
		}

		def hqlString = "from Study s where s.publicstudy = true or s.owner = :user or :user in elements(s.writers) order by s.title asc"
		if (max)
			return Study.findAll(hqlString, [user: user], [max: max])
		else
			return Study.findAll(hqlString, [user: user])
	}

	/**
	 * Returns a list of studies that are readable by the given user
	 */
	public static giveReadableStudies(SecUser user, Integer max = null, int offset = 0) {

		// Administrators are allowed to read everything
		if (user == null) {
			def c = Study.createCriteria()
			return c.listDistinct {
				if (max != null) maxResults(max)
				firstResult(offset)
				order("title", "asc")
				and {
					//					eq("published", true)
					eq("publicstudy", true)
				}
			}
		} else if (user.hasAdminRights()) {
			def c = Study.createCriteria()
			return c.listDistinct {
				if (max != null) maxResults(max)
				firstResult(offset)
				order("title", "asc")
			}
		} else {
			def hqlString = "from Study s where s.publicstudy = true or s.owner = :user or :user in elements(s.readers) OR :user in elements(s.writers) order by s.title asc"
			if (max)
				return Study.findAll(hqlString, [user: user], [max: max, offset: offset])
			else
				return Study.findAll(hqlString, [user: user])
		}
	}

	/**
	 * perform a text search on studies
	 * @param query
	 * @return
	 */
	public static textSearchReadableStudies(SecUser user, String query) {

		if (user == null) {
			// regular user
			def c = Study.createCriteria()
			return c.listDistinct {
				or {
					ilike("title", "%${query}%")
					ilike("description", "%${query}%")
				}
				and {
					eq("published", true)
					eq("publicstudy", true)
				}
			}
		} else if (user.hasAdminRights()) {
			// admin can search everything
			def c = Study.createCriteria()
			return c.listDistinct {
				or {
					ilike("title", "%${query}%")
					ilike("description", "%${query}%")
				}
			}
		} else {
			return Study.findAll("from Study s where s.title like '%${query}%' or s.description like '%${query}%' and (s.publicstudy = true or s.owner = :user or :user in elements(s.readers) OR :user in elements(s.writers)) order by s.title asc", [user: user])
		}
	}

	/**
	 * Returns the number of public studies
	 * @return int
	 */
	public static countPublicStudies() { return countPublicStudies(true) }
	public static countPublicStudies(boolean published) {
		def c = Study.createCriteria()
		return (c.listDistinct {
			and {
				eq("published", published)
				eq("publicstudy", true)
			}
		}).size()
	}

	/**
	 * Returns the number of private studies
	 * @return int
	 */
	public static countPrivateStudies() { return countPrivateStudies(false) }
	public static countPrivateStudies(boolean published) {
		def c = Study.createCriteria()
		return (c.listDistinct {
			and {
				eq("publicstudy", false)
			}
			or {
				eq("published", published)
				eq("publicstudy", true)
			}
		}).size()
	}

	/**
	 * Returns the number of studies that are readable by the given user
	 */
	public static countReadableStudies(SecUser user) {
		def c = Study.createCriteria()

		// got a user?
		if (user == null) {
			return c.count {
				and {
					eq("published", true)
					eq("publicstudy", true)
				}
			}
		} else if (user.hasAdminRights()) {
			// Administrators are allowed to read everything
			return Study.count()
		} else {
			return Study.executeQuery("select count(*) from Study s where s.publicstudy = true or s.owner = :user or :user in elements(s.readers) OR :user in elements(s.writers)", [user: user])[0]
		}
	}

	/**
	 * Returns the number of studies that are readable & writable by the given user
	 */
	public static countReadableAndWritableStudies(SecUser user) {
		def c = Study.createCriteria()

		// got a user?
		if (user == null) {
			return 0
		} else if (user.hasAdminRights()) {
			return Study.count()
		} else {
			return Study.executeQuery("select count(*) from Study s where s.publicstudy = true or s.owner = :user or :user in elements(s.readers) OR :user in elements(s.writers)", [user: user])[0]
		}
	}

	/**
	 * Basic equals method to check whether objects are equals, by comparing the ids
	 * @param o		Object to compare with
	 * @return		True iff the id of the given Study is equal to the id of this Study
	 */
	public boolean equals( Object o ) {
		if( o == null )
			return false;

		if( !( o instanceof Study ) )
			return false

		Study s = (Study) o;

		return this.id == s.id
	}

	/**
	 * Returns the minimum and maximum date of the events of this study
	 * @return  A map containing absolute minDate and maxDate (not relative)
	 */
	def getMinMaxEventDate() {
		long minDate = Long.MAX_VALUE;
		long maxDate = Long.MIN_VALUE;
		this.events.each {
			if(it.startTime < minDate) {
				minDate = it.startTime;
			}
			if(it.endTime > maxDate) {
				maxDate = it.endTime;
			}
			if(it.startTime > maxDate) {
				maxDate = it.startTime;
			}
		}
		this.samplingEvents.each {
			if(it.startTime < minDate) {
				minDate = it.startTime;
			}
			if(it.startTime > maxDate) {
				maxDate = it.startTime;
			}
		}
		long lngStartDate  = (Long) this.startDate.getTime();
		return ["minDate" : new Date( lngStartDate + minDate * 1000 ), "maxDate" : new Date( lngStartDate + maxDate * 1000 )];
	}

	// This closure is used in the before{Insert,Update,Delete} closures below.
	// It is necessary to prevent flushing in the same session as a top level
	// database action such as 'save' or 'addTo...'. This confuses hibernate and
	// produces hard to trace errors.
	// The same holds for flushing during validation (but that's not the case
	// here).
	// http://grails.1312388.n4.nabble.com/Grails-hibernate-flush-causes-IndexOutOfBoundsException-td3031979.html
	static manualFlush(closure) {
		withSession {session ->
			def save
			try {
				save = session.flushMode
				session.flushMode = org.hibernate.FlushMode.MANUAL
				closure()
			} finally {
				if (save) {
					session.flushMode = save
				}
			}
		}
	}

	// Send messages to modules about changes in this study
	def beforeInsert = {
		manualFlush{
			moduleNotificationService?.invalidateStudy( this )
		}
	}
	def beforeUpdate = {
		manualFlush{
			moduleNotificationService?.invalidateStudy( this )
		}
	}
	def beforeDelete = {
		manualFlush{
			moduleNotificationService?.invalidateStudy( this )
		}
	}

	/**
	 * return the unique species
	 * @see dbnp.query.StudyCompareController
	 */
	def uniqueSpecies = {
		return subjects.collect{ it.species }.unique()
	}

	/**
	 * return the unique event templates
	 * @see dbnp.query.StudyCompareController
	 */
	def uniqueEventTemplates = {
		return events.collect{ it.template }.unique()
	}

	/**
	 * return the unique sampling event templates
	 * @see dbnp.query.StudyCompareController
	 */
	def uniqueSamplingEventTemplates = {
		return samplingEvents.collect{ it.template }.unique()
	}

	/**
	 * return the unique assay modules
	 * @see dbnp.query.StudyCompareController
	 */
	def uniqueAssayModules = {
		return assays.collect{ it.module }.unique()
	}

	public String viewUrl() {
		throw new UnsupportedOperationException("Not implemented");
	}
}
