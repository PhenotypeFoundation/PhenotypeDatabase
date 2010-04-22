/**
 * SelectAddMore javascript class
 *
 * This class adds one more option ('add more...') to select elements
 * with a given 'rel' tag. When clicked upon a jquery dialog launches
 * containing an iFrame to a local or remote url. Upon closing, this
 * class is able to perfom another action. For example, to refresh
 * the page to reflect the changed made in the dialog, or dynamically
 * extend the select box.
 *
 * Example:
 * --------
 * <select ... rel='cars' myList="1,2,3">
 *  <option value="car1">car1</option>
 *  ...
 *  <option value="carN">carN</option>
 * </select>
 * ...
 * new SelectAddMore().init({
 *      'rel'       : 'cars',
 *      'url'       : 'http://my.site/modifyCars',
 *      'label'     : 'modify cars...',
 *      'class'     : 'myCSSClass',
 *      'onClose'   : function(scope) {
 *          alert('cars closed : ' + scope );
 *      }
 * });
 *
 * @author      Jeroen Wesbeek
 * @since       20100420
 * @package     wizard
 * @requires    jquery, jquery-ui
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
function SelectAddMore() {
}
SelectAddMore.prototype = {
    // default options
    options: {
        rel     : 'addmore',
        url     : 'http://www.youtube.com/watch?v=2WNrx2jq184',
        vars    : 'vars',
        label   : 'add more...',
        class   : 'addmore',
        width   : 800,
        height  : 500,
        onClose : function(scope) {
            // onClose handler does nothing by default
        }
    },
    
    /**
     * initialize object
     */
    init: function(options) {
        var that = this;

        // set class parameters
        $.each(options, function(key,value) {
            that.options[key] = value;
        });

        // find all matching select elements
        $("select[rel*='" + that.options.rel + "']").each(function() {
            // add the magic option
            that.addOpenDialogOption(this);
        });
    },

    /**
     * extend the select element
     */
    addOpenDialogOption: function(element) {
        var that    = this;
        var e       = $(element);
        var s       = e.children().size();
        var class   = that.options.class;
        var label   = that.options.label;
        var vars    = that.options.vars;
        var url     = that.options.url;
        var width   = that.options.width;
        var height  = that.options.height;
        var onClose = that.options.onClose;

        // add a magic option to the end of the select element
        e.append('<option value="" class="' + class + '">' + label + '</option>');

        // and bind and onChange event
        e.bind('change', function() {
            // was our magic option selected?
            if (this.selectedIndex == s) {
                // yeah, launch the dialog associated with this select
                // note that HTML5 options are being used to make
                // the dialog integrate with the application!
                // @see http://www.w3schools.com/html5/tag_iframe.asp
                $('<iframe src="' + url + '?' + vars + '=' + e.attr(vars) + '" sanbox="allow-same-origin" seamless />').dialog({
                    title   : label,
                    autoOpen: true,
                    width   : width,
                    height  : height,
                    modal   : true,
                    buttons : {
                                Ok  : function() { $(this).dialog('close'); }
                              },
                    close   : function() {
                        onClose(this);
                    }
                }).width(width - 10).height(height)
            }
        })
    }
}