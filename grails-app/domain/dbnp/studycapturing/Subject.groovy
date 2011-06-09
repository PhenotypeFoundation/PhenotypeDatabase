package dbnp.studycapturing

import org.dbnp.gdt.*

/**
 * This domain class describes the subjects in a study.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Subject extends TemplateEntity {
	// A Subject always belongs to one Study
	static belongsTo = [parent: Study]

	/** The name of the subject, which should be unique within the study   */
	String name

	/** The species of the subject. In the domainFields property, the ontologies from which this term may come are specified.   */
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
			comment: "The species name is based on the NCI Thesaurus / NCBI organismal classification ontology, a taxonomic classification of living organisms and associated artifacts. If a species is missing, please add it by using 'add more'",
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

	/**
	 * Returns a human readable string of a list of subjects, with a maximum number
	 * of characters
	 *
	 * @param subjectList List with Subject objects
	 * @param maxChars maximum number of characters returned
	 * @return human readble string with at most maxChars characters, representing the subjects given.
	 */
	public static String trimSubjectNames(ArrayList subjectList, Integer maxChars) {
		def simpleSubjects = subjectList.name.join(', ');
		def showSubjects

		// If the subjects will fit, show them all
		if (!maxChars || simpleSubjects.size() < maxChars) {
			showSubjects = simpleSubjects;
		} else {
			// Always add the first name
			def subjectNames = subjectList[0]?.name;

			// Continue adding names until the length is to long
			def id = 0;
			subjectList.each { subject ->
				if (id > 0) {
					if (subjectNames?.size() + subject.name?.size() < maxChars - 15) {
						subjectNames += ", " + subject.name;
					} else {
						return;
					}
				}
				id++;
			}

			// Add a postfix
			subjectNames += " and " + (subjectList?.size() - id) + " more";

			showSubjects = subjectNames;
		}

		return showSubjects
	}
}
