<%
/**
 * Load study to modify page
 *
 * @author  Jeroen Wesbeek
 * @since   20100414
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
	<span class="info">
		<span class="title">Edit a study</span>
		Select the study you would like to modify.
	</span>

	<wizard:studyElement name="study" description="Study" error="study" value="">
		The study you would like to load and edit
	</wizard:studyElement>
</af:page>