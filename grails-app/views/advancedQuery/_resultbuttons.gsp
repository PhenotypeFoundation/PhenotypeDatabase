<p class="options multiple">
	<g:if test="${search.getNumResults() == 0}">
		<a href="#" onClick="return false;" class="searchIn disabled">Search within results</a>
	</g:if>
	<g:else>
		<g:link class="searchIn" action="searchIn" id="${queryId}">Search within results</g:link>
	</g:else>
	<br />
	
	<g:link class="search" action="index">Search again</g:link><br />
	<g:link class="discard" action="discard" id="${queryId}">Discard results</g:link><br />
	<g:link class="listPrevious" action="list">Previous searches</g:link>
</p>
<p class="options multiple">
	<g:each in="${actions}" var="action">
		<g:if test="${search.getNumResults() == 0}">
			<a href="#" onClick="return false;" class="performAction ${action.name} disabled">${action.description}</a>
		</g:if>
		<g:else>
			<a class="performAction ${action.name}" href="${action.url}" onClick="performAction( $('form#results'), '${action.name}', '${action.module}' ); return false;">${action.description}</a>
		</g:else>
		<br />
	</g:each>
</p>
<br clear="all">