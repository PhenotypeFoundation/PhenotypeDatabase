<%
/**
 * main ajax flow template
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
<div id="ajaxflow">
<af:flow name="importer" class="ajaxFlow" commons="common" partials="pages" controller="[controller: 'importer', action: 'pages']" spinner="ajaxFlowWait">
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
<g:render template="common/on_page"/>
<g:render template="common/please_wait"/>
