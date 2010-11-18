<!--
  To change this template, choose Tools | Templates
  and open the template in the editor.
-->

<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta name="layout" content="main"/>
    <title>Importer wizard (simple)</title>    
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'importer.css')}"/>

<g:if env="production">
  <script type="text/javascript" src="${resource(dir: 'js', file: 'SelectAddMore.min.js')}"></script>
</g:if><g:else>
  <script type="text/javascript" src="${resource(dir: 'js', file: 'SelectAddMore.js')}"></script>
</g:else>

<g:javascript>
/**
 * Update one select based on another select
 *
 * @author
 * @see     http://www.grails.org/Tag+-+remoteFunction
 * @param   string  select (form) name
 * @param   string  JSON data
 * @param   boolean keep the first option
 * @param   int     selected option
 * @param   string  if null, show this as option instead
 * @void
 */
function updateSelect(name,data,keepFirstOption,selected,presentNullAsThis) {    
    var rselect = $('#'+name).get(0)
    var items = data

    // If a study has been selected, don't show the "Choose study" field, otherwise do    
    if ($('#'+'entity :selected').text() == 'Study')
      $('#studyfield').hide();
    else $('#studyfield').show();

    // set the entity name for the data template chooser
    //if ($('#'+'entity').val() == 'event')

    //$('select[name=template_id]').attr('entity', $('#'+'entity').val());
    $('select[name=template_id]').attr('entity', $('#'+'entity').val());

    if (items) {

        // remove old options
        var start = (keepFirstOption) ? 0 : -1;
        var i = rselect.length

        while (i > start) {
            rselect.remove(i)
            i--
        }

        // add new options
        $.each(items,function() {
            var i = rselect.options.length

            rselect.options[i] = new Option(
                (presentNullAsThis && this.name == null) ? presentNullAsThis : this.name,
                this.id
            );
            if (this.id == selected) rselect.options[i].selected = true
        });
    }

 // handle template selects 
  new SelectAddMore().init({
       rel     : 'typetemplate',
       url     : '/gscf/templateEditor',
       vars    : 'entity', // can be a comma separated list of variable names to pass on
       label   : 'add / modify ...',
       style   : 'modify',
       onClose : function(scope) {
           location.reload();
       }
    });
}

$(document).ready(function() {
    
    $('#simplewizardform').submit(function() {
	if ($('#file').val() == "") {
	    alert ("Please choose your Excel file to import.");
	    return false
	} else
	if ($('#entity').val() == "") {
	    $('#datatemplate').addClass("validationfail");
	    return false
	} else
	    $('#simplewizardform').submit();

	return false;
    });
});

</g:javascript>

<g:if env="production">
<script type="text/javascript" src="${resource(dir: 'js', file: 'SelectAddMore.min.js')}"></script>
</g:if><g:else>
<script type="text/javascript" src="${resource(dir: 'js', file: 'SelectAddMore.js')}"></script>
</g:else>
      
</head>
  <body>
    <h1>Importer wizard</h1>
    <p>You can import your Excel data to the server by choosing a file from your local harddisk in the form below.</p>
	<form id="simplewizardform" controller="importer" method="post" action="upload_simple" enctype="multipart/form-data">
	<table border="0">
    	<tr>
	    <td width="100px">
		Choose your Excel file to import:
	    </td>
	    <td width="100px">
		<input id="file" type="file" name="importfile"/>
	    </td>
	</tr>
	<tr>
	    <td width="100px">
		Use data from sheet:
	    </td>
	    <td width="100px">
		<g:select name="sheetindex" from="${1..25}"/>
	    </td>
	</tr>
	<tr>
	    <td width="100px">
		Columnheader starts at row:
	    </td>
	    <td width="100px">
		<g:select name="headerrow" from="${1..10}"/>
	    </td>
	</tr>
	<tr>
	    <td width="100px">
		Data starts at row:
	    </td>
	    <td width="100px">
		<g:select name="datamatrix_start" from="${2..10}"/>
	    </td>
	</tr>
	<tr id="studyfield">
	    <td>
		Choose your study:
	    </td>
	    <td>
		<g:select name="study.id" from="${studies}" optionKey="id"/>
	    </td>
	</tr>
	<tr>
	    <td>
		Choose type of data:
	    </td>
	    <td>
		<g:select                
		name="entity"
		id="entity"
		from="${entities}"		
		optionValue="${{it.value.name}}"
		optionKey="${{it.value.encrypted}}"
		noSelection="['':'-Choose type of data-']"
		onChange="${remoteFunction( controller: 'importer',
					    action:'ajaxGetTemplatesByEntity',
					    params: '\'entity=\'+escape(this.value)',
					    onSuccess:'updateSelect(\'template_id\',data,false,false,\'default\')')}" />
	    </td>
	</tr>
	<tr>
	    <td>
		<div id="datatemplate">Choose type of data template:</div>
	    </td>
	    <td>
		<g:select rel="typetemplate" entity="none" name="template_id" optionKey="id" optionValue="name" from="[]" />
	    </td>
	</tr>
	<tr>
	    <td colspan="2">
		<input type="submit" value="Next"/>
	    </td>
	</tr>
        </table>
	</form>

  </body>
</html>
