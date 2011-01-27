package dbnp.importer
import nl.grails.plugins.gdt.*

/**
 * Cell Domain Class
 *
 * Every cell
 *
 * @author  your email (+name?)
 * @since	2010mmdd
 * @package	???
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

class ImportCell extends Identity {
    MappingColumn mappingcolumn
    int entityidentifier
    String value

    static constraints = {
    }

    String toString() {
        return "`" + value + "`"
    }
}
