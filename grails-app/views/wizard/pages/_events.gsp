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
	<g:if env="development">
		<wizard:ajaxButtonElement description="Development feature (clear events)" name="clear" value="clear events" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()">
			This functionality is only available in development mode for debugging purposes and will not show in test and production environments
		</wizard:ajaxButtonElement>
	</g:if>

	<span class="info">
		<span class="title">Define all events that occur in your study</span>
		An event is any change ‘forced’ upon a subject, such as treatment, challenge, sampling. Choose an event type an define the different parameters of the event.		
	</span>

	<wizard:radioElement name="eventType" description="Type" elements="['event','sample']" value="${values?.eventType}">
		Type of event
	</wizard:radioElement>
	<wizard:templateElement name="eventTemplate" elementId="eventTemplate" description="Event Template" value="${event?.template}" entity="${dbnp.studycapturing.Event}" addDummy="true" ajaxOnChange="switchTemplate" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" >
		The template to use for this study
	</wizard:templateElement>
	<wizard:templateElement name="sampleTemplate" elementId="sampleTemplate" description="Sample Template" value="${event?.template}" entity="${dbnp.studycapturing.SamplingEvent}" addDummy="true" ajaxOnChange="switchTemplate" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" >
		The template to use for this study
	</wizard:templateElement>
	<g:if test="${event?.template}">
	<div id="${values?.eventType}TemplateFields">
	<g:if test="${event?.template}"><wizard:templateElements entity="${event}" /></g:if>
	<g:if test="${event?.template}"><wizard:buttonElement name="add" value="Add" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()"/></g:if>
	</div>
	</g:if>
	
	<script type="text/javascript">
	function swapTemplate(value,refresh) {
		$("div[id$='Template'],div[id$='TemplateFields']").each(function() {
			var e = $(this);
			if (e.attr('id').match("^"+value) != null) {
				e.show();
			} else {
				e.hide();
			}
		});

		if(refresh) {
		}
	}

	// handle template selectors
	$(document).ready(function() {
		// bind event handlers
		$("input[name=eventType]").click(function() {
			swapTemplate($(this).val(),true);
		});

		// handle selects
		swapTemplate($('input:radio[name=eventType]:checked').val(),false);
	});
	</script>

	<g:if test="${study.events || study.samplingEvents}">
		<g:each var="template" in="${study.giveAllEventTemplates()}">
			<g:set var="showHeader" value="${true}" />
			<h1>${template}</h1>
			<div class="table">
			<g:each var="event" in="${study.giveEventsForTemplate(template)}">
				<g:if test="${showHeader}">
				<g:set var="showHeader" value="${false}" />
				<div class="header">
					<div class="firstColumn">#</div>
					<div class="firstColumn"></div>
					<g:if test="${study.eventGroups}"><g:each var="eventGroup" in="${study.eventGroups}">
					<div class="column">
						<g:textField name="eventGroup_${eventGroup.getIdentifier()}_${template.getIdentifier()}" value="${eventGroup.name}" />
						<wizard:ajaxButton name="deleteEventGroup" src="../images/icons/famfamfam/delete.png" alt="delete this eventgroup" class="famfamfam" value="-" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" before="\$(\'input[name=do]\').val(${eventGroup.getIdentifier()});" afterSuccess="onWizardPage()" />
					</div>
					</g:each></g:if>
					<div class="firstColumn">
						<wizard:ajaxButton name="addEventGroup" src="../images/icons/famfamfam/add.png" alt="add a new eventgroup" class="famfamfam" value="+" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" />
					</div>
				  <wizard:templateColumnHeaders class="column" entity="${event}" />
				</div>
				</g:if>

				<div class="row">
					<div class="firstColumn">${event.getIdentifier()}</div>
					<div class="firstColumn">
						<wizard:ajaxButton name="deleteEvent" src="../images/icons/famfamfam/delete.png" alt="delete this subject" class="famfamfam" value="-" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" before="\$(\'input[name=do]\').val(${event.getIdentifier()});" afterSuccess="onWizardPage()"/>
					</div>
					<g:if test="${study.eventGroups}"><g:each var="eventGroup" in="${study.eventGroups}">
					<div class="column">
						<g:if test="${eventGroup.events.find{ it == event } || eventGroup.samplingEvents.find{ it == event }}">
							<input type="checkbox" name="event_${event.getIdentifier()}_group_${eventGroup.getIdentifier()}" checked="checked" />
						</g:if><g:else>
							<input type="checkbox" name="event_${event.getIdentifier()}_group_${eventGroup.getIdentifier()}"/>
						</g:else>
					</div>
					</g:each></g:if>
					<div class="firstColumn"></div>
					<wizard:templateColumns class="column" entity="${event}" name="event_${event.getIdentifier()}" />
				</div>

			</g:each>
			</div>
			<div class="sliderContainer">
				<div class="slider"></div>
			</div>
		</g:each>
	</g:if>

</wizard:pageContent>
