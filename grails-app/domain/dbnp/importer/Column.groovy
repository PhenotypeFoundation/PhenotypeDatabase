package dbnp.importer

class Column {
    String type
    String name
    String value

    static constraints = {
	type(nullable:true)
	name(nullable:true)
	value(nullable:true)
    }

    String toString() {
	return name
    }
}
