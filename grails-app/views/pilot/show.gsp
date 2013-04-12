
<%@ page import="dbnp.studycapturing.Study" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="${layout}" />        
        <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
            	<p>
            		<b>Instructions</b>: You are now on the Study page. 
            		All information within the application is related to a study.
            		For this pilot a Metabolomics Assay has been created.<br /><br />
            		
					<g:if test="${studyInstance.samples.size() == 0}">
                    	Currently there are no samples registered for this study. Please use the <b>"Import Samples"</b> to add samples to this study.                    	
					</g:if>

					<g:if test="${studyInstance.samples.size() > 0}">
            			Click on this assay to continue with the pilot.
					</g:if>
            	</p>
            	
                <table>
                    <tbody>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="study.title.label" default="Title" /></td>

                            <td valign="top" class="value">${fieldValue(bean: studyInstance, field: "title")} (${fieldValue(bean: studyInstance, field: "code")})</td>

                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">Samples (${studyInstance.samples.size()})</td>

                            <td valign="top" class="value">
                            	
                            	<g:if test="${studyInstance.samples.size() == 0}">
									<g:link controller="importer" action="index" params="[redirectTo: 'http://localhost:8080/gscf/pilot/show/'+studyInstance.id]">Import Samples</g:link>
                            	</g:if>
                            	
                            	<g:if test="${studyInstance.samples.size() > 0}">
									${studyInstance.samples.join( ', ' )}                            		
                            	</g:if>                            	
                            </td>

                        </tr>
                        
                        <g:if test="${studyInstance.samples.size() > 0}">                        
	                        <tr class="prop">
	                            <td valign="top" class="name">Assays</td>
	
	                            <td valign="top" class="value">
									<g:each in="${studyInstance.assays.unique()}" var="assay">
										<jumpbar:link
                      						linkDest="${createLink(action:'show', id:studyInstance.id)}"
                      						linkText='Go back to GSCF'
                      						frameSource="${assay.module.baseUrl}/assay/showByToken?id=${assay.giveUUID()}&sessionToken=${session.id}"
                      						pageTitle="Assay View in Module">
                      						${assay.name}
                    					</jumpbar:link><br />
									</g:each>                            
	                            </td>	
	                        </tr> 
                    	</g:if>
                    	
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${studyInstance?.id}" />
                     <g:each in="${extraparams}" var="param">
                       <input type="hidden" name="${param.key}" value="${param.value}">
                     </g:each>
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                    <span class="button"><g:link class="backToList" action="list" params="${extraparams}">Back to list</g:link></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
