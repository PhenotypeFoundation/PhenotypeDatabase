<%@ page import="org.dbnp.gdt.RelTime" %>
<%@ page import="org.dbnp.gdt.TemplateFieldType" %>
<af:page>
	<style>
		.selectionSummaryTd{
			padding: 0px !important;
			border-style: none !important;
		}		
		.selectionSummaryDiv{
			border: 1px solid #CCCCCC;
			margin-left: 5px;
			margin-right: 5px;
		}
		#buttonAddEquation{
			background-color: #F77777;
			color: #FFFFFF;
		}
	</style>
	<h1>Assign sets</h1>
	<p>
		<h2>Please indicate to which set you wish to assign each series of samples:</h2>
		
		<g:each in="${samplingEventTemplates}" var="template">
			<h1>${template.name}</h1>
			<table>
				<tr>
					<th>
						# samples in event
					</th>
					<g:each in="${samplingEventFields}" var="field">
						<th>
							${field.name}
						</th>
					</g:each>
					<th>
						Group
					</th>
					<th>
						Set A
					</th>
					<th>
						Set B
					</th>	
				</tr>
				<g:each in="${selectionTriples}" var="pair" status="p">
					<g:if test="${samplingEvents[pair[0]]?.template == template}">
						<tr>
							<td>
								${samplingEvents[pair[0]]?.samples?.size()}
							</td>
							<g:each in="${samplingEventFields}" var="field">
								<td>
									<g:if test="${field.type == TemplateFieldType.RELTIME}">
										<g:if test="${samplingEvents[pair[0]]?.fieldExists(field.name)}">
											${new RelTime( samplingEvents[pair[0]]?.getFieldValue(field.name) ).toString()}	
										</g:if>
									</g:if>
									<g:else>
										<g:if test="${samplingEvents[pair[0]]?.fieldExists(field.name)}">
											${samplingEvents[pair[0]]?.getFieldValue(field.name)}
										</g:if>
									</g:else>
								</td>
							</g:each>
							<td>
								${eventGroups[pair[1]]?.name}
							</td>
							<td>
								<g:checkBox name="A_${p}"/>
							</td>
							<td>
								<g:checkBox name="B_${p}"/>
							</td>
						</tr>
					</g:if>
				</g:each>
			</table>
		</g:each>
	<p>
	
	<h1>Build equations</h1>
	<script type="text/javascript">
		var numEq = 0;
		function addSymbol(symbol){
			$("#calculatorInput").val($("#calculatorInput").val()+symbol);
		}
		function addEquation(){
			$("#calculatorEquations").html(
					"<span id=\"pair_"+numEq+"\">"+
						"<span>"+
							"<input name=\"eq_val_"+numEq+"\""+
								" id=\"eq_val_"+numEq+"\""+
								" type=\"text\" size=\"90\">"+
							"</input>"+
						"</span>"+
						"<span>"+
							"&nbsp;&nbsp;&nbsp;&nbsp;Label: "+
							"<input name=\"eq_label_"+numEq+"\""+
								" type=\"text\" size=\"30\">"+
							"</input>"+
						"</span>"+
					"</span>"+
					""+$("#calculatorEquations").html());
			$("input#eq_val_"+numEq).val($("#calculatorInput").val());
			numEq++;
		}
	</script>
	<div id="calculator">
		<div id="calculatorButtons">
			<button type="button" onclick="addSymbol('(');">(</button>
			<button type="button" onclick="addSymbol(')');">)</button>
			<button type="button" onclick="addSymbol('A');">A</button>
			<button type="button" onclick="addSymbol('B');">B</button>
			<button type="button" onclick="addSymbol('-');">-</button>
			<button type="button" onclick="addSymbol('+');">+</button>
			<button type="button" onclick="addSymbol('/');">/</button>
			<button type="button" onclick="addSymbol('*');">*</button>
			<button type="button" onclick="addSymbol('2log(');">2log</button>
			<button type="button" onclick="addSymbol('ln(');">ln</button>
			<button type="button" onclick="addSymbol('median(');">median</button>
			<button type="button" onclick="addSymbol('avg(');">avg</button>
			<button type="button" onclick="addEquation();" id="buttonAddEquation">Add equation</button>
		</div>
		<div id="calculatorRadios">
			Indicate how you wish to handle the values in sets:
			<span>
				Take the average over the set:
				<g:radio name="calculatorMode" value="average" checked="true"/>
			</span>
			<span>
				Take the median in the set:
				<g:radio name="calculatorMode" value="median"/>
			</span>
		</div>
		<div>
			<input id="calculatorInput" type="text" size="135"></input>
		</div>
	</div>
	<h1>Equations:</h1>
	<p id="calculatorEquations">
	</p>
</af:page>
