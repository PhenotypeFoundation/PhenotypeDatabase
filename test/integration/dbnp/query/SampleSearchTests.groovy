package dbnp.query

import dbnp.studycapturing.*
import dbnp.data.*
import dbnp.authentication.*;
import grails.test.*

/**
 * SearchTests Test
 *
 * Description of my test
 *
 * @author  your email (+name?)
 * @since	2010mmdd
 * @package	???
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class SampleSearchTests extends GroovyTestCase {
    protected void setUp() {
        super.setUp()

		def owner = SecUser.findByUsername( "user" );
		
		// Look up the used ontologies which should be in the database by now
		def speciesOntology				= Ontology.getOrCreateOntologyByNcboId(1132)

		// Look up the used templates which should also be in the database by now
		def studyTemplate				= Template.findByName("Academic study")
		def mouseTemplate				= Template.findByName("Mouse")
		def dietTreatmentTemplate		= Template.findByName("Diet treatment")
		def liverSamplingEventTemplate	= Template.findByName("Liver extraction")
		def humanTissueSampleTemplate	= Template.findByName("Human tissue sample")
		def ccAssayTemplate				= Template.findByName("Clinical chemistry assay")

		// Add terms manually, to avoid having to do many HTTP requests to the BioPortal website
		def mouseTerm = Term.findByName( "Mus musculus" );

		// Add example mouse study
		def testStudy = new Study(
			template	: studyTemplate,
			title		: "TestStudy 1",
			description	: "C57Bl/6 mice were fed a high fat (45 en%) or low fat (10 en%) diet after a four week run-in on low fat diet.",
			code		: "TESTPPS3_leptin_module",
			researchQuestion: "Leptin etc.",
			ecCode		: "2007117.c",
			startDate	: Date.parse('yyyy-MM-dd', '2008-01-02'),
			owner		: owner
		).with { if (!validate()) { errors.each { println it} } else save()}

		def evLF = new Event(
			startTime	: 3600,
			endTime		: 3600 + 7 * 24 * 3600,
			template	: dietTreatmentTemplate
		).setFieldValue('Diet', 'low fat')

		def evS = new SamplingEvent(
			startTime	: 3600,
			template	: liverSamplingEventTemplate,
			sampleTemplate: humanTissueSampleTemplate).setFieldValue('Sample weight', 5F)
		// Add events to study
		testStudy.addToEvents(evLF).addToSamplingEvents(evS).with { if (!validate()) { errors.each { println it} } else save()}

		// Extra check if the SamplingEvents are saved correctly
		evS.with { if (!validate()) { errors.each { println it} } else save()}

		def LFBV1 = new EventGroup(name: "10% fat + vehicle for 1 week").addToEvents(evLF).addToSamplingEvents(evS)

		// Add subjects and samples and compose EventGroups
		def x = 1
		5.times {
			def currentSubject = new Subject(
				name: "TestA" + x++,
				species: mouseTerm,
				template: mouseTemplate,
			).setFieldValue("Gender", "Male")

			// We have to save the subject first, otherwise the parentEvent property of the sample cannot be set
			// (this is possibly a Grails or Hibernate bug)
			testStudy.addToSubjects(currentSubject)
			currentSubject.with { if (!validate()) { errors.each { println it} } else save()}

			// Add subject to appropriate EventGroup
			LFBV1.addToSubjects(currentSubject).save()

			// Create sample
			def currentSample = new Sample(
				name: currentSubject.name + '_B',
				material: mouseTerm,
				template: humanTissueSampleTemplate,
				parentSubject: currentSubject,
				parentEvent: evS
			);
			testStudy.addToSamples(currentSample)
			currentSample.with { if (!validate()) { errors.each { println it} } else save()}
			currentSample.setFieldValue("Text on vial", "T" + (Math.random() * 100L))
		}

		// Add EventGroups to study
		testStudy.addToEventGroups(LFBV1).with { if (!validate()) { errors.each { println it} } else save()}

		// Add SAM assay reference
		def clinicalModule = AssayModule.findByName( "SAM module for clinical data" );

		def testAssayRef = new Assay(
			name: 'Test Lipid profiling',
			template: ccAssayTemplate,
			module: clinicalModule,
			externalAssayID: 'TestPPS3_SAM'
		)

		testStudy.samples*.each {
			testAssayRef.addToSamples(it)
		}

		testStudy.addToAssays(testAssayRef);
		testStudy.save()

		assert Sample.list()*.name.contains( "TestA2_B" );
    }

    protected void tearDown() {
        super.tearDown()
    }

	void testExecute() {

		List criteria = [
			new Criterion( entity: "Sample", field: "name", operator: Operator.contains, value: "B" ),
			new Criterion( entity: "Study", field: "code", operator: Operator.equals, value: "TESTPPS3_leptin_module" ),
			new Criterion( entity: "Study", field: "code", operator: Operator.equals, value: "PPS3_leptin_module" )
		]
		
		def search = new SampleSearch();

		search.setCriteria( [ criteria[0] ] );
		search.execute();
		assert search.getResults().size() >= 2
		
		assert search.getResults()[0] instanceof Sample
		assert search.getResults()*.name.contains( "TestA2_B" );
		assert search.getResults()*.name.contains( "TestA4_B" );
		
		search.setCriteria( [ criteria[0], criteria[1] ] );
		search.execute();
		assert search.getResults().size() >= 1
		assert search.getResults()*.name.contains( "TestA2_B" );
		
		search.setCriteria( [ criteria[0], criteria[2] ] );
		search.execute();
		assert search.getResults().size() >= 1
		
		// Conflicting criteria shouldn't return any samples
		search.setCriteria( [ criteria[2], criteria[1] ] );
		search.execute();
		assert search.getResults().size() == 0
	}
	
	void testExecuteDifferentCriteria() {
		List criteria = [
			new Criterion( entity: "Study", field: "title", operator: Operator.contains, value: "TestStudy 1" ),
			new Criterion( entity: "Subject", field: "name", operator: Operator.contains, value: "Test" ),
			new Criterion( entity: "Sample", field: "name", operator: Operator.contains, value: "Test" ),
			new Criterion( entity: "Assay", field: "name", operator: Operator.contains, value: "Test" ),
			new Criterion( entity: "Event", field: "startTime", operator: Operator.equals, value: "3600" ),
			new Criterion( entity: "SamplingEvent", field: "startTime", operator: Operator.equals, value: "3600" ),
		]
		
		def search = new Search();

		// All criteria should result in 1 sample with code 'abc'
		criteria.each {
			println "Criterion " + it
			search.setCriteria( [ it ] );
			search.execute();
			assert search.getResults().size() >= 2
			assert search.getResults()[0] instanceof Sample;
			assert search.getResults()*.name.contains( "TestA2_B" );
		}
	}

	void testExecuteNonExistingCriteria() {
		List criteria = [
			new Criterion( entity: "Study", field: "title", operator: Operator.contains, value: "This shouldn't exist" ),
			new Criterion( entity: "Subject", field: "name", operator: Operator.contains, value: "This shouldn't exist" ),
			new Criterion( entity: "Sample", field: "name", operator: Operator.contains, value: "This shouldn't exist" ),
			new Criterion( entity: "Assay", field: "name", operator: Operator.contains, value: "This shouldn't exist" ),
			new Criterion( entity: "Event", field: "startTime", operator: Operator.equals, value: "481920" ),
			new Criterion( entity: "SamplingEvent", field: "startTime", operator: Operator.equals, value: "0192039" ),
		]
		
		def search = new SampleSearch();

		// All criteria should result in 1 study with code 'abc'
		criteria.each {
			println "Criterion " + it
			search.setCriteria( [ it ] );
			search.execute();
			assert search.getResults().size() == 0
		}
	}

}
