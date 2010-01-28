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
		<ol class="selectable">
			<g:each var="subject" status="i" in="${subjects}"><li class="ui-widget-content">Subject ${i} ${subject.species}</li></g:each>
		</ol>
	</div>

	<div class="groups">
		<div class="ui-widget-header droppable">
			<h1>Group 1<h1>
			<p>drop here...</p>
		</div>
	</div>
</div>
</wizard:pageContent>