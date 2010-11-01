
    <!-- TOPNAV //-->
    <ul class="topnav">
     <li><g:link controller="home" action="index">Home</g:link></li>
<sec:ifLoggedIn>
     <li><g:link controller="study" action="myStudies">My studies</g:link></li>
</sec:ifLoggedIn>
	<li>
      <a href="#">Studies</a>
      <ul class="subnav">
        <li><g:link controller="study" action="list">View studies</g:link></li>
		<li><g:link controller="wizard" action="index">Create/edit study</g:link></li>
		<li><g:link controller="importer" action="index">Import study data</g:link></li>
        <li><g:link controller="simpleQuery" action="index">Search study data</g:link></li>
      <li><g:link controller="exporter" action="index">Export as SimpleTox</g:link></li>
      </ul>
     </li>
	<li>
      <a href="#">Templates</a>
      <ul class="subnav">
        <li><g:link controller="templateEditor" action="study" params="${[standalone: true]}">Study templates</g:link></li>
        <li><g:link controller="templateEditor" action="subject" params="${[standalone: true]}">Subject templates</g:link></li>
        <li><g:link controller="templateEditor" action="event" params="${[standalone: true]}">Event templates</g:link></li>
        <li><g:link controller="templateEditor" action="samplingEvent" params="${[standalone: true]}">Sampling event templates</g:link></li>
        <li><g:link controller="templateEditor" action="sample" params="${[standalone: true]}">Sample templates</g:link></li>
        <li><g:link controller="templateEditor" action="assay" params="${[standalone: true]}">Assay templates</g:link></li>
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
      <li><g:link controller="publication" action="add">Add publication</g:link></li>
    </ul>
   </li>
    <g:if env="development">
     <li><g:link controller="query" action="index">Query database</g:link></li>
	 <li>
	  <a href="#">Scaffolded controllers</a>
	  <ul class="subnav"><g:each var="c" in="${grailsApplication.controllerClasses}">
	   <li><g:link controller="${c.logicalPropertyName}">${c.fullName}</g:link></li></g:each>
	  </ul>
     </li>
	</g:if>
   <sec:ifAllGranted roles="ROLE_ADMIN">
     <li>
	  <a href="#">User administation</a>
	  <ul class="subnav">
	    <li><g:link controller="user" class="icon icon_user">List Users</g:link></li>
	    <li><g:link controller="user" action="create" class="icon icon_user_add">Create User</g:link></li>
	    <li><g:link controller="logout" class="icon icon_cross">Sign out</g:link></li>
	  </ul>
     </li>
   </sec:ifAllGranted>
    </ul>
    <!-- /TOPNAV //-->