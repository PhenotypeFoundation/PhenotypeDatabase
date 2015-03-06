<!DOCTYPE html>
<html>
    <head>
        <script type="text/javascript">var baseUrl = '${resource(dir: '')}';</script>

        <g:javascript library="jquery" plugin="jquery"/>

        <script type="text/javascript" src="${resource(dir: 'js', file: 'SelectAddMore.js', plugin: 'gdt')}"></script>
        <link rel="stylesheet" href="${resource(dir: 'css', file: 'templateEditor.css', plugin: 'gdt')}"/>

        <title><g:layoutTitle default="Grails" /></title>
        <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'dialog.css')}"/>
        
        <g:layoutHead />
        <r:layoutResources />
    </head>
    <body>
        <g:layoutBody />
        <r:layoutResources />
    </body>
</html>