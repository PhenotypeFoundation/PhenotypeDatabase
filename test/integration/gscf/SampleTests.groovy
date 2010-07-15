package gscf

import dbnp.studycapturing.Study
import dbnp.studycapturing.Template
import grails.test.GrailsUnitTestCase
import dbnp.studycapturing.SamplingEvent
import dbnp.studycapturing.Sample

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

	final String testSampleName = "Test sample"
	final String testSampleTemplateName = "Human blood sample"

	final String testSamplingEventName = "Test sampling event"
	final String testSamplingEventTemplateName = "Blood extraction"
	final long testSamplingEventTime = 34534534L


	protected void setUp() {
		super.setUp()

		// Retrieve the study that should have been created in StudyTests
		def study = Study.findByTitle(testStudyName)
		assert study

		// Look up sampling event template
		def samplingEventTemplate = Template.findByName(testSamplingEventTemplateName)
		assert samplingEventTemplate

		// Create parent sampling event
		def samplingEvent = new SamplingEvent(
			startTime: testSamplingEventTime,
			endTime: testSamplingEventTime,
			template: samplingEventTemplate
		)

		if (!samplingEvent.validate()) {
			samplingEvent.errors.each { println it}
		}
		assert samplingEvent.validate()

		assert samplingEvent.save(flush:true)

		// Look up sample template
		def sampleTemplate = Template.findByName(testSampleTemplateName)
		assert sampleTemplate

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

		// A sample without a name should not be saveable
		sampleDB.name = null
		assert !sampleDB.validate()

		// A sample without a parent SamplingEvent should not be saveable
		sampleDB.name = testSampleName
		sampleDB.parentEvent = null
		assert !sampleDB.validate()
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
		    template: sampleTemplate,
		    parentEvent: samplingEvent
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

	}

	protected void tearDown() {
		super.tearDown()
	}

}