
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <title>Pilot</title>
</head>
<body>

	<h1>NMC Pilot</h1>
	<p>Thank you for participating in the 2010 Pilot of the NMC-DSP/GSCF application</p>
	<br />
	<table>
		<tr>		
	<g:if test="${studyInstanceList}">
		<td width="50%">
			<h3>Continue with an existing Pilot Study</h3>
			<g:each in="${studyInstanceList}" var="study">
				- <g:link controller="pilot" action="show" id="${study.id}">${study.title}</g:link><br />
			</g:each>
		</td>
	</g:if>
	<td width="50%">
		<h3>Start new Pilot here</h3>
    	<g:if test="${flash.message}">
    		<div class="message">${flash.message}</div>
    	</g:if>
      
      	<g:hasErrors bean="${studyInstance}">
      		<div class="errors">
        		<g:renderErrors bean="${studyInstance}" as="list" />
      		</div>
      	</g:hasErrors>
      	
      	<g:form action="save" method="post" >
          <div class="dialog">
          	<p>Start by creating a study. For this pilot a title and start date are sufficient. When finished click on the "Create/Invoeren" button.</p>
              <table>
                  <tbody>

                      <tr class="prop">
                          <td valign="top" class="name">
                              <label for="title"><g:message code="study.title.label" default="Title" /></label>
                          </td>
                          <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'title', 'errors')}">
                              <g:textField name="title" value="${studyInstance?.title}" />
                          </td>
                      </tr>
                      
                      <tr class="prop">
                          <td valign="top" class="name">
                              <label for="startDate"><g:message code="study.startDate.label" default="Start Date" /></label>
                          </td>
                          <td valign="top" class="value ${hasErrors(bean: studyInstance, field: 'startDate', 'errors')}">
                              <g:datePicker name="startDate" precision="day" value="${studyInstance?.startDate}"  />
                          </td>
                      </tr>                           
                      
                  </tbody>
              </table>
          </div>
          <div class="buttons">
             <g:each in="${extraparams}" var="param">
               <input type="hidden" name="${param.key}" value="${param.value}">
             </g:each>                
             <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
             <span class="button"><g:link class="cancel" action="list" params="${extraparams}">Cancel</g:link></span>
          </div>
      </g:form>
    </td>
  </tr>
</table>
<br /><br />
</body>
</html>
