package dbnp.importer
import dbnp.studycapturing.TemplateField

/**
* name: column name (in Excel)
* templatefieldtype: GSCF field type
* entity: GSCF entity class
* property: GSCF template field
* index: column index
* value: column value (optional, normally only name is used)
* identifier: true if this column is identifying (unique/primary key)
*/
class MappingColumn {

	String name
	dbnp.studycapturing.TemplateFieldType templatefieldtype
	Class entity
	String property
	Integer index
	String value
	Boolean identifier

    static constraints = {
	    name(unique: true)
    }

    String toString() {
	return "Name:" + name + "/TemplateFieldType:" + templatefieldtype + "/Entity:" + entity + "/Property:" + property + "/Index:" + index + "/Value:" + value + "/Identifier:" + identifier
    }
}
