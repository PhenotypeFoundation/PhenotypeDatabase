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
 * new SelectAddMore().init('cars','http://my.site/addCars','myList',function(scope) { alert('cars closed : '+scope);});
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
    rel: 'myRel',
    url: 'http://www.youtube.com/watch?v=2WNrx2jq184',
    vars: 'myVar',
    width: 800,
    height: 500,
    onClose: function(scope) {
        alert('the dialog was closed! --> ' + scope)
    },
    
    /**
     * initialize object
     */
    init: function(rel, url, vars, onClose) {
        var that = this;

        // set class parameters
        that.rel = rel;
        that.url = url;
        that.vars = vars;
        that.onClose = onClose;

        // find all term elements
        $("select[rel*='" + that.rel + "']").each(function() {
            // add the magic option
            that.addTermEditorOption(this);
        });
    },

    /**
     * extend the select element
     */
    addTermEditorOption: function(element) {
        var that = this;
        var e = $(element);
        var s = e.children().size();

        // add a magic option to the end of the select element
        e.append('<option value="" class="addTerm">add more...</option>');

        // and bind to the onChange event
        e.bind('change', function() {
            // was our magic option selected?
            if (this.selectedIndex == s) {
                // yeah, launch the term / ontology editor dialog
                // note that HTML5 options are being used to make
                // the dialog integrate with the application!
                // @see http://www.w3schools.com/html5/tag_iframe.asp
                $('<iframe src="' + that.url + '?' + that.vars + '=' + e.attr(that.vars) + '" sanbox="allow-same-origin" seamless />').dialog({
                    title: 'Add more...',
                    autoOpen: true,
                    width: that.width,
                    height: that.height,
                    modal: true,
                    close: function() {
                        that.onClose(this);
                    }
                }).width(that.width - 10).height(that.height)
            }
        })
    }
}