<%
/**
 * Finish page
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
<af:page>

	<span class="info">
		<span class="todo">Done!</span>
		<g:if test="${quickSave}">
			The modifications you have made to this study have been saved.
		</g:if>
		<g:else>
			The study you just created has been saved.
		</g:else>

		You can now <g:link controller="study" action="show" id="${study.id}">view the study</g:link>,
		<g:link controller="studyWizard" params="[jump:((session.jump?.action == 'simpleedit') ? 'simpleedit' : 'edit')]" id="${study.id}">edit the study</g:link>
		<g:if test="${quickSave}"> again</g:if> or
		<g:link controller="studyWizard" params="[jump:'create']">create a new study</g:link>.

	</span>

	<script type="text/javascript">
		// disable redirect warning
		var warnOnRedirect = false;
	</script>

</af:page>