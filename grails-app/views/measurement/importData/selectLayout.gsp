<html>
    <head>
      <meta name="layout" content="sammain"/>
      <title>Measurement importer</title>
        
      <r:require module="importer" />
    </head>
    <body>
        <content tag="contextmenu">
            <g:render template="contextmenu" />
        </content>
        <div class="data">

            <imp:importerHeader pages="${pages}" page="selectLayout" />

            <p>
                The data has been successfully read in.
                Now we need to know which layout the data uses. There are two layouts you can choose from:
            </p>
            <form method="post">
                <div class='layoutchoice'>
                    <div style="vertical-align: middle; text-align:center;">
                        <input type="radio" name="layoutselector" value="sample_layout" onclick="$('#subjectsample').slideUp('medium'); $('#samplesample').slideDown('medium');" <g:if test="${layoutguess=='sample_layout'}">checked=''</g:if>/>
                        <b>The sample layout</b>
                    </div>
                    <div id="samplesample" <g:if test="${layoutguess!='sample_layout'}">style="display: none"</g:if>>
                        <g:if test="${layoutguess=='sample_layout'}">Our guess is that this data uses the sample layout. </g:if>Using the sample layout and a sample of the data, the data types would be as follows:
                        <%
                            def content_sample_sample_layout = "<table style='width: auto;'>"
                            for(int i = 0; i < text?.size(); i++){
                                if(i == 5){
                                    break;
                                }
                                if(text[i]?.size()==0){
                                    content_sample_sample_layout += "<tr></tr>"
                                } else {
                                    content_sample_sample_layout += "<tr>"
                                    for(int j = 0; j < text[i]?.size(); j++){
                                        if(j == 5){
                                            break;
                                        }
                                        if(i==0){
                                            if(j==0 && text[i][j]!=null && text[i][j].length()!=0){
                                                def tmp = 'Warning: This cell contains data ("'+text[i][j]+'"), this will be ignored.';
                                                if(tmp.length()>25){
                                                    content_sample_sample_layout += '<td style="border: 1px solid lightgray; color: black;" class="badcell"><div class="tooltip importerInteractiveCell">'+tmp.substring(0,19)+' &hellip;<span>'+tmp+'</span></div></td>';
                                                } else {
                                                    content_sample_sample_layout += '<td style="border: 1px solid lightgray; color: black;" class="badcell">'+tmp+'</td>'
                                                }
                                            } else {
                                                def colour = ""
                                                if(j==1){colour = "darkgreen"}
                                                if(j==2){colour = "lightseagreen"}
                                                if(j==3){colour = "green"}
                                                content_sample_sample_layout += '<td style="border: 1px solid lightgray;color: '+colour+';">'
                                                if(text[i][j]!=null && text[i][j].length()>25){
                                                    content_sample_sample_layout += text[i][j].substring(0,19)+"&hellip;"
                                                } else {
                                                    content_sample_sample_layout += text[i][j]
                                                }
                                                content_sample_sample_layout += "</td>"
                                            }
                                        } else {
                                            def colour = ""
                                            if(j==0){colour = "purple"}
                                            else{colour = "blue"}
                                            content_sample_sample_layout += '<td style="border: 1px solid lightgray; color: '+colour+';">'
                                            if(text[i][j]!=null && text[i][j].length()>25){
                                                content_sample_sample_layout += text[i][j].substring(0,19)+"&hellip;"
                                            } else {
                                                content_sample_sample_layout += text[i][j]
                                            }
                                            content_sample_sample_layout += "</td>"
                                        }
                                    }
                                    content_sample_sample_layout += "</tr>"
                                }
                            }
                            content_sample_sample_layout += "</table>"
                            println content_sample_sample_layout
                        %>
                    </div>
                </div>
                <div class='layoutchoice'>
                    <g:if test="${disableSubjectLayout}">
                        Because the selected assay does not contain enough information to be able to use the subject layout (such as event timepoints and subject names), the subject layout cannot be used. If you do want to upload data that uses the subject layout, please enter the required information into GSCF and try again.
                    </g:if>
                    <g:else>
                        <div style="vertical-align: middle; text-align:center;">
                            <input type="radio" name="layoutselector" value="subject_layout" onclick="$('#samplesample').slideUp('medium'); $('#subjectsample').slideDown('medium');" <g:if test="${layoutguess=='subject_layout'}">checked=''</g:if>/>
                            <b>The subject layout</b>
                        </div>
                        <div id="subjectsample" <g:if test="${layoutguess!='subject_layout'}">style="display: none"</g:if>>
                            <g:if test="${layoutguess=='subject_layout'}">Our guess is that this data uses the subject layout. </g:if>
                            Using the subject layout and a sample of the data, the data types would be as follows:
                            <%
                                def content_sample_subject_layout = "<table style='width: auto;'>"
                                for(int i = 0; i < text?.size(); i++){
                                    if(i == 5){
                                        break;
                                    }
                                    if(text[i]?.size()==0){
                                        content_sample_subject_layout += "<tr></tr>"
                                    } else {
                                        content_sample_subject_layout += "<tr>"
                                        for(int j = 0; j < text[i]?.size(); j++){
                                            if(j == 5){
                                                break;
                                            }
                                            if(i==0){
                                                if(j==0 && text[i][j]!=null && text[i][j].length()!=0){
                                                    def tmp = 'Warning: This cell contains data ("'+text[i][j]+'"), this will be ignored.';
                                                    if(tmp.length()>25){
                                                        content_sample_subject_layout += '<td style="border: 1px solid lightgray; color: black;" class="badcell"><div class="tooltip importerInteractiveCell">'+tmp.substring(0,19)+' &hellip;<span>'+tmp+'</span></div></td>';
                                                    } else {
                                                        content_sample_subject_layout += '<td style="border: 1px solid lightgray; color: black;" class="badcell">'+tmp+'</td>'
                                                    }
                                                } else {
                                                    content_sample_subject_layout += '<td style="border: 1px solid lightgray; color: darkgreen;">';
                                                    if(text[i][j]!=null && text[i][j].length()>25){
                                                        content_sample_subject_layout += text[i][j].substring(0,19)+"&hellip;"
                                                    } else {
                                                        content_sample_subject_layout += text[i][j]
                                                    }
                                                    content_sample_subject_layout += "</td>"
                                                }
                                            } else {
                                                if(i==1){
                                                    if(j==0 && text[i][j]!=null && text[i][j].length()!=0){
                                                        def tmp = 'Warning: This cell contains data ("'+text[i][j]+'"), this will be ignored.';
                                                        if(tmp.length()>25){
                                                            content_sample_subject_layout += '<td style="border: 1px solid lightgray; color: black;" class="badcell"><div class="tooltip importerInteractiveCell">'+tmp.substring(0,19)+' &hellip;<span>'+tmp+'</span></div></td>';
                                                        } else {
                                                            content_sample_subject_layout += '<td style="border: 1px solid lightgray; color: black;" class="badcell">'+tmp+'</td>'
                                                        }
                                                    } else {
                                                        content_sample_subject_layout += '<td style="border: 1px solid lightgray; color: peru;">';
                                                    if(text[i][j]!=null && text[i][j].length()>25){
                                                        content_sample_subject_layout += text[i][j].substring(0,19)+"&hellip;"
                                                    } else {
                                                        content_sample_subject_layout += text[i][j]
                                                    }
                                                    content_sample_subject_layout += "</td>"
                                                    }
                                                } else {
                                                    def colour = ""
                                                    if(j==0){colour = "gray"}
                                                    else{colour = "blue"}
                                                    content_sample_subject_layout += '<td style="border: 1px solid lightgray;  color: '+colour+';">';
                                                    if(text[i][j]!=null && text[i][j].length()>25){
                                                        content_sample_subject_layout += text[i][j].substring(0,19)+"&hellip;"
                                                    } else {
                                                        content_sample_subject_layout += text[i][j]
                                                    }
                                                    content_sample_subject_layout += "</td>"
                                                }
                                            }
                                        }
                                        content_sample_subject_layout += "</tr>"
                                    }
                                }
                                content_sample_subject_layout += "</table>"
                                println content_sample_subject_layout
                            %>
                        </div>
                    </g:else>
                </div>
                <imp:importerFooter>
                    <g:submitButton name="previous" value="« Previous" action="previous"/>
                    <g:submitButton name="next" value="Next »" action="next"/>
                </imp:importerFooter>
            </form>
        </div>
    </body>
</html>