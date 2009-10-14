/**
 * Debug Controller
 * @Author  Jeroen Wesbeek
 * @Since   20091014
 * @Description
 *
 * If all controllers extend this debug controller in one piece of code the
 * behaviour of the other controllers can be -to some extent- manipulated.
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class DebugController {
    def scaffold = true;
    def index = { render('nothing to see here :)') }
}
