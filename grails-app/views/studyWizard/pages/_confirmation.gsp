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
<af:page>
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
		${(study.eventGroups) ? study.eventGroups.size() : 0} event groups. The study is results in
		${(study.samples) ? study.samples.size() : 0} samples analyzed using ${(study.assays) ? study.assays.size() : 0} assays.
		</p>
	</div>
	<h3><a href="#">Study</a></h3>
	<div>
		<p>
		  <ul>
		<g:each var="field" in="${study.giveFields()}">
			<g:if test="${study.getFieldValue(field.name)}"><li>${field.name} - ${study.getFieldValue(field.name)}</li></g:if>
		</g:each>
		  </ul>
		</p>
		Not right? Click <af:ajaxButton name="toStudy" value="here" afterSuccess="onPage()" class="prevnext" /> to go back to the study page and make corrections.
	</div>
	<h3><a href="#">Subjects</a></h3>
	<div>
		<g:each var="subject" in="${study.subjects}">
			<p><b>${subject}</b></p>
			<ul>
			<g:each var="field" in="${subject?.giveFields()}">
				<g:if test="${subject.getFieldValue(field.name)}"><li>${field.name} - ${subject.getFieldValue(field.name)}</li></g:if> 	
			</g:each>
			</ul>
		</g:each>

		Not right? Click <af:ajaxButton name="toSubjects" value="here" afterSuccess="onPage()" class="prevnext" /> to go back to the subjects page and make corrections.
	</div>
	<h3><a href="#">Events</a></h3>
	<div>
		<g:each var="template" in="${study.giveAllEventTemplates()}">
			<p><b>${template}</b></p>
			<ul>
			<g:each var="event" in="${study.giveEventsForTemplate(template)}">
				<li>
					<i><g:if test="${(event.getClass() == 'SamplingEvent')}">Sampling </g:if>Event</i>
					<ul>
				<g:each var="field" in="${event?.giveFields()}">
						<li>${field} - ${event.getFieldValue(field.name)}</li>
				</g:each>
					</ul>
				</li>
			</g:each>
			</ul>
		</g:each>

		Not right? Click <af:ajaxButton name="toEvents" value="here" afterSuccess="onPage()" class="prevnext" /> to go back to the events page and make corrections.
	</div>
	<h3><a href="#">Samples</a></h3>
	<div>
		<g:each var="sample" in="${study.samples}">
			<p><b>${sample}</b></p>
			<ul>
			<g:each var="field" in="${sample?.giveFields()}">
				<g:if test="${sample.getFieldValue(field.name)}"><li>${field.name} - ${sample.getFieldValue(field.name)}</li></g:if>
			</g:each>
			</ul>
		</g:each>

		Not right? Click <af:ajaxButton name="toSamples" value="here" afterSuccess="onPage()" class="prevnext" /> to go back to the subjects page and make corrections.
	</div>
	<h3><a href="#">Assays</a></h3>
	<div>
		<g:each var="assay" in="${study.assays}">
			<p><b>${assay}</b></p>
			<ul>
			<g:each var="field" in="${assay?.giveFields()}">
				<g:if test="${assay.getFieldValue(field.name)}"><li>${field.name} - ${assay.getFieldValue(field.name)}</li></g:if>
			</g:each>
			</ul>
		</g:each>

		Not right? Click <af:ajaxButton name="toAssays" value="here" afterSuccess="onPage()" class="prevnext" /> to go back to the subjects page and make corrections.
	</div>
</div>

</af:page>