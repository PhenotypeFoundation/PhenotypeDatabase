<%@ page import="dbnp.studycapturing.Sample" %>
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
		<th width="100">Assay Name</th>
		<th width="100">Module</th>
		<th>Link</th>
		<th>Samples</th>
        <th>Files</th>
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
		  <td>${assay?.name}</td>
		  <td>${assay?.module?.name}</td>
		  <td>
		  	<g:if test="${assay?.module.openInFrame == null || assay?.module.openInFrame == Boolean.TRUE}">
                <jumpbar:link frameSource="${assay?.module.baseUrl}/assay/showByToken/${assay?.UUID}" pageTitle="${assay?.module.name}">
				details
			  </jumpbar:link>
			 </g:if>
			 <g:else>
			 	<g:link url="${assay?.module.baseUrl}/assay/showByToken/${assay?.UUID}">details</g:link>
			 </g:else>
		</td>
		  <td>
			<% sortedAssaySamples = assay?.samples.sort( { a, b -> a.name <=> b.name } as Comparator )  %>
			${Sample.trimSampleNames( sortedAssaySamples, 400 )}
		  </td>
          <td>
              <g:set var="n" value="${1}" />
              <g:each in="${assay?.templateFileFields}" var="file">
                  <g:link url="${grailsApplication.config.gscf.baseURL+"file/get/"+file.value}">${"File " + n}</g:link>
                  <g:set var="n" value="${i + 1}" />
              </g:each>
          </td>
		</tr>
		<g:set var="i" value="${i + 1}" />

	  </g:each>
	</g:each>
  </table>
</g:else>