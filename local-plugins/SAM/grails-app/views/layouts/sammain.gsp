<% /* Use the default module layout as a base for our layout */ %>
<g:applyLayout name="module">
	<html>
	    <head>
	        <title><g:layoutTitle default="" /> | Measurements ${module} Module | dbXP</title>

	        <r:require modules="sam2"/>

	        <g:layoutHead />
			
			<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon"/>
	        
	        <script type="text/javascript">baseUrl = '${grailsApplication.config.grails.serverURL}';</script>
	    </head>
	    <body>
			<content tag="topnav">
				<% /* Insert only li tags for the top navigation, without surrounding ul */ %>
				<li><g:link controller="SAMHome" params="${[module: module]}">Home</g:link></li>
				<li>
					<a href="#">Browse</a>
					<ul class="subnav">
		    			<!-- <li><g:link controller="measurement" params="${[module: module]}">Measurements</g:link></li> -->
						<li><g:link controller="feature" params="${[module: module]}">Features</g:link></li>
						<li><g:link controller="SAMAssay" params="${[module: module]}">Assays</g:link></li>
                        <li><g:link controller="platform" params="${[module: module]}">Platforms</g:link></li>
		    		</ul>
		    	</li>
				<li>
					<a href="#">Import</a>
					<ul class="subnav">
                        <li><g:link controller="SAMImporter" action="upload" params="${[importer: "Platforms", module: module]}">Platforms</g:link></li>
                        <li><g:link controller="SAMImporter" action="upload" params="${[importer: "Features", module: module]}">Features</g:link></li>
                        <li><g:link controller="SAMImporter" action="upload" params="${[importer: "Measurements (sample layout)", module: module]}">Measurements (sample layout)</g:link></li>
                        <li><g:link controller="SAMImporter" action="upload" params="${[importer: "Measurements (subject layout)", module: module]}">Measurements (subject layout)</g:link></li>
		    		</ul>
		    	</li>
                <li>
                    <a href="${grailsApplication.config.gscf.baseURL + grailsApplication.config.gscf.documents.sam_userguide}" target="_blank">
                        User Guide
                        <img src="${fam.icon(name:"page_white_acrobat")}" alt="(pdf)" style="vertical-align:text-bottom;"/>
                    </a>
                </li>
				<li><g:link url="${grailsApplication.config.gscf.baseURL}">Go to GSCF</g:link></li>
                <g:if test="${grailsApplication.config.module.showVersionInfo}">
                    <li style="font-size: 9px; color: #888;"><g:message code="meta.app.version" default="Version: {0}" args="[meta(name: 'app.version')]"/><br />Changeset: <g:render template="/version"/></li>
                </g:if>
			</content>
			<div id="contextmenu" class="buttons">
				<ul>
					<g:pageProperty name="page.contextmenu" />
				</ul>					
			</div>
			
			<g:if test="${flash.message}">
				<p class="message">${flash.message.toString()}</p>
			</g:if>
			<g:if test="${flash.error}">
				<p class="error">${flash.error.toString()}</p>
			</g:if>
							
	        <g:layoutBody />
	    </body>
	</html>
</g:applyLayout>
