<html>
<head>
    <r:require modules="mapper,gscfimporter"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="sammain" />
    <title>SAM Feature Mapper</title>

    <script type="text/javascript">
        function choose(id, featureName) {

            var options = $('#options-' + id).data('options');
            var lastOptionIndex = 0;

            for (var i in options) {
                $("#ontologyTable tbody").append('<tr><td><input type="radio" name="select" value="'+i+'"></td><td id="'+i+'-name">'+options[i]['name']+'</td><td id="'+i+'-url"><a target="_blank" href="'+options[i]['url']+'">'+options[i]['url']+'</a></td></tr>');
                lastOptionIndex ++;
            }

            $("#ontologyTable tbody").append('<tr><td><input type="radio" name="select" value="'+(lastOptionIndex+1)+'"></td><td id="'+(lastOptionIndex+1)+'-name">'+featureName+'</td><td  id="'+(lastOptionIndex+1)+'-url"><input type="text" style="width: 260px" placeholder="Insert ONTOLOGY:URL"/></td></tr>');
            $("#ontologyTable tbody").append('<tr><td><input type="radio" name="select" value="'+(lastOptionIndex+2)+'"></td><td id="'+(lastOptionIndex+2)+'-name">Generate TNO identifier for '+featureName+'<input type="text" hidden="hidden" value="'+featureName+'"/></td><td></td></tr>');

            $("#dialog").dialog({
                title   : "Choose identifier for "+featureName,
                modal   : true,
                position: "center",
                width   : 600,
                height  : 400,
                buttons : {
                    Save  : function() {

                        var index = $('input[name=select]:checked').val();

                        if (typeof index !== 'undefined') {

                            index = parseInt(index);

                            if ( index <= lastOptionIndex ) {
                                var name = $('#'+index+'-name').html();
                                var url = $('#'+index+'-url a').html();

                                var userKey = $('#options-' + id).data('userKey');

                                $.ajax({
                                    url: "https://dashin.eu/idmapper/setid",
                                    type: "POST",
                                    data: { 'apiKey': '${apiKey}', 'userKey': userKey, 'name': name, 'url': url, 'ontology': '${ontology}' },
                                    success: function(data, textStatus, XmlHttpRequest) {
                                        setMatch( id, name, url );
                                    },
                                    error: function( request ) {
                                        console.log( "Request failed " + request.responseText );
                                    },
                                    complete: function( request, textStatus ) {
                                    }
                                });

                            }
                            else if ( index == lastOptionIndex+1 ) {
                                var name = $('#'+(index)+'-name').html();
                                var ontologyAndUrl = $('#'+(index)+'-url input').val().toLowerCase();
                                var ontology = ontologyAndUrl.split('http')[1].replace(':','').toUpperCase();
                                var url = 'http'+ontologyAndUrl.split('http')[1];

                                if ( url.startsWith('http') ) {
                                    $.ajax({
                                        url: "https://dashin.eu/idmapper/setcustomid",
                                        type: "POST",
                                        data: { 'apiKey': '${apiKey}', 'name': name, 'url': url, 'ontology': ontology },
                                        success: function(data, textStatus, XmlHttpRequest) {
                                            setMatch( id, name, url )
                                        },
                                        error: function( request ) {
                                            console.log( "Request failed " + request.responseText );
                                        },
                                        complete: function( request, textStatus ) {
                                        }
                                    });
                                }
                            }
                            else {
                                var name = $('#'+(index)+'-name input').val();

                                $.ajax({
                                    url: "https://dashin.eu/idmapper/generateid",
                                    type: "POST",
                                    data: { 'apiKey': '${apiKey}', 'name': name },
                                    success: function(data, textStatus, XmlHttpRequest) {
                                        data = JSON.parse(data);
                                        setMatch( id, name, data['id'] );
                                    },
                                    error: function( request ) {
                                        console.log( "Request failed " + request.responseText );
                                    },
                                    complete: function( request, textStatus ) {
                                    }
                                });
                            }

                            $(this).dialog("close");
                        }
                    },
                    Close  : function() {
                        $(this).dialog("close");

                    }
                },
                close   : function() {
                    $("#ontologyTable tbody tr").remove()
                }
            });
        }

        function search( id, featureName ) {
            $.ajax({
                url: "https://dashin.eu/idmapper/querytarget?target="+featureName+"&ontology=${ontology}",
                success: function(data, textStatus, XmlHttpRequest) {
                    data = JSON.parse(data);
                    var type = data['result type'];

                    switch(type) {
                        case 'Hit':
                            setMatch( id, featureName, data['result'] );
                            break;
                        case 'Multiple':
                            $('#search-' + id).html(type);
                            $('#search-' + id).attr('class', 'mapper-multiple');
                            $('#search-' + id).attr('onclick', 'choose('+id+', "'+featureName+'")');
                            $('#options-' + id).data('options',data['result']);
                            $('#options-' + id).data('userKey',data['user key']);
                            break;
                        case 'None':
                            $('#search-' + id).html(type);
                            $('#search-' + id).attr('class', 'mapper-none');
                            $('#search-' + id).attr('onclick', 'choose('+id+', "'+featureName+'")');
                            break;
                        case 'Error':
                            $('#search-' + id).html(type);
                            $('#search-' + id).attr('class', 'mapper-error');
                            break;
                    }
                },
                error: function( request ) {
                    console.log( "Request failed " + request.responseText );
                },
                complete: function( request, textStatus ) {
                }
            });
        }

        function setMatch( id, name, url ) {
            $('#search-' + id).attr('class', 'mapper-match');

            if ( url.startsWith('TNO') ) {
                $('#search-' + id).attr('href', '#');
            }
            else {
                $('#search-' + id).attr('href', url);
            }

            $('#search-' + id).attr('target', '_blank');
            $('#search-' + id).html(url.split("/")[url.split("/").length-1]);
            $('#search-' + id).remove('onclick');
            $('#mapped-' + id).val(url);
        }
    </script>
