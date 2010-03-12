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
    tableIdentifier:    null,
    rowIdentifier:      null,
    columnIdentifier:   null,
    selected:           null,

    /**
     * initialize object
     * @param tableIdentifier
     * @param rowIdentifier
     * @param columnIdentifier
     */
    init: function(tableIdentifier, rowIdentifier, columnIdentifier) {
        // store parameters globally
        this.tableIdentifier = tableIdentifier;
        this.rowIdentifier = rowIdentifier;
        this.columnIdentifier = columnIdentifier;

        // got table(s)?
        var table = $(tableIdentifier);
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
        var that = this;

        $(table).selectable({
            filter: this.rowIdentifier,
            stop: function() {
                // remember selected rows
                that.selected = $('.ui-selected', table);

                // bind on change to columns in rows
                that.selected.each(function() {
                    that.attachColumnHandlers(this,table);
                })
            }
        });
    },

    /**
     * attach handlers to the input elements in a table row
     * @param row
     */
    attachColumnHandlers: function(row,table) {
        var that = this;
        var count = 0;

        // define regular expressions
        var regAutoComplete = new RegExp("ui-autocomplete-input");

        $(this.columnIdentifier, $(row)).each(function() {
            var input = $(':input', $(this));
            // does this column contain an input field
            if (input) {
                var inputElement = $(input)
                var type = inputElement.attr('type');

                switch (type) {
                    case 'text':
                        // text input
                        var columnNumber = count;

                        // handle special cases
                        // if (inputElement.attr('rel') && regBp.test(inputElement.attr('rel'))) {
                        if (regAutoComplete.test(inputElement.attr('class'))) {
                            // this is a jquery-ui autocomplete field
                            inputElement.bind('autocompleteclose', function() {
                                // TODO: autocompletion deselects rows... which is what we don't want
                                //       to happen of course...
                                that.updateSingleInputElements(input, columnNumber, 'input');
                            })
                        } else {
                            // regular text element
                            inputElement.bind('keyup', function() {
                                that.updateSingleInputElements(input, columnNumber, 'input');
                            });
                        }
                        break;
                    case 'select-one':
                        // single select
                        var columnNumber = count;
                        inputElement.bind('change', function() {
                            that.updateSingleInputElements(input, columnNumber, 'select');
                        });
                        break;
                    case 'checkbox':
                        // checkbox
                        var columnNumber = count;
                        inputElement.bind('click', function() {
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
            $(that.columnIdentifier + ':eq(' + columnNumber + ') ' + elementSelector, $(this)).each(function() {
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
    }
}
