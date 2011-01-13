import dbnp.studycapturing.Study
import dbnp.studycapturing.Template
import org.codehaus.groovy.grails.commons.GrailsApplication

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
	    //if (!Template.count() && grails.util.GrailsUtil.environment != GrailsApplication.ENV_TEST && grails.util.GrailsUtil.environment != "dbnptest") {
		//    redirect(controller:'setup',action:'index')
	    //} else {
		    [ studyCount: dbnp.studycapturing.Study.count(), userCount: dbnp.authentication.SecUser.count(), facebookLikeUrl: '/' ]
	    //}
    }
    
}