</head>
<body>
<div class="basicTabLayout importer">
    <h1>
        <span class="truncated-title">
            Feature Mapper for ${module}
        </span>
    </h1>

    <span class="message info">
        %{--<span class="title">Choose platform</span>--}%
        The mapper will now search for an identifier for the features in the selected ontology.<br/><br/>
        Click <div class="mapper-match" style="display: inline-block;">Hit</div> to go to the found identifier.<br/>
        Click <div class="mapper-multiple" style="display: inline-block;">Multiple</div> to choose a suggested identifier or to add one manually.<br/>
        Click <div class="mapper-none" style="display: inline-block;">None</div> to manually add an identifier.<br/><br/>
        Names in <div style="display: inline-block; color: #808080">gray</div> are already mapped on previous occasions.
    </span>

    <g:form action="submitFeatures">
        <g:hiddenField name="module" value="${module}"/>
        <fieldset>
            <table>
                <tr>
                    <td><b>Name</b></td>
                    <td><b>Cleaned name</b></td>
                </tr>
                <g:each var="feature" in="${featureList}" status="i">
                    <tr>
                        <g:if test="${feature.externalIdentifier}">
                            <td class="mapper-previously">${feature.name}</td>
                            <td class="mapper-previously">${feature.getCleanedName()}</td>
                            <td class="mapper-match">
                                <g:if test="${feature.externalIdentifier.startsWith('TNO')}">
                                    ${feature.externalIdentifier}
                                </g:if>
                                <g:else>
                                    <a href="${feature.externalIdentifier}" target="_blank" class="mapper-match">${feature.externalIdentifier.split('/')[feature.externalIdentifier.split('/').size()-1]}</a>
                                </g:else>
                            </td>
                        </g:if>
                        <g:else>
                            <td>${feature.name}</td>
                            <td>${feature.getCleanedName()}</td>
                            <td><a class="mapper-searching" id="search-${i}" href="#">searching in <u>HMDB</u>...</a><div id="options-${i}"><g:hiddenField id="mapped-${i}" name="${feature.id}" value=""/></div></td>
                            <script type="text/javascript">
                                search( ${i}, '${feature.getCleanedName()}' );
                            </script>
                        </g:else>
                    </tr>
                </g:each>
            </table>
        </fieldset>

        <p class="options">
            <a href="#" onClick="submit()" class="next">Submit</a>
        </p>
    </g:form>

    <div id="dialog" title="Basic dialog" hidden="hidden">
        <table id="ontologyTable" style="border: none">
            <thead>
                <tr>
                    <th></th>
                    <th>Name</th>
                    <th>URL</th>
                </tr>
            </thead>
            <tbody>

            </tbody>
        </table>
    </div>
</div>
</body>
</html>
