/**
 * ImportRecord Domain Class
 *
 * This record contains the imported cells
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
package dbnp.importer
import dbnp.studycapturing.Identity

class ImportRecord extends Identity {
    static hasMany = [ importcells: ImportCell ]
    
    static constraints = {
    }

    String toString() {
        return "importrecord_id:"+this.getIdentifier()
    }
}
