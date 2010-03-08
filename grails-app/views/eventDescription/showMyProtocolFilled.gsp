<script type="text/javascript">

/* The following JS functions provide the dynamics of this view.
 * Mainly, rows of parameters can be added or removed by the user.
 * Additionally, each parameter can contain a STRINGLIST. In this case
 * the a link is activated. On following this link the user can activate
 * a dialog that displays all options of the STRINGLIST and edit them. */
var parametertypes= new Array();
var newRows=0;
<% dbnp.studycapturing.ProtocolParameterType.list().each{ print "parametertypes.push(\'${it}\');" } %>

/* create a prefix for all members of a protocol */
function setName(element,protocolId) {
    element.name='protocolId_'+protocolId+'_'+element.id;
}

function addRowEmpty(id){
    var tbody = document.getElementById(id);
    var row = document.createElement("tr");
    row.setAttribute('id','new' + (newRows++) );

    addTextFieldToRow(row,'classification',20); addTextFieldToRow(row,'unit',6);
    var textField=addSelector(row,null,[]); addTextFieldToRow(row,'reference',10); addTextFieldToRow(row,'description',20);
    addElementToRow(row,textField,'option',6); addRowButton(row); tbody.appendChild(row);
}


function addRow(id,newId,name,unit,type,reference,description,options) {

    var tbody = document.getElementById(id);
    var row = document.createElement("tr");
    row.setAttribute('id',newId);

    addTextFieldToRow(row,'classification',20).value=name;
    addTextFieldToRow(row,'unit',6).value=unit;
    var textField=addSelector(row,type,options);
    addTextFieldToRow(row,'reference',10).value=reference;
    addTextFieldToRow(row,'description',20).value=description;
    addElementToRow(row,textField,'option',6);
    addRowButton(row);
    tbody.appendChild(row);
}


  function addRowButton(parent) {
     var removeButton=document.createElement("input");
     var body=parent.parentNode;
     removeButton.setAttribute('type','button');
     removeButton.setAttribute('onclick',"removeRow('" + parent.id + "');");
     removeButton.setAttribute('value','remove');
     var td=document.createElement('td');
     td.appendChild(removeButton);
     parent.appendChild(td);
  }


  function addElementToRow(row,field,id,size){
     var td=document.createElement('td');
     td.setAttribute('id',id + '_' + row.id);
     td.appendChild(field);
     row.appendChild(td);
     return field;
  }


  function addTextFieldToRow(row,id,size){
     var input=document.createElement("input");
     input.setAttribute('type','text');
     input.setAttribute('id',id);
     input.setAttribute('name', 'row_' + row.id + '__' + id);
     input.setAttribute('size',size);
     var td=document.createElement('td');
     td.appendChild(input);
     row.appendChild(td);
     return input;
  }


  function removeRow(rowId){
     var row = document.getElementById(rowId);
     var body = row.parentNode;
     body.removeChild(row);
     jQuery(document.getElementById('dialog'+rowId)).remove();
  }


  // for the STRINGLIST type, display a link to show
  // all optional values of the parameter.
  function showLinkForSTRINGLIST(anchor,textNode,option,showDialogString,dialog) {
        if(option.value=='STRINGLIST') {
	   textNode.nodeValue='edit';
           anchor.setAttribute('onclick',showDialogString);
        }
        else {
           textNode.nodeValue='n.a.';
	   anchor.setAttribute('onclick','');
        }
  }



  function addRowToDialog(tbody,rowId) {
	var input=document.createElement('input');
	var id = tbody.rows.length + 1;
        input.setAttribute('name','parameterStringValue__new'+id+'__protocol__'+rowId);
	var tr=document.createElement('tr');
        tr.insertCell(-1).appendChild(input);
	tbody.appendChild(tr);
	var button=document.createElement('input');
	button.type='button';
	button.value='delete';
	button.onclick=function(){jQuery(tr).remove()};
	tr.insertCell(-1).appendChild(button);
  }


   // create the dialog for this STRINGLIST.
   // the dialog holds all possible values this parameter can take.
   // moreover, it is extendable.
   function addDialogForSelector(rowId,options) {
     var dialog = document.createElement('div');
     dialog.id='dialog'+rowId;
     dialog.setAttribute('name','hiddenDialog');

     var table=document.createElement('table');
     var tbody=document.createElement('tbody'); tbody.id='options_'+dialog.id;
     var tr=document.createElement('tr');
     var th=document.createElement('th');
     var tx=document.createTextNode('Parameter Values');
     dialog.appendChild(table);
     table.appendChild(tbody);
     tbody.appendChild(tr);
     tr.appendChild(tx);

     for(i=0;i<options.length;i+=2){
	 var input=document.createElement('input');
	 input.value=unescape(options[i]);
	 input.name='parameterStringValue__'+options[i+1]+'__protocol__'+rowId;
	 var tr=document.createElement('tr');
	 tbody.appendChild(tr);
	 tr.insertCell(-1).appendChild(input);
	 var button=document.createElement('input');
	 button.type='button';
	 button.value='delete';
	 button.onclick=function(){jQuery(tr).remove()};
	 tr.insertCell(-1).appendChild(button);
     }

     var button=document.createElement('input');
     button.setAttribute('type','Button');
     button.value='Add Option';
     dialog.appendChild(button);
     button.onclick=function(){ addRowToDialog(tbody,rowId); }

     return dialog;
   }


   function addSelector(row,selectedText,options){

     // add dialog for displaying paramter options
     // preserve row's id
     var dialog = addDialogForSelector(row.id,options);


     var selector=document.createElement("select");
     selector.setAttribute('id',"type"+row.id);
     setName(selector,row.id);
     var selectedIndex=0;
     for(i=0;i<parametertypes.length;i++) {
         var option = document.createElement("option");
         var text = document.createTextNode(parametertypes[i]);
	 option.appendChild(text);
	 selector.appendChild(option);
	 if(selectedText!=null&&selectedText==parametertypes[i]) { selectedIndex=i; }
     }


     //  td for the selector
     var td=document.createElement('td');
     td.appendChild(selector);
     td.appendChild(dialog);                          // dialog
     row.appendChild(td);
     jQuery(dialog).dialog({ autoOpen:false, });


     var showDialogString = "jQuery('#"+ dialog.id + "').dialog('open');"


     // set label for selected element
     selector.selectedIndex=selectedIndex;
     var option=selector.options[selector.selectedIndex];

     var anchor= document.createElement('a');
     anchor.name='myanchor';
     var textNode=document.createTextNode('');
     option=selector.options[selector.selectedIndex];
     textNode.nodeValue=(option.value=='STRINGLIST') ? 'edit' : 'n.a.';
     showLinkForSTRINGLIST(anchor,textNode,option,showDialogString,dialog);
     anchor.appendChild(textNode);


     // show edit link for STRINGLIST
     selector.onchange=function(){
         option=this.options[this.selectedIndex];
         showLinkForSTRINGLIST(anchor,textNode,option,showDialogString,dialog);
     }


     return anchor;
  }


  function getElementsByClass (className) {
      var all = document.all ? document.all :
      document.getElementsByTagName('*');
      var elements = new Array();
      for (var e = 0; e < all.length; e++)
         if (all[e].className == className)
         elements[elements.length] = all[e];
      return elements;
  }



  function addHiddenDialogsToForm() {
     var form=document.getElementById('showProtocolParameters');
     var dialogs=document.getElementsByName('hiddenDialog');
     for(i=0;i<dialogs.length;i++) {
         form.appendChild(dialogs[i]);
     }
  }

  function deleteHiddenDialogs() {
      var dialogs=document.getElementsByName('hiddenDialog');
      for(i=0;i<dialogs.length;i++) {
          jQuery(dialogs[i]).remove();
      }
  }

