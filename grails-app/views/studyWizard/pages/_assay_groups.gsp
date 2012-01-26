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
<%@ page import="org.dbnp.gdt.TemplateFieldType" %>
<%@ page import="org.dbnp.gdt.RelTime" %>
<af:page>
	<span class="info">
		<span class="title">Assign samples to assays</span>
		In the previous page you defined assays. In this page, you can define which assays are performed on which samples.
		The samples are grouped according to the EventGroups of their defining SamplingEvents.
	</span>

	<g:if test="${(study.samples && study.assays)}">
		<g:set var="previousTemplate" value=""/>
		<div class="tableEditor">
		<div class="header">
			<div class="firstColumn"></div>
			<div class="column" style="width:150px;">Sample Type</div>
			<div class="column" style="width:350px;">Sample Name</div>
			<g:each var="assay" in="${study.assays}">
			<div class="column">${assay}</div>
			</g:each>
		</div>
		<g:each var="sampleTemplate" in="${study.giveSampleTemplates()}">
			<g:each var="sample" in="${study.giveSamplesForTemplate(sampleTemplate)}">
				<div class="row">
					<div class="firstColumn"></div>
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
                        <div class="helpIcon"></div>
                        <div class="helpContent">
                            <h2>Time info:</h2>
                            <g:if test="${sample.parentEvent?.template.getFieldsByType(TemplateFieldType.RELTIME).isEmpty()}">
                                <b>Not available</b>
                            </g:if>
                            <g:else>
                                <g:each var="field" in="${sample.parentEvent?.template.getFieldsByType(TemplateFieldType.RELTIME)}">
                                    <b>${field.name}</b><br/>
                                    ${new RelTime(sample.parentEvent?.getFieldValue(field.name))}<br/>
                                </g:each>
                            </g:else>
                            <h2>String info:</h2>
                            <g:if test="${sample.parentEvent?.template.getFieldsByType(TemplateFieldType.STRING).isEmpty()}">
                                <b>Not available</b>
                            </g:if>
                            <g:else>
                                <g:each var="field" in="${sample.parentEvent?.template.getFieldsByType(TemplateFieldType.STRING)}">
                                    <b>${field.name}</b><br/>
                                    ${sample.parentEvent?.getFieldValue(field.name)}<br/>
                                </g:each>
                            </g:else>
                        </div>
					</div>
					</g:each>
				</div>
			</g:each>
		</g:each>
		</div>
	</g:if>

</af:page>