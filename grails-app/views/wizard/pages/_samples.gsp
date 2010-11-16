<%
/**
 * Samples page
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
		<wizard:ajaxButtonElement description="Development feature (regenerate samples)" name="regenerate" value="regenerate new samples" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()">
			This functionality is only available in development mode for debugging purposes and will not show in test and production environments
		</wizard:ajaxButtonElement>
	</g:if>
	
	<span class="info">
		<span class="title">Samples</span>
		Below you see all samples generated based on the subject / sampling event relations
		you have specified in the previous screens.<br/>
	</span>

	<script type="text/javascript">
		function switchTemplate( element ) {
			<wizard:ajaxSubmitJs functionName="switchTemplate" this="element" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()"/>
		}
	</script>

	<g:if test="${study.samples}">
		<g:if test="${study.samples.findAll{!it.template}.size()}">
		<h1>Samples that still need to have a template assigned</h1>
		<div class="table">
		<div class="header">
			<div class="firstColumn"></div>
			<div class="column">Sampling Event</div>
			<div class="column">Subject</div>
			<div class="column">Template</div>
			<div class="column" style="width:200px;">Name</div>
		</div>
		<g:set var="previousTemplate" value=""/>
		<g:each var="sample" in="${study.samples}">
			<g:if test="${!sample.template}">
				<div class="row">
					<div class="firstColumn">&nbsp;</div>
					<div class="column">
						<g:if test="${previousTemplate != sample.parentEvent.template.name}">
							<g:set var="previousTemplate" value="${sample.parentEvent.template.name}"/>
							${sample.parentEvent.template.name}
							<div class="helpIcon"></div>
							<div class="helpContent">
								<h1>${sample.parentEvent.template.name}</h1>
								<h2>Template Fields:</h2>
								<g:each var="field" in="${sample.parentEvent.giveFields()}">
									${field.name[0].toUpperCase() + field.name.substring(1)}<br/>
								</g:each>
							</div>
						</g:if>
					</div>
					<div class="column">
						${sample.parentSubject.name}
						<div class="helpIcon"></div>
						<div class="helpContent">
							<h1>${sample.parentSubject.template.name}</h1>
							<h2>Template Fields:</h2>
							<g:each var="field" in="${sample.parentSubject.giveFields()}">
								${field.name[0].toUpperCase() + field.name.substring(1)}<br/>
							</g:each>
						</div>
					</div>
					<div class="column">
						<wizard:templateSelect name="template_${sample.getIdentifier()}" entity="${dbnp.studycapturing.Sample}" value="${sample.template}" tableEditorChangeEvent="switchTemplate(element);" addDummy="true" />						
					</div>
					<div class="column">${sample.name}</div>
					<wizard:templateColumns name="sample_${sample.getIdentifier()}" class="column" id="1" entity="${sample}"/>
				</div>
			</g:if>
		</g:each>
		</div>
		<div class="sliderContainer">
			<div class="slider" ></div>
		</div>
		</g:if> 

		<g:each var="sampleTemplate" in="${study.giveSampleTemplates()}">
			<h1>${sampleTemplate.name}</h1>
			<g:set var="showHeader" value="${true}" />
			<g:set var="previousTemplate" value=""/>			
			<div class="table">
			<g:each var="sample" in="${study.giveSamplesForTemplate(sampleTemplate)}">
				<g:if test="${showHeader}">
					<g:set var="showHeader" value="${false}" />
					<div class="header">
						<div class="firstColumn"></div>
						<div class="column">Sampling Event</div>
						<div class="column">Subject</div>
						<div class="column">Template</div>
						<wizard:templateColumnHeaders entity="${sample}" class="column" columnWidths="[Name:200]"/>
					</div>
				</g:if>
				<div class="row">
					<div class="firstColumn"></div>
					<div class="column">
						<g:if test="${previousTemplate != sample.parentEvent.template.name}">
							<g:set var="previousTemplate" value="${sample.parentEvent.template.name}"/>
							${sample.parentEvent.template.name}
							<div class="helpIcon"></div>
							<div class="helpContent">
								<h1>${sample.parentEvent.template.name}</h1>
								<h2>Template Fields:</h2>
								<g:each var="field" in="${sample.parentEvent.giveFields()}">
									${field.name[0].toUpperCase() + field.name.substring(1)}<br/>
								</g:each>
							</div>
						</g:if>
					</div>
					<div class="column">
						${sample.parentSubject.name}
						<div class="helpIcon"></div>
						<div class="helpContent">
							<h1>${sample.parentSubject.template.name}</h1>
							<h2>Template Fields:</h2>
							<g:each var="field" in="${sample.parentSubject.giveFields()}">
								${field.name[0].toUpperCase() + field.name.substring(1)}<br/>
							</g:each>
						</div>
					</div>
					<div class="column">
						<wizard:templateSelect name="template_${sample.getIdentifier()}" entity="${dbnp.studycapturing.Sample}" value="${sample.template}" addDummy="true" tableEditorChangeEvent="switchTemplate(element);" />
					</div>
					<wizard:templateColumns name="sample_${sample.getIdentifier()}" class="column" id="1" entity="${sample}"/>
				</div>
			</g:each>
			</div>
			<div class="sliderContainer">
				<div class="slider" ></div>
			</div>
		</g:each>
	</g:if>
</wizard:pageContent>