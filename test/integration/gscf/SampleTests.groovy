package gscf

import dbnp.studycapturing.Study
import dbnp.studycapturing.Template
import grails.test.GrailsUnitTestCase
import dbnp.studycapturing.SamplingEvent
import dbnp.studycapturing.Sample
import dbnp.studycapturing.TemplateFieldType
import dbnp.studycapturing.Subject
import dbnp.studycapturing.EventGroup

/**
 * Test the creation of a Sample and its TemplateEntity functionality on data model level
 *
 * @author keesvb
 * @since 20100511
 * @package dbnp.studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

class SampleTests extends StudyTests {

	// This test extends StudyTests, so that we have a test study to assign as a parent study

	final String testSampleName = "Test sample @XYZ!"
	final String testSampleTemplateName = "Human blood sample"

	final String testSamplingEventName = "Test sampling event"
	final String testSamplingEventTemplateName = "Blood extraction"
	final long testSamplingEventTime = 34534534L
	final long testSamplingEventDuration = 1000L
	final String testEventGroupName = "Test Group"


	protected void setUp() {
		super.setUp()

		// Retrieve the study that should have been created in StudyTests
		def study = Study.findByTitle(testStudyName)
		assert study

		// Look up sample template
		def sampleTemplate = Template.findByName(testSampleTemplateName)
		assert sampleTemplate

		// Create sample with the retrieved study as parent
		def sample = new Sample(
		    name: testSampleName,
		    template: sampleTemplate
		)

		// At this point, the sample should not validate, because it doesn't have a parent study assigned
		assert !sample.validate()

		// Add the sample to the retrieved parent study
		study.addToSamples(sample)
		assert study.samples.find { it.name == sample.name}

		// Now, the sample should validate
		if (!sample.validate()) {
			sample.errors.each { println it}
		}
		assert sample.validate()

		// Make sure the sample is saved to the database
		assert sample.save(flush: true)

	}

	private void addParentSamplingEvent() {

		// Retrieve the sample
		def sampleDB = Sample.findByName(testSampleName)
		assert sampleDB

		// Retrieve the study that should have been created in StudyTests
		def study = Study.findByTitle(testStudyName)
		assert study

		// Look up sampling event template
		def samplingEventTemplate = Template.findByName(testSamplingEventTemplateName)
		assert samplingEventTemplate

		// Create parent sampling event
		def samplingEvent = new SamplingEvent(
			startTime: testSamplingEventTime,
			duration: testSamplingEventDuration,
			template: samplingEventTemplate,
			sampleTemplate: Template.findByName(testSampleTemplateName)
		)

		// The SamplingEvent should not validate at this point because it doesn't have a parent study
		assert !samplingEvent.validate()

		study.addToSamplingEvents(samplingEvent)
		// It should do fine now
		assert samplingEvent.validate()
		assert samplingEvent.save(flush:true)

		// Add sample to the sampling event
		samplingEvent.addToSamples(sampleDB)

		// Make sure the sampling event is really the parent event of the sample
		assert sampleDB.parentEvent
		assert sampleDB.parentEvent == samplingEvent
		assert sampleDB.parentEvent.startTime.equals(testSamplingEventTime)

	}

	private void addParentSubject() {

		def sampleDB = Sample.findByName(testSampleName)
		assert sampleDB

		// Retrieve the parent study
		def study = Study.findByTitle(testStudyName)
		assert study

		def subject = SubjectTests.createSubject(study)
		assert subject

		sampleDB.parentSubject = subject
		assert sampleDB.validate()
		assert sampleDB.save()

	}

	/**
	 * Test whether a study which has orphan (without parent subject/event) samples cannot be published
	 */
	void testStudyPublish() {
		def sampleDB = Sample.findByName(testSampleName)
		assert sampleDB

		// Retrieve the parent study
		def study = Study.findByTitle(testStudyName)
		assert study

		// Make sure the study validates at this point
		assert study.validate()

		// Try to publish the study, should fail as it has a sample without a parent sampling event
		study.published = true
		assert !study.validate()

		// Add parent sampling event
		addParentSamplingEvent()

		// Add parent subject
		addParentSubject()

		// Now the study should validate
		assert study.validate()
	}

	void testSave() {

		// Try to retrieve the sample and make sure it's the same
		def sampleDB = Sample.findByName(testSampleName)
		assert sampleDB
		assert sampleDB.name.equals(testSampleName)
		assert sampleDB.template.name.equals(testSampleTemplateName)

		println "The sample has parentEvent ${sampleDB.parentEvent.encodeAsHTML()}"
		// A sample without a name should not be saveable
		sampleDB.name = null
		assert !sampleDB.validate()

	}

	void testDelete() {
		def sampleDB = Sample.findByName(testSampleName)
		assert sampleDB
		sampleDB.delete()
		try {
			sampleDB.save()
			assert false // The save should not succeed since the sample is referenced by a study
		}
		catch(org.springframework.dao.InvalidDataAccessApiUsageException e) {
			sampleDB.discard()
			assert true // OK, correct exception (at least for the in-mem db, for PostgreSQL it's probably a different one...)
		}

		// Now, delete the sample from the study samples collection, and then the delete action should be cascaded to the sample itself
		def study = Study.findByTitle(testStudyName)
		assert study
		study.removeFromSamples sampleDB

		// Make sure the sample doesn't exist anymore at this point
		assert !Sample.findByName(testSampleName)
		assert Sample.count() == 0
		assert study.samples.size() == 0
	}

	void testDeleteViaParentSubject() {

		def sampleDB = Sample.findByName(testSampleName)
		assert sampleDB

		// Retrieve the parent study
		def study = Study.findByTitle(testStudyName)
		assert study

		// Add parent subject
		addParentSubject()
		Subject subject = sampleDB.parentSubject

		// Use the deleteSubject method
		def msg = study.deleteSubject(subject)
		println msg
		assert study.save()

		assert !study.subjects.contains(subject)

		assert !Subject.findByName(subject.name)
		assert !Sample.findByName(testSampleName)

		assert Subject.count() == 0
		assert Sample.count() == 0

	}

	void testDeleteViaParentSamplingEvent() {

		def sampleDB = Sample.findByName(testSampleName)
		assert sampleDB

		// Retrieve the parent study
		def study = Study.findByTitle(testStudyName)
		assert study

		// Add parent sampling event
		addParentSamplingEvent()

		def event = sampleDB.parentEvent
		assert event

		// Use the deleteSamplingEvent method
		def msg = study.deleteSamplingEvent(event)
		println msg
		assert study.save()

		assert !study.samplingEvents.contains(event)

		assert !SamplingEvent.findByStartTime(testSamplingEventTime)
		assert !Sample.findByName(testSampleName)

		assert SamplingEvent.count() == 0
		assert Sample.count() == 0

	}

	void testDeleteViaParentEventGroup() {

		def sampleDB = Sample.findByName(testSampleName)
		assert sampleDB

		// Retrieve the parent study
		def study = Study.findByTitle(testStudyName)
		assert study

		// Retrieve the sample's sampling event
		addParentSamplingEvent()
		def event = sampleDB.parentEvent
		assert event

		// Add parent subject
		addParentSubject()
		Subject subject = sampleDB.parentSubject

		// Create an event group in this study with the sample's sampling event and subject
		def group = new EventGroup(
		    name: testEventGroupName
		)
		study.addToEventGroups(group)
		assert group.validate()

		group.addToSubjects(subject)
		group.addToSamplingEvents(event)

		assert study.eventGroups.find { it.name == group.name}
		assert group.validate()
		assert study.save()

		// Use the deleteEventGroup method
		def msg = study.deleteEventGroup(group)
		println msg
		assert Sample.count() == 0 // trigger (if any) e.ObjectDeletedException: deleted object would be re-saved by cascade (remove deleted object from associations)
		if (!study.validate()) {
			study.errors.each { println it}
		}
		assert study.validate()

		assert study.save()

		assert !study.eventGroups.contains(group)
		assert !EventGroup.findByName(testEventGroupName)
		assert !Sample.findByName(testSampleName)

		assert EventGroup.count() == 0
		assert Sample.count() == 0

	}

	void testStudyRelation() {
		// Retrieve the parent study
		def study = Study.findByTitle(testStudyName)
		assert study

		// Test giveSampleTemplates
		def templates = study.giveSampleTemplates()
		assert templates
		assert templates.size() == 1
		assert templates.asList().first().name == testSampleTemplateName

		// Test if the sample is in the samples collection
		assert study.samples
		assert study.samples.size() == 1
		assert study.samples.first().name == testSampleName
	}


	void testParentStudy() {
		def sample = Sample.findByName(testSampleName)
		assert sample

		assert sample.parent
		assert sample.parent.code == testStudyCode
	}

	void testSampleUniqueNameConstraint() {
		def sample = Sample.findByName(testSampleName)
		assert sample

		def study = sample.parent
		assert study

		def sample2 = new Sample(
		    name: testSampleName,
		    template: sample.template,
		    parentEvent: sample.parentEvent
		)

		// Add the sample to the retrieved parent study
		study.addToSamples(sample2)

		// At this point, the sample should not validate or save, because there is already a sample with that name in the study
		assert !sample2.validate()
		assert !sample2.save(flush:true)

	}

	/**
	 * Test whether it's indeed not possible to add two yet-to-be-saved samples with the same name to a yet-to-be-saved study
	 */
	void testSampleUniqueNameConstraintAtValidate() {
		def sample = Sample.findByName(testSampleName)
		assert sample

		def study = sample.parent
		assert study

		def sample1 = new Sample(
		    name: testSampleName + "-double",
		    template: sample.template,
		    parentEvent: sample.parentEvent
		)

		def sample2 = new Sample(
			name: testSampleName + "-double",
		    template: sample.template,
		    parentEvent: sample.parentEvent
		)

		// Add the sample to the retrieved parent study
		study.addToSamples(sample1)
		study.addToSamples(sample2)

		// At this point, the sample should not validate or save, because there is already a sample with that name in the study
		assert !sample1.validate()
		assert !sample1.save(flush:true)

		assert !sample2.validate()
		assert !sample2.save(flush:true)
	}

	void testFindViaSamplingEvent() {

		// Add parent sampling event
		addParentSamplingEvent()
		
		// Try to retrieve the sampling event by using the time...
		// (should be also the parent study but that's not yet implemented)
		def samplingEventDB = SamplingEvent.findByStartTime(testSamplingEventTime)
		assert samplingEventDB

		def samples = samplingEventDB.getSamples()
		assert samples
		assert samples.size() == 1
		assert samples.every {it.name == testSampleName}
	}

	void testDomainFields() {
		def sample = Sample.findByName(testSampleName)
		assert sample

		// Make sure the domain fields exist
		assert sample.fieldExists('name')
		assert sample.fieldExists('material')

		// Make sure they are domain fields
		assert sample.isDomainField('name')
		assert sample.isDomainField('material')

		// Make sure that they have the right type
		assert sample.giveFieldType('name') == TemplateFieldType.STRING
		assert sample.giveFieldType('material') == TemplateFieldType.ONTOLOGYTERM

	}

	protected void tearDown() {
		super.tearDown()
	}

}