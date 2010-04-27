package dbnp.studycapturing

import dbnp.data.Term

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

	List<DomainTemplateField> giveDomainFields() {
		[ new DomainTemplateField(
                            name: 'name',
                            type: TemplateFieldType.STRING),
                        new DomainTemplateField(
                            name: 'species',
                            type: TemplateFieldType.ONTOLOGYTERM) ];
	}
}
