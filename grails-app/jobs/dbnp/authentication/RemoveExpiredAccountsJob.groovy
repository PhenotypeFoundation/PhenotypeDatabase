/**
 * Quartz job to remove expired un-confirmed accounts
 *
 * When a users creates an account, he/she has to confirm it by
 * clicking a link in the registration email. He has 1 day to confirm
 * account creation, after which the account will expire (deleted)
 *
 * @author  Jeroen Wesbeek <work@osx.eu>
 * @since	20120628
 * @package dbnp.authentication
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */

package dbnp.authentication

import javax.security.auth.login.AccountException

class RemoveExpiredAccountsJob {
    // days after account creation when un-confirmed accounts should expire
    static Integer accountExpiry = 1

    // the maximum number of expired accounts to delete per job run
    static Integer maxDeletionsPerBatch = 100

    static triggers = {
        // cronjob that runs every whole hour
        cron name: 'removeExpiredAccounts', cronExpression: "0 0 * * * ?"
    }

    def execute() {
        // fetch expired un-confirmed account creations
        def criteria = SecUser.createCriteria()
        def accounts = criteria.list(max: maxDeletionsPerBatch) {
            and {
                eq("userConfirmed", false)
                lt("dateCreated", new Date() - accountExpiry)
            }
        }

        // if we have expired accounts, delete them
        if (accounts.size()) {
            // show a log message
            log.info "removing ${accounts.size()} expired un-confirmed account creations"

            // remove accounts
            accounts.each {
	            // delete all registration tokens for this user
	            RegistrationCode.findAllWhere(user: it).each {
		            // probably we should take the expiration date of the token
		            // into account as well, but for now we're not
		            it.delete()
	            }

	            // and delete the account
	            it.delete()
            }
        }
    }
}
