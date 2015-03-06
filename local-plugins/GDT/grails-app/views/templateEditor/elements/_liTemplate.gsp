<g:set var="templateService" bean="templateService"/>
<g:set var="numUses" value="${templateService.numUses(template)}" />
<li id="template_${template.id}"class="ui-state-default">
  <g:if test='${templateadmin}'>
      <g:if test="${numUses > 0}">
          <g:render template="elements/liTemplateNonDeletable" model="['template': template, 'standalone': standalone, 'params': numUses]"/>
      </g:if>
      <g:else>
          <g:render template="elements/liTemplateEditable" model="['template': template, 'standalone': standalone]"/>
      </g:else>
  </g:if>

  <g:else>
      <g:render template="elements/liTemplateNonEditable" model="['template': template, 'standalone': standalone]"/>
  </g:else>
</li>