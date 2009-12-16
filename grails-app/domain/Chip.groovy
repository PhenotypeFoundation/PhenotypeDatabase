/**
 * The Chip Domain Class
 *
 * What is this?
 *
 * @author  Robert Kerkhoven
 * @since   20091215
 * @package Proteomics
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class Chip {
    static mapping = {
         table 'chip'
         // version is set to false, because this isn't available by default for legacy databases
         version false
         id generator:'identity', column:'id'
    }
    Integer id
    String name
    Date timeStamp
    String version
    String databaseName

    static constraints = {
        id(max: 2147483647)
        name(size: 1..200, blank: false)
        timeStamp()
        version(size: 1..45, blank: false)
        databaseName(size: 1..45, blank: false)
    }
    String toString() {
        return "${id}" 
    }
}