</script>















<tr class="prop">
    <td id='test'>  Protocol </td>
    <td> <g:select name="protocol" from="${dbnp.studycapturing.Protocol.list()}" value="${protocol}" optionKey="id"   optionValue="name"
		   onchange= "${remoteFunction( action:'showProtocolParameters', update:'showProtocolParameters', params:'\'id=\'+this.value' )}; deleteHiddenDialogs();" />
    </td>
</tr>


<tr><td></td>
<td>

<table id="someId" >


<thead>
    <tr class="prop">

         <th valign="top" class="name" width=200>
         <label for="protocolInstance">Name</label>
         </th>

         <th valign="top" class="name" width=200>
         <label for="protocolInstance">Unit</label>
         </th>

         <th valign="top" class="name" width=200>
         <label for="protocolInstance">Type</label>
         </th>

         <th valign="top" class="name" width=200>
         <label for="protocolInstance">Reference</label>
         </th>

         <th valign="top" class="name" width=200>
         <label for="protocolInstance">Description</label>
         </th>

         <th valign="top" class="name" width=200>
         <label for="protocolInstance">Options</label>
         </th>

         <th valign="top" class="name" width=200>
         <label for="protocolInstance">Delete </label>
         </th>

    </tr>
</thead>



<tbody id="showProtocolParameters">
    <g:include action="showProtocolParameters" controller="eventDescription" id="${description.id} params="[protocol:protocol]" />
</tbody>


<tbody> <tr>
<td></td> <td></td> <td></td> <td></td> <td></td> <td></td> <td> <input type="button" value="Add Parameter" onclick="addRowEmpty('showProtocolParameters')"/> </td>
</tr> </tbody>

</table>


</td> </tr>