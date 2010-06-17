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

	<g:if test="${events}">
	<g:each var="eventTemplate" in="${eventTemplates}">
		<g:set var="showHeader" value="${true}" />
		<h1>${eventTemplate.value.name}</h1>
	<div class="table">
		<g:each var="eventId" in="${eventTemplate.value.events}">
			<g:if test="${showHeader}">
		  	<g:set var="showHeader" value="${false}" />
			<div class="header">
				<div class="firstColumn">#</div>
				<div class="firstColumn"></div>
				<g:if test="${eventGroups}"><g:each var="eventGroup" status="g" in="${eventGroups}">
				<div class="column">
					<g:textField name="eventGroup_${g}_name" value="${eventGroup.name}" />
					<wizard:ajaxButton name="deleteEventGroup" src="../images/icons/famfamfam/delete.png" alt="delete this eventgroup" class="famfamfam" value="-" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" before="\$(\'input[name=do]\').val(${g});" afterSuccess="onWizardPage()" />
				</div>
				</g:each></g:if>
				<div class="firstColumn">
					<wizard:ajaxButton name="addEventGroup" src="../images/icons/famfamfam/add.png" alt="add a new eventgroup" class="famfamfam" value="+" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" />
				</div>
				<wizard:templateColumnHeaders entity="${events[ eventId ]}" class="column"/>
			</div>
			</g:if>
			<div class="row">
				<div class="firstColumn">${eventId + 1}</div>
				<div class="firstColumn">
					<wizard:ajaxButton name="deleteEvent" src="../images/icons/famfamfam/delete.png" alt="delete this subject" class="famfamfam" value="-" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" before="\$(\'input[name=do]\').val(${eventId});" afterSuccess="onWizardPage()"/>
				</div>
				<g:if test="${eventGroups}"><g:each var="eventGroup" status="j" in="${eventGroups}">
				<div class="column">
					<g:if test="${eventGroup.events.find{ it == events[ eventId ] } }">
						<input type="checkbox" name="event_${eventId}_group_${j}" checked="checked" />
					</g:if><g:else>
						<input type="checkbox" name="event_${eventId}_group_${j}"/>
					</g:else>
				</div>
				</g:each></g:if>
				<div class="firstColumn"></div>
				<wizard:templateColumns id="${eventId}" entity="${events[ eventId ]}" template="${events[ eventId ].template}" name="event_${eventId}" class="column" />
			</div>
		</g:each>
	</div>
	<div class="sliderContainer">
		<div class="slider"></div>
	</div>
	</g:each>
</g:if>

</wizard:pageContent>
