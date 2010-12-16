  <%
	/**
	 * Missing properties template which shows missing properties
	 *
	 * @author Tjeerd Abma
	 * @since 20100623
	 * @package importer
	 *
	 * Revision information: 
	 * $Rev: 1277 $
	 * $Author: t.w.abma@umcutrecht.nl $
	 * $Date: 2010-12-16 15:42:15 +0100 (Thu, 16 Dec 2010) $
	 */
%>

<link rel="stylesheet" href="${resource(dir: 'css', file: 'wizard.css')}"/>

<script type="text/javascript" src="${resource(dir: 'js', file: 'table-editor.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js', file: 'jquery.qtip-1.0.0-rc3.min.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js', file: 'importer.js')}"></script>

<g:if env="production">
<script type="text/javascript" src="${resource(dir: 'js', file: 'SelectAddMore.min.js')}"></script>
</g:if><g:else>
<script type="text/javascript" src="${resource(dir: 'js', file: 'SelectAddMore.js')}"></script>
</g:else>

<script>
  $(document).ready(function() {

// handle term selects
    new SelectAddMore().init({
        rel     : 'term',
        url     : baseUrl + '/termEditor',
        vars    : 'ontologies',
        label   : 'add more...',
        style   : 'addMore',
        onClose : function(scope) {
            refreshFlow();            
             //location.reload();
             /*updatefield = '<input type = "hidden" name="updatefield" value="true" / >';
             $('#missingpropertiesform').append(updatefield);
             $('#missingpropertiesform').submit();*/
        }
    });

  });
</script>

<script type="text/javascript">
  $(document).ready(function() {
        // mark error fields
        <g:each in="${failedcells}" var="record">
          <g:each in="${record.importcells}" var="cell">
          var element = $("select[name=entity_${cell.entityidentifier}_${cell.mappingcolumn.property}]");

          element.addClass('error')
          element.append( new Option("Invalid: ${cell.value}","#invalidterm", true, true) );
  
          </g:each>
        </g:each>
  });
</script>
  
<g:if test="${importer_invalidentities}"><br/><br/>
  <b>There are ${importer_invalidentities} entities which could not be validated, they are indicated by a red color, please correct them before continuing.</b>
</g:if>

<div class="wizard" id="wizard">
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
			<wizard:templateColumns id="${entity.hashCode()}" entity="${entity}" template="${entity.template}" name="entity_${entity.getIdentifier()}" class="column" subject="${entity.hashCode()}" addDummy="true" />
		    </div>
		</g:each>
	    </g:each>
    </div>
    <div>
	<br/>	
    </div>
</div>