package dbnp.importer

import dbnp.studycapturing.Identity

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
    String value

    static constraints = {
    }

    String toString() {
        return "`" + value + "`"
    }
}
