package dbnp.studycapturing

/**
 * This is the superclass for template fields. Normally, this class will not be instantiated.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class TemplateField implements Serializable {
	String name
	TemplateFieldType type
	String unit
	String comment // help string for the user interface

    static hasMany = [listEntries : TemplateFieldListItem] // to store the entries to choose from when the type is 'item from predefined list'

	static constraints = {
		name(unique: true)
		unit(nullable: true, blank: true)
		comment(nullable:true, blank: true)
	}

	static mapping = {
		comment type: 'text'
	}

	String toString() {
		return name
	}


	/**
	 * return an escaped name which can be used in business logic
	 * @return String
	 */
	def String escapedName() {
		return name.toLowerCase().replaceAll("([^a-zA-Z0-9])","_")
	}

	//TODO: make a convenience setter for a string array
	/*def setListEntries(ArrayList entries) {
		list=[]
		entries.each {
			list.add(new TemplateFieldListItem(name: it))
		}
		this
	}*/

}
