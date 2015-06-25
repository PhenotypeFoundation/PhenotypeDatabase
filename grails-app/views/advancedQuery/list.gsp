<%@ page import="dbnp.query.*" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Previous queries</title>
	<r:require module="advancedQuery" />
	<script type="text/javascript">
		function searchWithinResults( form ) {
			submitForm( form, '/advancedQuery/searchIn' );
		}
		function discardResults( form ) {
			submitForm( form, '/advancedQuery/discard' );
		}	
		function combineResults( form ) {
			submitForm( form, '/advancedQuery/combine' );
		}				
	</script>
	
</head>
<body>

<h1>Previous queries</h1>

<g:render template="/common/flashmessages" />

<g:if test="${searches.size() > 0}">
	<form id="searchform" method="post">
	<table id="searchresults" class="paginate">
		<thead>
			<tr>
				<th class="nonsortable"><input type="checkbox" id="checkAll" onClick="checkAllPaginated(this);" /></th>
				<th>#</th>
				<th>Type</th>
				<th>Criteria</th>
				<th># results</th>
				<th>time</th>
				<th class="nonsortable"></th>
				<th class="nonsortable"></th>
			</tr>
		</thead>
		<g:each in="${searches}" var="search">
			<tr>
				<td><g:checkBox name="id" value="${search.id}" checked="${false}" onClick="updateCheckAll(this);" /></td>
				<td>${search.id}</td>
				<td>${search.entity}</td>
				<td>
					<g:set var="criteria" value="${search.getCriteria()}" />
					<g:each in="${criteria}" var="criterion" status="j">
						<span class="entityfield">${criterion.entityField()}</span>
						<span class="operator">${criterion.operator}</span>
						<span class="value">
							<g:if test="${criterion.value instanceof Search}">
								<a href="${criterion.value.url}">${criterion.value?.toString()}</a>
							</g:if>
							<g:else>
								${criterion.value}
							</g:else>
						</span>
						<g:if test="${j < criteria.size() -1}">
							<g:if test="${search.searchMode == SearchMode.and}">and</g:if>
							<g:if test="${search.searchMode == SearchMode.or}">or</g:if>
						</g:if>						
					</g:each>
				</td>
				<td>${search.getNumResults()}</td>
				<td><g:formatDate date="${search.executionDate}" format="HH:mm" /></td>
				<td><a href="${search.url.encodeAsHTML()}"><img border="0" src="${fam.icon(name: 'application_form_magnify')}" alt="Show" /></a></td>
				<td><g:link action="discard" id="${search.id}"><img border="0" src="${fam.icon(name: 'basket_remove')}" alt="Discard" /></g:link></td>
			</tr>
		</g:each>
	</table>
	</form>	
</g:if>

<p class="options">
	<a href="#" class="combine" onClick="combineResults( $( '#searchform' ) ); return false;">Combine results</a>
	<a href="#" class="searchIn" onClick="searchWithinResults( $( '#searchform' ) ); return false;">Search within results</a>
	<g:link class="search" action="index">Search again</g:link>
	<a href="#" class="discard" onClick="discardResults( $( '#searchform' ) ); return false;">Discard results</a>
</p>
<br clear="all" />
</body>
</html>
