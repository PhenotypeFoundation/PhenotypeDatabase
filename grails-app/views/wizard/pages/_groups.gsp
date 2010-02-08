<%
/**
 * Subjects page
 *
 * @author  Jeroen Wesbeek
 * @since   20100113
 * @package wizard
 * @see     dbnp.studycapturing.WizardTagLib::previousNext
 * @see     dbnp.studycapturing.WizardController
 *
 * Revision information:
 * $Rev: 140 $
 * $Author: duh $
 * $Date: 2010-01-27 13:32:41 +0100 (Wed, 27 Jan 2010) $
 */
%>
<wizard:pageContent>
<div class="grouping">

	<div class="subjects">
		<g:each var="subject" status="i" in="${subjects}"><div class="subject">${subject.name}</div>
		</g:each>
	</div>
	<div class="right">
		<div class="form">
			name: <g:textField name="name" value="" /><br />
			<wizard:ajaxButton name="add" value="Add Group" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()" />
		</div>
		<div class="groups">
		<g:each var="group" status="i" in="${groups}">
			<div class="group">
				<div class="label">${group.name}</div>
				<div class="subjects">
				</div>
			</div>
		</g:each>
		</div>
	</div>
</div>
</wizard:pageContent>