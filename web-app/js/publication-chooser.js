/**
 * Publication chooser JavaScript class
 *
 * This class provides the possibility to search for publications from pubmed.
 * When used, an input field is converted to a jquery autocomplete field. The
 * field searches automatically for publications when 3 or more characters are
 * entered.
 * 
 * This class is copied from the ontology-chooser.
 *
 * Usage:
 * ------
 * <input type="text" name="..." rel="publication-pubmed" />
 *
 * Where the 'rel' value has the name 'publication' in the beginning. The text
 * after the dash is used for selecting the database to use. Multiple textboxes
 * can be used on one page, also textboxes that search different databases.
 * 
 * To initialize the textboxes, put the following javascript on the page. 
 * 
 *    PublicationChooser.init();
 * 
 * The script must be executed AFTER all textboxes have been loaded. A good way
 * to achieve that is to run the init function in the document.ready event:
 * 
 *    $(document).ready(function() {
 *        // initialize the publication chooser(s)
 *        PublicationChooser.init();
 *    });
 * 
 * Supporting different databases:
 * ---------------------------
 * The databases that can be used are stored in the availableDBs arraymap. This 
 * map consists of a key that describes the database to be used, and a value that
 * points to a function to be called when searching for the publications.
 * 
 * As such, this class is not able to search any database. In order to make an
 * interface for a database, a new method has to be created that searches for 
 * publications, and calls another method with  an array with strings or an 
 * object array with label/value properties. This is described on 
 * http://jqueryui.com/demos/autocomplete/#option-source
 * 
 * The function for searching has the following form:
 * 
 *     sourceExample = function( chooserObject, searchterm, response ) {
 *          response( [ 'result1', 'result2' ] );
 *     }
 *
 * The method takes the following parameters
 *
 *     chooserObject:  reference to the publicationChooser object itself
 *     searchterm:     string with the text that is entered in the textfield
 *     response:       reference to the function to be called when the results
 *                     have been fetched. The parameters are described above
 *
 * After the function has been created, it must be told to the publicationChooser
 * class.
 *
 *     PublicationChooser.prototype.availableDBs[ "example" ] = {
 *         'source': sourceExample
 *     };
 *
 * This line will provide the PublicationChooser class with the possibility to
 * search in the example database (<input rel="publication-example">), using the
 * function sourceExample. N.B. You can't use a dash in the name of the database
 * since that is used to separate different parts of the rel="" value.
 *
 * In order to do something with the selected result, two other methods can be
 * written;
 *
 *     selectExample = function( chooserObject, inputElement, event, ui ) {}
 *     closeExample = function( chooserObject, inputElement, event, ui ) {}
 *
 * The select method is calles when a user selects an item. The close method is
 * called when a user closes the dropdown, but did not select an item. The parameters
 * are:
 *
 *     chooserObject:  reference to the publicationChooser object itself
 *     inputElement:   reference to the input element
 *     event:          the event that has triggered the call. See http://docs.jquery.com/UI/Autocomplete#events
 *     ui:             the ui reference from jquery. If an item is selected,
 *                     ui.item contains a reference to that item. See http://docs.jquery.com/UI/Autocomplete#events
 *
 * In order to show the elements in the list, there is a render method. This method
 * accepts the item to be rendered, and returns an HTML string to be shown in the list
 *
 *     renderExample = function( item ) {}
 *
 * Of course, these methods must also be presented to the PublicationChooser object.
 *
 *     PublicationChooser.prototype.availableDBs[ "example" ] = {
 *         'source': sourceExample,
 *         'select': selectExample,
 *         'close':  closeExample,
 *         'render': renderExample
 *     };
 *
 * Example:
 * --------
 *
 *    sourceProgrammingLanguages = function( chooserObject, searchterm, response ) {
 *        var availableTags = ["c++", "java", "php", "coldfusion", "javascript", "asp", "ruby", "python", "c", "scala", "groovy", "haskell", "perl"];
 *        var foundTags = $.grep( availableTags, function( tag, i ){
 *            return ( tag.indexOf( searchterm ) !=-1 );
 *        } );
 *
 *        response( foundTags );
 *    }
 *
 *    selectProgrammingLanguages = function( chooserObject, inputElement, event, ui ) {
 *        alert( "Selected item: " + ui.item.value );
 *    }
 *
 *    closeProgrammingLanguages = function( chooserObject, inputElement, event, ui ) {
 *       alert( "No selected item" );
 *    }
 *
 *    renderProgrammingLanguages = function( item ) {
 *        return item.value;
 *    };
 *
 *    PublicationChooser.prototype.availableDBs[ "programminglanguages" ] = {
 *        'source': sourceProgrammingLanguages,
 *         'close' : closeProgrammingLanguages,
 *         'select': selectProgrammingLanguages,
 *        'render': renderProgrammingLanguages
 *     };
 *
 * Currently, only Pubmed is supported using publication-chooser.pubmed.js.
 * Other database can easily be supported. See for example
 * http://www.ncbi.nlm.nih.gov/bookshelf/br.fcgi?book=helpeutils&part=chapter2&rendertype=table&id=chapter2.chapter2_table1
 * for supported database by the esearch engine.
 *
 * @author      Robert Horlings
 * @since       20100526
 * @package     wizard
 * @requires    jquery, jquery-ui
 * @see         http://www.ncbi.nlm.nih.gov/bookshelf/br.fcgi?book=helpeutils
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
function PublicationChooser() {
}

/**
 * initialize object
 */
