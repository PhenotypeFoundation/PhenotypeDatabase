<%
/**
 * Templates page
 *
 * @author  Jeroen Wesbeek
 * @since   20100303
 * @package wizard
 * @see     dbnp.studycapturing.WizardTagLib::previousNext
 * @see     dbnp.studycapturing.WizardController
 *
 * Revision information:
 * $Rev: 959 $
 * $Author: j.a.m.wesbeek@umail.leidenuniv.nl $
 * $Date: 2010-10-20 21:13:14 +0200 (Wed, 20 Oct 2010) $
 */
%>
<af:page>
	<span class="bigtext">What would you like to do?</span>
	<af:ajaxButton name="next" class="bigbutton" value="Create a new study" alt="Create a new study" afterSuccess="onPage()" />
	<af:ajaxButton name="modify" class="bigbutton" value="Edit an existing study" alt="Edit an existing study" afterSuccess="onPage()" />
	<af:ajaxButton name="import" class="bigbutton" value="Import a study" alt="Import a study" afterSuccess="onPage()" />

	<span class="info">
		<span class="title">Create a new study via the step-by-step interface</span>
		This web interface will guide you through the total incorporation of your study in several steps:
		<ul>
			<li>Include all general study information</li>
			<li>Include all subject specific information</li>
			<li>Include all information on all variables of the study (treatment, challenges, sampling etc.)</li>
			<li>Confirmation of all information</li>
		</ul>
		It is possible to go back and forth in the tool, without losing information. Definitive storage will only occur after the confirmation step.
		<span class="title">Create a new study via the spreadsheet importer</span>
		You can do this by choosing Studies > Import data in the menu.
		This part of the applications will help you to upload large studies. This import function can be used for all study components (study, subject and event information) at once.
	    <span class="title">Edit an existing study</span>
		Only study owners can modify a their own studies. This part of the application can be used to extend the study information, for instance with new measurements.
	</span>

	<g:if env="development"><!--g:render template="pages/demo"//--></g:if>
</af:page>