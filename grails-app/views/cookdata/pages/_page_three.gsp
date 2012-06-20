<af:page>
	<h1>Page Three</h1>
	<p>
		<h2>Please indicate to which set you wish to assign each series of samples:</h2>
		<table>
			<tr>
				<th>
					Set A
				</th>
				<th>
					Set B
				</th>
				<th>
					Group
				</th>
				<th>
					Sampling event
				</th>
				<th>
					Sampling event, starttime
				</th>
				<th>
					# samples in selection
				</th>
				
			</tr>
			<g:each in="${selectionTriples}" var="pair" status="p">
				<tr>
					<td>
						<g:checkBox name="A_${p}"/>
					</td>
					<td>
						<g:checkBox name="B_${p}"/>
					</td>
					<td>
						${eventGroups[pair[1]]?.name}
					</td>
					<td>
						${samplingEvents[pair[0]]?.sampleTemplate.name}
					</td>
					<td>
						${samplingEvents[pair[0]]?.getStartTimeString()}
					</td>
					<td>
						${samplingEvents[pair[0]]?.samples.size()}
					</td>
				</tr>
			</g:each>
		</table>
	<p>
</af:page>
