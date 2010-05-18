package gscf

import grails.test.*
import dbnp.data.*


/**
 * OntologyTests Test
 *
 * Test ontology/term functionality on domain model level
 *
 * @author keesvb
 * @since 20100510
 * @package dbnp.studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class OntologyTests extends GrailsUnitTestCase {

	final String testOntologyName = "Test ontology"
	final String testOntologyDescription = "Test description"
	final String testOntologyUrl = "http://www.test.com"
	final String testOntologyVersionNumber = "1.0"
	final int testOntologyNcboId = 0
	final int testOntologyNcboVersionedId = 0
	final String testTermName = "Test term"
	final String testAccession = 'TEST01234$'

	protected void setUp() {
		super.setUp()

		def ontology = new Ontology(
				name: testOntologyName,
				description: testOntologyDescription,
				url: testOntologyUrl,
				versionNumber: testOntologyVersionNumber,
				ncboId: testOntologyNcboId,
				ncboVersionedId: testOntologyNcboVersionedId
		);

		// Validate and save ontology
		assert ontology.validate()
		assert ontology.save(flush:true)
	}

	protected void tearDown() {
		super.tearDown()
	}

	/**
	 * Test if ontology was properly saved
	 */
	void testSave () {

		// Try to retrieve the ontology and make sure it's the same
		def ontologyDB = Ontology.findByName(testOntologyName)
		assert ontologyDB
		assert ontologyDB.name.equals(testOntologyName)
		assert ontologyDB.description.equals(testOntologyDescription)
		assert ontologyDB.url.equals(testOntologyUrl)
		assert ontologyDB.versionNumber.equals(testOntologyVersionNumber)
		assert ontologyDB.ncboId.equals(testOntologyNcboId)
		assert ontologyDB.ncboVersionedId.equals(testOntologyNcboVersionedId)

	}


	/**
	* Test saving and retrieving a term within the ontology and test giveTermByName(name)
	*/
	void testTermSave() {

		// Find created ontology
		def testOntology = Ontology.findByName(testOntologyName)
		assert testOntology

		// Create a new term
		def term = new Term(
			name: testTermName,
			accession: testAccession,
			ontology: testOntology
		)

		assert term.validate()
		assert term.save(flush:true)

		// Try to retrieve the term from the ontology and make sure it's the same
		def termDB = testOntology.giveTermByName(testTermName)
		assert termDB.name.equals(testTermName)
		assert termDB.accession.equals(testAccession)
		assert termDB.ontology == testOntology
	}


	/**
	* Test giveTerms() method
	*/
	void testGiveTerms() {

		// Find created ontology
		def testOntology = Ontology.findByName(testOntologyName)
		assert testOntology

		def terms = testOntology.giveTerms()
		assert terms
		assert terms.size() == 1
		assert terms.asList().first().name.equals(testTermName)
	}


	/**
 	* Ontocat test (Ontocat example 1)
 	*
	* Shows how to list all the available ontologies in OLS
 	*
 	*/
	void testOntocat() {
		// Instantiate OLS service
		uk.ac.ebi.ontocat.OntologyService os = new uk.ac.ebi.ontocat.bioportal.BioportalOntologyService()
		// For all ontologies in OLS print their
		// full label and abbreviation
		uk.ac.ebi.ontocat.Ontology o = os.getOntology("1005")
			StringBuilder sb = new StringBuilder();
			sb.append(o.getAbbreviation());
			sb.append("\t");
			sb.append(o.getLabel());
			sb.append("\t");
			sb.append(o.getOntologyAccession());
			sb.append("\t");
			o.getVersionNumber() + o.getMetaPropertyValues().each {
				sb.append(it.dump())
			}
			System.out.println(sb.toString());
		
	}

}
