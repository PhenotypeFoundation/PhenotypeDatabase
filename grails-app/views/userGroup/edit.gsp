<head>
	<meta name='layout' content='main'/>
	<g:set var="entityName" value="${message(code: 'userGroup.label', default: 'UserGroup')}"/>
	<title><g:message code="default.edit.label" args="[entityName]"/></title>
	<r:require modules="tiptip,multiselect" />
   
    <r:script>
        $(document).ready(function() {
            $("#tabs").tabs();
            $('#groupName').focus();
            $("#optionalUsers").multiSelect({ selectableHeader: "Users not in group:", selectionHeader: "User in group:"});
        });
    </r:script>
</head>

<body>
    
<g:if test="${flash.userError}">
	<div id="error" class="error" title="errors">
		<p>
			${flash.userError}
		</p>
	</div>
</g:if>
<h3><g:message code="default.edit.label" args="[entityName]"/></h3>

<g:form action="update" name='userGroupEditForm' class="button-style">
	<g:hiddenField name="id" value="${userGroup?.id}"/>
	<g:hiddenField name="version" value="${userGroup?.version}"/>

	<div id="tabs" class="usermanagement">
		<ul>
			<li><a href="#groupinfo">Group Info</a></li>
                        <li><a href="#users">Users</a></li>
		</ul>

		<div id="groupinfo">

			<table>
				<tbody>
                        <tr><td>Group Name</td><td><g:textField name="groupName" value="${userGroup?.groupName}"/></td></tr>
			<tr><td>Group Description</td><td><g:textArea name="groupDescription" value="${userGroup?.groupDescription}"/></td></tr>
				</tbody>
			</table>
		</div>

                <div id="users">
                        <g:select id="optionalUsers" name="optionalUsers" from="${users}" optionKey="id" value="${selectedUsers?.id}" multiple="multiple" />
		</div>

	</div>

	<div style='float:left; margin-top: 10px;'>
		<input type="submit" value="Save"/>

		<g:if test='${userGroup}'>
			<input type="button" value="Delete" onClick="$('#userGroupDeleteForm').submit();
			return false;"/>
		</g:if>

	</div>

</g:form>

<g:if test='${userGroup}'>
	<g:form action="delete" name='userGroupDeleteForm'>
		<g:hiddenField name="id" value="${userGroup?.id}"/>
	</g:form>
</g:if>

</body>
