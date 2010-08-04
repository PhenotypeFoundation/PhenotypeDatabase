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
class SubjectTests extends StudyTests {

	static final String testSubjectName = "Test subject"
	static final String testSubjectTemplateName = "Human"
	static final String testSubjectSpeciesTerm = "Homo sapiens"
	static final String testSubjectBMITemplateFieldName = "BMI"
	static final double testSubjectBMI = 25.32
	static final String testSubjectGenderTemplateFieldName = "Gender"
	static final String testSubjectGender = "female"
	static final String testSubjectGenderDBName = "Female"

	/**
	 * Set up test: create test subject to use in the tests, thereby test creation
	 */
	protected void setUp() {
		super.setUp()

		// Retrieve the study that should have been created in StudyTests
		def study = Study.findByTitle(testStudyName)
		assert study

		createSubject(study)
	}

	public static Subject createSubject(Study parentStudy) {

		// Look up human template
		def humanTemplate = Template.findByName(testSubjectTemplateName)
		assert humanTemplate

		def speciesOntology = Ontology.getOrCreateOntologyByNcboId(1132)
		def humanTerm = new Term(
			name: 'Homo sapiens',
			ontology: speciesOntology,
			accession: '9606')

		assert humanTerm.validate()
		assert humanTerm.save(flush:true)
		assert humanTerm

		def subject = new Subject(
			name: testSubjectName,
			template: humanTemplate,
			species: humanTerm
		)

		// At this point, the sample should not validate, because it doesn't have a parent study assigned
		assert !subject.validate()

		// Add the subject to the retrieved parent study
		parentStudy.addToSubjects(subject)
		assert parentStudy.subjects.find { it.name == subject.name}

		// Now, the subject should validate
		if (!subject.validate()) {
			subject.errors.each { println it}
		}
		assert subject.validate()

		// Make sure the subject is saved to the database
		assert subject.save(flush: true)

		return subject

	}

	void testSave() {
		// Try to retrieve the subject and make sure it's the same
		def subjectDB = Subject.findByName(testSubjectName)
		assert subjectDB
		assert subjectDB.name.equals(testSubjectName)
		assert subjectDB.template.name.equals(testSubjectTemplateName)
		assert subjectDB.species.name.equals(testSubjectSpeciesTerm)
	}

	void testDelete() {
		def subjectDB = Subject.findByName(testSubjectName)
		subjectDB.delete()
		try {
			subjectDB.save()
			assert false // The save should not succeed since the subject is referenced by a study
		}
		catch(org.springframework.dao.InvalidDataAccessApiUsageException e) {
			subjectDB.discard()
			assert true // OK, correct exception (at least for the in-mem db, for PostgreSQL it's probably a different one...)
		}

		// Now, delete the subject from the study subjects collection, and then the delete action should be cascaded to the subject itself
		def study = Study.findByTitle(testStudyName)
		assert study
		study.removeFromSubjects subjectDB

		// Make sure the subject doesn't exist anymore at this point
		assert !Subject.findByName(testSubjectName)
		assert Subject.count() == 0
		assert study.subjects.size() == 0
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
		// Still, no parent study assigned
		assert !subject.validate()

		def study = Study.findByTitle(testStudyName)
		assert study
		study.addToSubjects subject

		// Now, the new subject should validate
		assert subject.validate()
		assert subject.save()

		// If the subject has the same name as another subject within the same study, it should not validate or save
		subject.name = testSubjectName
		assert !subject.validate()
		assert !subject.save()
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
		assert domainFields[0].name == 'name'
		assert domainFields[1].name == 'species'

		// Also, make sure isDomainField() says the same
		assert subject.isDomainField(domainFields[0])
		assert subject.isDomainField(domainFields[1])
		assert subject.isDomainField('name')
		assert subject.isDomainField('species')
		// To be sure it just doesn't always return true :-)
		assert !subject.isDomainField('123~!name')
		assert !subject.isDomainField(testSubjectBMITemplateFieldName)

		// Get the ontologies from species and make sure this is 1 ontology with NCBO ID 1132
		def speciesOntologies = domainFields[1].ontologies
		assert speciesOntologies.size() == 1

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
		if (!subject.validate()) {
			subject.errors.each { println it}
		}
		assert subject.validate()
		assert subject.save(flush:true)

		assert subject.getFieldValue('species') == humanTerm
		assert subject.getFieldValue('name').equals(testSubjectName)
		assert subject.getFieldValue('species') == humanTerm

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
	 * Test setFieldValue() and getFieldValue() for template fields
     * This cannot be done in separate tests, as the database state is reset in between
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

	void testSetGender() {
		def subject = Subject.findByName(testSubjectName)
		assert subject

		// Set gender
		subject.setFieldValue(testSubjectGenderTemplateFieldName,testSubjectGender)

		// Test if gender is set properly (to its canonical database name) via getFieldValue()
		assert subject.getFieldValue(testSubjectGenderTemplateFieldName).name == testSubjectGenderDBName

		// Try to save object
		assert subject.validate()
		assert subject.save(flush: true)

		// Try to retrieve the subject and make sure the Gender was stored properly
		def subjectDB = Subject.findByName(testSubjectName)
		assert subjectDB
		assert subjectDB.getFieldValue(testSubjectGenderTemplateFieldName).name == testSubjectGenderDBName
	}

	protected void tearDown() {
		super.tearDown()
	}

}
