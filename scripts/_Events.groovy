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
   // Fix for addToWriters()-method failing, seems that when the default.properties containing the configured users
   // cannot be read, the addToWriters()-method tries to add "null" users, which fails.
   // Added this ant script which moves the default.properties to the correct classpath. Seems to work now.
   // Might be a bug in Grails (at least till 1.3.7)?

   ant.echo (message: "Moving default configuration file (default.properties) ..." )
   ant.copy(file:"${basedir}/grails-app/conf/default.properties", todir:classesDirPath)
   ant.echo (message: "Default configuration file moved.")
}
