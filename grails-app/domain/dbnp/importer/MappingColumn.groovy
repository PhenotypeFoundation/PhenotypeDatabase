package dbnp.importer

class MappingColumn {

	String name
	dbnp.studycapturing.TemplateFieldType type
	Class entity
	String property

    static constraints = {
	    name(unique: true)
    }
}
