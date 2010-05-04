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

	List<TemplateField> giveDomainFields() {
		[ new TemplateField(
                            name: 'name',
                            type: TemplateFieldType.STRING),
                        new TemplateField(
                            name: 'species',
                            type: TemplateFieldType.ONTOLOGYTERM,
                            ontologies: [Ontology.findByNcboId(1132)]) ];
	}
}
