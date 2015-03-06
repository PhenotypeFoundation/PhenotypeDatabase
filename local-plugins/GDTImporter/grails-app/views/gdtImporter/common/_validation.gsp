  <%
	/**
	 * Missing properties template which shows missing properties
	 *
	 * @author Tjeerd Abma
	 * @since 20100623
	 * @package importer
	 *
	 * Revision information: 
	 * $Rev: 1492 $
	 * $Author: t.w.abma@umcutrecht.nl $
	 * $Date: 2011-02-04 11:16:53 +0100 (Fri, 04 Feb 2011) $
	 */
%>
<script type="text/javascript">
 $(document).ready(function() {
        // mark error fields
        <g:each in="${failedFields}" var="field">
          var element = $("select[name=${field.entity}]");

          if (element.size()) {
            element.addClass('error');
            element.append( new Option("Invalid: ${field.originalValue}","${field.originalValue}", true, true) );
          } else {
            element = $("input[name=${field.entity}]");
            element.addClass('error');
          }
        </g:each>
  });
</script>

<div class="wizard" id="wizard">
    <div class="tableEditor">
	<g:set var="showHeader" value="${true}"/>
	    <g:each status="index" var="entity" in="${entityList}">
		    <g:if test="${showHeader}">
			<g:set var="showHeader" value="${false}"/>
			<div class="header">
				<div class="firstColumn"></div>
				<af:templateColumnHeaders entity="${entity}" class="column" />
			</div>
			<input type="hidden" name="entity" value="${entity.class.name}">
		    </g:if>
		    <div class="row">
				<div class="firstColumn"></div>
			<af:templateColumns id="${entity.hashCode()}" entity="${entity}" template="${entity.template}" name="entity_${entity.identifier}" class="column" subject="${entity.hashCode()}" addDummy="true" />
		    </div>
		</g:each>
    </div>
    <div>
	<br/>	
    </div>
</div>