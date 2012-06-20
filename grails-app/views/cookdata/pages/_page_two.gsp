<af:page>
	<h1>Page Two</h1>
	<p>
		<h2>Please select one or more event groups, per sampling event:</h2>
		<table>
			<tr>
				<th>
					Sampling event
				</th>
				<th>
					Starttime
				</th>
				<th>
					# samples in event
				</th>
				<g:each in="${eventGroups}" var="group">
					<th>
						${group.name}
					</th>
				</g:each>
			</tr>
			<g:each in="${samplingEvents}" var="event" status="e">
				<tr>
					<td>
						${event.sampleTemplate.name}
					</td>
					<td>
						${event?.getStartTimeString()}
					</td>
					<td>
						${event?.samples.size()}
					</td>
					<g:each in="${eventGroups}" var="group" status="g">
						<td>
							<g:if test="${event.belongsToGroup([group])}">
								<g:checkBox name="${e}_${g}"/>
							</g:if>
						</td>
					</g:each>
				</tr>
			</g:each>
		</table>
	<p>
</af:page>
