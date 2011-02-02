<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Query database</title>
	<link rel="stylesheet" href="<g:resource dir="css" file="advancedQuery.css" />" type="text/css"/>
	<g:javascript src="advancedQuery.js" />
	<script type="text/javascript">
		// Make a list of fields to search in
		var queryableFields = [
			<g:set var="j" value="${0}" />
			<g:each in="${searchableFields}" var="entity">
				<g:each in="${entity.value}" var="field">
					<g:if test="${j > 0}">,</g:if>
					{
						value: "${entity.key.toString().encodeAsJavaScript()}.${field.toString().encodeAsJavaScript()}",
						show: "${(field[0].toUpperCase() + field[1..-1]).encodeAsJavaScript()}",
						label: "${entity.key.toString().encodeAsJavaScript()}.${field.toString().encodeAsJavaScript()}",
						entity: "${entity.key.toString().encodeAsJavaScript()}"
					}
					<g:set var="j" value="1" />
				</g:each>
			</g:each>
		];
	
		<g:if test="${criteria && criteria.size() > 0}">
			// Show given criteria
			$(function() {
				<g:each in="${criteria}" var="criterion">
					showCriterium("${criterion.entityField().encodeAsJavaScript()}", "${criterion.value.toString().encodeAsJavaScript()}", "${criterion.operator.toString().encodeAsJavaScript()}");
				</g:each>
				showHideNoCriteriaItem();
			});
		</g:if>
	</script>
</head>
<body>

<h1>Query database</h1>

<g:if test="${flash.error}">
	<div class="error">
		${flash.error.toString().encodeAsHTML()}
	</div>
</g:if>
<g:if test="${flash.message}">
	<div class="message">
		${flash.message.toString().encodeAsHTML()}
	</div>
</g:if>

<a href="<g:createLink action="list" />">View previous queries</a>

<form id="input_criteria">
	<h2>Add criterium</h2>
	<p class="explanation">
		N.B. Comparing numerical values is done without taking into
		account the units. E.g. a weight of 1 kg equals 1 grams.
	</p>
	<label for="field">Field</label>
		<select name="field" id="queryFieldSelect">
			<option value=""></option>
			<g:each in="${searchableFields}" var="entity">
				<optgroup label="${entity.key}">
					<g:each in="${entity.value}" var="field">
						<option value="${entity.key}.${field}">
							${field[0].toUpperCase() + field[1..-1]}
						</option>
					</g:each>
				</optgroup>
			</g:each>
		</select>
		
	<label for="value">Comparison</label>
		<select name="operator">
			<option value="equals">Equals</option>
			<option value="contains">Contains</option>
			<option value="&gt;=">Greater than or equals</option>
			<option value="&gt;">Greater than</option>
			<option value="&lt;">Lower than</option>
			<option value="&lt;=">Lower than or equals</option>
		</select> 

	<label for="value">Value</label>
		<input class='text' type="text" name="value" />
	
	<input class="button" type="button" onClick="addCriterium();" value="Add" />
</form>

<div id="searchForm">
	<g:form action="search" method="get">
		<label for="entity">Search for</label><g:select from="${entitiesToSearchFor}" optionKey="key" optionValue="value" name="entity" /><br />
		<label for="criteria">Criteria</label>
		<ul id="criteria">
			<li class="emptyList">No criteria added. Use the form on the right to specify criteria to search on.</li>
		</ul>
		
		<input type="submit" value="Run query" class="submitcriteria" disabled="disabled" />
	</g:form>
</div>

<br  clear="all" />
</body>
</html>
