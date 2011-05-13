<%
/**
 * last wizard page / tab
 *
 * @author Jeroen Wesbeek
 * @since  20110318
 *
 * Revision information:
 * $Rev:  66849 $
 * $Author:  duh $
 * $Date:  2010-12-08 15:12:54 +0100 (Wed, 08 Dec 2010) $
 */
%>
<af:page>
	<h1>Final Page</h1>

	This concludes the configuration wizard. Make sure to restart your tomcat container to load the new configuration.
	After restart you can <g:link controller="assayModule" action="list" class="icon icon_user_add">Manage Your Modules</g:link>
	(if you have any).
</af:page>
