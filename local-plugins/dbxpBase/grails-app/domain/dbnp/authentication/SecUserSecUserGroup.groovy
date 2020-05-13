package dbnp.authentication

/**
 * Link user to group
 * @author      Heleen de Weerd
 * @since	201402
 */

import org.apache.commons.lang.builder.HashCodeBuilder

class SecUserSecUserGroup implements Serializable {
        
        SecUser secUser
        SecUserGroup secUserGroup
        
        static belongsTo = [SecUser, SecUserGroup]
    
	boolean equals(other) {
		if (!(other instanceof SecUserSecUserGroup)) {
			return false
		}

		other.secUser?.id == secUser?.id &&
                other.secUserGroup?.id == secUserGroup?.id
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (secUser) builder.append(secUser.id)
		if (secUserGroup) builder.append(secUserGroup.id)
		builder.toHashCode()
	}

	static SecUserSecUserGroup get(long secUserId, long secUserGroupId) {
		find 'from SecUserSecUserGroup where secUser.id=:secUserId and secUserGroup.id=:secUserGroupId',
			[secUserId: secUserId, secUserGroupId: secUserGroupId]
	}

	static SecUserSecUserGroup create(SecUser secUser, SecUserGroup secUserGroup, boolean flush = false) {
		new SecUserSecUserGroup(secUser: secUser, secUserGroup: secUserGroup).save(flush: flush, insert: true)
	}

	static boolean remove(SecUser secUser, SecUserGroup secUserGroup, boolean flush = false) {
		SecUserSecUserGroup instance = SecUserSecUserGroup.findBySecUserAndSecUserGroup(secUser, secUserGroup)
		instance ? instance.delete(flush: flush) : false
	}

	static void removeAll(SecUser secUser) {
		executeUpdate('DELETE FROM SecUserSecUserGroup WHERE secUser=:secUser', [secUser: secUser])
	}

	static void removeAll(SecUserGroup secUserGroup) {
		executeUpdate('DELETE FROM SecUserSecUserGroup WHERE secUserGroup=:secUserGroup', [secUserGroup: secUserGroup])
	}

	static mapping = {
		id composite: ['secUserGroup', 'secUser']
		version false
	}
}
