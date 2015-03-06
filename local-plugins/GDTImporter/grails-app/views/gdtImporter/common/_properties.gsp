<%@ page import="org.dbnp.gdt.GdtService" %>
<%
/**
 * Properties template which shows columns and data and allows to assign Excel columns
 * to properties (TemplateFields)
 *
 * @author Tjeerd Abma
 * @since 20100210
 * @package gdtimporter
 *
 * Revision information: 
 * $Rev: 1556 $
 * $Author: t.w.abma@umcutrecht.nl $
 * $Date: 2011-02-24 13:38:17 +0100 (Thu, 24 Feb 2011) $
 */
%>

<table>
	<tr><td colspan="3"><h4>${entityToImport.name}</h4></td></tr>
	<tr>
		<td class="header" width="70%">
			<div id="currentmapping">Current import mapping:
			<g:if test="${importMappingInstance}">
				'${importMappingInstance.name}'
			</g:if>
			<g:else>
				none
			</g:else>
			</div>
		<td class="header">
			<input class="buttonsmall" id="clearselect" type="button" value="Clear" name="clearselect"
				   title="Clear all selections">
			<input class="buttonsmall" id="fuzzymatchselect" type="button" value="Match" name="fuzzymatchselect"
				   title="Automatically match columns to properties">
			<input type="hidden" name="useFuzzymatching" id="useFuzzymatching" value="false">
			<input class="buttonsmall" id="savepropertiesbutton" type="button" value="Save" name="savepropertiesbutton"
				   title="Save the currently set mappings">
			<input class="buttonsmall" id="loadpropertiesbutton" type="button" value="Load" name="loadpropertiesbutton"
				   title="Load previously saved mappings">
			<input class="buttonsmall" id="deletepropertiesbutton" type="button" value="Delete"
				   name="deletepropertiesbutton" title="Delete previously saved mappings">

			<div id="savemapping" style="display:none">
				Give current mapping a name:
				<input type="text" name="importMappingName" size="20" id="importMappingName">
				<input type="button" id="savemappingok" value="OK">
			</div>

			<div id="loadmapping" style="display:none">
				Select an existing mapping:
				<g:select name="loadImportMappingId" from="${ImportMappingList}"
						  noSelection="['':'-Select mapping-']" optionValue="name" optionKey="id"/>
				<input type="button" id="loadmappingok" value="OK">
			</div>

			<div id="deletemapping" style="display:none">
				Delete a mapping:
				<g:select name="deleteImportMappingId" from="${ImportMappingList}"
						  noSelection="['':'-Select mapping-']" optionValue="name" optionKey="id"/>
				<input type="button" id="deletemappingok" value="OK">
			</div>
		</td>
	</td>
	</tr>
</table>

<div style="width:auto">
	<table id="datamatrix">
		<thead>
		<tr>
			<g:set var="usedfuzzymatches" value="${'-'}"/>
			<g:each var="mappingcolumn" in="${header}" status="index">
				<!-- set selected values based on submitted columnproperties, actually refresh -->
				<g:if test="${columnProperty}">
					<g:set var="selected" value="${columnProperty.index['' + mappingcolumn.index + '']}"/>
				</g:if>
				<g:else>
					<g:set var="selected" value="${mappingcolumn.property}"/>
				</g:else>

				<g:set var="matchvalue" value="${mappingcolumn.name}"/>
				<th class="importerheader">${mappingcolumn.name}<br/>
					<!-- store the found match -->
					<g:set var="fuzzymatch"
						   value="${GdtImporter.propertyChooser(name:columnproperty, mappingcolumn:mappingcolumn, matchvalue:mappingcolumn.name, selected:selected, useFuzzymatching:useFuzzymatching, templateId:entityToImportSelectedTemplateId, returnmatchonly:'true')}"/>

					<g:if test="${usedfuzzymatches.contains( fuzzymatch.toString() ) }">
						<g:set var="matchvalue" value=""/>
					</g:if>

					<GdtImporter:propertyChooser name="columnproperty" mappingcolumn="${mappingcolumn}"
												 matchvalue="${matchvalue}" selected="${selected}"
												 useFuzzymatching="${useFuzzymatching}"
												 templateId="${entityToImportSelectedTemplateId}"
												 treshold="0.3" extraOptions="${NONGENERIC_extraOptions}"/>
				</th>
				<!-- build up a string with fuzzy matches used, to prevent duplicate fuzzy matching -->
				<g:set var="usedfuzzymatches" value="${usedfuzzymatches + ',' + fuzzymatch.toString() }"/>

			</g:each>
		</tr>
		</thead>
		<tbody>
		<g:each var="row" in="${dataMatrix}">
			<tr>
				<g:each var="column" in="${row}">
					<td class="dataMatrix">
						<g:if test="${column.toString()==''}">.</g:if>
						<g:else>${column.toString()}</g:else>
					</td>
				</g:each>
			</tr>
		</g:each>
		</tbody>
	</table>

</div>