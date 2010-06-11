<li id="template_${template.id}"class="ui-state-default">
  <g:if test="${template.inUse()}">
	<g:render template="elements/liTemplateNonEditable" model="['template': template]"/>
  </g:if>
  <g:else>
	<g:render template="elements/liTemplateEditable" model="['template': template]"/>
  </g:else>
</li>