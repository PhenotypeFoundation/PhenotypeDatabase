<html>
<head>
	<meta name="layout" content="main"/>
</head>
<body>

<div id="nav">
	<div class="homePagePanel">
		<div class="panelTop"></div>
		<div class="panelBody">
			<h1>Application Status</h1>
			<ul>
				<li>App version: <g:meta name="app.version"></g:meta></li>
				<li>App revision: <a href="https://trac.nbic.nl/gscf/changeset/<g:meta name="app.build.svn.revision"></g:meta>" target="_new"><g:meta name="app.build.svn.revision"></g:meta></a></li>
				<li>Grails version: <g:meta name="app.grails.version"></g:meta></li>
				<li>Groovy version: ${org.codehaus.groovy.runtime.InvokerHelper.getVersion()}</li>
				<li>JVM version: ${System.getProperty('java.version')}</li>
				<li>Controllers: ${grailsApplication.controllerClasses.size()}</li>
				<li>Domains: ${grailsApplication.domainClasses.size()}</li>
				<li>Services: ${grailsApplication.serviceClasses.size()}</li>
				<li>Tag Libraries: ${grailsApplication.tagLibClasses.size()}</li>
			</ul>
			<h1>Installed Plugins</h1>
			<ul>
				<g:set var="pluginManager"
					   value="${applicationContext.getBean('pluginManager')}"></g:set>

				<g:each var="plugin" in="${pluginManager.allPlugins}">
					<li>${plugin.name} - ${plugin.version}</li>
				</g:each>

			</ul>

			<h1>Request headers:</h1>
 			<ul>
				<g:each in="${request.headerNames}" var="r">
					 <li>${r} : ${request.getHeader(r)}</li>
				</g:each>
			 </ul>
		</div>
		<div class="panelBtm"></div>
	</div>
</div>


</body>
</html>