package dbnp.authentication

class SecUser implements Serializable {

	String username
	String password
	String email
	Date dateCreated
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
	boolean userConfirmed   // True if the user has confirmed his subscription using the link in the email
	boolean adminConfirmed  // True if the administrator has confirmed this subscription using the link in the email

	static constraints = {
		username blank: false, unique: true
		password blank: false
		email blank: false
	}

	static mapping = {
		password column: '`password`'
		enabled formula: 'USER_CONFIRMED AND ADMIN_CONFIRMED'
	}

	Set<SecRole> getAuthorities() {
		SecUserSecRole.findAllBySecUser(this).collect { it.secRole } as Set
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
}
