<%
/**
 * fourth wizard page / tab
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
	<h1>Apache Configuration</h1>

	<span class="message info">
		<span class="title">Apache Virtual Host Configuration</span>
		If you would like to use the Apache webserver as an entry point for this application because
		<li>you want the user to user ${configInfo?.properties.getProperty('grails.serverURL')} instead of http://</li>
		<li>you (might) want to loadbalance your application using multiple application servers</li>
		you can use the following apache virtual host configuration file as a basis:
	</span>

	<pre class="brush:plain">
&lt;VirtualHost *:80>
	ServerName ${domain}<g:if test="${!(domain =~ /^www/)}">
	ServerAlias www.${domain}</g:if>

	ErrorLog /var/log/apache2/${meta(name: 'app.name')}-${grails.util.GrailsUtil.environment}-error.log
	CustomLog /var/log/apache2/${meta(name: 'app.name')}-${grails.util.GrailsUtil.environment}-access.log combined

	&lt;IfModule mod_rewrite.c>
		RewriteEngine on
        <g:if test="${!(domain =~ /^www/)}">
		# keep listening for the serveralias, but redirect to
		# servername instead to make sure only one user session
		# is created (tomcat will create one user session per
		# domain which may lead to two (or more) usersessions
		# depending on the number of serveraliases)
		# see gscf ticket #321
		RewriteCond %\{HTTP_HOST\} ^www.${domain}\$ [NC]
		RewriteRule ^(.*)\$ http://${domain}\$1 [R=301,L]
		</g:if>
		# rewrite the /gscf-a.b.c-environment/ part of the url
		RewriteCond %\{HTTP_HOST\} ^${domain}\$ [NC]
		RewriteRule ^${context}/(.*)\$ /\$1 [L,PT,NC,NE]
	&lt;/IfModule>

	&lt;IfModule mod_proxy.c>
		&lt;Proxy *>
			Order deny,allow
			Allow from all
		&lt;/Proxy>

		ProxyStatus On
		ProxyPass / balancer://${meta(name: 'app.name')}-cluster${context}/ stickysession=JSESSIONID|jsessionid nofailover=On
		ProxyPassReverse / balancer://${meta(name: 'app.name')}-cluster${context}/
		ProxyPassReverseCookiePath ${context} /

		&lt;Location />
			SetOutputFilter proxy-html
			ProxyHTMLDoctype XHTML Legacy
			ProxyHTMLURLMap ${context}/	/
		&lt;/Location>

		&lt;Proxy balancer://${meta(name: 'app.name')}-cluster>
			BalancerMember ajp://localhost:8009
		&lt;/Proxy>
	&lt;/IfModule>
&lt;/VirtualHost>
	</pre>

</af:page>
