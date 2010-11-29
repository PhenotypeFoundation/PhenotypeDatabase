<%
  // Determine a list of all persons
  allPublications = studyList*.publications.flatten().unique()
%>
<g:if test="${allPublications?.size()==0}">
  No publications attached to these studies
</g:if>
<g:else>
  <table>
	<tr>
	  <thead>
		<th>Title</th>
		<th>Authors</th>
		<th>Comments</th>

		<g:if test="${multipleStudies}">
		  <g:each in="${studyList}" var="studyInstance">
			<th>${studyInstance.title}</th>
		  </g:each>
		</g:if>
	  </thead>
	</tr>
	<g:each in="${allPublications}" var="publication" status="i">
	  <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
		<td>${publication.title}</td>
		<td>
		  ${publication.authorsList}
		</td>
		<td>${publication.comments}</td>
		<g:if test="${multipleStudies}">
		  <g:each in="${studyList}" var="studyInstance">
			<td>
			  <g:if test="${publication in studyInstance.publications}">
				x
			  </g:if>
			</td>
		  </g:each>
		</g:if>
	  </tr>
	</g:each>
  </table>
</g:else>