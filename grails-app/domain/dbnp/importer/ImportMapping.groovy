package dbnp.importer

class ImportMapping implements Serializable {

	static hasMany = [columns: MappingColumn]
	static String t

	static constraints = {
	}
}
