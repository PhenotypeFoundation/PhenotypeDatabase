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
    <div class="content">
      <div class="element">
        <div class="description">Search term (e.g. 'paracetamol')</div>
        <div class="input"><g:textField name="term" value="" /></div>
      </div>
      <div class="element">
        <div class="description">Species (e.g. 'rattus norvegicus')</div>
        <div class="input"><g:select name="species" from="" value="${species}" noSelection="['':'--- select a species ---']"/></div>
      </div>
      <div class="element">
        <div class="description">Organ (e.g. 'liver')</div>
        <div class="input"><g:select name="organ" from="" value="${organ}" noSelection="['':'--- select organ/tissue ---']"/></div>
      </div>
    </div>
    <g:submitButton name="search" value="Search" />

    <br><br>
  

    <div id="accordion">
      <h3><a href="#">Clinical Data</a></h3>
      <div class="element">
        <div class="description">Compound (e.g. 'glucose')</div>
        <div class="input"><g:textField name="compound" value="" /></div>
        <div class="description">Value</div>
        <div class="input"><g:textField name="compound_value" value="" /></div>
        <g:submitButton name="add_compound" value="Add compound" />
      </div>

      <h3><a href="#">Transcriptomics</a></h3>
      <div class="element">
        <div class="description">List of Gene IDs or pathway IDs</div>
        <div class="input"><g:textField name="genepath" value="" /></div>
        <div class="description">Type of regulations</div>
        <div class="input"><g:select name="regulation" from="" value="${regulation}" noSelection="['':'--- select regulation ---']"/></div>
        <g:submitButton name="add_transciptome" value="Add transciptome" />
      </div>
      
      <h3><a href="#">Metabolomics</a></h3>
      <div>boe boe boe</div>
    </div>

    </g:form>

</div>