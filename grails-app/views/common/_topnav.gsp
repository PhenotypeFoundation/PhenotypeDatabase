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
	<sec:ifAllGranted roles="ROLE_ADMIN">
		<li>
			<a href="#"><img src="${fam.icon(name: 'user')}" alt="user administration"/></a>
			<ul class="subnav">
				<li><g:link controller="user" class="icon icon_user">List Users</g:link></li>
				<li><g:link controller="user" action="create" class="icon icon_user_add">Create User</g:link></li>
				<li><g:link controller="logout" class="icon icon_cross">Sign out</g:link></li>
			</ul>
		</li>
	</sec:ifAllGranted>
	<g:if test="${session.pilot == true}">
		<li><g:link controller="pilot" action="index">Pilot</g:link></li>
	</g:if>	
</ul>
<!-- /TOPNAV //-->