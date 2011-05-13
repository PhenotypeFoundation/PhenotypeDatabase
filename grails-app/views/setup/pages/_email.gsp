<%
/**
 * third wizard page / tab
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
	<h1>Email and URL configuration</h1>

	<span class="info">
		<span class="title">Email configuration</span>
		The application uses the default system mailer, which is probably postfix or sendmail, so you only need to
		configure what email adress will be used to send out emails to the users.
	</span>

	<af:textFieldElement name="grails.plugins.springsecurity.ui.forgotPassword.emailFrom" description="from address" error="grails.plugins.springsecurity.ui.forgotPassword.emailFrom" value="${configInfo?.properties.getProperty('grails.plugins.springsecurity.ui.forgotPassword.emailFrom')}" style="width: 300px;">
		The from address used for communication to the users.
	</af:textFieldElement>

	<span class="info">
		<span class="title">URL configuration</span>
		Define the URL the application will be running on. If you would like to run from Apache (preferable) and not directly from Tomcat
		see the Summary tab for an Apache virtual host configuration example.
		<li>running directly from tomcat: http://my.url:8080${org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().contextPath}</li>
		<li>running from Apache or Tomcat at port 80: http://my.url</li>
	</span>

	<af:textFieldElement name="grails.serverURL" description="serverl url" error="grails.serverURL" value="${configInfo?.properties.getProperty('grails.serverURL')}" style="width: 300px;">
		The server URL this application will be served on.
	</af:textFieldElement>

</af:page>
