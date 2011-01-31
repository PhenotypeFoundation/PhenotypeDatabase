package dbnp.studycapturing
import org.dbnp.gdt.*

/**
 * This class describes an Assay, which describes the application of a certain (omics) measurement to multiple samples.
 * The actual data of these measurements are described in submodules of dbNP. The type property describes in which module
 * this data can be found.
 */
class Assay extends TemplateEntity {
	// The name of the assay, which should indicate the measurements represented in this assay to the user.
	String name

	// The dbNP module in which the assay omics data can be found. */
	AssayModule module

	// The assay ID which is used in the dbNP submodule which contains the actual omics data of this assay.
	// This ID is generated in GSCF, but is used in the submodules to refer to this particular Assay.
	String externalAssayID

	/**
	* UUID of this assay
	*/
    String assayUUID

	/**
	 * return the domain fields for this domain class
	 * @return List
	 */
	static List<TemplateField> giveDomainFields() { return Assay.domainFields }
	static List<TemplateField> domainFields = [
		new TemplateField(
			name: 'name',
			type: TemplateFieldType.STRING,
			preferredIdentifier: true,
			required: true
		),
		new TemplateField(
			name: 'module',
			type: TemplateFieldType.MODULE,
			required: true
		),
		new TemplateField(
			name: 'externalAssayID',
			type: TemplateFieldType.STRING,
			required: true
		)
	]

	// An Assay always belongs to one study.
	static belongsTo = [parent: Study]

	// An Assay can have many samples on which it is performed, but all samples should be within the 'parent' Study.
	static hasMany = [samples: Sample]

	static constraints = {
		externalAssayID(nullable:false, blank:false, unique: true)
		assayUUID(nullable:true, unique: true)
	}

    static mapping = {
        sort "name"

        // Workaround for bug http://jira.codehaus.org/browse/GRAILS-6754
	templateTextFields type: 'text'
    }

	def String toString() {
		return name;
	}

    def getToken() {
		return giveUUID()
    }
	
	/**
	 * Returns the UUID of this sample and generates one if needed
	 */
	public String giveUUID() {
		if( !this.assayUUID ) {
			this.assayUUID = UUID.randomUUID().toString();
			if( !this.save(flush:true) ) {
				log.error "Couldn't save assay UUID: " + this.getErrors();
			}
		}
		
		return this.assayUUID;
	}
}
