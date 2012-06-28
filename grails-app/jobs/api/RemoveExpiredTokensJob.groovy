/**
 * Quartz job to remove expired API tokens
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

package api

class RemoveExpiredTokensJob {
    // days after a token's last usage after which it should expire
    static Integer tokenExpiry = 30

    static triggers = {
        // cronjob that runs every whole hour
        cron name: 'removeExpiredAPITokens', cronExpression: "0 0 * * * ?"
    }

    def execute() {
        // fetch all expired tokens
        def tokens = Token.findAllByLastUpdatedLessThanEquals(new Date() - tokenExpiry)

        // if we found any, delete them
        if (tokens.size()) {
            // show a log message
            log.error "removing ${tokens.size()} expired tokens"

            // delete tokens
            tokens.each { it.delete() }
        }
    }
}
