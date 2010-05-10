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
	final String testSubjectBMITemplateFieldName = "BMI"
	final double testSubjectBMI = 25.32

	protected void setUp() {
		super.setUp()
	}

	protected void tearDown() {
		super.tearDown()
	}

	/**
	 * Test creation and saving of a new human subject,
	 * by finding the appropriate template as should be defined in the Bootstrap,
	 * and finding the appropriate species as should be defined in the Bootstrap and giveDomainFields
	 *
	 */
	void testCreation() {

		// Look up human template
		def humanTemplate = Template.findByName('Human')
		assert humanTemplate

		def subject = new Subject(
			name: testSubjectName,
			template: humanTemplate
		)

		// This subject should not validate as required fields species is missing
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
		def humanTerm = speciesTerms.find { it.name == 'Homo sapiens'}// or speciesOntology.giveTermByName('Homo sapiens')
		assert humanTerm

		// Assign species, subject should now validate
		subject.species = humanTerm
		assert subject.validate()

		// Assign a template field using setFieldValue: BMI
		subject.setFieldValue(testSubjectBMITemplateFieldName,testSubjectBMI)

		// Try to retrieve it using getFieldValue
		assert subject.getFieldValue(testSubjectBMITemplateFieldName) == testSubjectBMI

		// Save subject
		assert subject.save(flush: true)

		// Try to retrieve the subject and make sure it's the same
		def subjectDB = Subject.findByName(testSubjectName)
		assert subjectDB
		assert subjectDB.name.equals(testSubjectName)
		assert subjectDB.template.name.equals(humanTemplate.name)
		assert subjectDB.species.name.equals(humanTerm.name)

		// Test giveFields
		def fields = subjectDB.giveFields()
		def i = 0
		assert fields[i++].name == 'name'
		assert fields[i++].name == 'species'
		humanTemplate.fields.each {
			assert fields[i++].name.equals(it.name)
		}

		// Test getFieldValue
		assert subjectDB.getFieldValue('name').equals(testSubjectName)
		assert subjectDB.getFieldValue('species') == humanTerm
		assert subjectDB.getFieldValue(testSubjectBMITemplateFieldName) == testSubjectBMI

	}

}
