/**
 * TemplateField instance for built-in domain fields of templateentities.
 * This object will not be persisted in the database
 *
 * @author  roberth
 * @since   20100427
 * @package dbnp.studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
package dbnp.studycapturing

class DomainTemplateField extends TemplateField {
    // There seems to be no way to override the save method
    // By creating a validator that returns false always, this item will never
    // be saved
    static constraints = {
        name(validator: {return false} )
    }
}
