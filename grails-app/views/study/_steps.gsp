<span class="steps">
	<g:if test="${active == 'study'}">
		<span class="active">study</span>
	</g:if>
	<g:else>
		<g:link controller="study" action="show" id="${study?.id}">study</g:link>
	</g:else>
	/
	<g:if test="${active == 'subjects'}">
		<span class="active">subjects</span>
	</g:if>
	<g:else>
		<g:link class="${study?.getSubjectCount() ? '' :'disabled' }" controller="study" action="subjects" id="${study?.id}">subjects</g:link>
	</g:else>
	/
	<g:if test="${active == 'design'}">
		<span class="active">design</span>
	</g:if>
	<g:else>
		<g:link class="${study?.getTotalEventCount() ? '' :'disabled' }" controller="study" action="design" id="${study?.id}">design</g:link>
	</g:else>
	/
	<g:if test="${active == 'samples'}">
		<span class="active">samples</span>
	</g:if>
	<g:else>
		<g:link class="${study?.getSampleCount() ? '' :'disabled' }" controller="study" action="samples" id="${study?.id}">samples</g:link>
	</g:else>	
	/
	<g:if test="${active == 'assays'}">
		<span class="active">assays</span>
	</g:if>
	<g:else>
		<g:link class="${study?.getAssayCount() ? '' :'disabled' }" controller="study" action="assays" id="${study?.id}">assays</g:link>
	</g:else>
</span>			
