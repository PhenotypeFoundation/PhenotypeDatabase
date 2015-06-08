	<div class="basicTabLayout studyEdit subject">
		<g:render template="/common/flashmessages" />
		
		<p>
			Please note that all required fields have to be filled, before the samples can be added. You can enter specific information
			for different samples after adding them
		</p>

		<g:hasErrors bean="${entity}">
			<div class="message errormessage">
				<g:renderErrors bean="${entity}" />
			</div>
		</g:hasErrors>  
		 
		<g:form action="${actionName}" name="sampleDetails">
			<g:hiddenField name="_action" />
			<g:if test="${entity?.parent?.id}">
				<g:hiddenField name="parentId" value="${entity?.parent?.id}" />
			</g:if>

			<div class="element"> 
				<div class="description">Count </div>
				<div class="input"><input type="text" name="count" value="${number}" /></div>
				<div class="helpContent">The number of samples to be added</div>
			</div>
			
			<af:templateElement name="template" description="Template"
				value="${entity?.template}" entity="${dbnp.studycapturing.Sample}"
				addDummy="true" onChange="if(\$( this ).val() != '') { \$( '#sampleDetails' ).submit(); }">
				Choose the type of sample you would like to create. Depending on the chosen template specific fields can be filled out. 
			</af:templateElement>
		
			<g:if test="${entity}">
				<g:if test="${entity.template?.description}">
					<div class="element">
						<div class="templatedescription">
							${entity.template?.description?.encodeAsHTML()}
						</div>
					</div>
				</g:if>
				<af:templateElements entity="${entity}" />
			</g:if>
		
		</g:form>
	
	</div>
