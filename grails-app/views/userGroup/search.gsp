<head>
	<meta name='layout' content='main'/>
	<title><g:message code='spring.security.ui.group.search'/></title>
</head>

<body>

<div>

	<g:form action='userGroupSearch' name='userGroupSearchForm'>

		<g:if test="${flash.message}">
		  <p>
			${flash.message}
		  </p>
		</g:if>
		<br/>

		<table>
			<tbody>

			<tr>
				<td><g:message code='usergroup.groupname.label' default='Groupname'/>:</td>
				<td><g:textField name='groupName' size='50' maxlength='255' autocomplete='off' value='${groupName}'/></td>
			</tr>
			<tr>
				<td colspan='2'><input type="submit" value="Search" /></td>
			</tr>
			</tbody>
		</table>
	</g:form>

	<g:if test='${searched}'>

	<%
	def queryParams = [group_name: group_name]
	%>

	<div class="list">
	<table>
		<thead>
		<tr>
			<g:sortableColumn property="groupName" title="${message(code: 'usergroup.groupname.label', default: 'GroupName')}" params="${queryParams}" />
			<g:sortableColumn property="groupDescription" title="${message(code: 'usergroup.groupdescription.label', default: 'GroupDescription')}" params="${queryParams}"/>
			
		</tr>
		</thead>

		<tbody>
		<g:each in="${results}" status="i" var="usergroup">
		<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        <td><g:link action="edit" id="${usergroup.id}">${fieldValue(bean: usergroup, field: "groupName")}</g:link></td>
			<td>${fieldValue(bean: usergroup, field: "groupDescription")}</td>

		</tr>
		</g:each>
		</tbody>
	</table>
	</div>

	<div class="paginateButtons">
		<g:paginate total="${totalCount}" params="${queryParams}" />
	</div>

	</g:if>

</div>

<script>
$(document).ready(function() {
	$("#groupname").focus().autocomplete({
		minLength: 3,
		cache: false,
		source: "${createLink(action: 'ajaxUserSearch')}"
	});
});

</script>

</body>
