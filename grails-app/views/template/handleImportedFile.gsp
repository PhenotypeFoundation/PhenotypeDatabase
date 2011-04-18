<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
	<meta name="layout" content="main"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="${resource(dir: 'css', file: 'templateImporter.css')}" />
    <title>Select templates to import</title>
  </head>
  <body>
    <h1>Select templates to import</h1>
	<g:if test="${templates?.size() > 0}">
	  <form method="post" action="<g:createLink action="saveImportedTemplates" />">
		<table id="importTemplates">
		  <thead>
			<tr>
			  <th></th><th>Templates in file</th><th>Fields</th><th>Alternative (equal) templates</th>
			</tr>
		  </thead>
		  <g:each in="${templates}" var="templateData" status="i">
			<tr class="${ i % 2 == 0 ? 'even' : 'odd'}">
			  <g:if test="${!templateData.error}">
				<td><input type="checkbox" name="selectedTemplate" <g:if test="${templateData.alternatives == null || templateData.alternatives.size() == 0}">checked="checked"</g:if> value="${templateData.key}" /></td>
				<td>
				  <h3>${templateData.template.name} <a href="#" onClick="$(this).parent().hide(); $('.otherTemplateName', $(this).parent().parent()).show()">(change)</a></h3>
				  <span class="otherTemplateName"><g:textField name="templateNames_${templateData.key}" value="${templateData.template.name}" /></span>
				  <p>${templateData.template.entity?.getName()}</p>
				  <p>${templateData.template.description}</p>
				</td>
				<td>
				  <g:if test="${templateData.template.fields?.size() > 0}">
				  <ol class="fields">
					<g:each in="${templateData.template.fields}" var="field">
					  <li>${field.name} (<g:if test="${field.unit}">${field.unit}, </g:if>${field.type})</li>
					</g:each>
				  </ol>
				  </g:if>
				  <g:else>
					(no fields)
				  </g:else>
				</td>
				<td>
				  <g:if test="${templateData.alternatives.size() > 0}">
					<ul>
					  <g:each in="${templateData.alternatives}" var="alternative">
						  <li>${alternative.name}</li>
					  </g:each>
					</ul>
				  </g:if>
				  <g:else>
					No alternatives found
				  </g:else>
				</td>
			  </g:if>
			  <g:else>
				<td></td>
				<td class="error">${templateData.error}</td>
				<td colspan="2"></td>
			  </g:else>
			</tr>
		  </g:each>
		</table>
		<input type="submit" />
	  </form>
	</g:if>
	<g:else>
	  No correctly formatted templates found in the XML file. Please use the export functionality to create XML files.
	</g:else>
  </body>
</html>
