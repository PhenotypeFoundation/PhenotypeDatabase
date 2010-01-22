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
        wizard.html('<span style="color:red;font-size:8px;">Firefox 3.6 contains <a href="http://code.google.com/p/fbug/issues/detail?id=2746" target="_new">a bug</a> in combination with Firebug\'s XMLHttpRequest spy which causes the wizard to not function anymore. Please make sure you have firebug\'s XMLHttpRequest spy disabled use use Firefox 3.5.7 instead...</span>' + wizard.html())
    }

    // attach Tooltips
    attachHelpTooltips();
});

// attach help tooltips
function attachHelpTooltips() {
    // attach help action on all wizard help icons
    $('div#wizard').find('div.helpIcon').each(function() {
        $(this).qtip({
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
            content: $(this).parent().parent().find('div.helpContent').html(),
            show: 'mouseover',
            hide: 'mouseout'
        })
    });
}
