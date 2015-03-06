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
 * $Rev: 959 $
 * $Author: j.a.m.wesbeek@umail.leidenuniv.nl $
 * $Date: 2010-10-20 21:13:14 +0200 (Wed, 20 Oct 2010) $
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
        style   : 'addmore',
        width   : 800,
        height  : 400,
        position: 'center',
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
        var style   = that.options.style;
        var label   = that.options.label;
        var vars    = that.options.vars;
        var url     = that.options.url;
        var width   = that.options.width;
        var height  = that.options.height;
        var onClose = that.options.onClose;
        var position= that.options.position;

        // add a magic option to the end of the select element
        e.append('<option value="" class="' + style + '">' + label + '</option>');

		// when the select box size is changed after initialization, the initial change binding must be removed
		e.unbind('change');

        // and bind and onChange event
        e.bind('change', function() {
            // was our magic option selected?
            if (this.selectedIndex == s) {
                // yeah, launch the dialog associated with this select
                // note that HTML5 options are being used to make
                // the dialog integrate with the application!
                // @see http://www.w3schools.com/html5/tag_iframe.asp
                var arrVars = vars.split(",");
                var uri = url + '?'
                for (var v in arrVars) {
                    var val = e.attr(arrVars[v]);
                    uri += arrVars[v] + '=' + ((val) ? val : '') + '&';
                }

                $('<iframe frameborder="0" src="' + uri + '" seamless />').dialog({
                    title   : label,
                    autoOpen: true,
                    width   : width,
                    height  : height,
                    modal   : true,
                    position: position,
                    buttons : {
                                Close  : function() { $(this).dialog('close'); }
                              },
                    close   : function() {
                        onClose(this);
                    }
                }).width(width - 10).height(height)
            }
        })
    }
}