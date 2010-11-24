package dbnp.studycapturing

import dbnp.data.Term
import dbnp.data.Ontology

/**
 * This domain class describes the subjects in a study.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Subject extends TemplateEntity {
	// uncommented due to searchable issue
	// @see http://jira.codehaus.org/browse/GRAILSPLUGINS-1577
	//static searchable = { [only: ['name']] }

	// A Subject always belongs to one Study
	static belongsTo = [parent: Study]

	/** The name of the subject, which should be unique within the study  */
	String name

	/** The species of the subject. In the domainFields property, the ontologies from which this term may come are specified.  */
	Term species

	static constraints = {
		// Ensure that the subject name is unique within the study
		name(unique: ['parent'])
	}

	static mapping = {
		name column: "subjectname"

                // Workaround for bug http://jira.codehaus.org/browse/GRAILS-6754
		templateTextFields type: 'text'
	}

	/**
	 * return the domain fields for this domain class
	 * @return List
	 */
	static List<TemplateField> giveDomainFields() { return Subject.domainFields; }

	// We have to specify an ontology list for the species property. However, at compile time, this ontology does of course not exist.
	// Therefore, the ontology is added at runtime in the bootstrap, possibly downloading the ontology properties if it is not present in the database yet.
	static List<TemplateField> domainFields = [
		new TemplateField(
			name: 'name',
			type: TemplateFieldType.STRING,
			preferredIdentifier: true,
			comment: 'Use the local subject name or the pre-defined name',
			required: true),
		new TemplateField(
			name: 'species',
			type: TemplateFieldType.ONTOLOGYTERM,
			comment: "The species name is based on the NEWT ontology; if a species is missing, please add it to the ontology using 'add more'",
			required: true)
	]

	/**
	 * Return by default the name of the subject.
	 *
	 * @return name field
	 */
	String toString() {
		return name
	}
}
