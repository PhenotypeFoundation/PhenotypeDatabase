/**
 * wizard javascript functions
 *
 * @author  Jeroen Wesbeek
 * @since   20100115
 * @package wizard
 * @see     dbnp.studycapturing.WizardTagLib
 * @see     dbnp.studycapturing.WizardController
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
$(document).ready(function() {
    re = /Firefox\/3\.6/gi;
    if (navigator.userAgent.match(re)) {
        // http://code.google.com/p/fbug/issues/detail?id=1899
        var wizard = $('div#wizard')
        wizard.html('<span style="color:red;font-size:8px;">You are using firefox 3.6, note that firefox 3.6 in combination with firebug (latest 1.6X.0a3) and XMLHttpRequest enabled in the console, break the workings of this wizard... Either disable console XMLHttpRequests, disable firebug altogether or downgrade to Firefox 3.5.7 instead</span>' + wizard.html())
    }

    // attach Tooltips
    attachHelpTooltips();
});

// attach help tooltips
function attachHelpTooltips() {
    // attach help action on all wizard help icons
    $('div#wizard').find('div.help').each(function() {
        $(this).find('div.icon').qtip({
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
                tip: true, // Give it a speech bubble tip with automatic corner detection
                name: 'blue' // Style it according to the preset 'cream' style
            },
            content: $(this).find('div.content').html(),
            show: 'mouseover',
            hide: 'mouseout'
        })
    })
}
