<%
/**
 * Subjects page
 *
 * @author  Jeroen Wesbeek
 * @since   20100113
 * @package wizard
 * @see     dbnp.studycapturing.WizardTagLib::previousNext
 * @see     dbnp.studycapturing.WizardController
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
<wizard:pageContent>

	<span class="info">
		<span class="title">Assign subjects to eventgroups</span>
		In the previous page you defined events and grouped them together into eventgroups. Here you need to define
		which subjects belong to which eventgroup (hence: what events <i>act upon</i> a particular subject)
	</span>

	<div class="table">
		<div class="header">
			<div class="column">Template</div>
			<div class="column">Subjects</div>
			<g:if test="${eventGroups}"><g:each var="eventGroup" status="g" in="${eventGroups}">
				<div class="column">
					${eventGroup.name}
					<div class="helpIcon"></div>
					<div class="helpContent">
						<h1>${eventGroup.name}</h1>
						<g:each var="event" status="e" in="${eventGroup.events}">
							<h2>${event.template}</h2>
							<g:each var="field" status="f" in="${event.giveFields()}">
								${field.name}: ${event.getFieldValue(field.name)}<br/>
							</g:each>
						</g:each>
					</div>
				</div>
			</g:each></g:if>
		</div>
		<g:each var="sTemplate" in="${subjectTemplates}">
			<g:set var="subjectTemplate" value="${sTemplate.getValue()}"/>
			<g:set var="showHeader" value="${true}"/>
			<g:each var="sId" in="${subjectTemplate.subjects}">
				<g:set var="subjectId" value="${sId.getValue()}"/>
				<div class="row">
					<div class="column">
						<g:if test="${showHeader}">
							<g:set var="showHeader" value="${false}"/>
							${subjectTemplate.name}
							<div class="helpIcon"></div>
							<div class="helpContent">
								<h1>${subjectTemplate.name}</h1>
								<h2>Template Fields:</h2>
								<g:each var="field" status="f" in="${subjects[ subjectId ].giveFields()}">
									${field.name[0].toUpperCase() + field.name.substring(1)}<br/>
								</g:each>
							</div>
						</g:if>
					</div>
					<div class="column">${subjects[subjectId].name}</div>
					<g:if test="${eventGroups}"><g:each var="eventGroup" status="g" in="${eventGroups}">
						<div class="column">
							<g:if test="${eventGroup.subjects.find{ it == subjects[ subjectId ] } }">
								<input type="checkbox" name="subject_${subjectId}_group_${g}" checked="checked"/>
							</g:if><g:else>
							<input type="checkbox" name="subject_${subjectId}_group_${g}"/>
						</g:else>
						</div>
					</g:each></g:if>
				</div>
			</g:each>
		</g:each>
	</div>
	<div class="sliderContainer">
		<div class="slider" ></div>
	</div>

</wizard:pageContent>