/**
 * table editor javascript class
 *
 * @author      Jeroen Wesbeek
 * @since       20100204
 * @package     wizard
 * @requires    jquery, jquery-ui
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
function TableEditor() {
}
TableEditor.prototype = {
    options : {
        tableIdentifier     :   'div.table',
        headerIdentifier    :   'div.header',
        rowIdentifier       :   'div.row',
        columnIdentifier    :   'div.column',
        selected            :   null
    },
    allSelected : false,

    /**
     * initialize object
     * @param options
     */
    init: function(options) {
        var that = this;
        
        // set class parameters
        if (options) {
            $.each(options, function(key,value) {
                that.options[key] = value;
            });
        }

        // got table(s)?
        var table = $(this.options.tableIdentifier);
        if (table) {
            // yes, initialize table(s)
            that = this;
            table.each(function() {
                that.initializeTable(this);
            });
        }
    },

    /**
     * initialize table
     * @param table
     */
    initializeTable: function(table) {
        var that  = this;
        var t = $(table);

        // initialize selectable
        t.selectable({
            filter: this.options.rowIdentifier,
            selected: function(event, ui) {
                that.cleanup(t);
                that.attachColumnHandlers(ui.selected);
            },
            unselected: function(event, ui) {
                that.cleanup(t);
                that.detachColumnHandlers(ui.selected);
            }
        });

        // insert a 'select all' element in the top-left header column
        var selectAllElement = $($(this.options.headerIdentifier + ':eq(0)', t ).find(':nth-child(1)')[0]);
        if (selectAllElement) {
            // set up the selectAll element
            selectAllElement
                .addClass('selectAll')
                .html('&nbsp;&nbsp;&nbsp;')
                .bind('click',function() {
                    that.selectAll(t);
                });

            // add a tooltip
            selectAllElement.qtip({
                content: 'leftMiddle',
                position: {
                    corner: {
                        tooltip: 'leftMiddle',
                        target: 'rightMiddle'
                    }
                },
                style: {
                    border: {
                        width: 5,
                        radius: 10
                    },
                    padding: 10,
                    textAlign: 'center',
                    tip: true,
                    name: 'blue'
                },
                content: "Click to select all rows in this table",
                show: 'mouseover',
                hide: 'mouseout',
                api: {
                    beforeShow: function() {
                        // not used at this moment
                    }
                }
            });
        }
    },

    /**
     * select all rows in the table
     * @param table
     */
    selectAll: function(table) {
        var that = this;
        this.cleanup(table);

        // select and bind row
        $(this.options.rowIdentifier, table).each(function() {
            var row = $(this);
            row.addClass('ui-selected');
            that.attachColumnHandlers(row);
        });

        // and set flag
        this.allSelected = true;
    },

    /**
     * check if the table needs cleanup
     * @param table
     */
    cleanup: function(table) {
        // check if all rows were selected
        if (this.allSelected) {
            // yes, then we need to cleanup. If we only used the jquery-ui
            // selector we wouldn't have to do so as it cleans up after
            // itself. But as we cannot programatically hook into the selector
            // we have to clean up ourselves. Perform a table cleanup and
            // unbind every handlers.
            this.deselectAll(table);
        }
    },

    /**
     * deselect all rows in the table
     * Note that this conflicts with the jquery selectable, so this is
     * NOT a user function, merely an 'underwater' function used for
     * consistency
     * @param table
     */
    deselectAll: function(table) {
        var that = this;

        // cleanup rows
        $(this.options.rowIdentifier, table).each(function() {
            var row = $(this);
            row.removeClass('ui-selected');
            that.detachColumnHandlers(row);
        });

        // and unset flag
        this.allSelected = false;
    },

    /**
     * de-attach input handlers for this row
     * @param row
     */
    detachColumnHandlers: function(row) {
        var that = this;

        $(this.options.columnIdentifier, row).each(function() {
            var input = $(':input', $(this));

            // does this column contain an input field
            if (input) {
                // unbind table editor event handlers
                $(input).unbind('.tableEditor');
            }            
        });
    },

    /**
     * attach handlers to the input elements in a table row
     * @param row
     */
    attachColumnHandlers: function(row) {
        var that = this;
        var count = 0;

        // define regular expressions
        var regAutoComplete = new RegExp("ui-autocomplete-input");

        $(this.options.columnIdentifier, row).each(function() {
            var input = $(':input', $(this));
            var inputElement = $(input)
            var type = inputElement.attr('type');

            // does this column contain an input field
            if (input && type) {
                // check field type
                switch (type) {
                    case 'text':
                        // text input
                        var columnNumber = count;

                        // handle special cases
                        // if (inputElement.attr('rel') && regBp.test(inputElement.attr('rel'))) {
                        if (regAutoComplete.test(inputElement.attr('class'))) {
                            // this is a jquery-ui autocomplete field
                            inputElement.bind('autocompleteclose.tableEditor', function() {
                                // TODO: autocompletion deselects rows... which is what we don't want
                                //       to happen of course...
                                that.updateSingleInputElements(input, columnNumber, 'input');
                            });
                        } else {
                            // regular text element
                            inputElement.bind('keyup.tableEditor', function() {
                                that.updateSingleInputElements(input, columnNumber, 'input');
                            });
                        }
                        break;
                    case 'select-one':
                        // single select
                        var columnNumber = count;
                        inputElement.bind('change.tableEditor', function() {
                            that.updateSingleInputElements(input, columnNumber, 'select');

                            // probably we want to bind these extra event handlers
                            // separately, but for now this will suffice :)
                            that.handleExtraEvents( inputElement );
                        });
                        break;
                    case 'checkbox':
                        // checkbox
                        var columnNumber = count;
                        inputElement.bind('click.tableEditor', function() {
                            that.updateSingleInputElements(input, columnNumber, 'input');
                        });
                        break;
                    case 'hidden':
                        // hidden is hidden :)
                        break;
                    case null:
                        // not an input field...
                        break;
                    default:
                        // oops, we need to extend this logic!
                        alert('unsupported element of type ' + type + ', please file a bug report containing this message and a screenshot for table-editor.js')
                        break;
                }
            }

            count++;
        });
    },

    /**
     * update all input elements in a selected column
     * @param element
     * @param columnNumber
     * @param elementSelector
     */
    updateSingleInputElements: function(element, columnNumber, elementSelector) {
        var that = this;
        var e = $(element);
        var c = e.parent();     // column
        var r = c.parent();     // row
        var t = r.parent();     // table
        var v = this.getValue(e);
        // TODO for multiples...

        // select all input elements in the selected rows
        $('.ui-selected', t).each(function() {
            $(that.options.columnIdentifier + ':eq(' + columnNumber + ') ' + elementSelector, $(this)).each(function() {
                var me = $(this)
                if (me.attr('type') != "hidden") {
                    var myVal = that.getValue(me);
                    if (myVal != v) {
                        that.setValue(me, v);
                    }
                }
            })
        })
    },

    /**
     * get the value /status of an input field based on it's type
     * @param input
     */
    getValue: function(input) {
        var i = $(input);

        switch (i.attr('type')) {
            case 'checkbox':
                return i.attr('checked');
                break;
            default:
                return i.val();
                break;
        }
    },

    /**
     * set the value / status of an input field based on it's type
     * @param input
     * @param value
     */
    setValue: function(input, value) {
        var i = $(input);

        switch (i.attr('type')) {
            case 'checkbox':
                return i.attr('checked', value);
                break;
            default:
                return i.val(value);
                break;
        }
    },

    /**
     * execute extra functions when binding to a particular event. The extra change
     * handlers are called after replicating template fields
     * example: <select ... tableEditorChangeEvent="console.log(element);" ... />
     * @param element
     */
    handleExtraEvents: function( element ) {
        // define parameters
        var events = ['change'];

        // check if we need to execute some more event handlers
        for ( var i=0; i < events.length; i++ ) {
            var call = element.attr('tableEditor' + events[ i ].substr(0, 1).toUpperCase() + events[ i ].substr(1).toLowerCase() + 'Event');
            if ( call ) {
                // yes, execute!
                eval( call );
            }
        }
    }
}