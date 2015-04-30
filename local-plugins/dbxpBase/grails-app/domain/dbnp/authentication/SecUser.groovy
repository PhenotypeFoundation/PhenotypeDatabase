package dbnp.authentication

class SecUser implements Serializable {
	String username         // for shibboleth this is request header: persistent-id
	String password         // for shibboleth this is springSecurityService.encodePassword("myDummyPassword", shibPersistentId)
	String displayName		// shibboleth request header: displayName
	String organization		// shibboleth request header: schacHomeOrganization
	String uid				// shibboleth request header: uid
	String voName			// shibboleth request header: coin-vo-name
	String userStatus		// shibboleth request header: coin-user-status
	String email
        String apiKey           // api key for clients using the API
	Date dateCreated

	boolean shibbolethUser = false
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
	boolean userConfirmed   // True if the user has confirmed his subscription using the link in the email
	boolean adminConfirmed  // True if the administrator has confirmed this subscription using the link in the email

        static hasMany = [
            secUserSecUserGroup: SecUserSecUserGroup
        ]
        
	static constraints = {
		username blank: false, unique: true
		password blank: true
		email blank: false
		displayName nullable: true
		organization nullable: true
		uid nullable: true
		voName nullable: true
		userStatus nullable: true
        apiKey nullable: true, unique: true
	}

	static mapping = {
		password column: '`password`'
		enabled formula: 'USER_CONFIRMED AND ADMIN_CONFIRMED'
	}

	Set<SecRole> getAuthorities() {
		SecUserSecRole.findAllBySecUser(this).collect { it.secRole } as Set
	}
        
        Set<SecUserGroup> getUserGroups() {
		SecUserSecUserGroup.findAllBySecUser(this).collect { it.secUserGroup } as Set
	}

	public boolean equals(Object y) {
		if (!(y instanceof SecUser)) {
			return false;
		}

		if (y == null) return false;

		return this.id == y.id
	}

	public boolean hasAdminRights() {
		return getAuthorities().contains(SecRole.findByAuthority('ROLE_ADMIN'));
	}

    public boolean hasTemplateAdminRights() {
        return hasAdminRights() || getAuthorities().contains(SecRole.findByAuthority('ROLE_TEMPLATEADMIN'))
    }

    public boolean hasClientRights() {
        return getAuthorities().contains(SecRole.findByAuthority('ROLE_CLIENT'));
    }

	/**
	 * Delete all remote logins for this user as well.
	 */
	def beforeDelete = {
		executeUpdate( "DELETE FROM SessionAuthenticatedUser sau WHERE sau.secUser = :secUser", [ "secUser": this ] );
	}

    /**
     * Generate a shared secret for this user
     * @void
     */
    def beforeInsert = {
        // api key set, if not we create one?
        if (!apiKey) {
            apiKey = UUID.randomUUID().toString()
        }
    }

    /**
     * Before update
     */
    def beforeUpdate = {
        // got an api key?
        if (!apiKey) {
            // generate an apiKey for this user
            apiKey = UUID.randomUUID().toString()
        }
    }

	/**
	 * return the text representation of this user
	 * @return
	 */
	def String toString() {
		if (shibbolethUser) {
			return displayName
		} else {
			return username
		}
	}
}
