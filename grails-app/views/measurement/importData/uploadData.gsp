<html>
    <head>
        <meta name="layout" content="sammain"/>
        <title>Measurement importer</title>

        <r:script type="text/javascript" disposition="head">
	        function createTextfield(id) {
	            $( "#"+id ).html("<br /><br /><span style='color: grey'>Add tab delimited data</span><br /><textarea id='"+id+"' name='"+id+"' rows='5' cols='20'></textarea>");
	            $( "#"+id ).resizable({
	                handles: "se"
	            });
	        }
        </r:script>
        
        <r:require module="importer" />
    </head>
    <body>
        <content tag="contextmenu">
            <g:render template="contextmenu" />
        </content>
        <div class="data">

            <imp:importerHeader pages="${pages}" page="uploadData" />

            <form method="post" enctype="multipart/form-data">

                <h2>Select the platform of the measurements you want to upload</h2>
                <g:select name="platform" from="${org.dbxp.sam.Platform.list()}" />
                <h2>Locate the file on your computer:</h2>
                <p class="fieldInfo">Make sure to add an <strong>excel</strong> or <strong>comma-separated file</strong> that has a structure that corresponds to one of the <a href="#" onClick="$( '#layouts' ).dialog( 'open' ); return false;">allowed layouts</a>. Alternatively, you can paste such data in the textfield, which you can find under the 'paste in textfield' option.</p>

                <g:if test="${message}">
                    <div class="errors">${message}</div>
                    <br />
                </g:if>

	            <input id="file" type="file" id="fileUpload" name="fileUpload"/> <span id="pasteField">or <a href="#" onclick="createTextfield('pasteField'); return false;">paste in textfield</a></span>
            
	            <div id="layouts">
	            	<h3>Two layouts</h3>
	            	You can upload your data file using two different layouts:<br />
		            <div style="display: inline-block; margin: 5px;">
		                <h2>The sample layout:</h2>
		                <br>
		                <r:img uri="/images/samplelayout.png"/>
		            </div>
		            <div style="display: inline-block; margin: 5px;">
		                <h2>The subject layout:</h2>
		                <br>
		                <r:img uri="/images/subjectlayout.png"/>
		            </div>
                </div>
                
                <imp:importerFooter>
                    <g:submitButton name="previous" value="« Previous" action="previous"/>
                    <g:submitButton name="next" value="Next »" action="next"/>
                </imp:importerFooter>
            </form>
        </div>
        
        <r:script>
        	$( '#layouts' ).dialog({
        		autoOpen: false,
        		modal: true,
        		height: 400,
        		width: 960,
        		resizable: false
        	});
        </r:script>
    </body>
</html>