<%
/**
 * main ajax flow template
 *
 * @author Jeroen Wesbeek
 * @since  20101220
 *
 * Revision information:
 * $Rev:  66849 $
 * $Author:  duh $
 * $Date:  2010-12-08 15:12:54 +0100 (Wed, 08 Dec 2010) $
 */
%>
<div id="ajaxflow">
<af:flow name="studyWizard" class="ajaxFlow" commons="common" partials="pages" controller="[controller: 'studyWizard', action: 'pages']">
	<%	/**
	 	 * The initial rendering of this template will result
	 	 * in automatically triggering the 'next' event in
	 	 * the webflow. This is required to render the initial
	 	 * page / partial and done by using af:triggerEvent
	 <af:triggerEvent name="next" afterSuccess="onPage();" />
		 */ %>
</af:flow>
<g:if env="development">
<af:error class="ajaxFlowError">
	[ajax errors go in here, normally it's safe to delete the af:error part]
</af:error>
</g:if>
</div>
<g:render template="common/on_page"/>

