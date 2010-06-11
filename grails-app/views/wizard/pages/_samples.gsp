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
		<g:each var="sampleData" in="${samples}">
			${sampleData}
			<div class="row">
				<!-- wizard:templateColumns class="column" id="1" entity="${sampleData['sample']}" //-->
				<!-- div class="column"><wizard:templateSelect entity="${sampleData.sample}" /></div //-->
			</div>
		</g:each>
		</div>
	</g:if>

</wizard:pageContent>