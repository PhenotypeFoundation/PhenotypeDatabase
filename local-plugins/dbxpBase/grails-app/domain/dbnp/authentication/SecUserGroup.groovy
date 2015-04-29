package dbnp.authentication

/**
 * Create groups to be able to add groups to studied instead of adding people one-by-one
 * @author      Heleen de Weerd
 * @since	201402
 */

class SecUserGroup implements Serializable {

        String groupName
        String groupDescription

        static hasMany = [ secUserSecUserGroup: SecUserSecUserGroup ]

	static constraints = {
		groupName blank: false, unique: true
                groupDescription nullable: true
	}

	Set <SecUser> getUsers() {
		SecUserSecUserGroup.findAllBySecUserGroup(this).collect { it.secUser } as Set
	}

        /**
	 * return the text representation of this user
	 * @return
	 */
	def String toString() {
                return groupName
	}
}
