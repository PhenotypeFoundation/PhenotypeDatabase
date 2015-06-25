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
	<h1>Summary</h1>

	<span class="message info">
		<span class="title">New application configuration</span>
		Congratulations, you just finished setting up and configuring the application. Below you see the configuration
		file (${configInfo.file}) you have just created.
	</span>

	Configuration file: ${configInfo.file}<br/>



	<pre class="brush:plain">${configInfo.file.text}</pre>

</af:page>
