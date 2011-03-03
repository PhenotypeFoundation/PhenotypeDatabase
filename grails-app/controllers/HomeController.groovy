import dbnp.studycapturing.*
import dbnp.authentication.*
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import grails.converters.JSON
import org.dbnp.gdt.Template

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
	def authenticationService
	def dataSource
	def gdtService

	def index = {
		// create sql instance for advanced queries
		def config = ConfigurationHolder.config
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
		if (db == "org.postgresql.Driver") {
			//sql.eachRow("SELECT b.*,(select cast( now() - '120 day'::interval * random() as date) FROM Template c WHERE c.id=b.id) as newDate FROM Template b WHERE b.id IN (SELECT id FROM Template a)") {sql.execute(sprintf("UPDATE Template SET date_created='%s' WHERE id=%s", it.newDate, it.id))}
			def studiesPerDay	= sql.rows("SELECT DISTINCT date_trunc('day', a.date_created) as day, (SELECT count(b.*) FROM study b WHERE date_trunc('day', b.date_created) = date_trunc('day', a.date_created)) as count FROM study a ORDER BY day ASC")
			def usersPerDay		= sql.rows("SELECT DISTINCT date_trunc('day', a.date_created) as day, (SELECT count(b.*) FROM sec_user b WHERE date_trunc('day', b.date_created) = date_trunc('day', a.date_created)) as count FROM sec_user a ORDER BY day ASC")
			def templatesPerDay	= sql.rows("SELECT DISTINCT date_trunc('day', a.date_created) as day, (SELECT count(b.*) FROM template b WHERE date_trunc('day', b.date_created) = date_trunc('day', a.date_created)) as count FROM template a ORDER BY day ASC")

			// combine daily statistics
			dailyStatistics = [:]
			long oneDay		= (1 * 24 * 60 * 60 * 1000)
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
				date.setTime(date.getTime() + oneDay)
			}
		}

		[
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
			facebookLikeUrl				: '/'
		]
	}

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
		Template.createCriteria().list {
			or {
				ilike("name", "%${query}%")
				ilike("description", "%${query}%")
			}
		}.each { template ->
			def entityName = template.entity.toString().split(/\./)
			def encodedEntity = gdtService.encryptEntity(template.entity.toString()).decodeURL()

			result.data << [
				link		: createLink(controller:'templateEditor', action:'template', params:[entity:encodedEntity, standalone:true, template:template.id]),
			    name		: "${template.name}",
				category	: "${entityName[entityName.size()-1]} Template"
			]
		}

		// set total
		result.total = result.data.size()

		// render result
		if (params.callback) {
			render "${params.callback}(${result as JSON})"
		} else {
			render result as JSON
		}
	}
}