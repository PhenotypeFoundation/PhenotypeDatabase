/*
 * Create autocomplete with a select
 * From : http://jqueryui.com/demos/autocomplete/#combobox
 */
(function( $ ) {
    $.widget( "ui.combobox", $.ui.autocomplete, {
        _create: function() {
            var self = this,
                select = this.element.hide(),
                selected = select.children( ":selected" ),
                value = selected.val() ? selected.text() : "";
            var input = this.input = $( "<input>" )
                .insertAfter( select )
                .val( value )
                .autocomplete({
                    delay: 0,
                    minLength: 0,
                    source: function( request, response ) {
                        var matcher = new RegExp( $.ui.autocomplete.escapeRegex(request.term), "i" );
                        var currCat = "";
                        response( select.find( "option, optgroup" ).map(function() {
                            if(this.nodeName=="OPTION") {
                                var text = $( this ).text();
                                if ( this.value && (( !request.term || matcher.test(text) ) || matcher.test(currCat)) ) {
                                    return {
                                        category: currCat,
                                        label: text.replace(
                                            new RegExp(
                                                "(?![^&;]+;)(?!<[^<>]*)(" +
                                                $.ui.autocomplete.escapeRegex(request.term) +
                                                ")(?![^<>]*>)(?![^&;]+;)", "gi"
                                            ), "$1" ),
                                        value: text,
                                        option: this
                                    };
                                }
                            } else {
                                currCat = $( this ).attr("label");
                            }
                        }) );
                    },
                    select: function( event, ui ) {
                        ui.item.option.selected = true;
                        self._trigger( "selected", event, {
                            item: ui.item.option
                        });
                        select.trigger("change");
                    },
                    change: function( event, ui ) {
                        if ( !ui.item ) {
                            var matcher = new RegExp( "^" + $.ui.autocomplete.escapeRegex( $(this).val() ) + "$", "i" ),
                                valid = false;
                            select.children( "option" ).each(function() {
                                if ( $( this ).text().match( matcher ) ) {
                                    this.selected = valid = true;
                                    return false;
                                }
                            });
                            if ( !valid ) {
                                // remove invalid value, as it didn't match anything
                                $( this ).val( "" );
                                select.val( "" );
                                input.data( "ui-autocomplete" ).term = "";
                                return false;
                            }
                        } 
                    }
                });

            input.data( "ui-autocomplete" )._renderMenu = function( ul, items ) {
                var self = this, currentCategory = "";
                $.each( items, function( index, item ) {
                    if ( item.category != currentCategory ) {
                        ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
                        currentCategory = item.category;
                    }
                    self._renderItem( ul, item );
                });
            };

            input.data( "ui-autocomplete" )._renderItem = function( ul, item ) {
                return $( "<li></li>" )
                    .data( "ui-autocomplete-item", item )
                    .append( "<a>" + item.label + "</a>" )
                    .appendTo( ul );
            };
        },

        destroy: function() {
            this.input.remove();
            this.element.show();
            $.Widget.prototype.destroy.call( this );
        }
    });
})( jQuery );
