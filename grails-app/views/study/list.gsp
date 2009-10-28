

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Study List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create">New Study</g:link></span>
        </div>
        <div class="body">
            <h1>Study List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                        
                   	        <g:sortableColumn property="code" title="Code" />
                        
                   	        <g:sortableColumn property="created" title="Created" />
                        
                   	        <g:sortableColumn property="description" title="Description" />
                        
                   	        <g:sortableColumn property="ecCode" title="Ec Code" />
                        
                   	        <g:sortableColumn property="modified" title="Modified" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${studyInstanceList}" status="i" var="studyInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${studyInstance.id}">${fieldValue(bean:studyInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:studyInstance, field:'code')}</td>
                        
                            <td>${fieldValue(bean:studyInstance, field:'created')}</td>
                        
                            <td>${fieldValue(bean:studyInstance, field:'description')}</td>
                        
                            <td>${fieldValue(bean:studyInstance, field:'ecCode')}</td>
                        
                            <td>${fieldValue(bean:studyInstance, field:'modified')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${studyInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
