package dbnp.authentication

import grails.converters.JSON
import dbnp.studycapturing.Study
import org.springframework.dao.DataIntegrityViolationException

/**
 * Controller to create UserGroups
 * @author Heleen de Weerd
 * @date 201502
 */
class UserGroupController {

	def springSecurityService

	static defaultAction = 'search'

	def create = {
        	def userGroup = new SecUserGroup(params)
                def users = SecUser.all
		[userGroup: userGroup, users:users]
	}

	def save = {
		def userGroup = new SecUserGroup(params)

                if (!userGroup.save(flush: true)) {
			render view: 'create', model: [userGroup: userGroup]
			return
		}

		flash.message = "${message(code: 'default.created.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), userGroup.id])}"
		redirect action: edit, id: userGroup.id
	}

	def edit = {
		def userGroup = findById()
                
		if (!userGroup) return

                return buildUserGroupModel(userGroup)
	}

	def update = {

		SecUserGroup userGroup = findById()

		if (!userGroup) return

		if (!versionCheck('usergroup.label', 'UserGroup', userGroup, [userGroup: userGroup])) {
			return
		}

		def oldDescription = userGroup.groupDescription
		userGroup.properties = params
		if (params.groupDescription && !params.groupDescription.equals(oldDescription)) {
			userGroup.groupDescription = params.groupDescription
		}

		def studies = Study.all

		def newUserIds = params.list('optionalUsers').collect() { it.toLong() }
		def currentUserIds = userGroup.getUsers().id

		def removeUsers = (currentUserIds - newUserIds).collect() { SecUser.get(it) }
		def addUsers = (newUserIds - currentUserIds).collect() { SecUser.get(it) }

		removeUsers.each() { user ->

			SecUserSecUserGroup.remove(user,userGroup, true)
			removeUserWhenNoOtherAccess( user, studies )
		}

		if ( addUsers.size() != 0 ) {

			addUsers.each() { user ->
				SecUserSecUserGroup.create(user,userGroup, true)
			}

			studies.findAll() { it.readerGroups.id.contains( userGroup.id ) }.each() { study ->
				addUsers.each() { user ->
					if ( !study.readers.contains( user ) ) {
						study.addToReaders( user )
						study.save( flush: true )
					}
				}
			}

			studies.findAll() { it.writerGroups.id.contains( userGroup.id ) }.each() { study ->
				addUsers.each() { user ->
					if ( !study.writers.contains( user ) ) {
						study.addToWriters( user )
						study.save( flush: true )
					}
				}
			}
		}

		flash.message = "${message(code: 'default.updated.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), userGroup.id])}"
		redirect action: edit, id: userGroup.id
	}

	def delete = {
		def userGroup = findById()
		if (!userGroup) return

		try {

			userGroup.users.each() { user ->
				removeUserWhenNoOtherAccess( user, Study.all )
			}

			SecUserSecUserGroup.removeAll(userGroup)
			userGroup.delete(flush: true)
			
			flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
			redirect action: search
		}
		catch (DataIntegrityViolationException e) {
			flash.userError = "${message(code: 'default.not.deleted.message', args: [message(code: 'userGroup.label', default: 'userGroup'), params.id])}"
			redirect action: edit, id: params.id
		}
	}

	def search = {

	}

	def userGroupSearch = {

		boolean useOffset = params.containsKey('offset')
		params.max = params.max ?: 10
		params.offset = params.offset ?: 0

		def hql = new StringBuilder('FROM SecUserGroup u WHERE 1=1 ')
		def queryParams = [:]

		for (name in ['groupName']) {
			if (params[name]) {
				hql.append " AND LOWER(u.$name) LIKE :$name"
				queryParams[name] = params[name].toLowerCase() + '%'
			}
		}

		int totalCount = SecUserGroup.executeQuery("SELECT COUNT(DISTINCT u) $hql", queryParams)[0]

                def max = params.max as Integer
		def offset = params.offset as Integer

		String orderBy = ''
		if (params.sort) {
			orderBy = " ORDER BY u.$params.sort ${params.order ?: 'ASC'}"
		}
        
		def results = SecUserGroup.executeQuery(
				"SELECT DISTINCT u $hql $orderBy",
				queryParams, [max: max, offset: offset])
		def model = [results: results, totalCount: totalCount, searched: true]

		// add query params to model for paging
		for (name in ['groupName']) {
		 	model[name] = params[name]
		}

		render view: 'search', model: model
	}

	/**
	 * Ajax call used by autocomplete textfield.
	 */
	def ajaxUserGroupSearch = {

		def jsonData = []

		if (params.term?.length() > 2) {
			String groupName = params.term

			setIfMissing 'max', 10, 100

			def results = SecUser.executeQuery(
					"SELECT DISTINCT u.groupName " +
					"FROM SecUserGroup u " +
					"WHERE LOWER(u.groupName) LIKE :name " +
					"ORDER BY u.groupName",
					[name: "${groupName.toLowerCase()}%"],
					[max: params.max])

			for (result in results) {
				jsonData << [value: result]
			}
		}

		render text: jsonData as JSON, contentType: 'application/json'
	}

	protected Map buildUserGroupModel(userGroup) {

                def users = SecUser.all.sort { it.username }
                def selectedUsers = userGroup.getUsers()
                
		return [userGroup: userGroup, users:users, selectedUsers:selectedUsers]
	}

	protected findById() {
		if(!params.id) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
			redirect action: search
		}

		def userGroup = SecUserGroup.get(params.id)
		
		if (!userGroup) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), params.id])}"
			redirect action: search
		}

		userGroup
	}

	protected boolean versionCheck(String messageCode, String messageCodeDefault, instance, model) {
		if (params.version) {
			def version = params.version.toLong()
			if (instance.version > version) {
				instance.errors.rejectValue('version', 'default.optimistic.locking.failure',
						[message(code: messageCode, default: messageCodeDefault)] as Object[],
						"Another user has updated this instance while you were editing")
				render view: 'edit', model: model
				return false
			}
		}
		true
	}

	private removeUserWhenNoOtherAccess( SecUser user, List<Study> studies ) {

		def groupIds = user.getUserGroups().id

		// If user does not have read access to study via another group, remove user from study readers
		studies.findAll() { it.readers.contains(user) && it.readerGroups.id.intersect( groupIds ).size() == 0 }.each() { study ->
			study.removeFromReaders( user )
			study.save( flush: true )
		}

		studies.findAll() { it.writers.contains(user) && it.writerGroups.id.intersect( groupIds ).size() == 0 }.each() { study ->
			study.removeFromWriters( user )
			study.save( flush: true )
		}
	}
}
