<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="main"/>
	<title>Query database</title>
	<link rel="stylesheet" href="<g:resource dir="css" file="advancedQuery.css" />" type="text/css"/>
	<g:javascript src="advancedQuery.js" />
</head>
<body>

<h1>Query database</h1>

<form id="input_criteria">
	<h2>Add criterium</h2>
	<label for="field">Field</label>
		<select name="field">
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
