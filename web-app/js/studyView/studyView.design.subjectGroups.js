if( typeof( StudyView ) === "undefined" ) {
    StudyView = {};
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
    dialog: {
        get: function() {
            return $( '#subjectGroupDialog' );
        },
        open: function() {
            // open the dialog
            var dialog = StudyView.design.subjectGroups.dialog;

            dialog.get().dialog( 'open' );

            // Initialize the datatable only after opening the dialog
            // to make sure column width are correct
            StudyView.design.subjectGroups.dialog.dataTable.initialize();
        },
        close: function() {
            // Close the dialog
            StudyView.design.subjectGroups.dialog.get().dialog( 'close' );
        },

        create: function() {
            StudyView.design.subjectGroups.dialog.get().dialog( {
                modal: true,
                autoOpen: false,
                width: 900,
                close: function(event,ui) {
                    // Clear the datatable
                    StudyView.design.subjectGroups.dialog.dataTable.destroy();

                }
            });
        },

        dataTable: {
            getId: function() {
                return 'subjectsTable';
            },
            get: function() {
                return StudyView.design.subjectGroups.dialog.get().find( "#" + this.getId() ).dataTable();
            },
            initialize: function() {
                return StudyView.datatables.initialize( "#" + this.getId() );
            },
            destroy: function() {
                // Clear any references on selection
                StudyView.datatables.destroy( this.getId() );

                // Clear the datatable itself
                this.get().fnDestroy();
            }
        }
    },

    initialize: function() {
        // Create a dialog to add or edit subject groups
        StudyView.design.subjectGroups.dialog.create();

        // Enable doubleclick edit
        $( "#timeline-eventgroups" ).on( "dblclick", ".timeline-groups-text", function( e ) {
            StudyView.design.subjectGroups.show( $(e.target).text() );
        });

        // Update overlay
        StudyView.design.subjectGroups.updateOverlay();
    },

    /**
     * Updates the overlay visibility, based on the number of subject groups
     */
    updateOverlay: function() {
        // Add overlay if no subjectgroups are defined
        if( StudyView.design.subjectGroups.groups.size() == 0 ) {
            $( "#studydesign .overlay" ).show();
        } else {
            $( "#studydesign .overlay" ).hide();
        }
    },

    show: function( groupName ) {
        var subjectGroup = StudyView.design.subjectGroups.groups.findByName( groupName );
        if( !subjectGroup )
            return;

        var dialog = StudyView.design.subjectGroups.dialog.get();
        dialog.data( "subjectgroup-id", subjectGroup.id );

        StudyView.design.subjectGroups.dialog.open();

        // Load the data for the timeline
        var tableId = StudyView.design.subjectGroups.dialog.dataTable.getId();

        var loadingSelection = $( "#" + tableId ).parents( ".dataTables_wrapper" ).find( ".loadingSelection");
        loadingSelection.slideDown(100);

        $.get( StudyView.design.subjectGroups.getDataUrl( subjectGroup ), function( data ) {

            dialog.dialog( "option", "title", "Subjectgroup: "+data.name );

            //Check the right checkboxes
            $.each( data.subjects, function( idx, el ) {
                StudyView.datatables.selection.select( tableId, el.id, true );
                $( "#subjectsTable_ids_" + el.id ).attr( "checked", true );
            } );

            // Remove loading message
            loadingSelection.slideUp(100);
        });
    },

    getDataUrl: function( group ) {
        return $( '#subjectGroup' ).attr( "action" ) + "Details/" + group.id;
    }
};