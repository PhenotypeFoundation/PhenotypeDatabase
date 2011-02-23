<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Simple study wizard</title>
	
	<g:render template="javascripts" />
</head>
<body>
	<div class="simpleWizard">
		<h1>Missing properties</h1>
	
		<g:if test="${flash.error}">
			<div class="errormessage">
				${flash.error.toString().encodeAsHTML()}
			</div>
		</g:if>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message.toString().encodeAsHTML()}
			</div>
		</g:if>		
			
		<g:form class="simpleWizard" name="missingFields" action="missingFields" controller="simpleWizard">
			<input type="hidden" name="wizard" value="true" />
			<input type="hidden" name="event" value="refresh" />
	
		   	<span class="info"> 
				<span class="title">Fill in missing properties</span>
				A number of records could not be succesfully imported. Fields giving an error are indicated by a red color. Please correct them before continuing.
				
				<g:if test="${rules}">
					<br /><br />The following errors occurred:<br />${rules}
				</g:if> 
			</span> 
			    
			<script type="text/javascript">
			 $(document).ready(function() {
			        // mark error fields
			        <g:each in="${imported.failedCells}" var="record">
			        	<g:each in="${record.importcells}" var="cell">
			        		markFailedField( "${cell.entityidentifier.toString().encodeAsJavaScript()}", "${cell.value.toString().encodeAsJavaScript()}" );
			        	</g:each>
			        </g:each>
			  });
			</script>
			
			<div class="wizard" id="wizard">
			    <div class="tableEditor">
				<g:set var="showHeader" value="${true}"/>
				    <g:each status="index" var="table" in="${imported.data}">
						<g:each status="i" var="entity" in="${table}">
						    <g:if test="${showHeader}">
								<g:set var="showHeader" value="${false}"/>
								<div class="header">
									<div class="firstColumn"></div>
									<af:templateColumnHeaders entity="${entity}" class="column" />
								</div>
								<input type="hidden" name="entity" value="${entity.getClass().getName()}">
						    </g:if>
						    <div class="row">
								<div class="firstColumn"></div>
								<af:templateColumns id="${entity.hashCode()}" entity="${entity}" template="${entity.template}" name="entity_${entity.getIdentifier()}" class="column" subject="${entity.hashCode()}" addDummy="true" />
						    </div>
						</g:each>
				    </g:each>
			    </div>
			    <div class="sliderContainer">
			      <div class="slider"></div>
			    </div>
		    <div>
		</g:form>
			
		<p class="options">
			<a href="#" onClick="submitForm( 'missingFields', 'previous' ); return false;" class="previous">Previous</a>
			<a href="#" onClick="submitForm( 'missingFields', 'next' ); return false;" class="next">Next</a>
		</p>
	</div>
</body>
</html>
