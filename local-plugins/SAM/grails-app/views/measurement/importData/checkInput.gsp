<html>
    <head>
        <meta name="layout" content="sammain"/>
        <title>Measurement importer</title>

        <style type="text/css">
            div.spinner {
                height: 16px;
                width: 16px;
                margin: 2px;
                background-image: url(${resource(dir: 'images', file: 'spinner.gif')});
                display: inline-block;
            }
        </style>
        <r:script type="text/javascript" disposition="head">
            $(document).ready(function() {
                $("#dialog").dialog({
                    width: 'auto',
                    autoOpen: false,
                    modal: true
                });
            });

            function newInputForm(obj, key){
                var op = $('input[name="operatorHidden'+key+'"]').val();
                var val = $('input[name="valueHidden'+key+'"]').val();
                var co = $('input[name="commentHidden'+key+'"]').val();
                $("#dialog").html("<div id='inputFields"+key+"'><p>Enter the measurement value (a number) here.<br><input name='valueInput"+key+"' type='text' style='width: 300px;' value='"+val+"'/></p><p>Enter the operator ('<' or '>') here.<br><input name='operatorInput"+key+"' type='text' value='"+op+"'/></p><p>Enter any comments here.<br><input name='commentInput"+key+"' type='text' style='width: 300px;' value='"+co+"'/></p><br><b><a href='#' onclick='newObjContent(["+key+"]); return false;'>Confirm these changes</a></b></div>");
                $("#dialog").dialog('open');
            }

            function newObjContent(key){
                busy = false;
                var objA = $('input[name="operatorInput'+key+'"]');
                var objB = $('input[name="valueInput'+key+'"]');
                var objC = $('input[name="commentInput'+key+'"]');
                var valA = jQuery.trim(objA.val());
                var valB = jQuery.trim(objB.val());
                var valC = jQuery.trim(objC.val());

                var blnRet = false;

                if(valA=="" || valA==">" || valA=="<") {
                    objA.css("background-color","");
                } else {
                    objA.css("background-color","pink");
                    blnRet = true;
                }

                var objRegExp  =  /^[0-9]*([,|.][0-9]+)?$/;

                if(valB=="" || objRegExp.test(valB)) {
                    objB.css("background-color","");
                } else {
                    objB.css("background-color","pink");
                    blnRet = true;
                }

                if(blnRet) {
                    return false;
                }

                var obj = $('td[id="'+key+'"]');
                obj.removeClass("importerOperatorCell importerCommentCell")
                var content = ""+valA+""+valB+" "+valC
                if(content.length>25){
                    obj.html('<div class="tooltip importerInteractiveCell">'+content.substring(0,19)+' &hellip;<span>'+content+'</span></div>');
                } else {
                    obj.html(content);
                }
                if(valC!=null && valC!=""){
                    obj.addClass("importerCommentCell")
                } else {
                    if(valA!=null && valA!=""){
                        obj.addClass("importerOperatorCell")
                    }
                }
                $('input[name="operatorHidden'+key+'"]').val(valA);
                $('input[name="valueHidden'+key+'"]').val(valB);
                $('input[name="commentHidden'+key+'"]').val(valC);
                $("#dialog").html("");
                $("#dialog").dialog('close');
            }

            function disableButtons(that) {
                $("div.spinner").remove();
                $(that).after("<div class='spinner'></div>");
            }
        </r:script>
        
        <r:require module="importer" />
        
    </head>
    <body>
        <content tag="contextmenu">
            <g:render template="contextmenu" />
        </content>
        <div class="data">

            <imp:importerHeader pages="${pages}" page="checkInput" />

            <p>
                Please check your input. Use the 'Previous' button to make changes when necessary. A cell with a white background contains only a number. <span class="importerOperatorCell">A cell with this color background</span> contains an operator, and <span class="importerCommentCell">a cell with this color background</span> contains at least a comment. To change the contents of special cells, such as those containing features, please go to the previous page. Clicking on a regular cell allows you to edit it's contents.
            </p>
            <form method="post">
                <%
                    def discard_i = []
                    def discard_j = []
                %>
                <table style="width: auto;">
                    <g:each in="${edited_text}" var="row" status="i">
                        <tr>
                            <g:each in="${row}" var="column" status="j">
                                <g:if test="${!(i==0&j==0) && !(layout=='subject_layout'&& (i==1&j==0))}">
                                    <% // The if-statement excludes cell A1, and also cell A2 when we are using subject layout, because of the fact that cell A2 is not interpreted in subject layout
                                        def op = operator?.get(i+','+j)
                                        def co = comments?.get(i+','+j)
                                        def timepoint = false
                                        if(layout == 'subject_layout'){
                                            if(i==1){
                                                timepoint = true
                                            }
                                        }
                                    %>
                                    <g:if test="${i==0 || j==0 || timepoint}">
                                        <g:if test="${column==null}">
                                            <td style="border: 1px solid lightgray;" class="importerDiscarded">Discarded</td>
                                            <%
                                                if(i==0 || timepoint){
                                                    discard_j.add(j)
                                                }
                                                if(j==0){
                                                    discard_i.add(i)
                                                }
                                            %>
                                        </g:if>
                                        <g:else>
                                            <td style="border: 1px solid lightgray;">
                                                <g:if test="${column.class!=java.lang.String}">
                                                    <g:if test="${column.class==org.dbxp.sam.Feature}">
                                                        <div class="tooltip">
                                                            ${column.name}
                                                            <span>
																<g:render template="featureExample" model="['featureInstance': column]" />
                                                            </span>
                                                        </div>
                                                    </g:if>
                                                    <g:else>
                                                        ${column.name}
                                                    </g:else>
                                                </g:if>
                                                <g:else>
                                                    ${column}
                                                </g:else>
                                            </td>
                                        </g:else>
                                    </g:if>
                                    <g:else>
                                        <g:hiddenField name="operatorHidden${i},${j}" value="${op}"/>
                                        <g:hiddenField name="commentHidden${i},${j}" value="${co}"/>
                                        <g:hiddenField name="valueHidden${i},${j}" value="${column}"/>
                                        <g:if test="${discard_i.contains(i) || discard_j.contains(j) || ignore.contains([i+','+j])}">
                                            <td id="${i},${j}" style="border: 1px solid lightgray;" class="importerDiscarded">
                                        </g:if>
                                        <g:else>
                                            <td id="${i},${j}" onclick="newInputForm($(this), '${i},${j}');" style="border: 1px solid lightgray;"

                                            <g:if test="${op!=null && co==null}">
                                                class="importerInteractiveCell importerOperatorCell"
                                            </g:if>
                                            <g:if test="${co!=null}">
                                                class="importerInteractiveCell importerCommentCell"
                                            </g:if>
                                            >
                                        </g:else>
                                        <%
                                            def content = ""
                                            if(op!=null){
                                                content += op
                                            }
                                            if(column!=null){
                                                content += column
                                            }
                                            if(co!=null){
                                                content += " "+co
                                            }
                                            content = content.trim()
                                        %>
                                        <g:if test="${content.length()>25}">
                                            <div class="tooltip">
                                                ${content.substring(0,19)} &hellip;
                                                <span>
                                                    ${content}
                                                </span>
                                            </div>
                                        </g:if>
                                        <g:else>
                                            ${content}
                                        </g:else>
                                        </td>
                                    </g:else>
                                </g:if>
                                <g:else>
                                    <td></td>
                                </g:else>
                            </g:each>
                        </tr>
                    </g:each>
                </table>
                
                <imp:importerFooter>
                    <g:submitButton id="button_previous" name="previous" value="« Previous" action="previous" onClick="disableButtons(this)"/>
                    <g:submitButton id="button_save" name="save" value="Save" action="save" onClick="disableButtons(this)"/>
                </imp:importerFooter>
            </form>
        </div>
        <div id="dialog" title="Edit cell contents">
        </div>
    </body>
</html>