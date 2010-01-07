dataSource {
	pooled = true
	driverClassName = "org.hsqldb.jdbcDriver"
	username = "sa"
	password = ""
}
hibernate {
    cache.use_second_level_cache=true
    cache.use_query_cache=true
    cache.provider_class='net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
        development {
		dataSource {
			dbCreate = "create-drop" // one of 'create', 'create-drop','update'
			url = "jdbc:hsqldb:mem:devDB"
		}
	}
	test {
		dataSource {
			dbCreate = "update"
			url = "jdbc:hsqldb:mem:testDb"
		}
	}
	production {
		dataSource {
			/*
			 * when releasing a new stable to the live environment
			 * you would probably comment out the dbCreate option
			 * so hibernate won't try to update (which is does not
			 * do so well) and you update the live database yourself
			 *
			 * @see http://grails.org/plugin/autobase
			 * @see http://wiki.github.com/RobertFischer/autobase/example-usage
			 */
			dbCreate = "update"
			username = "gscf"
			password = "dbnp"

			// PostgreSQL
			driverClassName = "org.postgresql.Driver"
			url = "jdbc:postgresql://localhost:5432/gscf"
                        dialect = org.hibernate.dialect.PostgreSQLDialect

			// MySQL
			//driverClassName = "com.mysql.jdbc.Driver"
			//url = "jdbc:mysql://localhost/gscf"
                        //dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"

			//In memory
			//url = "jdbc:hsqldb:file:prodDb;shutdown=true"
		}
	}
}