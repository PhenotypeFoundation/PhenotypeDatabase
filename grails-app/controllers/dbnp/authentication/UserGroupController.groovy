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
		
		// For the old users in the selected usergroup, remove them from the linked studies
		def selectedUsers = userGroup.getUsers()
		for(selectedUser in selectedUsers){
				def selectedGroups = selectedUser.getUserGroups().id
				def studies = Study.all.findAll{it.readers.contains(selectedUser)}

				for (studyU in studies){
						def studyGroups = studyU.readerGroups.id
						def overlap = selectedGroups.intersect(studyGroups)
						if(overlap.size() <= 1){
								studyU.removeFromReaders(selectedUser)
								studyU.save(flush: true)
						}
				}
				studies = Study.all.findAll{it.writers.contains(selectedUser)}
				for (studyU in studies){
						def studyGroups = studyU.writerGroups.id
						def overlap = selectedGroups.intersect(studyGroups)
						if(overlap.size() <= 1){
								studyU.removeFromWriters(selectedUser)
								studyU.save(flush: true)
						}
				}
				SecUserSecUserGroup.remove(selectedUser,userGroup, true)
		}

		def readerGroups = Study.all.findAll{it.readerGroups.contains(userGroup)}
		def writerGroups = Study.all.findAll{it.writerGroups.contains(userGroup)}

		if (!userGroup.save()) {
			render view: 'edit', model: buildUserGroupModel(userGroup)
			return
		}
                         
                // For the selected users, add them to the selected usergroup and the studies
                def users
                if(params.optionalUsers){                  
                    users = params.list('optionalUsers')
                    for(user in users){
                        SecUser singleUser = SecUser.get(user)
                        SecUserSecUserGroup sec = new SecUserSecUserGroup(secUser:singleUser, secUserGroup: userGroup)
                        sec.save(flush: true)
                    }
                }
                
                for (readerGroupStudy in readerGroups){
                        for(user in users){
                                SecUser singleUser = SecUser.get(user)
                                readerGroupStudy.addToReaders(singleUser)
                                readerGroupStudy.save(flush: true)
                        }
                }
                for (writerGroupStudy in writerGroups){
                       for(user in users){
                                SecUser singleUser = SecUser.get(user)
                                writerGroupStudy.addToWriters(singleUser)
                                writerGroupStudy.save(flush: true)
                        }
                }
        
		
		flash.message = "${message(code: 'default.updated.message', args: [message(code: 'userGroup.label', default: 'UserGroup'), userGroup.id])}"
		redirect action: edit, id: userGroup.id
	}

	def delete = {
		def userGroup = findById()
		if (!userGroup) return

		try {
			SecUserSecUserGroup.removeAll userGroup
			userGroup.delete flush: true
			
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

	protected void addUsers(userGroup) {
		for (String key in params.keySet()) {
			
		}
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
}
