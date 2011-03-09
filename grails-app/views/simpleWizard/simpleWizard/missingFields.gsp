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
	
		<g:if test="${error}">
			<div class="errormessage">
				${error.toString().encodeAsHTML()}
			</div>
		</g:if>
		<g:if test="${message}">
			<div class="message">
				${message.toString().encodeAsHTML()}
			</div>
		</g:if>	
			
		<g:form class="simpleWizard" name="missingFields" action="simpleWizard">
			<input type="hidden" name="_eventId" value="refresh" />
	
		   	<span class="info"> 
				<span class="title">Fill in missing properties</span>
				A number of records could not be succesfully imported. Fields giving an error are indicated by a red color. Please correct them before continuing.
				
				<g:if test="${imported.errors}">
					<br /><br />The following errors occurred:<br />
					<g:each in="${imported.errors}" var="err">
						${err}<br />
					</g:each>
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
				<g:set var="showHeader" value="${true}" />
				<g:set var="previousTemplate" value=""/>			
				<div class="tableEditor">
					<g:each var="record" in="${imported.data}">
						<g:if test="${showHeader}">
							<g:set var="showHeader" value="${false}" />
							<div class="header">
								<div class="firstColumn"></div>
								<g:each var="entity" in="${record}">
									<g:if test="${entity}">
										<af:templateColumnHeaders entity="${entity}" class="column" columnWidths="[Name:100]"/>
									</g:if>
								</g:each>
							</div>
						</g:if>
						<div class="row">
							<div class="firstColumn"></div>
							<g:each var="entity" in="${record}">
								<g:set var="entityName" value="${entity?.class?.name ? entity.class.name[ entity.class.name.lastIndexOf( '.' ) + 1 .. -1 ] : 'unknown'}" />
								<g:if test="${entity}">
									<af:templateColumns  id="${entity.hashCode()}" name="${entityName.toLowerCase()}_${entity.getIdentifier()}" template="${entity.template}" class="column" id="1" entity="${entity}" addDummy="true" subject="${entity.hashCode()}" />
								</g:if>
							</g:each>
						</div>
					</g:each>
				</div>
				<div class="sliderContainer">
					<div class="slider" ></div>
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
