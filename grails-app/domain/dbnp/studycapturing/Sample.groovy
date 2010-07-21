package dbnp.studycapturing

import dbnp.data.Term

/**
 * The Sample class describes an actual sample which results from a SamplingEvent.
 */
class Sample extends TemplateEntity {
	// uncommented due to searchable issue
	// @see http://jira.codehaus.org/browse/GRAILSPLUGINS-1577
	//static searchable = { [only: ['name']] }

	static belongsTo = [ parent : Study]

	Subject parentSubject
	SamplingEvent parentEvent

	String name             // should be unique with respect to the parent study (which can be inferred)
	Term material	        // material of the sample (should normally be bound to the BRENDA ontology)

	def getExternalSampleId() { name }

	/**
	 * return the domain fields for this domain class
	 * @return List
	 */
	static List<TemplateField> giveDomainFields() { return Sample.domainFields }
	static List<TemplateField> domainFields = [
		new TemplateField(
			name: 'name',
			type: TemplateFieldType.STRING,
			preferredIdentifier: true,
			required: true
		),
		new TemplateField(
			name: 'material',
			type: TemplateFieldType.ONTOLOGYTERM
		)
	]

	static constraints = {
		parentSubject(nullable:true)
		material(nullable: true)

		// Checks if the externalSampleId (currently defined as name) is really unique within each parent study of this sample.
		// This feature is tested by integration test SampleTests.testSampleUniqueNameConstraint
		name(unique:['parent'])
	}

	static getSamplesFor( event ) {
		return  Sample.findAll( 'from Sample s where s.parentEvent =:event', [event:event] )
	}

}
