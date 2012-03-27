/**
 * Token Domain Class
 *
 * Description of my domain class
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
package api

class Token {
    String deviceID
    String deviceToken
    BigInteger sequence

    static constraints = {
        deviceID nullable: false, unique: true, maxSize: 36
        deviceToken nullable: false, unique: true, maxSize: 36
    }
}
