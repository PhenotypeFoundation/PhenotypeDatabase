<p class="options">
	<g:link class="searchIn" action="searchIn" id="${queryId}">Search within results</g:link><br />
	<g:link class="search" action="index">Search again</g:link><br />
	<g:link class="discard" action="discard" id="${queryId}">Discard results</g:link><br />
	<g:link class="listPrevious" action="list">Previous searches</g:link>
</p>
<p class="options">
	<g:each in="${actions}" var="action">
		<a class="performAction ${action.name}" href="${action.url}" onClick="performAction( $('form#results'), '${action.name}', '${action.module}' ); return false;">${action.description}</a><br />
	</g:each>
</p>
<br clear="all">