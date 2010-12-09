import groovy.sql.Sql
import dbnp.studycapturing.Study

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
		// get a sql instance
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)

		// execute per-change check and upgrade code
		changeStudyDescription(sql)			// r1245 / r1246
	}

	/**
	 * execute database change r1245 / r1246 if required
	 * @param sql
	 */
	public static void changeStudyDescription(sql) {
		// check if we need to perform this upgrade
		if (sql.firstRow("SELECT count(*) as total FROM template_field WHERE templatefieldentity='dbnp.studycapturing.Study' AND templatefieldname='Description'").total > 0) {
			// grom that we are performing the upgrade
			"performing database upgrade: study description".grom()

			// database upgrade required
			try {
				// get the template field id
				def id = sql.firstRow("SELECT id FROM template_field WHERE templatefieldentity='dbnp.studycapturing.Study' AND templatefieldname='Description'").id

				// iterate through all obsolete study descriptions
				sql.eachRow("SELECT study_id, template_text_fields_elt as description FROM study_template_text_fields WHERE template_text_fields_idx='Description'") { row ->
					// migrate the template description to the study object itself
					// so we don't have to bother with sql injections, etc
					def study = Study.findById( row.study_id )
					study.setFieldValue('description', row.description)
					if (!(study.validate() && study.save())) {
						throw new Exception("could not save study with id ${row.study_id}")
					}
				}

				// delete all obsolete descriptions
				sql.execute("DELETE FROM study_template_text_fields WHERE template_text_fields_idx='Description'")

				// find all template id's where this field is used
				sql.eachRow("SELECT DISTINCT template_fields_id, fields_idx FROM template_template_field WHERE template_field_id=${id}") { row ->
					// delete the template_template_field reference
					sql.execute("DELETE FROM template_template_field WHERE template_field_id=${id} AND template_fields_id=${row.template_fields_id}")

					// and lower the idx-es of the remaining fields
					sql.execute("UPDATE template_template_field SET fields_idx=fields_idx-1 WHERE fields_idx>${row.fields_idx} AND template_fields_id=${row.template_fields_id}")
				}

				// and delete the obsolete template field
				sql.execute("DELETE FROM template_field WHERE id=${id}")
			} catch (Exception e) {
				"changeStudyDescription database upgrade failed: " + e.getMessage()
			}
		}
	}
}