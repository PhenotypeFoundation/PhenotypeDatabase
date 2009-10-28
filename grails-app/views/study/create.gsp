

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create Study</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">Study List</g:link></span>
        </div>
        <div class="body">
            <h1>Create Study</h1>
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
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="code">Code:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:studyInstance,field:'code','errors')}">
                                    <input type="text" id="code" name="code" value="${fieldValue(bean:studyInstance,field:'code')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="created">Created:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:studyInstance,field:'created','errors')}">
                                    <g:datePicker name="created" value="${studyInstance?.created}" precision="minute" ></g:datePicker>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Description:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:studyInstance,field:'description','errors')}">
                                    <input type="text" id="description" name="description" value="${fieldValue(bean:studyInstance,field:'description')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="ecCode">Ec Code:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:studyInstance,field:'ecCode','errors')}">
                                    <input type="text" id="ecCode" name="ecCode" value="${fieldValue(bean:studyInstance,field:'ecCode')}" />
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="modified">Modified:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:studyInstance,field:'modified','errors')}">
                                    <g:datePicker name="modified" value="${studyInstance?.modified}" precision="minute" ></g:datePicker>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="researchQuestion">Research Question:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:studyInstance,field:'researchQuestion','errors')}">
                                    <input type="text" id="researchQuestion" name="researchQuestion" value="${fieldValue(bean:studyInstance,field:'researchQuestion')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="startDate">Start Date:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:studyInstance,field:'startDate','errors')}">
                                    <g:datePicker name="startDate" value="${studyInstance?.startDate}" precision="minute" ></g:datePicker>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="title">Title:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:studyInstance,field:'title','errors')}">
                                    <input type="text" id="title" name="title" value="${fieldValue(bean:studyInstance,field:'title')}"/>
                                </td>
                            </tr> 
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><input class="save" type="submit" value="Create" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
