dataSource {
	pooled = true
	driverClassName = "org.hsqldb.jdbcDriver"
	username = "sa"
	password = ""
}
hibernate {
	cache.use_second_level_cache = true
	cache.use_query_cache = true
	cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
	development {
		dataSource {
			// by default we use an in memory development database
			dbCreate = "create-drop" // one of 'create', 'create-drop','update'
			url = "jdbc:hsqldb:mem:devDB"
			//loggingSql = true
		}
	}
	ci {
		// used by build script
		dataSource {
			dbCreate = "update"
			username = "gscf"
			password = "dbnp"

			// PostgreSQL
			driverClassName = "org.postgresql.Driver"
			url = "jdbc:postgresql://localhost:5432/gscf-ci"
			dialect = org.hibernate.dialect.PostgreSQLDialect
			//logSql = true	// enable logging while not yet final
		}
	}
	test {
		dataSource {
			dbCreate = "update"
			username = "gscf"
			password = "dbnp"

			// PostgreSQL
			driverClassName = "org.postgresql.Driver"
			url = "jdbc:postgresql://localhost:5432/gscf-test"
			dialect = org.hibernate.dialect.PostgreSQLDialect
			//logSql = true	// enable logging while not yet final
		}
	}
	dbnp-test {
		dataSource {
			dbCreate = "update"
			username = "gscf"
			password = "dbnp"

			// PostgreSQL
			driverClassName = "org.postgresql.Driver"
			url = "jdbc:postgresql://localhost:5432/gscf-test"
			dialect = org.hibernate.dialect.PostgreSQLDialect
			//logSql = true	// enable logging while not yet final
		}
	}
	dbnp-demo {
		dataSource {
			dbCreate = "update"
			username = "gscf"
			password = "dbnp"

			// PostgreSQL
			driverClassName = "org.postgresql.Driver"
			url = "jdbc:postgresql://localhost:5432/gscf-demo"
			dialect = org.hibernate.dialect.PostgreSQLDialect
			//logSql = true	// enable logging while not yet final
		}
	}
	production {
		dataSource {
			dbCreate = "update"
			username = "gscf"
			password = "dbnp"

			// PostgreSQL
			driverClassName = "org.postgresql.Driver"
			url = "jdbc:postgresql://localhost:5432/gscf-www"
			dialect = org.hibernate.dialect.PostgreSQLDialect
			//logSql = true	// enable logging while not yet final
		}
	}
	www {
		// used by build script
		dataSource {
			dbCreate = "update"
			username = "gscf"
			password = "dbnp"

			// PostgreSQL
			driverClassName = "org.postgresql.Driver"
			url = "jdbc:postgresql://localhost:5432/gscf-www"
			dialect = org.hibernate.dialect.PostgreSQLDialect
			//logSql = true	// enable logging while not yet final
		}
	}
}