import dbnp.studycapturing.Study

/**
 * Home Controler
 *
 * My Description
 *
 * @author  Kees van Bochove
 * @since   20091102
 * @package studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class HomeController {
    def index = {
       [ studyCount: dbnp.studycapturing.Study.count(), userCount: dbnp.authentication.SecUser.count() ]
    }
    
}
