<%
/**
 * page header template
 *
 * This template is actually rendered by the AjaxflowTagLib using
 * the following tags:
 *
 * <af:pageHeader>
 *
 * @author Jeroen Wesbeek
 * @since  20101220
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
%>
<g:hiddenField name="do" value="" />
<h1><g:if test="${(study && study.getFieldValue('title'))}">${study.title}</g:if><g:else>${pages[page - 1].title}</g:else> (page ${page} of ${pages.size()})</h1>
<g:render template="common/tabs"/>
<div class="content">
<script type="text/javascript">
function TableEditor() {
}
TableEditor.prototype = {
    options : {
        tableIdentifier     :   'div.table',
        headerIdentifier    :   'div.header',
        rowIdentifier       :   'div.row',
        columnIdentifier    :   'div.column',
		initialize			:	0
    },
    tempSelectElement		: null,
    tempSelectValue			: '',
	allSelected				: false,

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

        // intitialize tables
		$(this.options.tableIdentifier).each(function() {
			that.initializeTable($(this));
		});
    },

	initializeTable: function(table) {
		var that = this;

		// handle key presses
		this.attachInputElements(table);

		// Bind table wide mouseclicks (lighter implementation than
		// binding / unbinding select elements directly)
		// This only works for mozilla browsers, not for ie and
		 // webkit based browsers
		if ($.browser.mozilla) {
			table.bind('click', function() {
				var element = $('select:focus');

				// did we select a select element?
				if (element.attr('type')) {
					that.tempSelectElement	= element;
					that.tempSelectValue	= $('option:selected',element).val();
				}
			});
			table.bind('mouseup', function() {
				var element = $('select:focus');
				var type	= element.attr('type');

				// did we select a select element?
				if (type) {
					var column	= element.parent();
					var row		= element.parent().parent();
					var value	= $('option:selected',element).val();

					// has the element changed?
					if (that.tempSelectElement && element[0] == that.tempSelectElement[0] && that.tempSelectValue != value) {
						// replicate data
						that.replicateData(table,row,column,type,value);
					}
				}
			});
		}

		// initialize selectable
		table.selectable({
			filter: that.options.rowIdentifier,
			selected: function(event, ui) {
				that.cleanup(table);

				// on ie and webkit based browsers we need
				// to handle mouse clicks differently
				if (!$.browser.mozilla) {
					that.attachSelectElementsInRow(table, ui.selected);
				}
			},
			unselected: function(event, ui) {
				that.cleanup(table);

				// on ie and webkit based browsers we need
				// to handle mouse clicks differently
				if (!$.browser.mozilla) {
					that.detachColumnHandler(ui.unselected);
				}
			}
		});

		// add 'select all' buttons
		this.addSelectAllButton(table);
	},

	/**
	 * handle keypresses anywhere inside this table
	 * @param table
	 */
	attachInputElements: function(table) {
		var that = this;

		// bind keypresses anywhere in the table in
		// 1. select elements
		// 2. input elements
		table.bind('keyup.tableEditor', function(e) {
			var element = $('input:focus,select:focus');
			var type	= element.attr('type');

			if (element.attr('type')) {
				var column	= element.parent();
				var row		= element.parent().parent();
				var value	= element.val();

				// replicate data
				that.replicateData(table,row,column,type,value);
			}
		});
	},

	/**
	 * attach event handlers for select elements in row
	 * @param table
	 * @param row
	 */
	attachSelectElementsInRow: function(table, row) {
		var that = this;

		// iterate through all select elements in the selected rows
		$('select', row).each(function() {
			var element = $(this);
			var type	= element.attr('type');
			var column	= element.parent();
			var row		= element.parent().parent();

			element.bind('change.tableEditor',function() {
				// replicate data
				var value = $('option:selected',element).val();
				if (value) that.replicateData(table,row,column,type,value);
			});
		});
	},

	/**
	 * detach event handlers for specific fields in row
	 * @param row
	 */
	detachColumnHandler: function(row) {
		$('select', row).each(function() {
			// unbind table editor event handlers
			$(this).unbind('.tableEditor');
		});
	},

	/**
	 * get the column number of a particular column in a row
	 *
	 * @param row
	 * @param column
	 * @return int
	 */
	getColumnNumber: function(row, column) {
		var count = 0;
		var columnNumber = 0;

		// find which column number we are
		row.children().each(function() {
			var childColumn = $(this);
			if (childColumn[0] == column[0]) {
				columnNumber = count;
			}
			count++;
		});

		return columnNumber;
	},

	/**
	 *
	 */
	replicateData: function(table, row, column, type, value) {
		var that 			= this;
		var columnNumber	= this.getColumnNumber(row, column);
		var inputSelector	= "";

		// determine inputSelector
		switch (type) {
			case('text'):
				inputSelector = 'input';
				break;
			case('select-one'):
				inputSelector = 'select';
				break;
			default:
				inputSelector = 'input';
				break;
		}

		// only replicate if source row is also selected
		if (row.hasClass('ui-selected') || row.hasClass('table-editor-selected')) {
			console.log('replicating column '+columnNumber+' of type '+type+' : '+value);

			// find selected rows in this table
			$('.ui-selected, .table-editor-selected', table).each(function() {
				// don't replicate to source row
				if ($(this)[0] != row[0]) {
					// find input elements
					$(that.options.columnIdentifier + ':eq(' + (columnNumber-1) + ') ' + inputSelector, $(this)).each(function() {
						// set value
						$(this).val(value);
					});
				}
			});
		}
	},

	/**
	 * insert a select all button in the first column of the header row
	 * @param table
	 */
	addSelectAllButton: function(table) {
		var that = this;

        // insert a 'select all' element in the top-left header column
        var selectAllElement = $($(this.options.headerIdentifier + ':eq(0)', table ).find(':nth-child(1)')[0]);
        if (selectAllElement) {
            // set up the selectAll element
            selectAllElement
                .addClass('selectAll')
                .html('&nbsp;&nbsp;&nbsp;')
                .bind('mousedown',function() {
                    that.selectAll(table);
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

		// clean up the table
		this.cleanup(table);

		// select and bind row
		$(this.options.rowIdentifier, table).each(function() {
			var row = $(this);
			row.addClass('table-editor-selected');

			// on ie and webkit based browsers we need
			// to handle mouse clicks differently
			if (!$.browser.mozilla) {
				that.attachSelectElementsInRow(table, row);
			}
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
			// unbind every handler.
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
            row.removeClass('table-editor-selected');

			// on ie and webkit based browsers we need
			// to handle mouse clicks differently
			if (!$.browser.mozilla) {
				that.detachColumnHandler(row);
			}
        });

        // and unset flag
        this.allSelected = false;
    }
}

handleWizardTable();
new TableEditor().init({
	tableIdentifier : 'div.tableEditor',
	rowIdentifier   : 'div.row',
	columnIdentifier: 'div.column',
	headerIdentifier: 'div.header'
});
</script>