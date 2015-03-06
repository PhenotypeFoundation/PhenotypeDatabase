<%@ page import="org.dbxp.sam.Feature; org.dbxp.sam.Platform; org.dbnp.gdt.Template" %>
<html>
    <head>
        <meta name="layout" content="sammain"/>
        <title>Feature importer</title>
        <r:script type="text/javascript" disposition="head">
            $(document).ready(function() {
                insertSelectAddMore();
                strVal = "${inputField?.encodeAsJavaScript()}";
                if(strVal.length>0) {
                    createTextfield('pasteField','${inputField?.encodeAsJavaScript()}');
                }
            });

            function insertSelectAddMore() {
                new SelectAddMore().init({
                    rel  : 'template',
                    url  : baseUrl + '/templateEditor',
                    vars    : 'entity,ontologies',
                    label   : 'add / modify',
                    style   : 'modify',
                    onClose : function(scope) {
                        $.ajax({
                            url: baseUrl + "/feature/templateSelection",
                            success: function( returnHTML, textStatus, jqXHR ) {
                                $( "td#templateSelection" ).html( returnHTML );
                                insertSelectAddMore();

                                $("#template").children().each(function() {
                                    var child = $(this);
                                    var blnFound = false;
                                    for(i=0; i<prevTemplateOpt.length; i++) {
                                        if(child.val()==prevTemplateOpt[i].value) {
                                            blnFound = true;
                                            break;
                                        }
                                    }
                                    if(!blnFound || child.val()==selectedTempl) {
                                        if(!blnFound) {
                                            selectedTempl = child.val();
                                        }
                                        child.attr("selected","selected");
                                    }
                                });

                                prevTemplateOpt = null;
                            }
                        });
                    }
                });
            }

            function createTextfield(id, content) {
                $( "#"+id ).html("<span style='color: gray'>Add tab delimited data (<a href='#' onClick='createUpload(\"fileUpload\"); return false;'>close</a>)</span><br /><textarea id='"+id+"' name='"+id+"' rows='5' cols='20'>"+content+"</textarea>");
                $( "#"+id ).resizable({
                    handles: "se"
                });
            }

            function createUpload(id) {
                strContent = $("textarea#pasteField").val().replace(/\n/g,"\\n");
                $( "#pasteField" ).html('<input type="file" id="'+id+'" name="'+id+'"/> or <a href="#" onclick="createTextfield(\'pasteField\',\''+strContent+'\'); return false;">paste in textfield</a>');
            }


            var prevTemplateOpt = null;
            var selectedTempl = null;
            function handleTemplateChange(objSelect) {
                if($("#template option:selected").hasClass("modify")) {
                    prevTemplateOpt = $("#template").children();
                } else {
                    selectedTempl = $("#template option:selected").val();
                }
            }

        </r:script>
        
        <r:require module="importer" />
        
    </head>
    <body>
        <content tag="contextmenu">
      		<g:render template="contextmenu" />
        </content>
        <div class="data">

            <imp:importerHeader pages="${pages}" page="uploadAndSelectTemplate" />

            <p>You can import your Excel data to the server by choosing a file from your local harddisk in the form below. Alternatively, you can paste such data in the textfield, which you can find under the 'paste in textfield' option. Please make sure the data has a header row.</p>

            <g:if test="${message}">
                <div class="errors">${message}</div><br />
            </g:if>
            <div>
                <g:form method="post" enctype="multipart/form-data" name="importData" action="importData">
                    <table>
                        <tbody>
                            <tr>
                                <td width="100px">
                                    Choose your Excel file to import:
                                    <g:if test="${input!=null}">
                                        <p>The file <b>${input.originalFilename}</b> was loaded.</p>
                                    </g:if>
                                </td>
                                <td width="100px"><span id="pasteField"><input type="file" id="fileUpload" name="fileUpload"/> or <a href="#" onclick="createTextfield('pasteField','${inputField?.encodeAsJavaScript()}'); return false;">paste in textfield</a></span></td>
                            </tr>
                            <tr>
                                <td><div id="datatemplate">Choose type of data template (not required):</div></td>
                                <td id="templateSelection">
                                    <af:templateElement name="template" rel="template" description="" entity="${Feature}" ontologies="" value="${template}" error="template" addDummy="true" onChange="handleTemplateChange()"></af:templateElement>
                                </td>
                            </tr>
                        <tr>
                            <td>
                                Select the target platform for these features:
                            </td>
                            <td>
                                <g:select name="platform" from="${Platform.list()}" optionKey="id"/>
                            </td>
                        </tr>
                        </tbody>

                    </table>
                    <br />

                    <imp:importerFooter>
                        <g:submitButton name="previous" value="« Previous" action="" disabled="true"/>
                        <g:submitButton name="next" value="Next »" onClick="return !\$('option:selected', \$('#template') ).hasClass( 'modify' );" action="next"/>
                    </imp:importerFooter>
                </g:form>
            </div>
        </div>
    </body>
</html>