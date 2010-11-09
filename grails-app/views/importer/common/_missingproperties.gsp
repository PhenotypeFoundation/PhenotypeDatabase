<%
	/**
	 * Missing properties template which shows missing properties
	 *
	 * @author Tjeerd Abma
	 * @since 20100623
	 * @package importer
	 *
	 * Revision information:
	 * $Rev$
	 * $Author$
	 * $Date$
	 */
%>

<link rel="stylesheet" href="${resource(dir: 'css', file: 'wizard.css')}"/>

<script type="text/javascript" src="${resource(dir: 'js', file: 'table-editor.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.qtip-1.0.0-rc3.min.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js', file: 'importer.js')}"></script>

<div class="wizard" id="wizard">
<g:form name="missingpropertiesform" action="saveMissingProperties">
    <div class="table">
	<g:set var="showHeader" value="${true}"/>
	    <g:each status="index" var="table" in="${datamatrix}">
		<g:each status="i" var="entity" in="${table}">                  
		    <g:if test="${showHeader}">
			<g:set var="showHeader" value="${false}"/>
			<div class="header">
			    <div class="firstColumn">#</div>
			    <div class="firstColumn"></div>
				<wizard:templateColumnHeaders entity="${entity}" class="column" />
			</div>
			<input type="hidden" name="entity" value="${entity.getClass().getName()}">
		    </g:if>
		    <div class="row">
			<div class="firstColumn">#</div>
			<div class="firstColumn"></div>
			<wizard:templateColumns id="${entity.hashCode()}" entity="${entity}" template="${entity.template}" name="entity_${entity.hashCode()}" class="column" subject="${entity.hashCode()}" addDummy="true" />
		    </div>
		</g:each>
	    </g:each>
    </div>
    <div>
	<br/>
	<input type="submit" value="Accept changes">
    </div>
</g:form>    
</div>

