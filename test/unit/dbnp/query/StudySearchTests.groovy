package dbnp.query

import dbnp.studycapturing.*
import grails.test.*
import org.dbnp.gdt.AssayModule
import org.codehaus.groovy.grails.commons.ApplicationHolder

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
class StudySearchTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
		
		def studies = [ new Study( title: 'Study TNO-1', code: 'abc' ), new Study( title: 'Study TNO-2', code: 'def' ), new Study( title: 'Study NMC-1', code: 'ghi' ), new Study( title: 'Study NMC-2', code: 'jkl' ) ];
		def subjects = [ new Subject( name: 'Subject 1', parent: studies[0] ), new Subject( name: 'Subject 2', parent: studies[1] ) ]
		def events = [ new Event( startTime: 3600, parent: studies[0] ), new Event( startTime: 7200, parent: studies[1] ) ]
		def samplingEvents = [ new SamplingEvent( startTime: 3600, parent: studies[0] ), new SamplingEvent( startTime: 7200, parent: studies[1] ) ]
		def samples = [
				new Sample( name: 'Sample 1', parent: studies[0], parentSubject: subjects[ 0 ], parentEvent: samplingEvents[ 0 ] ),
				new Sample( name: 'Sample 2', parent: studies[1], parentSubject: subjects[ 1 ], parentEvent: samplingEvents[ 1 ] )
		]
		def assays = [ new Assay( name: 'Assay 1', parent: studies[0], samples: [samples[0]] ), new Assay( name: 'Assay 2', parent: studies[1], samples: [samples[1]] ) ]
		
		mockDomain( Study, studies );
		mockDomain( Subject, subjects );
		mockDomain( Sample, samples );
		mockDomain( Event, events );
		mockDomain( SamplingEvent, samplingEvents);
		mockDomain( Assay, assays );

        mockDomain( AssayModule );
		
		subjects.each { it.parent.addToSubjects( it ); }
		samples.each { it.parent.addToSamples( it ); }
		events.each { it.parent.addToEvents( it ); }
		samplingEvents.each { it.parent.addToSamplingEvents( it ); }
		samples.each { it.parent.addToSamples( it ); }
		assays.each { it.parent.addToAssays( it ); }

        // some mocks to make sure test doesn't break on finding 'moduleCommunicationService'
        ApplicationHolder.metaClass.static.getApplication = { [getMainContext: { [getBean: {a -> null}] }] }
		
    }

    protected void tearDown() {
        super.tearDown()
    }

	void testExecute() {

		List criteria = [
			new Criterion( entity: "Study", field: "title", operator: Operator.contains, value: "TNO" ),
			new Criterion( entity: "Study", field: "code", operator: Operator.equals, value: "abc" ),
			new Criterion( entity: "Study", field: "code", operator: Operator.equals, value: "ghi" )
		]
		
		def studySearch = new StudySearch();

		// Search without criteria
		studySearch.setCriteria( );
		studySearch.execute();
		
		assert studySearch.getResults().size() == 4
		assert studySearch.getResults()[0] instanceof Study
		assert studySearch.getResults()*.code.contains( "abc" );
		assert studySearch.getResults()*.code.contains( "def" );
		assert studySearch.getResults()*.code.contains( "ghi" );
		assert studySearch.getResults()*.code.contains( "jkl" );

		studySearch.setCriteria( [ criteria[0] ] );
		studySearch.execute();
		assert studySearch.getResults().size() == 2
		
		assert studySearch.getResults()*.code.contains( "abc" );
		assert studySearch.getResults()*.code.contains( "def" );
		
		studySearch.setCriteria( [ criteria[0], criteria[1] ] );
		studySearch.execute();
		assert studySearch.getResults().size() == 1
		assert studySearch.getResults()[0].code == "abc"
		
		studySearch.setCriteria( [ criteria[0], criteria[2] ] );
		studySearch.execute();
		assert studySearch.getResults().size() == 0

		studySearch.setCriteria( [ criteria[1], criteria[2] ] );
		studySearch.execute();
		assert studySearch.getResults().size() == 0
	}
	
	void testExecuteDifferentCriteria() {
		List criteria = [
			new Criterion( entity: "Study", field: "title", operator: Operator.contains, value: "TNO-1" ),
			new Criterion( entity: "Subject", field: "name", operator: Operator.contains, value: "1" ),
			new Criterion( entity: "Sample", field: "name", operator: Operator.contains, value: "1" ),
			new Criterion( entity: "Assay", field: "name", operator: Operator.contains, value: "1" ),
			new Criterion( entity: "Event", field: "startTime", operator: Operator.equals, value: "3600" ),
			new Criterion( entity: "SamplingEvent", field: "startTime", operator: Operator.equals, value: "3600" ),
		]
		
		def studySearch = new StudySearch();

		// All criteria should result in 1 study with code 'abc'
		criteria.each {
			println "Criterion " + it
			studySearch.setCriteria( [ it ] );
			studySearch.execute();
			assert studySearch.getResults().size() == 1
			assert studySearch.getResults()[0].code == "abc";
		}
	}

	void testExecuteNonExistingCriteria() {
		List criteria = [
			new Criterion( entity: "Study", field: "title", operator: Operator.contains, value: "TNO-3" ),
			new Criterion( entity: "Subject", field: "name", operator: Operator.contains, value: "4" ),
			new Criterion( entity: "Sample", field: "name", operator: Operator.contains, value: "5" ),
			new Criterion( entity: "Assay", field: "name", operator: Operator.contains, value: "6" ),
			new Criterion( entity: "Event", field: "startTime", operator: Operator.equals, value: "4800" ),
			new Criterion( entity: "SamplingEvent", field: "startTime", operator: Operator.equals, value: "360" ),
		]
		
		def studySearch = new StudySearch();

		// All criteria should result in 1 study with code 'abc'
		criteria.each {
			println "Criterion " + it
			studySearch.setCriteria( [ it ] );
			studySearch.execute();
			assert studySearch.getResults().size() == 0
		}
	}
}
