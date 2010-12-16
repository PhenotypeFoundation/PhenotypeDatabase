<%
/**
 * error page
 *
 * @author Jeroen Wesbeek
 * @since  20101206
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
<af:page>
<h1>Oops!</h1>
<p>
	We encountered an problem storing your data! You can either
	<af:ajaxButton name="toPageFive" value="try again" afterSuccess="onPage();" class="prevnext" />
	or file a bugreport.
</p>
</af:page>
