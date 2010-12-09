import groovy.sql.Sql

/**
 * A script to automatically perform database changes
 *
 * @Author	Jeroen Wesbeek
 * @Since	20101209
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class DatabaseUpgrade {
	/**
	 * handle database upgrades
	 *
	 * @param dataSource
	 */
	public static void handleUpgrades(dataSource) {
		// gromming debug message
		"handeling database upgrades".grom()

		// get a sql instance
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)

		// check for study description change from template
		// to required domain field (r1245 and r1246)
		changeStudyDescription(sql)
	}

	/**
	 * execute database change r1245 / r1246 if required
	 * @param sql
	 */
	public static void changeStudyDescription(sql) {
		"changeStudyDescription".grom()
		// check if we need to perform this upgrade
		if (sql.firstRow("SELECT count(*) as total FROM template_field WHERE templatefieldentity='dbnp.studycapturing.Study' AND templatefieldname='Description'").total > 0) {
			println "perform upgrade!"
		} else {
			println "upgrade not necessary!"
		}
	}
}