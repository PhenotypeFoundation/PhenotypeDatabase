<%@ page import="dbnp.studycapturing.Study" %>
<%--
  Created by IntelliJ IDEA.
  User: luddenv
  Date: 26-mei-2010
  Time: 13:17:50
  To change this template use File | Settings | File Templates.
--%>
<div id="simpleQuery" class="simplequery">
	<h1>Simple Query</h1>

    <g:form action="pages" name="simpleQueryForm" id="simpleQueryForm">
    <g:if test="${search_term}"><g:set var="preterm" value="${search_term}" /></g:if>

    <div id="accordion">
      <h3><a href="#">Search term</a></h3>
      <div>
        <div class="element">
          <div class="description">Search term (e.g. 'paracetamol')</div>
          <div class="input"><g:textField name="search_term" value="${preterm}" /></div>
        </div>
        <div class="element">
          <div class="description">Species (e.g. 'rattus norvegicus')</div>
          <div class="input"><g:select name="species" from="${species}" value="" noSelection="['':'--- select a species ---']"/></div>
        </div>
        <div class="element">
          <div class="description">Organ (e.g. 'liver')</div>
          <div class="input"><g:select name="organ" from="" value="${organ}" noSelection="['':'--- select organ/tissue ---']"/></div>
        </div>
      </div>

      <h3><a href="#">Simple Assays (optional)</a></h3>
      <div class="element">
        <div id="compoundGroup">

          <g:if test="${showFirstRowCompounds}">
          <div id="compoundRow1">
            <div class="descriptionSA">Compound</div><div class="input"><g:textField name="sa_compound" value=""/></div><div class="descriptionSA">Operator</div><div class="input" id="operatorInput"><g:select name="operator" from="${operators}" value="="/></div><div class="descriptionSA">Value</div><div class="input"><g:textField name="sa_value" value=""/></div>
          </div>
          </g:if>

          <g:else>
            <g:each status="i" in="${search_sa_compounds}" var="compound">
            <div id="compoundRow${i}">
              <div class="descriptionSA">Compound (e.g. 'glucose')</div><div class="input"><g:textField name="sa_compound" value="${search_sa_compounds.get(i)}"/></div><div class="descriptionSA">Type of regulations</div><div class="input"><g:select name="operator" from="${operators}" value="="/></div><div class="descriptionSA">Value</div><div class="input"><g:textField name="sa_value" value="${search_sa_values.get(i)}"/></div>
            </div>
            </g:each>
          </g:else>
        </div>
        <div id="addCompound" class="submit">Add compound</div>
      </div>

      <h3><a href="#">Transcriptomics (optional)</a></h3>

      <div class="element">
        <div id="transcriptomeGroup">
          <div id="transcriptomeRow1">
            <div class="description">List of Gene IDs or pathway IDs</div><div class="input"><g:textField name="genepath" value="" /></div><div class="description">Type of regulations</div><div class="input" id="regulationInput"><g:select name="regulation" from="" value="${regulation}" noSelection="['':'--- select regulation ---']"/></div>
          </div>
        </div>
        <div id="addTranscriptome" class="submit">Add transciptome</div>
      </div>
    </div>

    <g:submitButton name="search" value="Search"  /> <g:if test="${search_term}"><g:submitButton name="reset" value="Reset" /></g:if>

    </g:form>

    <g:if test="${search_term}">
        <h1><g:message code="Search results for term '${search_term}'"/></h1>

        <g:if test="${listStudies}">
          <div class="list">
            <table>
                <thead>
                    <tr>
                        <g:sortableColumn property="id" title="${message(code: 'study.id', default: 'ID')}" />
                        <g:sortableColumn property="title" title="${message(code: 'study.title.label', default: 'Study')}" />
                        <g:sortableColumn property="samples" title="${message(code: 'study.sample', default: 'Samples')}" />
                    </tr>
                </thead>
                <tbody>
                <g:each in="${listStudies}" var="Study" status="i" >
                  <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <td><g:link url="[action:'show',controller:'study']" id="${Study.id}">${fieldValue(bean: Study, field: "id")}</g:link></td>
                    <td><g:link url="[action:'show',controller:'study']" id="${Study.id}">${fieldValue(bean: Study, field: "title")}</g:link></td>
                    <td><g:link url="[action:'show',controller:'study']" id="${Study.id}">${fieldValue(bean: Study, field: "samples")}</g:link></td>
                  </tr>
                </g:each>
                </tbody>
            </table>
          </div>

        </g:if>
    </g:if>

</div>