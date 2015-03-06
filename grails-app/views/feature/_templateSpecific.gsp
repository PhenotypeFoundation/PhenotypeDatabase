<%@ page import="org.dbnp.gdt.TemplateFieldType; org.dbxp.sam.Feature; org.dbxp.sam.Platform" %>
<g:if test="${template!=null}">
    <table>
        <g:each in="${template.fields}" var="field" status="i">
            <tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'}">
                <td valign="top" class="fieldName">
                    ${field.name.capitalize()}
                    <g:if test="${field.type==TemplateFieldType.DOUBLE}">
                        [${field.unit}]
                    </g:if>
                    <g:if test="${field.required}">
                        <i>(required)</i>
                    </g:if>
                </td>
                <td valign="top" class="value ${hasErrors(bean: featureInstance, field: field.escapedName(), 'errors')}">
                    <%-- <g:textField name="${field.escapedName()}" value="${values[ field.name ]}"/> --%>
                    <%-- <af:renderTemplateField name="${field.escapedName()}" templateField="${field}" value="${values[ field.name ] !=null ? values[ field.name ] : ""}"/> --%>
                    <g:if test="${featureInstance?.id!=null}">
                        <af:renderTemplateField name="${field.escapedName()}" templateField="${field}" entity="${featureInstance}"/>
                    </g:if>
                    <g:else>
                        <g:if test="${values.get(field.escapedName())!=null}">
                            <af:renderTemplateField name="${field.escapedName()}" entity="${featureInstance}" templateField="${field}" value="${values[field.escapedName()]}"/>
                        </g:if>
                        <g:else>
                            <af:renderTemplateField name="${field.escapedName()}" entity="${featureInstance}" templateField="${field}" value=""/>
                        </g:else>
                    </g:else>
                </td>
            </tr>
        </g:each>
    </table>
</g:if>