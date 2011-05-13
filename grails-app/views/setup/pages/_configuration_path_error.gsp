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
	<h1>Configuration path</h1>

	<span class="info">
		<span class="error">Errors encountered</span>
		Unfortunately we could not create the the Configuration path. Please do so manually by issuing the following commands:
	</span>

	<pre class="brush:plain">
		mkdir -p ${configInfo.path}
		chown -R ${System.getProperty("user.name")} ${configInfo.path}
		chmod -R u+rwx ${configInfo.path}
	</pre>

	When done, click <i>next</i> to continue...

</af:page>