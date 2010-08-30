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
			<g:if test="${study.eventGroups}"><g:each var="eventGroup" in="${study.eventGroups}">
				<div class="column">
					${eventGroup.name}
					<div class="helpIcon"></div>
					<div class="helpContent">
						<h1>${eventGroup.name}</h1>
						<g:each var="event" status="e" in="${eventGroup.getAllEvents()}">
							<h2>${event.template}</h2>
							<g:each var="field" status="f" in="${event.giveFields()}">
								${field.name}: ${event.getFieldValue(field.name)}<br/>
							</g:each>
						</g:each>
					</div>
				</div>
			</g:each></g:if>
		</div>
		<g:each var="template" in="${study.giveSubjectTemplates()}">
			<g:set var="showHeader" value="${true}"/>
			<g:each var="subject" in="${study.giveSubjectsForTemplate(template)}">
			<div class="row">
				<div class="column">
					<g:if test="${showHeader}">
						<g:set var="showHeader" value="${false}"/>
						${template.name}
						<div class="helpIcon"></div>
						<div class="helpContent">
							<h1>${template.name}</h1>
							<h2>Template Fields:</h2>
							<g:each var="field" in="${subject.giveFields()}">
								${field.name[0].toUpperCase() + field.name.substring(1)}<br/>
							</g:each>
						</div>
					</g:if>
				</div>
				<div class="column">${subject.name}</div>
				<g:if test="${study.eventGroups}"><g:each var="eventGroup" in="${study.eventGroups}">
				<div class="column">
					<input type="checkbox" name="subject_${subject.getIdentifier()}_group_${eventGroup.getIdentifier()}"<g:if test="${eventGroup.subjects.find{ it == subject } }"> checked="checked"</g:if>/>
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