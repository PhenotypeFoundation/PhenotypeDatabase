package dbnp.studycapturing

import dbnp.data.Term
import dbnp.data.Ontology

/**
 * The Sample class describes an actual sample which results from a SamplingEvent.
 */
class Sample extends TemplateEntity {
	static searchable = true
	Subject parentSubject
	SamplingEvent parentEvent

	String name		// should be unique with respect to the parent study (which can be inferred)
	Term material	// a member that describes the quantity of the sample? --> should be in the templates

	/**
	 * return the domain fields for this domain class
	 * @return List
	 */
	static List<TemplateField> giveDomainFields() { return Sample.domainFields }
	static List<TemplateField> domainFields = [
		new TemplateField(
			name: 'name',
			type: TemplateFieldType.STRING,
			preferredIdentifier: true
		),
		new TemplateField(
			name: 'material',
			type: TemplateFieldType.ONTOLOGYTERM
		)
	]

	static constraints = {
		parentSubject(nullable:true)
	}

	static getSamplesFor( event ) {
		return  Sample.findAll( 'from Sample s where s.parentEvent =:event', [event:event] )
	}
}
