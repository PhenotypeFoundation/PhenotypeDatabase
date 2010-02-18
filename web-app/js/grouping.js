/**
 * grouping javascript class
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
function Grouping() {
}
Grouping.prototype = {
    itemsIdentifier:    null,
    itemIdentifier:     null,
    groupsIdentifier:   null,
    groupIdentifier:    null,
    addIdentifier:      null,
    removeIdentifier:   null,

    init: function(itemsIdentifier, itemIdentifier, groupsIdentifier, groupIdentifier, addIdentifier, removeIdentifier) {
        var that = this;

        this.itemsIdentifier    = itemsIdentifier;
        this.itemIdentifier     = itemIdentifier;
        this.groupsIdentifier   = groupsIdentifier;
        this.groupIdentifier    = groupIdentifier;
        this.addIdentifier      = addIdentifier;
        this.removeIdentifier   = removeIdentifier;

        this.initAdd();
        this.initRemove();
        this.initItems();
        this.initGroupItems();
        this.initGroups();
        //this.initGroups();
        
        console.log('bla')
    },

    initAdd: function() {
        var that = this;
        var add = $(this.addIdentifier);

        add.bind('mouseenter mouseleave',function() {
            $(this).toggleClass('add-hover');
        })

        add.bind('click',function() {
            that.addSelectedItemsToSelectedGroup();
        });
    },

    addSelectedItemsToSelectedGroup: function() {
        var that = this;
        var groups = $(this.groupIdentifier, $(this.groupsIdentifier))
        var group = $('.ui-selected', groups)
        console.log(groups)
        console.log(group)
    },

    initRemove: function() {
        var that = this;
        var remove = $(this.removeIdentifier);

        remove.bind('mouseenter mouseleave',function() {
            $(this).toggleClass('remove-hover');
        })

        remove.bind('click',function() {
            console.log('removveeeee!!!!');
        })
    },

    initItems: function() {
        var that = this;
        console.log($(this.itemsIdentifier).first());
        //$(this.itemsIdentifier).
        $(this.itemsIdentifier).first().selectable({
            filter: that.itemIdentifier,
            stop: function() {
                // done selecting, make all selected items draggable
                //that.makeDraggable(this);
            }
        });
    },

    initGroups: function() {
        var that = this;

        // init group items
        this.initGroupItems();
        
        // init groups
        $(this.itemsIdentifier).first().selectable({
            filter: that.groupIdentifier,
            stop: function() {

            }
        })
    },

    initGroupItems: function() {
        var that = this;

        $(this.groupsIdentifier).selectable({
            filter: that.groupIdentifier
        })
    }
}
/*

Grouping.prototype = {
    itemsIdentifier:    null,
    itemIdentifier:     null,
    groupsIdentifier:   null,
    groupIdentifier:    null,

    init: function(itemsIdentifier, itemIdentifier, groupsIdentifier, groupIdentifier) {
        this.itemsIdentifier    = itemsIdentifier;
        this.itemIdentifier     = itemIdentifier;
        this.groupsIdentifier   = groupsIdentifier;
        this.groupIdentifier    = groupIdentifier;

        // initialize grouping
        console.log('grouping initialized...');

        this.initializeGrouping();
    },

    initializeGrouping: function() {
        var that = this;

        // make all items selectable
        console.log($(this.itemsIdentifier))
        $(this.itemsIdentifier).selectable({
            filter: that.itemIdentifier,
            stop: function() {
                // done selecting, make all selected items draggable
                that.makeDraggable(this);
            }
        });

        // make all groups droppable
        this.makeDroppable();
    },

    // make all groups droppable
    makeDroppable: function() {
        var that = this;
        $(this.groupsIdentifier + ' > ' + this.groupIdentifier).each(function() {
            console.log(this);
            var g = this;
            var G = $(g)
            G.droppable({
                drop: function(event, ui) {
                    var list = $(that.itemsIdentifier,G).length ? $(that.itemsIdentifier,G) : $('<div class="items"/>').appendTo(G);

                    // append the dropped subjects to the group
                    $('.ui-selected', $(that.itemsIdentifier)).each(function() {
                        var E = $(this);

                        // add to list
                        E.appendTo(list);

                        // make sure they are sized and positioned properly
                        E.css({'left':'0px','top':'0px'});
                        G.css({'height': (G.height() + E.height()) + 'px'});
                    })
                }
            })
        })
    },

    // make all selected items draggable
    makeDraggable: function(element) {
        var that = this;
        var draggables = $('.ui-selected', element);
        var dl = draggables.length;
        var undraggables = $(':not(.ui-selected)', element);

        // walk through selected items
        draggables.each(function() {
            var d = this;
            var D = $(this);
            var c = D.html();

            // make it draggable
            $(this).draggable({
                revert: 'invalid',
                corsor: 'move',
                start: function(event,ui) {
                    // start dragging
                    that.groupDraggables(draggables,d)
                    D.html(dl + ' item' + ((dl>1) ? 's' : ''))
                },
                stop: function(event,ui) {
                    // stop dragging
                    that.ungroupDraggables(draggables,d);
                    D.html(c);
                }
            });
        })

        // ...and all unselected items not draggable
        undraggables.each(function() {
            $(this).draggable('destroy');    
        })
    },

    // hide selectables that are not being dragged, thus
    // visually grouping them in the draggable that is
    // being dragged
    groupDraggables: function(draggables,draggedElement) {
        draggables.each(function() {
            if (this != draggedElement) {
                $(this).animate({opacity: 0},200);
            }
        })
    },

    // make all hidden selectables visible agains
    ungroupDraggables: function(draggables,draggedElement) {
        draggables.each(function() {
           if (this != draggedElement) {
                $(this).animate({opacity: 100},200);
           }
        })
    }
}
*/