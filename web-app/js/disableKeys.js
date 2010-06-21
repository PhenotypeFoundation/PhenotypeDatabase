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
});