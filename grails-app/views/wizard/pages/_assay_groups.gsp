<%
/**
 * Assay Groups page
 *
 * @author  Jeroen Wesbeek
 * @since   20100817
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
		<span class="title">Assign samples to assays</span>
		In the previous page you defined assays. In this page, you can define which assays are performed on which samples.
		The samples are grouped according to the EventGroups of their defining SamplingEvents.
	</span>

	<g:if test="${(study.samples && study.assays)}">
		<g:set var="previousTemplate" value=""/>
		<div class="table">
		<div class="header">
			<div class="firstColumn">#</div>
			<div class="column" style="width:150px;">Sample Type</div>
			<div class="column" style="width:200px;">Sample Name</div>
			<g:each var="assay" in="${study.assays}">
			<div class="column">${assay}</div>
			</g:each>
		</div>
		<g:each var="sampleTemplate" in="${study.giveSampleTemplates()}">
			<g:each var="sample" in="${study.giveSamplesForTemplate(sampleTemplate)}">
				<div class="row">
					<div class="firstColumn">${sample.getIdentifier()}</div>
					<div class="column">
						<g:if test="${previousTemplate != sampleTemplate.name}">
							<g:set var="previousTemplate" value="${sampleTemplate.name}"/>
							${sampleTemplate.name}
							<div class="helpIcon"></div>
							<div class="helpContent">
								<h1>${sampleTemplate.name}</h1>
								<h2>Template Fields:</h2>
								<g:each var="field" in="${sample.giveFields()}">
									${field.name[0].toUpperCase() + field.name.substring(1)}<br/>
								</g:each>
							</div>
						</g:if>
					</div>
					<div class="column">${sample.name}</div>
					<g:each var="assay" in="${study.assays}">
					<div class="column">
						<input type="checkbox" name="sample_${sample.getIdentifier()}_assay_${assay.getIdentifier()}"<g:if test="${assay.samples.find{ it == sample } }"> checked="checked"</g:if>/>
					</div>
					</g:each>
				</div>
			</g:each>
		</g:each>
		</div>
		<div class="sliderContainer">
			<div class="slider" ></div>
		</div>
	</g:if>

</wizard:pageContent>