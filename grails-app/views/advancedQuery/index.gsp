<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Query database</title>
	<link rel="stylesheet" href="<g:resource dir="css" file="advancedQuery.css" />" type="text/css"/>
	<link rel="stylesheet" href="<g:resource dir="css" file="buttons.css" />" type="text/css"/>
	<g:javascript src="advancedQuery.js" />
	<script type="text/javascript">
		// Make a list of fields to search in
		var queryableFields = [
			<g:set var="j" value="${0}" />
			<g:each in="${searchableFields}" var="entity">
				<g:each in="${entity.value}" var="field">
					<g:if test="${j > 0}">,</g:if>
					{
						label: "${entity.key.toString().encodeAsJavaScript()}.${field.toString().encodeAsJavaScript()} ${entity.key.toString().encodeAsJavaScript()} ${field.toString().encodeAsJavaScript()}",
						show: "${(field[0].toUpperCase() + field[1..-1]).encodeAsJavaScript()}",
						value: "${entity.key.toString().encodeAsJavaScript()}.${field.toString().encodeAsJavaScript()}",
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
			});
		</g:if>
	</script>
</head>
<body>

<h1>Search database</h1>

<g:if test="${flash.error}">
	<div class="errormessage">
		${flash.error.toString().encodeAsHTML()}
	</div>
</g:if>
<g:if test="${flash.message}">
	<div class="message">
		${flash.message.toString().encodeAsHTML()}
	</div>
</g:if>

<div id="searchForm">
	<g:form action="search" method="get">

		<h3><span class="nummer">1</span>Select criteria</h3>
		<p class="explanation">
			N.B. Comparing numerical values is done without taking into
			account the units. E.g. a weight of 1 kg equals 1 grams.
		</p>
		<ul id="criteria">
			<li class="titlerow">
				<span class="entityfield">
					Field
				</span>
				<span class="operator">
					Operator
				</span>
				<span class="value">
					Value
				</span>
			</li>
			<li class="newCriterion">
				<span class="entityfield">
					<select name="criteria.0.entityfield" id="queryFieldSelect">
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
				</span>
				<span class="operator">
					<select id="operator" name="criteria.0.operator">
						<option value="equals">Equals</option>
						<option value="contains">Contains</option>
						<option value="&gt;=">&gt;=</option>
						<option value="&gt;">&gt;</option>
						<option value="&lt;">&lt;</option>
						<option value="&lt;=">&lt;=</option>
					</select>
				</span>
				<span class="value">
					<input class='text' type="text" id="value" name="criteria.0.value" />
				</span>
				<span class="addButton">
					<a href="#" onClick="addCriterion(); return false;">
						<img src="${fam.icon( name: 'add' )}" border="0">
					</a>
				</span>
			</li>
		</ul>
		
		<div id="searchMode">
			<h3><span class="nummer">1b</span>Search mode</h3>
			<p>
				Choose how to combine the given criteria:<br />
				<g:select from="${searchModes}" name="operator" />
			</p>
		</div>
		
		<h3><span class="nummer">2</span>Output type</h3>
		<p>
			Choose the type of output:<br />
			<g:select from="${entitiesToSearchFor}" optionKey="key" optionValue="value" name="entity" />
		</p>

		<h3><span class="nummer">3</span>Run query</h3>
		<p>
			<input type="submit" value="Run query" class="submitcriteria" />
		</p>
	</g:form>
	
	<br clear="all" />
</div>
<p class="options">
	<g:link class="listPrevious" action="list">Previous searches</g:link>
</p>
<br  clear="all" />
</body>
</html>
