// disable key presses
$(document).ready(function() {
    // disable enter key in input boxes to make sure
    // accidental submits do not happen
    $('input:text').each(function() {
        $(this).bind('keypress', function(e) {
            if (e.keyCode == 13) {
                return false;
            }
        });
    });

    // handle browser window events
    $(window).bind('keypress', function(e) {
        // disable going back by pressing escape
        if (e.keyCode == 8) return false;

        // disable reload by pressing F4
        if (e.keyCode == 115) return false;
    });
});