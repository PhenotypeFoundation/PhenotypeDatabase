<%@ page import="org.dbnp.gdt.RelTime" %>
<%@ page import="org.dbnp.gdt.TemplateFieldType" %>
<af:page>
    <a href="#" onclick="$('#0_0').attr('CHECKED','CHECKED');$('#1_1').attr('CHECKED','CHECKED');$('#2_2').attr('CHECKED','CHECKED');$('#3_3').attr('CHECKED','CHECKED');$('input.prevnext[name=next]').click();">REMOVE THIS LINK</a>
	<h1>Page Two</h1>
	<p>
		<h2>Please select one or more event groups, per sampling event:</h2>
		
					
		<g:each in="${samplingEventTemplates}" var="template">
			<h1>${template.name}</h1>
			<table>
				<tr>
					<th>
						# samples in event
					</th>
					<g:each in="${samplingEventFields}" var="field">
						<th>
							${field.name}
						</th>
					</g:each>
					<g:each in="${eventGroups}" var="group">
						<th>
							${group.name}
						</th>
					</g:each>
				</tr>
				<g:each in="${samplingEvents}" var="event" status="e">
					<g:if test="${event.template == template}">
						<tr>
							<td>
								${event?.samples.size()}
							</td>
							<g:each in="${samplingEventFields}" var="field">
								<td>
									<g:if test="${field.type == TemplateFieldType.RELTIME}">
									
										<g:if test="${event?.fieldExists(field.name)}">
											${new RelTime( event?.getFieldValue(field.name) ).toString()}	
										</g:if>
										
									</g:if>
									<g:else>
										<g:if test="${event?.fieldExists(field.name)}">
											${event?.getFieldValue(field.name)}
										</g:if>
									</g:else>
								</td>
							</g:each>
							<g:each in="${eventGroups}" var="group" status="g">
								<td>
									<g:if test="${event.belongsToGroup([group])}">
										<g:checkBox name="${e}_${g}"/>
									</g:if>
								</td>
							</g:each>
						</tr>
					</g:if>
				</g:each>
			</table>
		</g:each>
				
		
	<p>
</af:page>
