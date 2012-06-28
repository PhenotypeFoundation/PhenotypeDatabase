/**
 * Quartz configuration
 *
 * @author  Jeroen Wesbeek <work@osx.eu>
 * @since	20120628
 * @package api
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

quartz {
    // auto start quartz scheduler on applicaiton bootstrap
    autoStartup = true

    // store jobs in database
    jdbcStore = false

    // wait for jobs to finish
    waitForJobsToCompleteOnShutdown = true

    // monitor jobs in melody
    exposeSchedulerInRepository = false

    props {
        scheduler.skipUpdateCheck = true
    }
}

environments {
    test {
        quartz {
            autoStartup = false
        }
    }
}
