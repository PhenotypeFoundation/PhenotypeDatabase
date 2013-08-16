<span class="steps">
	<g:if test="${active == 'study'}">
		<span class="active">study</span>
	</g:if>
	<g:else>
		<g:if test="${study?.id}">
			<g:link action="properties" id="${study?.id}">study</g:link>
		</g:if>
		<g:else>
			<span class="disabled">study</span>
		</g:else>
	</g:else>
	/
	<g:if test="${active == 'subjects'}">
		<span class="active">subjects</span>
	</g:if>
	<g:else>
		<g:if test="${study?.id}">
			<g:link action="subjects" id="${study?.id}">subjects</g:link>
		</g:if>
		<g:else>
			<span class="disabled">subjects</span>
		</g:else>
	</g:else>
	/
	<g:if test="${active == 'design'}">
		<span class="active">design</span>
	</g:if>
	<g:else>
		<g:if test="${study?.id && study?.subjects.size()}">
			<g:link action="design" id="${study.id}">design</g:link>
		</g:if>
		<g:else>
			<span class="disabled">design</span>
		</g:else>
	</g:else>
	/
	<g:if test="${active == 'samples'}">
		<span class="active">samples</span>
	</g:if>
	<g:else>
		<g:if test="${study?.id && study?.subjects.size()}">
			<g:link action="samples" id="${study.id}">samples</g:link>
		</g:if>
		<g:else>
			<span class="disabled">samples</span>
		</g:else>
	</g:else>	
	/
	<g:if test="${active == 'assays'}">
		<span class="active">assays</span>
	</g:if>
	<g:else>
		<g:if test="${study?.id && study?.samples.size()}">
			<g:link action="assays" id="${study.id}">assays</g:link>
		</g:if>
		<g:else>
			<span class="disabled">assays</span>
		</g:else>
	</g:else>
</span>			
