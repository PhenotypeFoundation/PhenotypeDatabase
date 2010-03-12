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
 * $Rev$
 * $Author$
 * $Date$
 */
%>
<wizard:pageContent>
	<span class="info">
		<span class="title">Create or Modify</span>
		This page is still empty, but in the future you will be able to load a study to modify it, or choose
		to create a new one. At this moment you will only be able to create a new study so please click 'next'
		to continue
	</span>

	<span class="info">
		<span class="known">Known issues</span>
		<ul>
			<li>navigating away from the wizard will result in loss of work. While you are currently warned when
			    clicking links outside of the wizard, this problem still exists when clicking 'refresh' or the
				back / forward buttons</li>
			<li>no data is actually stored yet at the end of the wizard</li>
		</ul>
	</span>

	<!-- g:render template="pages/demo" //-->

</wizard:pageContent>