<%
/**
 * Configuration Location
 *
 * @author Jeroen Wesbeek
 * @since  20110513
 *
 * Revision information:
 * $Rev:  66849 $
 * $Author:  duh $
 * $Date:  2010-12-08 15:12:54 +0100 (Wed, 08 Dec 2010) $
 */
%>
<af:page>
<h1>Configuration file</h1>

<g:if test="${configInfo.pathSummary && configInfo.fileSummary}">
	<span class="message info">
		<span class="okay">The configuration file and path are okay!</span>
		Click next to continue...
	</span>
</g:if><g:else>
	<span class="message info">
		<span class="warning">
			You need to fix the configuration
			<g:if test="${!configInfo.pathSummary && !configInfo.fileSummary}">path and file</g:if>
			<g:elseif test="${!configInfo.pathSummary}">path</g:elseif>
			<g:elseif test="${!configInfo.fileSummary}">file</g:elseif>
			in order to continue
		</span>
		The application is currently using the default ${grails.util.GrailsUtil.environment} configuration. In the following
		steps you will configure the application to use a specific database and (if required) insert default templates and
		data.
	</span>
</g:else>

<div class="checklist">
	<ul class="header">
		<li class="path">Path</li>
		<li>exists</li>
		<li>readable</li>
		<li>writable</li>
	</ul>
	<ul>
		<li class="path">${configInfo.path}</li>
		<li><img src="${fam.icon(name: icons[(configInfo.pathExists.toString())])}"/></li>
		<li><img src="${fam.icon(name: icons[(configInfo.pathCanRead.toString())])}"/></li>
		<li><img src="${fam.icon(name: icons[(configInfo.pathCanWrite.toString())])}"/></li>
		<li class="summary"><g:if test="${!configInfo.pathSummary}"><img src="${fam.icon(name: 'arrow_right')}"/><af:ajaxButton name="toConfigurationPath" value="resolve issues" afterSuccess="onPage();" class="prevnext" /></g:if></li>
	</ul>
	<ul>
		<li class="path">${configInfo.file}</li>
		<li><img src="${fam.icon(name: icons[(configInfo.fileExists.toString())])}"/></li>
		<li><img src="${fam.icon(name: icons[(configInfo.fileCanRead.toString())])}"/></li>
		<li><img src="${fam.icon(name: icons[(configInfo.fileCanWrite.toString())])}"/></li>
		<li class="summary"><g:if test="${!configInfo.fileSummary}"><img src="${fam.icon(name: 'arrow_right')}"/><af:ajaxButton name="toConfigurationFile" value="resolve issues" afterSuccess="onPage();" class="prevnext" /></g:if></li>
	</ul>
</div>

</af:page>
