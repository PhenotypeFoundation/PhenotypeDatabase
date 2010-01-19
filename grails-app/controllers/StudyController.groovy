/**
 * Home Controler
 *
 * My Description
 *
 * @author  Kees van Bochove
 * @since   20091028
 * @package studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class StudyController extends BaseController {
    def scaffold = dbnp.studycapturing.Study

    def see = {
        render params
    }
}
