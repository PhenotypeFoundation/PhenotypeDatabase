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
	}

	protected void tearDown() {
		super.tearDown()
	}

	/**
	 * Test creation and saving of ontologies and terms
	 * and giveTerms and giveTermByName methods
	 */
	void testAll () {

		// === TEST Ontology

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

		// Try to retrieve the ontology and make sure it's the same
		def ontologyDB = Ontology.findByName(testOntologyName)
		assert ontologyDB
		assert ontologyDB.name.equals(testOntologyName)
		assert ontologyDB.description.equals(testOntologyDescription)
		assert ontologyDB.url.equals(testOntologyUrl)
		assert ontologyDB.versionNumber.equals(testOntologyVersionNumber)
		assert ontologyDB.ncboId.equals(testOntologyNcboId)
		assert ontologyDB.ncboVersionedId.equals(testOntologyNcboVersionedId)

		// Apparently, the saved ontology is not persisted between test methods :-(
		// Otherwise, we could separate these parts into different tests

		// === TEST Term and giveTermByName

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

		// === TEST giveTerms
		
		def terms = testOntology.giveTerms()
		assert terms
		assert terms.size() == 1
		assert terms.asList().first().name.equals(testTermName)

	}
}
