<%
	/**
	 * Wizard page three
	 *
	 * @author Jeroen Wesbeek
	 * @since 20100113
	 * @package wizard
	 * @see dbnp.studycapturing.WizardTagLib::previousNext
	 * @see dbnp.studycapturing.WizardController
	 *
	 * Revision information:
	 * $Rev$
	 * $Author$
	 * $Date$
	 */
%>
<wizard:pageContent>
	<wizard:textFieldElement name="myThirdName" value="a lotta description" description="very fancy help text">
		<img src="http://www.grails.org/images/new/grailslogo_topNav.png"/><br/>
		This is made with <a href="http://www.grails.org/" target="_new">Grails</a>...
	</wizard:textFieldElement>
	<wizard:textFieldElement name="myZero" value="12" description="more than four?" maxlength="4">
		you cannot enter more than 4 elements here, and also the
	 	width of this field is automatically scaled to the maximum character
	 	size
	</wizard:textFieldElement>
	<wizard:textFieldElement name="myThirdName" value="a lotta description" description="long help text">
		<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium dignissim tellus, id pharetra erat tempus sed. Sed bibendum libero eu lorem pretium nec fermentum ligula faucibus. Morbi gravida interdum ornare. Praesent lectus mi, ullamcorper nec semper nec, vulputate ornare elit. Nam eros metus, egestas a varius eget, facilisis ac purus. Maecenas lectus erat, rutrum id consequat ac, scelerisque ut diam.</p>
		<p>Donec euismod, tellus facilisis semper elementum, neque lorem volutpat ante, ac consectetur lectus ante sit amet neque. Donec hendrerit, libero quis suscipit iaculis, lacus ligula viverra nibh, eu condimentum diam dui sit amet quam. Praesent turpis orci, laoreet sodales adipiscing eget, ultrices at augue. Nullam sed dolor a velit posuere euismod. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Ut libero mauris, fermentum id congue sit amet, pharetra in purus. Lorem ipsum dolor sit amet, consectetur adipiscing elit.</p>
		<p>Nullam a blandit quam. Cras porta tempus lectus, vel varius lacus vulputate in. Aenean ac nunc lectus, hendrerit tempor elit. Sed ut varius diam.</p>
	</wizard:textFieldElement>
</wizard:pageContent>