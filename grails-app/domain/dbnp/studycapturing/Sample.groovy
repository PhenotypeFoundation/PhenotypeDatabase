package dbnp.studycapturing

import dbnp.data.Term
import dbnp.data.Ontology

/**
 * The Sample class describes an actual sample which results from a SamplingEvent.
 */
class Sample extends TemplateEntity {
        static searchable = { [only: ['name']] }

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

		// TODO: Write a validation method that checks if the externalSampleId (currently defined as name)
		// is really unique within each parent study of this sample.
		// Maybe this could also be a constraint, but we might run into trouble creating new Sample objects in e.g. the create wizard.
		// To be checked.
		// This feature is tested by integration test SampleTests.testSampleUniqueNameConstraint

		/*name(validator: { fields, obj, errors ->
			def error = false

			if (fields) {
				// Search through all study
				if (obj.parent.samples.findAll({ it.name == fields}).size() != 1) {
					errors.rejectValue(
						'name',
						'sample.name.NotUnique',
						['name',fields] as Object[],
						'The sample name is not unique within the study'
					)
				}
			}
			else {
				// If no value for name is specified, the sample should not validate
				error = true
			}

			return (!error)
		})*/
	}

	/*static def getParentStudies() {
		Study.findAll {
			it.samples.findAll {
				it.name == this.name
			}
		}
	}*/

	static getSamplesFor( event ) {
		return  Sample.findAll( 'from Sample s where s.parentEvent =:event', [event:event] )
	}

}
