package gscf

import grails.test.*
import dbnp.data.*

import uk.ac.ebi.ontocat.*

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
	* Test saving and retrieving a term within the ontology and test giveTermByName(name) and giveTerms()
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

		// Test giveTerms() and make sure the term is in there
		def terms = testOntology.giveTerms()
		assert terms
		assert terms.size() == 1
		assert terms.asList().first().name.equals(testTermName)
	}

	/**
 	* Ontocat test for debug purposes: show all properties of a certain ontology
 	*/
	private void testOntocatBioPortalDebug() {
		// Instantiate OLS service
		uk.ac.ebi.ontocat.OntologyService os = new uk.ac.ebi.ontocat.bioportal.BioportalOntologyService()

		// Find ontology by ncboId
		uk.ac.ebi.ontocat.Ontology o = os.getOntology("1005")
		StringBuilder sb = new StringBuilder();
		def bean = os.getOntologyBean()
		String codingScheme = bean.codingScheme
		sb.append("OntologyBean:\n")
		sb.append("property codingScheme="+codingScheme+"\n")
		sb.append("Bean.properties:\n")
		bean.properties.each {
			sb.append(it.key + "=" + it.value+"\n")
		}
		sb.append "Bean:\t" + bean.dump()
		sb.append(bean.properties['codingScheme'])
		sb.append("\t");
		sb.append(o.getAbbreviation());
		sb.append("\t");
		sb.append(o.getLabel());
		sb.append("\t");
		sb.append(o.getOntologyAccession());
		sb.append("\t");
		sb.append("Ontology meta properties:\n")
		o.getVersionNumber() + o.getMetaPropertyValues().each {
			sb.append(it.name + "=" + it.value+"\n")
		}
		sb.append("Ontology properties:\n");
		o.getProperties().each {
			sb.append(it.key + "=" + it.value+"\n")
		}
		sb.append("Ontology root terms:\n");
		os.getRootTerms(o).each {
			sb.append("Term ${os.makeLookupHyperlink(it.properties.get('accession'))} properties:\n")
			it.properties.each {
				sb.append it.key + "=" + it.value + "\n"
			}
		}
		System.out.println(sb.toString());
	}

	/**
	 * Add all OLS ontologies to the database via the Ontocat framework
	 */
	void testOntocatOLSOntologies() {
		// Instantiate EBI OLS service
		uk.ac.ebi.ontocat.OntologyService os = new uk.ac.ebi.ontocat.ols.OlsOntologyService()
		addOntologies(os)
	}

	/**
	 * Add all BioPortal ontologies to the database via the Ontocat framework
	 */
	void testOntocatBioPortalOntologies() {
		// Instantiate BioPortal service
		uk.ac.ebi.ontocat.OntologyService os = new uk.ac.ebi.ontocat.bioportal.BioportalOntologyService()
		addOntologies(os)
	}

	private void addOntologies(uk.ac.ebi.ontocat.OntologyService os) {

		// Iterate over all ontologies in OLS
		os.getOntologies().each {

			// Get bean and extract versionedId
			def bean = os.ontologyBean
			def matcher = bean.codingScheme =~ "/(\\d{5})/"
			assert matcher
			assert matcher.size() == 1
			assert matcher[0].size() == 2
			def versionedId = matcher[0][1]

			// Instantiate ontology
			def ontology = new Ontology(
					name: o.label,
					description: o.description,
					url: bean.properties['homepage'],
					//url: 'http://bioportal.bioontology.org/ontologies/' + versionedId,
					versionNumber: o.versionNumber,
					ncboId: o.ontologyAccession,
					ncboVersionedId: versionedId
			);

			// Validate and save ontology
			assert ontology.validate()
			assert ontology.save(flush:true)

			//println ontology.dump()
			}
		}
	}
}
