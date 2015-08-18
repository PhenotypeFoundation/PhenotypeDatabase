	<h3>Available treatments types</h3>
	<ul>
		<g:each in="${study.events}" var="event">
			<li id="event-${event.id}" data-duration="0" data-origin-id="${event.id}" data-origin-type="event"  data-url="${g.createLink( action: 'eventUpdate', id: event.id  )}">
				<span class="name">${event.name?.trim() ?: '[event without name]'}</span>
				<span class="designobject-buttons">
					<a href="#" class="edit">edit</a>
					<a href="#" class="delete">del</a>
				</span>
			</li>
		</g:each>
		<li class="add" data-url="${g.createLink( action: 'eventAdd', params: [ parentId: study.id ] )}"><a href="#" onClick="StudyEdit.design.events.add( 'event' ); return false;">Add new</a></li>
	</ul>
