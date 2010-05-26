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
 * after the dash is used for selecting the database to use. Currently, only
 * Pubmed is supported, that is also the default.
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
PublicationChooser.prototype = {
    minLength       : 3,        // minimum input length before launching Ajax request
    cache           : [],       // ontology cache
    maxResults      : 10,       // Max number of results retrieved
    database        : '',       // Default database to be used. As for now, also the only possible database
    availableDBs    : {},       // Available databases, filled by extensions. Key is databasename and value is reference to method to be called

    /**
     * initialize object
     */
    init: function() {
        var that = this;

        // find all ontology elements
        $("input[rel*='publication']").each(function() {
            that.initAutocomplete(this);
        });
    },

    /**
     * initialize the ontology autocompleter
     * @param element
     */
    initAutocomplete: function(element) {
        var that = this
        var inputElement = $(element);
        var selected = false;

        // determine what database to use
        var values = inputElement.attr('rel').split("-");
        if( values.length > 1 ) {
            // check for supported databases
            if( this.availableDBs[ values[ 1 ] ] ) {
                this.database = values[1];
            } else {
                alert( 'Database ' + values[1] + ' not supported. Using default: ' + this.database );
            }
        }

        // Put the autocomplete function on the input field. See jquery-ui
        inputElement.autocomplete({
            minLength: that.minLength,
            delay: 400,

            source: function(request, response) {
                        var q = $.trim(request.term);

                        // Check the cache first
                        if ( that.cache[ q ]) {
                            // yeah, lucky us! ;-P
                            response(that.cache[ q ]);
                        } else {
                            if( that.database != "" ) {
                                that.availableDBs[ that.database ]( that, q, response );
                            }
                        }
            },
            select: function(event, ui) {
                // mark that the user selected a suggestion
                selected = true;

                // option selected, set hidden fields
                var element = inputElement;

                // set hidden fields
                that.setInputValue(element, 'title', ui.item.title);
                that.setInputValue(element, 'authorsList', ui.item.authors.join( ', ' ));
                that.setInputValue(element, 'pubMedID', ui.item.id);
                that.setInputValue(element, 'doi', ui.item.doi);

                // remove error class (if present)
                element.removeClass('error');
            },
            close: function(event, ui) {
                // check if the user picked something from the ontology suggestions
                if (!selected) {
                    // no he didn't, clear the field(s)
                    var element = inputElement;

                    // set fields
                    inputElement.val('');
                    that.setInputValue(element, 'title', '');
                    that.setInputValue(element, 'authorsList', '');
                    that.setInputValue(element, 'pubMedID', '');

                    // add error class
                    element.addClass('error');
                }
            }

        })
        .data( "autocomplete" )._renderItem = this.renderPublication;
    },

    renderPublication: function( ul, item ) {
        return $( "<li></li>" )
                .data( "item.autocomplete", item )
                .append( "<a>" + item.title + "<br><span style='font-size: 7pt;' class='authors'>" + item.authors.join( ', ' )+ "</span></a>" )
                .appendTo( ul );
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