package dbnp.studycapturing

import dbnp.data.Term
import dbnp.data.Ontology

/**
 * The Sample class describes an actual sample which results from a SamplingEvent.
 */
class Sample extends TemplateEntity {
    static searchable = { [only: ['name']] }
  
	Subject parentSubject
	SamplingEvent parentEvent

	String name             // should be unique with respect to the parent study (which can be inferred)
	Term material	        // material of the sample (should normally be bound to the BRENDA ontology)

	def getExternalSampleId() { name }

	// TODO: Write a validation method that checks if the externalSampleId (currently defined as name)
	// is really unique within each parent study of this sample.
	// Maybe this could also be a constraint, but we might run into trouble creating new Sample objects in e.g. the create wizard.
	// To be checked.

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
