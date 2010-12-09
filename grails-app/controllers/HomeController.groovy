import dbnp.studycapturing.Study
import dbnp.studycapturing.Template

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
	    if (!Template.count()) {
		    redirect(controller:'setup',action:'index')		    
	    } else {
		    [ studyCount: dbnp.studycapturing.Study.count(), userCount: dbnp.authentication.SecUser.count(), facebookLikeUrl: '/' ]
	    }
    }
    
}
