package dbnp.studycapturing
import java.util.ArrayList;

import org.dbnp.gdt.*

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

	static belongsTo = [
		// A Sample always belongs to one study.
		parent			: Study,

		// A Sample optionally has a parent Subject from which it was taken, this Subject should be in the same parent study.
		parentSubject	: Subject,

		// Also, it has a parent SamplingEvent describing the actual sampling, also within the same parent study.
		parentEvent		: SamplingEvent,

		// And it has a parent EventGroup which tied it to its parent subject and parent event
		parentEventGroup: EventGroup

		// We can't have parentAssay since a Sample can belong to multiple Assays
	]

	String name             // should be unique with respect to the parent study (which can be inferred)
	Term material	        // material of the sample (should normally be bound to the BRENDA ontology)
	
	/**
	 * UUID of this sample
	 */
	String sampleUUID
	
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
			type: TemplateFieldType.ONTOLOGYTERM,
			comment: "The material is based on the BRENDA tissue / enzyme source ontology, a structured controlled vocabulary for the source of an enzyme. It comprises terms for tissues, cell lines, cell types and cell cultures from uni- and multicellular organisms. If a material is missing, please add it by using 'add more'"
		)
	]

	static constraints = {
		// The parent subject is optional, e.g. in a biobank of samples the subject could be unknown or non-existing.
		parentSubject(nullable:true)

		// The same holds for parentEvent
		parentEvent(nullable:true)

		// and for parentEventGroup
		parentEventGroup(nullable:true)

		// The material domain field is optional
		material(nullable: true)

		sampleUUID(nullable: true, unique: true)

		// Check if the externalSampleId (currently defined as name) is really unique within each parent study of this sample.
		// This feature is tested by integration test SampleTests.testSampleUniqueNameConstraint
		name(unique:['parent'])

		// Same, but also when the other sample is not even in the database
		// This feature is tested by integration test SampleTests.testSampleUniqueNameConstraintAtValidate
		name(validator: { field, obj, errors ->
			// 'obj' refers to the actual Sample object

			// define a boolean
			def error = false

			// check whether obj.parent.samples is not null at this stage to avoid null pointer exception
			if (obj.parent) {

				if (obj.parent.samples) {

					// check if there is exactly one sample with this name in the study (this one)
					if (obj.parent.samples.findAll{ it.name == obj.name}.size() > 1) {
						error = true
						errors.rejectValue(
							'name',
							'sample.UniqueNameViolation',
							[obj.name, obj.parent] as Object[],
							'Sample name {0} appears multiple times in study {1}'
							)
					}
				}
			}
			else {
				// if there is no parent study defined, fail immediately
				error = true
			}

			// got an error, or not?
			return (!error)
		})
	}

    static mapping = {
        sort "name"

        // Workaround for bug http://jira.codehaus.org/browse/GRAILS-6754
	templateTextFields type: 'text'
    }

	static getSamplesFor( event ) {
		return  Sample.findAll( 'from Sample s where s.parentEvent =:event', [event:event] )
	}

	def String toString() {
		return name
	}

	/**
	* Basic equals method to check whether objects are equals, by comparing the ids
	* @param o		Object to compare with
	* @return		True iff the id of the given Sample is equal to the id of this Sample
	*/
   public boolean equals( Object o ) {
	   if( o == null )
		   return false;
		   
	   if( !( o instanceof Sample ) )
		   return false
	   
	   Sample s = (Sample) o;
	   
	   return this.id == s.id
   }
	
	/**
	 * Returns the UUID of this sample and generates one if needed
	 */
	public String giveUUID() {
		if( !this.sampleUUID ) {
			this.sampleUUID = UUID.randomUUID().toString();
			if( !this.save(flush:true) ) {
				//println "Couldn't save sample UUID: " + this.getErrors();
			}
		}
		
		return this.sampleUUID;
	}
	
	/**
	* Returns a human readable string of a list of samples, with a maximum number
	* of characters
	*
	* @param sampleList List with Sample objects
	* @param maxChars maximum number of characters returned
	* @return human readble string with at most maxChars characters, representing the samples given.
	*/
   public static String trimSampleNames(ArrayList sampleList, Integer maxChars) {
	   def simpleSamples = sampleList.name.join(', ');
	   def showSamples

	   // If the subjects will fit, show them all
	   if (!maxChars || simpleSamples.size() < maxChars) {
		   showSamples = simpleSamples;
	   } else {
		   // Always add the first name
		   def sampleNames = sampleList[0]?.name;

		   // Continue adding names until the length is to long
		   def id = 0;
		   sampleList.each { sample ->
			   if (id > 0) {
				   if (sampleNames?.size() + sample.name?.size() < maxChars - 15) {
					   sampleNames += ", " + sample.name;
				   } else {
					   return;
				   }
			   }
			   id++;
		   }

		   // Add a postfix
		   sampleNames += " and " + (sampleList?.size() - id) + " more";

		   showSamples = sampleNames;
	   }

	   return showSamples
   }

}
