package dbnp.authentication

class SecRole {

	String authority

	static mapping = {
		cache true
	}

	static constraints = {
		authority blank: false, unique: true
	}

	static List<SecUser> findUsers(String authority) {
		def userRoles = SecUserSecRole.findAllBySecRole( SecRole.findByAuthority( authority ) );
		
		def users = [];
		userRoles.each { users.add( it.secUser ) }
		
		return users
	}
}
