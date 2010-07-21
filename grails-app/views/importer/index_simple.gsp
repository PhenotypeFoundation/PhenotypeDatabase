<!--
  To change this template, choose Tools | Templates
  and open the template in the editor.
-->

<%@ page contentType="text/html;charset=UTF-8" %>

<html>
  <head>
    <meta name="layout" content="main"/>
    <title>Importer wizard (simple)</title>

<g:javascript library="jquery" plugin="jquery"/>

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
}
</g:javascript>
</head>
  <body>
    <h1>Importer wizard (simple)</h1>
    <p>You can import your Excel data to the server by choosing a file from your local harddisk in the form below.</p>
	<g:form controller="importer" method="post" action="upload_simple" enctype="multipart/form-data">
	<table border="0">
    	<tr>
	    <td width="100px">
		Choose your Excel file to import:
	    </td>
	    <td width="100px">
		<input type="file" name="importfile"/>
	    </td>
	</tr>
	<tr>
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
		from="${entities}"		
		optionValue="${{it.value.name}}"
		optionKey="key"
		noSelection="['':'-Choose type of data-']"
		onChange="${remoteFunction( controller: 'importer',
					    action:'ajaxGetTemplatesByEntity',
					    params: '\'entity=\'+escape(this.value)',
					    onSuccess:'updateSelect(\'template_id\',data,false,false,\'default\')')}" />
	    </td>
	</tr>
	<tr>
	    <td>
		Choose type of data template:
	    </td>
	    <td>
		<g:select name="template_id" optionKey="id" optionValue="name" from="[]" />
	    </td>
	</tr>


	<tr>
	    <td colspan="2">
		<input type="submit" value="Next"/>
	    </td>
	</tr>
        </table>
	</g:form>

  </body>
</html>
