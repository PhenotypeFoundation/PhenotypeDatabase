<%@ page import="org.dbnp.gdt.GdtService" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Simple study wizard</title>
	
	<g:render template="javascripts" />
	
</head>
<body>
	<div class="simpleWizard">
		<h1>Existing samples</h1>
		
		<span class="info"> 
			<span class="title">Update sample properties</span> 
			Enter all the information about your samples. If you want to update the samples using excel, click
			the 'Update using excel' button below.
		</span>
		
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
		
		<g:if test="${flash.validationErrors}">
			<% def uniqueMessages = flash.validationErrors.value.unique(); %>
			<div class="errormessage">
				<g:each var="error" in="${uniqueMessages}">
					${error.value}<br />
				</g:each>
			</div>
		</g:if>  		
		
		<g:form class="simpleWizard" name="existingSamples" action="simpleWizard">
			<input type="hidden" name="_eventId" value="refresh" />
		
			<g:if test="${study.samples?.size()}">
				<g:each var="templateCombination" in="${templateCombinations}">
					<h2>${templateCombination}</h2>
					<g:set var="showHeader" value="${true}" />
					<g:set var="previousTemplate" value=""/>			
					<div class="tableEditor">
						<% def combinationRecords = records.findAll { it.templateCombination == templateCombination } %>
						<g:each var="record" in="${combinationRecords}">
							<g:if test="${showHeader}">
								<g:set var="showHeader" value="${false}" />
								<div class="header">
									<div class="firstColumn"></div>
									<g:each var="entity" in="${record.objects}">
										<g:if test="${entity.value}">
											<div class="column">${entity.key} template</div>
											<af:templateColumnHeaders entity="${entity.value}" class="column" columnWidths="[Name:100]"/>
										</g:if>
									</g:each>
								</div>
							</g:if>
							<div class="row">
								<div class="firstColumn"></div>
								<g:each var="entity" in="${record.objects}">
									<g:if test="${entity.value}">
										<div class="column">
											<af:templateSelect name="${entity.key.toLowerCase()}_${entity.value.getIdentifier()}_template" entity="${entity.value.class}" value="${entity.value?.template}" addDummy="true" tableEditorChangeEvent="switchTemplate(element);" />
										</div>
										<af:templateColumns name="${entity.key.toLowerCase()}_${entity.value.getIdentifier()}" class="column" id="1" entity="${entity.value}"/>
									</g:if>
								</g:each>
							</div>
						</g:each>
					</div>
					<div class="sliderContainer">
						<div class="slider" ></div>
					</div>
				</g:each>				
	
			</g:if>

		</g:form>
		
		<p class="options">
			<a href="#" onClick="submitForm( 'existingSamples', 'previous' ); return false;" class="previous">Previous</a>
			<a href="#" onClick="submitForm( 'existingSamples', 'next' ); return false;" class="next">Next</a>
			<a href="#" onClick="submitForm( 'existingSamples', 'update' ); return false;" class="excel">Update using excel</a>
			<a href="#" onClick="submitForm( 'existingSamples', 'skip' ); return false;" class="skip">Skip</a>
		</p>
	</div>	
</body>
</html>
