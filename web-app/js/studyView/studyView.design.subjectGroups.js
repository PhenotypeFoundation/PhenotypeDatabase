if( typeof( StudyView ) === "undefined" ) { 
	StudyView = {};
};

if( typeof( StudyView.design ) === "undefined" ) { 
	StudyView.design = {};
};

StudyView.design.subjectGroups = {
	groups: {
		data: [],
		
		findIndexById: function( id ) {
			for( i in StudyView.design.subjectGroups.groups.data ) {
				if( StudyView.design.subjectGroups.groups.data[ i ].id == id )
					return i;
			}
			
			return -1;
		},
		findIndexByName: function( name ) {
			for( i in StudyView.design.subjectGroups.groups.data ) {
				if( StudyView.design.subjectGroups.groups.data[ i ].name == name )
					return i;
			}
			
			return -1;
		},
		findByName: function( name ) {
			var i = StudyView.design.subjectGroups.groups.findIndexByName( name );
			
			if( i > -1 ) {
				return StudyView.design.subjectGroups.groups.data[ i ];
			} else {
				return null;
			}
		},
		findById: function( id ) {
			var i = StudyView.design.subjectGroups.groups.findIndexById( id );
			
			if( i > -1 ) {
				return StudyView.design.subjectGroups.groups.data[ i ];
			} else {
				return null;
			}
		},
		
		size: function() {
			return StudyView.design.subjectGroups.groups.data.length;
		},
	},
	
	updateTimeline: function() {
		if( StudyView.design.subjectGroups.groups.size() == 0 ) {
			$("#timeline-eventgroups" ).hide();
		} else {
			$("#timeline-eventgroups" ).show();
			StudyView.design.timelineObject.redraw();
		}
	},
	
	getDataUrl: function( group ) {
		return $( '#subjectGroup' ).attr( "action" ) + "Details/" + group.id;
	}
};
