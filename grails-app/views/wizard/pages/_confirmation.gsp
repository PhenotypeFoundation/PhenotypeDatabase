<%
/**
 * Confirmation page
 *
 * @author  Jeroen Wesbeek
 * @since   20100225
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
	<span class="title">You are almost there...</span>
	You are almost done creating your study. Below you will find a summary of the study you have just defined.
	If everything
	is well, click 'next' to permanently save the study you have created, or click 'previous' if you need to modify parts
	of your study.
</span>

<div id="accordion">
	<h3><a href="#">General overview</a></h3>
	<div>
		<p>
		You are about to create a study containing ${(study.subjects) ? study.subjects.size() : 0} subjects,
		${(study.events) ? study.events.size() : 0} events and ${(study.samplingEvents) ? study.samplingEvents.size(): 0} sampling events grouped into
		${(study.eventGroups) ? study.eventGroups.size() : 0} event groups and ${(study.samples) ? study.samples.size() : 0} samples.
		</p>
	</div>
	<h3><a href="#">Study</a></h3>
	<div>
		<p>
		  <ul>
		<g:each var="field" in="${study.giveFields()}">
			<li>${field.name} - ${study.getFieldValue(field.name)}</li>
		</g:each>
		  </ul>
		</p>
		Not right? Click <wizard:ajaxButton name="toStudy" value="here" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" class="prevnext" /> to go back to the study page and make corrections.
	</div>
	<h3><a href="#">Subjects</a></h3>
	<div>
		<p>
		${subjects}
		</p>
		Not right? Click <wizard:ajaxButton name="toSubjects" value="here" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" class="prevnext" /> to go back to the subjects page and make corrections.
	</div>
	<h3><a href="#">Events</a></h3>
	<div>
		<p>
		${subjects}
		</p>
		<ul>
			<li>List item one</li>
			<li>List item two</li>
			<li>List item three</li>
		</ul>
		Not right? Click <wizard:ajaxButton name="toEvents" value="here" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" class="prevnext" /> to go back to the events page and make corrections.
	</div>
</div>

<g:if env="development">
<span class="info">
	<span class="todo">TODO</span>
	<ul>
		<li>this page is not complete yet, finish it to summerize the study you are about to store in the database</li>
	</ul>
</span>
</g:if>

</wizard:pageContent>