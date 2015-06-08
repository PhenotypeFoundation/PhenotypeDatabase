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

	<span class="message info">
		<span class="error">Errors encountered</span>
		Unfortunately we could not create the the Configuration file. Please do so manually by issuing the following commands:
	</span>

	<pre class="brush:plain">
		echo "# ${meta(name: 'app.name')} ${grails.util.GrailsUtil.environment} configuration
		#
		# \$Author\$
		# \$Date\$
		# \$Rev\$" >> ${configInfo.file}
		chown ${System.getProperty("user.name")} ${configInfo.file}
		chmod u+rwx ${configInfo.file}
	</pre>

	When done, click <i>next</i> to continue...

</af:page>