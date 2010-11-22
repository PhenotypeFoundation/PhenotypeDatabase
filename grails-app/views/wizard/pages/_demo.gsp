<%
/**
 * Demo / Development template
 *
 * @author  Jeroen Wesbeek
 * @since   20100312
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

<g:if test="${demoElement}">
    <wizard:templateElements entity="${demoElement}" />
</g:if>
<span class="info">
	<span class="title">Ontology chooser</span>
	The ontology box below fetched suggestions from the NCBO webservice
</span>

<b>One (ontology 1032)</b><br/>
Ontology input box: <input type="text" name="test1" rel="ontology-1032" /><br/>
concept_id: <input type="text" name="test1-concept_id" /><br/>
ontology_id: <input type="text" name="test1-ontology_id" /><br/>
ncbo_id: <input type="text" name="test1-ncbo_id" /><br/>
full_id: <input type="text" name="test1-full_id" /><br/>

<b>Two (ontology 1032)</b><br/>
Ontology input box: <input type="text" name="test2" rel="ontology-1032" /><br/>
concept_id: <input type="text" name="test2-concept_id" /><br/>
ontology_id: <input type="text" name="test2-ontology_id" /><br/>
ncbo_id: <input type="text" name="test2-ncbo_id" /><br/>
full_id: <input type="text" name="test2-full_id" /><br/>

<b>Three (ontology 1007)</b><br/>
Ontology input box: <input type="text" name="test3" rel="ontology-1007" /><br/>
concept_id: <input type="text" name="test3-concept_id" /><br/>
ontology_id: <input type="text" name="test3-ontology_id" /><br/>
ncbo_id: <input type="text" name="test3-ncbo_id" /><br/>
full_id: <input type="text" name="test3-full_id" /><br/>

<b>Four (ontology: all)</b><br/>
Ontology input box: <input type="text" name="test4" rel="ontology-all" /><br/>
concept_id: <input type="text" name="test4-concept_id" /><br/>
ontology_id: <input type="text" name="test4-ontology_id" /><br/>
ncbo_id: <input type="text" name="test4-ncbo_id" /><br/>
full_id: <input type="text" name="test4-full_id" /><br/>
