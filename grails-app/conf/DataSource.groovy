dataSource {
	pooled = true
	driverClassName = "org.hsqldb.jdbcDriver"
	username = "sa"
	password = ""
}
hibernate {
    cache.use_second_level_cache=true
    cache.use_query_cache=true
    cache.provider_class='com.opensymphony.oscache.hibernate.OSCacheProvider'
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

                            dbCreate =  "update"
                            username = "gscf"
                            password = "dbnp"

                            //Postgres
                            driverClassName = "org.postgresql.Driver"
                            url = "jdbc:postgresql://localhost:5432/gscf"

                            //MySQL
                            //url = "jdbc:mysql://localhost/gscf"

                            //In memory
                            //driverClassName = "com.mysql.jdbc.Driver"
                            //url = "jdbc:hsqldb:file:prodDb;shutdown=true"
		}
	}
}