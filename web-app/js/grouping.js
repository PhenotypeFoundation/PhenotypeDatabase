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
/*
Grouping.prototype = {
    itemsIdentifier:    null,
    itemIdentifier:     null,
    groupsIdentifier:   null,
    groupIdentifier:    null,

    init: function(itemsIdentifier, itemIdentifier, groupsIdentifier, groupIdentifier) {
        var that = this;

        this.itemsIdentifier    = itemsIdentifier;
        this.itemIdentifier     = itemIdentifier;
        this.groupsIdentifier   = groupsIdentifier;
        this.groupIdentifier    = groupIdentifier;

        this.initItems();
    },

    initItems: function() {
        $(this.itemsIdentifier).
    }
}
*/

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

