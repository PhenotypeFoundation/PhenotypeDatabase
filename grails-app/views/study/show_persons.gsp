<%
  // Determine a list of all persons
  allPersons = studyList*.persons*.person.flatten().unique()
%>
<g:if test="${allPersons?.size()==0}">
  No persons involved in these studies
</g:if>
<g:else>
  <table>
	<tr>
	  <thead>
		<th>Name</th>
		<th>Affiliations</th>
		<th>Phone</th>
		<th>Email</th>
		<g:if test="${multipleStudies}">
		  <g:each in="${studyList}" var="studyInstance">
			<th>${studyInstance.title}</th>
		  </g:each>
		</g:if>
		<g:else>
		  <th>Role</th>
		</g:else>
	  </thead>
	</tr>
	<g:each in="${allPersons}" var="person" status="i">
	  <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
		<td>${person.firstName} ${person.prefix} ${person.lastName}</td>
		<td>
		  ${person.affiliations.join(', ')}
		</td>
		<td>${person.phone}</td>
		<td>${person.email}</td>
		<g:each in="${studyList}" var="studyInstance">
		  <%
			studyperson = studyInstance.persons.find { it.person == person }
		  %>
		  <td>
			<g:if test="${studyperson}">
			  ${studyperson.role.name}
			</g:if>
		   </td>
		</g:each>

	  </tr>
	</g:each>
  </table>
</g:else>