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
                           //	create a database first called gscf, add a user nmc with password nmcdsp and grant him all rights to nmc_dsp_tno
                            //	$ mysql --user=root
                            //	mysql> create database nmc_dsp_tno;
                            //	mysql> use nmc_dsp_tno;
                            //	mysql> grant all on nmc_dsp_tno.* to nmc@localhost identified by 'nmcdsp';
                            //	mysql> flush privileges;
                            //	mysql> exit
                            //	$ mysql --user=nmc -p --database=nmc_dsp_tno

                            driverClassName = "com.mysql.jdbc.Driver"
                            dbCreate =  "update"
                            username = "gscf"
                            password = "dbnp"
                            url = "jdbc:mysql://localhost/gscf"

                        //dbCreate = "update"
			//url = "jdbc:hsqldb:file:prodDb;shutdown=true"
		}
	}
}