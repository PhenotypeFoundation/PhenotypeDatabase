






<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'event.label', default: 'Event')}" />
        <title><g:message code="default.create.label" args="[entityName]" /></title>
	<g:render template="../common/jquerysetup"/>
	<g:render template="../common/jqueryuisetup"/>
	<g:render template="../common/jquerytmsetup"/>
    </head>



    <body>
                <h1>A Timepicker for jQuery-UI</h1>
                <p><a href='http://milesich.com/'>Martin Milesich</a> has written an extension of the jQuery-UI <a href='http://jqueryui.com/demos/datepicker/'>Datepicker</a> that includes time picking.</p>
                <p>I'm working with people who have a need for such a control and who thought his would suffice. But I think the sliding controls are non-obvious for users. With five minute intervals satisfactory to the task at hand I redesigned the plugin.</p>

                <p>Test date/time field: <input id="test" /></p>

                <script>
                        $(function() {
                            $('#test').datepicker({
                                duration: '',
                                showTime: true,
                                constrainInput: false
                             });
                        });
                </script>



    </body>

</html>

