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

	<img src="../images/wizard/wizard-chooser.png" />

	<span class="info">
		<span class="todo">TODO</span>
		<ul>
			<li>the buttons above actually do not yet work, it's just a mockup. In this prototype you can only create
			    a study; modifying a study will be implemented later. Click 'next' to continue...</li>
		</ul>
	</span>

	<span class="info">
		<span class="known">Known issues</span>
		<ul>
			<li>navigating away from the wizard will result in loss of work. While you are currently warned when
			    clicking links outside of the wizard, this problem still exists when clicking 'refresh' or the
				back / forward buttons</li>
		</ul>
	</span>

	<!-- g:render template="pages/demo" //-->

</wizard:pageContent>