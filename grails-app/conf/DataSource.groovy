/**
 * Default datasource configuration
 *
 * See environment specific configuration!
 *
 * @author Jeroen Wesbeek
 * @since 20110110
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
dataSource {
	pooled = true
	driverClassName = "org.hsqldb.jdbcDriver"
	dbCreate = "create-drop" // one of 'create', 'create-drop','update'
	url = "jdbc:hsqldb:mem:devDB"
	username = "sa"
	password = ""
}
hibernate {
	cache.use_second_level_cache = true
	cache.use_query_cache = true
	cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}