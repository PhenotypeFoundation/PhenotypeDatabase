    <%@ page import="org.dbnp.gdt.AssayModule" %>
    <!-- TOPNAV //-->
    <ul class="topnav">
     <li><g:link controller="home" action="index">Home</g:link></li>
	<li>
      <a href="#">Studies</a>
      <ul class="subnav">
	    <sec:ifLoggedIn>
		<li><g:link controller="study" action="myStudies">My studies</g:link></li>
		<li><g:link controller="study" action="list">All studies</g:link></li>
	    </sec:ifLoggedIn>
		<sec:ifNotLoggedIn>
		<li><g:link controller="study" action="list">View studies</g:link></li>
		</sec:ifNotLoggedIn>
		<li><g:link controller="studyWizard" action="index" params="[jump:'create']">Create a new study</g:link></li>
		<li><g:link controller="studyWizard" action="index" params="[jump:'edit']">Edit a study</g:link></li>
		<li><g:link controller="simpleWizard" action="index">Simple wizard</g:link></li>
		<li><g:link controller="importer" action="index">Import study data</g:link></li>
        <li><g:link controller="simpleQuery" action="index">Search study data</g:link></li>
        <li><g:link controller="exporter" action="index">Export as SimpleTox</g:link></li>
      </ul>
    </li>
    <sec:ifLoggedIn>
    <li>
      <a href="#">Assays</a>
      <ul class="subnav">
        <li><g:link controller="assay" action="selectAssay">Export Data to Excel</g:link> </li>
      </ul>
    </li>
    </sec:ifLoggedIn>
	<li>
		<a href="#">Templates</a>
		<ul class="subnav">
			<af:templateEditorMenu wrap="li" />
		</ul>
	</li>
	<li>
		<a href="#">Contacts</a>
		<ul class="subnav">
			<li><g:link controller="person" action="list">View persons</g:link></li>
			<li><g:link controller="personAffiliation" action="list">View affiliations</g:link></li>
			<li><g:link controller="personRole" action="list">View roles</g:link></li>
		</ul>
	</li>
	<li>
		<a href="#">Publications</a>
		<ul class="subnav">
			<li><g:link controller="publication" action="list">View publications</g:link></li>
			<li><g:link controller="publication" action="create">Add publication</g:link></li>
		</ul>
	</li>
	<li><g:link controller="advancedQuery">Search</g:link></li>
	<g:if test="${AssayModule.count()}">
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
			</ul>
		</li>
	</sec:ifAllGranted>
	<g:if test="${session.pilot == true}">
		<li><g:link controller="pilot" action="index">Pilot</g:link></li>
	</g:if>	
</ul>
<!-- /TOPNAV //-->