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

	<span class="bigtext">What would you like to do?</span>

	<wizard:ajaxButton name="next" class="bigbutton" value="Create a new study" alt="Create a new study" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" />

	<wizard:ajaxButton name="modify" class="bigbutton" value="Modify an existing study" alt="Modify an existing study" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" />

	<g:if env="development">
	<span class="info">
		<span class="known">Known issues</span>
		<ul>
			<li>navigating away from the wizard will result in loss of work. While you are currently warned when
			    clicking links outside of the wizard, this problem still exists when clicking 'refresh' or the
				back / forward buttons</li>
		</ul>
	</span>
	<!--g:render template="pages/demo"//-->
	</g:if>

</wizard:pageContent>