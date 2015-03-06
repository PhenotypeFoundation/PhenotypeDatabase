<%
/**
 * wizard please wait template
 *
 * This is used by the ajax buttons, also see the 'spinner'
 * argument in the flow definition in _ajaxflow.gsp
 *
 * @author Jeroen Wesbeek
 * @since  20101222
 *
 * Revision information:
 * $Rev: 1430 $
 * $Author: work@osx.eu $
 * $Date: 2011-01-21 21:05:36 +0100 (Fri, 21 Jan 2011) $
 */
%>
<div id="ajaxFlowWait" class="ajaxFlow" style="display:none;">
	<span class="waitBackground"></span>
	<span class="waiter">
		<span class="wait">
			<span class="title">Please Wait...</span>
			<span class="spinner"></span>
		</span>
	</span>
</div>