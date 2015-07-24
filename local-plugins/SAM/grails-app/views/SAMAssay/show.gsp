<%@ page import="java.math.MathContext" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="sammain" />
        <g:set var="entityName" value="${message(code: 'study.label', default: 'Study')}" />
        <title>Show ${module} assay ${assayInstance.name}</title>
        
        <style type="text/css">
        	.delete_button { display: none; }
        	td:hover .delete_button { display: inline; }
        </style>
    </head>
    <body>
        <content tag="contextmenu">
      		<li><g:link action="list" class="list"  params="${[module: module]}">Back to list</g:link></li>
        </content>    
		<h1>${module} ${assayInstance.name} / ${assayInstance.parent.title}</h1>
		
		<g:if test="${measurements.size() > 0}">
            <ul class="data_nav buttons ontop">
           		<li><g:link class="delete" controller="measurement" action="deleteByAssay" id="${assayInstance.id}" params="${[module: module]}" onClick="return confirm('Are you sure?');">Delete all measurements</g:link></li>
           		<li><g:link class="delete" controller="measurement" action="delete" onClick="if( \$( '#deleteform input:checked' ).length != 0 && confirm('Are you sure?') ) { \$( '#deleteform' ).submit(); } return false; ">Delete selected measurements</g:link></li>
            </ul>
            		
			<form id="deleteform" action="<g:createLink controller="measurement" action="delete" />" method="post">
				<input type="hidden" name="assayId" value="${assayInstance.id}" />
                <input type="hidden" name="module" value="${module}" />
				<table>
					<thead>
						<tr>
							<th></th>
							<g:each var="feature" in="${features}">
								<th>${feature} [${feature.unit}]</th>
							</g:each>
						</tr>
					</thead>
					<tbody>
						<g:set var="measurementIndex" value="${0}" />
						<g:each var="sample" in="${samples}">
							<tr>
								<td>${sample.name}</td>
								
								<g:each var="feature" in="${features}">
									<%--
										In every table cell, we should lookup the measurement that belongs to this sample and feature.
										Because the measurements are ordered in the same way as they are outputted to the screen 
										( sample.name, feature.name ), we can easily check whether the 'current' measurement belongs 
										to this cell. If not, we keep this cell empty.
										
										Because there might be multiple measurements for one cell, we first find all measurements for this cell.
										We show always the value/operator of the first measurement, but show all data in the comments field.
									--%>
									<g:set var="cellMeasurements" value="${[]}" />
									<g:set var="currentMeasurement" value="${measurements[ measurementIndex ]}" />
									<g:while test="${currentMeasurement?.sample?.id == sample.id && currentMeasurement?.feature?.id == feature.id}">
										<% cellMeasurements << currentMeasurement; %>
										<g:set var="currentMeasurement" value="${measurements[ ++measurementIndex ]}" />
									</g:while>
									
									<%-- 
										Now we know all measurements for this cell and the measurementIndex points to the 
										next measurement. If there are multiple measurements, we combine the data.
									--%>
									<g:if test="${cellMeasurements.size() > 0}">
										<% def comments = cellMeasurements[ 0 ].comments?.encodeAsHTML()
                                           def isNumeric = cellMeasurements[0].value.toString().isNumber() %>

										<g:if test="${cellMeasurements.size() > 1}">
											<%
                                                // Multiple measurements are no longer   allowed, so this code should not be triggered
                                                // TODO: Remove the multiple measurements code
												comments = cellMeasurements.collect {
													def description = ""
													
													if( it.value ){
														description += (it.operator ?: "")
                                                        if(it.value ==  it.value.round(3)){
                                                            description += it.value
                                                        } else {
                                                            description += "<span class='tooltip'>"+it.value.round(3).toString()+"<span>"+it.value+"</span></span>";
                                                        }
                                                    }

													if( it.comments )
														description += ( description ? "<br />" : "" ) + "<span class='comments'>" + it.comments.encodeAsHTML() + "</span>";
														 
												}.join( "<hr>" );
											%>
										</g:if>
										<td id="td${cellMeasurements[0].id}" class="${comments && isNumeric ? 'comments' : ''}">
											<% /* TODO: if multiple measurements are shown, this checkbox is not sufficient anymore */ %>
											<input type="checkbox" id="check${cellMeasurements[0].id}" name="ids" value="${cellMeasurements[0].id}" style="display:none;" />

											<g:if test="${cellMeasurements[0].operator}">${cellMeasurements[0].operator}</g:if>
                                            <g:if test="${isNumeric}">
                                                <g:if test="${comments}">
                                                    <%-- numeric value and comments --%>
                                                    <span class="tooltip"> ${cellMeasurements[0].value}<span>${comments}</span></span>
                                                </g:if>
                                                <g:else>
                                                    <g:if test="${cellMeasurements[0].value==cellMeasurements[0].value.round(3)}">
                                                        <%-- short numeric value without comments --%>
                                                        <span> ${cellMeasurements[0].value}</span>
                                                    </g:if>
                                                    <g:else>
                                                        <%-- long numeric value without comments; render short version and put entire number in tooltip --%>
                                                        <span class="tooltip"> ${cellMeasurements[0].value.round(3).toString()}<span>${cellMeasurements[0].value}</span></span>
                                                    </g:else>
                                                </g:else>
                                            </g:if>
                                            <g:else>
                                                <%-- measurement is not numeric, so use text value from comments --%>
                                                <span>${comments}</span>
											</g:else>
										</td>
										<script>
											$('#td${cellMeasurements[0].id}').on('click', function() {
												var checkbox = $('#check${cellMeasurements[0].id}');
												checkbox.prop('checked', !checkbox.prop('checked'));
												$(this).toggleClass('selected', checkbox.prop('checked'));
											});
										</script>
									</g:if>
									<g:else>
										<td></td>
									</g:else>
								</g:each>
							</tr>
						</g:each>
					</tbody>
				</table>
			</form>
			
            <br />
            <ul class="data_nav buttons">
           		<li><g:link class="delete" controller="measurement" action="deleteByAssay" id="${assayInstance.id}" params="${[module: module]}" onClick="return confirm('Are you sure?');">Delete all measurements</g:link></li>
           		<li><g:link class="delete" controller="measurement" action="delete" onClick="if( \$( '#deleteform input:checked' ).length != 0 && confirm('Are you sure?') ) { \$( '#deleteform' ).submit(); } return false; ">Delete selected measurements</g:link></li>
            </ul>
            			
			<g:if test="${hideEmpty}">
				<g:if test="${emptySamples > 0}">
					<p>
						${emptySamples} sample(s) are not shown because they have no measurements. 
						Click <g:link action="show" params="['id': assayInstance.id, 'hideEmpty': false, module: module]">here</g:link> to show all.
					</p>
				</g:if>
			</g:if>
			<g:else>
				<p>
					Click <g:link action="show" params="['id': assayInstance.id, 'hideEmpty': true, module: module]">here</g:link> to hide samples without measurements.
				</p>
			</g:else>
		</g:if>
		<g:else>
			<p>
				No measurements were found for this assay. Use the 
				<g:link controller="SAMImporter" action="upload" params="${[importer: "Measurements", module: module]}">importer</g:link>
				to import your data	or add your measurements <g:link controller="measurement" action="create" params="${[module: module]}">manually</g:link>.
			</p>
		</g:else>
    </body>
</html>
