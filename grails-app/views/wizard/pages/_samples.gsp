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

	<g:if test="${samples}">
		<g:if test="${samples.size() > samplesWithTemplate}">
			<g:set var="showHeader" value="${true}" />
			<h1>Samples that still need to have a template assigned</h1>
			<div class="table">
			<g:each status="s" var="sampleData" in="${samples}">
				<g:if test="${!sampleData.sample.template}">
					<g:if test="${showHeader}">
					<g:set var="showHeader" value="${false}" />
					<div class="header">
						<div class="firstColumn">#</div>
						<div class="column">Template</div>
					</div>
					</g:if>
					<div class="row">
						<div class="firstColumn">${s+1}</div>
						<div class="column">
							<wizard:templateSelect name="template_${s}" entity="${dbnp.studycapturing.Sample}" value="${sampleData['sample'].template}" addDummy="true" ajaxOnChange="switchTemplate" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" />
						</div>
						<wizard:templateColumns name="sample_${s}" class="column" id="1" entity="${sampleData.sample}"/>
					</div>
				</g:if>
			</g:each>
			</div>
			<div class="sliderContainer">
				<div class="slider" ></div>
			</div>
		</g:if>

		<g:each status="n" var="sampleTemplateName" in="${sampleTemplates}">
			<h1>${sampleTemplateName.value.name}</h1>
			<g:set var="showHeader" value="${true}" />
			<div class="table">
			<g:each status="s" var="sampleData" in="${samples}">
				<g:if test="${sampleData.sample.template.toString() == sampleTemplateName.value.name}">
					<g:if test="${showHeader}">
						<g:set var="showHeader" value="${false}" />
						<div class="header">
							<div class="firstColumn">#</div>
							<div class="column">Template</div>
							<wizard:templateColumnHeaders entity="${sampleData.sample}" class="column" />
						</div>
					</g:if>
					<div class="row">
						<div class="firstColumn">${s+1}</div>
						<div class="column">
							<wizard:templateSelect name="template_${s}" entity="${dbnp.studycapturing.Sample}" value="${sampleData['sample'].template}" addDummy="true" ajaxOnChange="switchTemplate" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" />
						</div>
						<wizard:templateColumns name="sample_${s}" class="column" id="1" entity="${sampleData.sample}"/>
					</div>
				</g:if>
			</g:each>
			</div>
			<div class="sliderContainer">
				<div class="slider" ></div>
			</div>
		</g:each>
	</g:if>	
</wizard:pageContent>