	<h3>Available sample types</h3>
	<ul>
		<g:each in="${study.samplingEvents}" var="samplingEvent">
			<li id="samplingEvent-${samplingEvent.id}" data-origin-id="${samplingEvent.id}" data-origin-type="samplingEvent" data-url="${g.createLink( action: 'samplingEventUpdate', id: samplingEvent.id  )}">
				<span class="name">${samplingEvent.name?.trim() ?: '[samplingevent without name]'}</span>
				<span class="designobject-buttons">
					<a href="#" class="edit">edit</a>
					<a href="#" class="delete">del</a>
				</span>
			</li>
		</g:each>
		<li class="add" data-url="${g.createLink( action: 'samplingEventAdd', params: [ parentId: study.id ] )}"><a href="#" onClick="StudyEdit.design.events.add( 'samplingEvent' ); return false;">Add new</a></li>
	</ul>