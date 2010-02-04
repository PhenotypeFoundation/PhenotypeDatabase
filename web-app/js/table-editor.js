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
function TableEditor() { }
TableEditor.prototype = {
    tableIdentifier:    null,
    rowIdentifier:      null,
    columnIdentifier:   null,

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

    initializeTable: function(table) {
        var that = this;

        $(table).selectable({
            filter: this.rowIdentifier,
            stop: function() {
                // bind on change to columns in rows
                $('.ui-selected', table).each(function() {
                    that.attachColumnHandlers(this);
                })
            }
        });
    },

    attachColumnHandlers: function(row) {
        var that = this;
        var count = 0;
        $(this.columnIdentifier, $(row)).each(function() {
            var input = $(':input', $(this));

            // does this column contain an input field
            if (input) {
                var type = $(input).attr('type');

                switch (type) {
                    case 'text':
                        // text input
                        var columnNumber = count;
                        $(input).bind('keyup', function() {
                            that.updateSingleInputElements(input, columnNumber, 'input');
                        })
                        break;
                    case 'select-one':
                        // single select
                        var columnNumber = count;
                        $(input).bind('change', function() {
                            that.updateSingleInputElements(input, columnNumber, 'select');
                        })
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
        })
    },

    updateSingleInputElements: function(element, columnNumber, elementSelector) {
        var that = this;
        var e = $(element);
        var c = e.parent();
        var r = c.parent();
        var t = r.parent();

        // get value(s)
        // TODO for multiple selects...
        var v = e.val();

        // select all input elements in the selected rows
        $('.ui-selected', t).each(function() {
            $(that.columnIdentifier + ':eq(' + columnNumber + ') ' + elementSelector, $(this)).each(function() {
                if ($(this).val() != v) {
                    $(this).val(v);
                    // TODO support multiple selects
                }
            })

        })
    }
}
