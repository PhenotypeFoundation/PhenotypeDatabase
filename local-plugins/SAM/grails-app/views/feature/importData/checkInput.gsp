<html>
    <head>
        <meta name="layout" content="sammain"/>
        <title>Feature importer</title>
        
        <r:require modules="tableEditor"/>
        <r:require module="importer" />
        
        <style type="text/css">
            .helpContent {
                display: none;
            }
            div .tableEditor .column .helpIcon {
                display: none;
            }
            div.spinner {
                height: 16px;
                width: 16px;
                margin: 2px;
                background-image: url(${resource(dir: 'images', file: 'spinner.gif')});
                display: inline-block;
            }
        </style>
        <r:script type="text/javascript" disposition="head">
            function disableButtons(that) {
                $("div.spinner").remove();
                $(that).after("<div class='spinner'></div>");
            }
        </r:script>
    </head>
    <body>
        <content tag="contextmenu">
      		<g:render template="contextmenu" />
        </content>
        <div class="data">

            <imp:importerHeader pages="${pages}" page="checkInput" />

            <p>
                Please check your input. Use the 'Previous' button to make changes when necessary.
            </p>
            <g:if test="${message}">
                <div class="errors">${message}</div><br />
            </g:if>
            <form method="post" novalidate>
                <div class="wizard" id="wizard">
                    <div class="tableEditor">
                        <g:set var="showHeader" value="${true}"/>
                        <g:each status="index" var="entity" in="${featureList}">
                            <g:if test="${showHeader}">
                                <g:set var="showHeader" value="${false}"/>
                                <div class="header">
                                    <div class="firstColumn"></div>
                                    <af:templateColumnHeaders entity="${entity}" class="column" />
                                </div>
                                <input type="hidden" name="entity" value="${entity.class.name}">
                            </g:if>
                            <div class="row">
                                <div class="firstColumn"></div>
                                <af:templateColumns id="${entity.hashCode()}" entity="${entity}" template="${entity.template}" name="entity_${entity.identifier}" class="column" subject="${entity.hashCode()}" addDummy="true" />
                            </div>
                        </g:each>
                    </div>
                </div>

                <br />

                <imp:importerFooter>
                    <g:submitButton id="button_previous" name="previous" value="« Previous" action="previous" onClick="disableButtons(this);"/>
                    <g:submitButton id="button_save" name="save" value="Save" action="save" onClick="disableButtons(this);"/>
                </imp:importerFooter>
            </form>
        </div>
        <r:script>
        	$(function() {
        		// Initialize editable table
        		onStudyWizardPage();
        	});
        	
        </r:script>
        
    </body>
</html>