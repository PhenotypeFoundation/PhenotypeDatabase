<script type="text/javascript">

var parametertypes= new Array();
<% dbnp.studycapturing.ProtocolParameterType.list().each{ print "parametertypes.push(\'${it}\');" } %>


function addRowEmpty(id){
    var tbody = document.getElementById(id);
    var row = document.createElement("tr");

    var newRowId=''
    if(tbody.getElementsByTagName('tr').length<=0) { newRowId='0'; }
    else { 
        var elements=tbody.getElementsByTagName('tr');
        var predecessor=elements[elements.length-1];
	newRowId=predecessor.id;
    }

    row.setAttribute('id',newRowId+'1');

    addTextFieldToRow(row,'classification',20); addTextFieldToRow(row,'unit',6);
    var textField=addSelector(row,null); addTextFieldToRow(row,'reference',10); addTextFieldToRow(row,'description',20);
    addElementToRow(row,textField,'option',6); addRowButton(row); tbody.appendChild(row);
}


function addRow(id,newId,name,unit,type,reference,description) {

    var tbody = document.getElementById(id);
    var row = document.createElement("tr");
    row.id=newId;

    var newRowId=''
    if(tbody.getElementsByTagName('tr').length<=0) { newRowId='0'; }
    else {
        var elements=tbody.getElementsByTagName('tr');
        var predecessor=elements[elements.length-1];
	newRowId=predecessor.id;
    }

    row.setAttribute('id',newRowId+'1');

    addTextFieldToRow(row,'classification',20).value=name;
    addTextFieldToRow(row,'unit',6).value=unit;
    var textField=addSelector(row,type);
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
     removeButton.setAttribute('onclick',"removeRow('" + parent.id + "')");
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



   function addRowToDialog(dialogTableBodyId) {
        var tbody = document.getElementById(dialogTableBodyId);
	var input=document.createElement('input');
        tbody.insertRow(-1).insertCell(-1).appendChild(input);
   }



   // create the dialog for this STRINGLIST.
   // the dialog holds all possible values this parameter can take.
   // moreover, it is extendable.
   function addDialogForSelector(rowId,options) {
     var dialog = document.createElement('div');
     dialog.setAttribute('id','dialog_'+rowId);
     //var dialogText = document.createTextNode(dialog.id);
     //dialog.appendChild(dialogText);


     var table=document.createElement('table');
     var tbody=document.createElement('tbody'); tbody.id='options_'+dialog.id;
     var tr=document.createElement('tr');
     var th=document.createElement('th');
     var tx=document.createTextNode('Parameter Values');
     dialog.appendChild(table);
     table.appendChild(tbody);
     tbody.appendChild(tr);
     tr.appendChild(tx);

     for(i=0;i<options.length;i++){
	 var input=document.createElement('input');
	 input.value=options[i];
         tbody.insertRow(-1).insertCell(-1).appendChild(input);
     }

     var button=document.createElement('input');
     button.setAttribute('type','Button');
     button.value='Add Option';
     dialog.appendChild(button);
     button.onclick=function(){ addRowToDialog('options_'+dialog.id); }


     return dialog;
   }


   function addSelector(row,selectedText){

     // add dialog for displaying paramter options
     // preserve row's id
     var options= [1,2,3,4,5,6,7,8,9];
     var dialog = addDialogForSelector(row.id,options);


     var selector=document.createElement("select");
     selector.setAttribute('id',"parameter_type"+row.id);
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
     row.appendChild(td);
     td.appendChild(dialog);                          // dialog
     jQuery(dialog).dialog({autoOpen:false});

     var showDialogString = "jQuery('#"+ dialog.id + "').dialog('open');"


     // set label for selected element
     selector.selectedIndex=selectedIndex;
     var option=selector.options[selector.selectedIndex];

     var anchor= document.createElement('a');
     anchor.setAttribute('name', 'myanchor');
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




// testing, remove later
function doStuff(caller) { var row = caller.parentNode.parentNode; $("#"+row.id).hide(); }


</script>



































<tr class="prop">
    <td id='test'>  nope </td>
    <td> <g:select name="selectedProtocol" from="${dbnp.studycapturing.Protocol.list()}" value="${protocol.id}" optionKey="id"   optionValue="name"
		   onchange= "${remoteFunction( action:'showProtocolParameters', update:'showProtocolParameters', params:'\'id=\'+this.value' )} " />
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
    <g:include action="showProtocolParameters" controller="eventDescription" id="${description.id}" />
</tbody>


<tbody> <tr>
<td></td> <td></td> <td></td> <td></td> <td></td> <td></td> <td> <input type="button" value="Add Parameter" onclick="addRowEmpty('showProtocolParameters')"> </td>
</tr> </tbody>

</table>


</td> </tr>