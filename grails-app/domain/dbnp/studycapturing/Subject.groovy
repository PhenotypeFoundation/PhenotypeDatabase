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
class Subject extends TemplateEntity implements Serializable {
	static searchable = true
	String name
	Term species
	
	/**
	 * return the domain fields for this domain class
	 * @return List
	 */
	List<TemplateField> giveDomainFields() {
		[
			new TemplateField(
				name: 'name',
				type: TemplateFieldType.STRING,
				preferredIdentifier: true,
				comment: 'Use the local subject name or the pre-defined name'),
			new TemplateField(
				name: 'species',
				type: TemplateFieldType.ONTOLOGYTERM,
				ontologies: [Ontology.findByNcboId(1132)],
				comment: "The species name is based on the NEWT ontology; if a species is missing, please add it to the ontology using 'add more'")
		]
	}
}
