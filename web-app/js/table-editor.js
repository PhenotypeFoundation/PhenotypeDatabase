/**
 * table editor javascript class
 *
 * @author      Jeroen Wesbeek
 * @since       20100204
 * @package     wizard
 * @requires    jquery, jquery-ui
 *
 * Revision information:
 * $Rev: 959 $
 * $Author: j.a.m.wesbeek@umail.leidenuniv.nl $
 * $Date: 2010-10-20 21:13:14 +0200 (Wed, 20 Oct 2010) $
 */
function TableEditor() {
}
TableEditor.prototype = {
    options : {
        tableIdentifier     :   'div.table',
        headerIdentifier    :   'div.header',
        rowIdentifier       :   'div.row',
        columnIdentifier    :   'div.column, div.firstColumn',
		initialize			:	0,
		minRowSliderCount	:	20,
		scrollTimeout		:	200,
		snapHeader			:	true,
		verticalSlider		:	true
    },
	allSelected				: false,
	tables					: [],
	date					: new Date(),
	timed					: null,
	window					: $(window),
	document				: $(document),
	scrollTop				: 0,
	actionCallback			: [],

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
			var table = $(this);

			// initialize table
			that.initializeTable(table);

			// cache table object
			that.tables[ that.tables.length ] = table;
		});

		// handle window scroll event if we have tables
		if (that.tables.length && that.options.snapHeader) {
			that.window.scroll(function () {
					// to do: clear timeout if it is set
					if (that.timed) clearTimeout(that.timed);

					// and set a new timeout
					if ($.browser.msie) {
						// ie does not support setTimeout arguments
						that.timed = setTimeout(function() { that.onScrollEnd(); }, that.options.scrollTimeout);
					} else {
						// but the rest of the browsers that actually
						// work as expected DO accept arguments
						that.timed = setTimeout(function(thisObj) { thisObj.onScrollEnd(); }, that.options.scrollTimeout, that);
					}
			});
		}

		return this;
    },

	/**
	 * initialize table
	 * @param table
	 */
	initializeTable: function(table) {
		var that = this;

		// handle key presses
		this.attachInputElements(table);

		// Bind table wide mouseclicks (lighter implementation than
		// binding / unbinding select elements directly)
		// This only works for mozilla browsers, not for ie and
		// webkit based browsers
		if ($.browser.mozilla) {
			table.bind('click', function(event) {
				var target	= event.target;
				var element	= $(target);
				var type	= target.localName;
				var pe		= (type=='option') ? $(target.parentNode) : element;
				var peType	= pe.attr('type');
				var column	= pe.parent();
				var row		= column.parent();

				// did the user select anything?
				if (type == 'option') {
					// the user selected an option in a select
					that.replicateData(table,row,column,pe.attr('type'),target.value);
				} else if (peType == 'checkbox') {
					that.replicateData(table,row,column,pe.attr('type'),target.checked);
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
					that.attachCheckboxElementsInRow(table, ui.selected);
				}

				that.attachSelectElementsInRow(table, ui.selected);
				that.attachDatepickerOnChangeInRow(table, ui.selected);
			},
			unselected: function(event, ui) {
				that.detachColumnHandlers(ui.unselected);
				//that.cleanup(table);
			}
		});

		// add 'select all' buttons
		this.addSelectAllButton(table);

		// style the table
		this.resizeTableColumns(table);

		// handle actions
		this.attachActionHandlers(table);
	},

	/**
	 * register a callback function for action callbacks
	 *
	 * usage:
	 * 	tableEditor.registerActionCallback('deleteEvent', function() {
	 * 		if (confirm('are you sure you want to delete ' + ((this.length>1) ? 'these '+this.length+' events?' : 'this event?'))) {
	 * 			$('input[name="do"]').val(this);
	 * 			<af:ajaxSubmitJs name="deleteEvent" afterSuccess="onPage()" />
	 * 		}
	 * 	});
	 *
	 * @param string
	 * @param function
	 */
	registerActionCallback: function(name, callback) {
		this.actionCallback[ name ] = callback;
	},

	/**
	 * attach action handlers
	 *
	 * usage:
	 * <div class="row" identitief="${event.getIdentifier()}">
	 *  <input type="button" value="" action="deleteEvent" class="delete" identifier="${event.getIdentifier()}" />
	 *  ...
	 * </div>
	 *
	 * @param table
	 */
	attachActionHandlers: function(table) {
		var that = this;

		// find all actions in this table
		$('[action]', table).each(function() {
			var action	= $(this);
			var id		= (action.attr('identifier')) ? action.attr('identifier') : '';
			action.bind('click', function() {
				that.handleAction(action.attr('action'),id,table);
			});
		});
	},

	/**
	 * handle an action click
	 *
	 * @param callbackFunctionName
	 * @param id
	 * @param table
	 */
	handleAction: function(callbackFunctionName, id, table) {
		var ids		= [];
		var exists	= false;
		var callback= this.actionCallback[callbackFunctionName];

		// gather identifiers
		$('.ui-selected, .table-editor-selected', table).each(function() {
			var row		= $(this);
			var rowId	= row.attr('identifier');

			if (rowId) {
				ids[ ids.length ] = rowId;
				if (rowId == id) exists = true;
			}
		});

		// got id's?
		if (callback) {
			if (ids.length && exists) {
				//eval(callbackFunction+"(ids);");
				callback.call(ids);
			} else if (id) {
				//eval(callbackFunction+"([id]);");
				callback.call([id]);
			}
		}
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

		// iterate through all select elements in the row
		$('select', row).each(function() {
			var element = $(this);
			var type	= 'select';
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
	 * attach event handlers for checkboxes in a row
	 * @param table
	 * @param row
	 */
	attachCheckboxElementsInRow: function(table, row) {
		var that = this;

		// iterate through all checkboxes in the row
		$('input:checkbox', row).each(function() {
			var element	= $(this);
			var type	= 'checkbox';
			var column	= element.parent();
			var row		= element.parent().parent();

			element.bind('click.tableEditor',function() {
				that.replicateData(table,row,column,type,element.is(':checked'));
			});
		});
	},

	/**
	 * attach change event handlers to all input fields that have date/datetime pickers
	 * so we can replicate those values as well
	 * @param table
	 * @param row
	 */
	attachDatepickerOnChangeInRow: function(table, row) {
		var that = this;

		// iterate through all date / datetime elements in the row
		$("input[type=text][rel$='date'], input[type=text][rel$='datetime']", row).each(function() {
			var element	= $(this);
			var type	= element.attr('type');
			var column	= element.parent();
			var row		= element.parent().parent();

			element.bind('change.tableEditor', function() {
				that.replicateData(table,row,column,type,element.val());
			});
		});
	},

	/**
	 * detach event handlers for specific fields in row
	 * @param row
	 */
	detachColumnHandlers: function(row) {
		if (!this.allSelected) {
			$("select, input[type=text][rel$='date'], input[type=text][rel$='datetime'], input:checkbox", row).each(function() {
				// unbind table editor event handlers
				$(this).unbind('.tableEditor');
			});
		}
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
	 * replicate form data to selected rows
	 * @param table
	 * @param row
	 * @param column
	 * @param type
	 * @param value
	 */
	replicateData: function(table, row, column, type, value) {
		var that 			= this;
		var columnNumber	= this.getColumnNumber(row, column);
		var inputSelector	= "";

		if (!type) return;

		// determine inputSelector
		switch (type) {
			case('text'):
				inputSelector = 'input';
				break;
			case('select'):
			case('select-one'):
				inputSelector = 'select';
				break;
			case('checkbox'):
				inputSelector = 'input:checkbox';
				break;
			default:
				inputSelector = 'input';
				break;
		}

		// as of jQuery 1.4.4 setting an option using 'val' does not work
		// find the selected index
		if (inputSelector == 'select') {
			var selectedIndex = $(inputSelector, $(that.options.columnIdentifier, row)[columnNumber])[0].selectedIndex;
		}

		// only replicate if source row is also selected
		if (row.hasClass('ui-selected') || row.hasClass('table-editor-selected')) {
			//console.log('replicating column '+columnNumber+' of type '+type+' : '+value);

			// find selected rows in this table
			$('.ui-selected, .table-editor-selected', table).each(function() {
				// don't replicate to source row
				if ($(this)[0] != row[0]) {
					// find input elements
					$(inputSelector, $(that.options.columnIdentifier, $(this))[columnNumber]).each(function() {
						// set value
						switch (type) {
							case('checkbox'):
								$(this).attr('checked', value);
								break;
							case('select'):
								// bug in jQuery 1.4.4, $(this).val(...) does not work
								$(this)[0].options[selectedIndex].selected = true;
								break;
							default:
								$(this).val(value);
								break;
						}
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
		//this.cleanup(table);

		// select and bind row
		$(this.options.rowIdentifier, table).each(function() {
			var row = $(this);

			// add class
			if (!row.hasClass('table-editor-selected'))
				row.addClass('table-editor-selected');

			// detach handlers in this row
			that.detachColumnHandlers(row);

			// on ie and webkit based browsers we need
			// to handle mouse clicks differently
			if (!$.browser.mozilla) {
				that.attachCheckboxElementsInRow(table, row);
			}
			that.attachSelectElementsInRow(table, row);
			that.attachDatepickerOnChangeInRow(table, row);
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
			this.allSelected = false;
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
				that.detachColumnHandlers(row);
			}
        });

        // and unset flag
        this.allSelected = false;
    },

    /**
     * resize the table columns so that they lineup properly
     * @param table
	 */
    resizeTableColumns: function(table) {
		var header	= $(this.options.headerIdentifier, table);
		var width	= 20;		// default column width
		var column	= 0;
		var columns	= [];
		var resized	= [];

		// calculate total width of elements in header
		header.children().each(function() {
			// calculate width per column
			var c = $(this);

			// if a column header contains help icons / helptext, make sure
			// to handle them before initializing the table otherwise the
			// widths are calculations are off...
			var columnWidth	= c.outerWidth(true);

            width += columnWidth;

			// remember column
			resized[ column ] = (c.attr('rel') == 'resized');
			columns[ column ] = c.width();
			column++;
		});

        //Prevent setting the width of row smaller than its parent
        if(header.parent().css('width').replace("px", "") < width) {
            header.css({ width: width + 'px' });
        }

		// set table row width and assume column widths are
		// identical to those in the header (css!)
		$(this.options.rowIdentifier, table).each(function() {
			var row = $(this);
			var column = 0;
			row.children().each(function() {
				var child = $(this);
				child.css({ width: columns[ column] + 'px' });
				if (resized[ column ]) {
					$(':input', child).each(function() {
						$(this).css({width: (columns[ column ] - 10) + 'px'});
					});
				}
				column++;
			});

            //Prevent setting the width of row smaller than its parent
            if(row.parent().css('width').replace("px", "") < width) {
			    row.css({ width: width + 'px' });
            }
		});

		// add sliders?
		if (header.width() > table.width()) {
			// add vertical slider?
			if (this.options.verticalSlider) {
				// yes
				this.addVerticalSlider(table);
			} else {
				// no, add a top and a bottom slider
            	if ($(this.options.rowIdentifier, table).size() > this.options.minRowSliderCount) this.addSlider(table, 'before');
            	this.addSlider(table, 'after');
			}
		}
    },

   	/**
   	 * add a slider to a table (either before or after the table)
   	 * @param table
   	 * @param location
	 */
   	addSlider: function(table, location) {
   		var that	= this;
   		var header	= $(this.options.headerIdentifier, table);
		var sliderContainer = $(document.createElement('div'));

		// add to table
		sliderContainer.addClass('sliderContainer');

		// where?
		if (location == 'before') {
			table.before(sliderContainer);
		} else if (location == 'after') {
			table.after(sliderContainer);
		}

		// initialize slider
		sliderContainer.slider({
			value	: 1,
			min		: 1,
			max		: header.width() - table.width(),
			step	: 1,
			slide	: function(event, ui) {
				$(that.options.headerIdentifier + ', ' + that.options.rowIdentifier, table).css({ 'margin-left': ( 1 - ui.value ) + 'px' });
			}
		});
   	},

	/**
	 * add a vertical slider to a table
	 * @param table
	 */
	addVerticalSlider: function(table) {
		// add a vertical slider to the header
		var that			= this;
		var header			= $(this.options.headerIdentifier, table);
		var sliderContainer	= $(document.createElement('div')).addClass('verticalSliderContainer');
		var max				= header.width() - table.width();

		// add to header
		table.before(sliderContainer);

		// initialize slider
		sliderContainer.slider({
			orientation: "vertical",
			value	: max,
			min		: 1,
			max		: max,
			step	: 1,
			slide	: function(event, ui) {
				$(that.options.headerIdentifier + ', ' + that.options.rowIdentifier, table).css({ 'margin-left': ( 0 - (max - ui.value) ) + 'px' });
			}
		});
	},

	/**
	 * handle end of page scroll
	 * @void
	 */
	onScrollEnd: function() {
		var that		= this;
		var time		= this.date.getTime();
		var top			= this.window.scrollTop();
		var bottom		= top + this.window.height();
		var direction	= (top > this.scrollTop) ? 'down' : 'up';

		// make sure we only fire once
		if (top > this.scrollTop || top < this.scrollTop) {
			this.scrollTop	= top;

			// iterate through tables
			for (var i in that.tables) {
				var table		= that.tables[i];
				var offset		= table.offset();
				var tableTop	= offset.top;
				var tableBottom	= tableTop + table.height();
				var header		= $(that.options.headerIdentifier, table);

				// check if table is visible
				if (tableTop <= bottom && tableBottom >= top) {
					var topRow		= null;
					var topRowNo	= 0;
					var headerNo	= 0;
					var count		= 0;

					$(that.options.rowIdentifier + ", " + that.options.headerIdentifier, table).each(function() {
						var row			= $(this);
						var rowTop		= row.offset().top;
						var rowBottom	= rowTop + row.height();
						count++;

						// find the top most visible row
						if (
							!topRow &&
							(
								(rowTop >= top && rowTop <= bottom)
							)
						) {
							topRow = row;
							topRowNo = count;
						}

						// is this the header?
						if (row[0] == header[0]) headerNo = count;
					});

					// got a topRow?
					if (topRow) {
						// check if we need to move the header
						if (headerNo == topRowNo) {
							// fine as it is, do nothing
						} else if (headerNo > topRowNo) {
							// we move the header up
							topRow.before(header);

							// reposition vertical slider?
							if (that.options.verticalSlider) that.repositionVerticalSlider(table,header);
						} else if (headerNo < topRowNo) {
							// we move the header down
							topRow.after(header);

							// reposition vertical slider?
							if (that.options.verticalSlider) that.repositionVerticalSlider(table,header);
						}
					}
				}
			}
		}
	},

	/**
	 * move slider to the same position as the table header
	 * @param table
	 * @param header
	 */
	repositionVerticalSlider: function(table, header) {
		// get vertical slider
		var slider	= table.prev();

		// move slider to header top
		slider.animate( { top: header.offset().top } , 200);
	}
}