import dbnp.studycapturing.*

import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.util.Holders
import grails.converters.JSON
import org.dbnp.gdt.Template

import dbnp.authentication.SecRole
import dbnp.authentication.SecUser
import dbnp.configuration.*

/**
 * Home Controler
 *
 * My Description
 *
 * @author  Kees van Bochove
 * @since   20091102
 * @package studycapturing
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class HomeController {
	def springSecurityService
	def authenticationService
	def dataSource
	def gdtService

	def index = {
		// got a nostats parameter?
		if (params.get('nostats')) {
			session.nostats = (params.get('nostats') == "true") ? true : false
		}

		// create sql instance for advanced queries
		def config = Holders.config
		def sql = new groovy.sql.Sql(dataSource)
		def db  = config.dataSource.driverClassName

		// get study statistics
		def user = authenticationService.getLoggedInUser()
		def studyCount 						= Study.count()
		def readableStudyCount 				= Study.countReadableStudies(user)
		def readableAndWritableStudyCount 	= Study.countReadableAndWritableStudies(user)
		def readOnlyStudyCount 				= (readableStudyCount - readableAndWritableStudyCount)
		def noAccessStudyCount 				= studyCount - readableAndWritableStudyCount
		def publishedPublicStudyCount		= Study.countPublicStudies(true)
		def unPublishedPublicStudyCount		= Study.countPublicStudies(false)
		def publicStudyCount				= publishedPublicStudyCount + unPublishedPublicStudyCount
		def publishedPrivateStudyCount		= Study.countPrivateStudies(true)
		def unPublishedPrivateStudyCount	= Study.countPrivateStudies(false)
		def privateStudyCount				= publishedPrivateStudyCount + unPublishedPrivateStudyCount

		// daily statistics
		def startDate, endDate, date, userTotal, studyTotal, templateTotal
		def dailyStatistics = [:]
		if (db == "org.postgresql.Driver" && studyCount > 0) {
			//sql.eachRow("SELECT b.*,(select cast( now() - '120 day'::interval * random() as date) FROM Template c WHERE c.id=b.id) as newDate FROM Template b WHERE b.id IN (SELECT id FROM Template a)") {sql.execute(sprintf("UPDATE Template SET date_created='%s' WHERE id=%s", it.newDate, it.id))}
			def studiesPerDay	= sql.rows("SELECT DISTINCT date_trunc('day', a.date_created) as day, (SELECT count(b.*) FROM study b WHERE date_trunc('day', b.date_created) = date_trunc('day', a.date_created)) as count FROM study a ORDER BY day ASC")
			def usersPerDay		= sql.rows("SELECT DISTINCT date_trunc('day', a.date_created) as day, (SELECT count(b.*) FROM sec_user b WHERE date_trunc('day', b.date_created) = date_trunc('day', a.date_created)) as count FROM sec_user a ORDER BY day ASC")
			def templatesPerDay	= sql.rows("SELECT DISTINCT date_trunc('day', a.date_created) as day, (SELECT count(b.*) FROM template b WHERE date_trunc('day', b.date_created) = date_trunc('day', a.date_created)) as count FROM template a ORDER BY day ASC")

			// combine daily statistics
			dailyStatistics = [:]
			startDate 		= (usersPerDay[0].day <= studiesPerDay[0].day) ? usersPerDay[0].day : studiesPerDay[0].day
			startDate		= (templatesPerDay[0].day != null && templatesPerDay[0].day < startDate) ? templatesPerDay[0].day : startDate
			endDate 		= (usersPerDay[usersPerDay.size()-1].day >= studiesPerDay[studiesPerDay.size()-1].day) ? usersPerDay[usersPerDay.size()-1].day : studiesPerDay[studiesPerDay.size()-1].day
			endDate			= (templatesPerDay[0].day != null && templatesPerDay[templatesPerDay.size()-1].day > endDate) ? templatesPerDay[templatesPerDay.size()-1].day : endDate
			date			= startDate.clone()

			userTotal		= 0
			studyTotal		= 0
			templateTotal	= 0
			while (date <= endDate) {
				def userDay		= usersPerDay.find{ it.day == date }
				def studyDay	= studiesPerDay.find{ it.day == date }
				def templateDay	= templatesPerDay.find{ it.day == date }
				def users		= (userDay) ? userDay.count : 0
				def studies		= (studyDay) ? studyDay.count : 0
				def templates	= (templateDay) ? templateDay.count : 0

				userTotal += users
				studyTotal += studies
				templateTotal += templates

				dailyStatistics[ date.clone() ] = [
					users			: users,
					userTotal		: userTotal,
					studies			: studies,
					studyTotal		: studyTotal,
					templates		: templates,
					templateTotal	: templateTotal
				]

                use ( groovy.time.TimeCategory ) {
                    date = date + 1.day
                }
			}
		}
	
		[
			// config
			showstats					: (session?.nostats) ? !session.nostats : true,

			// daily statistics
			startDate					: startDate,
			dailyStatistics				: dailyStatistics,

			// study
			studyCount					: studyCount,
			publishedPublicStudyCount	: publishedPublicStudyCount,
			unPublishedPublicStudyCount	: unPublishedPublicStudyCount,
			publicStudyCount			: publicStudyCount,
			publishedPrivateStudyCount	: publishedPrivateStudyCount,
			unPublishedPrivateStudyCount: unPublishedPrivateStudyCount,
			privateStudyCount			: privateStudyCount,
			readOnlyStudyCount			: readOnlyStudyCount,
			readWriteStudyCount			: readableAndWritableStudyCount,
			noAccessStudyCount			: noAccessStudyCount,
			readableTemplates			: org.dbnp.gdt.Template.count(),

			// miscelaneous
			facebookLikeUrl				: '/',
			db							: db,
            issueUrl                    : config.gscf.issueURL
		]
	}

	/**
	 * Quicksearch Closure
	 */
	def ajaxQuickSearch = {
		def query	= params.name_startsWith
		def result	= [ total: 0, data: [] ]
		def user	= authenticationService.getLoggedInUser()

		// search studies
		Study.textSearchReadableStudies(user,query).each { study ->
			result.data << [
				link		: createLink(controller:'study', action:'show', id:study.id),
			    name		: "${study.title}",
				category	: 'Study'
			]
		}

		// search templates
		Template.createCriteria().listDistinct {
			or {
				ilike("name", "%${query}%")
				ilike("description", "%${query}%")
			}
		}.each { template ->
			def entityName = template.entity.toString().split(/\./)
			def encodedEntity = gdtService.encodeEntity(template.entity.toString()).decodeURL()

			result.data << [
				link		: createLink(controller:'templateEditor', action:'template', params:[entity:encodedEntity, standalone:true, template:template.id]),
			    name		: "${template.name}",
				category	: "${entityName[entityName.size()-1]} Template"
			]
		}

		// set total
		result.total = result.data.size()

		// got results?
		if (!result.total) {
			result.data << [
			    link	: '',
				name	: "no results",
				category: ""
			]
		}

		// set output header to json
		response.contentType = 'application/json'

		// render result
		if (params.callback) {
			render "${params.callback}(${result as JSON})"
		} else {
			render result as JSON
		}
	}

	/**
	 * Log the user in as admin and jump to the setup wizard
	 */
	def setup = {
		def config	= Holders.config
		def db		= config.dataSource.driverClassName
		def user	= authenticationService.getLoggedInUser()

		// are we using the in-memory database in a non-development environment?
		if (db == "org.hsqldb.jdbcDriver" && grails.util.GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT) {
			// log in as administrator
			springSecurityService.reauthenticate(
				config.authentication.users.admin.username,
				config.authentication.users.admin.password
			)

			// and jump to the setup controller
			redirect(controller:"setup")
		}

		redirect(controller:"home")
	}
}