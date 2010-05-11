package gscf

import grails.test.*
import dbnp.studycapturing.*
import dbnp.data.*

/**
 * SubjectTests Test
 *
 * Test the creation of a Subject and its TemplateEntity functionality on data model level
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
class SubjectTests extends GrailsUnitTestCase {

	final String testSubjectName = "Test subject"
	final String testSubjectTemplateName = "Human"
	final String testSubjectSpeciesTerm = "Homo sapiens"
	final String testSubjectBMITemplateFieldName = "BMI"
	final double testSubjectBMI = 25.32

	/**
	 * Set up test: create test subject to use in the tests, thereby test creation
	 */
	protected void setUp() {
		super.setUp()

		// Look up human template
		def humanTemplate = Template.findByName(testSubjectTemplateName)
		assert humanTemplate

		def humanTerm = Term.findByName(testSubjectSpeciesTerm)
		assert humanTerm

		def subject = new Subject(
			name: testSubjectName,
			template: humanTemplate,
			species: humanTerm
		)

		assert subject.save(flush:true)
	}

	void testSave() {
		// Try to retrieve the subject and make sure it's the same
		def subjectDB = Subject.findByName(testSubjectName)
		assert subjectDB
		assert subjectDB.name.equals(testSubjectName)
		assert subjectDB.template.name.equals(testSubjectTemplateName)
		assert subjectDB.species.name.equals(testSubjectSpeciesTerm)
	}

	void testValidators() {

		def subject = new Subject(
			name: testSubjectName + "-test"
		);
		// Should not validate as template and species are missing
		assert !subject.validate()

		subject.template = Template.findByName(testSubjectTemplateName)
		// Still, species is missing
		assert !subject.validate()

		subject.species = Term.findByName(testSubjectSpeciesTerm)
		assert subject.validate()

		// Change name to already existing name, should fail
		subject.name = testSubjectName
		assert !subject.validate()
	}

	/**
	 * Test domain field setter of a subject,
	 * by finding the appropriate species as should be defined in the Bootstrap and giveDomainFields
	 * Test that species ontology is correctly defined in giveDomainFields
	 * Test setFieldValue() for domain field species
	 *
	 */
	void testDomainFieldSetters() {

		def subject = Subject.findByName(testSubjectName)
		assert subject

		// Remove species from subject, should not validate anymore
		subject.species = null
		assert !subject.validate()


		// Get domain fields and make sure they are name and species
		def domainFields = subject.giveDomainFields()
		assert domainFields
		println domainFields[0]
		assert domainFields[0].name == 'name'
		assert domainFields[1].name == 'species'

		// Get the ontologies from species and make sure this is 1 ontology with NCBO ID 1132
		def speciesOntologies = domainFields[1].ontologies
		assert speciesOntologies.size() == 1

		println speciesOntologies.class
		// Getting the only element in a set is hard in Grails...
		Ontology speciesOntology = speciesOntologies.asList().first()
		assert speciesOntology.ncboId == 1132

		def speciesTerms = speciesOntology.giveTerms()
		def humanTerm = speciesTerms.find { it.name == testSubjectSpeciesTerm}
		assert humanTerm

		// Make sure giveTermByName returns the same term
		assert humanTerm == speciesOntology.giveTermByName('Homo sapiens')

		// Make sure the (in this test used!) Term.findByName returns the same
		// If this doesn't hold, we probably should rewrite these tests
		assert humanTerm == Term.findByName(testSubjectSpeciesTerm)

		// Assign species, subject should now validate and save
		subject.setFieldValue('species',humanTerm)
		assert subject.validate()
		assert subject.save(flush:true)

		assert subjectDB.getFieldValue('species') == humanTerm
		assert subjectDB.getFieldValue('name').equals(testSubjectName)
		assert subjectDB.getFieldValue('species') == humanTerm

	}

	/**
	 * Test getFieldValue() for domain fields
	 */
	void testDomainFieldGetters() {

		def subjectDB = Subject.findByName(testSubjectName)
		assert subjectDB

		assert subjectDB.getFieldValue('name').equals(testSubjectName)
		assert subjectDB.getFieldValue('species') == Term.findByName(testSubjectSpeciesTerm)
	}

	/**
	 * Test setFieldValue() for template fields
	 */
	void testTemplateFieldSetters() {

		def subject = Subject.findByName(testSubjectName)
		assert subject

		// Assign a template field using setFieldValue: BMI
		subject.setFieldValue(testSubjectBMITemplateFieldName,testSubjectBMI)

		// Try to retrieve it using getFieldValue
		assert subject.getFieldValue(testSubjectBMITemplateFieldName) == testSubjectBMI

		// Save subject
		assert subject.save(flush: true)


	}

	/**
	* Test getFieldValue() for template fields
	*/
	void testTemplateFieldGetters() {

		// Try to retrieve the subject and make sure the BMI was stored
		def subjectDB = Subject.findByName(testSubjectName)
		assert subjectDB
		assert subjectDB.getFieldValue(testSubjectBMITemplateFieldName) == testSubjectBMI
	}

	/**
	 * Test giveFields(): should return name, species and template fields
	 */
	void testGiveFields() {

		def subject = Subject.findByName(testSubjectName)
		assert subject

		// Test giveFields
		def fields = subject.giveFields()
		def i = 0
		assert fields[i++].name == 'name'
		assert fields[i++].name == 'species'

		// Look up human template
		def humanTemplate = Template.findByName(testSubjectTemplateName)
		assert humanTemplate

		humanTemplate.fields.each {
			assert fields[i++].name.equals(it.name)
		}

	}

	protected void tearDown() {
		super.tearDown()
	}

}
