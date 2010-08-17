package dbnp.studycapturing

import dbnp.data.Term

/**
 * The Sample class describes an actual sample which results from a SamplingEvent.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Sample extends TemplateEntity {
	// uncommented due to searchable issue
	// @see http://jira.codehaus.org/browse/GRAILSPLUGINS-1577
	//static searchable = { [only: ['name']] }

	// A Sample always belongs to one study.
	static belongsTo = [parent : Study, parentSubject : Subject, parentEvent : SamplingEvent]

	// A Sample optionally has a parent Subject from which it was taken, this Subject should be in the same parent study.
	//long parentSubject

	// Also, it has a parent SamplingEvent describing the actual sampling, also within the same parent study.
	// Strange enough, we need to define parentEvent as a long here, otherwise the SamplingEvent gets serialized into the database (?!!)
	//long parentEvent

	String name             // should be unique with respect to the parent study (which can be inferred)
	Term material	        // material of the sample (should normally be bound to the BRENDA ontology)

	def getExternalSampleId() { name }

	/**
	 * return the domain fields for this domain class
	 * @return List
	 */
	static List<TemplateField> giveDomainFields() { return Sample.domainFields }

	// We have to specify an ontology list for the material property. However, at compile time, this ontology does of course not exist.
	// Therefore, the ontology is added at runtime in the bootstrap, possibly downloading the ontology properties if it is not present in the database yet.
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
		// The parent subject is optional, e.g. in a biobank of samples the subject could be unknown or non-existing.
		parentSubject(nullable:true)

		// The material domain field is optional
		material(nullable: true)

		// Check if the externalSampleId (currently defined as name) is really unique within each parent study of this sample.
		// This feature is tested by integration test SampleTests.testSampleUniqueNameConstraint
		name(unique:['parent'])
	}

	static getSamplesFor( event ) {
		return  Sample.findAll( 'from Sample s where s.parentEvent =:event', [event:event] )
	}

	def String toString() {
		return name
	}
}
