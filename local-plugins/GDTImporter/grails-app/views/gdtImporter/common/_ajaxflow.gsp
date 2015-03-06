<%
/**
 * main ajax flow template
 *
 * @author Jeroen Wesbeek
 * @since  20110310
 *
 * Revision information:
 * $Rev:  67320 $
 * $Author:  duh $
 * $Date:  2010-12-22 17:49:27 +0100 (Wed, 22 Dec 2010) $
 */
%>
<div id="ajaxflow">
<af:flow name="gdtImporter" class="ajaxFlow" commons="common" partials="pages" plugin="gdtimporter" spinner="ajaxFlowWait" controller="[controller: 'gdtImporter', action: 'pages']">
	<%	/**
	 	 * The initial rendering of this template will result
	 	 * in automatically triggering the 'next' event in
	 	 * the webflow. This is required to render the initial
	 	 * page / partial and done by using af:triggerEvent
		 */ %>
	<af:triggerEvent name="next" afterSuccess="onPage();" />
</af:flow>
<% /*
<g:if env="development">
<af:error class="ajaxFlowError">
	[ajax errors go in here, normally it's safe to delete the af:error part]
</af:error>
</g:if>
    */ %>
</div>
<g:render template="common/on_page" plugin="gdtimporter" />
<g:render template="common/please_wait" plugin="gdtimporter" />
