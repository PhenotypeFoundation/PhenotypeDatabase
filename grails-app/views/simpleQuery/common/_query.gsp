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
    <div class="content">
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
    <g:submitButton name="search" value="Search" /> <g:if test="${search_term}"><g:submitButton name="reset" value="Clear" /></g:if>

    <br><br>

    <div id="accordion">
      <h3><a href="#">Simple Assays</a></h3>
      <div class="element">
        <div id="compoundGroup">
          <g:if test="${resultString}">
          <div id="compoundRow1">
            <div class="description">Compound (e.g. 'glucose')</div>
            <div class="input"><g:textField name="sa_compound" value="${search_sa_compounds}"/></div>
            <div class="description">Value</div>
            <div class="input"><g:textField name="sa_value" value="${search_sa_values}"/></div>
          </div>
          </g:if>
          <g:else>
            <g:each status="i" in="${search_sa_compounds}" var="compound">
            <div id="compoundRow${i}">
              <div class="description">Compound (e.g. 'glucose')</div>
              <div class="input"><g:textField name="sa_compound" value="${compound}"/></div>
              <div class="description">Value</div>
              <div class="input"><g:textField name="sa_value" value="${search_sa_values[i]}"/></div>
            </div>
            </g:each>
          </g:else>
        </div>
        <div id="addCompound">Add compound</div>
      </div>

      <h3><a href="#">Transcriptomics</a></h3>

      <div class="element">
        <div id="transcriptomeGroup">
          <div id="transcriptomeRow1">
            <div class="description">List of Gene IDs or pathway IDs</div>
            <div class="input"><g:textField name="genepath" value="" /></div>
            <div class="description">Type of regulations</div>
            <div class="input" id="regulationInput"><g:select name="regulation" from="" value="${regulation}" noSelection="['':'--- select regulation ---']"/></div>
          </div>
        </div>
        <div id="addTranscriptome">Add transciptome</div>
      </div>
    </div>
    </g:form>

    <br><br>

    <g:if test="${search_term}">
        <h1><g:message code="Search results for term ${search_term}"/></h1>

        <g:if test="${listStudies}">
          <div class="list">
            <table>
                <thead>
                    <tr>
                        <g:sortableColumn property="title" title="${message(code: 'study.title.label', default: 'Study')}" />
                    </tr>
                </thead>
                <tbody>
                <g:each in="${listStudies}" var="Study" status="i" >
                  <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <td><g:link action="show" id="${Study.id}">${fieldValue(bean: Study, field: "title")}</g:link></td>
                  </tr>
                </g:each>
                </tbody>
            </table>
          </div>

        </g:if>
    </g:if>

</div>