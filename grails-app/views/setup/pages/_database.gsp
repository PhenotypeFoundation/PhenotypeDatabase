<%
/**
 * Database configuration
 *
 * @author Jeroen Wesbeek
 * @since  20110318
 *
 * Revision information:
 * $Rev:  67319 $
 * $Author:  duh $
 * $Date:  2010-12-22 17:45:42 +0100 (Wed, 22 Dec 2010) $
 */
%>
<af:page>
	<script type="text/javascript">
		var previous = "";
		var presetValues = {
			'org.postgresql.Driver' : {
				'url'		: 'jdbc:postgresql://localhost:5432/${meta(name: 'app.name')}-${grails.util.GrailsUtil.environment}',
				'username'	: '${meta(name: 'app.name')}',
				'password'	: 'dbnp'
			},
			'org.hsqldb.jdbcDriver' : {
				'url'		: 'jdbc:hsqldb:mem:${grails.util.GrailsUtil.environment}Database',
				'username'	: 'sa',
				'password'	: ''
			}
		}

		$(document).ready(function(){
			var select = $('select[name="dataSource.driverClassName"]');
			select.bind('change',function() {
				prefilFields($(this));
			});
			prefilFields(select);
		});

		function prefilFields(selectElement) {
			var url			= $('input[name="dataSource.url"]');
			var username	= $('input[name="dataSource.username"]');
			var password	= $('input[name="dataSource.password"]');
			var db			= $('option:selected',selectElement).val();

			// remember values?
			if (previous) {
				if (url.val()) presetValues[previous]['url'] = url.val();
				if (username.val()) presetValues[previous]['username'] = username.val();
				if (password.val()) presetValues[previous]['password'] = password.val();
			}

			// change input fields
			url.val(presetValues[db]['url']);
			username.val(presetValues[db]['username']);
			password.val(presetValues[db]['password']);

			// remember selection
			previous = db;
		}
	</script>
	<h1>Database configuration</h1>

	<af:selectElement name="dataSource.driverClassName" description="database type" error="driver" optionKey="name" optionValue="description" from="[[name:'org.postgresql.Driver', description:'PostgreSQL (prefered)'],[name:'org.hsqldb.jdbcDriver', description: 'In memory']]" value="${configInfo?.properties.getProperty('dataSource.driverClassName')}">
		Choose the database of choice. Note that while this application in principle supports different database types, it is specifically developer for PostgreSQL. Choosing
		a different database may result in unexpected issues therefore choosing PostgreSQL here is advisable.
		Also note that the In Memory database exists only at runtime, which means the database is lost when the application is restarted!
	</af:selectElement>
	<af:textFieldElement name="dataSource.url" description="url" error="url" value="${configInfo?.properties.getProperty('dataSource.url')}" style="width: 300px;">
		The URL of your database, example » jdbc:postgresql://localhost:5432/gscf-ci
	</af:textFieldElement>
	<af:textFieldElement name="dataSource.username" description="username" error="username" value="${configInfo?.properties.getProperty('dataSource.username')}" style="width: 100px;">
		The username for this database
	</af:textFieldElement>
	<af:textFieldElement name="dataSource.password" description="password" error="password" value="${configInfo?.properties.getProperty('dataSource.password')}" style="width: 100px;">
		The password for this database
	</af:textFieldElement>
	<af:selectElement name="dataSource.dbCreate" description="database creation type" error="dbcreate" optionKey="name" optionValue="description" from="[[name:'update', description:'Create/Update the existing database if changes are required (prefered choice)'],[name:'create', description:'Create the database if required, but do not perform updates'],[name:'create-drop', description: 'Drop the exisiting database and create a fresh database']]" value="${configInfo?.properties.getProperty('dataSource.dbCreate')}">
		The application is able to automatically create and update your database. Choose what option suites you best.
	</af:selectElement>

	<g:if test="${connection==false}">
	<span class="message info">
		<span class="error">Could not connect to database</span>
		Please make sure the database settings are correct
		<g:if test="${configInfo?.properties.getProperty('dataSource.driverClassName') == 'org.postgresql.Driver'}">
		or create the database and database user if you did not yet do so.<br/>

		First, as root, change to user postgres and start the Postgres console:
		<pre class="brush:plain">
			su - postgres
			psql
		</pre>

		Then run the following commands:
		<pre class="brush:plain">
			create database "${configInfo?.properties.getProperty('dataSource.url').split("/").last()}";
			create user ${configInfo?.properties.getProperty('dataSource.username')} with password '${configInfo?.properties.getProperty('dataSource.password')}';
			grant all privileges on database "${configInfo?.properties.getProperty('dataSource.url').split("/").last()}" to ${configInfo?.properties.getProperty('dataSource.username')};
			ALTER DATABASE "${configInfo?.properties.getProperty('dataSource.url').split("/").last()}" OWNER TO ${configInfo?.properties.getProperty('dataSource.username')};
		</pre>

		When finished, click <i>next</i> to try again.
		</g:if>
	</span>
	</g:if>

</af:page>
