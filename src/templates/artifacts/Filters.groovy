/**
 * @artifact.name@ Filters
 *
 * Description of my controller
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
@artifact.package@class @artifact.name@ {
    def filters = {
        all(controller:'*', action:'*') {
            before = {

            }
            after = {

            }
            afterView = {
                
            }
        }
    }
    
}
