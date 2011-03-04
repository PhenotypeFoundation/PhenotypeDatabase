package dbnp.query

import dbnp.authentication.SecUser
import dbnp.studycapturing.*
import dbnp.configuration.ExampleStudies

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

        ExampleStudies.addExampleStudies(SecUser.findByUsername('user'), SecUser.findByUsername('admin'))
    }

    protected void tearDown() {
        super.tearDown()
    }

	void testExecute() {

		List criteria = [
			new Criterion( entity: "Sample", field: "name", operator: Operator.contains, value: "B" ),
			new Criterion( entity: "Study", field: "code", operator: Operator.equals, value: "PPS3_leptin_module" ),
			new Criterion( entity: "Study", field: "code", operator: Operator.equals, value: "PPSH" )
		]

		def search = new SampleSearch();

		// Make sure the 'user' is logged in
		search.user = SecUser.findByUsername('user');

		search.setCriteria( [ criteria[0] ] );

        def a = Sample.findAll()

		search.execute();
		assert search.getResults().size() >= 2

		assert search.getResults()[0] instanceof Sample
		assert search.getResults()*.name.contains( "A2_B" );
		assert search.getResults()*.name.contains( "A4_B" );

		search.setCriteria( [ criteria[0], criteria[1] ] );
		search.execute();
		assert search.getResults().size() >= 1
		assert search.getResults()*.name.contains( "A2_B" );

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
			new Criterion( entity: "Study", field: "title", operator: Operator.contains, value: "NuGO PPS3 mouse study" ),
			new Criterion( entity: "Subject", field: "species", operator: Operator.equals, value: "Mus musculus" ),
			new Criterion( entity: "Sample", field: "name", operator: Operator.contains, value: "A" ),
			new Criterion( entity: "Assay", field: "name", operator: Operator.contains, value: "Lipid" ),
			//new Criterion( entity: "Event", field: "startTime", operator: Operator.equals, value: "3600" ),
			//new Criterion( entity: "SamplingEvent", field: "startTime", operator: Operator.equals, value: 3600 + 7 * 24 * 3600 ),
		]

		def search = new SampleSearch();

		// Make sure the 'user' is logged in
		search.user = SecUser.findByUsername('user');

		// All criteria should result in 1 sample with code 'abc'
		criteria.each {
			println "Criterion " + it
			search.setCriteria( [ it ] );
			search.execute();

			def results = search.getResults();
			assert results;
			assert results.size() >= 2
			assert results[0] instanceof Sample;
			assert results*.name.contains( "A2_B" );
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

		// Make sure the 'user' is logged in
		search.user = SecUser.findByUsername('user');

		// All criteria should result in 1 study with code 'abc'
		criteria.each {
			println "Criterion " + it
			search.setCriteria( [ it ] );
			search.execute();
			assert search.getResults().size() == 0
		}
	}

}