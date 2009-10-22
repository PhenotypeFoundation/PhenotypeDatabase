
    <!-- TOPNAV //-->
    <ul class="topnav">
      <li><g:link url="/${meta(name: 'app.name')}">Home</g:link></li>
      <li>
	<a href="#">Scaffolded controllers</a>
	<ul class="subnav"><g:each var="c" in="${grailsApplication.controllerClasses}">
	  <li><g:link controller="${c.logicalPropertyName}">${c.fullName}</g:link></li></g:each>
	</ul>
      </li>
    </ul>
    <!-- /TOPNAV //-->