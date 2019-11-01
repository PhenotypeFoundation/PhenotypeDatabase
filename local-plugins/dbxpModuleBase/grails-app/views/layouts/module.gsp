<%@ page contentType="text/html;charset=UTF-8" %>
<% /*
	This layout can be used as a base for your module. It loads jquery, jquery-ui and jquery-datatables. It also
	loads javascripts for the top navigation and pagination in datatables (see documentation below about pagination).

	You can use this layout in your projects by setting

		<meta name="layout" content="module">

	This way you will use the default layout. However, that way you have to set the to navigation bar in each view. You can 
	also create your own layout and extend the module layout. This can be done like the following example. 

	<g:applyLayout name="module">
		<html>
		<head>
	        <title><g:layoutTitle default="dbXP test module | dbNP"/></title>
			<g:layoutHead />
		</head>
		<body>
			<content tag="topnav">
				<!-- Insert only li tags for the top navigation, without surrounding ul -->
				<li><a href="${resource(dir: '')}">Home</a></li>
				<li>
					<a href="#" onClick="return false;">GSCF</a>
					<ul class="subnav">
						<li><g:link url="${grails.util.Holders.config.gscf.baseURL}">Go to GSCF</g:link></li>
					</ul>
				</li>
			</content>
			<g:layoutBody/>
		</body>
		</html>
	</g:applyLayout>

	You have to add the li's for the topnav list in between <content tag="topnav"> and </content>. You can also
	add additional javascripts, css files or content to the page, just as you are used to.

	How to use Datadatbles:
		Use a 'paginate' class on a table to create a paginated table using datatables plugin.

				<table id='samples' class="paginate">
					<thead>
						<tr><th>Name</th><th># samples</th></tr>
					</thead>
					<tbody>
						<tr><td>Robert</td><td>182</td></tr>
						<tr><td>Siemen</td><td>418</td></tr>
					</tbody>
				</table>

		will automatically create a paginated table, without any further actions. The pagination
		buttons will only appear if there is more than 1 page.

		Serverside tables:

		When you have a table with lots of rows, creating the HTML table can take a while. You can also
		create a table where the data for each page will be fetched from the server. This can be done using

				<table id='samples' class="paginate serverside" rel="/url/to/ajaxData">
					<thead>
						<tr><th>Name</th><th># samples</th></tr>
					</thead>
				</table>

		Where the /url/to/ajaxData is a method that returns the proper data for this table. See
		http://www.datatables.net/examples/data_sources/server_side.html for more information about this method.
*/ %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en-EN" xml:lang="en-EN">
	<head>
		<title><g:layoutTitle default="${grails.util.Holders.config.module.name} | dbXP"/></title>
		<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico', plugin: 'dbxpModuleBase' )}" type="image/x-icon"/>
		
		<% /* require modules from the resources plugin */ %>
		<r:require modules="moduleBase"/>

		<% /* Make sure the module javascript always knows their baseUrl. We want the application URL not the plugin specific resource URL */ %>
        <% /* CreateLink absolute doesn't seem to work: <r:script disposition="head">var baseUrl = '${g.createLink(absolute: true, url: '/')}'</r:script> */ %>
        <r:script disposition="head">var baseUrl = "http://${request.getServerName()}${request.getServerPort() == 80 ? "" : ":"+request.getServerPort()}${request.getContextPath()}"</r:script>

		<% /* include the layout resources that have to go in the head section */ %>
		<r:layoutResources/>

		<% /* render head of client views */ %>
		<g:layoutHead/>
		
		<% /* Define button icons */ %>
		<style type="text/css">
			/* Images for buttons */
			.buttons .view, 
			.buttons .show { background-image:  url(${fam.icon( name: 'magnifier' )}); }
			.buttons .add, 
			.buttons .create { background-image: url(${fam.icon( name: 'add' )}); }
			
			.buttons .edit { background-image:  url(${fam.icon( name: 'pencil' )}); }
			.buttons .delete, 
			.buttons .remove { background-image: url(${fam.icon( name: 'delete' )}); }
			
			.buttons .back { background-image: url(${fam.icon( name: 'arrow_left' )}); }
			.buttons .save { background-image:  url(${fam.icon( name: 'accept' )}); }
			.buttons .cancel { background-image:  url(${fam.icon( name: 'arrow_left' )}); }
			
			.buttons .list, 
			.buttons .backToList { background-image: url(${fam.icon( name: 'application' )}); }
			.buttons .otherList { background-image: url(${fam.icon( name: 'application_view_detail' )}); }
			
			.buttons .addAssociation { background-image:  url(${fam.icon( name: 'application_add' )}); }
			.buttons .editAssociation {	background-image:  url(${fam.icon( name: 'application_edit' )}); }
			.buttons .removeAssociation,
			.buttons .deleteAssociation { background-image:  url(${fam.icon( name: 'application_delete' )}); }
			
			.buttons .excel { background-image: url(${fam.icon( name: 'page_excel' )}); }
			
			/* Specific options */
			option.addMore { background-image: url(${fam.icon( name: 'add')}); }
			option.modify { background-image: url(${fam.icon( name: 'layout_add')}); }
			option.locked { background-image: url(${fam.icon( name: 'lock')}); }
			
			/* Messages and errors */
			#content p.message { background-image: url(${fam.icon( name: 'user')}); }
			#content p.error { background-image: url(${fam.icon( name: 'information')}); }
			
			/* Icon in the top bar */
			ul.topnav li.user_info { background-image: url(${fam.icon( name: 'user')}); }
		</style>
		
	</head>
	<body>
		<div id="header">
			<div id="logo">${grails.util.Holders.config.module.name}</div>
		    <ul class="topnav">
				<% /* Include topnav as specified by the page */ %>
				<g:pageProperty name="page.topnav" />
				<li class="user_info">
					<g:if test="${session?.gscfUser}">
						Hello ${session?.gscfUser?.username}&nbsp;&nbsp;|&nbsp;
			        	<g:link controller="logout" action="index">sign out</g:link>
			        </g:if>
			        <g:else>
			        	Hello Guest&nbsp;&nbsp;|&nbsp;
			        	<g:link controller="login" action="index">sign in</g:link>
			        </g:else>
				</li>	
			</ul>				
			<br clear="all" />
		</div>
		<div class="container">
			<div id="content">
				<g:layoutBody/>
			</div>

			<div id="footer">
				Copyright &copy; 2010 - <g:formatDate format="yyyy" date="${new Date()}"/>. All rights reserved.
			</div>
		</div>
		<% /* include the layout resources that can go in the bottom part of the body section */ %>
		<r:layoutResources/>
	</body>
</html>
