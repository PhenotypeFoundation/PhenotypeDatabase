/**
 * Event script
 *
 * This script will be run when starting the application and allows you to attach specifiek events
 *
 * @author  t.w.abma@umcutrecht.nl (Tjeerd Abma)
 * @since	20110622
 * @package
 *
 * Revision information:
 * @version $Rev$
 * @author $Author$
 * @date $Date$
*/

eventCompileEnd = {

   ant.echo (message: "Moving default configuration file (default.properties) ..." )
   ant.copy(file:"${basedir}/grails-app/conf/default.properties", todir:classesDirPath)
   ant.echo (message: "Default configuration file moved.")
}
