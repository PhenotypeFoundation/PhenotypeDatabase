package dbnp.importer
import org.dbnp.gdt.*

class ImportMapping implements Serializable {

	static hasMany = [columns: MappingColumn]    
    Template template
    Class entity
	String name


	static constraints = {
	}
}
