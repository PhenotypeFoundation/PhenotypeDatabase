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

		// Look up sampling event template
		def samplingEventTemplate = Template.findByName(testSamplingEventTemplateName)
		assert samplingEventTemplate

		// Look up sample template
		def sampleTemplate = Template.findByName(testSampleTemplateName)
		assert sampleTemplate

		// Create parent sampling event
		def samplingEvent = new SamplingEvent(
			startTime: testSamplingEventTime,
			duration: testSamplingEventDuration,
			template: samplingEventTemplate,
			sampleTemplate: sampleTemplate
		)

		if (!samplingEvent.validate()) {
			samplingEvent.errors.each { println it}
		}
		// The SamplingEvent should not validate at this point because it doesn't have a parent study
		assert !samplingEvent.validate()

		study.addToSamplingEvents(samplingEvent)
		// It should do fine now
		assert samplingEvent.validate()
		assert samplingEvent.save(flush:true)

		// Create sample with the retrieved study as parent
		def sample = new Sample(
		    name: testSampleName,
		    template: sampleTemplate,
		    parentEvent: samplingEvent
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

	void testSave() {

		// Try to retrieve the sample and make sure it's the same
		def sampleDB = Sample.findByName(testSampleName)
		assert sampleDB
		assert sampleDB.name.equals(testSampleName)
		assert sampleDB.template.name.equals(testSampleTemplateName)
		assert sampleDB.parentEvent
		assert sampleDB.parentEvent.startTime.equals(testSamplingEventTime)

		println "The sample has parentEvent ${sampleDB.parentEvent.encodeAsHTML()}"
		// A sample without a name should not be saveable
		sampleDB.name = null
		assert !sampleDB.validate()

		// A sample without a parent SamplingEvent should not be saveable
		sampleDB.name = testSampleName
		sampleDB.parentEvent = null
		assert !sampleDB.validate()
	}

	void testDelete() {
		def sampleDB = Sample.findByName(testSampleName)
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

		def subject = SubjectTests.createSubject(study)
		assert subject

		sampleDB.parentSubject = subject
		assert sampleDB.validate()
		assert sampleDB.save()

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
		def event = sampleDB.parentEvent
		assert event

		// Create a subject and add it at the sample's parent
		def subject = SubjectTests.createSubject(study)
		assert subject
		sampleDB.parentSubject = subject
		assert sampleDB.validate()
		assert sampleDB.save()

		// Create an event group in this study with the sample's sampling event
		def group = new EventGroup(
		    name: testEventGroupName
		)
		study.addToEventGroups(group)
		group.addToSubjects(subject)
		group.addToSamplingEvents(event)
		assert study.eventGroups.find { it.name == group.name}
		assert group.validate()
		assert study.save()

		// Use the deleteSamplingEvent method
		def msg = study.deleteEventGroup(group)
		println msg
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

	void testFindViaSamplingEvent() {
		// Try to retrieve the sampling event by using the time...
		// (should be also the parent study but that's not yet implemented)
		def samplingEventDB = SamplingEvent.findByStartTime(testSamplingEventTime)
		assert samplingEventDB

		def samples = samplingEventDB.getSamples()
		assert samples
		assert samples.size() == 1
		assert samples.first().name == testSampleName
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