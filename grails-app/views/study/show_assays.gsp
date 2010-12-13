<g:if test="${studyList*.assays?.flatten()?.size()==0}">
  No assays in these studies
</g:if>
<g:else>
  <table>
	<thead>
	  <tr>
		<g:if test="${multipleStudies}">
		  <th></th>
		</g:if>
		  <th width="100">Assay Code</th>
		<th width="100">Assay Name</th>
		<th width="100">Module</th>
		<th width="150">Platform</th>
		<th>Link</th>
		<th>Samples</th>
	  </tr>
	</thead>
	<g:set var="i" value="${1}" />

	<g:each in="${studyList}" var="studyInstance">
	  <g:each in="${studyInstance.assays}" var="assay" status="j">
		<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
		  <g:if test="${multipleStudies && j==0}">
			<td class="studytitle" rowspan="${studyInstance.assays?.size()}">
			  ${studyInstance.title}
			</td>
		  </g:if>
			<td>${assay.token}</td>
		  <td>${assay.name}</td>
		  <td>${assay.module.name}</td>
		  <td>${assay.module.platform}</td>
		  <td>
          <jumpbar:link frameSource="${assay.module.url}/assay/showByToken/${assay.externalAssayID}" pageTitle="Metabolomics Module">
			view
		  </jumpbar:link></td>
		  <td>
			<% sortedAssaySamples = assay.samples.sort( { a, b -> a.name <=> b.name } as Comparator )  %>
			${sortedAssaySamples.name.join( ', ' )}
		  </td>
		</tr>
		<g:set var="i" value="${i + 1}" />

	  </g:each>
	</g:each>
  </table>
</g:else>