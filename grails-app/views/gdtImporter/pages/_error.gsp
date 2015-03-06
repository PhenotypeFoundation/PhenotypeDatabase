<%
/**
 * error page
 *
 * @author Jeroen Wesbeek
 * @since  20101206
 *
 * Revision information:
 * $Rev: 1430 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
 */
%>
<af:page>
<h1>Oops!</h1>
<p>
	We encountered an problem storing your data! You can either
	<af:ajaxButton name="tryAgain" value="try again" afterSuccess="onPage();" class="prevnext" />
	or file a bug report.
</p>
</af:page>