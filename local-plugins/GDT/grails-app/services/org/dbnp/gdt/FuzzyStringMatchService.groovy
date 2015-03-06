/**
 * FuzzyStringMatchService Service
 * 
 * A String Comparison service utilizing the PostgreSQL
 * plugin: fuzzystrmatch
 *
 * @author  Jeroen Wesbeek (work@osx.eu)
 * @since	20110426
 * @package	org.dbnp.gdt
 *
 * Revision information:
 * $Rev: 1430 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
 */
package org.dbnp.gdt

import org.codehaus.groovy.grails.commons.ConfigurationHolder

class FuzzyStringMatchService {
	def dataSource
    def grailsApplication
	static transactional	= false
	static isPostgres		= false
	static checkForSupport	= true
	static hasSupport		= false

	private checkForSupport() {
		if (checkForSupport) {
			// get a sql instance
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)

			// get configuration
			def config = grailsApplication.config
			def db = config.dataSource.driverClassName

			// check for string comparison features?
			if (db == "org.postgresql.Driver") {
				// remember we are using a PostgreSQL backend
				isPostgres = true

				// check for fuzzystrmatch database support
				hasSupport = (sql.firstRow("SELECT count(*) as count FROM pg_proc WHERE proname='soundex'").count > 0)

				// make sure we do not check anymore
				checkForSupport = false

				// show installation feedback only once
				if (!hasSupport) {
					println "----------------------{ FuxxyStringMatchService.groovy }--------------------"
					println "PostgreSQL string comparison functions NOT available, to enable:"
					println "1. apt-get install postgresql-contrib-X.Y"
					println "2. as user postgres execute the following command:"
					println "		psql -d dBNAME -f /usr/share/postgresql/8.3/contrib/fuzzystrmatch.sql"
					println "   where DBNAME is the name of the database in use by this application"
					println "   (this instance uses: ${config.dataSource.url})"
					println "----------------------------------------------------------------------------"
				}
			}
		}

		return hasSupport
	}

	/**
	 * find all values that differ to a certain degree from a given value
	 * @param entity
	 * @param propertyName
	 * @param value
	 * @return
	 */
	def findByDifference(entity, propertyName, value) {
		findByDifference(entity, propertyName, value, 3)
	}
	def findByDifference(entity, propertyName, value, difference) {
		def results = []
		def entityName = entity.toString().split(/\./).last().toLowerCase()

		// is this PostgreSQL and do we support fuxxy string matching?
		if (false && checkForSupport()) {
			// get a sql instance
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)

			// yes, perform a postgres native soundex query
			try {
				sql.eachRow(
					sprintf("SELECT DISTINCT %s, difference(%s, '%s') as difference,id FROM %s WHERE difference(%s, '%s')>=3 ORDER BY difference,%s ASC",
						propertyName,
						propertyName,
						value,
						entityName,
						propertyName,
						value,
						propertyName
					)
				) { row ->
					results << [id: row.getProperty('id'), label: row.getProperty(propertyName)]
				}
			} catch (Exception e) {
				def message = e.getMessage()
				if (message =~ /column "([^\"]+)" does not exist/) {
					throw new Exception("no such property \"${propertyName}\" for ${entity.toString()}")
				} else {
					throw new Exception("unknown error occured on the PostgreSQL side: ${message}")
				}
			}
		} else {
			// no, perform a GORM like find instead
			try {
				entity.createCriteria().list {
					or {
						ilike(propertyName, "%${value}%")
						ilike(propertyName, "${value}%")
						ilike(propertyName, "%${value}")
					}
				}.each { element ->
					results << [ id: element.getProperty('id'), label: element.getProperty(propertyName) ]
				}
			} catch (Exception e) {
				throw new Exception("no such property \"${propertyName}\" for ${entity.toString()}")
			}
		}

		return results
	}
}