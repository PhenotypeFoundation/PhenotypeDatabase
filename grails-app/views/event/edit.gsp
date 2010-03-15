<html>

<head>
      <meta name="layout" content="main" />
      <g:set var="entityName" value="${message(code: 'event.label', default: 'Event')}" />
      <g:setProvider library="jquery"/>
      <script src="${createLinkTo(dir: 'js', file: 'timepicker-0.2.1.js')}" type="text/javascript"></script>
</head>





<body>




<script type="text/javascript">

     var nextSampleRowId=0;

     function addAddButton(elementId,sampleTableId) {
         var parent=document.getElementById(elementId);
	 var button=document.createElement('input');
	 button.setAttribute('type','button');
	 button.setAttribute('onclick','addEmptySampleRow(\''+sampleTableId+'\');');
	 button.value='add Sample';
	 parent.appendChild(button);
     }


     function addEmptySampleRow(elementId) {
          addSampleRow(elementId,'','');
     }


     function addSampleRow(elementId,text1,text2) {
	     addSampleRowWithId(elementId,text1,text2,nextSampleRowId);
	     nextSampleRowId++;
     }


     function addSampleRowWithId(elementId,text1,text2,newId) {

        var parent=document.getElementById(elementId);
        var tr=document.createElement('tr');
	tr.id='samplerow'+newId;
	parent.appendChild(tr);

        var td1=document.createElement('td');
	var input1= document.createElement('input');
	input1.type='text';
	input1.value=text1;
	input1.name='sampleName'+(newId);
	input1.id=input1.name
	td1.appendChild(input1);

        var td2=document.createElement('td');
	var input2= document.createElement('input');
	input2.type='text';
	input2.value=text2;
	input2.name='sampleMaterial'+(newId);
	input2.id=input2.name
	td2.appendChild(input2);

	tr.appendChild(td1);
	tr.appendChild(td2);

        var td3=document.createElement('td');
	var button=document.createElement('input');
	button.setAttribute('type','button');
        button.value='delete';
	button.onclick=function(){jQuery(tr).remove();}
	tr.appendChild(td3);
	td3.appendChild(button);
     }

</script>




        <div class="body">

            <h1><g:message code="default.create.label" args="[entityName]" /></h1>


            <g:if test="${flash.message}">
                <div class="message">${flash.message}</div>
            </g:if>
	    <g:hasErrors bean="${eventInstance}">
		<div class="errors"> <g:renderErrors bean="${eventInstance}" as="list" /> </div>
            </g:hasErrors>


            <g:form action="save" method="post" id="${eventInstance.id}">

                <div class="dialog">
                    <table>
                        <tbody>


                            <!-- show the EventDescription -->

                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="eventDescription.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: description, field: 'name', 'errors')}">
                                    <g:textField name="name" value="${description?.name}" />
                                </td>
                            </tr>


                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="description"><g:message code="eventDescription.description.label" default="Description" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: description, field: 'description', 'errors')}">
                                    <g:textArea name="description" value="${description?.description}" cols="40" rows="6" />
                                </td>
                            </tr>


                            <!-- show Event members -->

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="startTime"><g:message code="event.startTime" default="Start Time" /></label>
                                </td>
                                <td valign="top" class="name">
			            <script> $(function() { $('#startTime').datepicker({ duration: '', showTime: true, constrainInput: false });}); </script>
	                            <%  def displayStartTime =String.format("%tm/", sDate ) %>
	                            <%      displayStartTime+=String.format("%td/", sDate ) %>
	                            <%      displayStartTime+=String.format("%tY", sDate ) %>
	                            <%      displayStartTime+=String.format(" %tI:", sDate ) %>
	                            <%      displayStartTime+=String.format("%tM ", sDate ) %>
	                            <%      displayStartTime+=String.format("%tp", sDate ) %>
				    <g:textField name="startTime" value="${displayStartTime}" />
                                </td>
                            </tr>



                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="endTime"><g:message code="event.endTime.label" default="End Time" /></label>
                                </td>
                                <td valign="top" class="name">
			            <script> $(function() { $('#endTime').datepicker({ duration: '', showTime: true, constrainInput: false });}); </script>
	                            <%  def displayEndTime =String.format("%tm/", eDate ) %>
	                            <%      displayEndTime+=String.format("%td/", eDate ) %>
	                            <%      displayEndTime+=String.format("%tY", eDate ) %>
	                            <%      displayEndTime+=String.format(" %tI:", eDate ) %>
	                            <%      displayEndTime+=String.format("%tM ", eDate ) %>
	                            <%      displayEndTime+=String.format("%tp", eDate ) %>
				    <g:textField name="endTime" value="${displayEndTime}" />
                                </td>
                            </tr>



	                    <!-- select protocol -->

                            <tr class="prop">

                                <td valign="top" class="name" width=200 >
                                  <label for="protocol"><g:message code="eventDescription.protocol.label" default="Protocol" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: description, field: 'rotocol', 'errors')}">
		                    <% protocols=dbnp.studycapturing.Protocol.list() %>
                                    <g:select name="protocol" id="protocol" from="${protocols}" optionKey="id" optionValue="${{it.name}}"
			                      value="${{it?.id}}" onchange="${remoteFunction(action:'showPartial', controller:'eventDescription', update:'preview',
                                              params:'\'protocolid=\' + this.value')} " />
                                </td>
                            </tr>


	                    <!-- this part changes dynamically on select -->

	                    <tbody id="preview">
	                        <g:include action='showPartial' controller='eventDescription' params="[protocolid:protocols[0].id]" id="${eventInstance.id}" />
	                    </tbody>



                        </tbody>                <!-- end of main table -->







	                <!-- samplePartial -->

                        <g:if test="${showSample}">

                             <tbody>
                                     <tr> <td> Sample </td>  <td>
                                     <table> 
                                     <thead> <th>Name</th> <th>Material</th> <th> Delete </th> </thead>
				     <tbody id="Samples" class="Samples"> </tbody>
				     </table>
				     </td> </tr>
                                     <g:each var="sample" in="${samples}"> 
					     <script type="text/javascript">
                                                     addSampleRowWithId('Samples',"${sample.name}","${sample.material}","_existing_${sample.id}")
					     </script>
                                     </g:each>
				     <tr><td></td><td id="sampleAddButtonRow"></td></tr>
			     </tbody>

			     <script type="text/javascript">
                                     addAddButton('sampleAddButtonRow','Samples');
			     </script>

                        </g:if>



                    </table>
                </div>

                <div class="buttons">
                    <span class="button"><g:submitButton name="save" class="save" value="${message(code: 'default.button.save.label', default: 'Save')}" /></span>
                </div>


            </g:form>
        </div>


</body>
</html>
