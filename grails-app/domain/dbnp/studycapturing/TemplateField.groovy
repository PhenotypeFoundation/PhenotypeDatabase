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

    static hasMany = [listEntries : TemplateFieldListItem] // to store the entries to choose from when the type is 'item from predefined list'

	static constraints = {
		name(unique: true)
		unit(nullable: true, blank: true)
	}

	String toString() {
		return name
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