PublicationChooser.init = function( events ) {
    // find all ontology elements
    $("input[rel*='publication']").each(function() {
        new PublicationChooser().initAutocomplete( this, events );
    });
};

PublicationChooser.prototype = {
    minLength       : 3,        // minimum input length before launching Ajax request
    cache           : [],       // ontology cache
    maxResults      : 8,        // Max number of results retrieved
    database        : '',       // Default database to be used. As for now, also the only possible database
    availableDBs    : {},       // Available databases, filled by extensions. Key is databasename and value is reference to method to be called
    events          : {},       // Stores the events to fire
    
    /**
     * initialize the ontology autocompleter
     * @param element
     */
    initAutocomplete: function( element, customEvents ) {
        var that = this
        var inputElement = $(element);
        var selected = false;

        // Add spinner element
        if( !baseUrl ) {
            var baseUrl = '..';
        }
        var spinnerEl = document.createElement( 'img' );
        spinnerEl.setAttribute( 'id', inputElement.attr( 'id' ) + '_spinner' );
        spinnerEl.setAttribute( 'src', baseUrl + '/images/spinner.gif' );
        spinnerEl.setAttribute( 'style', 'margin-left: 5px;');

        // Add the element next to the input box
        inputElement.after( spinnerEl );
        $( spinnerEl ).hide();

        // Also add a 'not found' message
        var notfoundSpan = document.createElement( 'span' );
        notfoundSpan.setAttribute( 'id', inputElement.attr( 'id' ) + '_notfound' );
        
        var imgEl = document.createElement( 'img' );
        imgEl.setAttribute( 'id', inputElement.attr( 'id' ) + '_delete' );
        imgEl.setAttribute( 'src', baseUrl + '/plugins/famfamfam-1.0.1/images/icons/delete.png' );
        imgEl.setAttribute( 'style', 'margin-left: 5px; margin-right: 5px; ');

        notfoundSpan.appendChild( imgEl );
        notfoundSpan.appendChild( document.createTextNode( "No publications found." ) );
        
        // Add the element next to the spinner image
        $( spinnerEl ).after( notfoundSpan );
        $( notfoundSpan ).hide();

        // determine what database to use
        var values = inputElement.attr('rel').split("-");

        // Check which database the user wants
        if( values.length > 1 ) {
            // check for supported databases
            if( this.availableDBs[ values[ 1 ] ] ) {
                this.database = values[1];
                this.events = this.availableDBs[ this.database ];
            } else {
                this.database = values[1] + " (custom)";
                this.events = {};
            }
        } else {
            this.database = "(custom)";
            this.events = {};
        }

        // Add custom events to this object
        if( customEvents ) {
            $.each( customEvents, function( id, func ) {
                that.events[ id ] = func;
            })
        }
        
        // If no 'source' function is defined, nothing can be searched for
        if( !this.events[ 'source' ]) {
            alert( 'Database ' + this.database + ' not supported. Using none.' );
        }

        // Initialize cache for this element
        this.cache[ this.database ] = [];

        // Put the autocomplete function on the input field. See jquery-ui
        inputElement.autocomplete({
            minLength: that.minLength,
            delay: 300,

            source: function(request, response) {
                // Before the response function is executed, we have to 
                // check whether there are results or nog
                var improvedResponse = function( objects ) {
                    if( objects == null ) {
						objects = new Array();
					}
					if( objects.length == 0 ) {
                        $( '#' + inputElement.attr( 'id' ) + '_spinner' ).hide();
                        $( '#' + inputElement.attr( 'id' ) + '_notfound' ).show();
                    }
                    
                    response( objects );
                }

                var q = $.trim(request.term);

                // Check the cache first
                if ( that.cache[ that.database ][ q ]) {
                    // yeah, lucky us! ;-P
                    improvedResponse(that.cache[ that.database ][ q ]);
                } else {
                    if( that.database != "" && that.events[ 'source' ] ) {
                       that.events[ 'source' ]( that, q, improvedResponse );
                    }
                }
            },
            search: function(event, ui ) {
                that.selected = false;
                $( '#' + inputElement.attr( 'id' ) + '_spinner' ).show();
                $( '#' + inputElement.attr( 'id' ) + '_notfound' ).hide();
            },
            open: function(event, ui ) {
                $( '#' + inputElement.attr( 'id' ) + '_spinner' ).hide();
                $( '#' + inputElement.attr( 'id' ) + '_notfound' ).hide();
            },
            select: function(event, ui) {
                // mark that the user selected a suggestion
                that.selected = true;

                if( that.database != "" && that.events[ 'select' ] ) {
                    that.events[ 'select' ]( that, inputElement, event, ui );
                }
            },
            close: function(event, ui) {
                if( !that.selected ) {
                    if( that.database != "" && that.events[ 'close' ] ) {
                        that.events[ 'close' ]( that, inputElement, event, ui );
                    }

                    if( inputElement.closePublication ) {
                        inputElement.closePublication( that, event, ui );
                    }
                }
            }

        });

        // Enable custom rendering if wanted. Otherwise use renderPublication method
        var renderMethod;
        if( this.database != "" && this.availableDBs[ that.database ] && this.availableDBs[ that.database ][ 'render' ] ) {
            renderMethod = this.availableDBs[ that.database ][ 'render' ]; 
        } else {
            renderMethod = this.renderPublication;
        }

        inputElement.data( "autocomplete" )._renderItem = function( ul, item ) { 
            return that.renderAutoCompleteText( ul, item, renderMethod( item ) );
        };

    },

    /**
     * Renders a piece of text as a autocomplete menu item
     */
    renderAutoCompleteText: function( ul, item, text ) {
        return $( "<li></li>" )
                .data( "item.autocomplete", item )
                .append( "<a>" + text + "</a>" )
                .appendTo( ul );
    },

    /**
     * Default rendering for publications in the autocomplete list
     */
    renderPublication: function( item ) {
        return item.title + "<br><span style='font-size: 7pt;' class='authors'>" + item.authors.join( ', ' )+ "</span>";
    },

    /**
     * Set the value of a particular DOM element
     * @param inputElement
     * @param name
     * @param value
     */
    setInputValue: function(inputElement, name, value) {
        var elementName = inputElement.attr('name') + '-' + name;
        var searchElement = inputElement.parent().find("input[name='" + elementName + "']");

        // got a text/hidden field in the DOM?
        if (searchElement.size() > 0) {
            // yeah, set it
            $(searchElement[0]).val(value);
        } else {
            // no, dynamically insert it after the input element
            inputElement.after('<input type="hidden" name="' + elementName + '" value="' + value + '"/>');
        }
    }

}