<%
/**
 * Event Descriptions page
 *
 * @author  Jeroen Wesbeek
 * @since   20100118
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
		<span class="title">Describe all unique event types that occur in your study</span>
		These unique events are, for example, treatments, challenges and sampling events. Every event description
		should be unique. If your study, for example, samples both blood as well as tissue on one or more subjects,
		then create two sample descriptions. One for 'sampling blood', and one for 'sampling tissue'.
	</span>

	<span class="info">
		<span class="title">Note!</span>
		Currently 'classification' display all terms because there is no way to machine interpret
		the different values. So please -for now- make sure to pick the Ontology reference applicable for
		events until this particular challenge is solved.
	</span>

	<wizard:termElement name="classification" description="Classification" error="classification" value="${values?.classification}">
		The ontology reference for this particular type of event
	</wizard:termElement>
	<wizard:textFieldElement name="name" description="Name" error="name" value="${values?.name}">
		The name of the event description you are creating
	</wizard:textFieldElement>
	<wizard:textFieldElement name="description" description="Description" error="description" value="${values?.description}">
		A short description summarizing your event description
	</wizard:textFieldElement>
	<wizard:checkBoxElement name="isSamplingEvent" description="Sampling event" error="isSamplingEvent" value="${values?.isSamplingEvent}">
		Is this a sampling event description?
	</wizard:checkBoxElement>
	<wizard:buttonElement name="add" value="Add" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()"/>
<g:if test="${eventDescriptions}">
	<div class="table">
		<div class="header">
			<div class="firstColumn">#</div>
			<div class="firstColumn"></div>
			<div class="column">name</div>
			<div class="column">description</div>
			<div class="column">classification</div>
			<div class="column">sampling event</div>
			<div class="column">protocol</div>
		</div>
	<g:each var="eventDescription" status="i" in="${eventDescriptions}">
		<div class="row">
			<div class="firstColumn">${i+1}</div>
			<div class="firstColumn">
				<wizard:ajaxButton name="delete" src="../images/icons/famfamfam/delete.png" alt="delete this event" class="famfamfam" value="-" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" before="\$(\'input[name=do]\').val(${i});" afterSuccess="onWizardPage()" />
			</div>
			<div class="column"><g:textField name="eventDescription_${i}_name" value="${eventDescription.name}" size="12" maxlength="12" /></div>
			<div class="column"><g:textField name="eventDescription_${i}_description" value="${eventDescription.description}" size="12" maxlength="12" /></div>
			<div class="column"><wizard:termSelect name="eventDescription_${i}_classification" value="${eventDescription.classification}" /></div>
			<div class="column"><g:checkBox name="eventDescription_${i}_isSamplingEvent" value="${eventDescription.isSamplingEvent}" /></div>
			<div class="column"><g:if test="${eventDescription.protocol}">${eventDescription.protocol}</g:if><g:else>-</g:else></div>
		</div>
	</g:each>
	</div>
</g:if>
</wizard:pageContent>