<html xmlns="http://www.w3.org/1999/html">
<head>
	<meta name="layout" content="main"/>
	<r:require module="tiptip" />
    <style stype="text/css">
    description {
        display: block;
        padding: 0;
        margin: 0;
        font-style: italic;
        font-size: 8pt;
        color: #b1b4b3;
    }
    green {
        display: inline-block;
        color: #72a951;
    }
    red {
        display: inline-block;
        color: #ff4e00;
    }
    disabled {
        color: #b1b4b3;
    }
    comment {
        color: #f54d80;
    }
    </style>
    <r:script>
        $(document).ready(function() {
            $('li').each(function() {
                if (this.hasAttribute('description')) {
                    $(this).tipTip({defaultPosition: 'left', content: this.getAttribute('description')});
                }
            });
        });
    </r:script>
</head>
<body>

<div id="nav">
	<div class="homePagePanel">
		<div class="panelTop"></div>
		<div class="panelBody">
			<h1>Application Status</h1>
			<ul>
				<li>App version: <g:meta name="app.version"></g:meta></li>
				<li>Grails version: <g:meta name="app.grails.version"></g:meta></li>
				<li>Groovy version: ${groovy.lang.GroovySystem.getVersion()}</li>
				<li>JVM version: ${System.getProperty('java.version')}</li>
				<li>Controllers: ${grailsApplication.controllerClasses.size()}</li>
				<li>Domains: ${grailsApplication.domainClasses.size()}</li>
				<li>Services: ${grailsApplication.serviceClasses.size()}</li>
				<li>Tag Libraries: ${grailsApplication.tagLibClasses.size()}</li>
				<li>Environment: ${grails.util.Environment.current}</li>
        <li>Startup date: <g:formatDate date="${grailsApplication.mainContext.getStartupDate()}" format="yyyy-MM-dd HH:mm:ss"/></li>
      </ul>
      <h1>Build Information</h1>
      <ul>
        <li>Commit: <g:meta name="scm.version"></g:meta></li>
        <li>Date: <g:meta name="build.date"></g:meta></li>
        <li>Timezone: <g:meta name="build.timezone"></g:meta></li>
        <li>Java version: <g:meta name="build.java"></g:meta></li>
        <li>OS name: <g:meta name="env.os.name"></g:meta></li>
        <li>OS version: <g:meta name="env.os.version"></g:meta></li>
        <li>Username: <g:meta name="env.username"></g:meta></li>
        <li>Computer: <g:meta name="env.computer"></g:meta></li>
        <li>Architecture: <g:meta name="env.proc.type"></g:meta></li>
        <li>Cores: <g:meta name="env.proc.cores"></g:meta></li>
      </ul>
			<h1>Installed Plugins</h1>
			<ul>
				<g:set var="pluginManager"
					   value="${applicationContext.getBean('pluginManager')}"></g:set>

				<g:each var="plugin" in="${pluginManager.allPlugins}">
                    <g:set var="comma" value="${false}"/>
					<li<g:if test="${plugin.properties.containsKey("description")}"> description="${plugin.properties.description}"</g:if>>
                        ${plugin.name} - ${plugin.version}
                        <g:if test="${plugin.properties.author}">
                            <g:if test="${plugin.properties.containsKey('authorEmail')}">
                                by <a href="mailto:${plugin.properties.authorEmail.replaceAll(" and ", ",").replaceAll(" / ",",")}?subject=Grails plugin ${plugin.name} - ${plugin.version}">${plugin.properties.author}</a>
                            </g:if>
                            <g:else>
                                by ${plugin.properties.author}
                            </g:else>
                        </g:if>
                        <g:if test="${plugin.properties.containsKey("license")}">
                            ( license:
                            <g:if test="${plugin.properties.license == "APACHE"}"><green>${plugin.properties.license}</green></g:if>
                            <g:else><red>${plugin.properties.license}</red></g:else>
                            <g:set var="comma" value="${true}"/>
                        </g:if>
                        <g:if test="${plugin.properties.containsKey("documentation")}">
                            <g:if test="${comma}">, </g:if><g:else>( </g:else><a href="${plugin.properties.documentation}" target="_new">documentation</a>
                            <g:set var="comma" value="${true}"/>
                        </g:if>
                        <g:if test="${plugin.properties.containsKey("scm")}">
                            <g:if test="${comma}">, </g:if><g:else>( </g:else><a href="${plugin.properties.scm.url}">source</a>
                            <g:set var="comma" value="${true}"/>
                        </g:if>
                        <g:if test="${comma}"> )</g:if>
                    </li>
				</g:each>
			</ul>

			<h1>Available Controllers:</h1>
			<ul>
				<g:each var="c" in="${grailsApplication.controllerClasses.sort { it.naturalName } }">
                    <li class="controller" description="package name: ${c.getFullName()}">
                        <g:if test="${!c.properties.available}"><disabled></g:if>
                        ${c.getNaturalName()}
                        (
                        <g:link controller="${c.logicalPropertyName}" action="${c.defaultAction}">direct link</g:link>
                        <g:if test="${c.properties.flows.size() > 0}">, <comment>webflow</comment></g:if>
                        )
                        <g:if test="${!c.properties.available}"></disabled></g:if>
                    </li>
				</g:each>
			</ul>

            <h1>Domain Classes</h1>
            <ul>
                <g:each var="d" in="${grailsApplication.domainClasses.sort { it.naturalName }}">
                <g:set var="c" value="${grailsApplication.controllerClasses.find { it.getNaturalName() == d.getNaturalName() + " Controller" }}"/>
                <li class="domain" description="package name: ${d.getFullName()}">
                    ${d.getNaturalName()}
                    (
                        <comment>${d.getClazz().count()} records</comment>
                        <g:if test="${c}">
                            , <g:link controller="${c.logicalPropertyName}" action="${c.defaultAction}">view/edit</g:link>
                        </g:if>
                    )
                </li>

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
