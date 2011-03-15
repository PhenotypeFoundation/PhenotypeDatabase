import groovy.sql.Sql
import dbnp.studycapturing.Study
import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * A script to automatically perform database changes
 *
 * @Author Jeroen Wesbeek
 * @Since 20101209
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

		// get configuration
		def config = ConfigurationHolder.config
		def db = config.dataSource.driverClassName

		// execute per-change check and upgrade code
		changeStudyDescription(sql, db)				// r1245 / r1246
		changeStudyDescriptionToText(sql, db)		// r1327
		changeTemplateTextFieldSignatures(sql, db)	// prevent Grails issue, see http://jira.codehaus.org/browse/GRAILS-6754
		setAssayModuleDefaultValues(sql, db)		// r1490
		dropMappingColumnNameConstraint(sql, db)	// r1525
		makeMappingColumnValueNullable(sql, db)		// r1525
		alterStudyAndAssay(sql, db)					// r1594
	}

	/**
	 * execute database change r1245 / r1246 if required
	 * @param sql
	 * @param db
	 */
	public static void changeStudyDescription(sql, db) {
		// check if we need to perform this upgrade
		if (sql.firstRow("SELECT count(*) as total FROM template_field WHERE templatefieldentity='dbnp.studycapturing.Study' AND templatefieldname='Description'").total > 0) {
			// grom that we are performing the upgrade
			if (String.metaClass.getMetaMethod("grom")) "performing database upgrade: study description".grom()

			// database upgrade required
			try {
				// get the template field id
				def id = sql.firstRow("SELECT id FROM template_field WHERE templatefieldentity='dbnp.studycapturing.Study' AND templatefieldname='Description'").id

				// iterate through all obsolete study descriptions
				sql.eachRow("SELECT study_id, template_text_fields_elt as description FROM study_template_text_fields WHERE template_text_fields_idx='Description'") { row ->
					// migrate the template description to the study object itself
					// so we don't have to bother with sql injections, etc
					def study = Study.findById(row.study_id)
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
				println "changeStudyDescription database upgrade failed: " + e.getMessage()
			}
		}
	}

	/**
	 * execute database change r1327 if required
	 * @param sql
	 * @param db
	 */
	public static void changeStudyDescriptionToText(sql, db) {
		// are we running postgreSQL ?
		if (db == "org.postgresql.Driver") {
			// check if column 'description' in table 'study' is not of type 'text'
			if (sql.firstRow("SELECT count(*) as total FROM information_schema.columns WHERE columns.table_schema::text = 'public'::text AND columns.table_name='study' AND column_name='description' AND data_type != 'text'").total > 0) {
				// grom that we are performing the upgrade
				if (String.metaClass.getMetaMethod("grom")) "performing database upgrade: study description to text".grom()

				// database upgrade required
				try {
					// change the datatype of study::description to text
					sql.execute("ALTER TABLE study ALTER COLUMN description TYPE text")
				} catch (Exception e) {
					println "changeStudyDescriptionToText database upgrade failed: " + e.getMessage()
				}
			}
		}
	}

	/**
	 * it appears that some TEXT template fields are not of type 'text'
	 * due to an issue in how Grails' GORM works with domain inheritance
	 * (see http://jira.codehaus.org/browse/GRAILS-6754)
	 * @param sql
	 * @param db
	 */
	public static void changeTemplateTextFieldSignatures(sql, db) {
		if (db == "org.postgresql.Driver") {
			// check if any TEXT template fields are of type 'text'
			sql.eachRow("SELECT columns.table_name as tablename FROM information_schema.columns WHERE columns.table_schema::text = 'public'::text AND column_name='template_text_fields_elt' AND data_type != 'text';")
				{ row ->
					if (String.metaClass.getMetaMethod("grom")) "performing database upgrade: ${row.tablename} template_text_fields_string/elt to text".grom()
					try {
						// change the datatype of text fields to text
						sql.execute(sprintf("ALTER TABLE %s ALTER COLUMN template_text_fields_elt TYPE text", row.tablename))
						sql.execute(sprintf("ALTER TABLE %s ALTER COLUMN template_text_fields_string TYPE text", row.tablename))

					} catch (Exception e) {
						println "changeTemplateTextFieldSignatures database upgrade failed: " + e.getMessage()
					}
				}
		}
	}

	/**
	 * The fields 'notify' and 'openInFrame' have been added to AssayModule. However, there
	 * seems to be no method to setting the default values of these fields in the database. They
	 * are set to NULL by default, so all existing fields have 'NULL' set.
	 * This method sets the default values
	 * @param sql
	 * @param db
	 */
	public static void setAssayModuleDefaultValues(sql, db) {
		// do we need to perform this upgrade?
		if ((db == "org.postgresql.Driver" || db == "com.mysql.jdbc.Driver") &&
			(sql.firstRow("SELECT * FROM assay_module WHERE notify IS NULL") || sql.firstRow("SELECT * FROM assay_module WHERE open_in_frame IS NULL"))
		) {
			if (String.metaClass.getMetaMethod("grom")) "performing database upgrade: assay_module default values for boolean fields".grom()

			try {
				sql.execute("UPDATE assay_module SET notify=" + ((db == "org.postgresql.Driver") ? 'FALSE' : '0') + " WHERE notify IS NULL")
			} catch (Exception e) {
				println "setAssayModuleDefaultValues database upgrade failed, notify field couldn't be set to default value: " + e.getMessage()
			}

			// set open_in_frame?
			try {
				sql.execute("UPDATE assay_module SET open_in_frame=" + ((db == "org.postgresql.Driver") ? 'TRUE' : '1') + " WHERE open_in_frame IS NULL")
			} catch (Exception e) {
				// Maybe gdt plugin is not updated yet after revision 109 ?
				println "setAssayModuleDefaultValues database upgrade failed, openInFrame field couldn't be set to default value: " + e.getMessage()
			}
		}
	}

	/**
	 * Drop the unique constraint for the "name" column in the MappingColumn domain
	 *
	 * @param sql
	 * @param db
	 */
	public static void dropMappingColumnNameConstraint(sql, db) {
		// are we running postgreSQL ?
		if (db == "org.postgresql.Driver") {
			if (sql.firstRow("SELECT * FROM pg_constraint WHERE contype='mapping_column_name_key'")) {
				if (String.metaClass.getMetaMethod("grom")) "performing database upgrade: mapping column name constraint".grom()
				try {
					// Check if constraint still exists
					sql.execute("ALTER TABLE mapping_column DROP CONSTRAINT mapping_column_name_key")
				} catch (Exception e) {
					println "dropMappingColumnNameConstraint database upgrade failed, `name` field unique constraint couldn't be dropped: " + e.getMessage()
				}
			}
		}
	}

	/**
	 * the importer requires the value field to be nullable
	 * @param sql
	 * @param db
	 */
	public static void makeMappingColumnValueNullable(sql, db) {
		// are we running postgreSQL?
		if (db == "org.postgresql.Driver") {
			// do we need to perform this update?
			if (sql.firstRow("SELECT * FROM information_schema.columns WHERE columns.table_name='mapping_column' AND columns.column_name='value' AND is_nullable='NO'")) {
				if (String.metaClass.getMetaMethod("grom")) "performing database upgrade: making mapping_column::value nullable".grom()

				try {
					sql.execute("ALTER TABLE mapping_column ALTER COLUMN value DROP NOT NULL")
				} catch (Exception e) {
					println "makeMappingColumnValueNullable database upgrade failed: " + e.getMessage()
				}
			}
		}
	}

	/**
	 * The field study.code has been set to be nullable
	 * The field assay.externalAssayId has been removed
	 * @param sql
	 * @param db
	 */
	public static void alterStudyAndAssay(sql, db) {
		def updated = false

		// are we running postgreSQL ?
		if (db == "org.postgresql.Driver") {
			// see if table assay contains a column external_assayid
			if (sql.firstRow("SELECT * FROM information_schema.columns WHERE columns.table_name='assay' AND columns.column_name='external_assayid'")) {
				if (String.metaClass.getMetaMethod("grom")) "performing database upgrade: dropping column 'external_assayid' from table 'assay'".grom()

				try {
					sql.execute("ALTER TABLE assay DROP COLUMN external_assayid")
					updated = true
				} catch (Exception e) {
					println "alterStudyAndAssay database upgrade failed, externalAssayId could not be removed from assay: " + e.getMessage()
				}

			}

			// see if table study contains a column code which is not nullable
			if (sql.firstRow("SELECT * FROM information_schema.columns WHERE columns.table_name='study' AND columns.column_name='code' AND is_nullable='NO'")) {
				if (String.metaClass.getMetaMethod("grom")) "performing database upgrade: dropping column 'code' from table 'study'".grom()

				try {
					sql.execute("ALTER TABLE study ALTER COLUMN code DROP NOT NULL")
					updated = true
				} catch (Exception e) {
					println "alterStudyAndAssay database upgrade failed, study.code could not be set to accept null values: " + e.getMessage()
				}
			}

			// Load all studies and save them again. This prevents errors on saving later
			if (updated) {
				if (String.metaClass.getMetaMethod("grom")) "re-saving studies...".grom()

				Study.list().each { study ->
					if (String.metaClass.getMetaMethod("grom")) "re-saving study: ${study}".grom()
					study.save()
				}
			}
		}
	}
}