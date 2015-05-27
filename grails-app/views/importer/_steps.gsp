<span class="steps">
	<g:if test="${active == 'chooseType'}">
		<span class="active">Choose type</span>
	</g:if>
	<g:else>
		<span class="disabled">Choose type</span>
	</g:else>
	/
	<g:if test="${active == 'uploadFile'}">
		<span class="active">Upload file</span>
	</g:if>
	<g:else>
		<span class="disabled">Upload file</span>
	</g:else>
	/
	<g:if test="${active == 'matchDdata'}">
		<span class="active">Match data</span>
	</g:if>
	<g:else>
		<span class="disabled">Match data</span>
	</g:else>
	/
	<g:if test="${active == 'validation'}">
		<span class="active">Validation</span>
	</g:if>
	<g:else>
		<span class="disabled">Validation</span>
	</g:else>
	/
	<g:if test="${active == 'store'}">
		<span class="active">Store data</span>
	</g:if>
	<g:else>
		<span class="disabled">Store data</span>
	</g:else>
</span>			
