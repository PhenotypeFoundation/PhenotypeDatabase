if( typeof( StudyView ) === "undefined" ) { 
	StudyView = {};
}

/*************************************************
 *
 * Functions for adding publications to the study
 *
 ************************************************/

StudyView.publications = {
	
	/**
	 * Shows a publication on the screen
	 */
	show: function(element_id, id, title, authors, nr) {
		var titleDiv = document.createElement('div');
		titleDiv.className = 'title';
		titleDiv.appendChild(document.createTextNode(title));
	
		var authorsDiv = document.createElement('div');
		authorsDiv.className = 'authors';
		authorsDiv.appendChild(document.createTextNode(authors));
	
		var li = document.createElement('li');
		li.setAttribute('id', element_id + '_item_' + id);
		li.className = nr % 2 == 0 ? 'even' : 'odd';
		li.appendChild(titleDiv);
		li.appendChild(authorsDiv);
	
		$('#' + element_id + '_list').append(li);
	},

}


/*************************************************
 *
 * Functions for adding contacts to the study
 *
 ************************************************/

StudyView.contacts = {

	/**
	 * Shows a contact on the screen
	 */
	show: function(element_id, id, fullName, role, nr) {
		var titleDiv = document.createElement('div');
		titleDiv.className = 'person';
		titleDiv.appendChild(document.createTextNode(fullName));
	
		var authorsDiv = document.createElement('div');
		authorsDiv.className = 'role';
		authorsDiv.appendChild(document.createTextNode(role));
	
		var li = document.createElement('li');
		li.setAttribute('id', element_id + '_item_' + id);
		li.className = nr % 2 == 0 ? 'even' : 'odd';
		li.appendChild(titleDiv);
		li.appendChild(authorsDiv);
	
		$('#' + element_id + '_list').append(li);
	}
}

/*************************************************
 *
 * Functions for adding users (readers or writers) to the study
 *
 ************************************************/

StudyView.users = {
	/**
	 * Shows a publication on the screen
	 */
	show: function(element_id, id, username, nr) {
		var titleDiv = document.createElement('div');
		titleDiv.className = 'username';
		titleDiv.appendChild(document.createTextNode(username));
	
		var li = document.createElement('li');
		li.setAttribute('id', element_id + '_item_' + id);
		li.className = nr % 2 == 0 ? 'even' : 'odd';
		li.appendChild(titleDiv);
	
		$('#' + element_id + '_list').append(li);
	},
}