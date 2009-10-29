/**
 * Study Domain
 *
 * @author	email2gajula@gmail.com
 * @since	20091019
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

class Study {

    String      title
    String      description
    String      code
    String      researchQuestion
    Date        startDate
    String      ecCode
    Date        created
    Date        modified
    gscf.User   owner

    static constraints = {
    }
}
