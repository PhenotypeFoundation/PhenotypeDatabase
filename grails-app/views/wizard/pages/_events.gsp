<%
/**
 * Events page
 *
 * @author  Jeroen Wesbeek
 * @since   20100212
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
		<span class="title">Define all events that occur in your study</span>
		Bla bla bla we need a good help text here ;)
	</span>

	<wizard:templateElement name="template" description="Template" value="${event?.template}" entity="${dbnp.studycapturing.Event}" addDummy="true" ajaxOnChange="switchTemplate" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" >
		The template to use for this study
	</wizard:templateElement>
	<g:if test="${event?.template}"><wizard:templateElements entity="${event}" /></g:if>
	<g:if test="${event?.template}"><wizard:buttonElement name="add" value="Add" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()"/></g:if>

<g:if test="${events}">
	<g:each var="eventTemplate" in="${eventTemplates}">
		<h1>${eventTemplate.getValue().name} template</h1>
	<div class="table">
		<div class="header">
			<div class="firstColumn">#</div>
			<div class="firstColumn"></div>
			<wizard:templateColumnHeaders template="${eventTemplate.getValue().template}" class="column"/>
			<g:if test="${eventGroups}"><g:each var="eventGroup" status="g" in="${eventGroups}">
			<div class="column">
				<g:textField name="eventGroup_${g}_name" value="${eventGroup.name}" />
				<wizard:ajaxButton name="deleteEventGroup" src="../images/icons/famfamfam/delete.png" alt="delete this eventgroup" class="famfamfam" value="-" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" before="\$(\'input[name=do]\').val(${g});" afterSuccess="onWizardPage()" />
			</div>
			</g:each></g:if>
			<div class="column">
				<wizard:ajaxButton name="addEventGroup" src="../images/icons/famfamfam/add.png" alt="add a new eventgroup" class="famfamfam" value="+" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" />
			</div>
		</div>
		<g:each var="eventId" in="${eventTemplate.getValue().events}">
			<div class="row">
				<div class="firstColumn">${eventId + 1}</div>
				<div class="firstColumn">
					<wizard:ajaxButton name="delete" src="../images/icons/famfamfam/delete.png" alt="delete this subject" class="famfamfam" value="-" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" before="\$(\'input[name=do]\').val(${eventId});" afterSuccess="onWizardPage()"/>
				</div>
				<wizard:templateColumns id="${eventId}" entity="${events[ eventId ]}" template="${events[ eventId ].template}" name="event_${eventId}" class="column" />
				<g:if test="${eventGroups}"><g:each var="eventGroup" status="j" in="${eventGroups}">
				<div class="column">
					<g:if test="${eventGroup.events.find{ it == event} }">
						<input type="checkbox" name="event_${eventId}_group_${j}" checked="checked" />
					</g:if><g:else>
						<input type="checkbox" name="event_${eventId}_group_${j}"/>
					</g:else>
				</div>
				</g:each></g:if>
				<div class="column"></div>
			</div>
		</g:each>
	</div>
	<div class="sliderContainer">
		<div class="slider"></div>
	</div>
	</g:each>
</g:if>

</wizard:pageContent>