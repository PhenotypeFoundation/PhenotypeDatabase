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

	<span class="info">
		<span class="title">Samples</span>
		blah blah blaaa....
	</span>

	<g:if test="${samples}">
		<div class="table">
		<g:each status="s" var="sampleData" in="${samples}">
			<div class="row">
				<wizard:templateSelect name="template_${s}" entity="${dbnp.studycapturing.Sample}" addDummy="true" ajaxOnChange="switchTemplate" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" />
				<wizard:templateColumns class="column" id="1" entity="${sampleData['sample']}"/>
				<% /*
					${sampleData}
					<!-- wizard:templateColumns class="column" id="1" entity="${sampleData['sample']}" //-->
					<!-- div class="column"><wizard:templateSelect entity="${sampleData.sample}" /></div //-->
					*/ %>
			</div>
		</g:each>
		</div>
	</g:if>

</wizard:pageContent>