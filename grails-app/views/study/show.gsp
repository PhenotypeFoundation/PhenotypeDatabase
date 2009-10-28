

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show Study</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">Study List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Study</g:link></span>
        </div>
        <div class="body">
            <h1>Show Study</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name">Id:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:studyInstance, field:'id')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Code:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:studyInstance, field:'code')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Created:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:studyInstance, field:'created')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Description:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:studyInstance, field:'description')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Ec Code:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:studyInstance, field:'ecCode')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Modified:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:studyInstance, field:'modified')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Research Question:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:studyInstance, field:'researchQuestion')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Start Date:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:studyInstance, field:'startDate')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Title:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:studyInstance, field:'title')}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${studyInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
