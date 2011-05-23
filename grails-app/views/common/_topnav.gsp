    <%@ page import="org.dbnp.gdt.AssayModule" %>
    <%@ page import="org.codehaus.groovy.grails.commons.ConfigurationHolder" %>
    <!-- TOPNAV //-->
    <ul class="topnav">
    <li><g:link controller="home" action="index">Home</g:link></li>
    <li>
      <a href="#">Create</a>
      <ul class="subnav">
		<li><g:link controller="studyWizard" action="index" params="[jump:'create']">Create a new study</g:link></li>
		<li><g:link controller="studyWizard" action="index" params="[jump:'edit']">Edit a study</g:link></li>
	  </ul>
	</li>
	<li>
      <a href="#">Import</a>
      <ul class="subnav">
		<li><g:link controller="simpleWizard" action="index">A complete study with straightforward design</g:link></li>
		%{--<li><g:link controller="simpleWizard" action="index" params="[inferDesign: true]">A complete study with inferred design</g:link></li>--}%
	    <li><g:link controller="gdtImporter" action="index">A part of the study design</g:link></li>
	    <li><g:link controller="gdtImporter" action="index">A list of studies (choose Study)</g:link></li>
      </ul>
    </li>
    <li>
      <a href="#">Export</a>
      <ul class="subnav">
        <li><g:link controller="assay" action="assayExport">Export Assay Data to File</g:link> </li>
	    <li><g:link controller="exporter" action="index">Export studies as SimpleTox Excel file</g:link></li>
      </ul>
    </li>
	<li>
		<a href="#">Browse</a>
	    <ul class="subnav">
			<sec:ifLoggedIn>
			<li><g:link controller="study" action="myStudies">My studies</g:link></li>
			<li><g:link controller="study" action="list">All studies</g:link></li>
			</sec:ifLoggedIn>
			<sec:ifNotLoggedIn>
			<li><g:link controller="study" action="list">View studies</g:link></li>
			</sec:ifNotLoggedIn>
            <li><a href="#">Templates</a>
		    	<ul class="childnav">
					<af:templateEditorMenu wrap="li" />
				</ul>
		    </li>
			<li><a href="#">Contacts</a>
            	<ul class="childnav">
					<li><g:link controller="person" action="list">View persons</g:link></li>
					<li><g:link controller="personAffiliation" action="list">View affiliations</g:link></li>
					<li><g:link controller="personRole" action="list">View roles</g:link></li>
				</ul>
		    </li>
			<li><a href="#">Publications</a>
				<ul class="childnav">
					<li><g:link controller="publication" action="list">View publications</g:link></li>
					<li><g:link controller="publication" action="create">Add publication</g:link></li>
				</ul>
			</li>
		</ul>
	</li>
	<li>
		<a href="#">Search</a>
	    <ul class="subnav">
            <li><g:link controller="advancedQuery">Advanced search</g:link></li>
		</ul>
	</li>
	<g:if test="${ConfigurationHolder.config.modules.showInMenu && AssayModule.count()}">
		<li>
			<a href="#">Modules</a>
			<ul class="subnav">
				<g:each in="${AssayModule.list()}" var="assayModule">
					<li><a href="${assayModule.url}">${assayModule.name}</a>
				</g:each>
			</ul>
		</li>	
	</g:if>
	<sec:ifAllGranted roles="ROLE_ADMIN">
		<li>
			<a href="#">Admin</a>
			<ul class="subnav">
				<li><g:link controller="user" class="icon icon_user"><img src="${fam.icon(name: 'user')}" alt="user administration"/> List Users</g:link></li>
				<li><g:link controller="user" action="create" class="icon icon_user_add"><img src="${fam.icon(name: 'user')}" alt="user administration"/> Create User</g:link></li>
				<li><g:link controller="assayModule" action="list" class="icon icon_user_add"><img src="${fam.icon(name: 'disconnect')}" alt="module administration"/> Manage Modules</g:link></li>
				<li><g:link controller="setup" class="icon icon_user_add"><img src="${fam.icon(name: 'wand')}" alt="module administration"/> Setup wizard</g:link></li>
			    <li><g:link controller="info" class="icon icon_info"><img src="${fam.icon(name: 'lightning')}" alt="application information"/> Application information</g:link></li>
			</ul>
		</li>
	</sec:ifAllGranted>
	<g:if test="${session.pilot == true}">
		<li><g:link controller="pilot" action="index">Pilot</g:link></li>
	</g:if>	
	</ul>
	<!-- /TOPNAV //-->