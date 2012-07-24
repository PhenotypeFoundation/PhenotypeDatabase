<%
/**
 * main ajax flow template
 *
 * @author Jeroen Wesbeek
 * @since  20120620
 *
 * Revision information:
 * $Rev:  67320 $
 * $Author:  duh $
 * $Date:  2010-12-22 17:49:27 +0100 (Wed, 22 Dec 2010) $
 */
%>
<div id="ajaxflow">
<af:flow name="cookdata" class="ajaxFlow" commons="common" partials="pages" spinner="ajaxFlowWait" controller="[controller: 'cookdata', action: 'pages']">
	<%	/**
	 	 * The initial rendering of this template will result
	 	 * in automatically triggering the 'next' event in
	 	 * the webflow. This is required to render the initial
	 	 * page / partial and done by using af:triggerEvent
		 */ %>
	<af:triggerEvent name="next" afterSuccess="onPage();" />
</af:flow>
</div>
<g:render template="common/on_page"/>
<g:render template="common/please_wait"/>
