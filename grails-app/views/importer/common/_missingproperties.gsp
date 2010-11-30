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
            //refreshWebFlow();?
             //location.reload();
             updatefield = '<input type = "hidden" name="updatefield" value="true" / >';
             $('#missingpropertiesform').append(updatefield);
             $('#missingpropertiesform').submit();
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

<div class="wizard" id="wizard">
<g:form name="missingpropertiesform" id="missingpropertiesform" action="saveMissingProperties">
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
	<input type="submit" value="Accept changes">
    </div>
</g:form>    
</div>