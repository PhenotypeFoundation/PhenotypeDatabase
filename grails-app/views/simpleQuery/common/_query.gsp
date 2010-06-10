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
    <g:if test="${term}"><g:set var="preterm" value="${term}" /></g:if>
    <div class="content">
      <div class="element">
        <div class="description">Search term (e.g. 'paracetamol')</div>
        <div class="input"><g:textField name="term" value="${preterm}" /></div>
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
    <g:submitButton name="search" value="Search" /> <g:if test="${term}"><g:submitButton name="reset" value="Clear" /></g:if>
    </g:form>

    <br><br>

    <div id="accordion">
      <h3><a href="#">Simple Assays</a></h3>
      <div class="element">
        <g:form action="pages" name="addCompound" id="addCompound">
        <div class="description">Compound (e.g. 'glucose')</div>
        <div class="input"><g:textField name="compound" value="" /></div>
        <div class="description">Value</div>
        <div class="input"><g:textField name="compound_value" value="" /></div>
        <g:submitButton id="addCompound" name="addCompound" value="Add compound" />
        </g:form>
      </div>

      <h3><a href="#">Transcriptomics</a></h3>

      <div class="element">
        <g:form action="pages" name="addTransciptome" id="addTransciptome">
        <div class="description">List of Gene IDs or pathway IDs</div>
        <div class="input"><g:textField name="genepath" value="" /></div>
        <div class="description">Type of regulations</div>
        <div class="input"><g:select name="regulation" from="" value="${regulation}" noSelection="['':'--- select regulation ---']"/></div>
        <g:submitButton id="addTransciptome" name="addTransciptome" value="Add transciptome" />
        </g:form>
      </div>
    </div>

    <br><br>

    <g:if test="${term}">
        <h1><g:message code="Search results for term ${term}"/></h1>

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