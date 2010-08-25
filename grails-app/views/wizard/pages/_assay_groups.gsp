<%
/**
 * Assay Groups page
 *
 * @author  Jeroen Wesbeek
 * @since   20100817
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
		<span class="title">Assign samples to assays</span>
		In the previous page you defined assays. In this page, you can define which assays are performed on which samples.
		The samples are grouped according to the EventGroups of their defining SamplingEvents.
	</span>

	<%
	    // TODO: render a table of assays (in the columns) versus samples (in the rows)
		// with the samples grouped according to the EventGroups of the parent SamplingEvents
		// and with all boxes checked by default
	%>

</wizard:pageContent>