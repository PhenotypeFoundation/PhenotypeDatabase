package org.dbnp.gdt

/**
 * TemplateFieldListItem Domain Class. Used to represent the list items in a STRINGLIST TemplateField.
 *
 * Revision information:
 * $Rev: 959 $
 * $Author: j.a.m.wesbeek@umail.leidenuniv.nl $
 * $Date: 2010-10-20 21:13:14 +0200 (Wed, 20 Oct 2010) $
 */
class TemplateFieldListItem implements Serializable {
	// A TemplateFieldListItem always belongs to one TemplateField of TemplateFieldType STRINGLIST or EXTENDABLESTRINGLIST
	static belongsTo = [parent: TemplateField]

	/** The caption of the list item  */
	String name

	static constraints = {
	}

	static mapping = {
		name column: "templatefieldlistitemname"
	}

	String toString() {
		return name;
	}
}